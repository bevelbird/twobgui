// SPDX-License-Identifier: GPL-3.0-or-later
// (Unofficial) GUI for the E-Stim 2B
// Copyright (C) 2019 bevelbird
package bevelbird.twob.ui;

import bevelbird.twob.*;
import bevelbird.twob.mode.ChannelDetails;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import static bevelbird.twob.ui.UIHelper.OFF_ICON_BASE64;
import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.NONE;

/**
 * (Unofficial) GUI for the E-Stim 2B.
 * Copyright (C) 2019 bevelbird
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
public class TwoBGui {

    private static final String APP_NAME = "2B Controller";

    private ModeManager modeManager = new ModeManager();

    private TwoB twoB = new TwoB(modeManager);

    private ChannelValue channelValueA;
    private ChannelValue channelValueB;
    private ChannelValue channelValueC;
    private ChannelValue channelValueD;

    private SelectionListValue<TwoBMode> modeValue;

    private SelectionListValue<String> powerValue;

    private SliderExtraLabel sliderExtraLabelC;
    private SliderExtraLabel sliderExtraLabelD;

    private boolean connected = false;

    public void show(String device, String fakeVersion) {
        twoB.setFakeVersion(fakeVersion);
        show(device);
    }

    public void show(String device) {
        String osName = System.getProperty("os.name");
        if (osName.startsWith("Mac OS X") || osName.startsWith("macOS")) {
            // Set application name
            System.setProperty("apple.awt.application.name", APP_NAME);
        }

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                if (twoB != null) {
                    twoB.disconnect();
                    System.out.println("Disconnected");
                }
            }
        });

        frame.setTitle(APP_NAME);
        frame.setSize(450, 585);
        frame.setResizable(false);
        frame.setLocation(50, 50);

        JPanel contentPane = new JPanel();
        contentPane.setBackground(Color.WHITE);
        contentPane.setLayout(new GridBagLayout());

        // Status line
        StatusValue statusValue = new StatusValue();

        BiFunction<Channel, Integer, Boolean> channelFn = (channel, value) -> {
            boolean success = twoB.setChannelOutput(channel, value);
            if (connected) {
                if (success) {
                    statusValue.setText(String.format("%s = %d", channel, value), StatusValue.Status.OK);
                } else {
                    statusValue.setText(String.format("%s = %d failed", channel, value), StatusValue.Status.ERROR);
                }
            }
            updateGui();

            return success;

        };

        channelValueA = new ChannelValue(Channel.A, channelFn) {
            public String getValue0Text() {
                return "off";
            }

            public String getValue100Text() {
                return "MAX";
            }
        };
        channelValueB = new ChannelValue(Channel.B, channelFn) {
            public String getValue0Text() {
                return "off";
            }

            public String getValue100Text() {
                return "MAX";
            }
        };

        channelValueC = new ChannelValue(Channel.C, channelFn);
        channelValueD = new ChannelValue(Channel.D, channelFn);

        sliderExtraLabelC = new SliderExtraLabel();
        sliderExtraLabelD = new SliderExtraLabel();

        TwoBMode[] modes = twoB.getAvailableModes().toArray(new TwoBMode[]{});
        modeValue = new SelectionListValue<>("mode", modes, mode -> {
            if (!connected) {
                return false;
            }

            boolean success = twoB.setMode(mode);
            if (success) {
                // Mode change changes channel A/B/C/D values
                updateGui(); // update values & reconfigure for new mode
                statusValue.setText(String.format("%s activated", mode), StatusValue.Status.OK);
            } else {
                statusValue.setText(String.format("Switch to %s failed", mode), StatusValue.Status.ERROR);
            }
            return success;
        });

        Function<String, Boolean> powerFn = p -> {
            if (!connected) {
                return false;
            }

            boolean succes;
            String powerStatusLog;

            switch (p) {
                default:
                case "Low":
                    powerStatusLog = "Set power to low";
                    succes = twoB.setPowerLow();
                    break;

                case "High":
                    powerStatusLog = "Set power to high";
                    succes = twoB.setPowerHigh();
                    break;
            }

            if (succes) {
                statusValue.setText(powerStatusLog, StatusValue.Status.OK);
            } else {
                statusValue.setText(powerStatusLog + " failed", StatusValue.Status.ERROR);
            }
            updateGui();

            return succes;
        };

        String[] powerSettings = {"Low", "High"};
        powerValue = new SelectionListValue<>("power setting", powerSettings, powerFn);

        String[] connectionSettings;
        if (device != null && !device.isEmpty()) {
            connectionSettings = new String[]{device};
        } else {
            // Try to autodetect the device
            connectionSettings = TwoBDevice.detectDevice().toArray(new String[]{});
        }

        SelectionListValue<String> connectionValue = new SelectionListValue<>("connection setting", connectionSettings);

        ActionListener offButtonListener = event -> {
            // Off/Kill is active regardless of the connection state (safety assumption)
            if (!twoB.kill()) {
                // Try to set A/B to 0
                channelValueA.setValue(0);
                channelValueB.setValue(0);

                if (connected) {
                    // must be set after channel.setValue to override channel status
                    statusValue.setText("Off command failed", StatusValue.Status.ERROR);
                }
            }
            updateGui();
        };

        TextValue versionValue = new TextValue();
        ActionListener connectButtonListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!connected) {
                    String device = connectionValue.getValue();

                    // Connect button should be disabled in this case, but nevertheless ...
                    if (device == null || device.isEmpty()) {
                        statusValue.setText("No 2B device available", StatusValue.Status.ERROR);
                        return;
                    }
                    twoB.setDevice(device);
                    // Try to connect to device
                    if (!twoB.connect()) {
                        statusValue.setText("Can not detect a 2B device", StatusValue.Status.ERROR);
                        return;
                    }

                    // Update available modes in GUI
                    modeValue.updateValues(twoB.getAvailableModes().toArray(new TwoBMode[]{}));
                    // --

                    TwoBState state = twoB.getState();
                    versionValue.setText(state.getVersion());
                    statusValue.setText("Connected", StatusValue.Status.OK);
                    connected = true;

                    updateGui();
                } else {
                    twoB.disconnect();
                    versionValue.setText("");
                    statusValue.setText("not connected", StatusValue.Status.INFO);
                    connected = false;
                }
            }
        };

        // Create GUI

        // Channel A Slider/Button/Value panel
        JPanel channelA = createSliderComponent("Channel A", channelValueA, null);
        UIHelper.addGB(contentPane, channelA, 0, 0, 2, 1, NONE, new Insets(10, 5, 5, 5));

        // Channel B Slider/Button/Value panel
        JPanel channelB = createSliderComponent("Channel B", channelValueB, null);
        UIHelper.addGB(contentPane, channelB, 0, 1, 2, 1, NONE, new Insets(5, 5, 5, 5));

        // Off panel
        JPanel offComponent = createOffComponent(offButtonListener);
        UIHelper.addGB(contentPane, offComponent, 0, 2, 1, 1, BOTH, new Insets(10, 5, 10, 5));
        // Mode/Power panel
        JPanel modePowerComponent = createModePowerComponent(modeValue, powerValue);
        UIHelper.addGB(contentPane, modePowerComponent, 1, 2, 1, 1, BOTH, new Insets(10, 5, 10, 5));

        // Channel C Slider/Button/Value panel
        JPanel channelC = createSliderComponent("Channel C (mostly Rate)", channelValueC, sliderExtraLabelC);
        UIHelper.addGB(contentPane, channelC, 0, 3, 2, 1, NONE, new Insets(5, 5, 5, 5));

        // Channel D Slider/Button/Value panel
        JPanel channelD = createSliderComponent("Channel D (mostly Feel)", channelValueD, sliderExtraLabelD);
        UIHelper.addGB(contentPane, channelD, 0, 4, 2, 1, NONE, new Insets(5, 5, 5, 5));

        // Connection/Connect panel
        JPanel connectComponent = createConnectionComponent(connectionValue, connectButtonListener, versionValue);
        UIHelper.addGB(contentPane, connectComponent, 0, 5, 2, 1, BOTH, new Insets(10, 5, 5, 5));

        // Status panel
        JPanel statusPanel = createStatusComponent(statusValue);
        UIHelper.addGB(contentPane, statusPanel, 0, 6, 2, 1, BOTH, new Insets(5, 5, 5, 5));


        frame.setContentPane(contentPane);

        updateGui();

        // Make GUI visible
        frame.setVisible(true);
    }

    private void updateGui() {
        TwoBState state = twoB.getState();

        // no need to send values to 2B, already handled internally by 2B due to mode change
        channelValueA.setGuiValue(state.getChannelValue(Channel.A));
        channelValueB.setGuiValue(state.getChannelValue(Channel.B));
        channelValueC.setGuiValue(state.getChannelValue(Channel.C));
        channelValueD.setGuiValue(state.getChannelValue(Channel.D));

        modeValue.setGuiValue(state.getMode());
        reconfigureGui(); // Change channel C/D visibility (depending on mode).

        switch (state.getPower()) {
            default:
            case "L":
                powerValue.setGuiValue("Low");
                break;

            case "H":
                powerValue.setGuiValue("High");
                break;
        }
    }

    /**
     * Change channel C/D visibility (depending on mode).
     */
    private void reconfigureGui() {
        TwoBMode mode = modeValue.getValue();

        ChannelDetails detailsC = mode.getChannelDetailsC();
        if (detailsC.isVisible()) {
            sliderExtraLabelC.setText(detailsC.getTitle(), detailsC.getLow(), detailsC.getHigh());
        } else {
            sliderExtraLabelC.hide();
        }

        ChannelDetails detailsD = mode.getChannelDetailsD();
        if (detailsD.isVisible()) {
            sliderExtraLabelD.setText(detailsD.getTitle(), detailsD.getLow(), detailsD.getHigh());
        } else {
            sliderExtraLabelD.hide();
        }
    }

    private JPanel createSliderComponent(String sliderTitle, ChannelValue valueManager, SliderExtraLabel sliderExtraLabel) {
        // This container panel prevents collapsing (due to BorderLayout handling) when inner panel is set to invisible
        JPanel containerPanel = new JPanel();
        containerPanel.setLayout(new GridLayout(1, 1));

        // Panel for Title/Slider/Buttons/Value
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        // panel.setBackground(Color.LIGHT_GRAY); //-- DEBUG
        panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        containerPanel.add(panel);

        // JLabel: Title
        JLabel title = new JLabel(sliderTitle, SwingConstants.LEFT);
        title.setForeground(Color.GRAY);
        // title.setBackground(Color.LIGHT_GRAY); //-- DEBUG
        // title.setBorder(BorderFactory.createLineBorder(Color.RED)); //-- DEBUG
        title.setOpaque(true);
        UIHelper.addGB(panel, title, 0, 0, 1, 1, BOTH, new Insets(2, 5, 6, 0));

        // Panel for Buttons/Value
        JPanel buttonValuePanel = new JPanel();
        buttonValuePanel.setLayout(new GridBagLayout());
        // buttonValuePanel.setBackground(Color.LIGHT_GRAY); //-- DEBUG
        // buttonValuePanel.setBorder(BorderFactory.createLineBorder(Color.RED)); //-- DEBUG

        // JLabel: Value
        JLabel value = new JLabel(valueManager.getValue0Text(), SwingConstants.CENTER);
        value.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        value.setBackground(Color.WHITE);
        value.setOpaque(true);
        value.setPreferredSize(new Dimension(49, 49));
        UIHelper.addGB(buttonValuePanel, value, 2, 1, 1, 2, GridBagConstraints.CENTER, new Insets(0, 0, 0, 8));

        // JSlider
        JSlider slider = new JSlider();
        slider.setPreferredSize(new Dimension(280, 40));
        // slider.setBorder(BorderFactory.createLineBorder(Color.RED)); //-- DEBUG

        slider.setMinimum(0);
        slider.setMaximum(100);

        slider.setMinorTickSpacing(5);
        slider.setMajorTickSpacing(25);
        slider.setPaintTicks(true);

        if (sliderExtraLabel == null) {
            // No extra labels, show 0/50/100 label below slider
            slider.setLabelTable(slider.createStandardLabels(50));
            slider.setPaintLabels(true);
        } else {
            // Slider with extra labels: do not show default labels below slider
            slider.setPaintLabels(false); // is default
        }
        slider.setValue(0);

        slider.addChangeListener(event -> {
            valueManager.valueChanged(slider.getValue());
            switch (slider.getValue()) {
                case 0:
                    value.setText(valueManager.getValue0Text());
                    break;

                case 100:
                    value.setText(valueManager.getValue100Text());
                    break;

                default:
                    value.setText(slider.getValue() + "");
                    break;
            }
        });


        if (sliderExtraLabel == null) {
            UIHelper.addGB(panel, slider, 0, 1, new Insets(0, 0, 5, 0));
        } else {
            // Create extra low/high labels above slider

            JPanel sliderPanel = new JPanel();
            sliderPanel.setLayout(new GridBagLayout());

            slider.setMinimumSize(new Dimension(280, 40));
            sliderPanel.setPreferredSize(new Dimension(slider.getPreferredSize().width, slider.getPreferredSize().height + 8));
            // sliderPanel.setBorder(BorderFactory.createLineBorder(Color.RED)); //-- DEBUG

            JLabel left = new JLabel("low", SwingConstants.LEFT);
            left.setForeground(Color.GRAY);
            // left.setBorder(BorderFactory.createLineBorder(Color.GREEN)); //-- DEBUG

            JLabel right = new JLabel("high", SwingConstants.RIGHT);
            right.setForeground(Color.GRAY);
            // right.setBorder(BorderFactory.createLineBorder(Color.BLUE)); //-- DEBUG

            // Register labels to enable dynamic change
            sliderExtraLabel.registerExtraLabel(panel, title, left, right);

            UIHelper.addGB(sliderPanel, left, 0, 0, 1, 1, BOTH, new Insets(0, 11, -8, 0));
            // center left out - UIHelper.addGB(sliderPanel, new JLabel(" ", SwingConstants.CENTER), 1, 0, 1, 1, BOTH)
            UIHelper.addGB(sliderPanel, right, 2, 0, 1, 1, BOTH, new Insets(0, 0, -8, 11));

            UIHelper.addGB(sliderPanel, slider, 0, 1, 3, 1);

            UIHelper.addGB(panel, sliderPanel, 0, 1, new Insets(0, 0, 5, 0));
        }
        valueManager.registerSlider(slider);

        // Plus Button
        JButton plus = new JButton("+");
        plus.addActionListener(event -> slider.setValue(slider.getValue() + 1));
        UIHelper.addGB(buttonValuePanel, plus, 1, 1, 1, 1);

        // Minus Button
        JButton minus = new JButton("-");
        minus.addActionListener(event -> slider.setValue(slider.getValue() - 1));
        UIHelper.addGB(buttonValuePanel, minus, 1, 2, 1, 1);

        // Add Button/Value panel to panel
        UIHelper.addGB(panel, buttonValuePanel, 1, 0, 1, 2);

        return containerPanel;
    }

    private JPanel createOffComponent(ActionListener listener) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        // panel.setBackground(Color.LIGHT_GRAY); //-- DEBUG
        panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        JButton offButton = new JButton(" OFF ");
        Icon offIcon = new ImageIcon(Base64.getDecoder().decode(OFF_ICON_BASE64));
        offButton.setIcon(offIcon);

        offButton.addActionListener(listener);

        offButton.setMargin(new Insets(15, 20, 15, 20));
        UIHelper.addGB(panel, offButton, 0, 0, 1, 2, BOTH, new Insets(4, 4, 4, 4));

        return panel;
    }

    private JPanel createModePowerComponent(SelectionListValue<TwoBMode> modeValueManager, SelectionListValue<String> powerValueManager) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        // panel.setBackground(Color.LIGHT_GRAY); //--
        panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        // Mode Chooser
        JPanel modeChooserPanel = new JPanel();
        modeChooserPanel.setLayout(new GridBagLayout());

        JLabel modeLabel = new JLabel("Program Mode", SwingConstants.LEFT);
        modeLabel.setForeground(Color.GRAY);
        // modeLabel.setBackground(Color.LIGHT_GRAY); //-- DEBUG
        // modeLabel.setBorder(BorderFactory.createLineBorder(Color.RED)); //-- DEBUG
        modeLabel.setOpaque(true);
        UIHelper.addGB(modeChooserPanel, modeLabel, 0, 0, 1, 1, BOTH, new Insets(2, 6, 4, 0));

        // Mode Chooser ComboBox
        JComboBox<TwoBMode> modeChooser = new JComboBox<>(modeValueManager.getAvailableValues());
        modeChooser.setPrototypeDisplayValue(new TwoBMode(-1, "CONTINUOUSXX", null, null));
        modeChooser.setMaximumRowCount(modeValueManager.getAvailableValues().length);
        // modeChooser.setBorder(BorderFactory.createLineBorder(Color.RED)); //-- DEBUG
        UIHelper.addGB(modeChooserPanel, modeChooser, 0, 1);

        modeChooser.addItemListener(itemEvent -> {
            if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
                modeValueManager.valueChanged((TwoBMode) modeChooser.getSelectedItem());
            }
        });
        modeValueManager.registerComboBox(modeChooser);

        UIHelper.addGB(panel, modeChooserPanel, 0, 0, new Insets(0, 0, 0, 10));

        // Power Setting
        JPanel powerSettingPanel = new JPanel();
        powerSettingPanel.setLayout(new GridBagLayout());

        JLabel powerLabel = new JLabel("Power", SwingConstants.LEFT);
        powerLabel.setForeground(Color.GRAY);
        // powerLabel.setBackground(Color.LIGHT_GRAY); //-- DEBUG
        // powerLabel.setBorder(BorderFactory.createLineBorder(Color.RED)); //-- DEBUG
        powerLabel.setOpaque(true);
        UIHelper.addGB(powerSettingPanel, powerLabel, 0, 0, 1, 1, BOTH, new Insets(2, 6, 4, 0));

        // Power Chooser
        JComboBox<String> powerChooser = new JComboBox<>(powerValueManager.getAvailableValues());
        powerChooser.setMaximumRowCount(powerValueManager.getAvailableValues().length);
        // powerChooser.setBorder(BorderFactory.createLineBorder(Color.RED)); //-- DEBUG
        UIHelper.addGB(powerSettingPanel, powerChooser, 0, 1);

        powerChooser.addItemListener(event -> powerValueManager.valueChanged((String) powerChooser.getSelectedItem()));
        powerValueManager.registerComboBox(powerChooser);


        UIHelper.addGB(panel, powerSettingPanel, 1, 0, new Insets(0, 0, 0, 10));

        return panel;
    }

    private JPanel createConnectionComponent(SelectionListValue<String> connectionValueManager, ActionListener
            connectButtonListener, TextValue versionValue) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        // panel.setBackground(Color.LIGHT_GRAY); //-- DEBUG
        panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        JLabel connectionLabel = new JLabel("Connection", SwingConstants.LEFT);
        connectionLabel.setForeground(Color.GRAY);
        // connectionLabel.setBackground(Color.LIGHT_GRAY); //-- DEBUG
        // connectionLabel.setBorder(BorderFactory.createLineBorder(Color.RED)); //-- DEBUG
        connectionLabel.setOpaque(true);
        UIHelper.addGB(panel, connectionLabel, 0, 0, 1, 1, BOTH, new Insets(2, 6, 4, 0));

        // Connection Chooser
        JPanel connectionChooserPanel = new JPanel();
        connectionChooserPanel.setLayout(new GridBagLayout());
        connectionChooserPanel.setPreferredSize(new Dimension(330, 30));
        // connectionChooserPanel.setBorder(BorderFactory.createLineBorder(Color.RED)); //-- DEBUG

        // Connection Chooser ComboBox
        JComboBox<String> connectionChooser = new JComboBox<>(connectionValueManager.getAvailableValues());
        connectionChooser.setPrototypeDisplayValue("tty.usbserial-FT9NJV2C-xx");
        connectionChooser.setMaximumRowCount(connectionValueManager.getAvailableValues().length);
        // connectionChooser.setBorder(BorderFactory.createLineBorder(Color.RED)); //-- DEBUG
        UIHelper.addGB(connectionChooserPanel, connectionChooser, 0, 0);

        connectionChooser.addItemListener(event -> connectionValueManager.valueChanged((String) connectionChooser.getSelectedItem()));
        connectionValueManager.registerComboBox(connectionChooser);

        JButton connectButton = new JButton("Connect");
        connectButton.setPreferredSize(new Dimension(100, 20));

        connectButton.addActionListener(connectButtonListener);

        if (connectionValueManager.getAvailableValues().length == 0) {
            // no entries to choose
            connectButton.setEnabled(false);
        }

        UIHelper.addGB(connectionChooserPanel, connectButton, 1, 0, 1, 1, BOTH);

        UIHelper.addGB(panel, connectionChooserPanel, 0, 1, 1, 1, BOTH);

        // Panel: Connected & Version
        JPanel versionPanel = new JPanel();
        versionPanel.setLayout(new GridBagLayout());
        versionPanel.setPreferredSize(new Dimension(80, 40));
        // versionPanel.setBorder(BorderFactory.createLineBorder(Color.RED)); //-- DEBUG

        JLabel connected = new JLabel("", SwingConstants.LEFT);
        connected.setForeground(new Color(0, 160, 0));
        // connected.setBackground(Color.LIGHT_GRAY); //--  DEBUG
        // connected.setBorder(BorderFactory.createLineBorder(Color.RED)); //-- DEBUG
        connected.setOpaque(true);
        UIHelper.addGB(versionPanel, connected, 0, 0, 1, 1, BOTH);

        JLabel version = new JLabel("", SwingConstants.LEFT);
        version.setForeground(Color.GRAY);
        // version.setBackground(Color.LIGHT_GRAY); //-- DEBUG
        // version.setBorder(BorderFactory.createLineBorder(Color.RED)); //-- DEBUG
        version.setOpaque(true);
        versionValue.registerCallback(text -> {
            version.setText(text);
            if (text.isEmpty()) {
                connected.setText("");
                connectButton.setText("Connect");
            } else {
                connected.setText("Connected");
                connectButton.setText("Disconnect");
            }
            return true;
        });

        UIHelper.addGB(versionPanel, version, 0, 1, 1, 1, BOTH);

        UIHelper.addGB(panel, versionPanel, 1, 0, 1, 2, BOTH);

        return panel;
    }

    private JPanel createStatusComponent(StatusValue statusValue) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        // panel.setBackground(Color.LIGHT_GRAY); //-- DEBUG
        panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        JLabel titleLabel = new JLabel("Status:", SwingConstants.LEFT);
        titleLabel.setForeground(Color.GRAY);
        // titleLabel.setBackground(Color.LIGHT_GRAY); //-- DEBUG
        // titleLabel.setBorder(BorderFactory.createLineBorder(Color.RED)); //-- DEBUG
        titleLabel.setOpaque(true);
        UIHelper.addGB(panel, titleLabel, 0, 0, 1, 1, BOTH, new Insets(4, 6, 4, 0));

        JLabel statusLabel = new JLabel("not connected", SwingConstants.LEFT);
        statusLabel.setPreferredSize(new Dimension(350, 15));
        statusLabel.setForeground(Color.LIGHT_GRAY);
        // statusLabel.setBackground(Color.LIGHT_GRAY); //-- DEBUG
        // statusLabel.setBorder(BorderFactory.createLineBorder(Color.RED)); //-- DEBUG
        statusLabel.setOpaque(true);
        statusValue.registerCallback((text, status) -> {
            switch (status) {
                default:
                case INFO:
                    statusLabel.setForeground(Color.LIGHT_GRAY);
                    break;

                case OK:
                    statusLabel.setForeground(new Color(0, 160, 0));
                    break;

                case ERROR:
                    statusLabel.setForeground(Color.RED);
                    break;

                case WARN:
                    statusLabel.setForeground(Color.PINK);
                    break;

            }
            statusLabel.setText(text);
            return true;
        });

        UIHelper.addGB(panel, statusLabel, 1, 0, 1, 1, BOTH, new Insets(4, 6, 4, 0));

        return panel;
    }

    public void addModeConfig(String filename) {
        modeManager.addExternalModeConfig(filename);
    }

    private static Map<String, String> parseCommandline(String[] args) {
        Map<String, String> cmds = new HashMap<>();

        int pos = 0;
        while (pos < args.length) {
            switch (args[pos]) {
                case "-device":
                    pos = parseOption(args, cmds, pos, "device");
                    break;

                case "-version":
                    pos = parseOption(args, cmds, pos, "version");
                    break;

                case "-modesfile":
                    pos = parseOption(args, cmds, pos, "modesfile");
                    break;

                default:
                    break;
            }
            pos++;
        }

        return cmds;
    }

    private static int parseOption(String[] args, Map<String, String> cmds, int pos, String option) {
        if (pos < args.length - 1) {
            if (!args[pos + 1].startsWith("-")) {
                cmds.put(option, args[pos + 1]);
            }
            pos++;
        }
        return pos;
    }

    public static void main(String[] args) {
        // twobgui (device is autodetected (macOS) or hardcoded (other))

        // twobgui <device>
        // or
        // twobgui -device <device>

        // twobgui -modesfile <mode-config-file.txt>
        // add an additional mode configuration (for different firmware)

        // twobgui -version <fake-version>
        // twobgui -version <fake-version> -modesfile <mode-config-file.txt>
        // twobgui -device <device> -version <fake-version> -modesfile <mode-config-file.txt>
        // by setting a (fake-)version you can dry-test the gui, "connect" always succeeds, device is ignored

        TwoBGui gui = new TwoBGui();
        if (args.length == 0) {
            gui.show(null);
        } else if (args.length == 1) {
            gui.show(args[0]);
        } else {
            Map<String, String> cmds = parseCommandline(args);
            cmds.entrySet().forEach(e -> System.out.println(e.getKey() + ": " + e.getValue()));

            if (cmds.containsKey("modesfile")) {
                gui.addModeConfig(cmds.get("modesfile"));
            }

            if (cmds.containsKey("version")) {
                gui.show("dummyUSB", cmds.get("version"));
            } else if (cmds.containsKey("device")) {
                gui.show(cmds.get("device"));
            } else {
                gui.show(null);
            }
        }
    }

}

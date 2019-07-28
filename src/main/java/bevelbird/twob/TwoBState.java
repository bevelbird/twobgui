// SPDX-License-Identifier: GPL-3.0-or-later
// (Unofficial) GUI for the E-Stim 2B
// Copyright (C) 2019 bevelbird
package bevelbird.twob;

public class TwoBState {

    private TwoBModeConfig modeConfig;
    private TwoBMode mode;
    private TwoBChannels twoBChannels;
    private int battery;
    private String power;
    private int joinedChannels;
    private String version = "n/a";

    public TwoBState(TwoBModeConfig modeConfig, TwoBMode mode, TwoBChannels twoBChannels, int battery, String power, int joinedChannels) {
        this.modeConfig = modeConfig;
        this.mode = mode;
        this.twoBChannels = twoBChannels;
        this.battery = battery;
        this.power = power;
        this.joinedChannels = joinedChannels;
        this.version = modeConfig.getVersion();
    }

    public void updateTwoBModeConfig(TwoBModeConfig modeConfig) {
        this.modeConfig = modeConfig;
        this.version = modeConfig.getVersion();
    }

    /**
     * Get the current value of the battery.
     */
    public int getBattery() {
        return battery;
    }

    /**
     * Get the channels (including channel value) of the device.
     */
    public TwoBChannels getTwoBChannels() {
        return twoBChannels;
    }

    public int getChannelValue(Channel channel) {
        switch (channel) {
            case A:
                return twoBChannels.A.getValue();

            case B:
                return twoBChannels.B.getValue();

            case C:
                return twoBChannels.C.getValue();

            case D:
                return twoBChannels.D.getValue();

            default:
                return -1;
        }
    }

    // TODO remove
    protected void setChannelValue(Channel channel, int value) {
        // empty
    }

    /**
     * Get the current mode of the device.
     */
    public TwoBMode getMode() {
        return mode;
    }

    /**
     * Get the current power mode.
     */
    public String getPower() {
        return power;
    }

    /**
     * Get the joined channel value (indicates if channel A and B are linked (1) or not (0)).
     */
    public int getJoinedChannels() {
        return joinedChannels;
    }

    /**
     * Get the current firmware version.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Parse the reply of the 2B. Format: battery:A:B:C:D:mode:power:join:version
     * <p>
     * Samples:
     * 540:0:0:100:100:0:L:0:2.106
     * 32772:0:0:100:100:0:L:0:2.116B
     *
     * @param reply the reply
     */
    public void parseReply(String reply) {
        String[] replyArray = reply.split(":");

        try {
            battery = Integer.parseInt(replyArray[0]);

            twoBChannels.A.setValue(Integer.parseInt(replyArray[1]) / 2);
            twoBChannels.B.setValue(Integer.parseInt(replyArray[2]) / 2);
            twoBChannels.C.setValue(Integer.parseInt(replyArray[3]) / 2);
            twoBChannels.D.setValue(Integer.parseInt(replyArray[4]) / 2);

            mode = modeConfig.convert(Integer.parseInt(replyArray[5]));
            power = replyArray[6];
            joinedChannels = Integer.parseInt(replyArray[7]);
            version = replyArray[8];
            if (TwoBDevice.DEBUG) System.out.println("State: " + this.toString());
        } catch (NumberFormatException e) {
            // ignore ill formatted response
            // no state value changed
        }
    }

    @Override
    public String toString() {
        return mode + ": " + twoBChannels + ", P: " + power;
    }
}


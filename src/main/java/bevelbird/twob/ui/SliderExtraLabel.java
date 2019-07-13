// SPDX-License-Identifier: GPL-3.0-or-later
// (Unofficial) GUI for the E-Stim 2B
// Copyright (C) 2019 bevelbird
package bevelbird.twob.ui;

import javax.swing.*;

public class SliderExtraLabel {

    private JPanel sliderPanel;

    private JLabel titleLabel;

    private JLabel lowLabel;

    private JLabel highLabel;

    public void registerExtraLabel(JPanel sliderPanel, JLabel titleLabel, JLabel lowLabel, JLabel highLabel) {
        this.sliderPanel = sliderPanel;
        this.titleLabel = titleLabel;
        this.lowLabel = lowLabel;
        this.highLabel = highLabel;
    }

    public void setText(String title, String low, String high) {
        sliderPanel.setVisible(true);
        titleLabel.setText(title);
        lowLabel.setText(low);
        highLabel.setText(high);
    }

    public void hide() {
        sliderPanel.setVisible(false);
    }
}

// SPDX-License-Identifier: GPL-3.0-or-later
// (Unofficial) GUI for the E-Stim 2B
// Copyright (C) 2019 bevelbird
package bevelbird.twob.ui;

import javax.swing.border.Border;
import java.awt.*;

import static java.awt.GridBagConstraints.NONE;

/**
 * Helper for various UI things.
 */
public class UIHelper {

    public static final String OFF_ICON_BASE64 = "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";

    // gridx = column
    // gridy = row
    public static void addGB(Container container, Component component, int gridx, int gridy) {
        addGB(container, component, gridx, gridy, 1, 1, NONE, 0.0, 0.0, GridBagConstraints.CENTER, new Insets(0, 0, 0, 0), 0, 0);
    }

    public static void addGB(Container container, Component component, int gridx, int gridy, Insets insets) {
        addGB(container, component, gridx, gridy, 1, 1, NONE, 0.0, 0.0, GridBagConstraints.CENTER, insets, 0, 0);
    }

    public static void addGB(Container container, Component component, int gridx, int gridy, int gridwidth, int gridheight) {
        addGB(container, component, gridx, gridy, gridwidth, gridheight, NONE, 0.0, 0.0, GridBagConstraints.CENTER, new Insets(0, 0, 0, 0), 0, 0);
    }

    public static void addGB(Container container, Component component, int gridx, int gridy, int gridwidth, int gridheight, int fill) {
        addGB(container, component, gridx, gridy, gridwidth, gridheight, fill, 0.0, 0.0, GridBagConstraints.CENTER, new Insets(0, 0, 0, 0), 0, 0);
    }

    public static void addGB(Container container, Component component, int gridx, int gridy, int gridwidth, int gridheight, int fill, Insets insets) {
        addGB(container, component, gridx, gridy, gridwidth, gridheight, fill, 0.0, 0.0, GridBagConstraints.CENTER, insets, 0, 0);
    }

    public static void addGB(Container container, Component component, int gridx, int gridy, int gridwidth, int gridheight, int fill, int anchor) {
        addGB(container, component, gridx, gridy, gridwidth, gridheight, fill, 0.0, 0.0, anchor, new Insets(0, 0, 0, 0), 0, 0);
    }

    private static void addGB(Container container, Component component, int gridx, int gridy, int gridwidth, int gridheight,
                              int fill, double weightx, double weighty, int anchor, Insets insets,
                              int ipadx, int ipady) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = gridx;
        constraints.gridy = gridy;
        constraints.gridwidth = gridwidth;
        constraints.gridheight = gridheight;
        constraints.fill = fill;
        constraints.weightx = weightx;
        constraints.weighty = weighty;
        constraints.anchor = anchor;
        constraints.insets = insets;
        constraints.ipadx = ipadx;
        constraints.ipady = ipady;
        container.add(component, constraints);
    }
}

// SPDX-License-Identifier: GPL-3.0-or-later
// (Unofficial) GUI for the E-Stim 2B
// Copyright (C) 2019 bevelbird
package bevelbird.twob;

public interface TwoBChannel {

    /**
     * Get the current value of the channel in percent.
     */
    public int getValue();

    /**
     * Set the current value of the channel in percent.
     *
     * @param value the value in percent.
     */
    void setValue(int value);
}

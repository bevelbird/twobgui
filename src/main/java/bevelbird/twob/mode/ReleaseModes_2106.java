// SPDX-License-Identifier: GPL-3.0-or-later
// (Unofficial) GUI for the E-Stim 2B
// Copyright (C) 2019 bevelbird
package bevelbird.twob.mode;

import bevelbird.twob.TwoBMode;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static bevelbird.twob.mode.ChannelDetails.*;

public enum ReleaseModes_2106 {

    PULSE(0, RATE_C_SLOW_FAST, FEEL_D_SMOOTH_HARD), // GUI: 1
    BOUNCE(1, RATE_C_SLOW_FAST, FEEL_D_SMOOTH_HARD), // GUI: 2
    CONTINUOUS(2, FEEL_C_SMOOTH_HARD, INVISIBLE),
    A_SPLIT(3, RATE_C_SLOW_FAST, FEEL_D_SMOOTH_HARD),
    B_SPLIT(4, RATE_C_SLOW_FAST, FEEL_D_SMOOTH_HARD),
    WAVE(5, FLOW_C_SLOW_FAST, GRANULARITY_D),
    WATERFALL(6, FLOW_C_SLOW_FAST, GRANULARITY_D),
    SQUEEZE(7, SPEED_C_FAST_SLOW, FEEL_D_SMOOTH_HARD),
    MILK(8, SPEED_C_FAST_SLOW, FEEL_D_SMOOTH_HARD),
    THROB(9, RANGE_C_FAST_SLOW, INVISIBLE),
    THRUST(10, RANGE_C_FAST_SLOW, INVISIBLE),
    RANDOM(11, RANGE_C_SHORT_LONG, FEEL_D_SMOOTH_HARD),
    STEP(12, new ChannelDetails("Step (C)", "Short", "Long"), FEEL_D_SMOOTH_HARD),
    TRAINING(13, new ChannelDetails("Step (C)", "Short", "Long"), FEEL_D_SMOOTH_HARD);

    public static final String VERSION = "2.106"; // 2.105 too

    private TwoBMode twoBMode;

    ReleaseModes_2106(int code, ChannelDetails channelC, ChannelDetails channelD) {
        this.twoBMode = new TwoBMode(code, this.name(), channelC, channelD);
    }

    public TwoBMode getTwoBMode() {
        return twoBMode;
    }

    /**
     * Get a list of available modes for firmware 2.106.
     */
    public static List<ReleaseModes_2106> getAvailableModes() {
        return Arrays.stream(ReleaseModes_2106.values()).collect(Collectors.toList());
    }
}

// SPDX-License-Identifier: GPL-3.0-or-later
// (Unofficial) GUI for the E-Stim 2B
// Copyright (C) 2019 bevelbird
package bevelbird.twob.mode;

import bevelbird.twob.TwoBMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static bevelbird.twob.mode.ChannelDetails.*;

public enum ReleaseModes_2105 {

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

    public static final String VERSION = "2.105"; // 2.106 too

    public static final String OPTIONS = ""; // none

    private TwoBMode twoBMode;

    ReleaseModes_2105(int code, ChannelDetails channelC, ChannelDetails channelD) {
        this.twoBMode = new TwoBMode(code, this.name(), channelC, channelD);
    }

    public TwoBMode getTwoBMode() {
        return twoBMode;
    }

    public static List<TwoBMode> getAvailableTwoBModes() {
        List<TwoBMode> modes = new ArrayList<>();
        for (ReleaseModes_2105 m : values()) {
            modes.add(m.getTwoBMode());
        }
        return modes;
    }
}

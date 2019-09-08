// SPDX-License-Identifier: GPL-3.0-or-later
// (Unofficial) GUI for the E-Stim 2B
// Copyright (C) 2019 bevelbird
package bevelbird.twob;

import bevelbird.twob.mode.ChannelDetails;

public class TwoBMode {

    public static final TwoBMode NONE = new TwoBMode(-1, "NONE", null, null);

    private final int code;

    private final String name;

    private final ChannelDetails channelC;

    private final ChannelDetails channelD;

    public TwoBMode(int code, String name, ChannelDetails channelC, ChannelDetails channelD) {
        this.code = code;
        this.name = name;
        this.channelC = channelC;
        this.channelD = channelD;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public ChannelDetails getChannelDetailsC() {
        return channelC != null ? channelC : ChannelDetails.DEFAULT_C;
    }

    public ChannelDetails getChannelDetailsD() {
        return channelD != null ? channelD : ChannelDetails.DEFAULT_D;
    }

    @Override
    public String toString() {
        return name;
    }

    public String toLongString() {
        return name + "[" + code + "] C" + getChannelDetailsC() + " - D" + getChannelDetailsD();
    }

}

// SPDX-License-Identifier: GPL-3.0-or-later
// (Unofficial) GUI for the E-Stim 2B
// Copyright (C) 2019 bevelbird
package bevelbird.twob.mode;

public class ChannelDetails {

    public static final String SLOW = "Slow";
    public static final String FAST = "Fast";

    public static final String SMOOTH = "Smooth";
    public static final String HARD = "Hard";

    public static final String SOFT = "Soft";
    public static final String HARSH = "Harsh";

    public static final String SHORT = "Short";
    public static final String LONG = "Long";

    public static final ChannelDetails INVISIBLE = new ChannelDetails();

    public static final ChannelDetails DEFAULT_C = new ChannelDetails("Channel C (mostly Rate)", " ", " ");
    public static final ChannelDetails DEFAULT_D = new ChannelDetails("Channel D (mostly Feel)", " ", " ");


    public static final ChannelDetails RATE_C_SLOW_FAST = new ChannelDetails("Rate (C)", SLOW, FAST);

    public static final ChannelDetails RANGE_C_FAST_SLOW = new ChannelDetails("Range (C)", FAST, SLOW);
    public static final ChannelDetails RANGE_C_SHORT_LONG = new ChannelDetails("Range (C)", SHORT, LONG);

    public static final ChannelDetails FEEL_C_SMOOTH_HARD = new ChannelDetails("Feel (C)", SMOOTH, HARD);

    public static final ChannelDetails FLOW_C_FAST_SLOW = new ChannelDetails("Flow (C)", FAST, SLOW);
    public static final ChannelDetails FLOW_C_SLOW_FAST = new ChannelDetails("Flow (C)", SLOW, FAST);

    public static final ChannelDetails SPEED_C_FAST_SLOW = new ChannelDetails("Speed (C)", FAST, SLOW);


    public static final ChannelDetails FEEL_D_SMOOTH_HARD = new ChannelDetails("Feel (D)", SMOOTH, HARD);
    public static final ChannelDetails FEEL_D_SOFT_HARSH = new ChannelDetails("Feel (D)", SOFT, HARSH);

    public static final ChannelDetails GRANULARITY_D = new ChannelDetails("Granularity (D)", SOFT, HARSH);

    // --

    private boolean visible;

    private String title;

    private String low;

    private String high;


    public ChannelDetails() {
        visible = false;
    }

    public ChannelDetails(String title, String low, String high) {
        visible = true;
        this.title = title;
        this.low = low;
        this.high = high;
    }

    public boolean isVisible() {
        return visible;
    }

    public String getTitle() {
        return title;
    }

    public String getLow() {
        return low;
    }

    public String getHigh() {
        return high;
    }
}

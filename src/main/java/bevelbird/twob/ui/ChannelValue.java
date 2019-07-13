// SPDX-License-Identifier: GPL-3.0-or-later
// (Unofficial) GUI for the E-Stim 2B
// Copyright (C) 2019 bevelbird
package bevelbird.twob.ui;

import bevelbird.twob.Channel;

import javax.swing.*;
import java.util.function.BiFunction;

public class ChannelValue {

    private final Channel channel;
    private final BiFunction<Channel, Integer, Boolean> fn;

    private JSlider slider;
    private int oldValue = 0;

    public ChannelValue(Channel channel) {
        this(channel, null);
    }

    public ChannelValue(Channel channel, BiFunction<Channel, Integer, Boolean> fn) {
        this.channel = channel;
        this.fn = fn;
    }

    public void registerSlider(JSlider slider) {
        this.slider = slider;
    }

    public void valueChanged(int value) {
        // System.out.println(String.format("called: %s -> %s (was %s)", channel, value + "", oldValue + "")); // + "" is nullsafe

        if (oldValue == value) {
            return;
        }

        boolean success = execute(value);
        if (success) {
            oldValue = value;
        } else {
            // try to restore slider to previous state
            // Warning: causes some kind of double execution if clicked on slider
            if (slider != null) {
                slider.setValue(oldValue);
            }
        }
    }

    public boolean execute(int value) {
        if (fn == null) {
            return true;
        }
        return fn.apply(channel, value);
    }

    public void setValue(int value) {
        // oldValue = value; //-- without oldValue = value the change event will be processed
        boolean success = execute(value);
        if (success && slider != null) {
            slider.setValue(value);
        }
    }

    public void setGuiValue(int value) {
        oldValue = value; // this prevents getting an additional change event
        if (slider != null) {
            slider.setValue(value);
        }
    }

    public String getValue0Text() {
        return "0";
    }

    public String getValue100Text() {
        return "100";
    }
}

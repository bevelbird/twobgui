// SPDX-License-Identifier: GPL-3.0-or-later
// (Unofficial) GUI for the E-Stim 2B
// Copyright (C) 2019 bevelbird
package bevelbird.twob.ui;

import java.util.function.BiFunction;

public class StatusValue {

    public enum Status {
        INFO, OK, ERROR, WARN
    }

    private BiFunction<String, Status, Boolean> fn;

    public void registerCallback(BiFunction<String, Status, Boolean> fn) {
        this.fn = fn;
    }

    public void setText(String text, Status status) {
        if (fn != null) {
            fn.apply(text, status);
        }
    }
}

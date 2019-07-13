// SPDX-License-Identifier: GPL-3.0-or-later
// (Unofficial) GUI for the E-Stim 2B
// Copyright (C) 2019 bevelbird
package bevelbird.twob.ui;

import java.util.function.Function;

public class TextValue {

    private Function<String, Boolean> fn;

    public void registerCallback(Function<String, Boolean> fn) {
        this.fn = fn;
    }

    public void setText(String text) {
        if (fn != null) {
            fn.apply(text);
        }
    }
}

// SPDX-License-Identifier: GPL-3.0-or-later
// (Unofficial) GUI for the E-Stim 2B
// Copyright (C) 2019 bevelbird
package bevelbird.twob.ui;

import javax.swing.*;
import java.util.Arrays;
import java.util.function.Function;

public class SelectionListValue<E> {

    private final String valueName;
    private E[] values;
    private final Function<E, Boolean> fn;

    private JComboBox<E> comboBox;
    private E oldValue = null;

    public SelectionListValue(String valueName, E[] values) {
        this(valueName, values, null);
    }

    public SelectionListValue(String valueName, E[] values, Function<E, Boolean> fn) {
        this.valueName = valueName;
        this.values = values;
        this.fn = fn;

        if (values != null && values.length != 0) {
            oldValue = values[0];
        }
    }

    public void registerComboBox(JComboBox<E> comboBox) {
        this.comboBox = comboBox;
    }

    public E[] getAvailableValues() {
        return values;
    }

    public void valueChanged(E value) {
        // System.out.println(String.format("called: %s -> %s (was %s)", valueName, value + "", oldValue + "")); // + "" is nullsafe

        if (oldValue == value) {
            return;
        }

        boolean success = execute(value);
        if (success) {
            oldValue = value;
            // LOG_OK System.out.println(String.format("%s -> %s", valueName, value + "")); // + "" is nullsafe
        } else {
            // LOG_FAIL System.out.println(String.format("%s -> %s failed", valueName, value + "")); // + "" is nullsafe

            // try to restore combobox to previous state
            // Warning: causes some kind of double execution
            if (comboBox != null) {
                // System.out.println("restore previous value");
                comboBox.setSelectedItem(oldValue);
            }
        }
    }

    public boolean execute(E value) {
        if (fn == null) {
            return true;
        }
        return fn.apply(value);
    }

    public void setValue(E value) {
        // oldValue = value; //-- without oldValue = value the change event will be processed
        if (comboBox != null) {
            comboBox.setSelectedItem(value);
        }
    }

    public void setGuiValue(E value) {
        oldValue = value; // this prevents getting an additional change event
        if (comboBox != null) {
            comboBox.setSelectedItem(value);
        }
    }

    public E getValue() {
        return oldValue;
    }

    public void updateValues(E[] values) {
        this.values = values;
        if (values != null && values.length != 0) {
            oldValue = values[0];

            if (comboBox != null) {
                comboBox.removeAllItems();
                Arrays.stream(values).forEach(e -> comboBox.addItem(e));
                comboBox.setMaximumRowCount(values.length);
                comboBox.setSelectedItem(oldValue);
            }
        }
   }
}
// SPDX-License-Identifier: GPL-3.0-or-later
// (Unofficial) GUI for the E-Stim 2B
// Copyright (C) 2019 bevelbird
package bevelbird.twob;

public class TwoBChannels {

    public final TwoBChannel A = new TwoBChannelImpl(Channel.A);
    public final TwoBChannel B = new TwoBChannelImpl(Channel.B);
    public final TwoBChannel C = new TwoBChannelImpl(Channel.C);
    public final TwoBChannel D = new TwoBChannelImpl(Channel.D);

    @Override
    public String toString() {
        return "A: " + A.getValue() +
                ", B: " + B.getValue() +
                ", C: " + C.getValue() +
                ", D: " + D.getValue();
    }

    class TwoBChannelImpl implements TwoBChannel {

        private String name;
        private int value;

        private TwoBChannelImpl(Channel channel) {
            this.name = channel.name();
        }

        @Override
        public int getValue() {
            return value;
        }

        @Override
        public void setValue(int value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}

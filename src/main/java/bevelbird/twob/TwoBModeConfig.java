// SPDX-License-Identifier: GPL-3.0-or-later
// (Unofficial) GUI for the E-Stim 2B
// Copyright (C) 2019 bevelbird
package bevelbird.twob;

import java.util.List;
import java.util.Optional;

public class TwoBModeConfig {

    private final String version;

    private final String options;

    private final List<TwoBMode> modes;

    public TwoBModeConfig(String version, String options, List<TwoBMode> modes) {
        this.version = version;
        this.options = options;
        this.modes = modes;
    }

    /**
     * Get a list of available modes for the 2B.
     */
    public List<TwoBMode> getAvailableModes() {
        return modes;
    }

    /**
     * Convert mode code to TwoBMode.
     *
     * @param modeCode the code if the mode
     * @return the TwoBMode or {@code null} if the code does not exist
     */
    public TwoBMode convert(int modeCode) {
        Optional<TwoBMode> mode = modes.stream().filter(m -> m.getCode() == modeCode).findFirst();
        return mode.orElse(null);
    }

    /**
     * Convert mode name to TwoBMode.
     *
     * @param modeName the mode name
     * @return the TwoBMode or {@code null} if mode id does not exist
     */
    public TwoBMode convert(String modeName) {
        Optional<TwoBMode> mode = modes.stream().filter(m -> m.getName().equals(modeName)).findFirst();
        return mode.orElse(null);
    }

    public String getVersion() {
        return version;
    }

    public String getOptions() {
        return options;
    }
}

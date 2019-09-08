// SPDX-License-Identifier: GPL-3.0-or-later
// (Unofficial) GUI for the E-Stim 2B
// Copyright (C) 2019 bevelbird
package bevelbird.twob;

import bevelbird.twob.mode.ReleaseModes_2105;

import java.util.HashMap;
import java.util.Map;

public class ModeManager {

    private TwoBModeConfig defaultConfig;

    private Map<String, TwoBModeConfig> configs = new HashMap<>();

    public ModeManager() {
        TwoBModeConfig buildin2105config = new TwoBModeConfig(
                ReleaseModes_2105.VERSION,
                ReleaseModes_2105.OPTIONS,
                ReleaseModes_2105.getAvailableTwoBModes());

        defaultConfig = buildin2105config;
        configs.put(buildin2105config.getVersion(), buildin2105config);

        ModeConfigReader modeConfigReader = new ModeConfigReader();
        TwoBModeConfig jar2106config = modeConfigReader.readFromJar("/release_2106.txt");
        if (jar2106config != null) {
            configs.put(jar2106config.getVersion(), jar2106config);
        }
    }

    public TwoBModeConfig getTwoBModeConfig(String version) {
        return configs.getOrDefault(version, defaultConfig);
    }

    public TwoBModeConfig getDefaultTwoBModeConfig() {
        return defaultConfig;
    }

    public void addExternalModeConfig(String filename) {
        ModeConfigReader modeConfigReader = new ModeConfigReader();
        TwoBModeConfig config = modeConfigReader.read(filename);
        if (config != null) {
            configs.put(config.getVersion(), config);
        }
    }
}

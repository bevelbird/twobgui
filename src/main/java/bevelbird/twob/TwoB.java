// SPDX-License-Identifier: GPL-3.0-or-later
// (Unofficial) GUI for the E-Stim 2B
// Copyright (C) 2019 bevelbird
package bevelbird.twob;

import java.util.List;

/**
 * (Unofficial) GUI for the E-Stim 2B.
 * Copyright (C) 2019 bevelbird
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
public class TwoB {

    private ModeManager modeManager;

    private TwoBDevice twoBDevice = new TwoBDevice("unknown"); // dummy

    // DEVICE FAKE - Version set via commandline to test release/beta switch
    private String fakeVersion;

    private TwoBModeConfig modeConfig;

    private TwoBState state;

    public TwoB(ModeManager modeManager) {
        this.modeManager = modeManager;

        // Initially use a mode configuration for a release firmware (defaults to release 2.106)
        modeConfig = modeManager.getDefaultTwoBModeConfig();

        // Pre-assign an intermediate state (without concrete version)
        state = new TwoBState(modeConfig, TwoBMode.NONE, new TwoBChannels(), -1, "n/a", -1);
    }

    public void setDevice(String device) {
        twoBDevice = new TwoBDevice(device);
    }

    // DEVICE FAKE
    public void setFakeVersion(String fakeVersion) {
        this.fakeVersion = fakeVersion;
    }

    /**
     * Connects to the 2B.
     *
     * @return {@code true} if connection is established
     */
    public boolean connect() {
        twoBDevice.setLogCommunication(false); // LOG_OFF

        boolean success = twoBDevice.connect();

        // Gets the TwoBState
        boolean nop = twoBDevice.sendNop(state);

        // Get a matching mode config for the device version
        modeConfig = modeManager.getTwoBModeConfig(state.getVersion());

        if (fakeVersion != null) {
            // DEVICE FAKE
            modeConfig = modeManager.getTwoBModeConfig(fakeVersion);
            // If no config for fake version is available we get the default as a fallback.
            // This default has a wrong version identifier.
            // We "correct" this to show the fake version instead:
            modeConfig = new TwoBModeConfig(fakeVersion, "", modeConfig.getAvailableModes());
        }

        // reconfigure the state with the mode config
        state.updateTwoBModeConfig(modeConfig);

        // if DEVICE FAKE, return true, too
        return (success && nop) || (fakeVersion != null);
    }

    /**
     * Disconnect from the 2B.
     */
    public boolean disconnect() {
        return twoBDevice.disconnect();
    }

    /**
     * Get the state of the 2B (from last command).
     */
    public TwoBState getState() {
        return state;
    }

    /**
     * Send command and update state.
     *
     * @param cmd the command
     * @return {@code true} if successful
     */
    private boolean sendCommand(String cmd) {
        return twoBDevice.sendCommand(cmd, state);
    }

    /**
     * Set the 2B to a specific mode.
     */
    public boolean setMode(TwoBMode mode) {
        return sendCommand("M" + mode.getCode());
    }

    /**
     * Set the channel output to value.
     */
    public boolean setChannelOutput(Channel channel, int value) {
        // A,B: 0 to 100
        // C,D: 2 to 100
        return sendCommand(channel.name() + value);
    }

    /**
     * Set the high power mode.
     */
    public boolean setPowerHigh() {
        return sendCommand("H");
    }

    /**
     * Set the low power mode.
     */
    public boolean setPowerLow() {
        return sendCommand("L");
    }

    /**
     * Kill all output (sets channels A and B to 0).
     */
    public boolean kill() {
        return sendCommand("K");
    }

    /**
     * Get a list of available modes for the 2B.
     */
    public List<TwoBMode> getAvailableModes() {
        return modeConfig.getAvailableModes();
    }
}

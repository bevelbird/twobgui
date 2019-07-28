// SPDX-License-Identifier: GPL-3.0-or-later
// (Unofficial) GUI for the E-Stim 2B
// Copyright (C) 2019 bevelbird
package bevelbird.twob;

import com.fazecast.jSerialComm.SerialPort;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
public class TwoBDevice {

    static final boolean DEBUG = false;

    private static final int BAUD_RATE = 9600;

    private static final String WINDOWS_DEVICE = "COM3";
    private static final String MACOS_DEVICE = "/dev/tty.usbserial-FT9NJV2C";
    private static final String LINUX_DEVICE = "/dev/ttyUSB0";

    private final String device;

    private SerialPort serialPort = null;
    private InputStream inStream = null;
    private OutputStream outStream = null;

    private boolean logCommunication = true;

    public TwoBDevice(String device) {
        this.device = device;
    }

    public static List<String> detectDevice() {
        List<String> devices = new ArrayList<>();

        String osName = System.getProperty("os.name");
        if (osName.startsWith("Windows")) {
            if (DEBUG) System.out.println("Windows detected");
            devices.add(WINDOWS_DEVICE); // Java9: List.of()
        } else if (osName.startsWith("Mac OS X") || osName.startsWith("macOS")) {
            devices = macOSReadDevices();
            if (DEBUG) System.out.println("macOS detected");
        } else {
            if (DEBUG) System.out.println("Linux detected");
            devices.add(LINUX_DEVICE);
        }

        return devices;
    }

    private static List<String> macOSReadDevices() {
        String dirName = "/dev";

        try {
            List<Path> paths = Files.list(new File(dirName).toPath())
                    .filter(p -> p.getFileName().toString().startsWith("tty."))
                    .filter(p -> !p.getFileName().toString().contains("Incoming"))
                    .limit(10)
                    .collect(Collectors.toList());
            return paths.stream()
                    .map(p -> p.getFileName().toString())//.substring(4)) // strip off tty.
                    .collect(Collectors.toList());
        } catch (IOException e) {
            // Java9: return List.of()
            List<String> fallBackList = new ArrayList<>();
            fallBackList.add(MACOS_DEVICE);
            return fallBackList;
        }
    }

    public void setLogCommunication(boolean logCommunication) {
        this.logCommunication = logCommunication;
    }

    /**
     * Initialize connection to 2B.
     *
     * @return {@code true} if connection is established
     */
    public boolean connect() {
        if (isConnected()) {
            return true;
        }

        serialPort = SerialPort.getCommPort(device);

        // Sets the SerialPort to the right state
        serialPort.setComPortParameters(BAUD_RATE, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING | SerialPort.TIMEOUT_WRITE_BLOCKING, 100, 0);
        serialPort.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);
        if (!serialPort.openPort()) {
            return false;
        }
        outStream = serialPort.getOutputStream();
        inStream = serialPort.getInputStream();

        return true;
    }

    /**
     * Return if 2B connection is established.
     *
     * @return {@code true} if connection is established
     */
    public boolean isConnected() {
        return serialPort != null && serialPort.isOpen() && outStream != null && inStream != null;
    }

    /**
     * Disconnect from the 2B.
     *
     * @return {@code true} if connection is closed
     */
    public boolean disconnect() {
        if (!isConnected()) {
            return true;
        }

        try {
            inStream.close();
            outStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        inStream = null;
        outStream = null;
        serialPort.closePort();
        serialPort = null;

        return true;
    }

    /**
     * Send a NO-OP (empty command) to the 2B to update the state. Communication logging is disabled.
     *
     * @param twoBState the state filled with the result of the command if return is {@code true}
     * @return {@code true} if send successful
     */
    public boolean sendNop(TwoBState twoBState) {
        boolean oldLogCommunication = logCommunication;
        logCommunication = false;

        boolean success = sendCommand("", twoBState);

        logCommunication = oldLogCommunication;
        return success;
    }

    /**
     * Send a command to the 2B.
     *
     * @param cmd       the command
     * @param twoBState the state filled with the result of the command if return is {@code true}
     * @return {@code true} if send successful
     */
    public boolean sendCommand(String cmd, TwoBState twoBState) {
        if (!isConnected()) {
            return false;
        }

        if (send(cmd)) {
            String reply = recv();

            if (logCommunication) {
                System.out.println("SEND '" + cmd + "' -> RECEIVED: " + reply);
            }

            // Update state
            twoBState.parseReply(reply);

            return true;
        }
        return false;
    }

    private boolean send(String cmd) {
        try {
            if (DEBUG) System.out.print("SENDING '" + cmd + "'");

            String msg2B = cmd + "\n\r"; // may only \r is ok, too
            outStream.write(msg2B.getBytes("UTF-8"));

            if (DEBUG) System.out.print(" ...");
            sleep(100);

            if (DEBUG) System.out.print(" ok");
        } catch (IOException e) {
            if (DEBUG) System.out.println();

            e.printStackTrace();
            return false;
        }
        return true;
    }

    private String recv() /* SerialPortTimeoutException */ {
        try {
            if (DEBUG) System.out.print("RECEIVE ...");

            byte[] b = new byte[1000];
            int bytesRead = inStream.read(b);
            // 541:40:0:40:40:2:L:0:2.106
            // 32772:0:0:100:100:0:L:0:2.116B
            // 49664:0:0:100:100:0:L:0:0:2.118B

            if (DEBUG) System.out.println(" (" + bytesRead + " bytes)");

            String reply = new String(b, 0, bytesRead, Charset.forName("US-ASCII"));

            // Strip leading bytes - reply ends with \n
            int end = reply.indexOf('\n');
            if (end != -1) {
                reply = reply.substring(0, end);
            }

            if (DEBUG) System.out.println(String.format("RECEIVED %s", reply));

            return reply;

        } catch (IOException e) {
            // SerialPortTimeoutException
            if (DEBUG) System.out.println();
            e.printStackTrace();
            throw new RuntimeException("I/O problem", e);
        }
    }

    private void sleep(long timeout) {
        try {
            TimeUnit.MILLISECONDS.sleep(timeout);
        } catch (InterruptedException e) {
            // Ignored
            // Restore interrupted state
            Thread.currentThread().interrupt();
        }
    }
}

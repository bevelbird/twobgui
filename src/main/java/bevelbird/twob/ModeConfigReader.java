// SPDX-License-Identifier: GPL-3.0-or-later
// (Unofficial) GUI for the E-Stim 2B
// Copyright (C) 2019 bevelbird
package bevelbird.twob;

import bevelbird.twob.mode.ChannelDetails;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static bevelbird.twob.mode.ChannelDetails.DEFAULT_C;
import static bevelbird.twob.mode.ChannelDetails.DEFAULT_D;

public class ModeConfigReader {

    public TwoBModeConfig read(String filename) {
        List<String> lines = readFile(filename);
        if (lines.size() <= 3) {
            // no modes found
            System.out.println("No modes found in " + filename + ". Too few lines");
            return null;
        }
        return read(lines, true);
    }

    public TwoBModeConfig readFromJar(String filename) {
        List<String> lines = readFileFromJar(filename);
        if (lines.size() <= 3) {
            // no modes found
            // since the file is built-in this is an internal error
            return null;
        }
        return read(lines, false);
    }

    public TwoBModeConfig read(List<String> lines, boolean verbose) {
        String version = lines.get(0);
        String options = lines.get(1);

        if (verbose) {
            System.out.println("version: " + version);
            System.out.println("options: " + options);
        }

        if ("none".equals(options)) {
            options = "";
        }

        List<String> modelines = lines.subList(2, lines.size());

        List<TwoBMode> modes = new ArrayList<>();

        modelines.forEach(line -> {
            TwoBMode twobMode = processModeline(line);
            if (twobMode != null) {
                modes.add(twobMode);
                if (verbose) {
                    System.out.println("  " + twobMode.toLongString());
                }
            }
        });

        return new TwoBModeConfig(version, options, modes);
    }

    private TwoBMode processModeline(String modeline) {
        String[] mode = modeline.split(";");
        if (mode.length != 4) {
            System.out.println("Mode line ignored: Not exactly 4 fields - " + modeline);
            return null;
        }
        String name = mode[0];
        int code;
        try {
            code = Integer.parseInt(mode[1]);
        } catch (NumberFormatException e) {
            System.out.println("Mode line ignored: Code not numeric: " + mode[1] + " - " + modeline);
            return null;
        }

        ChannelDetails channelC = getChannelDetails(mode[2], DEFAULT_C);
        ChannelDetails channelD = getChannelDetails(mode[3], DEFAULT_D);

        return new TwoBMode(code, name, channelC, channelD);
    }

    private ChannelDetails getChannelDetails(String fieldname, ChannelDetails defaultDetails) {
        if (fieldname.startsWith("\"") && fieldname.endsWith(("\""))) {
            String[] details = fieldname.substring(1, fieldname.length() - 1).split("\\|");
            if (details.length != 3) {
                System.out.println("Wrong format of channel details: " + fieldname);
                return defaultDetails;
            }
            return new ChannelDetails(details[0], details[1], details[2]);
        }
        try {
            Object o = ChannelDetails.class.getField(fieldname).get(null);
            if (o instanceof ChannelDetails) {
                return (ChannelDetails) o;
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return defaultDetails;
        }

        return defaultDetails;
    }

    private List<String> readFile(String filename) {
        try {
            File f = new File(filename);
            Path path = Paths.get(f.toURI());

            try (Stream<String> linesStream = Files.lines(path)) {

                List<String> lines = linesStream.map(String::trim)
                        .filter(s -> !s.isEmpty() && !s.startsWith("#"))
                        .collect(Collectors.toList());
                return lines;
            }
        } catch (IOException e) {
            System.out.println("Exception " + e.getClass().getSimpleName() + ": " + e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<String> readFileFromJar(String filename) {
        InputStream inputStream = TwoBMode.class.getResourceAsStream(filename);
        if (inputStream == null) {
            // since the file is built-in this is an internal error
            // System.out.println("File not found in jar: " + filename); //-
            return Collections.emptyList();
        }

        List<String> lines = new ArrayList<>();
        try {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty() && !line.startsWith("#")) {
                        lines.add(line);
                    }
                }
            }
            return lines;

        } catch (IOException e) {
            System.out.println("Exception " + e.getClass().getSimpleName() + ": " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public static void main(String[] args) {
        ModeConfigReader pt = new ModeConfigReader();

        String filename = "/release_2106.txt";
        pt.read(filename);
    }
}

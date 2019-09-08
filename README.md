# twobgui

(Unofficial) GUI for the E-Stim 2B

![Contributions welcome](https://img.shields.io/badge/contributions-welcome-brightgreen.svg)
[![License: GPL v3+](https://img.shields.io/badge/License-GPL%20v3%2B-blue.svg)](https://www.gnu.org/licenses/gpl-3.0) 

---

twobgui is an (unofficial) GUI for the E-Stim 2B.

**Note that this is alpha software, and thus comes with absolutely no warranty whatsoever. Use at your own risk.**

Copyright (C) 2019 bevelbird

---

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

# License

twobgui is licensed under the terms of the GNU General Public License version 3 or any later version.
See the file LICENSE or visit [https://www.gnu.org/licenses/gpl-3.0](https://www.gnu.org/licenses/gpl-3.0) for details.

# Acknowledgements

This project was inspired by
["Unofficial Python API for the E-stim 2B" (fredhatt/estim2bapi)](https://github.com/fredhatt/estim2bapi)
and ["EstimAPI for the 2B" (xman2B/EstimAPI)](https://github.com/xman2B/EstimAPI).

# Installation

## For users

...

## For developers

Clone this repository

    $ git clone https://github.com/bevelbird/twobgui

and build with gradle

    $ ./gradlew build

# Usage

Start the GUI: Device is autodetected (macOS) or hardcoded (other):

    $ java -jar twobgui.jar

Start the GUI and set a device (eg. `/dev/tty.xxx` or `COM3:`):

    $ java -jar twobgui.jar device
    
or (versions newer than 0.2-alpha)
    
    $ java -jar twobgui.jar -device <device>

#### Versions newer than 0.2-alpha

Start the GUI and add an additional mode configuration (for different firmware versions):

    $ java -jar twobgui.jar -modesfile <mode-config-file.txt>

Start the GUI with a fake firmware version. By setting a (fake-)version you can dry-test the gui,
"connect" always succeeds, the device is ignored:

    $ java -jar twobgui.jar -version <fake-version>
    $ java -jar twobgui.jar -version <fake-version> -modesfile <mode-config-file.txt>
    $ java -jar twobgui.jar -device <device> -version <fake-version> -modesfile <mode-config-file.txt>

#### Version 0.2-alpha

Start the GUI with a fake device and a device version. By setting a version you can dry-test the gui, "connect" always succeeds:

    $ java -jar twobgui.jar fake-device version

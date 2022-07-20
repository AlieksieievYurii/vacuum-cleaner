# Motherboard
This is the main component containing A1 module and Core. The board is responsible for hardware communication with all sensors and motors.

Folder structure:
* /circuit-pcb - there are PCB layers which are needed for making PCB.
* /src - source code of A1 module.
* /tests -  containing different sketches for calibration, testing etc.

## A1
A1 module is based on Arduino Mega 2660 microcontroller, and it controls the entire hardware. Source code you can find [here](https://github.com/AlieksieievYurii/vacuum-cleaner/tree/clean-up/motherboard/src).

## Core
The Core component is a brain of the robot. It is based on Raspberyy Pi Zero 2 W. It controls the logic and communication with smartphone. Source code you can find [here](https://github.com/AlieksieievYurii/vacuum-cleaner/tree/main/core).

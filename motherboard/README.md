# Motherboard
This is the main component containing A1 module and Core. The board is responsible for hardware communication with all sensors and motors.

Folder structure:
* /circuit-pcb - there are Motherboard PCB layers which are needed for making PCB.
* /a1 - source code of A1 module.
* /core - source code of the Core.

## A1
A1 module is based on Arduino Mega 2660 microcontroller, and it controls the entire hardware. Source code you can find [here](https://github.com/AlieksieievYurii/vacuum-cleaner/tree/clean-up/motherboard/a1).

## Core
The Core component is a brain of the robot. It is based on Raspberyy Pi Zero 2 W. It controls the logic and communication with smartphone. Source code you can find [here](https://github.com/AlieksieievYurii/vacuum-cleaner/tree/clean-up/motherboard/core).

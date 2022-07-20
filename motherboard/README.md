# Mother Board
This is the main compoment containing A1 module and Core. The board is responsible for hardware commutication with all sensors and motors.

Folder structure:
* /circuit-pcb - there are PCB layers which are needed for making PCB.
* /src - source code of A1 module
* /tests - contaning different sketches for callibration, testing etc

## A1
A1 module is based on Arduino Mega 2660 microcontroller and it controlls the entire hardware. Source code you can find [here](https://github.com/AlieksieievYurii/vacuum-cleaner/tree/clean-up/motherboard/src).

## Core
Core component is a brain of the robot. It is based on Raspberyy Pi Zero 2 W. It controlls the logic and communication with smartphone. Source code you can find [here](https://github.com/AlieksieievYurii/vacuum-cleaner/tree/main/core).

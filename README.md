# vacuum-cleanervacuum-cleaner
The main aim of the project is to try to create a good DIY vacuum cleaner robot.

A few days ago, while I was sweeping the apartment, I started thinking of automatization becuase this process takes too much time in spite of my little apartment. There are many
vacuum cleaner robots, like Xiaomi. I thought about purchasing one. However, I would like to try to build my own :)

This project is about how to create a simple vacuum cleaner robot.

## Requirements
* The robot must be a rounded shape
* [TBD] The robot must be with the following sizes. H.Max - 100 mm, L.Max - 345 mm, W.Max - 345 mm
* Must be able to connect to Wifi
* Must be able to be configurable(e.g set up Wifi credentials) bia Bluetooth

## Stuff
* Raspberry Pi Zero W
* Arduino Mega
* 3D printing
* Android Mobile Phone

# Preparation
## Iteration 0:
### Purpose:
This iteration is about preparing environments, including:
- [X] Install Fusion 360
- [X] Install Arduino IDEA
- [X] Install Cura
- [X] Get initial experiance with Fusion 360
- [X] Get initial experiance with Cura & 3D printing
- [X] Buy & assemble 3D printer. ([Ender 3 Pro](https://www.creality3dofficial.com/products/creality-ender-3-pro-3d-printer))
- [X] Print some 3D models to get experience with it
- [X] Run any test program on Arduino Mega

## Iteration 1:
### Purpose:
This iteration is about buying\getting initial stuff, including:
- [X] Purchase Raspberry Pi Zero W
- [X] Buy a mottor
- [x] Install OS on Rasperry Pi
- [x] Connect Wifi on Raspberry Pi
- [x] Connect to Raspberry Pi via SHH
- [X] Set up a remotable Python Interpreter in PyCharm. (This is imposible) Come up with a workaround / e.g create python script
- [x] Create script that automatically enable pairing and discovering process on the Pi
- [x] Try to connect Raspberty Pi to Android smartphone via Bluetooth
- [x] Create a standarazied communication between Android App and Raspberry Pi via Bluetooth. [Link](https://github.com/AlieksieievYurii/vacuum-cleaner/blob/main/docs/bluetooth_communication.md)
- [x] Create a Python Service and Android Service to communicate via BL
- [x] Implement setting up Wifi connection via BL communication

## Iteration 2:
- [ ] Build A1 based on Arduino Mega so that it sends information(values from sensors) and accept some action commands
- [ ] Measure Voltage
- [ ] Measure Current
- [ ] Connect Mosfet

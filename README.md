# Vacuum Cleaner Robot 
The main aim of the project is to try to create a good DIY vacuum cleaner robot.

A few days ago, while I was sweeping the apartment, I started thinking of automatization because this process takes too much time in spite of my little apartment. There are many vacuum cleaner robots, like Xiaomi. I thought about purchasing one. However, I would like to try to build my own :)

This project is about how to create a simple vacuum cleaner robot. The following stuff is using:
* Arduino Mega 2560 - this part is called A1. It is responsible for hardware operations e.g managing motors, reading sensors etc.
* Arduino Nano - is responsible for power control.
* Raspberry Pi Zero W - core. It communicates with A1 module and sends instructions what to do. Basically, the core is a brain. Moreover, it is responsible for Bluetooth and Wi-Fi connection.
* Android Native App (SDK level >= 22) - is responsible for different configurations, analyzing, manual controlling etc.
* 3D printing - the entire model is printed by 3D printer with PLA plastic. [Fusion 360](https://www.autodesk.com/products/fusion-360/overview) is used for parametric modeling and [Cura](https://ultimaker.com/software/ultimaker-cura) for slicing.

## Overview
* The robot is a rounded shape with the following sizes:
* Weight: 3 KG
* Brushes: 2 side brushed and one main.
* Working time: approximately 30 minutes
* Wireless connection: Wifi and Bluetooth

## Architecture 
![image](https://user-images.githubusercontent.com/39415360/179946342-a393f9e0-408d-4760-a70b-cfd9a5e10395.png)

Generally, the robot's electronic part consists of two PCB: [motherboard](https://github.com/AlieksieievYurii/vacuum-cleaner/tree/clean-up/motherboard) and [power controller](https://github.com/AlieksieievYurii/vacuum-cleaner/tree/main/power-controller). Power Controller is responsible for switching on and off the power by pushing button or pragmatically e.g. by mobile phone or after the cleaning. Also, the controller is responsible for safety shutting down (when a user wants to shut down the robot by pushing the button, firstly it waits for shutting down the core and afterwards it cuts the power). Motherboard consists of two major units: [A1](https://github.com/AlieksieievYurii/vacuum-cleaner/tree/clean-up/motherboard/a1) and [Core](https://github.com/AlieksieievYurii/vacuum-cleaner/tree/clean-up/motherboard/core). Technically, A1 is an electronic interface between the Core and ECUs. It excepts instructions to execute, e.g. move forward on 50 CM or turn on the vacuum motor. Moreover, it reads the sensors and sends their states to the Core immediately. The Core itself is a brain that controls the robot's behavior, e.g. where to move, have to react when the robot faces obstacles etc. Also, the Core is responsible for Bluetooth and Wi-Fi communication with smartphone. Bluetooth's communication is needed for initial configuration of the robot, for example, providing Wi-Fi credentials. Wi-Fi communication is needed for manual controlling, review history of cleaning etc.

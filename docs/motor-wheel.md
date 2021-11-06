# Wheel motor
This document describes the integration of wheel motor. The module from Xiaomi Vacuum cleaner was chosen because it perfectly suits for the current purpose.

The motor wheel are from XIAOMI MIJIA STYTJ02HZM 1T.
![image](https://user-images.githubusercontent.com/39415360/140542558-f65f8d80-ed7f-4747-baa6-fe30600e59d4.png)


Little reverse enginering has been done. The following input pins are revealed:
![image](https://user-images.githubusercontent.com/39415360/140587418-a47e9df6-952d-4b95-ab0b-c5e2ae16635b.png)

**MA** and **MB** - are wires connected to DC mottor.</br>
**Vcc** - is connected to bouth hall sensors.</br>
**GND** - is connected to bouth hall sensors.</br>
**H1** - first hall sensor.</br>
**H2** - second hall sensor.</br>

A few experiments have been done to figure out the suitable voltages(motor and hall sensors) for the wheel module.</br>
The hall sensors(Vcc) - *3.3..5 V*</br>
The motor - *3..8 V* -> 40 mA - 100 mA</br>

1. Add info about pins
2. Info about voltage/current

https://automaticaddison.com/how-to-calculate-the-velocity-of-a-dc-motor-with-encoder/

https://automaticaddison.com/calculate-pulses-per-revolution-for-a-dc-motor-with-encoder/

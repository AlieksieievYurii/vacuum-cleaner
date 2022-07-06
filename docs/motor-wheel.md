# Wheel motor
This document describes the integration of wheel motor. The module from Xiaomi Vacuum cleaner was chosen because it perfectly suits for the current purpose.

The motor wheel are from XIAOMI MIJIA STYTJ02HZM 1T.
![image](https://user-images.githubusercontent.com/39415360/140542558-f65f8d80-ed7f-4747-baa6-fe30600e59d4.png)


Little reverse enginering has been done. The following input pins are revealed:
![image](https://user-images.githubusercontent.com/39415360/140587418-a47e9df6-952d-4b95-ab0b-c5e2ae16635b.png)

**MA** and **MB** - are wires connected to DC mottor.</br>
**Vcc** - is connected to bouth hall sensors.</br>
**GND** - is connected to bouth hall sensors.</br>
**H1** and **H2** - first and second hall sensors. Together works as an encoder, so the direction and speed can be measured.</br>

A few experiments have been done to figure out the suitable voltages(motor and hall sensors) for the wheel module.</br>
The hall sensors(Vcc) - *3.3..5 V*</br>
The motor - *3..8 V* -> 40 mA - 100 mA</br>

## Integration with Arduino
This section describes how to connect the wheel module to Arduino in order to measure rotation velocity(RPM). As an example Arduino Nano has been chosen, so connection should be as follows:</br>
**Vcc** - to 5v of Arduino.</br>
**GND** - it is obvious.</br>
**H1** - to pin 3.</br>
**H2** - to pin 2.</br>

The following code is used to measure number of pulses in one 360 rotation:

``` 
#define HALL_ENCODER_H2 2
// Keeps how many pulses are done during one INTERVAL
volatile long wheel_pulse_count = 0;
void setup() {
  Serial.begin(9600);
  pinMode(HALL_ENCODER_H2, INPUT);
  attachInterrupt(digitalPinToInterrupt(HALL_ENCODER_H2), right_wheel_pulse, RISING);
}
void loop() {
 Serial.print("Pulses: "); Serial.println(wheel_pulse_count);
}
void right_wheel_pulse() {
  wheel_pulse_count++;
}
```
A few measurements showed that in this Wheel Module, full rotation equals `237` pulses! This value is needed to calculate RPM. The following script measures the speed and direction:

``` 
#define INTERVAL 1000

// How many pulses in one 360 rotation, must be measured manually!!!!
#define ENC_COUNT_REV 237

// Distance made by a full rotation. Diameter of the wheel: 67.5 mm -> Pi * 67.5 => 212 mm
#define FULL_ROTATION_DISTANCE 212 

#define HALL_ENCODER_H1 3
#define HALL_ENCODER_H2 2

// True = Forward; False = Reverse
boolean forwad_direction = true;

// Keeps how many pulses are done during one INTERVAL
volatile long wheel_pulse_count = 0;

void setup() {
  Serial.begin(9600);

  pinMode(HALL_ENCODER_H1, INPUT);
  pinMode(HALL_ENCODER_H2, INPUT);

  attachInterrupt(digitalPinToInterrupt(HALL_ENCODER_H2), right_wheel_pulse, RISING);
}

void loop() {
  static uint32_t previous_milis = 0;
  const uint32_t currentMillis = millis();

  if (currentMillis - previous_milis > INTERVAL) {
    previous_milis = currentMillis;
    calculate_and_print_measurements();
  }
}

void calculate_and_print_measurements() {
  const float rpm_right = (float)(wheel_pulse_count * 60 / ENC_COUNT_REV);
  const float distance = rpm_right * FULL_ROTATION_DISTANCE;
  
  Serial.print("Pulses: "); Serial.println(wheel_pulse_count);
  Serial.print("Speed: "); Serial.print(rpm_right); Serial.println(" RPM");
  Serial.print("Distance: ");Serial.print(distance);Serial.println(" mm");
  Serial.print("Forwad: "); Serial.println(forwad_direction);
  Serial.println();

  wheel_pulse_count = 0;
}

void right_wheel_pulse() {
  forwad_direction = digitalRead(HALL_ENCODER_H1) == LOW ? true : false;
  wheel_pulse_count++;
}
```

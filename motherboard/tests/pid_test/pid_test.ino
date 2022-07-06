#include "utils.h"
#include "Adafruit_SH1106.h"

#define MOTOR 7
#define SPEED_SENSOR 2

// How many pulses in one 360 rotation, must be measured manually!!!!
#define ENC_COUNT_REV 237

// Distance made by a full rotation. Diameter of the wheel: 67.5 mm -> Pi * 67.5 => 212 mm
#define FULL_ROTATION_DISTANCE 212

#define CALL_INTERVAL 100
#define CALL_INTERVAL_IS_SECONDS 0.1

volatile uint32_t _wheel_pulses_for_speed = 0;
volatile uint32_t speed_sm_per_minute;

volatile float kp = 0, ki = 0, kd = 0, integral = 0, prevErr = 0;
volatile uint32_t set_point_sm_per_minute = 0;
volatile uint8_t motor_signal = 0;

#define OLED_RESET 13
Adafruit_SH1106 display(OLED_RESET);

void setup() {
  pinMode(MOTOR, OUTPUT);
  pinMode(SPEED_SENSOR, INPUT);
  Serial.begin(9600);
  attachInterrupt(digitalPinToInterrupt(SPEED_SENSOR), []() {
    _wheel_pulses_for_speed++;
  }, RISING);

  enable_Timer5(10, CHANNEL_A);

  Serial.println("speed:,setpoint:,signal");

  display.begin(SH1106_SWITCHCAPVCC, 0x3C);
  display.clearDisplay();
}

void parse_input() {
  if (Serial.available() > 0) {
    switch (Serial.read()) {
      case 'p': kp = Serial.parseFloat(); break;
      case 'i': ki = Serial.parseFloat(); break;
      case 'd': kd = Serial.parseFloat(); break;
      case 's': set_point_sm_per_minute = Serial.parseInt(); break;
      default: Serial.println("ERROR");
    }
  }
}

void display_info() {
  display.clearDisplay();
  display.setTextSize(1);
  display.setTextColor(WHITE);

  //==== Display KP ====
  display.setCursor(0, 20);
  display.print("P: ");
  display.print(kp);
  //===================

  //==== Display KI ====
  display.setCursor(0, 30);
  display.print("I: ");
  display.print(ki);
  //===================

  //==== Display KD ====
  display.setCursor(0, 40);
  display.print("D: ");
  display.print(kd);
  //===================

  //==== Display Current Speed ====
  display.setCursor(0, 50);
  display.print("SPEED: ");
  display.print(speed_sm_per_minute);
  //===================

  //==== Display Set Point Speed ====
  display.setCursor(0, 0);
  display.print("SP: ");
  display.print(set_point_sm_per_minute);
  //===================

  //==== Display Current Motor Signal ====
  display.setCursor(80, 00);
  display.print("S: ");
  display.print(motor_signal);
  //===================
  
  display.display();
}

void loop() {
  display_info();
  Serial.print(set_point_sm_per_minute);
  Serial.print(",");
  Serial.print(speed_sm_per_minute);
  Serial.print(",");
  Serial.print(motor_signal);
  Serial.println();
  parse_input();
}

int computePID(float input, float setpoint, float kp, float ki, float kd, float dt, int minOut, int maxOut) {
  float err = setpoint - input;
  static float integral = 0, prevErr = 0;
  integral = constrain(integral + (float)err * dt * ki, minOut, maxOut);
  float D = (err - prevErr) / dt;
  prevErr = err;
  return constrain(err * kp + integral + D * kd, minOut, maxOut);
}

void measure_pid() {
  motor_signal = computePID(speed_sm_per_minute, set_point_sm_per_minute, kp, ki, kd, 0.1, 0, 255);
  analogWrite(MOTOR, motor_signal);
}

void measure_speed() {
  const float rpm = (float)((_wheel_pulses_for_speed * (60000 / CALL_INTERVAL)) / ENC_COUNT_REV);
  speed_sm_per_minute = rpm * FULL_ROTATION_DISTANCE / 10;
  _wheel_pulses_for_speed = 0;
}

ISR(TIMER5_A) {
  measure_speed();
  measure_pid();
}

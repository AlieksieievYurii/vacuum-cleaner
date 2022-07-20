#ifndef __CONFIG__
#define __CONFIG__

#define LEFT_END 48
#define RIGHT_END 49

#define DUST_BOX_END 47

#define BUT_UP 28
#define BUT_OK 27
#define BUT_DOWN 26

#define LED_WIFI 22
#define LED_ERR 23
#define LED_ST 13

#define LED_BL_RED 44
#define LED_BL_GREEN 45
#define BUT_BL 43

#define LID_END 42

//======Define Wheel Motors====
#define RIGHT_FORWARD 5
#define RIGHT_BACKWARD 4
#define RIGHT_WHEEL_SPEED_SENSOR 3
#define RIGHT_WHEEL_DIRECTION_SENSOR 24

#define LEFT_FORWARD 7
#define LEFT_BACKWARD 6
#define LEFT_WHEEL_SPEED_SENSOR 2
#define LEFT_WHEEL_DIRECTION_SENSOR 25 
//=============================

#define BUZZER 46

#define VACUUM_MOTOR 10
#define LEFT_BRUSH_MOTOR 9
#define RIGHT_BRUSH_MOTOR 8

//=======Range Finder Pins=========
#define LEFT_RF_TRIG 32
#define LEFT_RF_ECHO 29

#define CENTER_RF_TRIG 33
#define CENTER_RF_ECHO 30


#define RIGHT_RF_TRIG 34
#define RIGHT_RF_ECHO 31
//=================================


//=========Battery Cells===========
#define CELL_A A3
#define CELL_B A2
#define CELL_C A1
#define CELL_D A0
//=================================

#define TEMP_HUMIDITY_SENSOR 35

//=========Cliff ends==============
#define FRONT_LEFT_CLIFF 36
#define FRONT_CENTER_CLIFF 37
#define FRONT_RIGHT_CLIFF 38
#define BACK_LEFT_CLIFF 40
#define BACK_CENTER_CLIFF 39
#define BACK_RIGHT_CLIFF 41

void init_hardware(void) {
  pinMode(LEFT_END, INPUT_PULLUP);
  pinMode(RIGHT_END, INPUT_PULLUP);
  pinMode(DUST_BOX_END, INPUT_PULLUP);
  pinMode(BUT_UP, INPUT_PULLUP);
  pinMode(BUT_OK, INPUT_PULLUP);
  pinMode(BUT_DOWN, INPUT_PULLUP);
  pinMode(LED_WIFI, OUTPUT);
  pinMode(LED_ERR, OUTPUT);
  pinMode(LED_ST, OUTPUT);
  pinMode(LED_BL_RED,OUTPUT);
  pinMode(LED_BL_GREEN,OUTPUT);
  pinMode(BUT_BL, INPUT_PULLUP);
  pinMode(LID_END, INPUT_PULLUP);
  pinMode(RIGHT_WHEEL_SPEED_SENSOR, INPUT);
  pinMode(RIGHT_WHEEL_DIRECTION_SENSOR, INPUT);
  pinMode(LEFT_WHEEL_SPEED_SENSOR, INPUT);
  pinMode(LEFT_WHEEL_DIRECTION_SENSOR, INPUT);
  pinMode(RIGHT_FORWARD, OUTPUT);
  pinMode(RIGHT_BACKWARD, OUTPUT);
  pinMode(LEFT_FORWARD, OUTPUT);
  pinMode(LEFT_BACKWARD, OUTPUT);
  pinMode(BUZZER, OUTPUT);
  pinMode(VACUUM_MOTOR, OUTPUT);
  pinMode(LEFT_BRUSH_MOTOR, OUTPUT);
  pinMode(RIGHT_BRUSH_MOTOR, OUTPUT);
  pinMode(LEFT_RF_TRIG, OUTPUT);
  pinMode(LEFT_RF_ECHO, INPUT);
  pinMode(CENTER_RF_TRIG, OUTPUT);
  pinMode(CENTER_RF_ECHO, INPUT);
  pinMode(RIGHT_RF_TRIG, OUTPUT);
  pinMode(RIGHT_RF_ECHO, INPUT);
  pinMode(CELL_A, INPUT);
  pinMode(CELL_B, INPUT);
  pinMode(CELL_C, INPUT);
  pinMode(CELL_D, INPUT);
  pinMode(TEMP_HUMIDITY_SENSOR, INPUT_PULLUP);
  pinMode(BACK_RIGHT_CLIFF, INPUT);
  pinMode(BACK_CENTER_CLIFF, INPUT);
  pinMode(BACK_LEFT_CLIFF, INPUT);
  pinMode(FRONT_RIGHT_CLIFF, INPUT);
  pinMode(FRONT_CENTER_CLIFF, INPUT);
  pinMode(FRONT_LEFT_CLIFF, INPUT);
}

#endif

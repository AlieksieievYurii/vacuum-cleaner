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
}

#endif

#include <Wire.h>

#define BUT 8
#define BUT_G_L 10 // Button Green Led
#define BUT_R_L 9 // Button Red Led
#define RELEY 6
#define CH_VOL A0

#include "utils.h"

#define POWER_CONTROLLER_ADDRESS 0x01

//======== Input Commands ===========
#define SET_TURNED_ON_STATE 0x01
#define SET_SHUTTING_DOWN_STATE 0x02
#define SET_TURNED_OFF_STATE 0x03
#define SET_ERROR_STATE 0x06
#define RESET_ERROR_STATE 0x07
//===================================

STATUS current_status = TURNED_OFF;
CHARGING_STATE charging_state = NOT_CHARGING;

bool is_error = false;

void setup(void) {
  pinMode(BUT, INPUT_PULLUP);
  pinMode(BUT_G_L, OUTPUT);
  pinMode(BUT_R_L, OUTPUT);
  pinMode(RELEY, OUTPUT);
  pinMode(CH_VOL, INPUT);

  Wire.onRequest(requestEvent);
  Wire.onReceive(receiveEvent);
  Wire.begin(POWER_CONTROLLER_ADDRESS);

  Serial.begin(9600);
}

void requestEvent() {
  Wire.write(current_status);
  Wire.write(charging_state);
}

void receiveEvent(int) {
  uint8_t command = Wire.read();
  on_receive_command(command);
}

void loop(void) {
  if (but_is_pressed()) {
    if (current_status == TURNED_OFF) {
      current_status = BOOTING_UP;
    } else if (current_status == TURNED_ON) {
      current_status = SHUTTING_DOWN;
    }
  }

  if (current_status == TURNED_OFF) {
    SET_RELAY(LOW);
  }
  else if (current_status == TURNED_ON || current_status == BOOTING_UP) {
    SET_RELAY(HIGH);
  }

  but_leds_handler();
  handle_charger_status();
}

void on_receive_command(uint8_t command) {
  switch (command) {
    case SET_TURNED_ON_STATE:
      if (current_status == BOOTING_UP)
        current_status = TURNED_ON;
      break;
    case SET_SHUTTING_DOWN_STATE:
      if (current_status == TURNED_ON)
        current_status = SHUTTING_DOWN;
      break;
    case SET_TURNED_OFF_STATE:
      if (current_status == SHUTTING_DOWN)
        current_status = TURNED_OFF;
      break;
    case SET_ERROR_STATE:
      is_error = true;
      break;
    case RESET_ERROR_STATE:
      is_error = false;
      break;
  }
}

void handle_charger_status() {
  float v = get_charger_voltage();
  if (v < 1) {
    charging_state = NOT_CHARGING;
    return;
  }

  if (v > MAX_ACCEPTABLE_CHARGING_VOLTAGE)
    charging_state = OVERVOLTAGE;
  else if (v < MIN_ACCEPTABLE_CHARGING_VOLTAGE)
    charging_state = UNDERVOLTAGE;
  else
    charging_state = CHARGING;
}

float get_charger_voltage(void) {
  return (float) analogRead(CH_VOL) * 20 / 1023;
}

void but_leds_handler() {
  if (charging_state != NOT_CHARGING) {
    fade_in_out_green_button_led();
    return;
  }

  if (is_error) {
    blink_red_green();
    return;
  }

  switch (current_status) {
    case BOOTING_UP: blink_but_g_l(); break;
    case TURNED_ON: set_but_g_l(); break;
    case SHUTTING_DOWN: blink_but_r_l(); break;
    case TURNED_OFF: turn_off_all_but_leds(); break;
  }
}

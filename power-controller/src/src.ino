/*
   PowerController is a board based on Arduino Nano that is responsible for proper switching on or off the main power.
   Also reads the battery's cells voltages. The board has DC-DC regulator that is considered as a charger,
   so the board also checks if the battery is charging correclty.
   The board itself is supposed to be connected with motherboard(with A1 module) by I2C protocol as Slave. A1 module sends specific commands, e.g
   to say that the mother board has been booted up or is goint to shut down.
*/

#include <Wire.h>


//============== Define pins =====================
#define BUTTON 8
#define BUTTON_GREEN_LED 10 // Button Green Led
#define BUTTON_RED_LED 9 // Button Red Led
#define RELEY 6
#define CHARGING_VOLTAGE_PIN A0
#define IS_CHARGED_PIN 5
#define IS_CHARGING_PIN 4
#define IS_CONSTANT_CURRENT_PIN 3

#define BATTERY_VOLTAGE A7

#include "utils.h"

#define POWER_CONTROLLER_ADDRESS 0x01

//======== Input Commands ===========
// Commands for the board through I2C. For more information refer to the documentation(power-controller/README.md)
#define SET_TURNED_ON_STATE 0x01
#define SET_SHUTTING_DOWN_STATE 0x02
#define SET_TURNED_OFF_STATE 0x03
#define SET_ERROR_STATE 0x04
#define RESET_ERROR_STATE 0x05
//===================================

STATUS current_status = TURNED_OFF;
CHARGING_STATE charging_state = NOT_CHARGING;
CHARDING_WORK_STATUS charging_work_status = OK;

bool is_error = false;

volatile float battery_voltage = 0;

void setup(void) {
  pinMode(BUTTON, INPUT_PULLUP);
  pinMode(BUTTON_GREEN_LED, OUTPUT);
  pinMode(BUTTON_RED_LED, OUTPUT);
  pinMode(RELEY, OUTPUT);
  pinMode(CHARGING_VOLTAGE_PIN, INPUT);
  pinMode(IS_CHARGED_PIN, INPUT);
  pinMode(IS_CHARGING_PIN, INPUT);
  pinMode(IS_CONSTANT_CURRENT_PIN, INPUT);
  pinMode(BATTERY_VOLTAGE, INPUT);

  Wire.onRequest(requestEvent);
  Wire.onReceive(receiveEvent);
  Wire.begin(POWER_CONTROLLER_ADDRESS);

  Serial.begin(9600);
}

void requestEvent() {
  Wire.write(current_status);
  Wire.write(charging_state);
  Wire.write(charging_work_status);
  Wire.write((uint8_t) battery_voltage); // Pushing integer part
  Wire.write(((uint8_t)(battery_voltage * 10) % 10)); // Pushing decimal part
}

void receiveEvent(int) {
  uint8_t command = Wire.read();
  on_receive_command(command);
}

void loop(void) {
  BUTTON_STATE button_state = get_button_state();

  if (button_state == PRESSED) {
    if (current_status == TURNED_OFF) {
      current_status = BOOTING_UP;
    } else if (current_status == TURNED_ON) {
      current_status = SHUTTING_DOWN;
    }
  } else if (button_state == LONG_PRESSED) {
    current_status = TURNED_OFF;
  }

  if (current_status == TURNED_OFF) {
    SET_RELAY(LOW);
  }
  else if (current_status == TURNED_ON || current_status == BOOTING_UP) {
    SET_RELAY(HIGH);
  }

  battery_voltage = MAP_FROM_ZERO_TO(analogRead(BATTERY_VOLTAGE), 20.2);

  but_leds_handler();
  handle_chargering();
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


/*
   Checks if the battery is charing. Moreover, it validates the voltage
   and proper work status of the DC-DC charger module.
*/
void handle_chargering(void) {
  float charging_voltage = CHARGING_VOLTAGE;

  //If the charding voltage is less than 1 -> the charging power is unplugged
  if (charging_voltage < 1) {
    charging_state = NOT_CHARGING;
    charging_work_status = OK;
    return;
  } else {
    if (IS_CHARGING && !IS_CHARGED) {
      charging_state = CHARGING;

      if (charging_voltage > MAX_ACCEPTABLE_CHARGING_VOLTAGE)
        charging_work_status = OVERVOLTAGE;
      else if (charging_voltage < MIN_ACCEPTABLE_CHARGING_VOLTAGE)
        charging_work_status = UNDERVOLTAGE;
      else
        charging_work_status = OK;

    } else if (IS_CHARGED && !IS_CHARGING) {
      charging_work_status = OK;
      charging_state = CHARGED;
    } else {
      charging_work_status = DISCREPANCY;
    }
  }
}

void but_leds_handler() {
  if (is_error) {
    blink_red_green();
    return;
  }

  if (charging_state == CHARGED) {
    fade_in_out_green_button_led();
    return;
  } else if (charging_state == CHARGING) {
    fade_in_out_red_button_led();
    return;
  }

  switch (current_status) {
    case BOOTING_UP: blink_button_green_led(); break;
    case TURNED_ON: set_button_green_led(); break;
    case SHUTTING_DOWN: blink_button_red_led(); break;
    case TURNED_OFF: turn_off_all_button_leds(); break;
  }
}

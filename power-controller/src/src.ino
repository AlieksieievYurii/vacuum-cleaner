/*
 * PowerController is a board based on Arduino Nano that is responsible for proper switching on or off the main power. 
 * Also reads the battery's cells voltages. The board has DC-DC regulator that is considered as a charger, 
 * so the board also checks if the battery is charging correclty.
 * The board itself is supposed to be connected with motherboard(with A1 module) by I2C protocol as Slave. A1 module sends specific commands, e.g 
 * to say that the mother board has been booted up or is goint to shut down.
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

//---------Battery Cells
#define CELL_A A2
#define CELL_B A3
#define CELL_C A6
#define CELL_D A7
//================================================

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

volatile float a_cell_voltage = 0, //0..4.2
               b_cell_voltage = 0, //0..8.4
               c_cell_voltage = 0, //0..12.6
               d_cell_voltage = 0; //0..16.8

void setup(void) {
  pinMode(BUTTON, INPUT_PULLUP);
  pinMode(BUTTON_GREEN_LED, OUTPUT);
  pinMode(BUTTON_RED_LED, OUTPUT);
  pinMode(RELEY, OUTPUT);
  pinMode(CHARGING_VOLTAGE_PIN, INPUT);
  pinMode(IS_CHARGED_PIN, INPUT);
  pinMode(IS_CHARGING_PIN, INPUT);
  pinMode(IS_CONSTANT_CURRENT_PIN, INPUT);
  pinMode(CELL_A, INPUT);
  pinMode(CELL_B, INPUT);
  pinMode(CELL_C, INPUT);
  pinMode(CELL_D, INPUT);

  Wire.onRequest(requestEvent);
  Wire.onReceive(receiveEvent);
  Wire.begin(POWER_CONTROLLER_ADDRESS);

  Serial.begin(9600);
}

void requestEvent() {
  Wire.write(current_status);
  Wire.write(charging_state);
  Wire.write(charging_work_status);
  Wire.write(FLOAT_TO_BINARY(a_cell_voltage));
  Wire.write(FLOAT_TO_BINARY(b_cell_voltage));
  Wire.write(FLOAT_TO_BINARY(c_cell_voltage));
  Wire.write(FLOAT_TO_BINARY(d_cell_voltage));
}

void receiveEvent(int) {
  uint8_t command = Wire.read();
  on_receive_command(command);
}

void loop(void) {
  if (button_is_pressed()) {
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
  handle_chargering();
  read_battery_cells_voltages();

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
 * Checks if the battery is charing. Moreover, it validates the voltage 
 * and proper work status of the DC-DC charger module.
 */
void handle_chargering(void) {
  float charging_voltage = CHARGING_VOLTAGE;

  //If the charding voltage is less than 1 -> the charging power is unplugged
  if (charging_voltage < 1) {
    charging_state = NOT_CHARGING;
    charging_work_status = OK;
    return;
  } else {
    if (IS_CHARGING && IS_CONSTANT_CURRENT && !IS_CHARGED) {
      charging_state = CHARGING;

      if (charging_voltage > MAX_ACCEPTABLE_CHARGING_VOLTAGE)
        charging_work_status = OVERVOLTAGE;
      else if (charging_voltage < MIN_ACCEPTABLE_CHARGING_VOLTAGE)
        charging_work_status = UNDERVOLTAGE;
      else
        charging_work_status = OK;

    } else if (IS_CHARGED && !IS_CONSTANT_CURRENT && !IS_CHARGING) {
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
  }else if (charging_state == CHARGING) {
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

/*
 * Reads voltage of 4 analog pins which are connected to the battery cells.
 * Cell A Voltage range: 0..4.2
 * Cell B Voltage range: 0..8.4
 * Cell C Voltage range: 0..12.6
 * Cell D Voltage range: 0..16.8
 */
void read_battery_cells_voltages(void) {
  a_cell_voltage = MAP_FROM_ZERO_TO(analogRead(CELL_A), 5.0);
  b_cell_voltage = constrain(MAP_FROM_ZERO_TO(analogRead(CELL_B), 10) - a_cell_voltage, 0, 5);
  c_cell_voltage = constrain(MAP_FROM_ZERO_TO(analogRead(CELL_C), 15) - b_cell_voltage - a_cell_voltage - 0.1, 0, 5);
  d_cell_voltage = constrain(MAP_FROM_ZERO_TO(analogRead(CELL_D), 20.0) - c_cell_voltage - b_cell_voltage - a_cell_voltage - 0.2, 0, 5);
}

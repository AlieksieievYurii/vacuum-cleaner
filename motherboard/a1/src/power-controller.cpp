#include "power-controller.h"

#include <Wire.h>

PowerController::PowerController() {
  Wire.begin();
}

void PowerController::tick() {
  if (_data_is_requested && Wire.available() == REQUESTED_BYTES) {
    power_state = Wire.read();
    battery_state = Wire.read();
    _data_is_requested = false;
  } else {
    Wire.requestFrom(POWER_CONTROLLER_ADDRESS, REQUESTED_BYTES);
    _data_is_requested = true;
  }
}

void PowerController::_send_byte(uint8_t command) {
  Wire.beginTransmission(POWER_CONTROLLER_ADDRESS);
  Wire.write(command);
  Wire.endTransmission();
}

void PowerController::set_state_TURNED_ON() {
  _send_byte(SET_TURNED_ON_STATE);
}
void PowerController::set_state_SHUTTING_DOWN() {
  _send_byte(SET_SHUTTING_DOWN_STATE);
}
void PowerController::set_state_TURNED_OFF() {
  _send_byte(SET_TURNED_OFF_STATE);
}

void PowerController::set_error_state() {
  _send_byte(SET_ERROR_STATE);
}

void PowerController::reset_error_state() {
  _send_byte(RESET_ERROR_STATE);
}

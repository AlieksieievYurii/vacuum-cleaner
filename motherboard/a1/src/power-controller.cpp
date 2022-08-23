#include "power-controller.h"

#include <Wire.h>

PowerController::PowerController() {
  Wire.begin();
}

void PowerController::tick() {
  if (_data_is_requested && Wire.available() == REQUESTED_BYTES) {
    power_state = Wire.read();
    charging_state = Wire.read();
    charging_work_status = Wire.read();
    bin_repr_voltage_cell_a = Wire.read();
    bin_repr_voltage_cell_b = Wire.read();
    bin_repr_voltage_cell_c = Wire.read();
    bin_repr_voltage_cell_d = Wire.read();
    _data_is_requested = false;

//    Serial.print("Power state: ");
//    Serial.print(power_state);
//    Serial.print("  Charging state: ");
//    Serial.print(charging_state);
//    Serial.print("  Charging work status: ");
//    Serial.print(charging_work_status);
//    Serial.print("  A: ");
//    Serial.print(bin_repr_voltage_cell_a);
//    Serial.print("  B: ");
//    Serial.print(bin_repr_voltage_cell_b);
//    Serial.print("  C: ");
//    Serial.print(bin_repr_voltage_cell_c);
//    Serial.print("  D: ");
//    Serial.println(bin_repr_voltage_cell_d);
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

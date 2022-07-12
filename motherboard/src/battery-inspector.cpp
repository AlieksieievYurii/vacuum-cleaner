#include "battery-inspector.h"

BatteryInspector::BatteryInspector(uint8_t a_cell_pin, uint8_t b_cell_pin, uint8_t c_cell_pin, uint8_t d_cell_pin) {
  _a_cell_pin = a_cell_pin;
  _b_cell_pin = b_cell_pin;
  _c_cell_pin = c_cell_pin;
  _d_cell_pin = d_cell_pin;
}


void BatteryInspector::tick() {
  a_cell_voltage = MAP_FROM_ZERO_TO(analogRead(_a_cell_pin), 5.0);
  b_cell_voltage = constrain(MAP_FROM_ZERO_TO(analogRead(_b_cell_pin), 10) - a_cell_voltage, 0, 5);
  c_cell_voltage = constrain(MAP_FROM_ZERO_TO(analogRead(_c_cell_pin), 15) - b_cell_voltage - a_cell_voltage - 0.1, 0, 5);
  d_cell_voltage = constrain(MAP_FROM_ZERO_TO(analogRead(_d_cell_pin), 20.0) - c_cell_voltage - b_cell_voltage - a_cell_voltage - 0.2, 0, 5);
  charged = CHARGE_IN_PERCENTAGE(a_cell_voltage) + CHARGE_IN_PERCENTAGE(b_cell_voltage) + CHARGE_IN_PERCENTAGE(c_cell_voltage) + CHARGE_IN_PERCENTAGE(d_cell_voltage);
}

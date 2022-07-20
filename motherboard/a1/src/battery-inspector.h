#ifndef __battery_inspector_h__
#define __battery_inspector_h__

#include <Arduino.h>

#define MIN_CELL_VOLTAGE 2.6
#define MAX_CELL_VOLTAGE 4.0

#define MAP_FROM_ZERO_TO(V,K) V * K / 1023.0

#define CHARGE_IN_PERCENTAGE(x) ((uint8_t)round((x - MIN_CELL_VOLTAGE) * 25 / (MAX_CELL_VOLTAGE - MIN_CELL_VOLTAGE)))

class BatteryInspector {
  public:
    volatile float a_cell_voltage = 0,
                   b_cell_voltage = 0,
                   c_cell_voltage = 0,
                   d_cell_voltage = 0;
    volatile uint8_t charged = 0;
    BatteryInspector(uint8_t a_cell_pin, uint8_t b_cell_pin, uint8_t c_cell_pin, uint8_t d_cell_pin);
    void tick();

  private:
    uint8_t _a_cell_pin, // max 4.2
            _b_cell_pin, // max 8.4
            _c_cell_pin, // max 12.6
            _d_cell_pin; // max 16.8
};

#endif

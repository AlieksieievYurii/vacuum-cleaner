#ifndef __motor_h__
#define __motor_h__

#include <Arduino.h>
#include "instruction-handler.h"

#define TICK_INTERVAL 100
#define SIGNAL_TICK_STEP 30

class Motor {
  public:
    Motor(InstructionHandler &instruction_handler, uint8_t pin);
    void tick();
    void set(uint16_t request_id, uint8_t value);

  private:
    InstructionHandler* _instruction_handler;
    uint8_t _pin;
    uint16_t _request_id;
    uint8_t _setpoint = 0;
    uint8_t _signal = 0;
    bool _is_set = true;
    uint32_t _last_tick = 0;
};

#endif

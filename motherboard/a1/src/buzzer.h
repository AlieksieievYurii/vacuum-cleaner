#ifndef __buzzer_h__
#define __buzzer_h__

#include "instruction-handler.h"
#include <Arduino.h>

class Buzzer {
  public:
    Buzzer(uint8_t pin, InstructionHandler &instruction_handler);
    void tick();
    void beep(uint16_t request_id, uint8_t count, uint16_t period);
    
  private:
    InstructionHandler* _instruction_handler;
    bool _is_executing = false;
    uint16_t _request_id = 0;
    uint8_t _pin;
    uint8_t _beep_count = 0;
    uint32_t _start_time = 0;
    uint16_t _period = 0;
};

#endif

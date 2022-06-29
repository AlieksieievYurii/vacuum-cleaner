#ifndef __buzzer_h__
#define __buzzer_h__

#include <Arduino.h>

class Buzzer {
  public:
    Buzzer(uint8_t pin);
    void tick();
    void beep(uint8_t count, uint16_t period);
    
  private:
    uint8_t _pin;
    uint8_t _beep_count = 0;
    uint32_t _start_time = 0;
    uint16_t _period = 0;
};

#endif

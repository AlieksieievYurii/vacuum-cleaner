#ifndef __button_h__
#define __button_h__
#include <Arduino.h>

#define LONG_PRESS_TIME 2000

enum State: byte {
  UNPRESSED, CLICK, LONG_CLICK
};

class Button {
  public:
    Button(uint8_t pin);
    void tick();
    State read_state();

  private:
    uint8_t _pin;
    uint32_t _start_time = 0;
    bool _pressed = false;
    bool _hold_flag = false;
    State _state = UNPRESSED;
};

#endif

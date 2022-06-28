#ifndef __led_h__
#define __led_h__
#include <Arduino.h>

#define BLINKING_DELAY 300

enum LedState : byte {
  ON, OFF, BLINKING
};

class Led {
  public:
    Led(uint8_t port);
    void tick();
    void on();
    void off();
    void start_blinking();

  private:
    uint8_t _port;
    LedState _led_state = OFF;
    uint32_t _time = 0;
};

#endif

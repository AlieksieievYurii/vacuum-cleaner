#include "led.h"

Led::Led(uint8_t port) {
  _port = port;
  
}

void Led::tick() {
  if (_led_state == ON)
    digitalWrite(_port, HIGH);
  else if (_led_state == OFF)
    digitalWrite(_port, LOW);
  else if (_led_state == BLINKING) {
    if (millis() - _time > BLINKING_DELAY) {
      digitalWrite(_port, !digitalRead(_port));
      _time = millis();
    }
  }
}

void Led::on() {
  _led_state = ON;
}

void Led::off() {
  _led_state = OFF;
}

void Led::start_blinking() {
  _led_state = BLINKING;
}

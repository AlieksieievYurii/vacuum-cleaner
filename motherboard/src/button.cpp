#include "button.h"
#include <Arduino.h>

#define IS_PRESSED digitalRead(_pin) == LOW

Button::Button(uint8_t pin) {
  _pin = pin;
}

void Button::tick() {
  if (IS_PRESSED && !_pressed) {
    _start_time = millis();
    _pressed = true;
  } else if (IS_PRESSED && _pressed && !_hold_flag && millis() - _start_time > LONG_PRESS_TIME) {
    _state = LONG_CLICK;
    _hold_flag = true;
    _pressed = false;
    _start_time = 0;
  } else if (!IS_PRESSED && _pressed && !_hold_flag) {
    _state = CLICK;
    _pressed = false;
    _start_time = 0;
  } else if (!IS_PRESSED) {
    _hold_flag = false;
    _pressed = false;
  }
}

State Button::read_state() {
  State current_state = _state;
  _state = UNPRESSED;
  return current_state;
}

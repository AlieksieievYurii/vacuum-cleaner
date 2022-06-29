#include "buzzer.h"

Buzzer::Buzzer(uint8_t pin) {
  _pin = pin;
}

void Buzzer::tick() {
  if (_beep_count == 0) {
    digitalWrite(_pin, LOW);
    return;
  }

  if (millis() - _start_time > _period) {
    if (digitalRead(_pin)) {
      digitalWrite(_pin, LOW);
      _beep_count--;
    } else
      digitalWrite(_pin, HIGH);

    _start_time = millis();
  }
}

void Buzzer::beep(uint8_t count, uint16_t period) {
  _beep_count = count;
  _period = period;
}

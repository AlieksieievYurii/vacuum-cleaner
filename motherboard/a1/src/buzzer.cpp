#include "buzzer.h"

Buzzer::Buzzer(uint8_t pin, InstructionHandler &instruction_handler) {
  _pin = pin;
  _instruction_handler = &instruction_handler;
}

void Buzzer::tick() {
  if (_beep_count == 0) {
    digitalWrite(_pin, LOW);
    if(_is_executing) {
      _instruction_handler->on_finished(_request_id);
      _is_executing = false;
    }
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

void Buzzer::beep(uint16_t request_id, uint8_t count, uint16_t period) {
  _beep_count = count;
  _period = period;
  _is_executing = true;
  _request_id = request_id;
}

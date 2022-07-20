#include "motor.h"

Motor::Motor(InstructionHandler &instruction_handler, uint8_t pin) {
  _instruction_handler = &instruction_handler;
  _pin = pin;
}

void Motor::set(uint16_t request_id, uint8_t value /*0..100*/) {
  if (value > 100) value = 100;
  _request_id = request_id;
  _setpoint = value * 255 / 100;
  _is_set = false;
}

void Motor::tick() {
  if (!_is_set and millis() - _last_tick > TICK_INTERVAL) {
    
    if (_signal > _setpoint)
      _signal -= _signal - _setpoint < SIGNAL_TICK_STEP ? _signal - _setpoint : SIGNAL_TICK_STEP;
    else if (_signal < _setpoint)
      _signal += _setpoint - _signal < SIGNAL_TICK_STEP ?  _setpoint - _signal : SIGNAL_TICK_STEP;
    else {
      _instruction_handler->on_finished(_request_id);
      _is_set = true;
    }
    analogWrite(_pin, _signal);
    _last_tick = millis();
  }
}

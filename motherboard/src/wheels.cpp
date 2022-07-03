#include "wheels.h"

Wheel::Wheel(uint8_t forward_pin, uint8_t backward_pin, uint8_t speed_sensor, uint8_t direction_sensor, void (*pulse_interupt)()) {
  _forward_pin = forward_pin;
  _backward_pin = backward_pin;
  _speed_sensor = speed_sensor;
  _direction_sensor = direction_sensor;
  attachInterrupt(digitalPinToInterrupt(speed_sensor), pulse_interupt, RISING);
}

void Wheel::set_PID(float kp, float ki, float kd) {
  _kp = kp;
  _ki = ki;
  _kd = kd;
}

void Wheel::tick() {
  _measure_speed();

  if (_wheel_state == MOVING && _wheel_pulses_count >= _pulses_to_move)
    _wheel_state = STOPED;

  if (_wheel_state == MOVING)
    _measure_pid_and_set_speed();
  else if (_wheel_state == STOPED) {
    if(_with_break) {
       digitalWrite(_forward_pin, HIGH);
    digitalWrite(_backward_pin, HIGH);
    }
    _wheel_state = IDLE;
  } else {
    digitalWrite(_forward_pin, LOW);
    digitalWrite(_backward_pin, LOW);
  }
}

void Wheel::_measure_speed() {
  const float rpm = (float)((_wheel_pulses_for_speed * (60000 / CALL_INTERVAL)) / ENC_COUNT_REV);
  _speed = rpm * FULL_ROTATION_DISTANCE / 10;
  _wheel_pulses_for_speed = 0;
}

void Wheel::pulse() {
  _wheel_pulses_for_speed++;
  _wheel_pulses_count++;
  _direction_is_forward = digitalRead(_direction_sensor);
}

void Wheel::move(float distanse_sm, uint32_t speed, bool with_break, bool forward) {
  digitalWrite(_forward_pin, LOW);
  digitalWrite(_backward_pin, LOW);
  _wheel_state = MOVING;
  _forward_direction_to_move = forward;
  _speed_setpoint = speed;
  _with_break = with_break;
  _wheel_pulses_count = 0;
  _pulses_to_move = distanse_sm * 10 / FULL_ROTATION_DISTANCE * ENC_COUNT_REV;
}

void Wheel::_measure_pid_and_set_speed() {
  float err = _speed_setpoint - _speed;
  float dt = 0.1;
  static float integral = 0, prevErr = 0;
  integral = constrain(integral + (float)err * dt * _ki, 0, 255);
  float D = (err - prevErr) / dt;
  prevErr = err;
  uint8_t res = constrain(err * _kp + integral + D * _kd, 0, 255);
  analogWrite(_forward_direction_to_move ? _forward_pin : _backward_pin, res);
}

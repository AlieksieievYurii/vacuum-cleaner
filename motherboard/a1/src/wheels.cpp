#include "wheels.h"

#define STOP_MOTOR \
  digitalWrite(_forward_pin, HIGH); \
  digitalWrite(_backward_pin, HIGH);

#define NETRAL_MOTOR \
  digitalWrite(_forward_pin, LOW); \
  digitalWrite(_backward_pin, LOW);

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
  if (wheel_state == MOVING && _wheel_pulses_count >= _pulses_to_move)
    wheel_state = STOPPED;

  if (wheel_state == MOVING || wheel_state == ENDLESS_WALKING) {
    _measure_speed();
    _measure_pid_and_set_speed();
  }
  else if (wheel_state == STOPPED) {
    if (_with_break) {
      STOP_MOTOR
    } else {
      NETRAL_MOTOR
    }
    wheel_state = IDLE;
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

void Wheel::move(uint32_t distanse_sm, uint32_t speed, bool with_break, bool forward) {
  NETRAL_MOTOR

  _forward_direction_to_move = forward;
  _speed_setpoint = speed;
  _with_break = with_break;
  _wheel_pulses_count = 0;
  _integral = 0;
  _prev_err = 0;
  _pulses_to_move = (float)(distanse_sm * 10) / FULL_ROTATION_DISTANCE * ENC_COUNT_REV;

  wheel_state = MOVING;
}

void Wheel::walk(uint32_t speed_sm_per_minute, bool forward) {
  NETRAL_MOTOR

  _forward_direction_to_move = forward;
  _speed_setpoint = speed_sm_per_minute;
  _integral = 0;
  _prev_err = 0;

  wheel_state = ENDLESS_WALKING;
}

void Wheel::_measure_pid_and_set_speed() {
  float err = _speed_setpoint - _speed;
  _integral = constrain(_integral + (float)err * CALL_INTERVAL_IN_SECONDS * _ki, 0, 255);
  float D = (err - _prev_err) / CALL_INTERVAL_IN_SECONDS;
  _prev_err = err;
  uint8_t res = constrain(err * _kp + _integral + D * _kd, 0, 255);
  analogWrite(_forward_direction_to_move ? _forward_pin : _backward_pin, res);
}

Wheels::Wheels(InstructionHandler &instruction_handler, Wheel &left_wheel, Wheel &right_wheel) {
  _instruction_handler = &instruction_handler;
  _left_wheel = &left_wheel;
  _right_wheel = &right_wheel;
}

void Wheels::tick() {
  if (_left_wheel->wheel_state == IDLE && _right_wheel->wheel_state == MOVING) {
    _right_wheel->wheel_state = STOPPED;
  } else if (_right_wheel->wheel_state == IDLE && _left_wheel->wheel_state == MOVING) {
    _left_wheel->wheel_state = STOPPED;
  }

  if (_is_moving && _left_wheel->wheel_state == IDLE && _right_wheel->wheel_state == IDLE) {
    _instruction_handler->on_finished(_request_id);
    _request_id = 0;
    _is_moving = false;
  }
}

void Wheels::move(uint16_t request_id, uint32_t  distance_sm, uint32_t speed_sm_per_minute, bool forward, HaltMode halt_mode) {
  _request_id = request_id;
  _is_moving = true;
  _left_wheel->move(distance_sm, speed_sm_per_minute, halt_mode == WITH_STOP, forward);
  _right_wheel->move(distance_sm, speed_sm_per_minute, halt_mode == WITH_STOP, forward);
}

void Wheels::walk(uint16_t request_id, uint32_t speed_sm_per_minute, bool forward) {
  _is_moving = true;
  _left_wheel->walk(speed_sm_per_minute, forward);
  _right_wheel->walk(speed_sm_per_minute, forward);

  _instruction_handler->on_finished(request_id);
}

uint32_t calculate_angle_distance_in_sm(uint16_t angle) {
  float radius_in_sm = WHEELS_BASE_LINE_DIAMETER_MM / 2 / 10.0;

  if (angle >= 360) {
    uint16_t full_turns = angle / 360;
    return ((angle - full_turns * 360) * PI * radius_in_sm) / 180 + full_turns * (2 * PI * radius_in_sm);
  } else {
    return (PI * radius_in_sm * angle) / 180;
  }
}

void Wheels::turn(uint16_t request_id, SideDirection side_direction, uint16_t degree, uint32_t speed_sm_per_minute, HaltMode halt_mode) {
  _request_id = request_id;
  _is_moving = true;

  uint32_t distance = calculate_angle_distance_in_sm(degree);

  _left_wheel->move(distance, speed_sm_per_minute, halt_mode == WITH_STOP, side_direction == RIGHT);
  _right_wheel->move(distance, speed_sm_per_minute, halt_mode == WITH_STOP, side_direction == LEFT);
}

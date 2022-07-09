#include "range-finder.h"

RangeFinder::RangeFinder(uint8_t left_t, uint8_t left_e, uint8_t center_t, uint8_t center_e, uint8_t right_t, uint8_t right_e) {
  _left_t = left_t;
  _left_e = left_e;
  _center_t = center_t;
  _center_e = center_e;
  _right_t = right_t;
  _right_e = right_e;
}

void RangeFinder::tick() {
  switch (_range_finder_to_proceed) {
    case 0x0: _measure_left_range(); break;
    case 0x1: _measure_center_range(); break;
    case 0x2: _measure_right_range(); break;
  }
  if (_range_finder_to_proceed >= 0x2)
    _range_finder_to_proceed = 0;
  else
    _range_finder_to_proceed++;
}

float measure_range_in_mm(uint8_t trig, uint8_t echo) {
  digitalWrite(trig, LOW);
  delayMicroseconds(5);
  digitalWrite(trig, HIGH);
  delayMicroseconds(10);
  digitalWrite(trig, LOW);

  uint32_t duration = pulseIn(echo, HIGH, PULSE_TIMEOUT);

  if (duration == 0)
    duration = PULSE_TIMEOUT / 2;
  return constrain( (duration / 2) / CM_PER_US, 0, 25.0) * 10; // Constrain max 25 cm
}

void RangeFinder::_measure_left_range() {
  _left_average_buffer[_left_buf_index++] = measure_range_in_mm(_left_t, _left_e);
  if (_left_buf_index >= AVERAGE_BUFFER_SIZE) _left_buf_index = 0;
}

void RangeFinder::_measure_center_range() {
  _center_average_buffer[_center_buf_index++] = measure_range_in_mm(_center_t, _center_e);

  if (_center_buf_index >= AVERAGE_BUFFER_SIZE) _center_buf_index = 0;
}

void RangeFinder::_measure_right_range() {
  _right_average_buffer[_right_buf_index++] = measure_range_in_mm(_right_t, _right_e);

  if (_right_buf_index >= AVERAGE_BUFFER_SIZE) _right_buf_index = 0;
}

uint8_t count_sum(uint16_t* ar) {
  uint16_t sum = 0;
  for (uint8_t i = 0; i < AVERAGE_BUFFER_SIZE; i++)
    sum += ar[i];

  return sum / AVERAGE_BUFFER_SIZE;
}

uint8_t RangeFinder::get_left_range_in_mm() {
  return count_sum(_left_average_buffer);
}
uint8_t RangeFinder::get_center_range_in_mm() {
  return count_sum(_center_average_buffer);
}
uint8_t RangeFinder::get_right_range_in_mm() {
  return count_sum(_right_average_buffer);
}

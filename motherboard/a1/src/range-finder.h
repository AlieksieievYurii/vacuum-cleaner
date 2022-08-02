#ifndef __range_finder_h__
#define __range_finder_h__

#include <Arduino.h>

#define AVERAGE_BUFFER_SIZE 5

#define PULSE_TIMEOUT 6000
#define CM_PER_US 29.1


class RangeFinder {
  public:
    RangeFinder(uint8_t left_t, uint8_t left_e, uint8_t center_t, uint8_t center_e, uint8_t right_t, uint8_t right_e);
    void tick();
    uint8_t get_left_range_in_mm();
    uint8_t get_center_range_in_mm();
    uint8_t get_right_range_in_mm();

  private:
    uint8_t _left_t, _left_e, _center_t, _center_e, _right_t, _right_e;
    uint8_t _range_finder_to_proceed = 0; //Range: 0..3 where 0 - left rangefinder, 1 - center rangefinder, 2 - right rangefinder
    uint16_t _left_average_buffer[AVERAGE_BUFFER_SIZE] = {0};
    uint16_t _center_average_buffer[AVERAGE_BUFFER_SIZE] = {0};
    uint16_t _right_average_buffer[AVERAGE_BUFFER_SIZE] = {0};
    uint8_t _left_buf_index = 0;
    uint8_t _center_buf_index = 0;
    uint8_t _right_buf_index = 0;

    void _measure_left_range();
    void _measure_center_range();
    void _measure_right_range();
};

#endif

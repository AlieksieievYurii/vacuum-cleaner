#ifndef __wheels_h__
#define __wheels_h__

#include <Arduino.h>
#include "instruction-handler.h"

// How many pulses in one 360 rotation, must be measured manually!!!!
#define ENC_COUNT_REV 235

// Distance made by a full rotation. Diameter of the wheel: 67.5 mm -> Pi * 67.5 => 212 mm
#define FULL_ROTATION_DISTANCE 212

#define CALL_INTERVAL 100

#define WHEELS_BASE_LINE_DIAMETER_MM 290

#define CALCULATE_ANGLE_DISTANCE(ANGLE) (3.14 * (WHEELS_BASE_LINE_DIAMETER_MM/2 / 10.0) * ANGLE) / 180

enum WheelState : byte {
  IDLE, MOVING, STOPPED
};

enum HaltMode : byte {
  WITH_STOP, NEUTRAL
};

enum SideDirection : byte {
  LEFT, RIGHT
};

class Wheel {
  public:
    Wheel(uint8_t forward_pin, uint8_t backward_pin, uint8_t speed_sensor, uint8_t direction_sensor, void (*pulse_interupt)());
    WheelState wheel_state = IDLE;
    void set_PID(float kp, float ki, float kd);
    void tick(); // Must be called every CALL_INTERVAL milliseconds
    void move(float distanse_sm, uint32_t speed, bool with_break, bool forward);
    void pulse();


  private:
    uint8_t _forward_pin, _backward_pin, _speed_sensor, _direction_sensor;
    float _kp, _ki, _kd;
    volatile uint32_t _wheel_pulses_for_speed = 0;
    volatile uint64_t _wheel_pulses_count = 0;
    volatile float _speed = 0.0; // sm per minute
    volatile bool _direction_is_forward;
    uint64_t _pulses_to_move = 0;
    float _speed_setpoint = 0; // sm per minute
    bool _with_break = false;
    bool _forward_direction_to_move = false;
    void _measure_speed();
    void _measure_pid_and_set_speed();
};

class Wheels {
  public:
    Wheels(InstructionHandler &instruction_handler, Wheel &left_wheel, Wheel &right_wheel);
    void tick();
    void move(uint16_t request_id, uint32_t distance_sm, uint32_t speed_sm_per_minute, bool forward, HaltMode halt_mode);
    void turn(uint16_t request_id, SideDirection side_direction, uint8_t degree, uint32_t speed_sm_per_minute, HaltMode halt_mode);

  private:
    InstructionHandler* _instruction_handler;
    Wheel* _left_wheel;
    Wheel* _right_wheel;
    bool _is_moving = false;
    uint16_t _request_id;

};

#endif

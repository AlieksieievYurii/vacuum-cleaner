#ifndef __implementation_h__
#define __implementation_h__

#include "utils.h"
#include "led.h"
#include "button.h"
#include "buzzer.h"
#include "wheels.h"
#include "motor.h"

#define IS_PRESSED(pin) !digitalRead(pin)

Led led_wifi(LED_WIFI);
Led led_error(LED_ERR);
Led led_status(LED_ST);

Button btn_up(BUT_UP);
Button btn_ok(BUT_OK);
Button btn_down(BUT_DOWN);

Buzzer buzzer(BUZZER, instruction_handler);

Wheel wheel_left(LEFT_FORWARD, LEFT_BACKWARD, LEFT_WHEEL_SPEED_SENSOR, LEFT_WHEEL_DIRECTION_SENSOR, []() {
  wheel_left.pulse();
});
Wheel wheel_right(RIGHT_FORWARD, RIGHT_BACKWARD, RIGHT_WHEEL_SPEED_SENSOR, RIGHT_WHEEL_DIRECTION_SENSOR, []() {
  wheel_right.pulse();
});

Wheels wheels(instruction_handler, wheel_left, wheel_right);

Motor vacuum_motor(instruction_handler, VACUUM_MOTOR);
Motor left_brush_motor(instruction_handler, LEFT_BRUSH_MOTOR); 
Motor right_brush_motor(instruction_handler, RIGHT_BRUSH_MOTOR);

void _handle_led(uint16_t id, Led &led, char* input) {
  switch (input[0]) {
    case 'H': led.on(); break;
    case 'L': led.off(); break;
    case 'B': led.start_blinking(); break;
    default:
      instruction_handler.on_failed(id, 0x1);
      return;
  }

  instruction_handler.on_finished(id);
}

void on_led_wifi(uint16_t id, char* input) {
  _handle_led(id, led_wifi, input);
}

void on_led_err(uint16_t id, char* input) {
  _handle_led(id, led_error, input);
}

void on_led_st(uint16_t id, char* input) {
  _handle_led(id, led_status, input);
}

void on_beep(uint16_t id, char* input) {
  int8_t index = find_character_index(input, ';', 2);

  if (index == -1) {
    instruction_handler.on_failed(id, 0x1);
    return;
  }

  char beep_count_array[2] = {0};
  char period_array[6] = {0};
  strncpy(beep_count_array, input, index);
  strncpy(period_array, &input[index + 1], 6);
  uint8_t bepp_count = strtol(beep_count_array, NULL, 16);
  uint16_t period = strtol(period_array, NULL, 16);
  buzzer.beep(id, bepp_count, period);
}

uint8_t get_controll_buttons_state() {
  uint8_t state = 0;

  switch (btn_up.read_state()) {
    case CLICK: state |= 0x10; break;
    case LONG_CLICK: state |= 0x30; break;
    case UNPRESSED: break;
  }

  switch (btn_ok.read_state()) {
    case CLICK: state |= 0x4; break;
    case LONG_CLICK: state |= 0xC; break;
    case UNPRESSED: break;
  }

  switch (btn_down.read_state()) {
    case CLICK: state |= 0x1; break;
    case LONG_CLICK: state |= 0x3; break;
    case UNPRESSED: break;
  }

  return state;
}

uint8_t get_ends_state() {
  uint8_t state = 0;

  if (IS_PRESSED(RIGHT_END ))
    state |= 0x1;

  if (IS_PRESSED(LEFT_END))
    state |= 0x2;

  if (IS_PRESSED(LID_END))
    state |= 0x4;

  if (IS_PRESSED(DUST_BOX_END))
    state |= 0x8;

  return state;
}

void on_move(uint16_t id, char* input) {
  const int8_t direction = fetch_unsigned_hex_number(input, 0);
  if (direction == PARSING_ERROR || direction == CANNOT_PARSE_NUMBER) {
    instruction_handler.on_failed(id, 0x1);
    return;
  }
  const int32_t distance_in_sm = fetch_unsigned_hex_number(input, 1);
  if (distance_in_sm == PARSING_ERROR || distance_in_sm == CANNOT_PARSE_NUMBER) {
    instruction_handler.on_failed(id, 0x1);
    return;
  }
  const int32_t speed_sm_per_minute = fetch_unsigned_hex_number(input, 2);
  if (speed_sm_per_minute == PARSING_ERROR || speed_sm_per_minute == CANNOT_PARSE_NUMBER) {
    instruction_handler.on_failed(id, 0x1);
    return;
  }

  const int8_t halt_mode_id = fetch_unsigned_hex_number(input, 3);
  if (halt_mode_id == PARSING_ERROR || halt_mode_id == CANNOT_PARSE_NUMBER) {
    instruction_handler.on_failed(id, 0x1);
    return;
  }

  bool forward = false;
  switch (direction) {
    case 0x1: forward = true; break;
    case 0x2: forward = false; break;
    default:
      instruction_handler.on_failed(id, 0x2);
      return;
  }

  HaltMode halt_mode;
  switch (halt_mode_id) {
    case 0x1: halt_mode = WITH_STOP; break;
    case 0x2: halt_mode = NEUTRAL; break;
    default:
      instruction_handler.on_failed(id, 0x3);
      return;
  }

  wheels.move(id, distance_in_sm, speed_sm_per_minute, forward, halt_mode);

}
//<1 - left, 2 - right>;<degree>;<speed>;<halt mode>
void on_turn(uint16_t id, char* input) {
  const int8_t side = fetch_unsigned_hex_number(input, 0);
  if (side == PARSING_ERROR || side == CANNOT_PARSE_NUMBER) {
    instruction_handler.on_failed(id, 0x1);
    return;
  }
  const int16_t degree = fetch_unsigned_hex_number(input, 1);
  if (degree == PARSING_ERROR || degree == CANNOT_PARSE_NUMBER) {
    instruction_handler.on_failed(id, 0x1);
    return;
  }

  const int16_t speed = fetch_unsigned_hex_number(input, 2);
  if (speed == PARSING_ERROR || speed == CANNOT_PARSE_NUMBER) {
    instruction_handler.on_failed(id, 0x1);
    return;
  }

  const int16_t halt_mode_id = fetch_unsigned_hex_number(input, 3);
  if (halt_mode_id == PARSING_ERROR || halt_mode_id == CANNOT_PARSE_NUMBER) {
    instruction_handler.on_failed(id, 0x1);
    return;
  }

  HaltMode halt_mode;
  switch (halt_mode_id) {
    case 0x1: halt_mode = WITH_STOP; break;
    case 0x2: halt_mode = NEUTRAL; break;
    default:
      instruction_handler.on_failed(id, 0x3);
      return;
  }

  switch (side) {
    case 0x1: wheels.turn(id, LEFT, degree, speed, halt_mode); break;
    case 0x2:  wheels.turn(id, RIGHT, degree, speed, halt_mode); break;
    default:
      instruction_handler.on_failed(id, 0x5);
      return;
  }

}

void set_motor_signal(Motor &motor, uint16_t id, char* input) {
  uint8_t value = fetch_unsigned_hex_number(input, 0);
  if (value > 100) {
    instruction_handler.on_failed(id, 0x1);
    return;
  }

  motor.set(id, value);
}

void on_vacuum_motor(uint16_t id, char* input) {
  set_motor_signal(vacuum_motor, id, input);
}

void on_left_brush_motor(uint16_t id, char* input) {
  set_motor_signal(left_brush_motor, id, input);
}

void on_right_brush_motor(uint16_t id, char* input) {
  set_motor_signal(right_brush_motor, id, input);
}

void propagandate_tick_signal() {
  led_wifi.tick();
  led_error.tick();
  led_status.tick();

  btn_up.tick();
  btn_ok.tick();
  btn_down.tick();

  buzzer.tick();

  wheels.tick();

  vacuum_motor.tick();
  left_brush_motor.tick();
  right_brush_motor.tick();
}

ISR(TIMER5_A) {
  wheel_right.tick();
  wheel_left.tick();
}

#endif

#ifndef __implementation_h__
#define __implementation_h__

#include "utils.h"
#include "led.h"
#include "button.h"
#include "buzzer.h"
#include "wheels.h"
#include "motor.h"
#include "range-finder.h"
#include "battery-inspector.h"
#include "ds3231.h"
#include "DHT11.h"
#include "power-controller.h"

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
Motor main_brush_motor(instruction_handler, MAIN_BRUSH_MOTOR);

RangeFinder range_finder(
  LEFT_RF_TRIG, LEFT_RF_ECHO,
  CENTER_RF_TRIG, CENTER_RF_ECHO,
  RIGHT_RF_TRIG, RIGHT_RF_ECHO
);

BatteryInspector battery_inspector(CELL_A, CELL_B, CELL_C, CELL_D);

DS3231 ds3231_clock;

DHT dht(TEMP_HUMIDITY_SENSOR);

PowerController power_controller;

void on_has_been_initialized(uint16_t id, char* input) {
  switch (input[0]) {
    case 'S': power_controller.set_state_TURNED_ON(); break;
    case 'F': power_controller.set_error_state(); break;
    default:
      instruction_handler.on_failed(id, 0x1);
      return;
  }

  instruction_handler.on_finished(id);
}

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

  state |= IS_PRESSED(RIGHT_END) << 0;
  state |= IS_PRESSED(LEFT_END) << 1;
  state |= IS_PRESSED(LID_END) << 2;
  state |= IS_PRESSED(DUST_BOX_END) << 3;

  return state;
}

uint32_t get_rangefinder_value() {
  uint32_t result = 0;
  result |= range_finder.get_left_range_in_mm();
  result |= (uint16_t)range_finder.get_center_range_in_mm() << 8;
  result |= (uint32_t)range_finder.get_right_range_in_mm() << 16;
  return result;
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

  const int8_t n_with_break = fetch_unsigned_hex_number(input, 3);
  if (n_with_break == PARSING_ERROR || n_with_break == CANNOT_PARSE_NUMBER) {
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

  bool with_break;
  switch (n_with_break) {
    case 0x1: with_break = true; break;
    case 0x2: with_break = false; break;
    default:
      instruction_handler.on_failed(id, 0x3);
      return;
  }

  wheels.move(id, distance_in_sm, speed_sm_per_minute, forward, with_break);

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

  const int8_t n_with_break = fetch_unsigned_hex_number(input, 3);
  if (n_with_break == PARSING_ERROR || n_with_break == CANNOT_PARSE_NUMBER) {
    instruction_handler.on_failed(id, 0x1);
    return;
  }

  bool with_break;
  switch (n_with_break) {
    case 0x1: with_break = true; break;
    case 0x2: with_break = false; break;
    default:
      instruction_handler.on_failed(id, 0x3);
      return;
  }

  switch (side) {
    case 0x1: wheels.turn(id, LEFT, degree, speed, with_break); break;
    case 0x2:  wheels.turn(id, RIGHT, degree, speed, with_break); break;
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

void on_main_brush_motor(uint16_t id, char* input) {
  set_motor_signal(main_brush_motor, id, input);
}

void on_request_battery_status(uint16_t id, char*) {
  String res = "";
  res += battery_inspector.a_cell_voltage;
  res += ";";
  res += battery_inspector.b_cell_voltage;
  res += ";";
  res += battery_inspector.c_cell_voltage;
  res += ";";
  res += battery_inspector.d_cell_voltage;
  res += ";";
  res += battery_inspector.charged;
  char result[25] = {0};
  res.toCharArray(result, 25);
  instruction_handler.on_result(id, result);
}

void on_get_current_time(uint16_t id, char* input) {
  //Input has newline in the end, so we need to get rid of it
  for (uint8_t i = 0; i < MAX_INPUT_SIZE; i++) {
    if (input[i] == '\n') {
      input[i] = '\0';
      break;
    }
  }

  RTCDateTime current_data_time = ds3231_clock.getDateTime();
  char *result = ds3231_clock.dateFormat(input, current_data_time);
  instruction_handler.on_result(id, result);
}

//Example of input: 2014;1;13;14;34;32
void on_set_data_time(uint16_t id, char* input) {
  const int16_t year = fetch_unsigned_hex_number(input, 0);
  const int8_t month = fetch_unsigned_hex_number(input, 1);
  const int8_t day = fetch_unsigned_hex_number(input, 2);
  const int8_t hour = fetch_unsigned_hex_number(input, 3);
  const int8_t minute = fetch_unsigned_hex_number(input, 4);
  const int8_t second = fetch_unsigned_hex_number(input, 5);

  if (year == PARSING_ERROR || month == PARSING_ERROR || day == PARSING_ERROR || hour == PARSING_ERROR || minute == PARSING_ERROR || second == PARSING_ERROR) {
    instruction_handler.on_failed(id, 0x1);
    return;
  }

  ds3231_clock.setDateTime(year, month, day, hour, minute, second);
  instruction_handler.on_finished(id);
}

void on_get_temp_and_humid(uint16_t id, char*) {
  float t = dht.readTemperature();
  float h = dht.readHumidity();

  if (isnan(h) || isnan(t)) {
    instruction_handler.on_failed(id, 0x1);
    return;
  }

  float hi = dht.computeHeatIndex(t, h);

  String res = "";
  res += t;
  res += ";";
  res += h;
  res += ";";
  res += hi;
  char result[25] = {0};
  res.toCharArray(result, 25);
  instruction_handler.on_result(id, result);
}


void on_set_shutting_down_state(uint16_t id, char*) {
  power_controller.set_state_SHUTTING_DOWN();
  instruction_handler.on_finished(id);
}

void on_cut_off_the_power(uint16_t id, char*) {
  power_controller.set_state_TURNED_OFF();
  instruction_handler.on_finished(id);
}

void on_set_error_state_in_power_controller(uint16_t id, char* input) {
  switch (input[0]) {
    case 'T': power_controller.set_error_state(); break;
    case 'F': power_controller.reset_error_state(); break;
    default:
      instruction_handler.on_failed(id, 0x1);
      return;
  }

  instruction_handler.on_finished(id);
}

void on_walk(uint16_t id, char* input) {
  int32_t speed_sm_per_minute = fetch_unsigned_hex_number(input, 0);

  if (speed_sm_per_minute == PARSING_ERROR || speed_sm_per_minute == CANNOT_PARSE_NUMBER) {
    instruction_handler.on_failed(id, 0x1);
    return;
  }

  int8_t direction = fetch_unsigned_hex_number(input, 1);

  if (direction == PARSING_ERROR || direction == CANNOT_PARSE_NUMBER) {
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


  wheels.walk(id, speed_sm_per_minute, forward);

}

uint8_t get_power_controller_state() {
  uint8_t res = 0;
  res |= power_controller.power_state;
  res |= power_controller.battery_state << 2;

  return res;
}

uint8_t get_cliffs_status() {
  uint8_t res = 0;

  res |= digitalRead(BACK_RIGHT_CLIFF) << 0;
  res |= digitalRead(BACK_CENTER_CLIFF) << 1;
  res |= digitalRead(BACK_LEFT_CLIFF) << 2;
  res |= digitalRead(FRONT_RIGHT_CLIFF) << 3;
  res |= digitalRead(FRONT_CENTER_CLIFF) << 4;
  res |= digitalRead(FRONT_LEFT_CLIFF) << 5;

  return res;
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
  main_brush_motor.tick();

  range_finder.tick();

  battery_inspector.tick();

  power_controller.tick();
}

ISR(TIMER5_A) {
  wheel_right.tick();
  wheel_left.tick();
}

#endif


#include "config.h"
#include "instruction-handler.h"
#include "utils.h"

InstructionHandler instruction_handler(Serial);

#include "implementation.h"

#define __ENABLE_SENSOR_READING__

void setup() {
  init_hardware();
  instruction_handler.begin(9600);
  instruction_handler.add(0x01, on_has_been_initialized);
  instruction_handler.add(0x02, on_led_wifi);
  instruction_handler.add(0x03, on_led_err);
  instruction_handler.add(0x04, on_led_st);
  instruction_handler.add(0x05, on_beep);
  instruction_handler.add(0x06, on_move);
  instruction_handler.add(0x07, on_turn);
  instruction_handler.add(0x08, on_vacuum_motor);
  instruction_handler.add(0x09, on_left_brush_motor);
  instruction_handler.add(0x0A, on_right_brush_motor);
  instruction_handler.add(0x0B, on_request_battery_status);
  instruction_handler.add(0x0C, on_get_current_time);
  instruction_handler.add(0x0D, on_set_data_time);
  instruction_handler.add(0x0E, on_get_temp_and_humid);
  instruction_handler.add(0x0F, on_set_shutting_down_state);
  instruction_handler.add(0x10, on_cut_off_the_power);
  instruction_handler.add(0x11, on_set_error_state_in_power_controller);
  instruction_handler.add(0x12, on_main_brush_motor);
  instruction_handler.add(0x13, on_walk);
  
  enable_Timer5(20, CHANNEL_A);
  ds3231_clock.begin();

  wheel_left.set_PID(0.1, 0.1, 0);
  wheel_right.set_PID(0.1, 0.1, 0);
}

void loop() {  
  instruction_handler.perform();
  propagandate_tick_signal();
 
#ifdef __ENABLE_SENSOR_READING__
  instruction_handler.reset_sensors_output_buffer();
  instruction_handler.add_sensor_output(0x01, get_controll_buttons_state());
  instruction_handler.add_sensor_output(0x02, get_ends_state());
  instruction_handler.add_sensor_output(0x03, get_rangefinder_value());
  instruction_handler.add_sensor_output(0x04, get_cliffs_status());
  instruction_handler.add_sensor_output(0x05, get_power_controller_state());
  instruction_handler.send_sensors_output();
#endif
}

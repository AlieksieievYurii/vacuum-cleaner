#include "config.h"
#include "instruction-handler.h"
#include "utils.h"

InstructionHandler instruction_handler(Serial);

#include "implementation.h"

//#define __ENABLE_SENSOR_READING__

void setup() {
  init_hardware();
  instruction_handler.begin(9600);
  instruction_handler.add(0x02, on_led_wifi);
  instruction_handler.add(0x03, on_led_err);
  instruction_handler.add(0x04, on_led_st);
  instruction_handler.add(0x05, on_beep);
  instruction_handler.add(0x06, on_move);
  instruction_handler.add(0x07, on_turn);
  instruction_handler.add(0x08, on_vacuum_motor);
  enable_Timer5(20, CHANNEL_A);

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
  instruction_handler.send_sensors_output();
#endif
}

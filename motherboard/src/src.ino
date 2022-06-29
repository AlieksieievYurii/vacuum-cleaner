#include "config.h"
#include "instruction-handler.h"

InstructionHandler instruction_handler(Serial);

#include "implementation.h"

void setup() {
  init_hardware();
  instruction_handler.begin(9600);
  instruction_handler.add(0x02, on_led_wifi);
  instruction_handler.add(0x03, on_led_err);
  instruction_handler.add(0x04, on_led_st);

}

void loop() {
  instruction_handler.perform();
  propagandate_tick_signal();
  
  instruction_handler.reset_sensors_output_buffer();
  instruction_handler.add_sensor_output(0x01, get_controll_buttons_state());
  instruction_handler.send_sensors_output();
}

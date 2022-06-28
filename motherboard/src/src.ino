#include "config.h"
#include "instruction-handler.h"
#include "led.h"

InstructionHandler instruction_handler(Serial);

Led led(13);

void diode(uint16_t id, char* input) {
  if (input[0] == 'H') {
    led.on();
    instruction_handler.on_finished(id);
  }
  else if (input[0] == 'L') {
    led.off();
    instruction_handler.on_finished(id);
  }
  else if (input[0] == 'B') {
    led.start_blinking();
    instruction_handler.on_finished(id);
  } else
    instruction_handler.on_failed(id, 0xf);


}

void motor(uint16_t id, char* input) {
  Serial.println("M");
  Serial.println(id);
  Serial.println(input);

}

void setup() {
  init_hardware();
  instruction_handler.begin(9600);
  instruction_handler.add(0xFF, diode);
  instruction_handler.add(0x01, motor);
}

void loop() {
  instruction_handler.perform();
  led.tick();

  instruction_handler.reset_sensors_output_buffer();
  instruction_handler.add_sensor_output(0x01, 0x02);
  instruction_handler.add_sensor_output(0x02, 0x03);
  instruction_handler.add_sensor_output(0xff, 0x0f);
  instruction_handler.add_sensor_output(0xff, 0x0f);
  instruction_handler.add_sensor_output(0xff, 0x0f);
  instruction_handler.add_sensor_output(0xff, 0x0f);
  instruction_handler.add_sensor_output(0xff, 0x0f);
  instruction_handler.add_sensor_output(0xff, 0x0f);
  instruction_handler.add_sensor_output(0xff, 0x0f);
  instruction_handler.add_sensor_output(0xff, 0x0f);
  instruction_handler.add_sensor_output(0xff, 0x0f);
  instruction_handler.send_sensors_output();
}

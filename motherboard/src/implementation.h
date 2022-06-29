#ifndef __implementation_h__
#define __implementation_h__

#include "utils.h"
#include "led.h"
#include "button.h"
#include "buzzer.h"

Led led_wifi(LED_WIFI);
Led led_error(LED_ERR);
Led led_status(LED_ST);

Button btn_up(BUT_UP);
Button btn_ok(BUT_OK);
Button btn_down(BUT_DOWN);

#define IS_PRESSED(pin) !digitalRead(pin)

Buzzer buzzer(BUZZER);

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
  buzzer.beep(bepp_count, period);
  instruction_handler.on_finished(id);
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

void propagandate_tick_signal() {
  led_wifi.tick();
  led_error.tick();
  led_status.tick();

  btn_up.tick();
  btn_ok.tick();
  btn_down.tick();

  buzzer.tick();
}

#endif

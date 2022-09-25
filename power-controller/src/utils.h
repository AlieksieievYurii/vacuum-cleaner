#ifndef __UTILS__
#define __UTILS__

#define everyMillis(interval, action) \
  {                                     \
    static unsigned long t = 0UL;       \
    if( millis() - t > (interval) )     \
    {                                   \
      { action; }                       \
      t = millis();                     \
    }                                   \
  }

/*
   Represents the current status of Power Controller
*/
enum STATUS : byte {TURNED_OFF, BOOTING_UP, TURNED_ON, SHUTTING_DOWN};

/*
   This enum points the charging status.
*/
enum CHARGING_STATE : byte {NOT_CHARGING, CHARGING, CHARGED};

/*
   This enum points the current working status of charging.
   This is needed to understand if something is wrong with the charger(electronic module based on LM2596 3A DC-DC)
   OK - the charging voltage is normal 15+-1V.
   OVERVOLTAGE - if during charging the voltage is more than 16V
   UNDERVOLTAGE - if during charging the voltage is less than 14V
   DISCREPANCY - if output charging leds are discrepanced during the charging. TODO: Check that
*/
enum CHARDING_WORK_STATUS : byte {OK, OVERVOLTAGE, UNDERVOLTAGE, DISCREPANCY};

enum BUTTON_STATE : byte {NOTHING, PRESSED, LONG_PRESSED};

#define BUTTON_IS_PRESSED digitalRead(BUTTON) == LOW

#define SET_RELAY(ST) digitalWrite(RELEY, ST);

#define BLINKING_DELAY 200
#define LATEST_BUTTON_CLICK_DELAY 500

#define MAX_ACCEPTABLE_CHARGING_VOLTAGE 16
#define MIN_ACCEPTABLE_CHARGING_VOLTAGE 14

#define CHARGING_VOLTAGE (float) analogRead(CHARGING_VOLTAGE_PIN) * 20 / 1023

#define IS_CHARGED digitalRead(IS_CHARGED_PIN)
#define IS_CHARGING !digitalRead(IS_CHARGING_PIN) //Low signal means it is charging
#define IS_CONSTANT_CURRENT digitalRead(IS_CONSTANT_CURRENT_PIN)

//Maps given value V to K using relation 0..1023
#define MAP_FROM_ZERO_TO(V,K) V * K / 1023.0

BUTTON_STATE get_button_state(void) {
  static bool was_pressed = false, was_long_click = false;
  static uint32_t latest_click, click_timestamp = 0UL;

  if (millis() - latest_click < LATEST_BUTTON_CLICK_DELAY)
    return NOTHING;

  if (BUTTON_IS_PRESSED) {
    was_pressed = true;

    if (click_timestamp  == 0)
      click_timestamp = millis();

    if (was_long_click == false && millis() - click_timestamp > 3000) {
      was_long_click = true;
      click_timestamp = 0L;
      return LONG_PRESSED;
    }
  }
  else {
    if (was_pressed == true) {
      was_pressed = false;
      latest_click =  millis();
      click_timestamp = 0L;

      if (was_long_click == true) {
        was_long_click = false;
        return NOTHING;
      }

      return PRESSED;
    }
  }

  return NOTHING;
}

void blink_button_green_led() {
  digitalWrite(BUTTON_RED_LED, LOW);
  everyMillis(BLINKING_DELAY, {
    digitalWrite(BUTTON_GREEN_LED, !digitalRead(BUTTON_GREEN_LED));
  })
}

void blink_button_red_led() {
  digitalWrite(BUTTON_GREEN_LED, LOW);
  everyMillis(BLINKING_DELAY, {
    digitalWrite(BUTTON_RED_LED, !digitalRead(BUTTON_RED_LED));
  })
}

void fade_in_out_green_button_led() {
#define FADE_IN_GREEN 1
#define FADE_OUT_GREEN 2
  static uint8_t flag = FADE_IN_GREEN, k = 0;
  digitalWrite(BUTTON_RED_LED, LOW);

  everyMillis(5, {
    if (flag == FADE_IN_GREEN) {
      analogWrite(BUTTON_GREEN_LED, k++);
      if (k == 255)
        flag = FADE_OUT_GREEN;
    } else if (flag == FADE_OUT_GREEN) {
      analogWrite(BUTTON_GREEN_LED, k--);
      if (k == 0)
        flag = FADE_IN_GREEN;
    }
  })
}

void fade_in_out_red_button_led() {
#define FADE_IN_RED 1
#define FADE_OUT_RED 2
  static uint8_t flag = FADE_IN_RED, k = 0;
  digitalWrite(BUTTON_GREEN_LED, LOW);

  everyMillis(5, {
    if (flag == FADE_IN_RED) {
      analogWrite(BUTTON_RED_LED, k++);
      if (k == 255)
        flag = FADE_OUT_RED;
    } else if (flag == FADE_OUT_RED) {
      analogWrite(BUTTON_RED_LED, k--);
      if (k == 0)
        flag = FADE_IN_RED;
    }
  })
}

void blink_red_green() {
  static bool flag = false;
  everyMillis(300, {
    digitalWrite(BUTTON_RED_LED, flag);
    digitalWrite(BUTTON_GREEN_LED, !flag);
    flag = !flag;
  })
}

void set_button_green_led() {
  digitalWrite(BUTTON_GREEN_LED, HIGH);
  digitalWrite(BUTTON_RED_LED, LOW);
}

void turn_off_all_button_leds() {
  digitalWrite(BUTTON_GREEN_LED, LOW);
  digitalWrite(BUTTON_RED_LED, LOW);
}

#endif

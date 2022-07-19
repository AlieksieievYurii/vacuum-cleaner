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

enum STATUS : byte {TURNED_OFF, BOOTING_UP, TURNED_ON, SHUTTING_DOWN};
enum CHARGING_STATE : byte {NOT_CHARGING, CHARGING, OVERVOLTAGE, UNDERVOLTAGE};

#define BUT_IS_PRESSED digitalRead(BUT) == LOW

#define SET_RELAY(ST) digitalWrite(RELEY, ST);

#define BLINKING_DELAY 200
#define LATEST_BUT_CLICK_DELAY 500

#define MAX_ACCEPTABLE_CHARGING_VOLTAGE 18
#define MIN_ACCEPTABLE_CHARGING_VOLTAGE 16

bool but_is_pressed(void) {
  static bool was_pressed = false;
  static unsigned long latest_click = 0UL;

  if (millis() - latest_click < LATEST_BUT_CLICK_DELAY)
    return false;

  if (BUT_IS_PRESSED)
    was_pressed = true;
  else {
    if (was_pressed == true) {
      was_pressed = false;
      latest_click =  millis();
      return true;
    }
  }
  return false;
}

void blink_but_g_l() {
  digitalWrite(BUT_R_L, LOW);
  everyMillis(BLINKING_DELAY, {
    digitalWrite(BUT_G_L, !digitalRead(BUT_G_L));
  })
}

void blink_but_r_l() {
  digitalWrite(BUT_G_L, LOW);
  everyMillis(BLINKING_DELAY, {
    digitalWrite(BUT_R_L, !digitalRead(BUT_R_L));
  })
}

void fade_in_out_green_button_led() {
#define FADE_IN_GREEN 1
#define FADE_OUT_GREEN 2
  static uint8_t flag = FADE_IN_GREEN, k = 0;
  digitalWrite(BUT_R_L, LOW);

  everyMillis(10, {
    if (flag == FADE_IN_GREEN) {
      analogWrite(BUT_G_L, k++);
      if (k == 255)
        flag = FADE_OUT_GREEN;
    } else if (flag == FADE_OUT_GREEN) {
      analogWrite(BUT_G_L, k--);
      if (k == 0)
        flag = FADE_IN_GREEN;
    }
  })
}

void blink_red_green() {
  static bool flag = false;
  everyMillis(300, {
    digitalWrite(BUT_R_L, flag);
    digitalWrite(BUT_G_L, !flag);
    flag = !flag;
  })
}

void set_but_g_l() {
  digitalWrite(BUT_G_L, HIGH);
  digitalWrite(BUT_R_L, LOW);
}

void turn_off_all_but_leds() {
  digitalWrite(BUT_G_L, LOW);
  digitalWrite(BUT_R_L, LOW);
}

#endif

// ============== ON HOLD ================

#ifndef __display_h__
#define __display_h__

#include <Adafruit_SH1106.h>
#define OLED_RESET 4

enum DisplayState : byte {
  INITIALIZATION, SHOW_ERROR, SHOW_DEFAULT, SHOW_READY
};

class Display {
  public:
    void begin();
    void tick();

    void show_initialization();
    void show_error(char* message);
    void show_default(int8_t charged, float battery_voltage);
    void show_ready();

  private:
    Adafruit_SH1106* _display;
    DisplayState _display_state;
    uint8_t _index = 0;
    uint32_t _time = 0;
    int8_t _charged = 0; // if -1 -> the battery is charging
    float _battery_voltage = 0;
    char* _error_message = 0;
    void _draw_initialization(void);
    void _draw_error(void);
    void _draw_default(void);
    void _draw_ready(void);
};

#endif

#ifndef __display_h__
#define __display_h__

#include <Adafruit_SH1106.h>
#define OLED_RESET 4

enum DisplayState : byte {
  INITIALIZATION
};

class Display {
  public:
    void begin();
    void tick();

    void show_initialization();

  private:
    Adafruit_SH1106* _display;
    DisplayState _display_state;
    uint8_t _index = 0;
    uint32_t _time = 0;
    void _draw_initialization(void);
};

#endif

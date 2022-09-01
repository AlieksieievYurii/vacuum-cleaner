#include "display.h"

void Display::begin() {
  Adafruit_SH1106 display(OLED_RESET);
  display.begin(SH1106_SWITCHCAPVCC, 0x3C);
  display.clearDisplay();
  _display = &display;
}

void Display::show_initialization() {

  _display_state = INITIALIZATION;
}

void Display::tick() {
  if (millis() - _time > 300) {
    switch (_display_state) {
      case INITIALIZATION: _draw_initialization(); break;
    }

    _time = millis();
  }
}

void Display::_draw_initialization(void) {
  if (_index <= 5) {
    _display->drawCircle(64, 40, _index++ * 4, WHITE);
    _display->display();
  } else {
    _index = 0;
    _display->clearDisplay();
  }
  _display->setTextColor(WHITE);
  _display->setTextSize(1);
  _display->setCursor(10, 0);
  _display->print("Initialization...");
  _display->display();
}

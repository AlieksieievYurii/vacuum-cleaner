#include "display.h"

void Display::begin() {
  Adafruit_SH1106 display(OLED_RESET);
  display.begin(SH1106_SWITCHCAPVCC, 0x3C);
  display.clearDisplay();
  _display = &display;
}

void Display::show_initialization() {
  _index = 0;
  _display_state = INITIALIZATION;
}

void Display::show_error(char* message) {
  _index = 0;
  _error_message = message;
  _display_state = SHOW_ERROR;
}

void Display::show_default(int8_t charged, float battery_voltage) {
  _charged = charged;
  _battery_voltage = battery_voltage;
  _display_state = SHOW_DEFAULT;
}

void Display::tick() {
  if (millis() - _time > 300) {
    switch (_display_state) {
      case INITIALIZATION: _draw_initialization(); break;
      case SHOW_ERROR: _draw_error(); break;
      case SHOW_DEFAULT: _draw_default(); break;
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

void Display::_draw_error(void) {
  _display->clearDisplay();
  _display->setTextColor(WHITE);
  _display->setTextSize(1);
  _display->setCursor(0, 20);
  _display->print(_error_message);
  if (_index == 1) {
    _display->setCursor(20, 0);
    _display->print("!!! ERROR !!!");
    _index = 0;
  } else _index = 1;

  _display->display();
}

void Display::_draw_default(void) {
  _display->clearDisplay();
  _display->setTextColor(WHITE);
  _display->setTextSize(1);
  _display->setCursor(5, 0);
  if (_charged < 0) {
    _display->print("Charg... ");
  } else {
    _display->print("Bat: ");
    _display->print(_charged);
    _display->print("% (");
  }

  _display->print(_battery_voltage);
  _display->print("V)");
  _display->setTextSize(1);
  _display->setCursor(0, 30);
  _display->print("Vacuum Cleaner Robot");

  _display->display();
}

#ifndef __DHT11_h__
#define __DHT11_h__

#include "Arduino.h"

class DHT {
  public:
    DHT(uint8_t pin);
    float readTemperature();
    float computeHeatIndex(float temp, float percentHumidity);
    float readHumidity();

  private:
    uint8_t data[5];
    uint8_t _pin;
    uint8_t _bit, _port;
    uint32_t _lastreadtime, _maxcycles;
    bool _lastresult;
    uint32_t _expectPulse(bool level);
    bool _read();
};

class InterruptLock {
  public:
    InterruptLock() {
      noInterrupts();
    }
    ~InterruptLock() {
      interrupts();
    }
};

#endif

#include "DHT11.h"

#define MIN_INTERVAL 2000
#define TIMEOUT UINT32_MAX

DHT::DHT(uint8_t pin) {
  _pin = pin;
  _bit = digitalPinToBitMask(pin);
  _port = digitalPinToPort(pin);
  _maxcycles = microsecondsToClockCycles(1000);
  _lastreadtime = millis() - MIN_INTERVAL;
}

float DHT::readTemperature() {
  float f = NAN;

  if (_read()) {
    f = data[2];
    if (data[3] & 0x80) {
      f = -1 - f;
    }
    f += (data[3] & 0x0f) * 0.1;
  }
  return f;
}


float DHT::readHumidity() {
  float f = NAN;
  if (_read()) {
    f = data[0] + data[1] * 0.1;
  }
  return f;
}

float DHT::computeHeatIndex(float temp, float percentHumidity) {
  float hi;
  float temperature = temp * 1.8 + 32; //convert to F
  
  hi = 0.5 * (temperature + 61.0 + ((temperature - 68.0) * 1.2) +
              (percentHumidity * 0.094));

  if (hi > 79) {
    hi = -42.379 + 2.04901523 * temperature + 10.14333127 * percentHumidity +
         -0.22475541 * temperature * percentHumidity +
         -0.00683783 * pow(temperature, 2) +
         -0.05481717 * pow(percentHumidity, 2) +
         0.00122874 * pow(temperature, 2) * percentHumidity +
         0.00085282 * temperature * pow(percentHumidity, 2) +
         -0.00000199 * pow(temperature, 2) * pow(percentHumidity, 2);

    if ((percentHumidity < 13) && (temperature >= 80.0) &&
        (temperature <= 112.0))
      hi -= ((13.0 - percentHumidity) * 0.25) *
            sqrt((17.0 - abs(temperature - 95.0)) * 0.05882);

    else if ((percentHumidity > 85.0) && (temperature >= 80.0) &&
             (temperature <= 87.0))
      hi += ((percentHumidity - 85.0) * 0.1) * ((87.0 - temperature) * 0.2);
  }

  return (hi - 32) * 0.55555;
}

bool DHT::_read() {
  uint32_t currenttime = millis();
  if (((currenttime - _lastreadtime) < MIN_INTERVAL)) {
    return _lastresult; // return last correct measurement
  }
  _lastreadtime = currenttime;

  data[0] = data[1] = data[2] = data[3] = data[4] = 0;

  pinMode(_pin, INPUT_PULLUP);
  delay(1);

  
  pinMode(_pin, OUTPUT);
  digitalWrite(_pin, LOW);
  delay(20); 

  uint32_t cycles[80];
  {   
    pinMode(_pin, INPUT_PULLUP);   
    delayMicroseconds(55);   
    InterruptLock lock;

    
    if (_expectPulse(LOW) == TIMEOUT) {
      _lastresult = false;
      return _lastresult;
    }
    if (_expectPulse(HIGH) == TIMEOUT) {
      _lastresult = false;
      return _lastresult;
    }

   
    for (int i = 0; i < 80; i += 2) {
      cycles[i] = _expectPulse(LOW);
      cycles[i + 1] = _expectPulse(HIGH);
    }
  } 

  for (int i = 0; i < 40; ++i) {
    uint32_t lowCycles = cycles[2 * i];
    uint32_t highCycles = cycles[2 * i + 1];
    if ((lowCycles == TIMEOUT) || (highCycles == TIMEOUT)) {
      _lastresult = false;
      return _lastresult;
    }
    data[i / 8] <<= 1;
    
    if (highCycles > lowCycles) {
      data[i / 8] |= 1;
    }
    
  }
  
  if (data[4] == ((data[0] + data[1] + data[2] + data[3]) & 0xFF)) {
    _lastresult = true;
    return _lastresult;
  } else {

    _lastresult = false;
    return _lastresult;
  }
}


uint32_t DHT::_expectPulse(bool level) {
  uint16_t count = 0;
  uint8_t portState = level ? _bit : 0;
  while ((*portInputRegister(_port) & _bit) == portState) {
    if (count++ >= _maxcycles) {
      return TIMEOUT; // Exceeded timeout, fail.
    }
  }
  return count;
}

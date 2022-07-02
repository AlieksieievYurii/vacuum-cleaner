#ifndef __utils_h__
#define __utils_h__

#include <Arduino.h>

#define MAX_PERIOD_16 (1000000UL * 1024UL / F_CPU * 65536UL)
#define CHANNEL_A 0x00
#define CHANNEL_B 0x01
#define CHANNEL_C 0x02

#define TIMER5_A  TIMER5_COMPA_vect
#define TIMER5_B  TIMER5_COMPB_vect
#define TIMER5_C  TIMER5_COMPC_vect


int32_t find_character_index(char* input, char character, uint8_t limit) {
  for (uint32_t i = 0; i <= limit; i++) {
    if (input[i] == character)
      return i;
  }
  return -1;
}

uint32_t enable_Timer5_with_period(uint32_t period, uint8_t source) {
  period = constrain(period, 1, MAX_PERIOD_16);

  uint32_t _timer5_cycles = F_CPU / 1000000 * period;  // Calculation of the number of timer cycles per period
  uint8_t _timer5_prescaler = 0x00;
  uint16_t _timer5_divider = 0x00;

  if (_timer5_cycles < 65536UL) {   // Сhoose optimal divider for the timer
    _timer5_prescaler = 0x01;
    _timer5_divider = 1UL;
  } else if (_timer5_cycles < 65536UL * 8) {
    _timer5_prescaler = 0x02;
    _timer5_divider = 8UL;
  } else if (_timer5_cycles < 65536UL * 64) {
    _timer5_prescaler = 0x03;
    _timer5_divider = 64UL;
  } else if (_timer5_cycles < 65536UL * 256) {
    _timer5_prescaler = 0x04;
    _timer5_divider = 256UL;
  } else {
    _timer5_prescaler = 0x05;
    _timer5_divider = 1024UL;
  }

  uint16_t _timer5_top = (_timer5_cycles < 65536UL * 1024 ? (_timer5_cycles / _timer5_divider) : 65536UL) ;

  TCCR5A = (TCCR5A & 0xFC);
  TCCR5B = ((1 << WGM53) | (1 << WGM52) | _timer5_prescaler);   // CTC mode + set prescaler
  ICR5 = _timer5_top - 1;             // Set timer top

   switch (source) {
    case CHANNEL_A: TIMSK5 |= (1 << OCIE5A); break;
    case CHANNEL_B: TIMSK5 |= (1 << OCIE5B); break;
    case CHANNEL_C: TIMSK5 |= (1 << OCIE5C); break;
  }

  return (1000000UL / ((F_CPU / _timer5_divider) / _timer5_top));   // Return real timer period
}

float enable_Timer5(uint32_t frequency, uint8_t source) {
  return 1000000.0F / (enable_Timer5_with_period(1000000.0F / frequency, source));
}

#endif

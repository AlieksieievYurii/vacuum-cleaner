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

#define VALIDATE_PARSING(value, error_code)                                   \
      {                                                                       \
        if (value == PARSING_ERROR || value == CANNOT_PARSE_NUMBER) {         \
          instruction_handler.on_failed(id, error_code);                      \
          return;                                                             \
        }                                                                     \
      }                                                                       \

int32_t find_character_index(char* input, char character, uint8_t limit, const uint32_t from = 0) {
  for (uint32_t i = 0; i <= limit; i++) {
    if (input[from + i] == character)
      return i;
  }
  return -1;
}

#define PARSING_ERROR -1
#define CANNOT_PARSE_NUMBER -2
int32_t fetch_unsigned_hex_number(char* input, uint8_t group) {
#define MAX_VALUE_SIZE 4
#define SEPARATOR ';'
  char buffer[MAX_VALUE_SIZE] = {0};
  uint16_t start_index = 0;
  uint16_t end_index = 0;
  uint16_t separator_count = 0;
  bool is_found = false;
  char* endptr = NULL;

  //======Find start index======
  if (group > 0) {
    for (uint16_t i = 0; i < 100; i++) {
      if (input[i] == SEPARATOR)
        separator_count++;

      if (separator_count == group) {
        start_index = i;
        is_found = true;
        break;
      }
    }
    if (!is_found)
      return PARSING_ERROR;

    start_index += 1; // Skip the separator
  }
  //==============================

  //=====Find end index=======
  is_found = false;
  for (uint16_t i = start_index; i <= start_index + MAX_VALUE_SIZE; i++) {
    if (input[i] == SEPARATOR || input[i] == '\n') {
      end_index = i;
      is_found = true;
      break;
    }
  }

  if (!is_found)
    return PARSING_ERROR;
  //==============================

  strncpy(buffer, &input[start_index], end_index - start_index);
  int32_t result = (int32_t)strtol(buffer, &endptr, 16);

  if (endptr == buffer)
    return CANNOT_PARSE_NUMBER;
  
  return result;
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

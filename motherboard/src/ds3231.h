#ifndef __DS3231_H__
#define __DS3231_H__
#include "Arduino.h"

#define DS3231_ADDRESS              (0x68)

#define DS3231_REG_TIME             (0x00)
#define DS3231_REG_ALARM_1          (0x07)
#define DS3231_REG_ALARM_2          (0x0B)
#define DS3231_REG_CONTROL          (0x0E)
#define DS3231_REG_STATUS           (0x0F)
#define DS3231_REG_TEMPERATURE      (0x11)

#ifndef RTCDATETIME_STRUCT_H
#define RTCDATETIME_STRUCT_H
struct RTCDateTime
{
  uint16_t year;
  uint8_t month;
  uint8_t day;
  uint8_t hour;
  uint8_t minute;
  uint8_t second;
  uint8_t dayOfWeek;
  uint32_t unixtime;
};
#endif

typedef enum
{
  DS3231_1HZ          = 0x00,
  DS3231_4096HZ       = 0x01,
  DS3231_8192HZ       = 0x02,
  DS3231_32768HZ      = 0x03
} DS3231_sqw_t;

typedef enum
{
  DS3231_EVERY_SECOND   = 0b00001111,
  DS3231_MATCH_S        = 0b00001110,
  DS3231_MATCH_M_S      = 0b00001100,
  DS3231_MATCH_H_M_S    = 0b00001000,
  DS3231_MATCH_DT_H_M_S = 0b00000000,
  DS3231_MATCH_DY_H_M_S = 0b00010000
} DS3231_alarm1_t;

typedef enum
{
  DS3231_EVERY_MINUTE   = 0b00001110,
  DS3231_MATCH_M        = 0b00001100,
  DS3231_MATCH_H_M      = 0b00001000,
  DS3231_MATCH_DT_H_M   = 0b00000000,
  DS3231_MATCH_DY_H_M   = 0b00010000
} DS3231_alarm2_t;

class DS3231
{
  public:
    bool begin(void);
    void setDateTime(uint16_t year, uint8_t month, uint8_t day, uint8_t hour, uint8_t minute, uint8_t second);
    RTCDateTime getDateTime(void);
    uint8_t isReady(void);
    float readTemperature(void);
    void setBattery(bool timeBattery, bool squareBattery);
    char* dateFormat(const char* dateFormat, RTCDateTime dt);

  private:
    RTCDateTime t;

    const char *strDayOfWeek(uint8_t dayOfWeek);
    const char *strMonth(uint8_t month);
    const char *strAmPm(uint8_t hour, bool uppercase);
    const char *strDaySufix(uint8_t day);

    uint8_t hour12(uint8_t hour24);
    uint8_t bcd2dec(uint8_t bcd);
    uint8_t dec2bcd(uint8_t dec);

    long time2long(uint16_t days, uint8_t hours, uint8_t minutes, uint8_t seconds);
    uint16_t date2days(uint16_t year, uint8_t month, uint8_t day);
    uint8_t daysInMonth(uint16_t year, uint8_t month);
    uint16_t dayInYear(uint16_t year, uint8_t month, uint8_t day);
    bool isLeapYear(uint16_t year);
    uint8_t dow(uint16_t y, uint8_t m, uint8_t d);

    uint32_t unixtime(void);

    void writeRegister8(uint8_t reg, uint8_t value);
    uint8_t readRegister8(uint8_t reg);
};

#endif

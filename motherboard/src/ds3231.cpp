#include "Arduino.h"

#include <Wire.h>
#include "ds3231.h"

const uint8_t daysArray [] PROGMEM = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
const uint8_t dowArray[] PROGMEM = { 0, 3, 2, 5, 0, 3, 5, 1, 4, 6, 2, 4 };

const char* UNKNOWN = "Unknown";
const char *DAYS_SHORT[] = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
const char *MONTH_SHORT[] = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
const char *AM_OR_PM[] = {"AM", "PM", "am", "pm"};
const char *DAY_SUFFIX[] = {"st", "nd", "rd", "th"};

bool DS3231::begin(void) {
  Wire.begin();
  setBattery(true, false);

  t.year = 2000;
  t.month = 1;
  t.day = 1;
  t.hour = 0;
  t.minute = 0;
  t.second = 0;
  t.dayOfWeek = 6;
  t.unixtime = 946681200;

  return true;
}

void DS3231::setDateTime(uint16_t year, uint8_t month, uint8_t day, uint8_t hour, uint8_t minute, uint8_t second) {
  Wire.beginTransmission(DS3231_ADDRESS);
  Wire.write(DS3231_REG_TIME);
  Wire.write(dec2bcd(second));
  Wire.write(dec2bcd(minute));
  Wire.write(dec2bcd(hour));
  Wire.write(dec2bcd(dow(year, month, day)));
  Wire.write(dec2bcd(day));
  Wire.write(dec2bcd(month));
  Wire.write(dec2bcd(year - 2000));
  Wire.write(DS3231_REG_TIME);
  Wire.endTransmission();
}

char* DS3231::dateFormat(const char* dateFormat, RTCDateTime dt)
{
  static char buffer[255];
  buffer[0] = 0;
  char helper[11];

  while (*dateFormat != '\0')
  {
    switch (dateFormat[0])
    {
      // Day decoder
      case 'd':
        sprintf(helper, "%02d", dt.day);
        strcat(buffer, (const char *)helper);
        break;
      case 'j':
        sprintf(helper, "%d", dt.day);
        strcat(buffer, (const char *)helper);
        break;
      case 'l':
        strcat(buffer, (const char *)strDayOfWeek(dt.dayOfWeek));
        break;
      case 'D':
        strncat(buffer, strDayOfWeek(dt.dayOfWeek), 3);
        break;
      case 'N':
        sprintf(helper, "%d", dt.dayOfWeek);
        strcat(buffer, (const char *)helper);
        break;
      case 'w':
        sprintf(helper, "%d", (dt.dayOfWeek + 7) % 7);
        strcat(buffer, (const char *)helper);
        break;
      case 'z':
        sprintf(helper, "%d", dayInYear(dt.year, dt.month, dt.day));
        strcat(buffer, (const char *)helper);
        break;
      case 'S':
        strcat(buffer, (const char *)strDaySufix(dt.day));
        break;

      // Month decoder
      case 'm':
        sprintf(helper, "%02d", dt.month);
        strcat(buffer, (const char *)helper);
        break;
      case 'n':
        sprintf(helper, "%d", dt.month);
        strcat(buffer, (const char *)helper);
        break;
      case 'F':
        strcat(buffer, (const char *)strMonth(dt.month));
        break;
      case 'M':
        strncat(buffer, (const char *)strMonth(dt.month), 3);
        break;
      case 't':
        sprintf(helper, "%d", daysInMonth(dt.year, dt.month));
        strcat(buffer, (const char *)helper);
        break;

      // Year decoder
      case 'Y':
        sprintf(helper, "%d", dt.year);
        strcat(buffer, (const char *)helper);
        break;
      case 'y': sprintf(helper, "%02d", dt.year - 2000);
        strcat(buffer, (const char *)helper);
        break;
      case 'L':
        sprintf(helper, "%d", isLeapYear(dt.year));
        strcat(buffer, (const char *)helper);
        break;

      // Hour decoder
      case 'H':
        sprintf(helper, "%02d", dt.hour);
        strcat(buffer, (const char *)helper);
        break;
      case 'G':
        sprintf(helper, "%d", dt.hour);
        strcat(buffer, (const char *)helper);
        break;
      case 'h':
        sprintf(helper, "%02d", hour12(dt.hour));
        strcat(buffer, (const char *)helper);
        break;
      case 'g':
        sprintf(helper, "%d", hour12(dt.hour));
        strcat(buffer, (const char *)helper);
        break;
      case 'A':
        strcat(buffer, (const char *)strAmPm(dt.hour, true));
        break;
      case 'a':
        strcat(buffer, (const char *)strAmPm(dt.hour, false));
        break;

      // Minute decoder
      case 'i':
        sprintf(helper, "%02d", dt.minute);
        strcat(buffer, (const char *)helper);
        break;

      // Second decoder
      case 's':
        sprintf(helper, "%02d", dt.second);
        strcat(buffer, (const char *)helper);
        break;

      // Misc decoder
      case 'U':
        sprintf(helper, "%lu", dt.unixtime);
        strcat(buffer, (const char *)helper);
        break;

      default:
        strncat(buffer, dateFormat, 1);
        break;
    }
    dateFormat++;
  }

  return buffer;
}

RTCDateTime DS3231::getDateTime(void)
{
  int values[7];

  Wire.beginTransmission(DS3231_ADDRESS);
  Wire.write(DS3231_REG_TIME);
  Wire.endTransmission();
  Wire.requestFrom(DS3231_ADDRESS, 7);

  while (!Wire.available()) {};

  for (int i = 6; i >= 0; i--) {
    values[i] = bcd2dec(Wire.read());
  }

  Wire.endTransmission();

  t.year = values[0] + 2000;
  t.month = values[1];
  t.day = values[2];
  t.dayOfWeek = values[3];
  t.hour = values[4];
  t.minute = values[5];
  t.second = values[6];
  t.unixtime = unixtime();

  return t;
}

void DS3231::setBattery(bool timeBattery, bool squareBattery) {
  uint8_t value;

  value = readRegister8(DS3231_REG_CONTROL);

  if (squareBattery)
    value |= 0b01000000;
  else
    value &= 0b10111111;


  if (timeBattery)
    value &= 0b01111011;
  else
    value |= 0b10000000;

  writeRegister8(DS3231_REG_CONTROL, value);
}

float DS3231::readTemperature(void) {
  uint8_t msb, lsb;
  Wire.beginTransmission(DS3231_ADDRESS);
  Wire.write(DS3231_REG_TEMPERATURE);
  Wire.endTransmission();

  Wire.requestFrom(DS3231_ADDRESS, 2);

  while (!Wire.available()) {};

  msb = Wire.read();
  lsb = Wire.read();

  return ((((short)msb << 8) | (short)lsb) >> 6) / 4.0f;
}

uint8_t DS3231::bcd2dec(uint8_t bcd) {
  return ((bcd / 16) * 10) + (bcd % 16);
}

uint8_t DS3231::dec2bcd(uint8_t dec) {
  return ((dec / 10) * 16) + (dec % 10);
}

const char *DS3231::strDayOfWeek(uint8_t dayOfWeek) {
  if (dayOfWeek <= 6)
    return DAYS_SHORT[dayOfWeek];
  else
    return UNKNOWN;
}

const char *DS3231::strMonth(uint8_t month) {
  if (month <= 12)
    return MONTH_SHORT[month];
  else
    return UNKNOWN;
}

const char *DS3231::strAmPm(uint8_t hour, bool uppercase) {
  if (hour < 12) {
    if (uppercase)
      return AM_OR_PM[0];
    else
      return AM_OR_PM[2];
  } else {
    if (uppercase)
      return AM_OR_PM[1];
    else
      return AM_OR_PM[3];
  }
}

const char *DS3231::strDaySufix(uint8_t day) {
  if (day % 10 == 1)
    return DAY_SUFFIX[0];
  else if (day % 10 == 2)
    return DAY_SUFFIX[1];

  if (day % 10 == 3)
    return DAY_SUFFIX[2];


  return DAY_SUFFIX[3];
}

uint8_t DS3231::hour12(uint8_t hour24) {
  if (hour24 == 0)
    return 12;


  if (hour24 > 12)
    return (hour24 - 12);

  return hour24;
}

long DS3231::time2long(uint16_t days, uint8_t hours, uint8_t minutes, uint8_t seconds) {
  return ((days * 24L + hours) * 60 + minutes) * 60 + seconds;
}

uint16_t DS3231::dayInYear(uint16_t year, uint8_t month, uint8_t day) {
  uint16_t fromDate;
  uint16_t toDate;

  fromDate = date2days(year, 1, 1);
  toDate = date2days(year, month, day);

  return (toDate - fromDate);
}

bool DS3231::isLeapYear(uint16_t year) {
  return (year % 4 == 0);
}

uint8_t DS3231::daysInMonth(uint16_t year, uint8_t month) {
  uint8_t days;

  days = pgm_read_byte(daysArray + month - 1);

  if ((month == 2) && isLeapYear(year))
    ++days;


  return days;
}

uint16_t DS3231::date2days(uint16_t year, uint8_t month, uint8_t day) {
  year = year - 2000;

  uint16_t days16 = day;

  for (uint8_t i = 1; i < month; ++i) {
    days16 += pgm_read_byte(daysArray + i - 1);
  }

  if ((month == 2) && isLeapYear(year))
    ++days16;

  return days16 + 365 * year + (year + 3) / 4 - 1;
}

uint32_t DS3231::unixtime(void) {
  uint32_t u;

  u = time2long(date2days(t.year, t.month, t.day), t.hour, t.minute, t.second);
  u += 946681200;

  return u;
}


uint8_t DS3231::dow(uint16_t y, uint8_t m, uint8_t d) {
  uint8_t dow;

  y -= m < 3;
  dow = ((y + y / 4 - y / 100 + y / 400 + pgm_read_byte(dowArray + (m - 1)) + d) % 7);

  if (dow == 0)
    return 7;

  return dow;
}

void DS3231::writeRegister8(uint8_t reg, uint8_t value) {
  Wire.beginTransmission(DS3231_ADDRESS);
  Wire.write(reg);
  Wire.write(value);
  Wire.endTransmission();
}

uint8_t DS3231::readRegister8(uint8_t reg) {
  uint8_t value;
  Wire.beginTransmission(DS3231_ADDRESS);
  Wire.write(reg);
  Wire.endTransmission();
  Wire.requestFrom(DS3231_ADDRESS, 1);
  while (!Wire.available()) {};
  value = Wire.read();
  Wire.endTransmission();

  return value;
}

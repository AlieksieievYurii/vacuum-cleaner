#ifndef __power_controller_h__
#define __power_controller_h__

#include <Arduino.h>

#define POWER_CONTROLLER_ADDRESS 0x01

#define REQUESTED_BYTES 2


//======== Comands IDs ========
#define SET_TURNED_ON_STATE 0x01
#define SET_SHUTTING_DOWN_STATE 0x02
#define SET_TURNED_OFF_STATE 0x03
#define SET_ERROR_STATE 0x06
#define RESET_ERROR_STATE 0x07
//==============================

class PowerController {
  public:
    PowerController();
    uint8_t power_state, battery_state;
    void tick();
    void set_state_TURNED_ON();
    void set_state_SHUTTING_DOWN();
    void set_state_TURNED_OFF();

  private:
    bool _data_is_requested = false;
    void _send_byte(uint8_t command);

};

#endif

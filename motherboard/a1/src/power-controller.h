#ifndef __power_controller_h__
#define __power_controller_h__

#include <Arduino.h>

#define POWER_CONTROLLER_ADDRESS 0x01

#define REQUESTED_BYTES 7


//======== Comands IDs ========
#define SET_TURNED_ON_STATE 0x1
#define SET_SHUTTING_DOWN_STATE 0x2
#define SET_TURNED_OFF_STATE 0x3
#define SET_ERROR_STATE 0x4
#define RESET_ERROR_STATE 0x5
//==============================

class PowerController {
  public:
    PowerController();
    uint8_t power_state, charging_state, charging_work_status;
    uint8_t bin_repr_voltage_cell_a, 
            bin_repr_voltage_cell_b, 
            bin_repr_voltage_cell_c, 
            bin_repr_voltage_cell_d;
    void tick();
    void set_state_TURNED_ON();
    void set_state_SHUTTING_DOWN();
    void set_state_TURNED_OFF();
    void set_error_state();
    void reset_error_state();

  private:
    bool _data_is_requested = false;
    void _send_byte(uint8_t command);

};

#endif

#ifndef __handler_h__
#define __handler_h__

#include <Arduino.h>

#define START_PREFIX '#'
#define COMMAND_SIZE 2
#define ID_DEVIDER ':'
#define PARAMETERS_DEVIDER ':'
#define ID_SIZE 4
#define MAX_INPUT_SIZE 100

#define MAX_COMMAND_SIZE 1 + COMMAND_SIZE + ID_SIZE + MAX_INPUT_SIZE

#define RESPONSE_BUFFER_SIZE 18

#define SERIAL_READING_TIMEOUT_MS 200

enum SerialReadingStatus : byte {
  NONE, READY, ERROR
};

enum InstructionStatus : byte {
  OK, UNDEFINED_COMMAND, WRONG_INSTRUCTION
};

struct Instruction {
  int command;
  void (*callback)(uint16_t id, char* input);
};

class InstructionHandler {
  public:
    InstructionHandler(HardwareSerial &serial_stream);
    void begin(uint16_t sp);
    void add(int command, void (*callback)(uint16_t id, char* input));
    void perform();
    void on_finished(uint16_t request_id);
    void on_failed(uint16_t request_id, uint8_t error_code);
    void add_sensor_output(uint8_t id, uint16_t value);
    void send_sensors_output();
    void reset_sensors_output_buffer();

  private:
    Instruction instructions[100];
    char command[MAX_COMMAND_SIZE] = {0};
    String _sensors_output;
    HardwareSerial* _serial_stream;
    uint16_t _instruction_append_index = 0;
    SerialReadingStatus _read_from_sream(char* buf);
    InstructionStatus _handle(char* input);
    void _send(char* message);
    void _execute_instruction(uint16_t request_id, Instruction instruction, char* value);
};

#endif

#include "instruction-handler.h"
#include <stdio.h>
#include <string.h>
#include <stdlib.h>

InstructionHandler::InstructionHandler(HardwareSerial &serial_stream) {
  _serial_stream = &serial_stream;
}

void InstructionHandler::begin(uint16_t sp) {
  _serial_stream->begin(sp);
}

void InstructionHandler::add(int command, void (*callback)(uint16_t id, char* input)) {
  instructions[_instruction_append_index++] = {command, callback};
}

SerialReadingStatus InstructionHandler::_read_from_sream(char* buf) {
  if (_serial_stream->available() == 0)
    return NONE;

  uint16_t index = 0;
  uint32_t start_time = millis();
  char symbol;
  memset(buf, 0, MAX_COMMAND_SIZE);

  while (1) {
    if (_serial_stream->available()) {
      symbol  = _serial_stream->read();
      buf[index++] = symbol;
      if (symbol == '\n') {
        return READY;
      } else if (index >= MAX_COMMAND_SIZE)
        return ERROR;
    }

    if (millis() - start_time > SERIAL_READING_TIMEOUT_MS) {
      return ERROR;
    }
      
  }
}

void InstructionHandler::perform() {
  SerialReadingStatus st = _read_from_sream(command);
  if (st == READY) {
    InstructionStatus is = _handle(command);
    if (is == UNDEFINED_COMMAND) {
      char uc_message[] = "UC";
      _send(uc_message);
    } else if (is == WRONG_INSTRUCTION) {
      char wi_message[] = "WI";
      _send(wi_message);
    }
  } else if (st == ERROR) {
    char error_message[] = "SE";
    _send(error_message);
  }
}

void InstructionHandler::_send(char* message) {
  uint16_t index = 0;
  while (message[index] != 0) {
    _serial_stream->print(message[index++]);
    delay(2);
  }
  _serial_stream->println();
}

uint8_t fetch_command(char* input) {
  char command_array[COMMAND_SIZE + 1] = {0};
  strncpy(command_array, &input[1], COMMAND_SIZE);
  return  (uint8_t)strtol(command_array, NULL, 16);
}

uint16_t fetch_request_id(char* input) {
  char request_id_array[ID_SIZE + 1] = {0};
  strncpy(request_id_array, &input[1 + COMMAND_SIZE + 1], ID_SIZE);
  return (uint16_t)strtol(request_id_array, NULL, 16);
}

void  InstructionHandler::on_finished(uint16_t request_id) {
  char message[] = "$S:%X";
  char message_buffer[RESPONSE_BUFFER_SIZE] = {0};
  sprintf(message_buffer, message, request_id);

  _send(message_buffer);
}

void InstructionHandler::on_failed(uint16_t request_id, uint8_t error_code) {
  char message[] = "$F:%X:%X";
  char message_buffer[RESPONSE_BUFFER_SIZE] = {0};
  snprintf(message_buffer, RESPONSE_BUFFER_SIZE, message, request_id, error_code);

  _send(message_buffer);
}

InstructionStatus InstructionHandler::_handle(char* input) {
  if (input[0] != START_PREFIX || // Validate if starts with command prefix
      input[COMMAND_SIZE + 1] != ID_DEVIDER || // Validate ID devider
      input[COMMAND_SIZE + 1 + ID_SIZE + 1] != PARAMETERS_DEVIDER // Validate parameters devider
     )
    return WRONG_INSTRUCTION;

  uint8_t command = fetch_command(input);
  uint16_t request_id = fetch_request_id(input);

  char value[MAX_INPUT_SIZE] = {0};
  strncpy(value, &input[1 + COMMAND_SIZE + 1 + ID_SIZE + 1], MAX_INPUT_SIZE);

  for (uint16_t i = 0; i < _instruction_append_index; i++) {
    if (instructions[i].command == command) {
      instructions[i].callback(request_id, value);
      return OK;
    }
  }

  return UNDEFINED_COMMAND;
}

void InstructionHandler::add_sensor_output(uint8_t id, uint32_t value) {
  _sensors_output += id;
  _sensors_output += ":";
  _sensors_output += value;
  _sensors_output += ";";
}

void InstructionHandler::reset_sensors_output_buffer() {
  _sensors_output = "@";
}

void InstructionHandler::send_sensors_output() {
  char buf[64] = {0};
  _sensors_output.toCharArray(buf, 64);
  _send(buf);
}

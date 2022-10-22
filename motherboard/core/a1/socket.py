from time import sleep
from typing import List, Optional

import serial
from serial.threaded import LineReader, ReaderThread

from a1.exceptions import A1Exception
from a1.models import A1Data, Job, Response, Request

from utils.logger import Logger


class SerialA1Communicator(LineReader):
    logger = None

    def __init__(self):
        self.data = A1Data()
        self._responses: List[Response] = []
        self._instruction_id_k: int = 0
        super().__init__()

    def connection_made(self, transport):
        super().connection_made(transport)
        self.logger.debug('Connected')

    def handle_line(self, line: str):
        initial_byte = line[0]
        if initial_byte == '@':
            self.data.parse_and_refresh(line)
        # a1_logger.debug(f'Received Data: {self.data}')
        elif initial_byte == '$':
            self._handle_request_response(line)
        else:
            self.logger.debug(f'Unknown: {line}')

    def send_instruction(self, instruction_id: int, parameters: str, timeout: Optional[int] = None) -> Job:
        instruction_id = f'{instruction_id:x}'.rjust(2, '0')
        request_id = self._generate_uid()
        line = f'#{instruction_id}:{request_id}:{parameters if parameters else ""}'

        self.logger.debug(f'Send Instruction:{line}')

        self.write_line(line)
        request = Request(int(request_id, 16), int(instruction_id, 16), parameters)

        return Job(request, self._responses, timeout)

    def _generate_uid(self) -> str:
        if self._instruction_id_k > 0xFFFF:
            self._instruction_id_k = 0

        self._instruction_id_k += 1

        return f"{self._instruction_id_k:x}".rjust(4, '0')

    def _handle_request_response(self, line):
        response = Response.parse(line)
        self.logger.debug(f'Receive Response: {line}')
        self._responses.append(response)


class A1Socket(object):
    def __init__(self, port: str, speed: int, logger: Logger):
        self._serial_con = serial.Serial(baudrate=speed)
        self._serial_con.port = port
        serial_communicator = SerialA1Communicator
        serial_communicator.logger = logger
        self._reader_thread = ReaderThread(self._serial_con, serial_communicator)

    def open(self):
        self._serial_con.open()
        sleep(2)
        self._serial_con.setDTR(False)
        sleep(2)
        self._serial_con.reset_input_buffer()
        self._serial_con.setDTR(True)
        self._reader_thread.start()
        sleep(1)

    @property
    def __protocol(self) -> SerialA1Communicator:
        if not self._reader_thread.protocol:
            raise A1Exception('UART socket is not opened!')
        return self._reader_thread.protocol

    def send_instruction(self, instruction_id: int, parameters: str, timeout: Optional[int] = 1000) -> Job:
        return self.__protocol.send_instruction(instruction_id, parameters, timeout)

    @property
    def data(self) -> A1Data:
        return self.__protocol.data

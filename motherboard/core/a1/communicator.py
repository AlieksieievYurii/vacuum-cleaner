from time import sleep
from typing import List, Optional

import serial
from serial.threaded import LineReader, ReaderThread

from a1.models import A1Data, Job, Response, Request

import logging

logging.basicConfig(level=logging.DEBUG, format='%(asctime)s:%(message)s')


class Handler(LineReader):
    def __init__(self):
        self.data = A1Data()
        self._responses: List[Response] = []
        self._instruction_id_k: int = 0
        super().__init__()

    def connection_made(self, transport):
        super().connection_made(transport)
        logging.debug('Connected')

    def handle_line(self, line: str):
        initial_byte = line[0]
        if initial_byte == '@':
            self.data.parse_and_refresh(line)
            # logging.debug(f'Received Data: {self.data}')
        elif initial_byte == '$':
            self._handle_request_response(line)
        else:
            logging.debug(f'Unknown: {line}')

    def send_instruction(self, instruction_id: int, parameters: str, timeout: Optional[int] = None) -> Job:
        instruction_id = f'{instruction_id:x}'.rjust(2, '0')
        request_id = self._generate_uid()
        line = f'#{instruction_id}:{request_id}:{parameters if parameters else ""}'

        logging.debug(f'Send Instruction:{line}')

        self.write_line(line)
        request = Request(int(request_id, 16), instruction_id, parameters)

        return Job(request, self._responses, timeout)

    def _generate_uid(self) -> str:
        if self._instruction_id_k > 0xFFFF:
            self._instruction_id_k = 0

        self._instruction_id_k += 1

        return f"{self._instruction_id_k:x}".rjust(4, '0')

    def _handle_request_response(self, line):
        response = Response.parse(line)
        logging.debug(f'Receive Response: {line}')
        self._responses.append(response)


class A1(object):
    def __init__(self):
        self._serial_con = serial.Serial(port='COM5', baudrate=9600)
        self._reader_thread = ReaderThread(self._serial_con, Handler)
        self._reader_thread.setDaemon(False)

    def open(self):
        self._serial_con.close()
        sleep(5)
        self._serial_con.open()
        self._serial_con.reset_input_buffer()
        self._serial_con.reset_output_buffer()
        self._reader_thread.start()
        sleep(3)

    def led_wifi(self, on: bool) -> Job:
        return self._handler.send_instruction(0x02, 'H' if on else 'L', timeout=100)

    @property
    def _handler(self) -> Handler:
        return self._reader_thread.protocol


if __name__ == '__main__':
    a1 = A1()
    a1.open()
    flag = False
    while True:
        r = a1.led_wifi(flag).expect()
        print(r)
        flag = flag is False

import time
from time import sleep

import serial
from serial.threaded import LineReader, ReaderThread

from a1.models import A1Data, Job


class Handler(LineReader):
    def __init__(self):
        self.data = A1Data()
        self._responses = []
        super().__init__()

    def connection_made(self, transport):
        super().connection_made(transport)
        print('Connected')

    def handle_line(self, line: str):
        initial_byte = line[0]
        if initial_byte == '@':
            self.data.parse_and_refresh(line)
        elif initial_byte == '$':
            self._handle_request_response(line)
        else:
            print(line)

    def send_instruction(self, instruction_id: str, parameters: str) -> Job:
        instruction_id = f'{instruction_id:x}'.rjust(2, '0')
        request_id = self._generate_uid()
        line = f'#{instruction_id}:{request_id}:{parameters if parameters else ""}'
        self.write_line(line)

        return Job(request_id, self._responses, 0)

    @staticmethod
    def _generate_uid() -> int:
        current_time_ms = str(round(time.time() * 1000))
        return f"{current_time_ms[-4:]}".rjust(4, '0')

    def _handle_request_response(self, line):
        print(line)


if __name__ == '__main__':
    serial_con = serial.Serial(port='COM5', baudrate=9600)
    serial_con.close()
    sleep(5)
    serial_con.open()
    serial_con.reset_input_buffer()
    serial_con.reset_output_buffer()
    a = ReaderThread(serial_con, Handler)
    a.setDaemon(False)
    a.start()
    sleep(3)
    a.protocol.send_instruction(5, '10;f')
    # while True:
    #     print(a.protocol.data.end_right_trig)

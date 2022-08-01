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
        request = Request(int(request_id, 16), int(instruction_id, 16), parameters)

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
        self._serial_con.setDTR(False)
        sleep(2)
        self._serial_con.reset_input_buffer()
        self._serial_con.setDTR(True)
        self._reader_thread.start()
        sleep(1)
        # self._serial_con.close()
        # sleep(5)
        # self._serial_con.open()
        # self._serial_con.reset_input_buffer()
        # self._serial_con.reset_output_buffer()
        #
        # sleep(3)

    def led_wifi(self, on: bool) -> Job:
        return self._handler.send_instruction(0x02, 'H' if on else 'L', timeout=100)

    def walk(self) -> Job:
        return self._handler.send_instruction(0x13, "ff;1")

    def move_back(self):
        return self._handler.send_instruction(0x06, "2;32;2ff;2")

    def turn_vacuum(self):
        return self._handler.send_instruction(0x08, "40")

    def main_brush(self):
        return self._handler.send_instruction(0x12, "1b")

    def left_brush(self):
        return self._handler.send_instruction(0x09, "1f")

    @property
    def _handler(self) -> Handler:
        return self._reader_thread.protocol

    @property
    def input(self) -> A1Data:
        return self._handler.data


class Robot(A1):

    def walk_forward(self, speed: int) -> Job:
        return self._walk(forward=True, speed=speed)

    def walk_backward(self, speed: int) -> Job:
        return self._walk(forward=False, speed=speed)

    def move_forward(self, distance: int, speed: int, with_break: bool = True) -> Job:
        return self._move(distance, speed, forward=True, with_break=with_break)

    def move_backward(self, distance: int, speed: int, with_break: bool = True):
        return self._move(distance, speed, forward=True, with_break=with_break)

    def turn_left(self, angle: int, speed: int, with_break: bool = True) -> Job:
        return self._turn(left=True, angle=angle, speed=speed, with_break=with_break)

    def turn_right(self, angle: int, speed: int, with_break: bool = True) -> Job:
        return self._turn(left=False, angle=angle, speed=speed, with_break=with_break)

    def _move(self, distance: int, speed: int, forward: bool, with_break: bool) -> Job:
        parameters = f'{"1" if forward else "2"};{distance:x};{speed:x};{"1" if with_break else "2"}'
        return self._handler.send_instruction(0x06, parameters)

    def _turn(self, left: bool, angle: int, speed: int, with_break: bool) -> Job:
        parameters = f"{'1' if left else '2'};{angle:x};{speed:x};{'1' if with_break else '2'}"
        return self._handler.send_instruction(0x07, parameters)

    def _walk(self, forward: bool, speed: int) -> Job:
        return self._handler.send_instruction(0x13, f"{'1' if forward else '2'};{speed:x}")


if __name__ == '__main__':
    a1 = Robot()
    a1.open()
    flag = False
    input('Press enter to start...')
    a1.walk_forward(2000).expect()
    # while True:
    #     a1.turn_left(90, 2000, False).expect()
    #     a1.turn_right(90, 1000, False).expect()
    #     print('f')

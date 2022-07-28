import re
import time
from dataclasses import dataclass
from typing import List, Tuple, Optional

import serial


class Connector(object):
    def __init__(self):
        self._comm = serial.Serial(port='COM5', baudrate=9600, timeout=None)

    def write_line(self, line: str) -> None:
        self._comm.write(f'{line}\n')

    def read_line(self) -> str:
        return self._comm.read_until().decode()


@dataclass
class A1Data(object):
    button_up_click: bool
    button_ok_click: bool
    button_down_click: bool
    end_right_trig: bool
    end_left_trig: bool
    end_dust_box_trig: bool
    end_lid_trig: bool

    PATTERN = re.compile(r'(\d):(\d+);')

    @classmethod
    def parse(cls, received_message: str) -> 'A1Data':
        button_states: Tuple[bool] = None
        ends_states: Tuple[bool] = None
        for result in cls.PATTERN.findall(received_message):
            if result[0] == '1':
                button_states = cls._parse_buttons_state(int(result[1]))
            elif result[0] == '2':
                ends_states = cls._parse_ends_state(int(result[1]))

        return cls(
            button_up_click=button_states[0],
            button_ok_click=button_states[1],
            button_down_click=button_states[2],
            end_right_trig=ends_states[0],
            end_left_trig=ends_states[1],
            end_lid_trig=ends_states[2],
            end_dust_box_trig=ends_states[3]
        )

    @staticmethod
    def _parse_buttons_state(value: int) -> Tuple[bool]:
        return (
            False,
            False,
            False
        )

    @staticmethod
    def _parse_ends_state(value: int) -> Tuple[bool]:
        return (
            bool(value & 0x1),  # Right End
            bool(value & 0x2),  # Left End
            bool(value & 0x4),  # Lid End
            bool(value & 0x8),  # Dust Box End
        )


class Job(object):
    def __init__(self, job_id: str, connector: Connector):
        self.job_id = job_id
        self._connector = connector


@dataclass
class InstructionStatus(object):
    instruction_id: str
    successful: bool

    @classmethod
    def parse(cls, string: str):
        pass


class ExpComm(object):
    def __init__(self):
        self.comm = serial.Serial(port='COM5', baudrate=9600, timeout=None)
        self.data: A1Data = None
        self.resp_buff = []

    def init_com(self):
        while True:
            response = self.comm.readline().decode()
            if response[0] == '@' and response[-1] == '\n':
                return None

    def tick(self):
        fist_byte = self.comm.read().decode()
        if fist_byte == '@':
            self.data = A1Data.parse(self.comm.read_until().decode())
        elif fist_byte == '$':
            print(self.comm.read_until().decode())

    def _send_instruction(self, instruction_id: int, parameters: Optional[str] = None):
        instruction_id = f'{instruction_id:x}'.rjust(2, '0')
        self.comm.write(f'#{instruction_id}:{self._generate_uid()}:{parameters if parameters else ""}')

    @staticmethod
    def _generate_uid() -> str:
        current_time_ms = str(round(time.time() * 1000))
        return f"{current_time_ms[-4:]}".rjust(2, '0')

    def move_back(self):
        self.comm.write(f'#06:ff11:2;28;500;2\n'.encode())


if __name__ == '__main__':
    a = ExpComm()
    a.init_com()

    while True:
        a.tick()
        if a.data.end_right_trig:
            a.move_back()
            print('dupa')

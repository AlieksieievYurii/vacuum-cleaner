import re
from typing import List


class Response(object):
    id: str


class Job(object):
    def __init__(self, request_id: str, responses: List, timeout: int):
        self._request_id = request_id
        self._responses = responses
        self._timeout = timeout

    @property
    def is_finished(self) -> bool:
        for resp in self._responses:
            if resp.id == self._request_id:
                return True
        return False


class A1Data(object):
    __PATTERN = re.compile(r'(\d):(\d+);')

    def __init__(self):
        self.button_up_click: bool = False
        self.button_ok_click: bool = False
        self.button_down_click: bool = False
        self.end_right_trig: bool = False
        self.end_left_trig: bool = False
        self.end_dust_box_trig: bool = False
        self.end_lid_trig: bool = False

    def parse_and_refresh(self, string: str):
        for result in self.__PATTERN.findall(string):
            sensor_id = result[0]
            if sensor_id == '1':
                self._parse_and_set_buttons_state(int(result[1]))
            elif sensor_id == '2':
                self._parse_and_set_ends_state(int(result[1]))

    def _parse_and_set_buttons_state(self, value: int) -> None:
        # TODO implement
        self.button_up_click = False
        self.button_ok_click = False
        self.button_down_click = False

    def _parse_and_set_ends_state(self, value: int) -> None:
        self.end_right_trig = bool(value & 0x1)
        self.end_left_trig = bool(value & 0x2)
        self.end_dust_box_trig = bool(value & 0x4)
        self.end_lid_trig = bool(value & 0x8)

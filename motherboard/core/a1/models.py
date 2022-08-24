import re
from dataclasses import dataclass
from typing import List, Optional

from a1.exceptions import CannotParse, InstructionTimeout, InstructionFailureException
from utils.utils import millis


@dataclass
class Request(object):
    """
    Class represents request model for A1 module.
    """
    id: int
    instruction_id: int
    parameters: str


@dataclass
class Response(object):
    """
    Class represents response module from A1 module. There is a factory function which parses the line response
    """
    id: int
    is_successful: bool
    timestamp: int
    _error_code: Optional[int] = None
    _data: Optional[str] = None

    __REGEX = re.compile(r'\$([SFR]):([\dABCDEF]{1,4})(:(.*))?')

    @property
    def data(self) -> str:
        if not self._data:
            raise Exception('This response is not supposed to have data')
        return self._data

    @property
    def error_code(self):
        if self.is_successful:
            raise Exception('The response is successful. So it does not have error code')
        return self._error_code

    @classmethod
    def parse(cls, string: str) -> 'Response':
        parsed_groups = cls.__REGEX.match(string)
        if not parsed_groups:
            raise CannotParse(f"Cannot parse the response from A1: {string}")

        if parsed_groups.group(1) in ('S', 'R'):
            is_successful = True
            error_code = None
        else:
            is_successful = False
            error_code = parsed_groups.group(4)

        return cls(
            id=int(parsed_groups.group(2), 16),
            is_successful=is_successful,
            _error_code=error_code,
            _data=parsed_groups.group(4),
            timestamp=millis()
        )


class Job(object):
    def __init__(self, request: Request, responses: List, timeout: Optional[int] = None):
        self._request = request
        self._responses = responses
        self._timeout: Optional[int] = timeout
        self._time = millis()
        self._response: Optional[Response] = None

    def expect(self) -> Response:
        while True:
            if self.response:
                return self.response

    @property
    def response(self) -> Optional[Response]:
        if self._response:
            return self._response

        if self._timeout and millis() - self._time >= self._timeout:
            raise InstructionTimeout(self._request, self._timeout)
        else:
            self._time = millis()

        for index, resp in enumerate(self._responses):
            if resp.id == self._request.id:
                self._response = self._responses.pop(index)
                if not self._response.is_successful:
                    raise InstructionFailureException(self._request, self._response.error_code)
                return self._response


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
        self.rangefinder_left_value: int = 0
        self.rangefinder_center_value: int = 0
        self.rangefinder_right_value: int = 0
        self.back_left_cliff_breakage: bool = False
        self.back_center_cliff_breakage: bool = False
        self.back_right_cliff_breakage: bool = False
        self.front_left_cliff_breakage: bool = False
        self.front_center_cliff_breakage: bool = False
        self.front_right_cliff_breakage: bool = False
        self.is_about_to_shut_down: bool = False  # Todo
        self.cell_a_voltage: float = 0.0
        self.cell_b_voltage: float = 0.0
        self.cell_c_voltage: float = 0.0
        self.cell_d_voltage: float = 0.0

    def parse_and_refresh(self, string: str):
        parsers = {
            0x1: self._parse_and_set_buttons_state,
            0x2: self._parse_and_set_ends_state,
            0x3: self._parse_dis_values,
            0x4: self._parse_cliffs,
            0x6: self._parse_voltages_of_battery_cells
        }
        for result in self.__PATTERN.findall(string):
            sensor_id = int(result[0], 16)
            value = int(result[1])
            f = parsers.get(sensor_id)
            if f:
                f(value)

    def _parse_cliffs(self, value: int) -> None:
        self.back_right_cliff_breakage = bool(value >> 0x0 & 0x1)
        self.back_center_cliff_breakage = bool(value >> 0x1 & 0x1)
        self.back_left_cliff_breakage = bool(value >> 0x2 & 0x1)
        self.front_right_cliff_breakage = bool(value >> 0x3 & 0x1)
        self.front_center_cliff_breakage = bool(value >> 0x4 & 0x1)
        self.front_left_cliff_breakage = bool(value >> 0x5 & 0x1)

    def _parse_voltages_of_battery_cells(self, value: int) -> None:
        def bin_to_float(v: int) -> float:
            return (v >> 0x4) + ((v & 0xF) / 10)

        self.cell_a_voltage = bin_to_float(value & 0xFF)
        self.cell_b_voltage = bin_to_float((value >> 0x8) & 0xFF)
        self.cell_c_voltage = bin_to_float((value >> 0x10) & 0xFF)
        self.cell_d_voltage = bin_to_float((value >> 0x18) & 0xFF)

    def _parse_and_set_buttons_state(self, value: int) -> None:
        # TODO implement
        self.button_up_click = False
        self.button_ok_click = False
        self.button_down_click = False

    def _parse_and_set_ends_state(self, value: int) -> None:
        self.end_right_trig = bool(value & 0x1)
        self.end_left_trig = bool(value & 0x2)
        self.end_lid_trig = bool(value & 0x4)
        self.end_dust_box_trig = bool(value & 0x8)

    def _parse_dis_values(self, value: int) -> None:
        self.rangefinder_left_value = value & 0xFF
        self.rangefinder_center_value = value >> 0x8 & 0xFF
        self.rangefinder_right_value = value >> 0x10 & 0xFF

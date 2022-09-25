import re
from dataclasses import dataclass
from enum import Enum
from typing import List, Optional

from a1.exceptions import CannotParse, InstructionTimeout, InstructionFailureException
from utils.utils import millis, constrain_number


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

    def raise_if_failed(self):
        if not self.is_successful:
            raise Exception(f'The instruction has failed! ID:{self.id}. Error Code: {self.error_code}')

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

        for index, resp in enumerate(self._responses):
            if resp.id == self._request.id:
                self._response = self._responses.pop(index)
                if not self._response.is_successful:
                    raise InstructionFailureException(self._request, self._response.error_code)
                return self._response


class ButtonState(Enum):
    NOTHING = 0
    CLICK = 1
    LONG_PRESS = 3


class ChargingState(Enum):
    NO_CHARGING = 0
    CHARGING = 1
    CHARGED = 2


class A1Data(object):
    __PATTERN = re.compile(r'(\d):(\d+);')
    MAX_BATTERY_VOLTAGE: float = 16.7
    MIN_BATTERY_VOLTAGE: float = 13.10

    def __init__(self):
        self._button_up_click: ButtonState = ButtonState.NOTHING
        self._button_ok_click: ButtonState = ButtonState.NOTHING
        self._button_down_click: ButtonState = ButtonState.NOTHING
        self._bluetooth_button_click: ButtonState = ButtonState.NOTHING
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
        self.battery_voltage: float = 0.0
        self.battery_capacity: int = 0  # 0...100
        self.is_shut_down_button_triggered: bool = False
        self.charging_state: ChargingState = ChargingState.NO_CHARGING

    @property
    def button_up(self) -> ButtonState:
        if self._button_up_click is not ButtonState.NOTHING:
            but = self._button_up_click
            self._button_up_click = ButtonState.NOTHING
            return but
        return self._button_up_click

    @property
    def button_ok(self) -> ButtonState:
        if self._button_ok_click is not ButtonState.NOTHING:
            but = self._button_ok_click
            self._button_ok_click = ButtonState.NOTHING
            return but
        return self._button_ok_click

    @property
    def button_down(self) -> ButtonState:
        if self._button_down_click is not ButtonState.NOTHING:
            but = self._button_down_click
            self._button_down_click = ButtonState.NOTHING
            return but
        return self._button_down_click

    @property
    def bluetooth_button(self) -> ButtonState:
        if self._bluetooth_button_click is not ButtonState.NOTHING:
            but = self._bluetooth_button_click
            self._bluetooth_button_click = ButtonState.NOTHING
            return but
        return self._bluetooth_button_click

    def parse_and_refresh(self, string: str):
        parsers = {
            0x1: self._parse_and_set_buttons_state,
            0x2: self._parse_and_set_ends_state,
            0x3: self._parse_dis_values,
            0x4: self._parse_cliffs,
            0x5: self._parse_power_controller_states,
            0x6: self._parse_battery_voltage_value
        }
        for result in self.__PATTERN.findall(string):
            sensor_id = int(result[0], 16)
            value = int(result[1])
            f = parsers.get(sensor_id)
            if f:
                f(value)

    def _parse_power_controller_states(self, value: int) -> None:
        power_state = value & 0x3  # Fetch first 3 bits which represents power state
        # 1 - booting up
        # 2 - 0x0 (TURNED_OFF)
        # 0x1 (BOOTING_UP)
        # 0x2 (TURNED_ON)
        # 0x3 (SHUTTING_DOWN)
        self.is_shut_down_button_triggered = power_state == 0x3
        self.charging_state = ChargingState((value >> 3) & 0x3)

    def _parse_cliffs(self, value: int) -> None:
        self.back_right_cliff_breakage = bool(value >> 0x0 & 0x1)
        self.back_center_cliff_breakage = bool(value >> 0x1 & 0x1)
        self.back_left_cliff_breakage = bool(value >> 0x2 & 0x1)
        self.front_right_cliff_breakage = bool(value >> 0x3 & 0x1)
        self.front_center_cliff_breakage = bool(value >> 0x4 & 0x1)
        self.front_left_cliff_breakage = bool(value >> 0x5 & 0x1)

    def _parse_battery_voltage_value(self, value: int) -> None:
        decimal_part = value & 0xff
        integer_part = (value >> 0x8) & 0xff
        self.battery_voltage = integer_part + decimal_part / 10

        capacity = round((self.battery_voltage - self.MIN_BATTERY_VOLTAGE) * 100 / (
                self.MAX_BATTERY_VOLTAGE - self.MIN_BATTERY_VOLTAGE))
        self.battery_capacity = constrain_number(capacity, 0, 100)

    def _parse_and_set_buttons_state(self, value: int) -> None:
        if self._bluetooth_button_click is ButtonState.NOTHING:
            self._bluetooth_button_click = ButtonState((value >> 0x6) & 0x3)

        if self._button_up_click is ButtonState.NOTHING:
            self._button_up_click = ButtonState((value >> 0x4) & 0x3)

        if self._button_ok_click is ButtonState.NOTHING:
            self._button_ok_click = ButtonState((value >> 0x2) & 0x3)

        if self._button_down_click is ButtonState.NOTHING:
            self._button_down_click = ButtonState(value & 0x3)

    def _parse_and_set_ends_state(self, value: int) -> None:
        self.end_right_trig = bool(value & 0x1)
        self.end_left_trig = bool(value & 0x2)
        self.end_lid_trig = bool(value & 0x4)
        self.end_dust_box_trig = bool(value & 0x8)

    def _parse_dis_values(self, value: int) -> None:
        self.rangefinder_left_value = value & 0xFF
        self.rangefinder_center_value = value >> 0x8 & 0xFF
        self.rangefinder_right_value = value >> 0x10 & 0xFF

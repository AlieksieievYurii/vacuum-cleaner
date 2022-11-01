import abc
from abc import ABC
from enum import Enum
from typing import Optional

from a1.models import Job, A1Data, Response, Request
from utils.logger import Logger
from utils.utils import millis


class LedState(Enum):
    OFF = 'L'
    ON = 'H'
    BLINKING = 'B'


class Robot(ABC):
    @abc.abstractmethod
    def core_is_initialized(self, is_successful: bool) -> None:
        pass

    @abc.abstractmethod
    def set_error_state(self, enable: bool = True) -> None:
        pass

    @abc.abstractmethod
    def beep(self, count: int = 3, period: int = 100) -> None:
        pass

    @abc.abstractmethod
    def set_bluetooth_led_state(self, green: bool, state: LedState) -> None:
        pass

    @abc.abstractmethod
    def set_wifi_led(self, state: LedState) -> None:
        pass

    @abc.abstractmethod
    def set_error_led(self, state: LedState) -> None:
        pass

    @abc.abstractmethod
    def set_vacuum_motor(self, value: int) -> Job:
        pass

    @abc.abstractmethod
    def set_left_brush_motor(self, value: int) -> Job:
        pass

    @abc.abstractmethod
    def set_right_brush_motor(self, value: int) -> Job:
        pass

    @abc.abstractmethod
    def set_main_brush_motor(self, value: int) -> Job:
        pass

    @abc.abstractmethod
    def walk_forward(self, speed: int) -> Job:
        pass

    @abc.abstractmethod
    def walk_backward(self, speed: int) -> Job:
        pass

    @abc.abstractmethod
    def move_forward(self, distance: int, speed: int, with_break: bool = True) -> Job:
        pass

    @abc.abstractmethod
    def move_backward(self, distance: int, speed: int, with_break: bool = True):
        pass

    @abc.abstractmethod
    def turn_left(self, angle: int, speed: int, with_break: bool = True) -> Job:
        pass

    @abc.abstractmethod
    def turn_right(self, angle: int, speed: int, with_break: bool = True) -> Job:
        pass

    @abc.abstractmethod
    def rotate_right(self, speed: int) -> Job:
        pass

    @abc.abstractmethod
    def rotate_left(self, speed: int) -> Job:
        pass

    @abc.abstractmethod
    def stop_movement(self, with_break: bool) -> Job:
        pass

    @abc.abstractmethod
    def get_date_time(self) -> Job:
        pass

    @abc.abstractmethod
    def set_date_time(self, datetime) -> Job:
        pass

    @abc.abstractmethod
    def set_pid(self, p: float, i: float, d: float) -> Job:
        pass

    @abc.abstractmethod
    def set_timer_to_cut_off_power(self, seconds: int) -> Job:
        pass

    @abc.abstractmethod
    def set_booting_up_led(self) -> Job:
        pass

    @abc.abstractmethod
    def set_shutting_down_led(self) -> Job:
        pass

    @property
    @abc.abstractmethod
    def data(self) -> A1Data:
        pass

    def connect(self) -> None:
        # Optional method to override
        pass


class RobotUART(Robot):
    def __init__(self, socket, logger: Logger):
        self._socket = socket
        self._logger = logger

    def core_is_initialized(self, is_successful: bool) -> None:
        """
        Sends the signal to A1 to inform that the core is ready!

        :param is_successful: True if yes
        :return: None
        """

        resp = self._socket.send_instruction(0x01, 'S' if is_successful else 'F').expect()
        resp.raise_if_failed()

    def beep(self, count: int = 3, period: int = 100) -> None:
        """
        Sends the signal to A1 to make beeps

        :param count: beep cont
        :param period: period between beeps
        :return: None
        """
        self._socket.send_instruction(0x05, f'{count:x};{period:x}', timeout=None).expect().raise_if_failed()

    def set_error_state(self, enable: bool = True) -> None:
        self._socket.send_instruction(0x11, f'{"T" if enable else "F"}').expect().raise_if_failed()

    def set_bluetooth_led_state(self, green: bool, state: LedState) -> None:
        self._socket.send_instruction(0x17, f'{state.value};{"G" if green else "R"}').expect().raise_if_failed()

    def set_wifi_led(self, state: LedState) -> None:
        self._socket.send_instruction(0x02, f'{state.value}').expect().raise_if_failed()

    def set_error_led(self, state: LedState) -> None:
        self._socket.send_instruction(0x03, f'{state.value}').expect().raise_if_failed()

    def set_vacuum_motor(self, value: int) -> Job:
        return self._socket.send_instruction(0x08, f'{value:x}')

    def set_left_brush_motor(self, value: int) -> Job:
        return self._socket.send_instruction(0x09, f'{value:x}')

    def set_right_brush_motor(self, value: int) -> Job:
        return self._socket.send_instruction(0x0A, f'{value:x}')

    def set_main_brush_motor(self, value: int) -> Job:
        return self._socket.send_instruction(0x12, f'{value:x}')

    def walk_forward(self, speed: int) -> Job:
        return self._walk(forward=True, speed=speed)

    def walk_backward(self, speed: int) -> Job:
        return self._walk(forward=False, speed=speed)

    def move_forward(self, distance: int, speed: int, with_break: bool = True) -> Job:
        return self._move(distance, speed, forward=True, with_break=with_break)

    def move_backward(self, distance: int, speed: int, with_break: bool = True):
        return self._move(distance, speed, forward=False, with_break=with_break)

    def turn_left(self, angle: int, speed: int, with_break: bool = True) -> Job:
        return self._turn(left=True, angle=angle, speed=speed, with_break=with_break)

    def turn_right(self, angle: int, speed: int, with_break: bool = True) -> Job:
        return self._turn(left=False, angle=angle, speed=speed, with_break=with_break)

    def rotate_right(self, speed: int) -> Job:
        return self._rotate(left=False, speed=speed)

    def rotate_left(self, speed: int) -> Job:
        return self._rotate(left=True, speed=speed)

    def stop_movement(self, with_break: bool) -> Job:
        return self.move_forward(0, 0, with_break)

    def get_date_time(self) -> Job:
        return self._socket.send_instruction(0x0C, 'Y-d-m H:i:s')

    def set_date_time(self, datetime) -> Job:
        parameters = f'{datetime.year:x};{datetime.month:x};{datetime.day:x};' \
                     f'{datetime.hour:x};{datetime.minute:x};{datetime.second:x}'
        return self._socket.send_instruction(0x0D, parameters)

    def set_pid(self, p: float, i: float, d: float) -> Job:
        return self._socket.send_instruction(0x15, f'{p};{i};{d}')

    def set_timer_to_cut_off_power(self, seconds: int) -> Job:
        return self._socket.send_instruction(0xFF, f'{seconds:x}')

    def set_booting_up_led(self) -> Job:
        return self._socket.send_instruction(0x16, '')

    def set_shutting_down_led(self) -> Job:
        return self._socket.send_instruction(0x0F, '')

    def _rotate(self, left: bool, speed: int) -> Job:
        parameters = f'{"1" if left else "2"};{speed:x}'
        return self._socket.send_instruction(0x14, parameters)

    def _move(self, distance: int, speed: int, forward: bool, with_break: bool) -> Job:
        self._logger.info(f'Move: {"F" if forward else "B"}; Dis: {distance} CM; Speed: {speed}; Break: {with_break}')
        parameters = f'{"1" if forward else "2"};{distance:x};{speed:x};{"1" if with_break else "2"}'
        return self._socket.send_instruction(0x06, parameters, timeout=None)

    def _turn(self, left: bool, angle: int, speed: int, with_break: bool) -> Job:
        self._logger.info(f'Turn: {"L" if left else "R"}; Angle: {angle}; Speed: {speed}; Break: {with_break}')
        parameters = f"{'1' if left else '2'};{angle:x};{speed:x};{'1' if with_break else '2'}"
        return self._socket.send_instruction(0x07, parameters, timeout=None)

    def _walk(self, forward: bool, speed: int) -> Job:
        self._logger.info(f'Walk: {"F" if forward else "B"}; Speed: {speed}')
        return self._socket.send_instruction(0x13, f"{'1' if forward else '2'};{speed:x}")

    @property
    def data(self) -> A1Data:
        return self._socket.data

    def connect(self) -> None:
        self._socket.open()


class RobotMockUp(Robot):
    def __init__(self, logger: Logger):
        self._id = 1
        self._logger = logger

    def core_is_initialized(self, is_successful: bool) -> None:
        self._logger.debug(f'Send: Core is initialized: Successful: {is_successful}')

    def beep(self, count: int = 3, period: int = 100) -> None:
        self._logger.debug(f'Send: Beep -> Count: {count}; period: {period}')

    def set_bluetooth_led_state(self, green: bool, state: LedState) -> None:
        self._logger.debug(f'Send: Set Bluetooth led({"green" if green else "red"}) state -> state: {state}')

    def set_wifi_led(self, state: LedState) -> None:
        self._logger.debug(f'Send: Set Wifi Led state -> state: {state}')

    def set_error_led(self, state: LedState) -> None:
        self._logger.debug(f'Send: Set Error Led state -> state: {state}')

    def set_error_state(self, enable: bool = True) -> None:
        self._logger.debug(f'Send: Set Error State({"T" if enable else "F"})')

    def set_vacuum_motor(self, value: int) -> Job:
        self._logger.debug(f'Send: set_vacuum_motor. Value: {value}')
        return self._send_instruction(1)

    def set_left_brush_motor(self, value: int) -> Job:
        self._logger.debug(f'Send: set_left_brush_motor. Value: {value}')
        return self._send_instruction(2)

    def set_right_brush_motor(self, value: int) -> Job:
        self._logger.debug(f'Send: set_right_brush_motor. Value: {value}')
        return self._send_instruction(3)

    def set_main_brush_motor(self, value: int) -> Job:
        self._logger.debug(f'Send: set_main_brush_motor. Value: {value}')
        return self._send_instruction(4)

    def walk_forward(self, speed: int) -> Job:
        self._logger.debug(f'Send: walk_forward. Speed: {speed}')
        return self._send_instruction(5)

    def walk_backward(self, speed: int) -> Job:
        self._logger.debug(f'Send: walk_backward. Speed: {speed}')
        return self._send_instruction(6)

    def move_forward(self, distance: int, speed: int, with_break: bool = True) -> Job:
        self._logger.debug(f'Send: move_forward. Distance: {distance}; Speed: {speed}; Break: {with_break}')
        return self._send_instruction(7)

    def move_backward(self, distance: int, speed: int, with_break: bool = True):
        self._logger.debug(f'Send: move_backward. Distance: {distance}; Speed: {speed}; Break: {with_break}')
        return self._send_instruction(8)

    def turn_left(self, angle: int, speed: int, with_break: bool = True) -> Job:
        self._logger.debug(f'Send: turn_left. Angle: {angle}; Speed: {speed}; Break: {with_break}')
        return self._send_instruction(9)

    def turn_right(self, angle: int, speed: int, with_break: bool = True) -> Job:
        self._logger.debug(f'Send: turn_right. Angle: {angle}; Speed: {speed}; Break: {with_break}')
        return self._send_instruction(10)

    def rotate_right(self, speed: int) -> Job:
        self._logger.debug(f'Send: rotate_right. Speed: {speed}')
        return self._send_instruction(11)

    def rotate_left(self, speed: int) -> Job:
        self._logger.debug(f'Send: rotate_left. Speed: {speed}')
        return self._send_instruction(12)

    def stop_movement(self, with_break: bool) -> Job:
        self._logger.debug(f'Send: stop_movement. Break: {with_break}')
        return self._send_instruction(13)

    def get_date_time(self) -> Job:
        self._logger.debug(f'Send: get_date_time')
        return self._send_instruction(14, response='2022-3-4 14:22:12')

    def set_date_time(self, datetime) -> Job:
        self._logger.debug(f'Send: set_date_time. Datetime: {datetime}')
        return self._send_instruction(15)

    def set_pid(self, p: float, i: float, d: float) -> Job:
        self._logger.debug(f'Send: set_pid. P: {p}; I: {i}; D: {d}')
        return self._send_instruction(16)

    def set_timer_to_cut_off_power(self, seconds: int) -> Job:
        self._logger.debug(f'Send: set_timer_to_cut_off_power. Seconds: {seconds}')
        return self._send_instruction(17)

    def set_booting_up_led(self) -> Job:
        self._logger.debug(f'Send: set_booting_up_led')
        return self._send_instruction(18)

    def set_shutting_down_led(self) -> Job:
        self._logger.debug(f'Send: set_shutting_down_led')
        return self._send_instruction(19)

    def _send_instruction(self, instruction_id: int, response: Optional[str] = None) -> Job:
        response = Response(id=self._id, is_successful=True, timestamp=millis(), _data=response)
        job = Job(Request(self._id, instruction_id, ''), [response], 1000)
        self._id += 1
        return job

    @property
    def data(self) -> A1Data:
        return A1Data()

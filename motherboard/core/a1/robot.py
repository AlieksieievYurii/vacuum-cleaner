from a1.models import Job, A1Data
from a1.socket import A1Socket
from utils.logger import a1_logger


class Robot(object):
    def __init__(self, socket: A1Socket):
        self._socket = socket

    def core_is_initialized(self, is_successful: bool):
        return self._socket.send_instruction(0x01, 'S' if is_successful else 'F')

    def beep(self, count: int = 3, period: int = 100) -> Job:
        return self._socket.send_instruction(0x05, f'{count:x};{period:x}')

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

    def set_timer_to_cut_off_power(self, seconds: int) -> Job:
        return self._socket.send_instruction(0xFF, f'{seconds:x}')

    def _rotate(self, left: bool, speed: int) -> Job:
        parameters = f'{"1" if left else "2"};{speed:x}'
        return self._socket.send_instruction(0x14, parameters)

    def _move(self, distance: int, speed: int, forward: bool, with_break: bool) -> Job:
        a1_logger.print_movement(forward, speed, distance, with_break)
        parameters = f'{"1" if forward else "2"};{distance:x};{speed:x};{"1" if with_break else "2"}'
        return self._socket.send_instruction(0x06, parameters)

    def _turn(self, left: bool, angle: int, speed: int, with_break: bool) -> Job:
        a1_logger.print_turn(left, speed, angle, with_break)
        parameters = f"{'1' if left else '2'};{angle:x};{speed:x};{'1' if with_break else '2'}"
        return self._socket.send_instruction(0x07, parameters)

    def _walk(self, forward: bool, speed: int) -> Job:
        a1_logger.print_movement(forward, speed, distance=None, with_stop=False)
        return self._socket.send_instruction(0x13, f"{'1' if forward else '2'};{speed:x}")

    @property
    def data(self) -> A1Data:
        return self._socket.data

    def connect(self) -> None:
        self._socket.open()

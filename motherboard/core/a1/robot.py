from a1.models import Job
from a1.socket import A1Socket
from utils.logger import a1_logger


class Robot(object):
    def __init__(self, socket: A1Socket):
        self._socket = socket

    def beep(self, count: int = 3, period: int = 100) -> Job:
        return self._socket.send_instruction(0x05, f'{count:x};{period:x}')

    def set_vacuum_motor(self, value: int) -> Job:
        return self._socket.send_instruction(0x08, f'{value:x}')

    def set_left_brush_motor(self, value: int) -> Job:
        return self._socket.send_instruction(0x09, f'{value:x}')

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

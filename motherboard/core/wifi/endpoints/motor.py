from typing import Callable

from a1.robot import Robot
from utils.request_handler.models import RequestHandler, Request, AttributeHolder, Field


class MotorEndpointException(Exception):
    pass


class RequestModel(object):
    motor_name = Field('motor_name', str, is_required=True)
    value = Field('value', int, is_required=True)


class Motor(RequestHandler):
    endpoint = '/set-motor'
    request_model = RequestModel
    response_model = None

    def __init__(self, robot: Robot):
        self._motors = {
            'vacuum': robot.set_vacuum_motor,
            'main_brush': robot.set_main_brush_motor,
            'left_brush': robot.set_left_brush_motor,
            'right_brush': robot.set_right_brush_motor
        }

    def perform(self, request: Request, data: AttributeHolder) -> None:
        if data.value > 100 or data.value < 0:
            raise MotorEndpointException('Value must be in range 0..100')

        motor_fun_call: Callable = self._motors.get(data.motor_name)

        if motor_fun_call:
            motor_fun_call(data.value)
        else:
            raise MotorEndpointException(f'There is not such motor name "{data.motor_name}"')

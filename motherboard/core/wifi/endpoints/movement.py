from a1.robot import Robot
from utils.request_handler.models import RequestHandler, Request, AttributeHolder, Field
from utils.utils import boolean


class MovementRequestModel(object):
    direction = Field('direction', str, is_required=True)
    speed = Field('speed', int, is_required=True)


class StopMovementRequestModel(object):
    with_break = Field('with_break', boolean, is_required=True)


class StopMovement(RequestHandler):
    endpoint = '/stop-movement'
    request_model = StopMovementRequestModel
    response_model = None

    def __init__(self, robot: Robot):
        self._robot = robot

    def perform(self, request: Request, data: AttributeHolder):
        self._robot.stop_movement(data.with_break)


class Movement(RequestHandler):
    endpoint = '/movement'
    request_model = MovementRequestModel
    response_model = None

    def __init__(self, robot: Robot):
        self._movements = {
            'forward': robot.walk_forward,
            'backward': robot.walk_backward,
            'left': robot.rotate_left,
            'right': robot.rotate_right
        }

    def perform(self, request: Request, data: AttributeHolder):
        if data.speed < 0 or data.speed > 3000:
            raise Exception('Wrong speed value. Speed must be in range 0..2000')

        movement_call = self._movements.get(data.direction)

        if movement_call:
            movement_call(data.speed)
        else:
            raise Exception(f'Wrong direction "{data.direction}"')

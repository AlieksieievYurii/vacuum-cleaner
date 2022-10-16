import random

from a1.robot import Robot
from algo.algorithms.algorithm import Algorithm, FieldParameter, ArgumentsHolder, ExecutionState

from utils.utils import random_true


class Simple(Algorithm):
    NAME: str = 'simple'
    DESCRIPTION: str = 'Just simple algorithm of cleaning'

    speed = FieldParameter('speed', range(0, 2000), default=1000)
    reverse_dis = FieldParameter('reverse_distance', range(0, 1000), default=20)
    reverse_dis_speed = FieldParameter('reverse_distance_speed', range(0, 2000), default=1000)
    reverse_with_break = FieldParameter('reverse_with_break', bool, default=False)

    # test = FieldParameter('test', ['1', '2', '3'], default='2')
    # test2 = FieldParameter('test2', int, default=1)
    # test233 = FieldParameter('test22', float, default=1.0)
    # test3 = FieldParameter('test3', bool, default=False)
    # test31 = FieldParameter('test31', bool, default=False)
    # test32 = FieldParameter('test32', bool, default=True)
    # test33 = FieldParameter('test33', bool, default=True)
    # test4 = FieldParameter('test4', str, default='dupa')

    def __init__(self, arguments: ArgumentsHolder):
        super().__init__(arguments)

        self._is_lower_speed = False

    def loop(self, robot: Robot, state: ExecutionState):
        while not state.is_break_event:
            robot.walk_forward(self._args.speed).expect()
            while not state.is_break_event:
                self._correct_speed(robot)
                if self._check_bumpers(robot, state):
                    break

    def _check_bumpers(self, robot: Robot, state: ExecutionState) -> bool:
        if robot.data.end_right_trig or robot.data.end_left_trig:
            self._move_backward(robot)
            if state.is_break_event:
                return False

            if random_true():
                self._turn_left(robot, random.randint(10, 180))
            else:
                self._turn_right(robot, random.randint(10, 180))
            return True

        return False

    def _correct_speed(self, robot: Robot):
        if robot.data.rangefinder_right_value + robot.data.rangefinder_left_value \
                + robot.data.rangefinder_center_value < 750:
            if not self._is_lower_speed:
                robot.walk_forward(self._args.speed // 2).expect()
                self._is_lower_speed = True
        elif self._is_lower_speed:
            robot.walk_forward(self._args.speed).expect()
            self._is_lower_speed = False

    def on_prepare(self, robot: Robot):
        robot.set_left_brush_motor(10)

    def on_pause(self, robot: Robot):
        robot.stop_movement(True)
        robot.set_left_brush_motor(2)

    def on_resume(self, robot: Robot):
        robot.set_left_brush_motor(10)

    def on_finish(self, robot: Robot):
        robot.stop_movement(True)
        robot.set_left_brush_motor(0)

    def _move_backward(self, robot: Robot) -> None:
        robot.move_backward(self._args.reverse_dis, self._args.reverse_dis_speed,
                            self._args.reverse_with_break).expect()

    def _turn_left(self, robot: Robot, angle: int):
        robot.turn_left(angle, self._args.speed, True).expect()

    def _turn_right(self, robot: Robot, angle: int):
        robot.turn_right(angle, self._args.speed, True).expect()

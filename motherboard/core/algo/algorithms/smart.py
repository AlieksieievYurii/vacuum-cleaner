from a1.robot import Robot
from algo.algorithms.algorithm import Algorithm, FieldParameter, ExecutionState


class Smart(Algorithm):
    NAME: str = 'smart'
    DESCRIPTION: str = 'Just smart algorithm of cleaning'

    speed_argument = FieldParameter('speed', range(0, 1000), default=50)
    test = FieldParameter('test3', float, default=1.0)

    def init(self, arguments):
        pass

    def loop(self, robot: Robot, state: ExecutionState):
        pass

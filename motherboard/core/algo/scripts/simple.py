from a1.models import A1Data
from algo.scripts.script import Script, Argument


class Simple(Script):
    NAME: str = 'simple'
    DESCRIPTION: str = 'Just simple algorithm of cleaning'

    speed_argument = Argument('speed', range(0, 100), default=50)
    test = Argument('test', [1, 2, 3], default=1)
    test2 = Argument('test2', int, default=1)
    test3 = Argument('test3', bool, default=False)

    def init(self, arguments):
        pass

    def loop(self, data: A1Data):
        pass

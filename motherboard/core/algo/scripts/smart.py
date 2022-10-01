from a1.models import A1Data
from algo.scripts.script import Script, Argument


class Smart(Script):
    NAME: str = 'smart'
    DESCRIPTION: str = 'Just smart algorithm of cleaning'

    speed_argument = Argument('speed', range(0, 1000), default=50)
    test = Argument('test3', float, default=1.0)


    def init(self, arguments):
        pass

    def loop(self, data: A1Data):
        pass

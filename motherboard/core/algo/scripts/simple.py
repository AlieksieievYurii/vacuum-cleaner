from a1.models import A1Data
from algo.scripts.script import Script, FieldParameter


class Simple(Script):
    NAME: str = 'simple'
    DESCRIPTION: str = 'Just simple algorithm of cleaning'

    speed_argument = FieldParameter('speed', range(0, 100), default=50)
    test = FieldParameter('test', ['1', '2', '3'], default='2')
    test2 = FieldParameter('test2', int, default=1)
    test233 = FieldParameter('test22', float, default=1.0)
    test3 = FieldParameter('test3', bool, default=False)
    test31 = FieldParameter('test31', bool, default=False)
    test32 = FieldParameter('test32', bool, default=True)
    test33 = FieldParameter('test33', bool, default=True)
    test4 = FieldParameter('test4', str, default='dupa')

    def init(self, arguments):
        pass

    def loop(self, data: A1Data):
        pass

import abc
from abc import ABC


class Voice(ABC):
    @abc.abstractmethod
    def say_introduction(self):
        pass

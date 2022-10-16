import abc
from abc import ABC
from dataclasses import dataclass
from enum import Enum
from typing import Any

from a1.models import A1Data
from a1.robot import Robot


class AlgorithmException(Exception):
    pass


class ExecutionState(object):
    class State(Enum):
        NONE = "none"
        RUNNING = "running"
        PAUSED = "paused"
        STOPPED = "stopped"

    def __init__(self, init_value=State.NONE):
        self._state = init_value

    @property
    def is_break_event(self) -> bool:
        return self._state in (self.State.PAUSED, self.State.STOPPED)

    @property
    def is_working(self) -> bool:
        return self._state is not self.State.NONE

    def set_state(self, state: State):
        self._state = state

    def equals(self, state: State) -> bool:
        return self._state == state

    @property
    def state(self) -> State:
        return self._state


@dataclass
class FieldParameter(object):
    name: str
    type: Any
    default: Any


class ArgumentsHolder(object):
    """
    Represents dynamic attributes holder

    @DynamicAttrs
    """
    pass


class Algorithm(ABC):
    """
    Represents abstract class that is supposed to be extended and the loop method must be implemented containing
    cleaning/movement logic fo the robot
    """

    def __init__(self, arguments: ArgumentsHolder):
        self._args = arguments

    @abc.abstractmethod
    def on_prepare(self, robot: Robot):
        pass

    @abc.abstractmethod
    def loop(self, robot: Robot, state: ExecutionState):
        pass

    @abc.abstractmethod
    def on_pause(self, robot: Robot):
        pass

    @abc.abstractmethod
    def on_resume(self, robot: Robot):
        pass

    @abc.abstractmethod
    def on_finish(self, robot: Robot):
        pass

    @classmethod
    def get_name(cls) -> str:
        name = cls.__dict__.get('NAME')
        if not name:
            raise AlgorithmException('You must define NAME class variable')
        return name

    @classmethod
    def get_description(cls) -> str:
        description = cls.__dict__.get('DESCRIPTION')
        if not description:
            raise AlgorithmException('You must define DESCRIPTION class variable')
        return description

    @classmethod
    def get_parameters(cls) -> dict:
        """
        Returns the parameters(FieldParameter) of the implementation class.
        For example:
        >>> class SomeAlgorithm(Algorithm):
        >>>     param_a = FieldParameter('paramA', str, default='hi')
        >>>     param_b = FieldParameter('paramB', int, default=123)
        >>>     def loop(self, data: A1Data):
        >>>         pass
        >>> SomeAlgorithm.get_parameters()
        >>> {"param_a": FieldParameter('paramA', str, 'hi'), "param_b": FieldParameter('paramB', int, 123)}

        :return: Dict where each key is reference variable and the value is instance of FieldParameter
        """

        return {var: val for var, val in cls.__dict__.items() if isinstance(val, FieldParameter)}

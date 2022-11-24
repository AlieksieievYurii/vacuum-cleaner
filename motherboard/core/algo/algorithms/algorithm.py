import abc
from abc import ABC
from dataclasses import dataclass
from enum import Enum
from typing import Any, Optional
from a1.robot import Robot
from utils.logger import Logger


class AlgorithmException(Exception):
    pass


# Pause Reason Codes
MANUAL_PAUSE_REASON: str = '_manual_pause_'
LID_IS_OPENED_PAUSE_REASON: str = '_lid_is_opened_'
DUST_BOX_OUT_PAUSE_REASON: str = '_dust_box_out_'
# ===============================

# Stop Reason Codes
MANUAL_STOP_REASON: str = '_manual_stop_'
ERROR_OCCURRED_STOP_REASON: str = '_error_occurred_stop_'


class ExecutionState(object):
    class State(Enum):
        IDLE = "idle"
        RUNNING = "running"
        PAUSED = "paused"
        STOPPED = "stopped"

    def __init__(self, init_value=State.IDLE):
        self._state = init_value
        self._pause_reason: Optional[str] = None

    @property
    def is_break_event(self) -> bool:
        return self._state in (self.State.PAUSED, self.State.STOPPED)

    @property
    def is_working(self) -> bool:
        return self._state is not self.State.IDLE

    def set_pause_state(self, reason: str):
        self._pause_reason = reason
        self.set_state(self.State.PAUSED)

    @property
    def pause_reason(self) -> Optional[str]:
        return self._pause_reason

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

    def __init__(self, arguments: ArgumentsHolder, logger: Logger):
        self._args = arguments
        self._logger = logger

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

    def print_info(self, message: str) -> None:
        self._logger.info(f'Algorithm<{self.get_name()}>: {message}')

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
        >>>     def loop(self, robot: Robot, state: ExecutionState):
        >>>         pass
        >>>     ...
        >>> SomeAlgorithm.get_parameters()
        >>> {"param_a": FieldParameter('paramA', str, 'hi'), "param_b": FieldParameter('paramB', int, 123)}

        :return: Dict where each key is reference variable and the value is instance of FieldParameter
        """

        return {var: val for var, val in cls.__dict__.items() if isinstance(val, FieldParameter)}

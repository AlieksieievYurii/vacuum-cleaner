import abc
from abc import ABC
from dataclasses import dataclass
from typing import Any

from a1.models import A1Data


class AlgorithmException(Exception):
    pass


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
        self._arguments = arguments

    @abc.abstractmethod
    def loop(self, data: A1Data):
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

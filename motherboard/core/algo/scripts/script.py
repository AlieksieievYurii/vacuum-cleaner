import abc
from abc import ABC
from dataclasses import dataclass
from typing import Any

from a1.models import A1Data


class ScriptException(Exception):
    pass


@dataclass
class Argument(object):
    name: str
    type: Any
    default: Any


class ArgumentsHolder(object):
    """
    Represents dynamic attributes holder for Arguments

    @DynamicAttrs
    """
    pass


class Script(ABC):

    @abc.abstractmethod
    def loop(self, data: A1Data):
        pass

    @classmethod
    def get_name(cls) -> str:
        name = cls.__dict__.get('NAME')
        if not name:
            raise ScriptException('You must define NAME class variable')
        return name

    @classmethod
    def get_description(cls) -> str:
        description = cls.__dict__.get('DESCRIPTION')
        if not description:
            raise ScriptException('You must define DESCRIPTION class variable')
        return description

    @classmethod
    def get_arguments(cls) -> dict:
        return {var: val for var, val in cls.__dict__.items() if isinstance(val, Argument)}

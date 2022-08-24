import abc
from dataclasses import dataclass
from enum import Enum
from typing import Type, Dict, Optional
from utils.request_handler.exceptions import RequiredFieldIsNotFound, InvalidRequestData


class AttributeHolder(object):
    """
    Represents dynamic attributes holder for Field class

    @DynamicAttrs
    """
    pass


@dataclass
class Field(object):
    """
    Is used to annotate some classes' fields so that is able to parse from dict
    Usage:
        >>> class SomeClass(object):
        >>>     some_var = Field(name='someVar', type=str, is_required=True)

    """
    name: str
    type: Type
    is_required: bool = False


class Status(str, Enum):
    OK: str = 'OK'
    ERROR: str = 'ERROR'
    BAD_REQUEST: str = 'BAD_REQUEST'


@dataclass
class Request(object):
    endpoint: str
    request_id: str
    parameters: Optional[Dict]

    @classmethod
    def parse(cls, request: Dict) -> 'Request':
        try:
            return cls(endpoint=request['endpoint'],
                       request_id=request['request_id'],
                       parameters=request.get('parameters'))
        except KeyError:
            raise InvalidRequestData(data=request)


@dataclass
class Response(object):
    endpoint: str
    request_id: str
    status: Status
    data: Optional[Dict] = None
    error_message: Optional[str] = None


class RequestHandler(abc.ABC):
    endpoint: str
    request_model: Optional[object]
    response_model: object

    @abc.abstractmethod
    def perform(self, request: Request, data: AttributeHolder):
        pass

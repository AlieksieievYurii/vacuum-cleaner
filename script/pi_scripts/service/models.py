import abc
import json
from dataclasses import dataclass
from enum import Enum
from json import JSONDecodeError
from typing import Type, Dict, Optional

from service.exceptions import NoRequiredVariable, InvalidRequest


class AttributeHolder(object):
    pass


@dataclass
class Field(object):
    """
    Is used to annotate some classes' fields so that is able to parse from dict
    Usage:
        >>> class SomeClass(object):
        >>>     some_var = Field(name='someVar', type=str, is_required=True)
    Then the following dict can be parsed and converted into object with defiled Fields.
    For example:
        >>> data = {'someVar': 'some value'}
        >>> obj = Field.parse_from_dict(SomeClass, data)
        >>> obj.some_var # some value
    """
    name: str
    type: Type
    is_required: bool = False

    @classmethod
    def parse_from_dict(cls, obj, data: Dict) -> AttributeHolder:
        fields = {var: val for var, val in obj.__dict__.items() if isinstance(val, Field)}
        obj = AttributeHolder()

        for field_name, field in fields.items():
            if field.is_required and field_name not in data:
                raise NoRequiredVariable(f'"{field_name}" is required!')
            setattr(obj, field.name, field.type(data.get(field_name)))
        return obj

    @classmethod
    def to_data(cls, instance) -> Dict:
        fields = {k: v for k, v in instance.__class__.__dict__.items() if isinstance(v, Field)}
        data = {}
        for field_name, field in fields.items():
            if field.is_required and field_name not in instance.__dict__:
                raise NoRequiredVariable(f'"{field_name}" is required!')
            data[field.name] = field.type(instance.__dict__.get(field_name))
        return data


class Status(str, Enum):
    OK: str = 'OK'
    ERROR: str = 'ERROR'
    BAD_REQUEST: str = 'BAD_REQUEST'


class PacketType(str, Enum):
    REQUEST: str = "REQUEST"
    RESPONSE: str = "RESPONSE"


@dataclass
class Packet(object):
    type: PacketType
    content: Dict

    @classmethod
    def parse(cls, data: str) -> 'Packet':
        try:
            request = json.loads(data)
        except JSONDecodeError:
            raise InvalidRequest(request_text=data)

        try:
            return cls(type=PacketType(request['type']), content=request['content'])
        except KeyError:
            raise InvalidRequest(request=request)


@dataclass
class Request(object):
    request_name: str
    request_id: str
    parameters: Optional[Dict]

    @classmethod
    def parse(cls, request: Dict) -> 'Request':
        try:
            return cls(request_name=request['request_name'],
                       request_id=request['request_id'],
                       parameters=request.get('parameters'))
        except KeyError:
            raise InvalidRequest(request=request)


@dataclass
class Response(object):
    request_name: str
    request_id: str
    status: Status
    error_message: Optional[str]
    response: Optional[Dict]

    @classmethod
    def parse(cls, response: Dict):
        return cls(
            request_name=response['request_name'],
            request_id=response['request_id'],
            status=Status(response['status']),
            error_message=response.get('error_message'),
            response=response.get('response')
        )


class RequestModel(abc.ABC):
    pass


class ResponseModel(abc.ABC):
    def __init__(self, **kwargs):
        for k, v in kwargs.items():
            setattr(self, k, v)


class RequestHandler(abc.ABC):
    request: str
    request_model: RequestModel
    response_model: ResponseModel

    @abc.abstractmethod
    def handle(self, request, data: RequestModel):
        pass

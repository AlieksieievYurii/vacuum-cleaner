import abc
import inspect
import json
from dataclasses import dataclass
from enum import Enum
from json import JSONDecodeError
from typing import Type, Dict, Optional

from service.exceptions import RequiredFieldIsNotFound, NoRequiredVariable, InvalidRequest


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
    request_id: int
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
    request_id: int
    status: Status
    error_message: Optional[str]
    response: Optional[Dict]


class RequestModel(object):
    @dataclass
    class Field(object):
        name: str
        type: Type
        is_required: bool = False

    @classmethod
    def parse(cls, data: Dict):
        fields = list(filter(lambda f: isinstance(f[1], RequestModel.Field), inspect.getmembers(cls)))
        request_model = RequestModel()
        for field_name, field in fields:
            value = cls._get_field_value(data, field)
            setattr(request_model, field_name, value)
        return request_model

    @staticmethod
    def _get_field_value(data: Dict, field: Field) -> any:
        if field.is_required and field.name not in data:
            raise RequiredFieldIsNotFound(field, data)
        return field.type(data[field.name]) if field.name in data else None


class ResponseModel(abc.ABC):
    @dataclass
    class Field(object):
        name: str
        type: Type
        is_required: bool = False

    def __init__(self, **kwargs):
        fields = list(filter(lambda f: isinstance(f[1], ResponseModel.Field), inspect.getmembers(self)))
        self.data = {}

        for field_name, field in fields:
            if field.is_required and field_name not in kwargs:
                raise NoRequiredVariable()
            self.data[field.name] = field.type(kwargs.get(field_name))


class RequestHandler(abc.ABC):
    request: str
    request_model: RequestModel
    response_model: ResponseModel

    @abc.abstractmethod
    def handle(self, request, data: RequestModel):
        pass

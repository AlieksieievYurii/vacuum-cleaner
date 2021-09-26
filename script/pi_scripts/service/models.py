import abc
import inspect
import json
from dataclasses import dataclass
from json import JSONDecodeError
from typing import Type, Dict, Optional

from service.exceptions import RequiredFieldIsNotFound, NoRequiredVariable, InvalidRequest


@dataclass
class Request(object):
    request_name: str
    request_id: int
    data: Optional[Dict]

    @classmethod
    def parse(cls, data: str):
        try:
            request = json.loads(data)
        except JSONDecodeError:
            raise InvalidRequest(request_text=data)

        try:
            return cls(request_name=request['request_name'],
                       request_id=request['request_id'],
                       data=request.get('data'))
        except KeyError:
            raise InvalidRequest(request=request)


@dataclass
class Response(object):
    request_name: str
    request_id: int
    data: Optional[Dict]


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

import subprocess
from dataclasses import dataclass

from utils.request_handler.models import RequestHandler, Request, Field


class RequestModel(object):
    name = Field('name', str, is_required=True)
    age = Field('age', int, is_required=True)
    is_developer = Field('is_developer', bool)


@dataclass
class ResponseModel(object):
    passport_id: str
    dir_out: str


class HelloWorldRequest(RequestHandler):
    endpoint = '/hello-world'
    request_model = RequestModel
    response_model = ResponseModel

    def perform(self, request: Request, data: RequestModel) -> ResponseModel:
        passport_id = f'{data.name}_{data.age}{"#" if data.is_developer else "@"}'
        out = subprocess.run(["dir"], capture_output=True, text=True, check=True, shell=True).stdout

        return ResponseModel(passport_id, out)

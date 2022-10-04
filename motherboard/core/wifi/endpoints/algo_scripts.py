from dataclasses import dataclass
from typing import List, Any

from algo.algo_manager import AlgorithmManager
from utils.config import Configuration
from utils.request_handler.models import RequestHandler, Request, AttributeHolder, Field, ListType


@dataclass
class AlgorithmArgument(object):
    name: str
    value_type: str
    default_value: Any
    current_value: Any


@dataclass
class AlgorithmScript(object):
    name: str
    description: str
    arguments: List[AlgorithmArgument]


@dataclass
class ResponseModel(object):
    current_script: str
    scripts: List[AlgorithmScript]


class Parameter(object):
    name = Field('name', str, is_required=True)
    value = Field('value', Any, is_required=True)


class RequestModel(object):
    script_name = Field('script_name', str, is_required=True)
    parameters = Field('parameters', ListType(Parameter), is_required=True)


class GetAlgorithmScriptsRequest(RequestHandler):
    endpoint = '/get-algorithm-scripts'
    request_model = None
    response_model = ResponseModel

    def __init__(self, algorithm_manager: AlgorithmManager):
        self._algorithm_manager = algorithm_manager

    def perform(self, request: Request, data: AttributeHolder) -> ResponseModel:
        scripts = []
        for script in self._algorithm_manager.get_scripts():
            arguments_list = [AlgorithmArgument(
                name=argument['name'],
                value_type=argument['value_type'],
                default_value=argument['default'],
                current_value=argument['value']
            ) for argument in script['arguments']]

            scripts.append(AlgorithmScript(
                name=script['name'],
                description=script['description'],
                arguments=arguments_list
            ))

        return ResponseModel(current_script=self._algorithm_manager.get_current_script_name(), scripts=scripts)


class SetAlgorithmScriptRequest(RequestHandler):
    endpoint = '/set-algorithm-scripts'
    request_model = RequestModel
    response_model = None

    def __init__(self, algorithm_manager: AlgorithmManager, config: Configuration):
        self._algorithm_manager = algorithm_manager
        self._config = config

    def perform(self, request: Request, data: AttributeHolder):
        self._algorithm_manager.set_script_parameters(data.script_name, {p.name: p.value for p in data.parameters})
        self._config.set_target_cleaning_algorithm(data.script_name)
        self._algorithm_manager.set_script(data.script_name)

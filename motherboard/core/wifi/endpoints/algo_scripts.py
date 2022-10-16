from dataclasses import dataclass
from typing import List, Any, Optional

from algo.algo_manager import AlgorithmManager, AlgorithmExecutionInfo
from algo.algorithms.algorithm import ExecutionState
from utils.config import Configuration
from utils.request_handler.models import RequestHandler, Request, AttributeHolder, Field, ListType


@dataclass
class AlgorithmParameter(object):
    name: str
    value_type: str
    default_value: Any
    current_value: Any


@dataclass
class Algorithm(object):
    name: str
    description: str
    parameters: List[AlgorithmParameter]


@dataclass
class ResponseModel(object):
    current_algorithm: str
    algorithms: List[Algorithm]


class Parameter(object):
    name = Field('name', str, is_required=True)
    value = Field('value', Any, is_required=True)


class RequestModel(object):
    algorithm_name = Field('algorithm_name', str, is_required=True)
    arguments = Field('arguments', ListType(Parameter), is_required=True)


class GetAlgorithmsRequest(RequestHandler):
    endpoint = '/get-algorithms'
    request_model = None
    response_model = ResponseModel

    def __init__(self, algorithm_manager: AlgorithmManager):
        self._algorithm_manager = algorithm_manager

    def perform(self, request: Request, data: AttributeHolder) -> ResponseModel:
        algorithms = []
        for algorithm in self._algorithm_manager.get_algorithms():
            parameters = [AlgorithmParameter(
                name=parameter['name'],
                value_type=parameter['value_type'],
                default_value=parameter['default'],
                current_value=parameter['value']
            ) for parameter in algorithm['arguments']]

            algorithms.append(Algorithm(
                name=algorithm['name'],
                description=algorithm['description'],
                parameters=parameters
            ))

        return ResponseModel(self._algorithm_manager.get_current_algorithm_name(), algorithms)


class SetAlgorithmScriptRequest(RequestHandler):
    endpoint = '/set-algorithm'
    request_model = RequestModel
    response_model = None

    def __init__(self, algorithm_manager: AlgorithmManager, config: Configuration):
        self._algorithm_manager = algorithm_manager
        self._config = config

    def perform(self, request: Request, data: AttributeHolder):
        self._algorithm_manager.save_algorithm_arguments(data.algorithm_name, {a.name: a.value for a in data.arguments})
        self._config.set_target_cleaning_algorithm(data.algorithm_name)
        self._algorithm_manager.set_algorithm(data.algorithm_name)


@dataclass
class CleaningStatus(object):
    status: str
    cleaning_info: Optional[AlgorithmExecutionInfo]


class ManageCleaningExecutionRequestModel(object):
    command = Field('command', str, is_required=True)


class ManageCleaningExecutionRequest(RequestHandler):
    endpoint = '/manage-cleaning'
    request_model = ManageCleaningExecutionRequestModel
    response_model = None

    def __init__(self, algorithm_manager: AlgorithmManager):
        self._algorithm_manager = algorithm_manager

    def perform(self, request: Request, data: AttributeHolder):
        command: Optional = {
            'start': self._algorithm_manager.start,
            'pause': self._algorithm_manager.pause,
            'resume': self._algorithm_manager.resume,
            'stop': self._algorithm_manager.stop
        }[data.command]

        if not command:
            raise Exception(f'Wrong command: {data.command}')
        else:
            command()


class GetCleaningStatusRequest(RequestHandler):
    endpoint = '/get-cleaning-status'
    request_model = None
    response_model = CleaningStatus

    def __init__(self, algorithm_manager: AlgorithmManager):
        self._algorithm_manager = algorithm_manager

    def perform(self, request: Request, data: AttributeHolder) -> CleaningStatus:
        if self._algorithm_manager.current_state.equals(ExecutionState.State.RUNNING):
            status = 'running'
        elif self._algorithm_manager.current_state.equals(ExecutionState.State.PAUSED):
            status = 'paused'
        else:
            status = 'none'

        return CleaningStatus(status=status, cleaning_info=self._algorithm_manager.algorithm_execution_info)

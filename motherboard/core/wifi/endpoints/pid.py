from dataclasses import dataclass

from utils.config import Configuration
from utils.request_handler.models import RequestHandler, Request, AttributeHolder, Field


@dataclass
class CurrentPidSettingsResponseModel(object):
    proportional: float
    integral: float
    derivative: float


class PidSettingsRequestModel(object):
    proportional = Field('proportional', float, is_required=True)
    integral = Field('integral', float, is_required=True)
    derivative = Field('derivative', float, is_required=True)


class GetCurrentPidSettings(RequestHandler):
    endpoint = '/get-current-pid'
    request_model = None
    response_model = CurrentPidSettingsResponseModel

    def __init__(self, config: Configuration):
        self._config = config

    def perform(self, request: Request, data: AttributeHolder):
        p, i, d = self._config.get_pid_settings()

        return CurrentPidSettingsResponseModel(p, i, d)


class SetPidSettings(RequestHandler):
    endpoint = '/set-current-pid'
    request_model = PidSettingsRequestModel
    response_model = None

    def __init__(self, config: Configuration):
        self._config = config

    def perform(self, request: Request, data: AttributeHolder):
        self._config.set_pid_settings(
            proportional=data.proportional,
            integral=data.integral,
            derivative=data.derivative
        )

from dataclasses import dataclass

from utils.config import Configuration
from utils.request_handler.models import RequestHandler, Request, AttributeHolder


@dataclass
class CurrentPidSettingsResponseModel(object):
    proportional: float
    integral: float
    derivative: float


class GetCurrentPidSettings(RequestHandler):
    endpoint = '/get-current-pid'
    request_model = None
    response_model = CurrentPidSettingsResponseModel

    def __init__(self, config: Configuration):
        self._config = config

    def perform(self, request: Request, data: AttributeHolder):
        p, i, d = self._config.get_pid_settings()

        return CurrentPidSettingsResponseModel(p, i, d)

from utils.os import OperationSystem
from utils.request_handler.models import RequestHandler, Request, AttributeHolder, Field


class SetWifiCredentialsRequestModel(object):
    ssid = Field('ssid', str, is_required=True)
    password = Field('password', str, is_required=True)


class SetWifiCredentialsRequestHandler(RequestHandler):
    endpoint = '/set-wifi-credentials'
    request_model = SetWifiCredentialsRequestModel
    response_model = None

    def __init__(self, os: OperationSystem):
        self._os = os

    def perform(self, request: Request, data: AttributeHolder):
        self._os.set_wifi_credentials(data.ssid, data.password)

from dataclasses import dataclass

from utils.os import OperationSystem
from utils.request_handler.models import RequestHandler, Request, AttributeHolder, Field


class SetWifiCredentialsRequestModel(object):
    ssid = Field('ssid', str, is_required=True)
    password = Field('password', str, is_required=True)


@dataclass
class WifiCredentialsResponseModel(object):
    ssid: str
    password: str
    key_mgmt: str


class SetWifiCredentialsRequestHandler(RequestHandler):
    endpoint = '/set-wifi-credentials'
    request_model = SetWifiCredentialsRequestModel
    response_model = None

    def __init__(self, os: OperationSystem):
        self._os = os

    def perform(self, request: Request, data: AttributeHolder):
        self._os.set_wifi_credentials(data.ssid, data.password)


class GetCurrentWifiCredentialsRequestHandler(RequestHandler):
    endpoint = '/get-current-wifi-credentials'
    request_model = None
    response_model = WifiCredentialsResponseModel

    def __init__(self, os: OperationSystem):
        self._os = os

    def perform(self, request: Request, data: AttributeHolder):
        wpa_conf = self._os.get_wpa_config()
        return WifiCredentialsResponseModel(
            ssid=wpa_conf['ssid'],
            password=wpa_conf['psk'],
            key_mgmt=wpa_conf['key_mgmt']
        )

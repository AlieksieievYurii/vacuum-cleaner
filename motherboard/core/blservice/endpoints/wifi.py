from dataclasses import dataclass
from time import sleep
from typing import List

from utils.os import OperationSystem, OperationSystemException
from utils.request_handler.models import RequestHandler, Request, AttributeHolder, Field


class SetWifiCredentialsRequestModel(object):
    ssid = Field('ssid', str, is_required=True)
    password = Field('password', str, is_required=True)


@dataclass
class WifiCredentialsResponseModel(object):
    ssid: str
    password: str
    key_mgmt: str


@dataclass
class NetworkInfo(object):
    ip_address: str


@dataclass
class AccessPoint(object):
    ssid: str
    address: str


@dataclass
class NetWorkScan(object):
    available_access_points: List[AccessPoint]


class SetWifiCredentialsRequestHandler(RequestHandler):
    endpoint = '/setup-wifi'
    request_model = SetWifiCredentialsRequestModel
    response_model = NetworkInfo

    def __init__(self, os: OperationSystem):
        self._os = os

    def perform(self, request: Request, data: AttributeHolder):
        self._os.set_wifi_credentials(data.ssid, data.password)
        self._os.apply_wifi_settings()
        sleep(10)
        ip = self._os.get_ip_address()
        if ip:
            return NetworkInfo(ip)
        else:
            raise OperationSystemException('Can not get IP. The robot is not connected to a network')


class GetAvailableAccessPointsRequestHandler(RequestHandler):
    endpoint = '/get-available-access-points'
    request_model = None
    response_model = NetWorkScan

    def __init__(self, os: OperationSystem):
        self._os = os

    def perform(self, request: Request, data: AttributeHolder):
        aps = self._os.get_available_access_points()
        return NetWorkScan(
            available_access_points=list(map(lambda ap: AccessPoint(ssid=ap['ssid'], address=ap['address']), aps))
        )


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

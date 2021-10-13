import re
import subprocess
from pathlib import Path
from typing import List

from service.models import RequestHandler, ResponseModel, Field, Request, RequestModel


class GetWifiSettingsRequestHandler(RequestHandler):
    class WifiSettingsModel(ResponseModel):
        ssid = Field(name='ssid', type=str, is_required=True)
        password = Field(name='password', type=str, is_required=True)

    request = "get_wifi_settings"
    request_model = None
    response_model = WifiSettingsModel

    def handle(self, request: Request, data: None):
        wifi_conf = Path('/etc/wpa_supplicant/wpa_supplicant.conf')
        content = wifi_conf.read_text()
        ssid = re.search(r'ssid="(.+)"', content).group(1)
        password = re.search(r'psk="(.+)"', content).group(1)
        return GetWifiSettingsRequestHandler.WifiSettingsModel(ssid=ssid, password=password)


class SetWifiSettingsRequestHandler(RequestHandler):
    class WifiSettingsRequestModel(RequestModel):
        ssid = Field(name='ssid', type=str, is_required=True)
        password = Field(name='password', type=str, is_required=True)

    class WifiSettingsResponseModel(ResponseModel):
        is_connected = Field(name='is_connected', type=bool, is_required=True)
        ip = Field(name='ip', type=str)
        error_message = Field('error_message', type=str)

    request = "set_wifi_settings"
    request_model = WifiSettingsRequestModel
    response_model = WifiSettingsResponseModel

    _WIFI_SETTING_CONTENT = """ctrl_interface=DIR=/var/run/wpa_supplicant GROUP=netdev
update_config=1
country=US

network={{
    ssid="{ssid}"
    scan_ssid=1
    psk="{password}"
    key_mgmt=WPA-PSK
}}
"""

    def handle(self, request: Request, data: WifiSettingsRequestModel):
        wifi_conf = Path('/etc/wpa_supplicant/wpa_supplicant.conf')
        wifi_conf.write_text(data=self._WIFI_SETTING_CONTENT.format(ssid=data.ssid, password=data.password))

        reconfigure_out = self._run_command(['wpa_cli', '-i', 'wlan0', 'reconfigure'])
        if reconfigure_out.strip() != 'OK':
            return SetWifiSettingsRequestHandler.WifiSettingsResponseModel(is_connected=False,
                                                                           error_message=reconfigure_out)

        check = self._run_command(['ifconfig', 'wlan0'])
        address = re.search(r'inet\s(\d+.\d+.\d+.\d+)', check).group(1)

        return SetWifiSettingsRequestHandler.WifiSettingsResponseModel(is_connected=True, ip=address)

    @staticmethod
    def _run_command(command: List[str]) -> str:
        output = subprocess.run(command, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
        return output.stdout.decode("utf-8")

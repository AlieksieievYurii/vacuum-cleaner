import re
import threading
from pathlib import Path

from service.models import RequestHandler, ResponseModel, Field, Request


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

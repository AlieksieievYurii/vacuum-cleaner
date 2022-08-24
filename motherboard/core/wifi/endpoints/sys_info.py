import platform
import re
import socket
import uuid
from dataclasses import dataclass

from utils.request_handler.models import RequestHandler, Request, AttributeHolder


@dataclass
class ResponseModel(object):
    platform: str
    platform_release: str
    platform_version: str
    architecture: str
    hostname: str
    ip_address: str
    mac_address: str
    processor: str


class GetRobotSysInfo(RequestHandler):
    endpoint = '/get-sys-info'
    request_model = None
    response_model = ResponseModel

    def perform(self, request: Request, _: AttributeHolder) -> ResponseModel:
        return ResponseModel(
            platform=platform.system(),
            platform_release=platform.release(),
            platform_version=platform.version(),
            architecture=platform.machine(),
            hostname=socket.gethostname(),
            ip_address=socket.gethostbyname(socket.gethostname()),
            mac_address=':'.join(re.findall('..', '%012x' % uuid.getnode())),
            processor=platform.processor()
        )

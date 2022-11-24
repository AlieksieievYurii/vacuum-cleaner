from utils.request_handler.models import RequestHandler, Field, Request, AttributeHolder


class PowerEndpointException(Exception):
    pass


class PowerRequestModel(object):
    command = Field('command', str, is_required=True)


class PowerRequestHandler(RequestHandler):
    endpoint = '/power'
    request_model = PowerRequestModel
    response_model = None

    def __init__(self, on_shut_down, on_reboot):
        self._on_shut_down = on_shut_down
        self._on_reboot = on_reboot

    def perform(self, request: Request, data: AttributeHolder) -> None:
        if data.command == 'shutdown':
            self._on_shut_down()
        elif data.command == 'reboot':
            self._on_reboot()
        else:
            raise PowerEndpointException(f'Wrong command: {data.command}')

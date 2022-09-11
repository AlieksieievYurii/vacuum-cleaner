from threading import Thread

from utils.logger import WifiModuleLogger
from utils.request_handler.handler import RequestHandlerService
from utils.request_handler.models import RequestHandler
from wifi.comunicator import WifiCommunicator


class WifiEndpointsHandler(Thread):
    """
    Class that handles Wifi connection and executes registered endpoints. It works in separated thread where
    firstly it accepts connection, then starts executing incoming requests in the same thread
    """

    def __init__(self, wifi_communicator: WifiCommunicator, logger: WifiModuleLogger):
        self._wifi_communicator = wifi_communicator
        self._logger = logger
        self._request_handler = RequestHandlerService(self._wifi_communicator, logger)
        super().__init__(name="WifiConnection", daemon=True)

    def register_endpoint(self, endpoint_handler: RequestHandler) -> None:
        self._request_handler.register(endpoint_handler)

    def run(self) -> None:
        while True:
            self._logger.info('Waiting for connection...')
            client_address = self._wifi_communicator.accept_connection()
            self._logger.info(f'Accepted connection by {client_address}')
            self._request_handler.start_handling()

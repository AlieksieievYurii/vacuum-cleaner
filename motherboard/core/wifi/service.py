from threading import Thread

from utils.event import Eventer
from utils.logger import Logger
from utils.request_handler.handler import RequestHandlerService
from utils.request_handler.models import RequestHandler
from wifi.comunicator import WifiCommunicator


class WifiService(Thread):
    """
    Class that handles Wi-fi connection and executes registered endpoints. It works in separated thread where
    firstly it accepts connection, then starts executing incoming requests in the same thread
    """

    WIFI_SERVICE_FAILED_EVENT: int = 0x1

    def __init__(self, wifi_communicator: WifiCommunicator, logger: Logger):
        self._wifi_communicator = wifi_communicator
        self._logger = logger
        self._request_handler = RequestHandlerService(self._wifi_communicator, logger)
        self.events = Eventer()
        super().__init__(name="WifiConnection", daemon=True)

    def register_endpoint(self, endpoint_handler: RequestHandler) -> None:
        self._request_handler.register(endpoint_handler)

    def run(self) -> None:
        try:
            while True:
                self._logger.info('Waiting for connection...')
                client_address = self._wifi_communicator.accept_connection()
                self._logger.info(f'Accepted connection by {client_address}')
                self._request_handler.start_handling()
        except Exception as error:
            self._logger.error(f'Error occurred in Wi-Fi Service: {error}')
            self.events.emit(self.WIFI_SERVICE_FAILED_EVENT)

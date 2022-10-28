from threading import Thread

from bluetooth.communicator import BluetoothCommunicator
from utils.logger import Logger
from utils.request_handler.handler import RequestHandlerService
from utils.request_handler.models import RequestHandler


class BluetoothEndpointsHandler(Thread):
    def __init__(self, bluetooth_communicator: BluetoothCommunicator, logger: Logger):
        self._bluetooth_communicator = bluetooth_communicator
        self._logger = logger
        self._request_handler = RequestHandlerService(self._bluetooth_communicator, logger)
        super().__init__(name="WifiConnection", daemon=True)

    def register_endpoint(self, endpoint_handler: RequestHandler) -> None:
        self._request_handler.register(endpoint_handler)

    def run(self) -> None:
        while True:
            self._logger.info('Waiting for connection...')
            self._bluetooth_communicator.accept_connection()
            self._logger.info('Accepted connection')
            self._request_handler.start_handling()

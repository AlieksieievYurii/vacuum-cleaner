from threading import Thread

from blservice.bluetoothctl import Bluetoothctl
from blservice.communicator import BluetoothCommunicator
from utils.logger import Logger
from utils.request_handler.handler import RequestHandlerService
from utils.request_handler.models import RequestHandler


class BluetoothService(Thread):
    def __init__(self, bluetooth_communicator: BluetoothCommunicator, logger: Logger):
        self._bluetooth_communicator = bluetooth_communicator
        self._logger = logger
        self._request_handler = RequestHandlerService(self._bluetooth_communicator, logger)
        self._pairing_thread = None
        self._is_paring_process_enabled = False
        super().__init__(name="WifiConnection", daemon=True)

    def register_endpoint(self, endpoint_handler: RequestHandler) -> None:
        self._request_handler.register(endpoint_handler)

    @property
    def is_paring_process_enabled(self) -> bool:
        return self._is_paring_process_enabled

    def __pairing(self):
        with Bluetoothctl(self._logger).open() as ctl:
            ctl.agent_off()
            ctl.power_on()
            ctl.discoverable_on()
            ctl.pairable_on()
            ctl.agent_no_input_no_output()
            ctl.default_agent()
            ctl.accept_incoming_pairing_request()
            self._logger.info('Pairing process has finished')
            self._is_paring_process_enabled = False

    def enable_pairing(self):
        self._is_paring_process_enabled = True
        if self._pairing_thread:
            self._pairing_thread.join()
            self._pairing_thread = None
        else:
            self._pairing_thread = Thread(target=self.__pairing, name="pairingThread")
            self._pairing_thread.start()

    def run(self) -> None:
        with Bluetoothctl(self._logger).open() as ctl:
            ctl.agent_off()
            ctl.power_on()
            ctl.discoverable_on()
            ctl.pairable_on()
            ctl.agent_no_input_no_output()
            ctl.default_agent()
            self._bluetooth_communicator.advertise()

        while True:
            self._logger.info('Waiting for socket connection...')
            self._bluetooth_communicator.accept_connection()
            self._logger.info('Socket connection is accepted. Run Request Handler')
            self._request_handler.start_handling()

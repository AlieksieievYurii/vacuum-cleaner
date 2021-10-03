import json
import logging
from threading import Thread
from typing import Dict

from service.communitator.communicator import Communicator
import bluetooth


class BluetoothCommunicator(Communicator):
    UUID = "94f39d29-7d6d-437d-973b-fba39e49d4ee"

    def __init__(self):
        self.server_sock = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
        self.server_sock.bind(("", bluetooth.PORT_ANY))
        self.server_sock.listen(1)
        self.client_sock = None

    def _connect(self):
        logging.info('Start connecting...')
        bluetooth.advertise_service(self.server_sock, "SampleServer", service_id=self.UUID,
                                    service_classes=[self.UUID, bluetooth.SERIAL_PORT_CLASS],
                                    profiles=[bluetooth.SERIAL_PORT_PROFILE])
        logging.info("Waiting for connection on RFCOMM channel", self.server_sock.getsockname()[1])
        client_sock, client_info = self.server_sock.accept()
        logging.info("Accepted connection from", client_info)
        self.client_sock = client_sock

    def send(self, data: Dict) -> None:
        logging.debug(f'Send data: {data}')
        self.client_sock.send(json.dumps(data))

    def listen_output(self, call_back) -> None:
        self._connect()

        def inner_callback():
            while True:
                data = self.client_sock.recv(1024)
                if not data:
                    break
                call_back(data.decode().rstrip())

        Thread(target=inner_callback, daemon=True).start()
        logging.debug('Started listening for the output.')

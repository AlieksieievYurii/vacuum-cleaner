from typing import Optional
import bluetooth
from utils.communicator import Communicator, CommunicatorConnectionClosed


class BluetoothCommunicator(Communicator):
    UUID = "94f39d29-7d6d-437d-973b-fba39e49d4ee"

    def __init__(self):
        self.server_sock = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
        self._client_sock = None
        self._reader = None

    def advertise(self):
        self.server_sock.bind(("", bluetooth.PORT_ANY))
        self.server_sock.listen(1)
        bluetooth.advertise_service(self.server_sock, self.UUID, service_id=self.UUID,
                                    service_classes=[self.UUID, bluetooth.SERIAL_PORT_CLASS],
                                    profiles=[bluetooth.SERIAL_PORT_PROFILE])

    def accept_connection(self):
        client_sock, client_info = self.server_sock.accept()
        self._client_sock = client_sock
        self._reader = client_sock.makefile()

    def send(self, data: str) -> None:
        self._client_sock.sendall(f'{data}\n'.encode())

    def read(self) -> str:
        try:
            line: Optional[str] = self._reader.readline()
        except ConnectionResetError:
            raise CommunicatorConnectionClosed(f"Connection Reset!")

        if not line:
            raise CommunicatorConnectionClosed(f"Connection is closed")

        return line

import socket
from typing import Optional

from utils.communicator import Communicator, CommunicatorConnectionClosed


class WifiCommunicator(Communicator):
    def __init__(self, port: int):
        self._socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self._socket.bind(('', port))
        self._connection = None
        self._reader = None

    def accept_connection(self) -> str:
        self._socket.listen()
        self._connection, client_address = self._socket.accept()
        self._reader = self._connection.makefile()

        return client_address

    def send(self, data: str) -> None:
        self._connection.sendall(f'{data}\n'.encode())

    def read(self) -> str:
        line: Optional[str] = self._reader.readline()

        if not line:
            raise CommunicatorConnectionClosed(f"Connection is closed")

        return line

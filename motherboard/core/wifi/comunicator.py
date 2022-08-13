import socket
from typing import Optional

from utils.communicator import Communicator, CommunicatorConnectionClosed


class WifiCommunicator(Communicator):
    def __init__(self):
        self._socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self._socket.bind(('', 1488))
        self._connection = None
        self._reader = None

    def accept_connection(self):
        print(socket.gethostbyname(socket.gethostname()))
        print('Waiting for connection...')
        self._socket.listen()
        self._connection, addr = self._socket.accept()
        self._reader = self._connection.makefile()
        print(f'Connected with: {addr}')

    def send(self, data: str) -> None:
        self._connection.sendall(f'{data}\n'.encode())

    def read(self) -> str:
        line: Optional[str] = self._reader.readline()

        if not line:
            raise CommunicatorConnectionClosed(f"Connection is closed")

        return line

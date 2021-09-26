from threading import Thread
from time import sleep
from typing import Dict

from pi_scripts.service.communitator.communicator import Communicator


class DummyCommunicator(Communicator):
    def send(self, data: Dict) -> None:
        print(f'[DUMMY COMMUNICATOR] {data}')

    def listen_output(self, call_back) -> None:
        def inner_callback():
            data = {}
            while True:
                call_back(data)
                sleep(1)

        Thread(target=inner_callback).start()


if __name__ == '__main__':
    communicator = DummyCommunicator()
    communicator.listen_output(call_back=lambda data: print(data))

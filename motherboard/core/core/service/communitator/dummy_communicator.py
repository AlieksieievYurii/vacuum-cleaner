from threading import Thread
from time import sleep
from typing import Dict

from service.communitator.communicator import Communicator


class DummyCommunicator(Communicator):
    def send(self, data: Dict) -> None:
        print(f'[DUMMY COMMUNICATOR] {data}')

    def listen_output(self, call_back) -> None:
        def inner_callback():
            data = {'request_name': 'get_all_info', 'request_id': 123, 'data': {'var_one': 'test'}}
            while True:
                call_back(data)
                sleep(10)

        Thread(target=inner_callback, daemon=True).start()


if __name__ == '__main__':
    communicator = DummyCommunicator()
    communicator.listen_output(call_back=lambda data: print(data))

import abc
from typing import Dict


class Communicator(abc.ABC):
    @abc.abstractmethod
    def send(self, data: Dict) -> None:
        pass

    @abc.abstractmethod
    def listen_output(self, call_back) -> None:
        pass

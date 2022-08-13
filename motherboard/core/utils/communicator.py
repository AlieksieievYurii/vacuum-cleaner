import abc


class CommunicatorConnectionClosed(Exception):
    pass


class Communicator(abc.ABC):
    @abc.abstractmethod
    def send(self, data: str) -> None:
        pass

    @abc.abstractmethod
    def read(self) -> str:
        pass

from abc import ABC, abstractmethod
from sys import platform
import re
import subprocess


class OperationSystem(ABC):
    @abstractmethod
    def set_date_time(self, data_time: str) -> None:
        """
        Abstract function that is supposed to run OS command to set up data time using the following format Y-d-m H:i:s

        :param data_time: string representation of data time. Example: 2000-06-02 08:47:46
        :return: None
        """
        pass

    @abstractmethod
    def is_ntp_synchronized(self) -> bool:
        """
        Abstract function that is supposed to check if NTP is synchronized

        :return: True if yes, otherwise False
        """

        pass


class WindowsOperationSystem(OperationSystem):

    def set_date_time(self, data_time: str) -> None:
        print(f'TEST. Set datetime: {data_time}')

    def is_ntp_synchronized(self) -> bool:
        return False


class LinuxOperationSystem(OperationSystem):
    def set_date_time(self, data_time: str) -> None:
        subprocess.run(['timedatectl', 'set-time', data_time], check=True)

    def is_ntp_synchronized(self) -> bool:
        out: str = subprocess.run(['timedatectl', 'status'], capture_output=True, text=True, check=True).stdout
        match = re.findall(r'\s+(System clock synchronized: yes)\s+', out)

        return bool(match)


def get_operation_system() -> OperationSystem:
    if platform == "linux" or platform == "linux2":
        return LinuxOperationSystem()
    elif platform == "win32":
        return WindowsOperationSystem()
    else:
        raise Exception('Unhandled OS. Trying to get OperationSystem handler for unknown OS')

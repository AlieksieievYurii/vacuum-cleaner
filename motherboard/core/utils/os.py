from abc import ABC, abstractmethod
from pathlib import Path
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

    @abstractmethod
    def shutdown(self) -> None:
        """
        Abstract function that is supposed to perform shutdown the OS

        :return: None
        """
        pass

    @abstractmethod
    def reboot(self) -> None:
        """
        Abstract function that is supposed to perform reboot the OS

        :return: None
        """
        pass

    @abstractmethod
    def play_sound(self, file: Path) -> None:
        """
        Abstract function that is supposed to play given media file

        :return: None
        """
        pass

    @abstractmethod
    def set_wifi_credentials(self, ssid: str, password: str) -> None:
        """
        Abstract function that is supposed to save/set Wifi credentials without applying

        :param ssid: SSID of WI-FI access point
        :param password: password
        :return: None
        """
        pass

    @abstractmethod
    def get_wpa_config(self) -> dict:
        """
        Abstract function that is supposed to return a dict containing WPA configuration

        :return: dict containing WPA config
        """


class WindowsOperationSystem(OperationSystem):
    """
    The class is used just as mock up. It is called so because the development of the Core is done on Windows OS
    """

    def reboot(self) -> None:
        print('Perform reboot')

    def play_sound(self, file: Path) -> None:
        pass

    def set_wifi_credentials(self, ssid: str, password: str) -> None:
        print(f'Set/Save Wi-fi credentials: SSID: {ssid}; Password: {password}')

    def shutdown(self) -> None:
        print('TEST. Perform shutdown')

    def set_date_time(self, data_time: str) -> None:
        print(f'TEST. Set datetime: {data_time}')

    def is_ntp_synchronized(self) -> bool:
        return False

    def get_wpa_config(self) -> dict:
        return {
            'ssid': 'toya334234',
            'psk': '123ffwsdcv34',
            'key_mgmt': 'WPA-PSK'
        }


class LinuxOperationSystem(OperationSystem):

    def __init__(self):
        self._wpa_supplicant_conf_file = Path('/etc/wpa_supplicant/wpa_supplicant.conf')

    def set_wifi_credentials(self, ssid: str, password: str) -> None:
        print(f'Set/Save Wi-fi credentials: SSID: {ssid}; Password: {password}')

    def reboot(self) -> None:
        subprocess.run(['reboot'])

    def play_sound(self, file: Path) -> None:
        subprocess.run(['omxplayer', file.as_posix()], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)

    def shutdown(self) -> None:
        subprocess.run(['shutdown', '-r', 'now'], check=True)

    def set_date_time(self, data_time: str) -> None:
        subprocess.run(['timedatectl', 'set-time', data_time], check=True)

    def is_ntp_synchronized(self) -> bool:
        out: str = subprocess.run(['timedatectl', 'status'], capture_output=True, text=True, check=True).stdout

        match = re.findall(r'\s+(NTP service: active)\s+', out)

        return bool(match)

    def get_wpa_config(self) -> dict:
        wpa_conf_content = self._wpa_supplicant_conf_file.read_text()
        ssid = re.search(r'ssid\s*=\s*\"(\S+)\"', wpa_conf_content)
        psk = re.search(r'psk\s*=\s*\"(\S+)\"', wpa_conf_content)
        key_mgmt = re.search(r'key_mgmt\s*=\s*(\S+)', wpa_conf_content)

        return {
            'ssid': ssid.group(1),
            'psk': psk.group(1),
            'key_mgmt': key_mgmt.group(1)
        }


def get_operation_system() -> OperationSystem:
    if platform == "linux" or platform == "linux2":
        return LinuxOperationSystem()
    elif platform == "win32":
        return WindowsOperationSystem()
    else:
        raise Exception('Unhandled OS. Trying to get OperationSystem handler for unknown OS')

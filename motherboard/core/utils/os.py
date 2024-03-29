from abc import ABC, abstractmethod
from pathlib import Path
from sys import platform
import re
import subprocess
from typing import Optional, List


class OperationSystemException(Exception):
    pass


WPA_SETTINGS_CONTENT = """ctrl_interface=DIR=/var/run/wpa_supplicant GROUP=netdev
update_config=1
country=US

network={{
    ssid="{ssid}"
    scan_ssid=1
    psk="{psk}"
    key_mgmt=WPA-PSK
}}
"""


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
        Abstract function that is supposed to save/set Wi-fi credentials without applying

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

    @abstractmethod
    def get_ip_address(self) -> Optional[str]:
        """
        Abstract function that is supposed to return robot's IP address in the local network if the robot is connected
        to the network. Otherwise returns None

        :return: ip address
        """
        pass

    @abstractmethod
    def apply_wifi_settings(self) -> None:
        """
        Abstract function that is supposed to apply/refresh network settings

        :return: None
        """
        pass

    @abstractmethod
    def get_available_access_points(self) -> List[dict]:
        pass


class WindowsOperationSystem(OperationSystem):
    """
    The class is used just as mock up. It is called so because the development of the Core is done on Windows OS
    """

    def reboot(self) -> None:
        print('Perform reboot')

    def play_sound(self, file: Path) -> None:
        print(f'Play sound: {file}')

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

    def get_ip_address(self) -> Optional[str]:
        return "192.168.18.12"

    def apply_wifi_settings(self) -> None:
        print('apply_wifi_settings')

    def get_available_access_points(self) -> List[dict]:
        return [
            {'ssid': 'toya1', 'address': '3b:132:54:c5:f5'},
            {'ssid': 'toya2', 'address': '4b:132:54:c5:f5'},
        ]


class LinuxOperationSystem(OperationSystem):

    def __init__(self):
        self._wpa_supplicant_conf_file = Path('/etc/wpa_supplicant/wpa_supplicant.conf')
        self._playing_sound_process: Optional[subprocess.Popen] = None

    def set_wifi_credentials(self, ssid: str, password: str) -> None:
        content = WPA_SETTINGS_CONTENT.format(ssid=ssid, psk=password)
        self._wpa_supplicant_conf_file.write_text(content)

    def reboot(self) -> None:
        subprocess.run(['reboot'])

    def play_sound(self, file: Path) -> None:
        if self._playing_sound_process and self._playing_sound_process.returncode is None:
            self._playing_sound_process.terminate()

        self._playing_sound_process = subprocess.Popen(['omxplayer', file.as_posix()], stdout=subprocess.PIPE,
                                                       stderr=subprocess.DEVNULL)

    def shutdown(self) -> None:
        subprocess.run(['shutdown', '-r', 'now'], check=True)

    def set_date_time(self, data_time: str) -> None:
        subprocess.run(['timedatectl', 'set-time', data_time], check=True)

    def is_ntp_synchronized(self) -> bool:
        out: str = subprocess.run(['timedatectl', 'status'], capture_output=True, text=True, check=True).stdout

        match = re.findall(r'\s+(NTP service: active)\s+', out)

        return bool(match)

    def get_wpa_config(self) -> dict:
        if not self._wpa_supplicant_conf_file.exists():
            raise OperationSystemException('WPA file does not exist')

        wpa_conf_content = self._wpa_supplicant_conf_file.read_text()
        ssid = re.search(r'ssid\s*=\s*\"(.+)\"', wpa_conf_content)
        psk = re.search(r'psk\s*=\s*\"(.+)\"', wpa_conf_content)
        key_mgmt = re.search(r'key_mgmt\s*=\s*(\S+)', wpa_conf_content)
        try:
            return {
                'ssid': ssid.group(1) if ssid else "",
                'psk': psk.group(1) if psk else "",
                'key_mgmt': key_mgmt.group(1) if key_mgmt else ""
            }
        except Exception as error:
            raise OperationSystemException(f'Can not load WPA config: {error}')

    def get_ip_address(self) -> Optional[str]:
        output = subprocess.run(['ifconfig', 'wlan0'], stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
        response = output.stdout.decode("utf-8")
        match = re.search(r'inet\s(\d+.\d+.\d+.\d+)', response)
        if match:
            return match.group(1)
        else:
            return None

    def apply_wifi_settings(self) -> None:
        output = subprocess.run(['wpa_cli', '-i', 'wlan0', 'reconfigure'], stdout=subprocess.PIPE,
                                stderr=subprocess.STDOUT)
        if output.stdout.decode("utf-8").strip() != 'OK':
            raise OperationSystemException(f'Applying Wi-fi settings has failed: {output.stdout}')

    def get_available_access_points(self) -> List[dict]:
        output = subprocess.run(['iwlist', 'wlan0', 'scan'], stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
        re_ap_info_address = re.compile(r'Address:\s(.+)|ESSID:"(.+)"\n')
        re_ap_info_ssid = re.compile(r'ESSID:"(.+)"\n')
        aps = []
        for str_ap in re.split(r'Cell\s\d+\s-\s', output.stdout.decode("utf-8")):
            ap_info_address = re_ap_info_address.search(str_ap)
            ap_info_ssid = re_ap_info_ssid.search(str_ap)
            if ap_info_address and ap_info_ssid:
                aps.append({
                    'ssid': ap_info_ssid.group(1),
                    'address': ap_info_address.group(1)
                })

        return aps


def get_operation_system() -> OperationSystem:
    if platform == "linux" or platform == "linux2":
        return LinuxOperationSystem()
    elif platform == "win32":
        return WindowsOperationSystem()
    else:
        raise OperationSystemException('Unhandled OS. Trying to get OperationSystem handler for unknown OS')

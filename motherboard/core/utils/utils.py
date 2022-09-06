import os
import re
import subprocess
import time
from sys import platform


def millis() -> int:
    return round(time.time() * 1000)


def constrain_number(value: int, min_valu: int, max_value: int) -> int:
    return max(min(max_value, value), min_valu)


def boolean(value) -> bool:
    if isinstance(value, bool):
        return value
    elif isinstance(value, str):
        if value.lower() == 'false':
            return False
        elif value.lower() == 'true':
            return True
        else:
            raise ValueError(f'Cannot convert string value "{value}" to Boolean')
    else:
        raise ValueError(f'Cannot "{value}" to Boolean')


def os_set_data_time(data_time: str) -> None:
    """
    Runs OS command to set up data time using the following format Y-d-m H:i:s
    It handles Linux and Windows. (Temporary) However, for windows it is ignore because dev is done on windows

    :param data_time: string representation of data time. Example: 2000-06-02 08:47:46
    :return: None
    """

    if platform == "linux" or platform == "linux2":
        subprocess.run(['timedatectl', 'set-time', data_time], check=True)
    elif platform == "win32":
        print(f'IGNORE installing time for windows. Date time: {data_time}')
    else:
        raise Exception('Unknown OS to setup date time')


def is_ntp_synchronized() -> bool:
    """
    Checks if NTP is synchronized

    :return: True if yes, otherwise False
    """

    out: str = subprocess.run(['timedatectl', 'status'], capture_output=True, text=True, check=True).stdout
    match = re.findall(r'\s+(System clock synchronized: yes)\s+', out)

    return bool(match)

import os
from typing import Any, Optional

settings_prod = {
    'SOCKET_PORT': 1489,
    'UART_PORT': '/dev/serial0',
    'UART_SPEED': 9600,
    'LOGS_FOLDER': '/home/pi/logs',
    'CORE_CONFIG': '/home/pi/config.json',
    'A1_MOCKUP': False
}

settings_development = {
    'SOCKET_PORT': 1489,
    'UART_PORT': 'COM5',
    'UART_SPEED': 9600,
    'LOGS_FOLDER': r'D:\vacuum-cleaner\motherboard\core\logs',
    'CORE_CONFIG': r'D:\vacuum-cleaner\motherboard\core\config.json',
    'A1_MOCKUP': False
}

is_production = os.getenv('VACUUM_ROBOT_CLEANER_PRODUCTION') is not None


def get(name: str) -> Any:
    settings = settings_prod if is_production else settings_development
    value: Optional = settings.get(name)
    if value is None:
        raise RuntimeError(
            f'Can not get setting attribute: {name}. Settings mode: {"production" if is_production else "development"}')
    return value

import logging
from pathlib import Path
from typing import Optional

formatter = logging.Formatter('%(asctime)s%(msecs)03d %(levelname)s %(message)s', '%H:%M:%S:')


def _create_logger(name: str, console: bool, _formatter):
    logger = logging.getLogger(name)
    logger.setLevel(logging.DEBUG)
    if console:
        handler = logging.StreamHandler()
        handler.setFormatter(_formatter)
        logger.addHandler(handler)
    else:
        logs_folder = Path('logs')
        logs = logs_folder.joinpath(f'{name}.txt')
        logs.parent.mkdir(parents=True, exist_ok=True)
        handler = logging.FileHandler(logs)
        handler.setFormatter(formatter)
        logger.addHandler(handler)
    return logger


class WifiModuleLogger(object):
    def __init__(self, console=False):
        self._logger = _create_logger('wifi-module', console, formatter)

    def debug(self, message: str) -> None:
        self._logger.debug(message)

    def error(self, message: str) -> None:
        self._logger.error(message)

    def info(self, message: str) -> None:
        self._logger.info(message)


class A1Logger(object):
    def __init__(self, console=False):
        self._robot_movement_logger = _create_logger('robot-movement', console, formatter)
        self._a1_logger = _create_logger('a1', console, formatter)

    def print_movement(self, forward: bool, speed: int, distance: Optional[int] = None,
                       with_stop: bool = False) -> None:
        direction_and_speed = f"direction:{'F' if forward else 'B'};speed:{speed}"
        distance = f'distance:{distance};' if distance else 'distance:infinitive'
        with_stop = f'with_stop:true' if with_stop else ''
        self._robot_movement_logger.info(f'MOVE: {direction_and_speed};{distance};{with_stop};')

    def print_turn(self, left: bool, speed: int, angle: int, with_stop: bool = False) -> None:
        side_speed_angle = f"side:{'L' if left else 'R'};speed:{speed};angle:{angle}"
        with_stop = f'with_stop:true' if with_stop else ''

        self._robot_movement_logger.info(f'TURN: {side_speed_angle};{with_stop};')

    def debug(self, message: str) -> None:
        self._a1_logger.debug(message)


a1_logger = A1Logger(True)
wifi_module_logger = WifiModuleLogger(True)

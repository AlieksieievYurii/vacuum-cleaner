import logging
from pathlib import Path
from typing import Optional
from datetime import datetime

formatter = logging.Formatter('%(asctime)s%(msecs)03d %(name)s %(levelname)s %(message)s', '%H:%M:%S:')


def _create_logger(name: str, console: bool, _formatter):
    logger = logging.getLogger(name)
    logger.setLevel(logging.DEBUG)
    if console:
        handler = logging.StreamHandler()
        handler.setFormatter(_formatter)
        logger.addHandler(handler)
    else:
        logs_folder = Path(__file__).parent / 'logs'
        print(logs_folder)
        logs_folder.mkdir(parents=True, exist_ok=True)
        logs = logs_folder.joinpath(f'{name}_{datetime.now().strftime("%d_%m_%Y-%H_%M_%S")}.txt')
        handler = logging.FileHandler(logs)
        handler.setFormatter(formatter)
        logger.addHandler(handler)
    return logger


class Logger(object):
    def __init__(self, name: str, log_file: Optional[Path]):
        self._logger = logging.getLogger(name)
        self._logger.setLevel(logging.DEBUG)
        if log_file:
            handler = logging.FileHandler(log_file)
            handler.setFormatter(formatter)
            self._logger.addHandler(handler)
        else:
            handler = logging.StreamHandler()
            handler.setFormatter(formatter)
            self._logger.addHandler(handler)

    def debug(self, message: str) -> None:
        self._logger.debug(message)

    def error(self, message: str) -> None:
        self._logger.error(message)

    def info(self, message: str) -> None:
        self._logger.info(message)

    def critical(self, message: str) -> None:
        self._logger.critical(message)


class LoggerFactory(object):
    def __init__(self, logs_folder: str):
        self._path = Path(logs_folder)
        self._session_log_folder_path = None

    @property
    def _session_log_folder(self) -> Path:
        if self._session_log_folder_path is None:
            self._session_log_folder_path = self._path.joinpath(datetime.now().strftime("%d_%m_%Y-%H_%M_%S"))
            self._session_log_folder_path.mkdir(parents=True, exist_ok=True)
        return self._session_log_folder_path

    def get_logger(self, name: str, console: bool) -> Logger:
        return Logger(name, log_file=None if console else self._session_log_folder.joinpath(f'{name}.txt'))

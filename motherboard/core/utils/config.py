import json
from pathlib import Path
from typing import Optional


class Configuration(object):
    DEFAULT_CLEANING_ALGORITHM: str = 'simple'
    DEFAULT_PID_SETTINGS = (0.1, 0.1, 0)

    def __init__(self, path: str):
        self._config_file = Path(path)

        if not self._config_file.exists():
            self._save_config({})

    def get_pid_settings(self) -> tuple:
        config: dict = self._get_config()

        def setup_and_return_default_values() -> tuple:
            config['PID'] = {
                'proportional': self.DEFAULT_PID_SETTINGS[0],
                'integral': self.DEFAULT_PID_SETTINGS[1],
                'derivative': self.DEFAULT_PID_SETTINGS[2],
            }
            self._save_config(config)
            return self.DEFAULT_PID_SETTINGS

        pid_settings: Optional[dict] = config.get('PID')

        if not pid_settings:
            return setup_and_return_default_values()

        p, i, d = pid_settings.get('proportional'), pid_settings.get('integral'), pid_settings.get('derivative')

        if p is not None and i is not None and d is not None:
            return p, i, d
        else:
            return setup_and_return_default_values()

    def set_pid_settings(self, proportional: float, integral: float, derivative: float) -> None:
        config: dict = self._get_config()
        config['PID'] = {
            'proportional': proportional,
            'integral': integral,
            'derivative': derivative,
        }

        self._save_config(config)

    def set_target_cleaning_algorithm(self, name: str):
        config: dict = self._get_config()
        config['cleaning_algorithm'] = name
        self._save_config(config)

    def get_selected_cleaning_algorithm(self) -> str:
        config = self._get_config()
        cleaning_algorithm: Optional[str] = config.get('cleaning_algorithm')
        if not cleaning_algorithm:
            self.set_target_cleaning_algorithm(self.DEFAULT_CLEANING_ALGORITHM)
            return self.DEFAULT_CLEANING_ALGORITHM
        return cleaning_algorithm

    def _get_config(self) -> dict:
        return json.loads(self._config_file.read_text())

    def _save_config(self, config: dict) -> None:
        self._config_file.write_text(json.dumps(config, indent=4))

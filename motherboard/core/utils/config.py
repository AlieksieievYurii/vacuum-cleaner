import json
from pathlib import Path
from typing import Optional


class Configuration(object):
    DEFAULT_CLEANING_ALGORITHM: str = 'simple'

    def __init__(self, path: str = r'D:\vacuum-cleaner\motherboard\core\config.json'):
        self._config_file = Path(path)

        if not self._config_file.exists():
            self._save_config({})

    def set_pid_settings(self, proportional: float, integral: float, derivative: float) -> None:
        pass

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

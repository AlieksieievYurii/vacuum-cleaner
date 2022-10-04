import json
from pathlib import Path
from threading import Thread
from typing import Optional, List, Type, Any

from a1.models import A1Data
from algo.scripts.smart import Smart
from algo.scripts.script import Script, ArgumentsHolder
from algo.scripts.simple import Simple
from utils.logger import AlgorithmManagerLogger


class AlgorithmManagerException(Exception):
    pass


class LoadingScriptArgumentsException(AlgorithmManagerException):
    pass


class AlgorithmManager(object):
    ALGORITHM_SCRIPTS: List[Type[Script]] = [
        Simple,
        Smart
    ]

    def __init__(self, a1_data: A1Data, logger: AlgorithmManagerLogger):
        self._a1_data = a1_data
        self._logger = logger
        self._algorithm_script: Optional[Script] = None
        self._algorithm_script_arguments_holder: Optional[ArgumentsHolder] = None
        self._working_thread: Optional[Thread] = None
        self._configs_folder: Path = Path(__file__).parent / 'scripts' / 'configs'

    def set_script(self, name: str) -> None:
        self._logger.info(f'Set Cleaning Algorithm Script: {name}')
        self._algorithm_script = self._get_script(name)
        self._algorithm_script_arguments_holder = self._load_arguments(self._algorithm_script)

    def set_script_parameters(self, script_name: str, arguments: dict) -> None:
        """
        Sets parameters for the given script. The parameters are saved to the file BUT NOT APPLIED
        # TODO refactor the code by specifying what is argument and parameter
        :param script_name: target script to apply for
        :param arguments: dict of parameters e.g {"parameter_name": param_value}
        :return: None
        """

        script = self._get_script(script_name)
        res = {}
        for arg in script.get_arguments().values():
            value = arguments.get(arg.name)
            if value is None:
                raise AlgorithmManagerException(f'Parameter is not found')
            if not self._does_value_meet_type(arg.type, value):
                raise AlgorithmManagerException(f'Wrong type. Name: {arg.name}; Value: {value}')

            res[arg.name] = value
        self._save_config_for(script, res)

    def get_current_script_name(self) -> str:
        if self._algorithm_script:
            return self._algorithm_script.get_name()
        raise AlgorithmManagerException('Script is not set')

    def get_scripts(self) -> List[dict]:
        """
        Returns the list of dicts containing all registered algorithm scripts.

        :return: list of dicts
        """
        return [self.get_script_info(script) for script in self.ALGORITHM_SCRIPTS]

    def get_script_info(self, script: Type[Script]) -> dict:
        """
        Returns the information(name, description, arguments) of the given Script class

        :param script: subclass of Script
        :return: dict containing name, description, arguments
        """

        script_info: dict = {
            'name': script.get_name(),
            'description': script.get_description()
        }

        saved_values = self._load_arguments(script)

        script_info['arguments'] = [{
            'name': arg.name,
            'value_type': self._represent_type(arg.type),
            'default': arg.default,
            'value': saved_values.__getattribute__(arg_ref)
        } for arg_ref, arg in script.get_arguments().items()]

        return script_info

    def start(self):
        if self._working_thread:
            raise AlgorithmManagerException('Algorithm is already executing')
        if not self._algorithm_script:
            raise AlgorithmManagerException('Can not start algorithm, because not selected')

        self._working_thread = Thread('Algo', target=self._algorithm_loop, daemon=False)
        self._working_thread.start()

    def _algorithm_loop(self) -> None:
        while True:
            self._algorithm_script.loop(self._a1_data)

    def _get_script(self, script_name: str) -> Type[Script]:
        for script in self.ALGORITHM_SCRIPTS:
            if script.get_name() == script_name:
                return script
        else:
            raise AlgorithmManagerException(f'Can not find the given script: {script_name}')

    def _load_arguments(self, script: Script) -> ArgumentsHolder:
        self._logger.info(f'Try to load "{script.get_name()}" arguments from the config')
        try:
            args = self._load_arguments_values_from_file(script)
            self._logger.info(f'Saved arguments of "{script.get_name()}" are loaded')
            return args
        except LoadingScriptArgumentsException as error:
            self._logger.error(
                f'Can not load script algorithm config for {script.get_name()}.'
                f' So recreating with default values. Reason: {error}')
            return self._recreate_argument_config_file(script)

    def _load_arguments_values_from_file(self, script: Script) -> ArgumentsHolder:
        """
        Reads the given config file. Tries to get correct value for the given script's arguments.
        Otherwise the exception is raised.

        :param script: an instance of target Script
        :return: ArgumentsHolder containing arguments with saved values
        """

        arguments_holder = ArgumentsHolder()
        config_dict = self._read_config_for(script)

        for arg_ref_name, arg in script.get_arguments().items():
            value: Optional = config_dict.get(arg.name)
            if value is None:
                raise LoadingScriptArgumentsException(
                    f'Value of "{arg.name}" does not exist. Val: {arg.default}; Type: {arg.type}')
            elif self._does_value_meet_type(arg.type, value):
                setattr(arguments_holder, arg_ref_name, value)
            else:
                raise LoadingScriptArgumentsException(
                    f'Value of "{arg.name}" does not meet the type. Val: {arg.default}; Type: {arg.type}')

        return arguments_holder

    def _recreate_argument_config_file(self, script: Script) -> ArgumentsHolder:
        """
        Creates/Overrides script config file with the default values.

        :param script: an instance of target Script
        :return: ArgumentsHolder containing arguments with default values
        """

        arguments_holder = ArgumentsHolder()
        result: dict = {}
        for arg_ref_name, arg in script.get_arguments().items():
            if arg.type is None:
                raise LoadingScriptArgumentsException('Type can not be None')
            elif arg.default is None:
                raise LoadingScriptArgumentsException('Default Value can not be None')
            elif not self._does_value_meet_type(arg.type, arg.default):
                raise LoadingScriptArgumentsException(
                    f'Default value does not meet the type. Val: {arg.default}; Type: {arg.type} ')
            result[arg.name] = arg.default
            setattr(arguments_holder, arg_ref_name, arg.default)

        self._save_config_for(script, result)

        return arguments_holder

    @staticmethod
    def _does_value_meet_type(value_type: Any, value: Any) -> bool:
        if isinstance(value_type, range) or isinstance(value_type, list):
            return value in value_type
        else:
            return isinstance(value, value_type)

    @staticmethod
    def _represent_type(t: Any) -> str:
        primitive_types = {
            bool: 'boolean',
            str: 'string',
            int: 'integer',
            float: 'floating'
        }

        if isinstance(t, range):
            return f'{t.start}..{t.stop}'
        elif isinstance(t, list):
            return ','.join(str(v) for v in t)
        else:
            primitive_type = primitive_types.get(t)
            if primitive_type:
                return primitive_type

        raise AlgorithmManagerException(f'Unsupported type for the argument: {t}')

    def _read_config_for(self, script: Script) -> dict:
        config_file = self._configs_folder / f'{script.get_name()}.json'
        if not config_file.exists():
            raise LoadingScriptArgumentsException(f'Config file for "{script.get_name()}" does not exist!')
        return json.loads(config_file.read_bytes())

    def _save_config_for(self, script: Script, parameters: dict) -> None:
        config_file = self._configs_folder / f'{script.get_name()}.json'
        config_file.write_text(json.dumps(parameters, indent=4))

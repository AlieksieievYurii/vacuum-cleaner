import json
from pathlib import Path
from threading import Thread
from typing import Optional, List, Type, Any

from a1.models import A1Data
from algo.algorithms.smart import Smart
from algo.algorithms.algorithm import Algorithm, ArgumentsHolder
from algo.algorithms.simple import Simple
from utils.logger import AlgorithmManagerLogger


class AlgorithmManagerException(Exception):
    pass


class LoadingAlgorithmArgumentsException(AlgorithmManagerException):
    pass


class AlgorithmManager(object):
    ALGORITHMS: List[Type[Algorithm]] = [
        Simple,
        Smart
    ]

    def __init__(self, a1_data: A1Data, logger: AlgorithmManagerLogger):
        self._a1_data = a1_data
        self._logger = logger
        self._algorithm: Optional[Algorithm] = None
        self._working_thread: Optional[Thread] = None
        self._configs_folder: Path = Path(__file__).parent / 'algorithms' / 'configs'

        self._stop = False

    def set_algorithm(self, name: str) -> None:
        """
        Sets algorithm for execution. This function must be called firstly before start

        :param name: algorithm's name
        :return: None
        """

        if self.is_running:
            raise AlgorithmManagerException('Forbidden to set target algorithm during the execution ')

        self._logger.info(f'Set Cleaning Algorithm: {name}')
        algorithm = self._get_algorithm_class(name)
        algorithm_arguments_holder = self._load_arguments(algorithm)
        self._algorithm = algorithm(algorithm_arguments_holder)

    def save_algorithm_arguments(self, algorithm_name: str, arguments: dict) -> None:
        """
        Saves arguments for the given algorithm to the file, BUT NOT APPLIED to the selected algorithm

        :param algorithm_name: target algorithm to apply for
        :param arguments: dict of arguments e.g {"parameter_name": param_value}
        :return: None
        """

        algorithm = self._get_algorithm_class(algorithm_name)
        res = {}
        for arg in algorithm.get_parameters().values():
            value = arguments.get(arg.name)

            if value is None:
                raise AlgorithmManagerException(f'Argument value for "{arg.name}" is not found')
            if not self._does_value_meet_type(arg.type, value):
                raise AlgorithmManagerException(f'Wrong type. Name: {arg.name}; Value: {value}')

            res[arg.name] = value

        self._save_config_for(algorithm, res)

    def get_current_algorithm_name(self) -> str:
        """
        Gets current algorithm's name.

        :return: algorithm's unique name
        """

        if self._algorithm:
            return self._algorithm.get_name()
        raise AlgorithmManagerException('Algorithm is not set')

    def get_algorithms(self) -> List[dict]:
        """
        Returns the list of dicts containing all registered algorithms.

        :return: list of dicts
        """

        return [self.get_algorithm_info(algorithm) for algorithm in self.ALGORITHMS]

    def get_algorithm_info(self, algorithm: Type[Algorithm]) -> dict:
        """
        Returns the information(name, description, arguments) of the given Algorithm class

        :param algorithm: subclass of Algorithm
        :return: dict containing name, description, arguments
        """

        algorithm_info: dict = {
            'name': algorithm.get_name(),
            'description': algorithm.get_description()
        }

        saved_values = self._load_arguments(algorithm)

        algorithm_info['arguments'] = [{
            'name': parameter.name,
            'value_type': self._represent_type(parameter.type),
            'default': parameter.default,
            'value': saved_values.__getattribute__(parameter_ref)
        } for parameter_ref, parameter in algorithm.get_parameters().items()]

        return algorithm_info

    def start(self) -> None:
        """
        Starts selected algorithm execution in the separated thread.

        :return: None
        """

        if self._working_thread:
            raise AlgorithmManagerException('Algorithm is already executing')
        if not self._algorithm:
            raise AlgorithmManagerException('Can not start algorithm, because not selected')

        self._working_thread = Thread(name='algorithm-execution', target=self._algorithm_loop, daemon=False)
        self._working_thread.start()

    @property
    def is_running(self) -> bool:
        return self._working_thread is not None

    def is_paused(self) -> bool:
        pass

    def stop(self) -> None:
        """
        Stops algorithm execution and waits until thread is closed.

        :return: None
        """

        self._stop = True
        self._working_thread.join()
        self._working_thread = None

    def _algorithm_loop(self) -> None:
        while True:
            if self._stop:
                break

            self._algorithm.loop(self._a1_data)

    def _get_algorithm_class(self, algorithm_name: str) -> Type[Algorithm]:
        for algorithm in self.ALGORITHMS:
            if algorithm.get_name() == algorithm_name:
                return algorithm
        else:
            raise AlgorithmManagerException(f'Can not find the given algorithm: {algorithm_name}')

    def _load_arguments(self, algorithm: Algorithm) -> ArgumentsHolder:
        self._logger.info(f'Try to load "{algorithm.get_name()}" arguments from the config')
        try:
            args = self._load_arguments_from_file(algorithm)
            self._logger.info(f'Saved arguments of "{algorithm.get_name()}" are loaded')
            return args
        except LoadingAlgorithmArgumentsException as error:
            self._logger.error(
                f'Can not load algorithm config for {algorithm.get_name()}.'
                f' So recreating with default values. Reason: {error}')
            return self._recreate_argument_config_file(algorithm)

    def _load_arguments_from_file(self, algorithm: Type[Algorithm]) -> ArgumentsHolder:
        """
        Reads the given config file. Tries to get correct value for the given algorithm's parameters.
        Otherwise the exception is raised.

        :param algorithm: class of Algorithm
        :return: ArgumentsHolder containing arguments with saved values
        """

        arguments_holder = ArgumentsHolder()
        config_dict = self._read_config_for(algorithm)

        for parameter_ref, parameter in algorithm.get_parameters().items():
            value: Optional = config_dict.get(parameter.name)
            if value is None:
                raise LoadingAlgorithmArgumentsException(
                    f'Value of "{parameter.name}" does not exist. Val: {parameter.default}; Type: {parameter.type}')
            elif self._does_value_meet_type(parameter.type, value):
                setattr(arguments_holder, parameter_ref, value)
            else:
                raise LoadingAlgorithmArgumentsException(
                    f'Value of "{parameter.name}" does not meet the type.'
                    f' Val: {parameter.default}; Type: {parameter.type}')

        return arguments_holder

    def _recreate_argument_config_file(self, algorithm: Type[Algorithm]) -> ArgumentsHolder:
        """
        Creates/Overrides algorithm config file with the default values.

        :param algorithm: class of Algorithm
        :return: ArgumentsHolder containing arguments with default values
        """

        arguments_holder = ArgumentsHolder()
        result: dict = {}
        for parameter_ref, parameter in algorithm.get_parameters().items():
            if parameter.type is None:
                raise LoadingAlgorithmArgumentsException('Type can not be None')
            elif parameter.default is None:
                raise LoadingAlgorithmArgumentsException('Default Value can not be None')
            elif not self._does_value_meet_type(parameter.type, parameter.default):
                raise LoadingAlgorithmArgumentsException(
                    f'Default value does not meet the type. Val: {parameter.default}; Type: {parameter.type} ')
            result[parameter.name] = parameter.default
            setattr(arguments_holder, parameter_ref, parameter.default)

        self._save_config_for(algorithm, result)

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

    def _read_config_for(self, algorithm: Algorithm) -> dict:
        config_file = self._configs_folder / f'{algorithm.get_name()}.json'
        if not config_file.exists():
            raise LoadingAlgorithmArgumentsException(f'Config file for "{algorithm.get_name()}" does not exist!')
        return json.loads(config_file.read_bytes())

    def _save_config_for(self, algorithm: Algorithm, parameters: dict) -> None:
        config_file = self._configs_folder / f'{algorithm.get_name()}.json'
        config_file.write_text(json.dumps(parameters, indent=4))

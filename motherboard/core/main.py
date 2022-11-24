import settings

from a1.robot import Robot, RobotUART, RobotMockUp
from a1.socket import A1Socket
from algo.algo_manager import AlgorithmManager
from blservice.communicator import BluetoothCommunicator
from blservice.service import BluetoothService
from core import Core
from utils.config import Configuration
from utils.os import OperationSystem, get_operation_system
from utils.speetch.voice import Voice
from utils.speetch.voices import RudeMaximVoice

from wifi.comunicator import WifiCommunicator

from utils.logger import LoggerFactory, Logger
from wifi.service import WifiService


def get_robot(robot_logger: Logger, a1_logger: Logger) -> Robot:
    if settings.get('A1_MOCKUP'):
        return RobotMockUp(robot_logger)
    else:
        a1_socket = A1Socket(settings.get('UART_PORT'), settings.get('UART_SPEED'), a1_logger)
        return RobotUART(a1_socket, robot_logger)


def main() -> None:
    logger_factory = LoggerFactory(settings.get('LOGS_FOLDER'))
    core_logger = logger_factory.get_logger('core', settings.get('CAPTURE_CORE_LOG'))
    wifi_logger = logger_factory.get_logger('wifi', settings.get('CAPTURE_WIFI_LOG'))
    robot_logger = logger_factory.get_logger('robot', settings.get('CAPTURE_ROBOT_LOG'))
    a1_socket_logger = logger_factory.get_logger('a1-socket', settings.get('CAPTURE_A1_LOG'))
    bluetooth_logger = logger_factory.get_logger('blservice', settings.get('CAPTURE_A1_LOG'))
    algorithm_manager_logger = logger_factory.get_logger('algo-manager', settings.get('CAPTURE_ALGO_MANAGER_LOG'))

    operation_system: OperationSystem = get_operation_system()
    robot = get_robot(robot_logger, a1_socket_logger)
    config = Configuration(settings.get('CORE_CONFIG'))
    wifi_communicator = WifiCommunicator(settings.get('SOCKET_PORT'))
    wifi_service = WifiService(wifi_communicator, wifi_logger)
    bluetooth_communicator = BluetoothCommunicator()
    bluetooth_service = BluetoothService(bluetooth_communicator, bluetooth_logger)
    voice: Voice = RudeMaximVoice(operation_system)
    algorithm_manager = AlgorithmManager(robot, voice, algorithm_manager_logger)

    core = Core(os=operation_system,
                robot=robot,
                config=config,
                wifi_service=wifi_service,
                bluetooth_service=bluetooth_service,
                algorithm_manager=algorithm_manager,
                voice=voice,
                logger=core_logger,
                debug=False)
    core.run()


# @robotcleaner

if __name__ == '__main__':
    main()

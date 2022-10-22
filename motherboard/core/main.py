import settings

from a1.robot import Robot
from a1.socket import A1Socket
from algo.algo_manager import AlgorithmManager
from bluetooth.handler import BluetoothEndpointsHandler
from core import Core
from utils.config import Configuration
from utils.os import OperationSystem, get_operation_system
from utils.speetch.voice import Voice
from utils.speetch.voices import RudeMaximVoice

from wifi.comunicator import WifiCommunicator

from utils.logger import wifi_module_logger, CoreLogger, algorithm_manager_logger
from wifi.handler import WifiEndpointsHandler

def main():
    operation_system: OperationSystem = get_operation_system()
    a1_socket = A1Socket(settings.get('UART_PORT'), settings.get('UART_SPEED'))
    robot = Robot(a1_socket)
    config = Configuration(settings.get('CORE_CONFIG'))
    wifi_communicator = WifiCommunicator(settings.get('SOCKET_PORT'))
    wifi_endpoints_handler = WifiEndpointsHandler(wifi_communicator, wifi_module_logger)
    bl_endpoint_handler = BluetoothEndpointsHandler()
    algorithm_manager = AlgorithmManager(robot, algorithm_manager_logger)
    voice: Voice = RudeMaximVoice(operation_system)
    core = Core(os=operation_system,
                robot=robot,
                config=config,
                wifi_endpoints_handler=wifi_endpoints_handler,
                bl_endpoint_handler=bl_endpoint_handler,
                algorithm_manager=algorithm_manager,
                voice=voice,
                logger=CoreLogger(True),
                debug=False)
    core.run()


# @robotcleaner

if __name__ == '__main__':
    main()

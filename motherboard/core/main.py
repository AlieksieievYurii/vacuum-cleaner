from time import sleep

from a1.robot import Robot
from a1.socket import A1Socket
from utils.request_handler.handler import RequestHandlerService
from wifi.comunicator import WifiCommunicator
from wifi.endpoints.a1_data import GetA1DataRequestHandler
from wifi.endpoints.hello_world import HelloWorldRequest
from utils.logger import wifi_module_logger
from wifi.endpoints.motor import Motor
from wifi.endpoints.movement import Movement, StopMovement
from wifi.endpoints.sys_info import GetRobotSysInfo


def main():
    #COM5
    a1_socket = A1Socket("/dev/serial0")
    a1_socket.open()

    robot = Robot(a1_socket)

    wifi_communicator = WifiCommunicator()
    wifi_request_handler_service = RequestHandlerService(wifi_communicator, wifi_module_logger)
    wifi_request_handler_service.register(HelloWorldRequest())
    wifi_request_handler_service.register(GetRobotSysInfo())
    wifi_request_handler_service.register(Motor(robot))
    wifi_request_handler_service.register(Movement(robot))
    wifi_request_handler_service.register(StopMovement(robot))
    wifi_request_handler_service.register(GetA1DataRequestHandler(robot))

    robot.core_is_initialized(True).expect()

    wifi_communicator.accept_connection()
    wifi_request_handler_service.start()

    while True:
        if wifi_request_handler_service.is_connection_closed:
            wifi_communicator.accept_connection()
            wifi_request_handler_service.start()


if __name__ == '__main__':
    main()

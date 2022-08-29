
from a1.robot import Robot
from a1.socket import A1Socket
from bluetooth.handler import BluetoothEndpointsHandler
from wifi.comunicator import WifiCommunicator
from wifi.endpoints.a1_data import GetA1DataRequestHandler
from wifi.endpoints.hello_world import HelloWorldRequest
from utils.logger import wifi_module_logger
from wifi.endpoints.motor import Motor
from wifi.endpoints.movement import Movement, StopMovement
from wifi.endpoints.sys_info import GetRobotSysInfo
from wifi.handler import WifiEndpointsHandler

WIFI_SOCKET_PORT = 1489


class Core(object):
    def __init__(self, robot: Robot, wifi_endpoints_handler: WifiEndpointsHandler,
                 bluetooth_endpoint_handler: BluetoothEndpointsHandler):
        self._robot = robot
        self._wifi_endpoints_handler = wifi_endpoints_handler
        self._bluetooth_endpoint_handler = bluetooth_endpoint_handler
        self._wifi_endpoints_handler.register_endpoint(HelloWorldRequest())
        self._wifi_endpoints_handler.register_endpoint(HelloWorldRequest())
        self._wifi_endpoints_handler.register_endpoint(GetRobotSysInfo())
        self._wifi_endpoints_handler.register_endpoint(Motor(self._robot))
        self._wifi_endpoints_handler.register_endpoint(Movement(self._robot))
        self._wifi_endpoints_handler.register_endpoint(StopMovement(self._robot))
        self._wifi_endpoints_handler.register_endpoint(GetA1DataRequestHandler(self._robot))

    def run(self) -> None:
        self._robot.connect()
        self._wifi_endpoints_handler.start()
        self._run_core_loop()

    def _run_core_loop(self) -> None:
        while True:
            pass


def main():
    # COM5
    a1_socket = A1Socket("/dev/serial0")
    robot = Robot(a1_socket)
    wifi_communicator = WifiCommunicator(WIFI_SOCKET_PORT)
    wifi_endpoints_handler = WifiEndpointsHandler(wifi_communicator, wifi_module_logger)
    bluetooth_endpoints_handler = BluetoothEndpointsHandler()
    core = Core(robot, wifi_endpoints_handler, bluetooth_endpoints_handler)
    core.run()


if __name__ == '__main__':
    main()

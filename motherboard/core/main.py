from datetime import datetime

from a1.models import ButtonState
from a1.robot import Robot
from a1.socket import A1Socket
from bluetooth.handler import BluetoothEndpointsHandler
from utils.utils import is_ntp_synchronized, os_set_data_time
from wifi.comunicator import WifiCommunicator
from wifi.endpoints.a1_data import GetA1DataRequestHandler
from wifi.endpoints.hello_world import HelloWorldRequest
from utils.logger import wifi_module_logger, CoreLogger
from wifi.endpoints.motor import Motor
from wifi.endpoints.movement import Movement, StopMovement
from wifi.endpoints.sys_info import GetRobotSysInfo
from wifi.handler import WifiEndpointsHandler

WIFI_SOCKET_PORT = 1489


class Core(object):
    def __init__(self, robot: Robot, wifi_endpoints_handler: WifiEndpointsHandler,
                 bluetooth_endpoint_handler: BluetoothEndpointsHandler, logger: CoreLogger, **kwargs):
        self._debug = bool(kwargs.get('debug'))
        self._robot = robot
        self._wifi_endpoints_handler = wifi_endpoints_handler
        self._bluetooth_endpoint_handler = bluetooth_endpoint_handler
        self._logger = logger
        self._wifi_endpoints_handler.register_endpoint(HelloWorldRequest())
        self._wifi_endpoints_handler.register_endpoint(HelloWorldRequest())
        self._wifi_endpoints_handler.register_endpoint(GetRobotSysInfo())
        self._wifi_endpoints_handler.register_endpoint(Motor(self._robot))
        self._wifi_endpoints_handler.register_endpoint(Movement(self._robot))
        self._wifi_endpoints_handler.register_endpoint(StopMovement(self._robot))
        self._wifi_endpoints_handler.register_endpoint(GetA1DataRequestHandler(self._robot))

    def run(self) -> None:
        self._logger.print_entry_point()

        try:
            self._robot.connect()
        except Exception as error:
            self._logger.critical(f'Cannot establish connection with A1 module. Reason:{error}')
            if self._debug:
                raise error
            return None

        self._wifi_endpoints_handler.start()
        #self._initialization()
        try:
            self._run_core_loop()
        except Exception as error:
            self._logger.critical(f'Core loop is failed. Reason: {error}')
            if self._debug:
                raise error

    def _initialization(self) -> None:
        self._set_core_data_time()

    def _set_core_data_time(self) -> None:
        self._logger.info('Setting DateTime...')
        if is_ntp_synchronized():
            self._logger.info('NTP is synchronized on the core! Setting the datetime for A1 RTC')
            now = datetime.now()
            self._robot.set_date_time(now)
        else:
            self._logger.info('NTP is not synchronized on the core! Reading from A1 RTC')
            rtc_data_time = self._robot.get_date_time().expect().data
            os_set_data_time(rtc_data_time)

    def _run_core_loop(self) -> None:
        while True:
            if self._is_shutting_down_triggered():
                self._shut_down_core()
                break

            but = self._robot.data.button_up
            if but is ButtonState.CLICK:
                print('UP CLICK')
            elif but is ButtonState.LONG_PRESS:
                print('UP LONG PRESS')

            but2 = self._robot.data.button_down
            if but2 is ButtonState.CLICK:
                print('DOWN CLICK')
            elif but2 is ButtonState.LONG_PRESS:
                print('DOWN LONG PRESS')

            but3 = self._robot.data.button_ok
            if but3 is ButtonState.CLICK:
                print('OK CLICK')
            elif but3 is ButtonState.LONG_PRESS:
                print('OK LONG PRESS')

            but4 = self._robot.data.bluetooth_button
            if but4 is ButtonState.CLICK:
                print('B CLICK')
            elif but4 is ButtonState.LONG_PRESS:
                print('B LONG PRESS')

    def _is_shutting_down_triggered(self) -> bool:
        pass

    def _shut_down_core(self) -> None:
        pass


def main():
    # COM5 /dev/serial0
    a1_socket = A1Socket("COM5")
    robot = Robot(a1_socket)
    wifi_communicator = WifiCommunicator(WIFI_SOCKET_PORT)
    wifi_endpoints_handler = WifiEndpointsHandler(wifi_communicator, wifi_module_logger)
    bluetooth_endpoints_handler = BluetoothEndpointsHandler()
    core = Core(robot, wifi_endpoints_handler, bluetooth_endpoints_handler, CoreLogger(True), debug=False)
    core.run()


if __name__ == '__main__':
    main()

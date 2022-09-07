from datetime import datetime

from a1.models import ButtonState
from a1.robot import Robot
from a1.socket import A1Socket
from bluetooth.handler import BluetoothEndpointsHandler
from utils.os import OperationSystem, get_operation_system
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
    def __init__(self, os: OperationSystem, robot: Robot, wifi_endpoints_handler: WifiEndpointsHandler,
                 bluetooth_endpoint_handler: BluetoothEndpointsHandler, logger: CoreLogger, **kwargs):
        self._debug = bool(kwargs.get('debug'))
        self._os = os
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

        self._robot.core_is_initialized(True)

        self._wifi_endpoints_handler.start()
        self._initialization()
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
        if self._os.is_ntp_synchronized():
            self._logger.info('NTP is synchronized on the core! Setting the datetime for A1 RTC')
            now = datetime.now()
            self._robot.set_date_time(now)
        else:
            self._logger.info('NTP is not synchronized on the core! Reading from A1 RTC')
            rtc_data_time = self._robot.get_date_time().expect().data
            self._os.set_date_time(rtc_data_time)

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
        return self._robot.data.is_shut_down_button_triggered

    def _shut_down_core(self) -> None:
        self._logger.info('Preparing to shutdown')
        self._logger.info('Stop Wifi Service...')
        # self._wifi_endpoints_handler.stop() TODO
        self._logger.info('Stop Bluetooth Service...')
        # self._bluetooth_endpoints_handler.stop() TODO
        self._logger.info('Send signal to turn off the robot in 10 seconds to A1...')
        self._robot.set_timer_to_cut_off_power(15).expect()
        self._logger.info('Close A1 Connection...')
        # TODO
        self._logger.info('Perform shutdown the system...')
        self._os.shutdown()


def main():
    os: OperationSystem = get_operation_system()
    # COM5 /dev/serial0
    a1_socket = A1Socket("COM5")
    robot = Robot(a1_socket)
    wifi_communicator = WifiCommunicator(WIFI_SOCKET_PORT)
    wifi_endpoints_handler = WifiEndpointsHandler(wifi_communicator, wifi_module_logger)
    bluetooth_endpoints_handler = BluetoothEndpointsHandler()
    core = Core(os, robot, wifi_endpoints_handler, bluetooth_endpoints_handler, CoreLogger(True), debug=False)
    core.run()


if __name__ == '__main__':
    main()

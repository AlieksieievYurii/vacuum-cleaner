from datetime import datetime

from a1.models import ButtonState
from a1.robot import Robot
from a1.socket import A1Socket
from algo.algo_manager import AlgorithmManager
from bluetooth.handler import BluetoothEndpointsHandler
from utils.config import Configuration
from utils.os import OperationSystem, get_operation_system
from utils.speetch.voice import Voice
from utils.speetch.voices import RudeMaximVoice
from utils.utils import get_typed_arg
from wifi.comunicator import WifiCommunicator
from wifi.endpoints.a1_data import GetA1DataRequestHandler
from wifi.endpoints.algo_scripts import GetAlgorithmsRequest, SetAlgorithmScriptRequest
from wifi.endpoints.hello_world import HelloWorldRequest
from utils.logger import wifi_module_logger, CoreLogger, algorithm_manager_logger
from wifi.endpoints.motor import Motor
from wifi.endpoints.movement import Movement, StopMovement
from wifi.endpoints.sys_info import GetRobotSysInfo
from wifi.handler import WifiEndpointsHandler

WIFI_SOCKET_PORT = 1489


class Core(object):

    def __init__(self, **kwargs):
        self._debug: bool = bool(kwargs.get('debug'))
        self._os: OperationSystem = get_typed_arg('os', OperationSystem, kwargs)
        self._robot: Robot = get_typed_arg('robot', Robot, kwargs)
        self._config: Configuration = get_typed_arg('config', Configuration, kwargs)
        self._wifi_endpoints_handler: WifiEndpointsHandler = get_typed_arg('wifi_endpoints_handler',
                                                                           WifiEndpointsHandler, kwargs)
        self._bluetooth_endpoint_handler: BluetoothEndpointsHandler = get_typed_arg('bl_endpoint_handler',
                                                                                    BluetoothEndpointsHandler, kwargs)
        self._algorithm_manager: AlgorithmManager = get_typed_arg('algorithm_manager', AlgorithmManager, kwargs)
        self._voice: Voice = get_typed_arg('voice', Voice, kwargs)
        self._logger: CoreLogger = get_typed_arg('logger', CoreLogger, kwargs)
        self._wifi_endpoints_handler.register_endpoint(HelloWorldRequest())
        self._wifi_endpoints_handler.register_endpoint(HelloWorldRequest())
        self._wifi_endpoints_handler.register_endpoint(GetRobotSysInfo())
        self._wifi_endpoints_handler.register_endpoint(Motor(self._robot))
        self._wifi_endpoints_handler.register_endpoint(Movement(self._robot))
        self._wifi_endpoints_handler.register_endpoint(StopMovement(self._robot))
        self._wifi_endpoints_handler.register_endpoint(GetA1DataRequestHandler(self._robot))
        self._wifi_endpoints_handler.register_endpoint(GetAlgorithmsRequest(self._algorithm_manager))
        self._wifi_endpoints_handler.register_endpoint(SetAlgorithmScriptRequest(self._algorithm_manager, self._config))

    def run(self) -> None:
        self._logger.print_entry_point()

        try:
            self._robot.connect()
        except Exception as error:
            self._logger.critical(f'Cannot establish connection with A1 module. Reason:{error}')
            if self._debug:
                raise error
            return None

        self._robot.core_is_initialized(is_successful=True)
        self._robot.beep(3, 100)

        self._wifi_endpoints_handler.start()
        self._initialization()
        try:
            self._run_core_loop()
        except Exception as error:
            self._logger.critical(f'Core loop is failed. Reason: {error}')
            if self._debug:
                raise error

    def _initialization(self) -> None:
        self._init_pid_settings()
        self._algorithm_manager.set_algorithm(self._config.get_selected_cleaning_algorithm())
        self._set_core_data_time()

    def _init_pid_settings(self) -> None:
        p, i, d = self._config.get_pid_settings()
        self._logger.info(f'Setting up PID values for a1: P({p}) I({i}) D({d})')
        self._robot.set_pid(p, i, d).expect()

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
        self._voice.say_introduction()
        while True:
            if self._is_shutting_down_triggered():
                self._shut_down_core()
                break

            #
            # but = self._robot.data.button_up
            # if but is ButtonState.CLICK:
            #     print('UP CLICK')
            # elif but is ButtonState.LONG_PRESS:
            #     print('UP LONG PRESS')
            #
            # but2 = self._robot.data.button_down
            # if but2 is ButtonState.CLICK:
            #     print('DOWN CLICK')
            # elif but2 is ButtonState.LONG_PRESS:
            #     print('DOWN LONG PRESS')
            #
            # but3 = self._robot.data.button_ok
            # if but3 is ButtonState.CLICK:
            #     print('OK CLICK')
            # elif but3 is ButtonState.LONG_PRESS:
            #     print('OK LONG PRESS')
            #
            # but4 = self._robot.data.bluetooth_button
            # if but4 is ButtonState.CLICK:
            #     print('B CLICK')
            # elif but4 is ButtonState.LONG_PRESS:
            #     print('B LONG PRESS')

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
    ##COM5 /dev/serial0
    a1_socket = A1Socket("COM5")
    robot = Robot(a1_socket)
    config = Configuration()
    wifi_communicator = WifiCommunicator(WIFI_SOCKET_PORT)
    wifi_endpoints_handler = WifiEndpointsHandler(wifi_communicator, wifi_module_logger)
    bl_endpoint_handler = BluetoothEndpointsHandler()
    algorithm_manager = AlgorithmManager(None, algorithm_manager_logger)
    voice: Voice = RudeMaximVoice(os)
    core = Core(os=os,
                robot=robot,
                config=config,
                wifi_endpoints_handler=wifi_endpoints_handler,
                bl_endpoint_handler=bl_endpoint_handler,
                algorithm_manager=algorithm_manager,
                voice=voice,
                logger=CoreLogger(True),
                debug=False)
    core.run()


if __name__ == '__main__':
    main()

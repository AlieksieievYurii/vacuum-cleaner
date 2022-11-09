from datetime import datetime
from time import sleep
from typing import Optional

from a1.models import ButtonState
from a1.robot import Robot, LedState
from algo.algo_manager import AlgorithmManager
from algo.algorithms.algorithm import LID_IS_OPENED_PAUSE_REASON, DUST_BOX_OUT_PAUSE_REASON, ExecutionState
from blservice.endpoints.wifi import SetWifiCredentialsRequestHandler, GetCurrentWifiCredentialsRequestHandler, \
    GetAvailableAccessPointsRequestHandler
from blservice.service import BluetoothService
from utils.config import Configuration
from utils.os import OperationSystem
from utils.speetch.voice import Voice
from utils.scheduler import Scheduler

from utils.utils import get_typed_arg

from wifi.endpoints.a1_data import GetA1DataRequestHandler
from wifi.endpoints.algo_scripts import GetAlgorithmsRequest, SetAlgorithmScriptRequest, \
    ManageCleaningExecutionRequest, GetCleaningStatusRequest
from utils.logger import Logger
from wifi.endpoints.motor import Motor
from wifi.endpoints.movement import Movement, StopMovement
from wifi.endpoints.pid import GetCurrentPidSettings, SetPidSettings
from wifi.endpoints.power import PowerRequestHandler
from wifi.endpoints.sys_info import GetRobotSysInfo
from wifi.service import WifiService


class Core(object):

    def __init__(self, **kwargs):
        self._debug: bool = bool(kwargs.get('debug'))
        self._os: OperationSystem = get_typed_arg('os', OperationSystem, kwargs)
        self._robot: Robot = get_typed_arg('robot', Robot, kwargs)
        self._config: Configuration = get_typed_arg('config', Configuration, kwargs)
        self._operation_shut_down = False
        self._wifi_service: WifiService = get_typed_arg('wifi_service', WifiService, kwargs)
        self._bluetooth_service: BluetoothService = get_typed_arg('bluetooth_service', BluetoothService, kwargs)
        self._algorithm_manager: AlgorithmManager = get_typed_arg('algorithm_manager', AlgorithmManager, kwargs)
        self._voice: Voice = get_typed_arg('voice', Voice, kwargs)
        self._logger: Logger = get_typed_arg('logger', Logger, kwargs)

        self.scheduler = Scheduler()
        self.scheduler.register(interval=10000, fun=self._check_wifi_connection)
        self.__register_endpoints()

    def __register_endpoints(self):
        self._bluetooth_service.register_endpoint(SetWifiCredentialsRequestHandler(self._os))
        self._bluetooth_service.register_endpoint(GetCurrentWifiCredentialsRequestHandler(self._os))
        self._bluetooth_service.register_endpoint(GetAvailableAccessPointsRequestHandler(self._os))
        self._wifi_service.register_endpoint(GetRobotSysInfo())
        self._wifi_service.register_endpoint(Motor(self._robot))
        self._wifi_service.register_endpoint(Movement(self._robot))
        self._wifi_service.register_endpoint(StopMovement(self._robot))
        self._wifi_service.register_endpoint(GetA1DataRequestHandler(self._robot))
        self._wifi_service.register_endpoint(GetCurrentPidSettings(self._config))
        self._wifi_service.register_endpoint(SetPidSettings(self._config, self._robot))
        self._wifi_service.register_endpoint(GetAlgorithmsRequest(self._algorithm_manager))
        self._wifi_service.register_endpoint(SetAlgorithmScriptRequest(self._algorithm_manager, self._config))
        self._wifi_service.register_endpoint(ManageCleaningExecutionRequest(self._algorithm_manager))
        self._wifi_service.register_endpoint(GetCleaningStatusRequest(self._algorithm_manager))
        self._wifi_service.register_endpoint(PowerRequestHandler(self._on_shut_down, self._on_reboot))

    def _on_reboot(self):
        self._robot.set_booting_up_led().expect()
        self._os.reboot()

    def _on_shut_down(self):
        self._operation_shut_down = True

    def run(self) -> None:
        self._logger.info('====== START ======')

        try:
            self._robot.connect()
        except Exception as error:
            self._logger.critical(f'Cannot establish connection with A1 module. Reason:{error}')
            if self._debug:
                raise error
            return None

        try:
            self._initialization()
        except Exception as error:
            self._logger.critical(f'Initialization is failed. Reason: {error}')
            if self._debug:
                raise error
            return None

        try:
            self._run_core_loop()
        except Exception as error:
            self._logger.critical(f'Core loop is failed. Reason: {error}')
            self._robot.set_error_state()
            if self._debug:
                raise error
        else:
            self._logger.info('==== Core has been finished successfully ===')

    def _initialization(self) -> None:
        self._bluetooth_service.disable_bluetooth()
        self._robot.core_is_initialized(is_successful=True)
        self._robot.set_error_state(enable=False)
        self._robot.set_error_led(LedState.OFF)

        self._robot.beep(1, 50)

        self._wifi_service.start()

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
        # self._voice.say_introduction()
        while True:
            if self._is_shutting_down_triggered():
                self._shut_down_core()
                break

            self._handle_bluetooth_events()

            wifi_service_event: Optional[int] = self._wifi_service.events.get()
            if wifi_service_event == WifiService.WIFI_SERVICE_FAILED_EVENT:
                self._robot.set_error_led(LedState.ON)

            if self._algorithm_manager.current_state.equals(ExecutionState.State.RUNNING):
                if not self._robot.data.lid_is_closed:
                    self._algorithm_manager.pause(LID_IS_OPENED_PAUSE_REASON)
                elif not self._robot.data.dust_box_present:
                    self._algorithm_manager.pause(DUST_BOX_OUT_PAUSE_REASON)

            self.scheduler.tick()

    def _handle_bluetooth_events(self) -> None:
        bl_button = self._robot.data.bluetooth_button
        if bl_button is ButtonState.CLICK and not self._bluetooth_service.is_alive():
            self._logger.info('Start Bluetooth service...')
            self._bluetooth_service.start()

        if bl_button is ButtonState.LONG_PRESS and not self._bluetooth_service.is_paring_process_enabled:
            if not self._bluetooth_service.is_alive():
                self._logger.info('Start Bluetooth service...')
                self._bluetooth_service.start()
            self._logger.info('Enable pairing...')
            self._bluetooth_service.enable_pairing()

        bluetooth_service_event: Optional[int] = self._bluetooth_service.events.get()
        if bluetooth_service_event in (BluetoothService.START_SERVICE_EVENT, BluetoothService.DISCONNECTED_EVENT,
                                       BluetoothService.FINISH_PAIRING_EVENT):
            self._robot.set_bluetooth_led_state(green=False, state=LedState.ON)
        elif bluetooth_service_event == BluetoothService.CONNECTED_EVENT:
            self._robot.set_bluetooth_led_state(green=True, state=LedState.ON)
        elif bluetooth_service_event == BluetoothService.START_PAIRING_EVENT:
            self._robot.set_bluetooth_led_state(green=True, state=LedState.BLINKING)
        elif bluetooth_service_event == BluetoothService.ERROR_OCCURRED_EVENT:
            self._robot.set_bluetooth_led_state(green=False, state=LedState.BLINKING)
            self._logger.error('Error occurred in Bluetooth Service')

    def _check_wifi_connection(self) -> None:
        self._robot.set_wifi_led(LedState.ON if self._os.get_ip_address() else LedState.OFF)

    def _is_shutting_down_triggered(self) -> bool:
        return self._robot.data.is_shut_down_button_triggered or self._operation_shut_down

    def _shut_down_core(self) -> None:
        self._logger.info('Preparing to shutdown')
        self._logger.info('Stop Wifi Service...')
        # self._wifi_endpoints_handler.stop() TODO
        self._logger.info('Stop Bluetooth Service...')
        # self._bluetooth_endpoints_handler.stop() TODO
        self._logger.info('Send signal to turn off the robot in 10 seconds to A1...')
        self._robot.set_shutting_down_led().expect()
        sleep(0.5)
        self._robot.set_timer_to_cut_off_power(15).expect()
        sleep(1)
        self._logger.info('Close A1 Connection...')
        # TODO
        self._logger.info('Perform shutdown the system...')
        self._os.shutdown()

import threading
from signal import pause

from bluetooth_service import BluetoothService
from hard_ware_interface import HardWareInterface
import logging

logging.basicConfig(level='DEBUG')


class Core(object):
    def __init__(self):
        self.bluetooth_service = BluetoothService()
        self.hardware_interface = HardWareInterface(callbacks={
            'start_bluetooth_service': self._on_start_bluetooth_service,
            'stop_bluetooth_service': self._on_stop_bluetooth_service
        })

    def _on_stop_bluetooth_service(self):
        print('Stop', threading.currentThread().getName())
        self.bluetooth_service.stop()

    def _on_start_bluetooth_service(self):
        print('Start', threading.currentThread().getName())
        self.bluetooth_service.start()

    def start(self):
        self.hardware_interface.start()


def main() -> None:
    core = Core()
    core.start()
    pause()


if __name__ == '__main__':
    main()

import logging
from typing import Dict
from gpiozero import LED, Button


# start_bluetooth_service
# stop_bluetooth_service

class HardWareInterface(object):
    def __init__(self, callbacks: Dict):
        self._callbacks = callbacks
        self._bl_status_led = LED('GPIO14')
        self._bl_button = Button('GPIO4', hold_time=3)
        self._is_bl_service_enabled = False

    def _on_bl_button_long_press(self):
        if self._is_bl_service_enabled:
            self._bl_status_led.off()
            stop_bluetooth_service = self._callbacks.get('stop_bluetooth_service')
            if stop_bluetooth_service:
                stop_bluetooth_service()
            self._is_bl_service_enabled = False
        else:
            self._bl_status_led.on()
            start_bluetooth_service = self._callbacks.get('start_bluetooth_service')
            if start_bluetooth_service:
                start_bluetooth_service()
            self._is_bl_service_enabled = True

    def _run(self):
        self._bl_button.when_held = self._on_bl_button_long_press

    def start(self):
        logging.info('Start Hardware interface...')
        self._run()
        # Thread(target=self._run, daemon=True).start()

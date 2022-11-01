from contextlib import contextmanager
from time import sleep

import pexpect

from utils.logger import Logger


class Bluetoothctl(object):
    def __init__(self, logger: Logger):
        self.child = None
        self._logger = logger

    @contextmanager
    def open(self):
        self.child = pexpect.spawn('bluetoothctl')
        try:
            yield self
        finally:
            self.child.close()

    def _send(self, command: str):
        self._logger.debug(f'[bluetoothctl] Send command: {command}')
        self.child.sendline(command)
        self.child.expect(["bluetooth", pexpect.EOF])
        response = self.child.readline().strip()
        self._logger.debug(f'[bluetoothctl] Received response: {response}')

    def agent_off(self):
        self._send('agent off')

    def power_on(self):
        self._send('power on')

    def power_off(self):
        self._send('power off')

    def discoverable_on(self):
        self._send('discoverable on')

    def pairable_on(self):
        self._send('pairable on')

    def agent_no_input_no_output(self):
        self._send('agent NoInputNoOutput')

    def default_agent(self):
        self._send('default-agent')

    def accept_incoming_pairing_request(self, timeout: int):
        self._logger.info('[bluetoothctl] Waiting for incoming pairing request to accept...')
        try:
            self.child.expect('.+pairing.+', timeout=timeout)
        except pexpect.exceptions.TIMEOUT:
            return
        self.child.sendline('yes')
        self._logger.info('[bluetoothctl] The pairing request is accepted')
        sleep(10)

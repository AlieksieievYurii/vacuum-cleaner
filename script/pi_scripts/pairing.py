from contextlib import contextmanager
from time import sleep

import pexpect


class Bluetoothctl(object):

    def __init__(self):
        self.child = None

    @contextmanager
    def open(self):
        self.child = pexpect.spawn('sudo bluetoothctl')
        try:
            yield self
        finally:
            self.child.close()

    def _send(self, command: str):
        self.child.sendline(command)
        self.child.expect(["bluetooth", pexpect.EOF])
        print(self.child.readline().strip().decode('utf-8'))

    def agent_off(self):
        self._send('agent off')

    def power_on(self):
        self._send('power on')

    def discoverable_on(self):
        self._send('discoverable on')

    def pairable_on(self):
        self._send('pairable on')

    def agent_no_input_no_output(self):
        self._send('agent NoInputNoOutput')

    def default_agent(self):
        self._send('default-agent')

    def accept_incoming_pairing_request(self):
        print('Waiting for incoming requests...')
        self.child.expect('.+pairing.+', timeout=3 * 60)
        self.child.sendline('yes')
        print('Connected!')
        sleep(10)


if __name__ == '__main__':
    with Bluetoothctl().open() as ctl:
        ctl.agent_off()
        ctl.power_on()
        ctl.discoverable_on()
        ctl.pairable_on()
        ctl.agent_no_input_no_output()
        ctl.default_agent()
        ctl.accept_incoming_pairing_request()

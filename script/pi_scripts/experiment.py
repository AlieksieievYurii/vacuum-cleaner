import logging

from service.communitator.bluetooth_communicator import BluetoothCommunicator
from service.handlers import GetWifiSettingsRequestHandler, SetWifiSettingsRequestHandler
from service.service import Service

logging.basicConfig(level='DEBUG')

if __name__ == '__main__':
    print('Start...')
    dummy_communicator = BluetoothCommunicator()
    service = Service(communicator=dummy_communicator, handlers=[
        GetWifiSettingsRequestHandler,
        SetWifiSettingsRequestHandler
    ])

    service.start()

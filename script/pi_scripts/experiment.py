import threading
import logging

from service.communitator.bluetooth_communicator import BluetoothCommunicator
from service.models import ResponseModel, RequestModel, RequestHandler, Field
from service.service import Service

logging.basicConfig(level='DEBUG')


# noinspection PyCallByClass
class TestRequestModel(RequestModel):
    test_variable_one = Field(name="var_one", type=str, is_required=True)
    test_variable_two = Field(name="var_two", type=int)


class ClientNameResponse(ResponseModel):
    server_name = Field(name="server_name", type=str, is_required=True)
    client_name = Field(name="client_name", type=str, is_required=True)
    test_name = Field(name="test_name", type=str, is_required=False)


# noinspection PyCallByClass
class InfoResponseModel(ResponseModel):
    user_name = Field(name='user_name', type=str, is_required=True)


class GetAllInfoRequestHandler(RequestHandler):
    request = "get_all_info"
    request_model = None
    response_model = InfoResponseModel

    def __init__(self, test_input):
        self.test_input = test_input

    def handle(self, request, data):
        print(f'Is called from {threading.currentThread().getName()}')
        return InfoResponseModel(user_name='yurii')


if __name__ == '__main__':
    print('Start...')
    dummy_communicator = BluetoothCommunicator()
    service = Service(communicator=dummy_communicator, handlers=[
        GetAllInfoRequestHandler("Test")
    ])

    service.start()
    # a = service.send("validation_request", ClientNameResponse, parameters={'server_name': 'Raspberry Pi Zero'})
    # print(a.server_name)
    # print(a.client_name)
    service._communicator.send({'dupa': '123'})

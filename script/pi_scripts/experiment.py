import threading

from service.communitator.bluetooth_communicator import BluetoothCommunicator
from service.models import ResponseModel, RequestModel, RequestHandler
from service.service import Service


# noinspection PyCallByClass
class TestRequestModel(RequestModel):
    test_variable_one = RequestModel.Field(name="var_one", type=str, is_required=True)
    test_variable_two = RequestModel.Field(name="var_two", type=int)


# noinspection PyCallByClass
class InfoResponseModel(ResponseModel):
    user_name = ResponseModel.Field(name='user_name', type=str, is_required=True)


class GetAllInfoRequestHandler(RequestHandler):
    request = "get_all_info"
    request_model = None
    response_model = InfoResponseModel

    def handle(self, request, data):
        print(f'Is called from {threading.currentThread().getName()}')
        return InfoResponseModel(user_name='yurii')


if __name__ == '__main__':
    print('Start...')
    dummy_communicator = BluetoothCommunicator()
    service = Service(communicator=dummy_communicator, handlers=[
        GetAllInfoRequestHandler
    ])
    service.start()

#
# import json
#
# from json import JSONDecodeError
# from threading import Thread
# from typing import Dict, List
#
# # import bluetooth
#
# """
# Request: {
#             request: str
#             body: Json
#         }
#
#
# """
#
#
# class Connector(object):
#     uuid = "94f39d29-7d6d-437d-973b-fba39e49d4ee"
#
#     def __init__(self):
#         self.server_sock = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
#         self.server_sock.bind(("", bluetooth.PORT_ANY))
#         self.server_sock.listen(1)
#         self.client_sock = None
#
#     def connect(self):
#         bluetooth.advertise_service(self.server_sock, "SampleServer", service_id=self.uuid,
#                                     service_classes=[self.uuid, bluetooth.SERIAL_PORT_CLASS],
#                                     profiles=[bluetooth.SERIAL_PORT_PROFILE])
#         print("Waiting for connection on RFCOMM channel", self.server_sock.getsockname()[1])
#         client_sock, client_info = self.server_sock.accept()
#         print("Accepted connection from", client_info)
#         self.client_sock = client_sock
#
#     def start_listening(self, callback):
#         def inner_callback():
#             while True:
#                 data = self.client_sock.recv(1024)
#                 if not data:
#                     break
#                 callback(data.decode().rstrip())
#
#         Thread(target=inner_callback).start()
#
#
#
#
# class BluetoothEngine(object):
#     def __init__(self, config: List[RequestHandler]):
#         self._config = config
#         self._connector = Connector()
#
#     def _execute_request_handler(self, request_handler: RequestHandler, json_data: Dict):
#         request_data = request_handler.request_model.parse(json_data) if request_handler.request_model else None
#
#     def _handle_request(self, request: str, json_data: Dict):
#         for request_handler in self._config:
#             if request_handler.request == request:
#                 self._execute_request_handler(request_handler, json_data)
#                 break
#         else:
#             raise Exception(f'Request "{request}" is not found')
#
#     def _handle_input(self, text: str):
#         try:
#             json_response = json.loads(text)
#         except JSONDecodeError:
#             pass
#         else:
#             self._handle_request(json_response.pop('request'), json_response)
#
#     def start(self):
#         self._connector.connect()
#         self._connector.start_listening(callback=self._handle_input)
#
#
# if __name__ == '__main__':
#     a = TestRequestModel
#
#     GetAllInfoRequestHandler.request_model.parse({'var_one': 'a', 'var_two':  12})
#
#
# def main() -> None:
#     a = BluetoothEngine(config=[
#         GetAllInfoRequestHandler()
#     ])
#     a.start()
#
# # if __name__ == '__main__':
# #     main()

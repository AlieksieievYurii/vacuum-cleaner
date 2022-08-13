from utils.request_handler.handler import RequestHandlerService
from wifi.comunicator import WifiCommunicator
from wifi.endpoints.hello_world import HelloWorldRequest
from utils.logger import wifi_module_logger


def main():
    wifi_communicator = WifiCommunicator()
    wifi_request_handler_service = RequestHandlerService(wifi_communicator, wifi_module_logger)
    wifi_request_handler_service.register(HelloWorldRequest())

    wifi_communicator.accept_connection()
    wifi_request_handler_service.start()

    while True:
        if wifi_request_handler_service.is_connection_closed:
            wifi_communicator.accept_connection()
            wifi_request_handler_service.start()


if __name__ == '__main__':
    main()

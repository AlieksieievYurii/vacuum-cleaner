import inspect
import time
from dataclasses import asdict
from threading import Thread
from typing import List, Type, Optional, Union, Dict

from service.exceptions import RequestHandlerIsNotRegistered, RequestDataIsNotFound, NoRequiredResponse, \
    InvalidRequest, ServiceException
from service.communitator.communicator import Communicator
from service.models import RequestHandler, RequestModel, ResponseModel, Request, Response, Packet, \
    PacketType, Status, Field
import queue
import logging


class Service(object):
    def __init__(self, communicator: Communicator, handlers: List[Union[RequestHandler, Type[RequestHandler]]]):
        self._communicator = communicator
        self._handlers = handlers
        self._queue = queue.Queue()
        self._response_row: List[Response] = []

    def start(self) -> None:
        """
        Blocking method! Starts listening Communicator as well as starts executing Queue with Requests

        :return: None
        """
        self._communicator.listen_output(call_back=self._communicator_callback)

        def _fun():
            self._start_handling_queue()

        Thread(target=_fun, daemon=False).start()

    def send(self, request_name: str, response_model: Type[ResponseModel], parameters: Optional[Dict] = None):
        request = Request(
            request_name=request_name,
            request_id=round(time.time() * 1000),
            parameters=parameters
        )
        self._send_request(request)
        return self._await_for_response(request, response_model)

    def _await_for_response(self, request: Request, response_model: Type[ResponseModel], timeout: int = 5000):
        while True:
            for response in self._response_row:
                if response.request_id == request.request_id and response.request_name == request.request_name:
                    # TODO handle bad request and error
                    return Field.parse_from_dict(response_model, response.response)

    def _start_handling_queue(self) -> None:
        """
        Blocking method that handles the queue where are located RequestHandlers

        :return: None
        """
        while True:
            request = self._queue.get()
            request()

    def _communicator_callback(self, data: str) -> None:
        """
        This method is called by the Communicator.

        :param data: Data from the communicator
        :return: None
        """
        logging.debug(f'Data received: {data}')
        try:
            packet = Packet.parse(data)
        except InvalidRequest as error:
            print(f'Cannot parse packet! Error: {error}')
        else:
            if packet.type == PacketType.REQUEST:
                request = Request.parse(packet.content)
                self._handle_packet(request)
            elif packet.type == PacketType.RESPONSE:
                self._add_response_to_row(packet.content)

    def _add_response_to_row(self, request: Dict):
        request = Response.parse(request)
        if len(self._response_row) >= 5:
            self._response_row.pop(0)

        self._response_row.append(request)

    def _handle_packet(self, request: Request):
        try:
            handler_request: Union[RequestHandler, Type[RequestHandler]] = self._find_request_handler(
                request.request_name)
            request_model_data: Optional[RequestModel] = self._get_request_model_data(handler_request, request)
        except ServiceException as error:
            self._send_error(request, str(error))
        else:
            self._queue.put(lambda: self._execute_request(handler_request, request, request_model_data))

    def _find_request_handler(self, request_name: str) -> Type[RequestHandler]:
        for handler in self._handlers:
            if handler.request == request_name:
                return handler
        else:
            raise RequestHandlerIsNotRegistered(request_name)

    @staticmethod
    def _get_request_model_data(handler_request: Type[RequestHandler], request: Request) -> Optional[RequestModel]:
        if handler_request.request_model:
            if not request.parameters:
                raise RequestDataIsNotFound()
            return Field.parse_from_dict(handler_request.request_model, request.parameters)
        else:
            return None

    def _execute_request(self, handler_request: Union[RequestHandler, Type[RequestHandler]], request: Request,
                         request_model: Optional[RequestModel]):
        handler_request_instance = handler_request() if inspect.isclass(handler_request) else handler_request
        response: Optional[ResponseModel] = handler_request_instance.handle(request, request_model)

        if handler_request.response_model and (not response or not isinstance(response, ResponseModel)):
            raise NoRequiredResponse(handler_request.response_model, response)

        self._send_response(request, response)

    def _send_request(self, request: Request) -> None:
        packet = Packet(PacketType.REQUEST, asdict(request))
        self._communicator.send(asdict(packet))

    def _send_error(self, request: Request, error_message: str):
        response = Response(
            request_name=request.request_name,
            request_id=request.request_id,
            status=Status.ERROR,
            error_message=error_message,
            response=None
        )
        self._communicator.send(asdict(Packet(PacketType.RESPONSE, asdict(response))))

    def _send_response(self, request: Request, response: ResponseModel):
        response = Response(
            request_name=request.request_name,
            request_id=request.request_id,
            response=Field.to_data(response),
            status=Status.OK,
            error_message=None
        )
        self._communicator.send(asdict(Packet(PacketType.RESPONSE, asdict(response))))

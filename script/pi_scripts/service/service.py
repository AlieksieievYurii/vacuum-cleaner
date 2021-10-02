from dataclasses import asdict
from typing import List, Type, Optional

from service.exceptions import RequestHandlerIsNotRegistered, RequestDataIsNotFound, NoRequiredResponse, \
    InvalidRequest, ServiceException
from service.communitator.communicator import Communicator
from service.models import RequestHandler, RequestModel, ResponseModel, Request, Response, Packet, \
    PacketType, Status
import queue


class Service(object):
    def __init__(self, communicator: Communicator, handlers: List[RequestHandler]):
        self._communicator = communicator
        self._handlers = handlers
        self._queue = queue.Queue()

    def start(self) -> None:
        """
        Blocking method! Starts listening Communicator as well as starts executing Queue with Requests

        :return: None
        """
        self._communicator.listen_output(call_back=self._communicator_callback)
        self._start_handling_queue()

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
        try:
            packet = Packet.parse(data)
        except InvalidRequest as error:
            print(f'Cannot parse packet! Error: {error}')
        else:
            if packet.type == PacketType.REQUEST:
                request = Request.parse(packet.content)
                self._handle_packet(request)
            elif packet.type == PacketType.RESPONSE:
                pass

    def _handle_packet(self, request: Request):
        try:
            handler_request: Type[RequestHandler] = self._find_request_handler(request.request_name)
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
            return handler_request.request_model.parse(request.parameters)
        else:
            return None

    def _execute_request(self, handler_request: Type[RequestHandler], request: Request,
                         request_model: Optional[RequestModel]):
        response: Optional[ResponseModel] = handler_request().handle(request, request_model)

        if handler_request.response_model and (not response or not isinstance(response, ResponseModel)):
            raise NoRequiredResponse(handler_request.response_model, response)

        self._send_response(request, response)

    def _send_error(self, request: Request, error_message: str):
        response = Response(
            request_name=request.request_name,
            request_id=request.request_id,
            status=Status.ERROR,
            error_message=error_message,
            response=None
        )
        self._communicator.send(asdict(Packet(PacketType.RESPONSE, asdict(response))))

    def _send_response(self, request: Request, response_model: ResponseModel):
        response = Response(
            request_name=request.request_name,
            request_id=request.request_id,
            response=response_model.data,
            status=Status.OK,
            error_message=None
        )
        self._communicator.send(asdict(Packet(PacketType.RESPONSE, asdict(response))))

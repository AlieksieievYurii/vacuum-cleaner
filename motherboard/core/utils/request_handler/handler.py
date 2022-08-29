import dataclasses
import json
import queue
import threading
from json import JSONDecodeError
from typing import Optional, List, Dict

from utils.communicator import Communicator, CommunicatorConnectionClosed
from utils.request_handler.exceptions import ParsingRequestErrorException, RequiredFieldIsNotFound
from utils.request_handler.models import Request, RequestHandler, Field, Response, Status, AttributeHolder


class RequestHandlerService(object):
    def __init__(self, communicator: Communicator, logger):
        self._communicator = communicator
        self._request_handlers: List[RequestHandler] = []
        self._queue = queue.Queue()
        self._logger = logger
        self._reader_thread = threading.Thread(target=self._keep_reading_data, name="DataReader")
        self.is_connection_closed = False

    def register(self, request_handler: RequestHandler):
        """
        Registers given class as endpoint handler. The request_handler must extend abstract RequestHandler class.

        :param request_handler: instance of the class extending abstract RequestHandler class
        :return: None
        """
        # TODO Handle the case when trying to register with already append endpoint
        self._request_handlers.append(request_handler)

    def start_handling(self) -> None:
        """
        Endless Blocking function that handles incoming request in the thread of the caller.
        Accepting incoming request are done in separated thread. If connection is lost/closed, the function is released.

        :return: None
        """

        self.is_connection_closed = False
        self._reader_thread.start()
        self._keep_handling_incoming_requests()

    def _keep_reading_data(self) -> None:
        """
        Endless blocking function that receives Requests, parses it and adds to the queue

        :return: None
        """

        while True:
            if self.is_connection_closed:
                break
            try:
                data = self._communicator.read()
            except CommunicatorConnectionClosed:
                self.is_connection_closed = True
            else:
                self._logger.debug(f'Received data: {data}')
                request = self._parse_request(data)
                self._queue.put(request)

    @staticmethod
    def _parse_request(data: str) -> Request:
        """
        Tries to parse request, otherwise ParsingRequestErrorException is raised

        :param data: string of data containing correct json
        :return: instance of Request class
        """

        try:
            request = json.loads(data)
        except JSONDecodeError:
            raise ParsingRequestErrorException(data)
        else:
            return Request.parse(request)

    def _keep_handling_incoming_requests(self) -> None:
        """
        Endless blocking function that checks the queue for new Requests and execute them. If not found, error response
        (BAD_REQUEST) is send

        :return: None
        """

        while True:
            if self.is_connection_closed:
                break
            if self._queue.qsize():
                request: Request = self._queue.get()
                request_handler = self._find_corresponding_handler(request)
                if request_handler:
                    self._perform_request(request, request_handler)
                else:
                    self._send_error(request, 'Wrong endpoint', True)

    def _perform_request(self, request: Request, request_handler: RequestHandler) -> None:
        if request_handler.request_model:
            attrs = self._parse_fields(request_handler.request_model, request.parameters)
        else:
            attrs = AttributeHolder()
            if request.parameters:
                self._send_error(request, 'This endpoint does not require parameters')

        try:
            response = request_handler.perform(request, attrs)
        except Exception as error:
            self._send_error(request, str(error))
        else:
            self._send_successful_result(request, response)

    @staticmethod
    def _parse_fields(obj, data: Dict) -> AttributeHolder:
        """
        Uses given obj class/instance to fetch Fields and looks for the parameters in data.
        Then the following dict can be parsed and converted into object with defiled Fields.
        Example:
            >>> class SomeClass(object):
            >>>     some_var = Field(name='someVar', type=str, is_required=True)
            >>> data = {'someVar': 'some value'}
            >> obj = RequestHandlerService._parse_fields(SomeClass, data)
            >> obj.some_var # some value

        :param obj: class/instance to scan for fields
        :param data: data with the parameters
        :return: AttributeHolder -> namespace containing data
        """

        fields = {var: val for var, val in obj.__dict__.items() if isinstance(val, Field)}
        attrs = AttributeHolder()

        for field_name, field in fields.items():
            if field.is_required and field_name not in data:
                raise RequiredFieldIsNotFound(field_name, data)
            setattr(attrs, field.name, field.type(data.get(field_name)))
        return attrs

    def _find_corresponding_handler(self, request: Request) -> Optional[RequestHandler]:
        for handler in self._request_handlers:
            if handler.endpoint == request.endpoint:
                return handler

    def _send_successful_result(self, request: Request, response_data) -> None:
        response = Response(endpoint=request.endpoint,
                            request_id=request.request_id,
                            status=Status.OK,
                            data=response_data)
        response_json = json.dumps(dataclasses.asdict(response))
        self._logger.debug(f'Response Data: {response_json}')
        self._communicator.send(response_json)

    def _send_error(self, request: Request, error: str, bad_request: bool = False) -> None:
        response = Response(endpoint=request.endpoint,
                            request_id=request.request_id,
                            status=Status.BAD_REQUEST if bad_request else Status.ERROR,
                            error_message=error)
        error_response_json = json.dumps(dataclasses.asdict(response))
        self._logger.error(f'Error Response Data: {error_response_json}')
        self._communicator.send(error_response_json)

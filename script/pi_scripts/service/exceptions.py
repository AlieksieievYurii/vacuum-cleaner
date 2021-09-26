from typing import Dict, Type, Optional


class ServiceException(Exception):
    pass


class InvalidRequest(ServiceException):
    def __init__(self, request: Optional[Dict] = None, request_text: Optional[str] = None):
        if request:
            exception_message = f'Request is invalid: {request}'
        elif request_text:
            exception_message = f'Request text is invalid: {request_text}'
        else:
            exception_message = 'Unknown error'
        super().__init__(exception_message)


class RequestHandlerIsNotRegistered(ServiceException):
    def __init__(self, request: str):
        super().__init__(f'Request handler is not registered: {request}')


class RequestDataIsNotFound(ServiceException):
    def __init__(self):
        super().__init__('Request Data is not found in the request')


class RequiredFieldIsNotFound(ServiceException):
    def __init__(self, field, request_data: Dict):
        super().__init__(f'Required field: {field} in not found in {request_data}')


class NoRequiredResponse(ServiceException):
    def __init__(self, required_response_model, current_response):
        super().__init__(f'Expected response model: {required_response_model}. Actually response: {current_response}')


class NoRequiredVariable(ServiceException):
    pass

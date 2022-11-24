from typing import Dict


class RequestHandlerException(Exception):
    """Represents basic exception for Request handler"""
    pass


class ParsingRequestErrorException(RequestHandlerException):
    """
    Exception representing failure parsing of request
    """

    def __init__(self, data: str):
        super().__init__(f'Can not parse the following: {data}')


class InvalidRequestData(RequestHandlerException):
    """
    Exception representing the fail of Request Data parsing
    """
    pass


class RequiredFieldIsNotFound(RequestHandlerException):
    """
    Exception is associated with Field class and represents error when required filed is not found in the request
    """

    def __init__(self, field, request_data: Dict):
        super().__init__(f'Required field: {field} in not found in {request_data}')

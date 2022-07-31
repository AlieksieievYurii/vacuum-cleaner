class A1Exception(Exception):
    pass


class CannotParse(A1Exception):
    pass


class TimeoutException(A1Exception):
    pass


class InstructionTimeout(TimeoutException):
    def __init__(self, instruction_id: int, request_id: int, timeout: int):
        super().__init__(f"Instruction id: {instruction_id:x}, Request ID: {request_id:x}, Timeout: {timeout}")

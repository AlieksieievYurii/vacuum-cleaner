class A1Exception(Exception):
    pass


class CannotParse(A1Exception):
    pass


class TimeoutException(A1Exception):
    pass


class InstructionTimeout(TimeoutException):
    def __init__(self, request, timeout: int):
        super().__init__(f"Instruction id: {request.instruction_id:x}, Request ID: {request.id:x}, Timeout: {timeout}")


class InstructionFailureException(A1Exception):
    def __init__(self, request, error_code: int):
        super().__init__(f'Instruction failed. Instruction id: {request.instruction_id:x}, '
                         f'Request ID: {request.id:x}, Error Code: {error_code}')

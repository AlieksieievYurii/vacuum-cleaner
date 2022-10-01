import time
from typing import Any


def get_typed_arg(name: str, t: Any, kwargs: dict) -> Any:
    """
    Returns value of the given key of the kwargs. ALso it checks if the value exists and its type

    :param name: key
    :param t: type
    :param kwargs: dict
    :return: typed value
    """

    value = kwargs.get(name)
    if not value:
        raise ValueError(f'Can not fetch "{name}" key!')
    elif not isinstance(value, t):
        raise ValueError(f'Wrong type of "{name}"!')
    return value


def millis() -> int:
    """
    Returns current time in millis

    :return: current time in millis
    """

    return round(time.time() * 1000)


def constrain_number(value: int, min_value: int, max_value: int) -> int:
    """
    Constrains given integer value with minimum value and maximum.

    :param value: integer number to consrain
    :param min_value: minimum value including
    :param max_value: maximum value including
    :return: constrained integer value
    """

    return max(min(max_value, value), min_value)


def boolean(value) -> bool:
    """
    Converts given value[string or boolean] to boolean value.

    :param value: string containing true or false. Also can be boolean directly
    :return: converted value
    """

    if isinstance(value, bool):
        return value
    elif isinstance(value, str):
        if value.lower() == 'false':
            return False
        elif value.lower() == 'true':
            return True
        else:
            raise ValueError(f'Cannot convert string value "{value}" to Boolean')
    else:
        raise ValueError(f'Cannot "{value}" to Boolean')

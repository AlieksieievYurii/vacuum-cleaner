import time


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

import time


def millis() -> int:
    return round(time.time() * 1000)


def constrain_number(value: int, min_value: int, max_value: int) -> int:
    return max(min(max_value, value), min_value)


def boolean(value) -> bool:
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

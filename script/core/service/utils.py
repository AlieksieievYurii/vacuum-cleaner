import time


def get_time_in_millis() -> int:
    return round(time.time() * 1000)
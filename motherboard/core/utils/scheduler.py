from dataclasses import dataclass
from typing import Callable, List

from utils.utils import millis


@dataclass
class QueueItem(object):
    interval: int
    fun: Callable

    last_call_time: int = 0


class Scheduler(object):
    def __init__(self):
        self._queue: List[QueueItem] = []

    def register(self, interval: int, fun: Callable):
        self._queue.append(QueueItem(interval, fun))

    def tick(self):
        for item in self._queue:
            if millis() - item.last_call_time >= item.interval:
                item.fun()
                item.last_call_time = millis()

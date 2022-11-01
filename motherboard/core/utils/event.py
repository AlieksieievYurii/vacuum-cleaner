from typing import Set, Optional


class Eventer(object):
    def __init__(self):
        super().__init__()
        self._events: Set[int] = set()

    def emit(self, event: int) -> None:
        self._events.add(event)

    def get(self) -> Optional[int]:
        try:
            return self._events.pop()
        except KeyError:
            return None

import threading
from typing import Optional


class WorkerThread(threading.Thread):
    """
    Thread which can keep raised exception that was raised in the thread
    """

    def __init__(self, *args, **kwargs):
        self._error_exception: Exception = None
        super().__init__(*args, **kwargs)

    def run(self) -> None:
        try:
            super().run()
        except Exception as error:
            self._error_exception = error

    @property
    def is_failed(self) -> Optional[Exception]:
        """
        Returns Exception is it has been raised. Otherwise None

        :return: Exception or None
        """

        if not self.is_alive() and self._error_exception:
            return self._error_exception

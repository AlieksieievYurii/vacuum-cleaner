import abc
from abc import ABC


class Voice(ABC):
    @abc.abstractmethod
    def say_introduction(self):
        pass

    @abc.abstractmethod
    def say_something_is_wrong(self):
        pass

    @abc.abstractmethod
    def say_lid_is_opened(self):
        pass

    @abc.abstractmethod
    def say_dust_box_is_out(self):
        pass

    @abc.abstractmethod
    def say_start_cleaning(self):
        pass

    @abc.abstractmethod
    def say_cleaning_is_finished(self):
        pass

    @abc.abstractmethod
    def say_goodbye(self):
        pass

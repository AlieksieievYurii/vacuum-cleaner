from pathlib import Path

from utils.os import OperationSystem
from utils.speetch.voice import Voice


class RudeMaximVoice(Voice):
    def __init__(self, os: OperationSystem):
        self._os = os
        self._res_dir = Path(__file__).parent / 'res' / 'rude_maxim'

    def say_introduction(self):
        self._os.play_sound(self._res_dir.joinpath('introduction_1.mp3'))

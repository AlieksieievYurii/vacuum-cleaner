import random
from pathlib import Path

from utils.os import OperationSystem
from utils.speetch.voice import Voice


class RudeMaximVoice(Voice):
    def __init__(self, os: OperationSystem):
        self._os = os
        self._res_dir = Path(__file__).parent / 'res' / 'rude_maxim'

    def say_introduction(self):
        random_number = random.randint(1, 3)
        self._os.play_sound(self._res_dir / f'introduction_{random_number}.mp3')

    def say_something_is_wrong(self):
        self._os.play_sound(self._res_dir / f'error.mp3')

    def say_lid_is_opened(self):
        self._os.play_sound(self._res_dir / f'lid_opened.mp3')

    def say_dust_box_is_out(self):
        self._os.play_sound(self._res_dir / f'dust_box_out.mp3')

    def say_start_cleaning(self):
        self._os.play_sound(self._res_dir / f'start_cleaning.mp3')

    def say_cleaning_is_finished(self):
        self._os.play_sound(self._res_dir / f'cleaning_finished.mp3')

    def say_goodbye(self):
        self._os.play_sound(self._res_dir / f'goodbye.mp3')

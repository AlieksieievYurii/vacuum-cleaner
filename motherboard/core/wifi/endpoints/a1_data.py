from dataclasses import dataclass

from a1.robot import Robot
from utils.request_handler.models import RequestHandler, Request, AttributeHolder


@dataclass
class A1DataResponseModel(object):
    el: bool  # Bumper Left End
    er: bool  # Bumper Right End
    eb: bool  # Box Presence End
    ec: bool  # Lid End
    lrs: int  # Left Range Sensor
    crs: int  # Center Range Sensor
    rrs: int  # Right Range Sensor
    bv: float  # Battery voltage
    bc: int  # Battery Capacity
    cs: int  # Charging State. NO CHARGING = 0, CHARGING = 1, CHARGED = 2


class GetA1DataRequestHandler(RequestHandler):
    endpoint = '/get-a1-data'
    request_model = None
    response_model = A1DataResponseModel

    def __init__(self, robot: Robot):
        self._robot = robot

    def perform(self, request: Request, data: AttributeHolder) -> A1DataResponseModel:
        return A1DataResponseModel(
            el=self._robot.data.end_left_trig,
            er=self._robot.data.end_right_trig,
            eb=self._robot.data.end_dust_box_trig,
            ec=self._robot.data.end_lid_trig,
            lrs=self._robot.data.rangefinder_left_value,
            crs=self._robot.data.rangefinder_center_value,
            rrs=self._robot.data.rangefinder_right_value,
            bv=self._robot.data.battery_voltage,
            bc=self._robot.data.battery_capacity,
            cs=self._robot.data.charging_state.value
        )

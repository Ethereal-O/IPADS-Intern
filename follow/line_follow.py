import numpy
from info.info_manager import info_manager


class Follower:
    def __init__(self):
        info_manager.set_is_running_inline(True)
        info_manager.set_angular_z(0)

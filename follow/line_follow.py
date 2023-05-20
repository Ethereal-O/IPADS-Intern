import numpy
from info.info_manager import info_manager_thread_local


class Follower:
    def __init__(self):
        info_manager_thread_local.info_manager.set_is_running_inline(True)
        info_manager_thread_local.info_manager.set_angular_z(0)

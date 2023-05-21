from info.info_manager import info_manager_thread_local
from config import config
import threading
import time


class odometry:
    def __init__(self):
        self.info_manager = info_manager_thread_local.info_manager
        self.odo_thread = threading.Thread(target=self.odometry_callback).start()

    def odometry_callback(self):
        while True:
            if self.info_manager.get_is_running():
                self.info_manager.set_mileage(self.info_manager.get_mileage(
                ) + config.SLEEP_TIME*self.info_manager.get_linear_x())
            time.sleep(0.1*config.SLEEP_TIME)

from info.info_manager import info_manager
from config import config
import threading
import time


class odometry:
    def __init__(self):
        self.odo_thread = threading.Thread(target=self.odometry_callback).start()

    def odometry_callback(self):
        while True:
            if info_manager.get_is_running():
                info_manager.set_mileage(info_manager.get_mileage(
                ) + config.SLEEP_TIME*info_manager.get_linear_x())
            time.sleep(config.SLEEP_TIME)

from info.info_manager import info_manager
import threading
import time
from config import config


def single_publish():
    while (True):
        info_manager.publish_twist()
        time.sleep(config.SLEEP_TIME)


def start_continue_publish():
    thread = threading.Thread(target=single_publish)
    thread.start()

from info.info_manager import info_manager
import threading
import time
from geometry_msgs.msg import Twist
import rospy
from config import config


class scheduler:
    def __init__(self):
        self.cmd_vel_pub = rospy.Publisher("~cmd_vel", Twist, queue_size=1)

    def publish_twist(self):
        if (info_manager.get_is_running()):
            self.cmd_vel_pub.publish(info_manager.get_twist())

    def single_publish(self):
        while (True):
            self.publish_twist()
            time.sleep(config.SLEEP_TIME)

    def start_continue_publish(self):
        thread = threading.Thread(target=self.single_publish)
        thread.start()

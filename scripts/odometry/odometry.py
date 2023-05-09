from nav_msgs.msg import Odometry
import rospy
from info.info_manager import info_manager


class odometry:
    def __init__(self):
        self.odo_sub = rospy.Subscriber(
            "/odom", Odometry, self.odometry_callback)

    def odometry_callback(self, msg):
        info_manager.set_mileage(msg.pose.pose.position.x)

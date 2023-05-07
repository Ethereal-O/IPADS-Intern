from geometry_msgs.msg import Twist
import rospy


class infoManager:
    def __init__(self):
        self.twist = Twist()
        self.cmd_vel_pub = rospy.Publisher("~cmd_vel", Twist, queue_size=1)

    def get_linear_x(self):
        return self.twist.linear.x

    def set_linear_x(self, val):
        self.twist.linear.x = val

    def get_angular_z(self):
        return self.twist.angular.z

    def set_angular_z(self, val):
        self.twist.angular.z = val

    def get_twist(self):
        return self.twist

    def set_twist(self, val):
        self.twist = val
        rospy.logwarn(str(self.twist.linear.x)+" "+str(self.twist.angular.z))

    def publish_twist(self):
        self.cmd_vel_pub.publish(self.twist)
        
    def stop(self):
        self.twist=Twist()
        self.publish_twist()


info_manager = infoManager()

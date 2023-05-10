from geometry_msgs.msg import Twist
import rospy
import json
from config import config


def log_printer(func):
    def new_func(_, val):
        rospy.logwarn(val)
        return func(_, val)
    return new_func


class infoManager:
    def __init__(self):
        # flag to check if can running inline
        self.is_running_inline = False
        # flag to check if is stopping
        self.is_running_outsidestop = True
        # flag to check if can running
        self.is_running = False
        # speed and angular control
        self.twist = Twist()
        # run mileage
        self.mileage = 0
        # passenger num
        self.passenger_num = 0
        # passenger capicity
        self.capicity = config.CAPICITY

    def set_is_running_inline(self, val):
        self.is_running_inline = val
        self.set_is_running(
            self.is_running_inline and self.is_running_outsidestop)

    def get_is_running_inline(self):
        return self.is_running_inline

    def set_is_running_outsidestop(self, val):
        self.is_running_outsidestop = val
        self.set_is_running(
            self.is_running_inline and self.is_running_outsidestop)

    def get_is_running_outsidestop(self):
        return self.is_running_outsidestop

    @log_printer
    def set_is_running(self, val):
        self.is_running = val

    def get_is_running(self):
        return self.is_running

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

    def get_mileage(self):
        return self.mileage

    # @log_printer
    def set_mileage(self, val):
        self.mileage = val

    def get_passenger_num(self):
        return self.passenger_num

    @log_printer
    def set_passenger_num(self, val):
        self.passenger_num = val

    def get_all(self):
        data = [{'mileage': self.mileage, 'speed': self.twist.linear.x,
                 'passenger_num': self.passenger_num}]

    def stop(self):
        self.twist = Twist()


info_manager = infoManager()

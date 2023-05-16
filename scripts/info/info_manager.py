from geometry_msgs.msg import Twist
import rospy
import json
from config import config
from helper.helper import helper
import time


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
        # car id
        self.id = config.CAR_ID

    def set_is_running_inline(self, val):
        self.is_running_inline = val
        self.set_is_running(
            self.is_running_inline and self.is_running_outsidestop)

    def get_is_running_inline(self):
        return self.is_running_inline

    @helper.log_printer
    def set_is_running_outsidestop(self, val):
        self.is_running_outsidestop = val
        self.set_is_running(
            self.is_running_inline and self.is_running_outsidestop)

    def get_is_running_outsidestop(self):
        return self.is_running_outsidestop

    # @helper.log_printer
    def set_is_running(self, val):
        self.is_running = val

    def get_is_running(self):
        return self.is_running

    def get_linear_x(self):
        return self.twist.linear.x

    # @helper.log_printer
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

    # @helper.log_printer
    def set_mileage(self, val):
        self.mileage = val

    def get_passenger_num(self):
        return self.passenger_num

    @helper.log_printer
    def set_passenger_num(self, val):
        self.passenger_num = val

    # @helper.log_printer
    def get_all(self):
        # data = [{'id': self.id, 'mileage': self.mileage, 'speed': self.twist.linear.x,
        #          'passenger_num': self.passenger_num}]
        data = str(int(config.TIME_SCALE*time.time()))+","+str(self.id)+","+str(int(config.SPEED_SCALE*self.twist.linear.x)) + \
            ","+str(int(0 if self.mileage < 0 else config.MILEAGE_SCALE*self.mileage)) + \
            ","+str(self.passenger_num)
        return data

    def stop(self):
        self.twist = Twist()


info_manager = infoManager()

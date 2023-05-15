from info.info_manager import info_manager
import threading
import time
from geometry_msgs.msg import Twist
import rospy
from config import config
# from webSocket import webSocket
from sockets import sockets
from rpc.rpc_client import rpc_client
import random


class scheduler:
    def __init__(self):
        self.cmd_vel_pub = rospy.Publisher("~cmd_vel", Twist, queue_size=1)
        # self.wsapp = webSocket.webSocket()
        self.tcp_client = sockets.sockets()
        self.rpc_client = rpc_client()
        self.have_stop = {}

    def publish_twist(self):
        twist = info_manager.get_twist()
        if not info_manager.get_is_running():
            twist = Twist()
        self.cmd_vel_pub.publish(twist)

    def single_publish(self):
        while (True):
            self.publish_twist()
            time.sleep(config.SLEEP_TIME)

    def start_continue_publish(self):
        threading.Thread(target=self.single_publish).start()

    def start_socket(self):
        # self.wsapp.start()
        self.tcp_client.start()
        pass

    def check_is_stop(self, mileage):
        mileage *= 10
        if (mileage % 10 <= 1 or mileage % 10 >= 9) and self.have_stop.get(mileage//10) == None:
            self.have_stop[mileage//10] = True
            return True
        return False

    def caculate_stop_num(self, mileage):
        return 0 if mileage < 0 else (mileage*10)//10

    def caculate_new_passenger(self, num):
        now_num = info_manager.get_passenger_num()
        get_off_num = min(
            int(random.random()*config.RANDOM_GETOFF_SCALE), now_num)
        new_num = min(config.CAPICITY, num+now_num-get_off_num)
        get_on_num = new_num-now_num+get_off_num
        max_change = max(get_off_num, get_on_num)
        return new_num, get_off_num, get_on_num, max_change/config.HUMAN_SPEED

    def check_and_report(self, stop_time):
        # if stop_time > config.MAXSTOPTIME:
        #     self.wsapp.send_message(stop_time)
        # if stop_time > config.MAX_STOP_TIME:
        #     self.tcp_client.send_message(stop_time)
        pass

    def resume_running(self):
        info_manager.set_is_running_outsidestop(True)

    def rpc_check_loop(self):
        while (True):
            if info_manager.get_is_running_outsidestop() and self.check_is_stop(info_manager.get_mileage()):
                # stop running
                info_manager.set_is_running_outsidestop(False)
                # get passenger
                want_num = self.rpc_client.get_passenger_num(
                    self.caculate_stop_num(info_manager.get_mileage()))
                # get real passenger
                now_num, get_off_num, get_on_num, stop_time = self.caculate_new_passenger(
                    want_num)
                # reply to edge server
                self.rpc_client.reduce_passenger_num(
                    self.caculate_stop_num(info_manager.get_mileage()), get_on_num)
                # check and report
                self.check_and_report(stop_time)
                # set passenger
                info_manager.set_passenger_num(now_num)
                # start timer to resume
                threading.Timer(stop_time,
                                self.resume_running).start()
            time.sleep(config.SLEEP_TIME)

    def start_rpc_checker(self):
        threading.Thread(target=self.rpc_check_loop).start()

    def start_all(self):
        # start changing paramter
        self.start_continue_publish()
        # start socket client
        self.start_socket()
        # start rpc client
        self.start_rpc_checker()

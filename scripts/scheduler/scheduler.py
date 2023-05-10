from info.info_manager import info_manager
import threading
import time
from geometry_msgs.msg import Twist
import rospy
from config import config
from webSocket import webSocket
from rpc.rpc_client import rpc_client


class scheduler:
    def __init__(self):
        self.cmd_vel_pub = rospy.Publisher("~cmd_vel", Twist, queue_size=1)
        # self.wsapp = webSocket.webSocket()
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

    def start_websocket(self):
        # self.wsapp.start()
        pass

    def check_is_stop(self, mileage):
        mileage *= 10
        if (mileage % 10 <= 1 or mileage % 10 >= 9) and self.have_stop.get(mileage//10) == None:
            self.have_stop[mileage//10] = True
            return True
        return False

    def caculate_stop_num(self, mileage):
        return (mileage*10)//10

    def caculate_stop_time(self, num):
        return num/3

    def resume_running(self):
        info_manager.set_is_running_outsidestop(True)

    def rpc_check_loop(self):
        while (True):
            if info_manager.get_is_running_outsidestop() and self.check_is_stop(info_manager.get_mileage()):
                info_manager.set_is_running_outsidestop(False)
                num = self.rpc_client.get_passenger_num(
                    self.caculate_stop_num(info_manager.get_mileage()))
                info_manager.set_passenger_num(num)
                # start timer to resume
                threading.Timer(self.caculate_stop_time(num),
                                self.resume_running).start()
            time.sleep(config.SLEEP_TIME)

    def start_rpc_checker(self):
        threading.Thread(target=self.rpc_check_loop).start()

    def start_all(self):
        # start changing paramter
        self.start_continue_publish()
        # start websocket client
        self.start_websocket()
        # start rpc client
        self.start_rpc_checker()

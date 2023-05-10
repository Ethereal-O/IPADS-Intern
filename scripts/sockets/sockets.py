import socket
import threading
import time
import rospy
from config import config
from info.info_manager import info_manager


class sockets:
    def __init__(self):
        self.tcp_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

    def __del__(self):
        self.tcp_socket.close()

    def start(self):
        self.tcp_socket.connect(
            (config.CLOUD_SERVER_IP, config.CLOUD_SERVE_PORT))
        threading.Thread(target=self.report).start()
        threading.Thread(target=self.recv_msg).start()

    def report(self):
        while (True):
            self.tcp_socket.send(str(info_manager.get_all()).encode("utf-8"))
            time.sleep(config.SLEEP_TIME)

    def recv_msg(self):
        while (True):
            recv_data = self.tcp_socket.recvfrom(config.BUF_SIZE)
            info_manager.set_linear_x(float(recv_data[0].decode('utf-8')))

    def send_message(self, message):
        self.tcp_socket.send(message)

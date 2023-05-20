import socket
import threading
import time
from config import config
from info.info_manager import info_manager_thread_local


class sockets:
    def __init__(self):
        self.info_manager = info_manager_thread_local.info_manager
        self.tcp_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

    def __del__(self):
        self.running = False
        self.fd.close()
        self.tcp_socket.close()

    def start(self):
        self.tcp_socket.connect(
            (config.CLOUD_SERVER_IP, config.CLOUD_SERVE_PORT))
        self.fd = self.tcp_socket.makefile("rw")
        self.running = True
        self.send_message(config.CAR_INDENTITY + str(self.info_manager.get_id()))
        threading.Thread(target=self.report).start()
        threading.Thread(target=self.recv_msg).start()

    def report(self):
        while self.running and self.info_manager.get_is_running():
            self.send_message(self.info_manager.get_all())
            time.sleep(config.SLEEP_TIME)

    def recv_msg(self):
        while self.running:
            recv_data = self.fd.readline()
            self.info_manager.set_linear_x(
                float(recv_data) / config.SPEED_SCALE)

    def send_message(self, message):
        self.fd.write(str(message) + config.END_FLAG)
        self.fd.flush()

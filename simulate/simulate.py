import socket
import threading
import time
from config import config
from info.info_manager import info_manager_thread_local


class simulate:
    def __init__(self):
        self.info_manager = info_manager_thread_local.info_manager
        self.tcp_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

    def __del__(self):
        self.running = False
        self.fd.close()
        self.tcp_socket.close()

    def start(self):
        self.tcp_socket.connect(
            (config.SIMULATE_SERVER_IP, config.SIMULATE_SERVE_PORT))
        self.fd = self.tcp_socket.makefile("rw")
        self.running = True
        threading.Thread(target=self.report).start()

    def report(self):
        while self.running:
            if self.info_manager.get_mileage() > 0:
                self.send_message(self.info_manager.get_simualte_all())
            time.sleep(config.SLEEP_TIME)

    def send_message(self, message):
        self.fd.write(str(message) + config.END_FLAG)
        self.fd.flush()

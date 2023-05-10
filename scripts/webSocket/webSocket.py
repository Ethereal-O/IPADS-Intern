from config import config
import websocket
import threading
import time
import rospy
from info.info_manager import info_manager


class webSocket:
    def __init__(self):
        self.wsapp = websocket.WebSocketApp(config.CLOUD_SERVER,
                                            on_open=self.on_open,
                                            on_message=self.on_message,
                                            on_close=self.on_close)

    def start(self):
        self.wsapp.run_forever()

    def report(self):
        while (True):
            self.send_message(info_manager.get_all())
            time.sleep(config.SLEEP_TIME)

    def on_open(self, wsapp):
        rospy.logwarn("on_open")
        threading.Thread(target=self.report).start()

    def on_message(self, wsapp, message):
        rospy.logwarn("on_message:" + str(message))
        info_manager.set_linear_x(message)

    def on_close(self, wsapp):
        rospy.logwarn("on_close")

    def send_message(self, message):
        self.wsapp.send(message)

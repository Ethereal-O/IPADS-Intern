from config import config
import websocket
import threading


class webSocket:
    def __init__(self):
        self.wsapp = websocket.WebSocketApp(config.SERVER,
                                            on_open=self.on_open,
                                            on_message=self.on_message,
                                            on_close=self.on_close)
        self.wsapp.run_forever()

    def on_open(self, wsapp):
        print("on_open")

    def on_message(self, wsapp, message):
        print("on_message:", message)

    def on_close(self, wsapp):
        print("on_close")

    def send_message(self, message):
        self.wsapp.send(message)


wsapp = None
# wsapp = webSocket()

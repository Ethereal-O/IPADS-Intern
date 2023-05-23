import threading
import car
from config import config


def start_simulate(times):
    for i in range(times):
        threading.Thread(target=car.start, args=(i,)).start()



if __name__ == "__main__":
    start_simulate(config.MAX_CAR_NUM)

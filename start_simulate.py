from multiprocessing import Process
import car


def start_simulate(times):
    process_list = []
    for i in range(times):
        p = Process(target=car.start, args=(i,))
        p.start()
        process_list.append(p)

    for p in process_list:
        p.join()


if __name__ == "__main__":
    start_simulate(10)

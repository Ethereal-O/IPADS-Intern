import matplotlib.pyplot as plt
import time

iter = []
times = []
passenger_nums = []
for i in range(10):
    passenger_nums.append([])
file_path = "./log/passengerNum.txt"
with open(file_path) as f:
    lines = f.readlines()
    for i in range(len(lines)-1):
        line = lines[i].split(",")
        iter.append(i)
        times.append(float(line[0]))
        for j in range(10):
            passenger_nums[j].append(float(line[j+2]))

plt.xlabel("iter")
plt.ylabel("passenger_num")
time.sleep(20)
for i in range(len(times)-1):
    begin_time = time.time()
    plt.title('Time :{}'.format((float(times[i])-float(times[0]))/1000000))
    for j in range(10):
        plt.plot(iter[:i], passenger_nums[j][:i],
                 label="station_"+str(j) if i == 0 else "")
    plt.legend()
    plt.pause((float(times[i+1])-float(times[i])) /
              1000000-time.time()+begin_time)
plt.ioff()


# plt.title('Time :{}'.format((float(times[i])-float(times[0]))/1000000))
# for j in range(10):
#     plt.xlabel("iter")
#     plt.ylabel("passenger_num")
#     plt.plot(iter[:i], passenger_nums[j][:i], label="station_"+str(j))
#     plt.legend()
# plt.show()

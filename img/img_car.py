import matplotlib.pyplot as plt

times = []
speeds = []
mileages = []
passenger_nums = []
for i in range(10):
    file_path = "./log/log"+str(i)+".txt"
    with open(file_path) as f:
        iter = []
        time = []
        speed = []
        mileage = []
        passenger_num = []
        lines = f.readlines()
        for i in range(len(lines)-1):
            line = lines[i].split(",")
            next_line = lines[i+1].split(",")
            iter.append(i)
            time.append(float(line[0]))
            speed.append(float(line[2]))
            mileage.append(float(line[3]))
            passenger_num.append(float(line[4]))
        times.append(time)
        speeds.append(speed)
        mileages.append(mileage)
        passenger_nums.append(passenger_num)

max_iter = max(max([len(i) for i in times]), max([len(i) for i in speeds]), max(
    [len(i) for i in mileages]), max([len(i) for i in passenger_nums]))

for i in range(10):
    if len(times[i]) < max_iter:
        for j in range(max_iter-len(times[i])):
            times[i].append(times[i][-1])
    if len(speeds[i]) < max_iter:
        for j in range(max_iter-len(speeds[i])):
            speeds[i].append(0)
    if len(mileages[i]) < max_iter:
        for j in range(max_iter-len(mileages[i])):
            mileages[i].append(mileages[i][-1])
    if len(passenger_nums[i]) < max_iter:
        for j in range(max_iter-len(passenger_nums[i])):
            passenger_nums[i].append(0)

# for i in range(max_iter-1):
#     plt.title('Time :{}'.format((float(times[9][i])-float(times[9][0]))/1000))
#     for j in range(10):
#         plt.subplot(131)
#         plt.plot(iter[:i], speeds[j][:i])
#         plt.subplot(132)
#         plt.plot(iter[:i], mileages[j][:i])
#         plt.subplot(133)
#         plt.plot(iter[:i], passenger_nums[j][:i])
#     plt.pause((float(times[9][i+1])-float(times[9][i]))/1000)  # 等待
# plt.ioff()

i = max_iter//2-2
plt.title('Time :{}'.format((float(times[9][i])-float(times[9][0]))/1000))
for j in range(10):
    plt.subplot(131)
    plt.plot(iter[:i], speeds[j][:i])
    plt.subplot(132)
    plt.plot(iter[:i], mileages[j][:i])
    plt.subplot(133)
    plt.plot(iter[:i], passenger_nums[j][:i])
plt.waitforbuttonpress()

import matplotlib.pyplot as plt
from scipy import stats

# read real world input data
file = open("PCS_TEST_DETERMINSTIC_S21617.txt", "r")
file.readline()
content = file.readlines()
line = [c.split() for c in content]

# put them in a list
interArrivalTime = [float(line[i+1][1]) - float(line[i][1]) for i in range(len(line) - 1)]
location = [int(line[i][2]) for i in range(len(line))]
callDuration = [float(line[i][3]) for i in range(len(line))]
speed = [float(line[i][4]) for i in range(len(line))]


# setting up scatter plots
interArrivalTime_X = [interArrivalTime[i] for i in range(len(interArrivalTime) - 1)]
interArrivalTime_Y = [interArrivalTime[i+1] for i in range(len(interArrivalTime) - 1)]
location_X = [location[i] for i in range(len(location) - 1)]
location_Y = [location[i+1] for i in range(len(location) - 1)]
callDuration_X = [callDuration[i] for i in range(len(callDuration) - 1)]
callDuration_Y = [callDuration[i+1] for i in range(len(callDuration) - 1)]
speed_X = [speed[i] for i in range(len(speed) - 1)]
speed_Y = [speed[i+1] for i in range(len(speed) - 1)]

# plot scartter plots
plt.scatter(interArrivalTime_X, interArrivalTime_Y, facecolor='red')
plt.xlabel('Inter-Arrival Time')
plt.show()
plt.scatter(location_X, location_Y, facecolor='red')
plt.xlabel('Base Station')
plt.show()
plt.scatter(callDuration_X, callDuration_Y, facecolor='red')
plt.xlabel('Call Duration')
plt.show()
plt.scatter(speed_X, speed_Y, facecolor='red')
plt.xlabel('Car\'s speed')
plt.show()

# histogram comparisons
plt.hist(interArrivalTime, 80, facecolor='red')
plt.xlabel('Inter-Arrival Time')
plt.show()
plt.hist(location, 10, facecolor='red')
plt.xlabel('Base Station')
plt.show()
plt.hist(callDuration, 100, facecolor='red')
plt.xlabel('Call Duration')
plt.show()
plt.hist(speed, 70, facecolor='red')
plt.xlabel('Car\'s speed')
plt.show()


param_interArrivalTime = stats.expon.fit(interArrivalTime)
param_location = stats.uniform.fit(location)
param_callDuration = stats.expon.fit(callDuration)
param_speed = stats.norm.fit(speed)

print "beta of inter arrival time: " + str(param_interArrivalTime[1])
print "theta of base station: " + str(param_location[1])
print "beta of call duration: " + str(param_callDuration[1])
print "mean, s.d. of car's speed: " + str(param_speed)

print "ks of inter arrival time: " + str(stats.kstest(interArrivalTime, lambda x: stats.expon.cdf(x, *param_interArrivalTime)))
print "ks of base station: " + str(stats.kstest(location, lambda x: stats.uniform.cdf(x, *param_location)))
print "ks of call duration: " + str(stats.kstest(callDuration, lambda x: stats.expon.cdf(x, *param_callDuration)))
print "ks of car's speed: " + str(stats.kstest(speed, lambda x: stats.norm.cdf(x, *param_speed)))

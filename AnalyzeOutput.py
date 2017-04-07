import matplotlib.pyplot as plt
import numpy as np
from scipy import stats
import math

def ProcessIndiStats(filename):
    file = open(filename, "r")

    # estimate lines per replication using first replication
    numLinesPerRep = 0
    while True:
        x = file.readline().split()
        if int(x[0]) == 0:
            numLinesPerRep += 1
        else: break

    # begin at start of file again so that actual analysis can happen
    file.seek(0, 0)

    # initialize all mean counters
    meanPercentageBlocked = []
    meanPercentageDropped = []
    meanTotalHandover = []
    for i in range(numLinesPerRep):
        meanPercentageBlocked.append(0.0)
        meanPercentageDropped.append(0.0)
        meanTotalHandover.append(0.0)

    # calculate mean here
    n = numLinesPerRep-1
    for k in range(100):
        for i in range(n):
            x = file.readline().split()
            if x[1] == "NaN" or x[2] == "NaN":
                x[1] = 0.0
                x[2] = 0.0
            meanPercentageBlocked[i] = (meanPercentageBlocked[i] * (k) + float(x[1]))/(k+1.0)
            meanPercentageDropped[i] = (meanPercentageDropped[i] * (k) + float(x[2]))/(k+1.0)
            meanTotalHandover[i] = (meanTotalHandover[i] * (k) + float(x[2]))/(k+1.0)

    # plot warm up graph
    plt.plot(meanPercentageBlocked)
    plt.xlabel(filename + ": Mean Percentage Blocked")
    plt.show()
    plt.plot(meanPercentageDropped)
    plt.xlabel(filename + ": Mean Percentage Dropped")
    plt.show()
    plt.plot(meanTotalHandover)
    plt.xlabel(filename + ": Mean Total Handover")
    plt.show()

def ProcessStats(filename, n, addString):
    file = open(filename, "r")
    content = file.readlines()
    line = [c.split() for c in content]

    # add blocked, dropped calls, and handover into respective list
    blockCalls = []
    droppedCalls = []
    handOver = []
    for l in line:
        if not l: break
        blockCalls.append(float(l[0]))
        droppedCalls.append(float(l[1]))
        handOver.append(float(l[2]))


    confidenceIntervals = confidenceRange(blockCalls, n)
    print "FCA Scheme: " + addString
    print "Pass Quality of Service Requirements -> blocked calls: " + confidenceTest(confidenceIntervals, 2) + "%"
    print "Percentage blocked calls mean confidence interval: " + confidenceMean(confidenceIntervals)
    confidenceIntervals = confidenceRange(droppedCalls, n)
    print "Pass Quality of Service Requirements -> dropped calls: " + confidenceTest(confidenceIntervals, 1) + "%"
    print "Percentage dropped calls mean confidence interval: " + confidenceMean(confidenceIntervals)
    print "Handover mean confidence interval: " + confidenceMean(confidenceRange(handOver, n)) + "\n"


def confidenceMean(confidenceIntervals):
    ci_low = []
    ci_high = []
    for ci in confidenceIntervals:
        ci_low.append(ci[0])
        ci_high.append(ci[1])

    return str([np.mean(ci_low), np.mean(ci_high)])

def confidenceRange(calls, limit):
    n = limit
    tmp = []
    toReturn = []
    for c in calls:
        n -= 1
        if n >= 0: tmp.append(c)
        else:
            # n = limit - 1 because c is added later in this block, so no need for n = limit
            n = limit - 1

            # calculate mean, sd, and confidence interval here
            mean = np.mean(tmp)
            sample_sd = np.std(tmp, ddof=1)
            d = 1.833 * math.sqrt(1.0/10.0) * sample_sd
            toReturn.append([mean - d, mean + d])

            # reset tmp list and append c in since we don't want to waste c
            tmp = []
            tmp.append(c)

    return toReturn

def confidenceTest(confidenceIntervals, tolerance):
    passed = 0.0
    total = 0.0
    for ci in confidenceIntervals:
        if ci[1] < tolerance:
            passed += 1.0
            total += 1.0

    return str(passed / total * 100)

ProcessIndiStats("stats_indi_no_res.txt")
ProcessIndiStats("stats_indi_one_res.txt")

# n is the number of elements per confidence interval
n = 10
ProcessStats("stats_no_res.txt", n, "No Reservation")
ProcessStats("stats_one_res.txt", n, "One Reservation")

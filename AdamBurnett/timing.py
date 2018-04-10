import os
import glob
import math
import datetime
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as pl
import numpy as np
import string
from datetime import datetime

controldata = []
testdata = []
controllog = "/home/adam/Thesis/logs/control.log"
testlog = "/home/adam/Thesis/logs/test.log"
startdatelog = "/home/adam/Thesis/logs/date.log"
startdatestr = ""

#Build timing plot
with open(controllog, "r") as ifile:
        controldata = map(float, ifile)

with open(testlog, "r") as ifile:
        testdata = map(float, ifile)

#Read in the start characterization date

startdatestr = open(startdatelog, 'r').read()
startdate = datetime.strptime(startdatestr, "%m.%d.%y\n")

controlcoeff = np.polyfit(timingaxis, controldata, 1)
testcoeff = np.polyfit(timingaxis, testdata, 1)

controlline = []
testline = []

pl.plot(testdata, 'bo', label='Test Region')
pl.plot(timingaxis, testline, linestyle='solid', color='blue')
pl.plot(controldata, 'ro', label='Control Region')
pl.plot(timingaxis, controlline, linestyle='solid', color='red')

ax = pl.subplots()

pl.legend(loc='upper center', bbox_to_anchor=(0.5,-0.1))
pl.axis([1, len(controldata), min(testdata)-.3, max(testdata)+.3])
pl.title('Characterization Results')
pl.xlabel('Time: Hours')
pl.ylabel('Percentage Change from Original')
pl.subplots_adjust(bottom=0.2)
pl.savefig('timing.png')

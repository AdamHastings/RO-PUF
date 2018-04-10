import pylab as pl
import glob
import os

from datetime import datetime

MOUNT = "/home/adam/mount/users/adamh5/logs/"
THESIS_DIR = "/home/adam/Thesis/"
LOGS_DIR = "logs/XMD/"
TIMING_LOGS = THESIS_DIR + 'logs/XMD/*.log'

#Get the most recent Thermistor log

def sort(logs):
	for i in range(1,len(logs)):
		for j in range(0,len(logs)-1):
			if (parseDate(logs[j]) > parseDate(logs[j+1])):
				swap(j, j+1, logs)
			
def swap(i, j, logs):
	temp = logs[i]
	logs[i] = logs[j]
	logs[j] = temp

def parseDate(logFile):
	date = datetime.strptime(logFile, THESIS_DIR + LOGS_DIR + 'output-%m.%d.%y-%H.%M.log')
	return date

def myFunction():
	logs = glob.glob(TIMING_LOGS)
	sort(logs)
	for log in logs:
		print log

myFunction()



#testplot, = pl.plot(controlregion, 'bo', label='Test Region')
#controlplot, = pl.plot(testregion, 'ro', label='Control Region')
#legend = pl.legend(loc='upper center', bbox_to_anchor=(0.5,-0.1))
#pl.title('Characterization Results')
#pl.xlabel('Time: Hours')
#pl.ylabel('Percentage Change from Original')
#pl.show()

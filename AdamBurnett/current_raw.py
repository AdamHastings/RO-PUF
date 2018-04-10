#Created by: Adam Burnett
#Email:adam.burnett@byu.edu
import os
import glob
import math
import datetime
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as pl
import numpy as np

#change these when switching machines
MOUNT = "/home/adam/mount/users/adamh5/logs/"
BOARD = "Burn"
THESIS_DIR = "/home/adam/Thesis/"
LOGS_DIR = "logs/XMD/"
TIMING_LOGS = THESIS_DIR + 'logs/XMD/*.log'
burnCurrentLogs = MOUNT + 'Burn_Current/*.log'
burnCurrentValues = []
burnCurrentTimes = []
#end

#Variable Declarations
diodeLogs = MOUNT + "Diode/*.log"
thermistorLogs = MOUNT + "Thermistor/*.log"
oneTwoLogs = MOUNT + BOARD + "-1.2V/*.log"
twoFiveLogs = MOUNT + BOARD + "-2.5V/*.log"
threeThreeLogs = MOUNT + BOARD + "-3.3V/*.log"
diodeValues = []
thermistorValues = []
oneTwoValues = []
twoFiveValues = []
threeThreeValues = []
diodeTimes = []
thermistorTimes = []
oneTwoTimes = []
twoFiveTimes = []
threeThreeTimes = []
error = 0
num_values = 7
testRegion = "Test Region"
controlRegion = "Control Region"
originalControlAverage = 0
originalTestAverage = 0
controlPercentages = []
testPercentages = []
timingTimes = []
timingTestTimes = []
controlPercentFromRec = 0
testPercentFromRec = 0

controlAverageArray = []
testAverageArray = []

		
#Get the date of the next to most recent log
logs = sorted(glob.glob(THESIS_DIR + "logs/XMD/*.log"))
logs.sort(key=os.path.getmtime)

if (len(logs) > 1):
        recent = logs[len(logs)-2]
        recentname = recent.split('-')
        recentdate = recentname[1].replace(".","/") + " " + recentname[2].replace(".log","").replace(".",":")

else:
        recentdate = ""
		

def getData( path, format, data = [], hours = [] ):
	"Builds average data and times arrays"
	logFiles = sorted(glob.glob(path))
	logFiles.sort(key=os.path.getmtime)

	if (len(logFiles) > 0):
		firstLog = logFiles[0]
		firstDateString = firstLog.split('-',1)[-1]
		firstDate = datetime.datetime.strptime(firstDateString, format) 
		
		for i,log in enumerate(logFiles):
			#find the average over an hour (log)
			temp = []
			currentDateString = log.split('-',1)[-1]
			currentDate = datetime.datetime.strptime(currentDateString, format)
			
			with open(log, "r") as ifile:
				temp = map(float, ifile)
			data.append(sum(temp) / len(temp))
			#add how many hours it has been onto the time array
			timeDifference = currentDate - firstDate
			hours.append(timeDifference.days*24+timeDifference.seconds/3600)
			if (i == len(logFiles) - 2):
				break
				
		
	else:
		print "No Log files present!"
		error = 1

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
	date = datetime.datetime.strptime(logFile, THESIS_DIR + LOGS_DIR + 'output-%m.%d.%y-%H.%M.log')
	return date

def getTimingData( controlData = [], testData = [], timingData = [], testTimingData = [] ):
	"Gets the control and test region data"
	global testPercentFromRec
	global controlPercentFromRec
	logFiles = sorted(glob.glob(TIMING_LOGS))

	sort(logFiles)

	tempTest = []
	tempControl = []
	originalDate = ""
	format = '%m.%d.%y-%H.%M.log'
	
	if (len(logFiles) > 0):
		originalLog = logFiles[0]
		firstDateString = originalLog.split('-',1)[-1]
		originalDate = datetime.datetime.strptime(firstDateString, format)
		os.system("sed -i 's/^M//' " + originalLog)
		#get original characterization values
		with open(originalLog, "r") as ifile:
			for line in ifile:
				if testRegion in line:
					for i in range(0,num_values):
						line = ifile.next()
						tempTest.append(int(line))
					ifile.next()
					for i in range(0,num_values):
						line = ifile.next()
						tempControl.append(int(line))
		ifile.close()
		
		#calculate original averages
		originalControlAverage = sum(tempControl) / len(tempControl)
		originalTestAverage = sum(tempTest) / len(tempTest)
	
		#controlData.append(originalControlAverage)
		#testData.append(originalTestAverage)		

		#get all values and fill array
		for log in logFiles:
			tempTest = []
			tempControl = []
			with open(log, "r") as ifile:
				for line in ifile:
					if testRegion in line:
						for i in range(0,num_values):
							line = ifile.next()
							tempTest.append(int(line))
						ifile.next()
						for i in range(0,num_values):
							line = ifile.next()
							tempControl.append(int(line))
			controlAverage = sum(tempControl) / len(tempControl)
			testAverage = sum(tempTest) / len(tempTest)
			
			controlData.append(controlAverage)

			flag = 1
			if (len(testData) > 0):
				if not(100*(testData[len(testData)-1] - testAverage)/float(testData[len(testData)-1]) < -0.013):
					#print "%.7f" % float(100*(testData[len(testData)-1] - testAverage)/float(testData[len(testData)-1]))
					testData.append(testAverage)
				else:
					flag = 0
			else:
				testData.append(testAverage)
			
			
			testPercentFromOrig = 100*(originalTestAverage - testAverage)/float(originalTestAverage)
			controlPercentFromOrig = 100*(originalControlAverage - controlAverage)/float(originalControlAverage)
			
			#find the date
			currentDateString = log.split('-',1)[-1]
			currentDate = datetime.datetime.strptime(currentDateString, format)
			#find the time difference in hours and append
			timeDifference = currentDate - originalDate
			
			timingData.append(timeDifference.days*24+timeDifference.seconds/3600)
			
			if (flag):
				testTimingData.append(timeDifference.days*24+timeDifference.seconds/3600)
		
		#compare recent
		if (len(testAverageArray) > 1):
			newestCtrl = controlAverageArray[len(controlAverageArray)-1]
			recentCtrl = controlAverageArray[len(controlAverageArray)-2]
			newestTest = testAverageArray[len(testAverageArray)-1]
			recentTest = testAverageArray[len(testAverageArray)-2]
			controlPercentFromRec = 100*(recentCtrl-newestCtrl)/float(recentCtrl)
			testPercentFromRec = 100*(recentTest-newestTest)/float(recentTest)
		
	else:
		print "Timing data missing!"
		error = 2

def plotData( xvalues, yvalues, title, xlabel, ylabel, label, filename ):
	"Plots the data passed in"
	plot = pl.plot(xvalues, yvalues, 'bo', label=label)
	pl.legend(loc='upper center', bbox_to_anchor=(0.5,-0.1))
	x1,x2,y1,y2 = pl.axis()
	pl.axis([x1, x2, min(yvalues)-.2, max(yvalues)+.2])
	pl.title(title)
	pl.xlabel(xlabel)
	pl.ylabel(ylabel)
	pl.savefig(filename)
	pl.clf()

def plotTiming( timing, testTiming, control, test, filename ):

	testPlot, = pl.plot(testTiming, test, 'bo', label='Test Region')
	controlplot, = pl.plot(timing, control, 'ro', label='Control Region')

	controlLine = []
	testLine = []

	controlCoeff = np.polyfit(timing, control, 1)
	testCoeff = np.polyfit(testTiming, test, 1)

	print controlCoeff
	print testCoeff

	for x in range(0, len(control)):
		controlLine.append(controlCoeff[0]*timing[x] + controlCoeff[1])
	
	for x in range(0, len(test)):
		testLine.append(testCoeff[0]*testTiming[x] + testCoeff[1])	

	pl.plot(testTiming, testLine, linestyle='solid', color='blue')
	pl.plot(timing, controlLine, linestyle='solid', color='red')

	x1,x2,y1,y2 = pl.axis()
	
	legend = pl.legend(loc='upper center', bbox_to_anchor=(0.5,-0.1))
	pl.axis([x1,x2, 25000000, 25150000])
	pl.title('Characterization Results')
	pl.xlabel('Time: Hours from start of experiments')
	pl.ylabel('Averages')
	pl.subplots_adjust(bottom=0.2)
	pl.savefig(filename)
	pl.clf()

#Get all of the data to plot
getTimingData( controlAverageArray, testAverageArray, timingTimes, timingTestTimes )

#plot the data
#Timing
plotTiming( timingTimes, timingTestTimes, controlAverageArray, testAverageArray, 'logs/test.png')

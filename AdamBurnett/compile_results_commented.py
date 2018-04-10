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
controlPercentFromRec = 0
testPercentFromRec = 0

controlAverageArray = []
testAverageArray = []

		
#Get the date of the next to most recent log
logs = sorted(glob.glob(THESIS_DIR + "logs/XMD/*.log"))
logs.sort(key=os.path.getmtime)

#If we have more than one log, populate recent date string for email
if (len(logs) > 1):
        recent = logs[len(logs)-2]
        recentname = recent.split('-')
        recentdate = recentname[1].replace(".","/") + " " + recentname[2].replace(".log","").replace(".",":")

else:
        recentdate = ""
		
#Gets the data from the path specified and places it in data.
#The format specifies the date format needed to parse the data
#From the filename. The hours array is created for the x-axis
#of the plot. The date is parsed from the filename and compared with 
#the date of the other files in the path.
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
		error = 1 #This is never checked, but assumed to not occur

#Sorts the logs based on the date. Implements a bubble sort
def sort(logs):
	for i in range(1,len(logs)):
		for j in range(0,len(logs)-1):
			if (parseDate(logs[j]) > parseDate(logs[j+1])):
				swap(j, j+1, logs)

#Routine used for sort			
def swap(i, j, logs):
	temp = logs[i]
	logs[i] = logs[j]
	logs[j] = temp

#Given a filename, it parses the date from it given the format string
def parseDate(logFile):
	date = datetime.datetime.strptime(logFile, THESIS_DIR + LOGS_DIR + 'output-%m.%d.%y-%H.%M.log')
	return date

#This routine is used for getting the timing data from the logs.
def getTimingData( controlData = [], testData = [], timingData = [] ):
	"Gets the control and test region data"
	global testPercentFromRec
	global controlPercentFromRec
	logFiles = sorted(glob.glob(TIMING_LOGS))

	sort(logFiles)

	tempTest = []
	tempControl = []
	originalDate = ""
	format = '%m.%d.%y-%H.%M.log'
	
	#Find the first date on the logs to compare to
	if (len(logFiles) > 0):
		originalLog = logFiles[0]
		firstDateString = originalLog.split('-',1)[-1]
		originalDate = datetime.datetime.strptime(firstDateString, format)
		os.system("sed -i 's/^M//' " + originalLog) #replaces windows style newline characters with unix style newlines
		#get original characterization values
		with open(originalLog, "r") as ifile:
			for line in ifile:
				if testRegion in line: #compares each line in the log until it finds the string "Test Region"
					for i in range(0,num_values):
						line = ifile.next()
						tempTest.append(int(line)) #Get the number from the line
					ifile.next()
					for i in range(0,num_values): #Same happens for control region
						line = ifile.next()
						tempControl.append(int(line))
		ifile.close()
		
		#calculate original averages
		originalControlAverage = sum(tempControl) / len(tempControl)
		originalTestAverage = sum(tempTest) / len(tempTest)
		
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
			#Calculate per log averages for each region
			controlAverage = sum(tempControl) / len(tempControl)
			testAverage = sum(tempTest) / len(tempTest)
			
			#Append those values to a cumulative array
			controlAverageArray.append(controlAverage)
			testAverageArray.append(testAverage)
			
			#Calculate percentage differences
			testPercentFromOrig = 100*(originalTestAverage - testAverage)/float(originalTestAverage)
			controlPercentFromOrig = 100*(originalControlAverage - controlAverage)/float(originalControlAverage)
			
			#Append the data to arrays used for plotting
			controlData.append(controlPercentFromOrig)
			testData.append(testPercentFromOrig)
			
			#find the date
			currentDateString = log.split('-',1)[-1]
			currentDate = datetime.datetime.strptime(currentDateString, format)
			#find the time difference in hours and append
			timeDifference = currentDate - originalDate
			timingData.append(timeDifference.days*24+timeDifference.seconds/3600)
		
		#compare to most recent measurement
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

def plotTiming( timing, control, test, filename ):
	testPlot, = pl.plot(timing, test, 'bo', label='Test Region')
	controlplot, = pl.plot(timing, control, 'ro', label='Control Region')
	x1,x2,y1,y2 = pl.axis()
	
	timingAxis = np.linspace(1,len(control),len(control))

	controlCoeff = np.polyfit(timingAxis, control, 1)
	testCoeff = np.polyfit(timingAxis, test, 1)

	controlLine = []
	testLine = []

	for x in range(0, len(control)):
		controlLine.append(controlCoeff[0]*timingAxis[x] + controlCoeff[1])
		testLine.append(testCoeff[0]*timingAxis[x] + testCoeff[1])

	pl.plot(timing, testLine, linestyle='solid', color='blue')
	pl.plot(timing, controlLine, linestyle='solid', color='red')
	
	legend = pl.legend(loc='upper center', bbox_to_anchor=(0.5,-0.1))
	pl.axis([x1,x2, min(test)-.2, max(test)+.2])
	pl.title('Characterization Results')
	pl.xlabel('Time: Hours from start of experiments')
	pl.ylabel('Percentage Change from Original')
	pl.subplots_adjust(bottom=0.2)
	pl.savefig(filename)
	pl.clf()

#Get all of the data to plot
getData( diodeLogs, '%m.%d.%Y-%H.log', diodeValues, diodeTimes )
getData( thermistorLogs, '%m.%d.%Y-%H.log', thermistorValues, thermistorTimes )
getData( oneTwoLogs, '1.2V/1.2V-%m.%d.%Y-%H.log', oneTwoValues, oneTwoTimes )
getData( twoFiveLogs, '2.5V/2.5V-%m.%d.%Y-%H.log', twoFiveValues, twoFiveTimes )
getData( threeThreeLogs, '3.3V/3.3V-%m.%d.%Y-%H.log', threeThreeValues, threeThreeTimes )
getData( burnCurrentLogs, '%m.%d.%Y-%H.log', burnCurrentValues, burnCurrentTimes )
getTimingData( controlPercentages, testPercentages, timingTimes )

#plot the data
#Timing
plotTiming( timingTimes, controlPercentages, testPercentages, 'logs/current.png')
#Diode
plotData( diodeTimes, diodeValues, 'Temperature', 'Hours since experiments began', 'Average Temperature (C)', 'Temperature', 'logs/diode.png' )
#Thermistor
plotData( thermistorTimes, thermistorValues, 'Oven Thermistor Data', 'Hours since experiments began', 'Average Temperature (C)', 'Thermistor', 'logs/thermistor.png' )
#1.2V
plotData( oneTwoTimes, oneTwoValues, '1.2V Supply', 'Hours since experiments began', 'Average Voltage (V)', '1.2V', 'logs/1.2V.png' )
#2.5V
plotData( twoFiveTimes, twoFiveValues, '2.5V Supply', 'Hours since experiments began', 'Average Voltage (V)', '2.5V', 'logs/2.5V.png' )
#3.3V
plotData( threeThreeTimes, threeThreeValues, '3.3V Supply', 'Hours since experiments began', 'Average Voltage (V)', '3.3V', 'logs/3.3V.png' )
#1.2V Current
plotData( burnCurrentTimes, burnCurrentValues, '1.2V Supply Current (Burn Board)', 'Hours since experiments began', 'Current (A)', 'Current', 'logs/burn_current.png' )


#Write message to file

message = open("message.txt", 'w')
message.truncate()

message.write("Hi all, \r\n\nHere are the most recent results:\r\nLast characterization previous to this one: " + recentdate + 
			 "\r\n\nTest Region\r\n\nCurrent Average: " + str(testAverageArray[len(testAverageArray)-1]) + "\r\nDifference from original characterization: " + str(testPercentages[len(testPercentages)-1]) + 
			 "%\r\nDifference from most recent characterization: " + str(testPercentFromRec) + "%\r\n\r\nControl Region \r\n\nCurrent Average: " + str(controlAverageArray[len(controlAverageArray)-1]) 
			 + "\r\nDifference from original characterization: " + str(controlPercentages[len(controlPercentages)-1]) + "%\r\nDifference from most recent characterization: " + str(controlPercentFromRec) 
			 + "%\r\n\nI have attached the log files and the current plots for reference.\r\n\nAdam Burnett")

message.close()

import pylab as pl
import numpy as np
import os
import glob

temperaturelogs = sorted(glob.glob("/home/adam/mount/users/adamh5/dropbox/TemperatureData/*.log"))
recenttemperature = temperaturelogs[len(temperaturelogs)-2]
temperature = []
xaxis = []

#Read data from most recent temperature file
with open(recenttemperature, "r") as ifile:
        temperature = map(float, ifile)

#Build the x range
value = 0;
line = []
for x in range(0,len(temperature)):
	xaxis.append(value)
	value += 6
coefficients = np.polyfit(xaxis, temperature, 1)

for x in range(0,len(temperature)):
	line.append(coefficients[0]*xaxis[x] + coefficients[1])

pl.plot(xaxis, temperature, 'ro', label='Temperature (C)')
pl.plot(xaxis, line, linestyle='solid')
legend = pl.legend(loc='upper center', bbox_to_anchor=(0.5,-0.1))
pl.axis([0,(3600/6)-1, 45,47])
pl.title('Temperature')
pl.xlabel('Time (arbitrary units)')
pl.ylabel('Temperature (C)')
pl.show()


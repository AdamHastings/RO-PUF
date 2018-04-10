#!/bin/bash

MONTH=$( date +%m)
DAY=$( date +%d)
YEAR=$( date +%y)
HOUR=$( date +%H)
RECENTLOG="output-$MONTH.$DAY.$YEAR-$HOUR.00.log"
RECENTLOG="/home/adam/Thesis/logs/XMD/"$RECENTLOG
THESISLOG="/home/adam/Thesis/logs/"
PLOT=$THESISLOG"current.png"
DIODE=$THESISLOG"diode.png"
THERMISTOR=$THESISLOG"thermistor.png"
ONETWO=$THESISLOG"1.2V.png"
TWOFIVE=$THESISLOG"2.5V.png"
THREETHREE=$THESISLOG"3.3V.png"
CURRENT=$THESISLOG"burn_current.png"
MESSAGELOC="/home/adam/Thesis/message.txt"

#Send Email!
SUBJECT="Characterization Results (Burn Board)"
#RECIPIENTS="ihavenoface@gmail.com"
#RECIPIENTS="ihavenoface@gmail.com,roberthale88@gmail.com,itsadamhastings@gmail.com,brad_hutchings@byu.edu"
RECIPIENTS="ihavenoface@gmail.com,brad_hutchings@byu.edu"
#cat /home/adam/Thesis/message.txt | mail -s $SUBJECT -aFrom:"Adam <adam.burnett@byu.edu>" $RECIPIENTS
#mutt -s "Test from mutt" ihavenoface@gmail.com < /home/adam/Thesis/message.txt -a output-11.06.15-12.12.log
#echo $RECENTLOG
#mutt -s "$SUBJECT" $RECIPIENTS < $MESSAGELOC -a $RECENTLOG -a $PLOT -a $DIODE -a $THERMISTOR -a $ONETWO -a $TWOFIVE -a $THREETHREE -a $CURRENT
mutt -s "$SUBJECT" $RECIPIENTS < $MESSAGELOC -a $PLOT -a $DIODE -a $THERMISTOR -a $ONETWO -a $TWOFIVE -a $THREETHREE -a $CURRENT

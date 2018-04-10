#!/bin/bash
DATE=$( date '+%m.%d.%y-%H.%M' )
LOG=$( echo "output-$DATE.log" )
LOGPATH="/home/adam/Thesis/logs/XMD/"
#source /home/adam/xilinx
cd /home/adam/Thesis
./characterize_LT.sh > $LOGPATH$LOG
#Blanking the FPGA between characterizations
./blank.sh > /dev/null
python compile_results.py
./send_email.sh

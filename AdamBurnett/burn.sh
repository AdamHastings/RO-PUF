#!/usr/bin/expect

set timeout 6000

spawn "/opt/Xilinx/14.4/ISE_DS/EDK/bin/lin64/xmd"
expect "XMD%" { send "" }
expect "XMD%" { send "fpga -f /home/adam/Thesis/burn_24HM.bit\r" }
expect "XMD%" { send "exit\r" }

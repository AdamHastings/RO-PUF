#!/usr/bin/expect

set timeout 6000

spawn "/opt/Xilinx/14.4/ISE_DS/EDK/bin/lin64/xmd"
expect "XMD%" { send "" }
expect "XMD%" { send "fpga -f /home/adam/Thesis/characterizer_LT_row_50_OMUX7.bit\r" }
expect "XMD%" { send "connect mb mdm\r" }
expect "XMD%" { send "terminal -jtaguart_server\r" }
expect "XMD%" { send "dow /home/adam/Thesis/workspace/characterizer/Debug/characterizer.elf\r" }
expect "XMD%" { send "run\r" }
expect "done." { send "disconnect 0\r" }
expect "XMD%" { send "exit\r" }

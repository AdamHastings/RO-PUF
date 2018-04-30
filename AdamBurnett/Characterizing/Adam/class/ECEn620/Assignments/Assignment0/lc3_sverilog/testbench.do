vsim -novopt lc3_testbench

add wave clk
add wave rst
add wave -radix hex memory_dout
add wave -radix hex memory_addr
add wave -radix hex memory_din
add wave memWE
add wave -radix hex sim:/lc3_testbench/LC3/DATAPATH/BUSS
add wave fetchUpdateStatus
add wave executeUpdateStatus
add wave sim:/lc3_testbench/LC3/CONTROL/CurrentState
add wave sim:/lc3_testbench/LC3/CONTROL/NextState
add wave -radix hex sim:/lc3_testbench/LC3/DATAPATH/PC
add wave sim:/lc3_testbench/LC3/DATAPATH/ldPC
add wave -radix hex sim:/lc3_testbench/LC3/DATAPATH/MAR
add wave sim:/lc3_testbench/LC3/DATAPATH/ldMAR
add wave -radix hex sim:/lc3_testbench/LC3/DATAPATH/IR
add wave sim:/lc3_testbench/LC3/DATAPATH/ldIR
add wave -radix hex sim:/lc3_testbench/LC3/DATAPATH/MDR
add wave sim:/lc3_testbench/LC3/DATAPATH/ldMDR

add wave -radix hex sim:/lc3_testbench/LC3/DATAPATH/DR
add wave -radix hex sim:/lc3_testbench/LC3/DATAPATH/SR1
add wave -radix hex sim:/lc3_testbench/LC3/DATAPATH/SR2
add wave -radix hex sim:/lc3_testbench/LC3/DATAPATH/RA
add wave -radix hex sim:/lc3_testbench/LC3/DATAPATH/RB
add wave -radix hex sim:/lc3_testbench/LC3/DATAPATH/ADDER

#add wave sim:/lc3_testbench/LC3/DATAPATH/regWE
add wave -radix hex sim:/lc3_testbench/LC3/DATAPATH/REGFILE\[0\]
add wave -radix hex sim:/lc3_testbench/LC3/DATAPATH/REGFILE\[1\]
add wave -radix hex sim:/lc3_testbench/LC3/DATAPATH/REGFILE\[2\]
add wave -radix hex sim:/lc3_testbench/LC3/DATAPATH/REGFILE\[3\]
add wave -radix hex sim:/lc3_testbench/LC3/DATAPATH/REGFILE\[4\]
add wave -radix hex sim:/lc3_testbench/LC3/DATAPATH/REGFILE\[5\]
add wave -radix hex sim:/lc3_testbench/LC3/DATAPATH/REGFILE\[6\]
add wave -radix hex sim:/lc3_testbench/LC3/DATAPATH/REGFILE\[7\]



run 2000000 ns
#Test Datapath

#Figure out how to compile
source testLib.do

vsim -novopt lc3_datapath

#Initialize the Circuit

#Set the Clock 
force -freeze sim:/lc3_datapath/clk 1 0, 0 {10ns} -r 20ns
add wave sim:/lc3_datapath/clk 
#Reset the Circuit

force -drive sim:/lc3_datapath/rst 1 0
add wave sim:/lc3_datapath/rst

#Set Control Signals low
force -drive sim:/lc3_datapath/aluControl 00 0
force -drive sim:/lc3_datapath/enaALU 0 0
force -drive sim:/lc3_datapath/SR1 000 0
force -drive sim:/lc3_datapath/SR2 000 0
force -drive sim:/lc3_datapath/DR 000 0
force -drive sim:/lc3_datapath/regWE 0 0
force -drive sim:/lc3_datapath/selPC 00 0
force -drive sim:/lc3_datapath/enaMARM 0 0
force -drive sim:/lc3_datapath/selMAR 0 0
force -drive sim:/lc3_datapath/selEAB1 0 0
force -drive sim:/lc3_datapath/selEAB2 00 0
force -drive sim:/lc3_datapath/enaPC 0 0
force -drive sim:/lc3_datapath/ldPC 0 0
force -drive sim:/lc3_datapath/ldIR 0 0
force -drive sim:/lc3_datapath/ldMAR 0 0
force -drive sim:/lc3_datapath/ldMDR 0 0
force -drive sim:/lc3_datapath/selMDR 0 0
#force -drive sim:/lc3_datapath/memWE 0 0
force -drive sim:/lc3_datapath/flagWE 0 0
force -drive sim:/lc3_datapath/enaMDR 0 0
force -drive sim:/lc3_datapath/rst 1 0

#run 10 Clock Cycles
runClk 10 

#Bring Circuit out of Reset
force -drive sim:/lc3_datapath/rst 0 0

runClk 20 

echo "Ensure that PC was reset correctly" 
checkValue "PC" hex 0000 

##################### TEST REGISTERS ########################

##########################
## TEST PROGRAM COUNTER ##
##########################

add wave -radix hex PC
add wave -radix hex PCMUX 
add wave ldPC
runClk 2 

testRegister "PC" "PCMUX" "ldPC" 16

##########################
##       TEST MAR       ##
##########################

add wave -radix hex MAR
add wave -radix hex BUSS 
add wave ldMAR
runClk 2 

testRegister "MAR" "BUSS" "ldMAR" 16

##########################
##       TEST MDR       ##
##########################

add wave -radix hex MDR
add wave -radix hex MDRMUX 
add wave ldMDR
runClk 2 

testRegister "MDR" "MDRMUX" "ldMDR" 16

##########################
## INSTRUCTION REGISTER ##
##########################

add wave -radix hex IR
add wave -radix hex BUSS 
add wave ldIR
runClk 2 

testRegister "IR" "BUSS" "ldIR" 16

##########################
##  TEST REGISTER FILE  ##
##########################

add wave -radix hex REGFILE
add wave -radix hex BUSS 
add wave regWE
runClk 2 

for { set i 0 } {$i < 8} {incr i} {
  force -drive DR 10#$i
  testRegister "REGFILE\[$i\]" "BUSS" "regWE" 16  
}    

##########################
##     TEST MEMORY      ##
##########################

#add wave -radix hex MEMORY
#add wave -radix hex MAR
#add wave -radix hex MDR 
#add wave memWE
#runClk 2 

#for { set i 0 } {$i < 8} {incr i} {
#  noforce MAR
#  force -freeze MAR 10#$i
#  testRegister "MEMORY\[$i\]" "MDR" "memWE" 16  
#}

##################### TEST MULTIPLEXORS ########################

##########################
##     TEST MARMUX      ##
##########################

add wave -radix hex MARMUX
add wave -radix hex selMAR
add wave -radix hex ADDER
add wave -radix hex ZEXT  

textMultiplexor "MARMUX" [list "ADDER" "ZEXT"] "selMAR" 16 

##########################
##     TEST PCMUX      ##
##########################

add wave -radix hex PCMUX
add wave -radix hex selPC
add wave -radix hex PCINCR
add wave -radix hex ADDER
add wave -radix hex BUSS  

textMultiplexor "PCMUX" [list "PCINCR" "ADDER" "BUSS" ] "selPC" 16 

##########################
##    TEST ADDR1MUX     ##
##########################

add wave -radix hex ADDR1MUX
add wave -radix hex selEAB1
add wave -radix hex PC
add wave -radix hex RA  

textMultiplexor "ADDR1MUX" [list "PC" "RA" ] "selEAB1" 16

##########################
##    TEST ADDR2MUX     ##
##########################

add wave -radix hex ADDR2MUX
add wave -radix hex selEAB2
add wave -radix hex ZERO16
add wave -radix hex SEXT5  
add wave -radix hex SEXT8
add wave -radix hex SEXT10

textMultiplexor "ADDR2MUX" [list "ZERO16" "SEXT5" "SEXT8" "SEXT10" ] "selEAB2" 16

##########################
##    TEST SR2MUX     ##
##########################

add wave -radix hex SR2MUX
add wave -radix hex IR\[5\]
add wave -radix hex RB
add wave -radix hex SEXT4 

textMultiplexor "SR2MUX" [list "RB" "SEXT4" ] "IR\[5\] " 16

##########################
##    TEST MDR2MUX     ##
##########################

add wave -radix hex MDRMUX
add wave -radix hex selMDR
add wave -radix hex BUSS
add wave -radix hex memOut

textMultiplexor "MDRMUX" [list "BUSS" "memOut" ] "selMDR" 16

##########################
##   TEST SEXT, ZEXT    ##
##########################

echo "Checking Sign Extension, Zero Extension"

add wave -radix hex IR
add wave -radix hex SEXT4
add wave -radix hex SEXT5
add wave -radix hex SEXT8
add wave -radix hex SEXT10
add wave -radix hex ZEXT

#noforce SEXT4
#noforce SEXT5
#noforce SEXT8
#noforce SEXT10
#noforce ZEXT

echo "Check ZERO Extension Correct" 

force -freeze IR 16#0000
run 10ps
simtime
checkValue "SEXT4" hex 0000
checkValue "SEXT5" hex 0000
checkValue "SEXT8" hex 0000
checkValue "SEXT10" hex 0000
checkValue "ZEXT" hex 0000
runClk 1

echo "Test IR Bit 4" 
simtime
noforce IR
force -freeze IR 16#0010
run 10ps
checkValue "SEXT4" hex fff0
checkValue "SEXT5" hex 0010
checkValue "SEXT8" hex 0010
checkValue "SEXT10" hex 0010
checkValue "ZEXT" hex 0010
runClk 1

echo "Test IR Bit 5" 
simtime
noforce IR
force -freeze IR 16#0020
run 10ps
checkValue "SEXT4" hex 0000
checkValue "SEXT5" hex ffe0
checkValue "SEXT8" hex 0020
checkValue "SEXT10" hex 0020
checkValue "ZEXT" hex 0020
runClk 1

echo "Test IR Bit 8" 
simtime
noforce IR
force -freeze IR 16#0100
run 10ps
checkValue "SEXT4" hex 0000
checkValue "SEXT5" hex 0000
checkValue "SEXT8" hex ff00
checkValue "SEXT10" hex 0100
checkValue "ZEXT" hex 0000
runClk 1

echo "Test IR Bit 10" 
simtime
noforce IR
force -freeze IR 16#0400
run 10ps
checkValue "SEXT4" hex 0000
checkValue "SEXT5" hex 0000
checkValue "SEXT8" hex 0000
checkValue "SEXT10" hex fc00
checkValue "ZEXT" hex 0000
runClk 1

echo "Test IR Bit 7, ZEXT" 
simtime
noforce IR
force -freeze IR 16#00FF
run 10ps
checkValue "SEXT4" hex ffff
checkValue "SEXT5" hex ffff
checkValue "SEXT8" hex 00ff
checkValue "SEXT10" hex 00ff
checkValue "ZEXT" hex 00ff
runClk 1

noforce IR

##########################
##      TEST ADDER      ##
##########################

echo "Testing Adder"

add wave -radix hex ADDER
add wave -radix hex ADDR1MUX
add wave -radix hex ADDR2MUX

runClk 1
noforce ADDER
noforce ADDR1MUX
noforce ADDR2MUX
force -freeze ADDR1MUX 10#0
force -freeze ADDR2MUX 10#0
run 10ps
checkValue "ADDER" hex 0000

runClk 1
noforce ADDR1MUX
noforce ADDR2MUX
force -freeze ADDR1MUX 10#1
force -freeze ADDR2MUX 10#1
run 10ps
checkValue "ADDER" hex 0002

runClk 1
noforce ADDR1MUX
noforce ADDR2MUX
force -freeze ADDR1MUX 10#32767
force -freeze ADDR2MUX 10#1
run 10ps
checkValue "ADDER" unsigned 32768
runClk 1

##########################
##      TEST ALU        ##
##########################

add wave -radix hex ALU
add wave -radix hex aluControl
add wave -radix hex RA
add wave -radix hex SR2MUX

echo "Testing ALU" 

noforce RA
noforce SR2MUX
echo "Test PASS A" 
force -drive aluControl 00
force -freeze RA 16#0000
force -freeze SR2MUX 16#FFFF
run 10ps
checkValue "ALU" hex 0000
runClk 1

noforce RA
noforce SR2MUX
force -freeze RA 16#ffff
force -freeze SR2MUX 16#0000
run 10ps
checkValue "ALU" hex ffff
runClk 1

echo "Testing ADD"
noforce RA
noforce SR2MUX
force -drive aluControl 01
force -freeze RA 16#0001
force -freeze SR2MUX 16#0001
run 10ps
checkValue "ALU" hex 0002
runClk 1


echo "Testing AND"
noforce RA
noforce SR2MUX
force -drive aluControl 10
force -freeze RA 16#AAAA
force -freeze SR2MUX 16#FFFF
run 10ps
checkValue "ALU" hex aaaa
runClk 1

echo "Testing NOT"

force -drive aluControl 11
run 10ps
checkValue "ALU" hex 5555
runClk 1
noforce RA
noforce SR2MUX

##########################
##      TEST BUS        ##
##########################

echo "Testing BUSS Drivers"

add wave -radix hex BUSS
add wave enaALU
add wave enaMARM
add wave enaPC
add wave enaMDR

noforce MARMUX
noforce PC
noforce ALU
noforce MDR
force -freeze MARMUX 16#ffff
force -freeze PC 16#ffff
force -freeze ALU 16#ffff
force -freeze MDR 16#ffff

force -drive enaALU 0
force -drive enaMARM 0
force -drive enaPC 0
force -drive enaMDR 0

run 10ps

checkValue BUSS hex zzzz

runClk 1

force -drive enaALU 1
force -drive enaMARM 0
force -drive enaPC 0
force -drive enaMDR 0

run 10ps

checkValue BUSS hex ffff

runClk 1

force -drive enaALU 0
force -drive enaMARM 1
force -drive enaPC 0
force -drive enaMDR 0

run 10ps

checkValue BUSS hex ffff

runClk 1

force -drive enaALU 0
force -drive enaMARM 0
force -drive enaPC 1
force -drive enaMDR 0

run 10ps

checkValue BUSS hex ffff

runClk 1

force -drive enaALU 0
force -drive enaMARM 0
force -drive enaPC 0
force -drive enaMDR 1

run 10ps

checkValue BUSS hex ffff

runClk 1

echo "done" 
#quit -sim
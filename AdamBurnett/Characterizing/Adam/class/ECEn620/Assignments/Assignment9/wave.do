add wave *
add wave -radix ascii top/instr
add wave -radix ascii top/state
add wave -radix hex top/mem_bus/cb/*
add wave -radix hex top/DUT/Processor/R0_out
add wave -radix hex top/DUT/Processor/R1_out
add wave -radix hex top/DUT/Processor/R2_out
add wave -radix hex top/DUT/Processor/R3_out
add wave -radix hex top/DUT/Processor/PC_count
add wave -radix hex top/DUT/Processor/instruction

run 500 ns


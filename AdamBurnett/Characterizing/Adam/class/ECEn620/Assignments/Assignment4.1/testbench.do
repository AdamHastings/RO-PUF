quit -sim
vsim -novopt testbench
add wave -radix hex *
add wave -radix hex sim:/testbench/mem_bus/*
add wave -radix hex sim:/testbench/mem/mem_bus/*
run 500 ns
quit -sim
vsim -novopt testbench
add wave -radix hex *
add wave -radix hex sim:/testbench/mem_bus/*
#add wave sim:/testbench/mem/*
run 50000 ns

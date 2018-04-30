quit -sim
vsim -novopt testbench
add wave -radix hex *
#add wave sim:/testbench/mem/*
run 1000 ns

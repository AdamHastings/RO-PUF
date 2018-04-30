quit -sim
vsim -novopt top
add wave -radix hex *
add wave -radix hex sim:/top/mem_bus/*
add wave -radix hex sim:/top/mem_bus/cb/*
#add wave -radix hex sim:/top/mem/mem_bus/*
run 5000 ns

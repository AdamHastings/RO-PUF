quit -sim
vsim -novopt testbench
add wave -radix hex * 
run 150 ns

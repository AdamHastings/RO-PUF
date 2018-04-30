quit -sim
vsim -novopt top
add wave -radix hex *
add wave -radix hex sim:/top/mem_bus/*
add wave -radix hex sim:/top/mem_bus/cb/*
add wave sim:/TransactionPkg::Transaction::error
add wave sim:/top/mem_bus/read_write_sig_assertion
run 5000 ns

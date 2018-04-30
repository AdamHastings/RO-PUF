TOPLEVEL=tb1
VERILOG_FILES= simutil.v bindfile.sv fifo1.sv tb1.sv

questa_gui: 
	vlib work
	vmap work work
	vlog -mfcu -sv +acc ${VERILOG_FILES}
	vsim -novopt -assertdebug -do "view wave;do wave.do; run 26000 ns" ${TOPLEVEL}

questa_batch: ${VERILOG_FILES} clean
	vlib work
	vmap work work
	vlog -mfcu -sv +acc ${VERILOG_FILES}
	vsim -c -novopt -assertdebug -do "run -all" ${TOPLEVEL}

clean:
	@rm -rf work transcript vsim.wlf

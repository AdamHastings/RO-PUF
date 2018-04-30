TOPLEVEL=top
VERILOG_FILES=tb_pkg.sv arb_if.sv arbiter.vp golden_arb.sv test.sv top.sv

questa_gui: 
	vlib work
	vmap work work
	vlog ${VERILOG_FILES}
	vsim -novopt -coverage -msgmode both -displaymsgmode both -do "view wave;do wave.do;run -all" ${TOPLEVEL}

questa_batch: ${VERILOG_FILES} clean
	vlib work
	vmap work work
	vlog ${VERILOG_FILES}
	vsim -c -novopt -coverage -do "run -all" ${TOPLEVEL}

clean:
	@rm -rf work transcript vsim.wlf

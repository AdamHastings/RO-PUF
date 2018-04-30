TOPLEVEL=top
VERILOG_FILES= risc_spm/Alu_RISC.v risc_spm/Control_Unit.v risc_spm/mux3_1.v risc_spm/Register_Unit.v risc_spm/Clock_Unit.v risc_spm/Memory_Unit.v risc_spm/mux5_1.v risc_spm/Processing_Unit.v risc_spm/RISC_SPM.v spm_if.sv TbEnvPkg.sv test.sv top.sv

questa_gui: 
	vlib work
	vmap work work
	vlog -mfcu -sv ${VERILOG_FILES}
	vsim -novopt -coverage -msgmode both -displaymsgmode both -do "view wave;do wave.do;run -all" ${TOPLEVEL}

questa_batch: ${VERILOG_FILES} clean
	vlib work
	vmap work work
	vlog -mfcu -sv ${VERILOG_FILES}
	vsim -c -novopt -coverage -do "run -all" ${TOPLEVEL}

clean:
	@rm -rf work transcript vsim.wlf

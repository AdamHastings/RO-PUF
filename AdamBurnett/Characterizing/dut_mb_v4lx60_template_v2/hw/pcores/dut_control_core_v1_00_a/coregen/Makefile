## User Logic ILA
UL_ILA=ul_ila
UL_ILA_INST=UL_ILA_I
UL_ILA_HDL_LOC=../hdl/vhdl/user_logic.vhd
## Project Files
UL_ILA_PROJECT=$(UL_ILA).xco
UL_ILA_NGC=$(UL_ILA).ngc
UL_ILA_CDC=$(UL_ILA).cdc
COREGEN_PROJECT=coregen.cgp

all: $(UL_ILA_NGC) $(UL_ILA_INST).cdc

## User Logic ILA - Generate NGC
$(UL_ILA_NGC): $(UL_ILA_PROJECT) $(COREGEN_PROJECT)
	coregen -p $(COREGEN_PROJECT) -b $(UL_ILA_PROJECT)

## User Logic ILA - Generate CDC with Signal Names
$(UL_ILA_INST).cdc: $(UL_ILA_CDC)
	generate_cdc.py $(UL_ILA_CDC) $(UL_ILA_HDL_LOC) $(UL_ILA_INST) 

clean:
	rm -rf coregen.cgc coregen.log
	rm -rf create_$(UL_ILA).tcl
	rm -rf edit_$(UL_ILA).tcl
	rm -rf $(UL_ILA).asy
	rm -rf $(UL_ILA).cdc
	rm -rf $(UL_ILA).constraints
	rm -rf $(UL_ILA)_flist.txt
	rm -rf 	$(UL_ILA).gise
	rm -rf $(UL_ILA).ncf
	rm -rf $(UL_ILA)_readme.txt
	rm -rf $(UL_ILA).sym
	rm -rf $(UL_ILA).ucf
	rm -rf $(UL_ILA).vhd
	rm -rf $(UL_ILA).vho
	rm -rf $(UL_ILA).xdc
	rm -rf $(UL_ILA).xise
	rm -rf $(UL_ILA)_xmdf.tcl
	rm -rf tmp
	rm -rf _xmsgs

real_clean: clean
	rm -rf $(UL_ILA).ngc
	rm -rf $(UL_ILA_INST).cdc

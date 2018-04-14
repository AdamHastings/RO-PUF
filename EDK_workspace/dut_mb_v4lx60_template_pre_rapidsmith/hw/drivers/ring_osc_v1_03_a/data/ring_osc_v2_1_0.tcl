##############################################################################
## Filename:          E:\Adam\Characterizing\characterizer_planahead\dut_mb_v4lx60_template_pre_rapidsmith\hw/drivers/ring_osc_v1_03_a/data/ring_osc_v2_1_0.tcl
## Description:       Microprocess Driver Command (tcl)
## Date:              Tue Jun 09 14:43:10 2015 (by Create and Import Peripheral Wizard)
##############################################################################

#uses "xillib.tcl"

proc generate {drv_handle} {
  xdefine_include_file $drv_handle "xparameters.h" "ring_osc" "NUM_INSTANCES" "DEVICE_ID" "C_BASEADDR" "C_HIGHADDR" 
}

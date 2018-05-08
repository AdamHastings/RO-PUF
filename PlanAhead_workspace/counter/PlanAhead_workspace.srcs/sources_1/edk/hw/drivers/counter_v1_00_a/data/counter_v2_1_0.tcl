##############################################################################
## Filename:          /home/adam/RO-PUF/XPS_workspace/hw/drivers/counter_v1_00_a/data/counter_v2_1_0.tcl
## Description:       Microprocess Driver Command (tcl)
## Date:              Wed Apr 25 17:03:45 2018 (by Create and Import Peripheral Wizard)
##############################################################################

#uses "xillib.tcl"

proc generate {drv_handle} {
  xdefine_include_file $drv_handle "xparameters.h" "counter" "NUM_INSTANCES" "DEVICE_ID" "C_BASEADDR" "C_HIGHADDR" 
}

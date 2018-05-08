/*****************************************************************************
* Filename:          E:\Adam\Characterizing\characterizer_planahead\dut_mb_v4lx60_template_pre_rapidsmith\hw/drivers/ring_osc_v1_03_a/src/ring_osc.h
* Version:           1.03.a
* Description:       ring_osc Driver Header File
* Date:              Tue Jun 09 14:43:10 2015 (by Create and Import Peripheral Wizard)
*****************************************************************************/

#ifndef RING_OSC_H
#define RING_OSC_H

/***************************** Include Files *******************************/

#include "xbasic_types.h"
#include "xstatus.h"
#include "xil_io.h"

/************************** Constant Definitions ***************************/


/**
 * User Logic Slave Space Offsets
 * -- SLV_REG0 : user logic slave module register 0
 * -- SLV_REG1 : user logic slave module register 1
 * -- SLV_REG2 : user logic slave module register 2
 * -- SLV_REG3 : user logic slave module register 3
 * -- SLV_REG4 : user logic slave module register 4
 * -- SLV_REG5 : user logic slave module register 5
 * -- SLV_REG6 : user logic slave module register 6
 * -- SLV_REG7 : user logic slave module register 7
 */
#define RING_OSC_USER_SLV_SPACE_OFFSET (0x00000000)
#define RING_OSC_SLV_REG0_OFFSET (RING_OSC_USER_SLV_SPACE_OFFSET + 0x00000000)
#define RING_OSC_SLV_REG1_OFFSET (RING_OSC_USER_SLV_SPACE_OFFSET + 0x00000004)
#define RING_OSC_SLV_REG2_OFFSET (RING_OSC_USER_SLV_SPACE_OFFSET + 0x00000008)
#define RING_OSC_SLV_REG3_OFFSET (RING_OSC_USER_SLV_SPACE_OFFSET + 0x0000000C)
#define RING_OSC_SLV_REG4_OFFSET (RING_OSC_USER_SLV_SPACE_OFFSET + 0x00000010)
#define RING_OSC_SLV_REG5_OFFSET (RING_OSC_USER_SLV_SPACE_OFFSET + 0x00000014)
#define RING_OSC_SLV_REG6_OFFSET (RING_OSC_USER_SLV_SPACE_OFFSET + 0x00000018)
#define RING_OSC_SLV_REG7_OFFSET (RING_OSC_USER_SLV_SPACE_OFFSET + 0x0000001C)

/**************************** Type Definitions *****************************/


/***************** Macros (Inline Functions) Definitions *******************/

/**
 *
 * Write a value to a RING_OSC register. A 32 bit write is performed.
 * If the component is implemented in a smaller width, only the least
 * significant data is written.
 *
 * @param   BaseAddress is the base address of the RING_OSC device.
 * @param   RegOffset is the register offset from the base to write to.
 * @param   Data is the data written to the register.
 *
 * @return  None.
 *
 * @note
 * C-style signature:
 * 	void RING_OSC_mWriteReg(Xuint32 BaseAddress, unsigned RegOffset, Xuint32 Data)
 *
 */
#define RING_OSC_mWriteReg(BaseAddress, RegOffset, Data) \
 	Xil_Out32((BaseAddress) + (RegOffset), (Xuint32)(Data))

/**
 *
 * Read a value from a RING_OSC register. A 32 bit read is performed.
 * If the component is implemented in a smaller width, only the least
 * significant data is read from the register. The most significant data
 * will be read as 0.
 *
 * @param   BaseAddress is the base address of the RING_OSC device.
 * @param   RegOffset is the register offset from the base to write to.
 *
 * @return  Data is the data from the register.
 *
 * @note
 * C-style signature:
 * 	Xuint32 RING_OSC_mReadReg(Xuint32 BaseAddress, unsigned RegOffset)
 *
 */
#define RING_OSC_mReadReg(BaseAddress, RegOffset) \
 	Xil_In32((BaseAddress) + (RegOffset))


/**
 *
 * Write/Read 32 bit value to/from RING_OSC user logic slave registers.
 *
 * @param   BaseAddress is the base address of the RING_OSC device.
 * @param   RegOffset is the offset from the slave register to write to or read from.
 * @param   Value is the data written to the register.
 *
 * @return  Data is the data from the user logic slave register.
 *
 * @note
 * C-style signature:
 * 	void RING_OSC_mWriteSlaveRegn(Xuint32 BaseAddress, unsigned RegOffset, Xuint32 Value)
 * 	Xuint32 RING_OSC_mReadSlaveRegn(Xuint32 BaseAddress, unsigned RegOffset)
 *
 */
#define RING_OSC_mWriteSlaveReg0(BaseAddress, RegOffset, Value) \
 	Xil_Out32((BaseAddress) + (RING_OSC_SLV_REG0_OFFSET) + (RegOffset), (Xuint32)(Value))
#define RING_OSC_mWriteSlaveReg1(BaseAddress, RegOffset, Value) \
 	Xil_Out32((BaseAddress) + (RING_OSC_SLV_REG1_OFFSET) + (RegOffset), (Xuint32)(Value))
#define RING_OSC_mWriteSlaveReg2(BaseAddress, RegOffset, Value) \
 	Xil_Out32((BaseAddress) + (RING_OSC_SLV_REG2_OFFSET) + (RegOffset), (Xuint32)(Value))
#define RING_OSC_mWriteSlaveReg3(BaseAddress, RegOffset, Value) \
 	Xil_Out32((BaseAddress) + (RING_OSC_SLV_REG3_OFFSET) + (RegOffset), (Xuint32)(Value))
#define RING_OSC_mWriteSlaveReg4(BaseAddress, RegOffset, Value) \
 	Xil_Out32((BaseAddress) + (RING_OSC_SLV_REG4_OFFSET) + (RegOffset), (Xuint32)(Value))
#define RING_OSC_mWriteSlaveReg5(BaseAddress, RegOffset, Value) \
 	Xil_Out32((BaseAddress) + (RING_OSC_SLV_REG5_OFFSET) + (RegOffset), (Xuint32)(Value))
#define RING_OSC_mWriteSlaveReg6(BaseAddress, RegOffset, Value) \
 	Xil_Out32((BaseAddress) + (RING_OSC_SLV_REG6_OFFSET) + (RegOffset), (Xuint32)(Value))
#define RING_OSC_mWriteSlaveReg7(BaseAddress, RegOffset, Value) \
 	Xil_Out32((BaseAddress) + (RING_OSC_SLV_REG7_OFFSET) + (RegOffset), (Xuint32)(Value))

#define RING_OSC_mReadSlaveReg0(BaseAddress, RegOffset) \
 	Xil_In32((BaseAddress) + (RING_OSC_SLV_REG0_OFFSET) + (RegOffset))
#define RING_OSC_mReadSlaveReg1(BaseAddress, RegOffset) \
 	Xil_In32((BaseAddress) + (RING_OSC_SLV_REG1_OFFSET) + (RegOffset))
#define RING_OSC_mReadSlaveReg2(BaseAddress, RegOffset) \
 	Xil_In32((BaseAddress) + (RING_OSC_SLV_REG2_OFFSET) + (RegOffset))
#define RING_OSC_mReadSlaveReg3(BaseAddress, RegOffset) \
 	Xil_In32((BaseAddress) + (RING_OSC_SLV_REG3_OFFSET) + (RegOffset))
#define RING_OSC_mReadSlaveReg4(BaseAddress, RegOffset) \
 	Xil_In32((BaseAddress) + (RING_OSC_SLV_REG4_OFFSET) + (RegOffset))
#define RING_OSC_mReadSlaveReg5(BaseAddress, RegOffset) \
 	Xil_In32((BaseAddress) + (RING_OSC_SLV_REG5_OFFSET) + (RegOffset))
#define RING_OSC_mReadSlaveReg6(BaseAddress, RegOffset) \
 	Xil_In32((BaseAddress) + (RING_OSC_SLV_REG6_OFFSET) + (RegOffset))
#define RING_OSC_mReadSlaveReg7(BaseAddress, RegOffset) \
 	Xil_In32((BaseAddress) + (RING_OSC_SLV_REG7_OFFSET) + (RegOffset))

/************************** Function Prototypes ****************************/


/**
 *
 * Run a self-test on the driver/device. Note this may be a destructive test if
 * resets of the device are performed.
 *
 * If the hardware system is not built correctly, this function may never
 * return to the caller.
 *
 * @param   baseaddr_p is the base address of the RING_OSC instance to be worked on.
 *
 * @return
 *
 *    - XST_SUCCESS   if all self-test code passed
 *    - XST_FAILURE   if any self-test code failed
 *
 * @note    Caching must be turned off for this function to work.
 * @note    Self test may fail if data memory and device are not on the same bus.
 *
 */
XStatus RING_OSC_SelfTest(void * baseaddr_p);

#endif /** RING_OSC_H */

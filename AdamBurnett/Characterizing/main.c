#include <stdio.h>
#include <xparameters.h>
#include <time.h>
#include <xuartlite.h>
#include <xuartlite_l.h>
#include <xio.h>
#include "ring_osc.h"
#include "xtmrctr.h"
#include "xintc_l.h"
// Date:       Version:  Developer:  Notes:
// 2014/01/06  1.0       aschmidt    Created and works with HW v1.0

#define SW_VERSION         "1.0"
#define UART_TX_FIFO_EMPTY 0x00000004

//the macros below correspond to bit locations specified in the user_logic.vhd file
#define SW_RESET           0x00000001
#define DUT_UART_REQUEST   0x00000002
#define DUT_UART_GRANTED   0x00000004
#define DUT_UART_RELEASE   0x00000008
#define DUT_TEST_DONE      0x00000010

#define RELOAD				10
#define CLK_TICKS			100000000

XTmrCtr timer0;
XTmrCtr* timer0ptr = &timer0;
int timerReloadCount = 0;

typedef struct
{
	unsigned int ctrl_reg;
	unsigned int status_reg;
	unsigned int ver_reg;
} control_core_regs;

void interrupt_handler_dispatcher(void* ptr) {
	int intc_status = XIntc_GetIntrStatus(XPAR_INTC_0_BASEADDR);
	// Check the FIT interrupt first.
	if (intc_status & XPAR_XPS_TIMER_0_INTERRUPT_MASK){
		XIntc_AckIntr(XPAR_INTC_0_BASEADDR, XPAR_XPS_TIMER_0_INTERRUPT_MASK);
		int count = RING_OSC_mReadSlaveReg0(XPAR_RING_OSC_0_BASEADDR, RING_OSC_SLV_REG0_OFFSET);
		if (timerReloadCount < RELOAD)
		{
			xil_printf("%d\r\n", count);
			timerReloadCount++;
			RING_OSC_mWriteReg(XPAR_RING_OSC_0_BASEADDR, RING_OSC_SLV_REG1_OFFSET, 0); //Reset
			RING_OSC_mWriteReg(XPAR_RING_OSC_0_BASEADDR, RING_OSC_SLV_REG1_OFFSET, 0xFFFFFFFF); //start
			XTmrCtr_Start(timer0ptr, XPAR_XPS_TIMER_0_DEVICE_ID);
		}
		else
		{
			xil_printf("done.\r\n");
		}
	}
}

int main()
{
	//Interrupts setup
	microblaze_register_handler(interrupt_handler_dispatcher, NULL);
	XIntc_EnableIntr(XPAR_INTC_0_BASEADDR, XPAR_XPS_TIMER_0_INTERRUPT_MASK);
	XIntc_MasterEnable(XPAR_INTC_0_BASEADDR);
	microblaze_enable_interrupts();

	control_core_regs *core_regs = (control_core_regs*)(XPAR_DUT_CONTROL_CORE_0_BASEADDR);

	unsigned int *uart_status    = (unsigned int*)(XPAR_UARTLITE_0_BASEADDR + 0x8);

	/* Assert DUT_UART_REQUEST to get access to Minicom */
	core_regs->ctrl_reg = DUT_UART_REQUEST;

	/* Wait until PUF Power Board Grants access to Minicom */
	while ((core_regs->status_reg & DUT_UART_GRANTED) != DUT_UART_GRANTED);

	//Initialize the timer
	XStatus Status;
	Status = XTmrCtr_Initialize(timer0ptr, XPAR_XPS_TIMER_0_DEVICE_ID);

	if (Status != XST_SUCCESS)
	{
		xil_printf("\r\nTimer counter init failed\r\n");
		return XST_FAILURE;
	}


	Status = XTmrCtr_SelfTest(timer0ptr, 0);
	if (Status != XST_SUCCESS)
	{
		print("\r\nTimer counter self test failed\r\n");
		  return XST_FAILURE;
	}

	xil_printf("Characterizing Ring Oscillator\r\n\r\n");

	xil_printf("Enter a time to run the ring oscillator for (in seconds): ");

	unsigned int value;

	int seconds = 30;

	while(1)
	{
		value = XIo_In32(XPAR_RS232_UART_1_BASEADDR);
		if (value != 0x00 && value != 0x0D && value != 0x0A)
		{
			seconds = (seconds*10) + ((value)-48);
		}
		else if (value == 0x0D || value == 0x0A)
		{
			//xil_printf("\r\nNumber of seconds entered: %d\r\n", seconds);
			break;
		}
	}

	xil_printf("\r\nRunning ring oscillator for %d seconds\r\n", seconds);

	//Configuring the timer
	XTmrCtr_SetOptions(timer0ptr, XPAR_XPS_TIMER_0_DEVICE_ID, (XTC_AUTO_RELOAD_OPTION|XTC_INT_MODE_OPTION|XTC_DOWN_COUNT_OPTION));
	XTmrCtr_SetResetValue(timer0ptr, XPAR_XPS_TIMER_0_DEVICE_ID, seconds*CLK_TICKS);
	XTmrCtr_Start(timer0ptr, XPAR_XPS_TIMER_0_DEVICE_ID);

	xil_printf("Starting ring oscillator...\r\n");

	RING_OSC_mWriteReg(XPAR_RING_OSC_0_BASEADDR, RING_OSC_SLV_REG1_OFFSET, 0xFFFFFFFF);

	while (1)
	{
		//Waiting for the timer interrupt
	}

	core_regs->ctrl_reg |= DUT_TEST_DONE;
	fflush(stdout);

	/* Wait until UART finishes printing */
	while ((*uart_status & UART_TX_FIFO_EMPTY) != UART_TX_FIFO_EMPTY) ;

	core_regs->ctrl_reg = 0x2;

	/* Release Minicom */
	core_regs->ctrl_reg = DUT_TEST_DONE | DUT_UART_RELEASE;

  return 0;
}

#include <stdio.h>
#include <xparameters.h>
#include <time.h>
#include <xuartlite.h>
#include <xuartlite_l.h>
#include <xio.h>
#include "ring_osc.h"

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

typedef struct
{
	unsigned int ctrl_reg;
	unsigned int status_reg;
	unsigned int ver_reg;
} control_core_regs;


int main()
{

	control_core_regs *core_regs = (control_core_regs*)(XPAR_DUT_CONTROL_CORE_0_BASEADDR);

	unsigned int *uart_status    = (unsigned int*)(XPAR_UARTLITE_0_BASEADDR + 0x8);

	/* Assert DUT_UART_REQUEST to get access to Minicom */
	core_regs->ctrl_reg = DUT_UART_REQUEST;

	/* Wait until PUF Power Board Grants access to Minicom */
	while ((core_regs->status_reg & DUT_UART_GRANTED) != DUT_UART_GRANTED) ;

	unsigned int* uart_lite_inst = XIo_In32(XPAR_RS232_UART_1_BASEADDR);

	char c;
	xil_printf("Characterizing Ring Oscillator\r\n");
	xil_printf("-------------- ---- ----------\r\n\r\n");

	xil_printf("Enter a time to run the ring oscillator for (in seconds): ");

	unsigned int value;
	int i = 0;

	int seconds = 0;

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


	xil_printf("\r\n Running ring oscillator for %d seconds\r\n", seconds);
	xil_printf("Starting ring oscillator...\r\n");
	RING_OSC_mWriteReg(XPAR_RING_OSC_0_BASEADDR, RING_OSC_SLV_REG1_OFFSET, 0xFFFFFFFF);

	//xil_printf("Wrote value to register\r\n");
	while(1)
	{
		//spin until done counting
	}

	c = getchar();

	if (c == 'q')
	{
		xil_printf("done\r\n");
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

#include <stdio.h>
#include <xparameters.h>
#include <xuartlite.h>
#include <xuartlite_l.h>
#include <xio.h>
#include "ring_osc.h"
#include "xtmrctr.h"
#include "xintc_l.h"
#include <string.h>
#include <stdint.h>
#include <stdlib.h>

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

#define RELOAD				7
#define CLK_TICKS			100000000
#define NUM_RING_OSCILLATORS 2
#define CHARACTERIZE_TIME	15
//#define CHARACTERIZE_TIME	1
#define INTERVAL			15*CLK_TICKS
#define MAX_BUF 1024

//Timer defines
#define TIMER0_CONTROL	0x00000000
#define TIMER0_LOAD		0x00000004
#define TIMER0_COUNTER	0x00000008
#define TIMER1_CONTROL	0x00000010
#define TIMER1_LOAD		0x00000014
#define TIMER1_COUNTER	0x00000018

#define	TIMER1_LOAD_BIT		0x00000010
#define ENABLE_TIMERS_BIT	0x00000200

extern char inbyte(void);
extern void outbyte(char c);

XTmrCtr timer0;
XTmrCtr* timer0ptr = &timer0;
int timerReloadCount = 0;
int seconds = 0;

int numIntervals = 0;
int currentInterval = 0;

volatile int done = 0;

unsigned long values[RELOAD][NUM_RING_OSCILLATORS];

void printValue(unsigned long value);
void determineIntervals(int seconds);
void printAllValues();

typedef struct
{
	unsigned int ctrl_reg;
	unsigned int status_reg;
	unsigned int ver_reg;
} control_core_regs;

void interrupt_handler_dispatcher(void* ptr) {
	//Checking the timer interrupt
	xil_printf("in dispatcher\r\n");
	int intc_status = XIntc_GetIntrStatus(XPAR_INTC_0_BASEADDR);
	if (intc_status & XPAR_XPS_TIMER_0_INTERRUPT_MASK)
	{
		XIntc_AckIntr(XPAR_INTC_0_BASEADDR, XPAR_XPS_TIMER_0_INTERRUPT_MASK);
		currentInterval++;
		if (currentInterval == numIntervals)
		{
			currentInterval = 0;
			RING_OSC_mWriteReg(XPAR_RING_OSC_1_BASEADDR, RING_OSC_SLV_REG2_OFFSET, 0); //Reset
			RING_OSC_mWriteReg(XPAR_RING_OSC_2_BASEADDR, RING_OSC_SLV_REG2_OFFSET, 0); //Reset
			values[timerReloadCount][0] = RING_OSC_mReadSlaveReg0(XPAR_RING_OSC_1_BASEADDR, RING_OSC_SLV_REG0_OFFSET);
			values[timerReloadCount][1] = RING_OSC_mReadSlaveReg0(XPAR_RING_OSC_2_BASEADDR, RING_OSC_SLV_REG0_OFFSET);
			if (timerReloadCount < RELOAD)
			{
				timerReloadCount++;
				if (timerReloadCount == RELOAD)
				{
					//xil_printf("done.\r\n");
					done = 1;
				}
				else
				{
					RING_OSC_mWriteReg(XPAR_RING_OSC_1_BASEADDR, RING_OSC_SLV_REG2_OFFSET, 0xFFFFFFFF); //start
					RING_OSC_mWriteReg(XPAR_RING_OSC_2_BASEADDR, RING_OSC_SLV_REG2_OFFSET, 0xFFFFFFFF); //start
					XTmrCtr_Start(timer0ptr, XPAR_XPS_TIMER_0_DEVICE_ID);
				}
			}
			else
			{
				//xil_printf("done.\r\n");
				done = 1;
			}
		}
		else
		{
			XTmrCtr_Start(timer0ptr, XPAR_XPS_TIMER_0_DEVICE_ID);
		}
	}
}

int main()
{
	xil_printf("hELLO wORLD\r\n");
	//Interrupts setup
	microblaze_register_handler(interrupt_handler_dispatcher, NULL);
	XIntc_EnableIntr(XPAR_INTC_0_BASEADDR, XPAR_XPS_TIMER_0_INTERRUPT_MASK);
	XIntc_MasterEnable(XPAR_INTC_0_BASEADDR);
	microblaze_enable_interrupts();

	control_core_regs *core_regs = (control_core_regs*)(XPAR_DUT_CONTROL_CORE_0_BASEADDR);

	/* Assert DUT_UART_REQUEST to get access to Minicom */
	core_regs->ctrl_reg = DUT_UART_REQUEST;

	/* Wait until PUF Power Board Grants access to Minicom */
	while ((core_regs->status_reg & DUT_UART_GRANTED) != DUT_UART_GRANTED);

	//Initialize the timer
	XStatus Status;
	Status = XTmrCtr_Initialize(timer0ptr, XPAR_XPS_TIMER_0_DEVICE_ID);

	//Initialize the Software Registers
	RING_OSC_mWriteReg(XPAR_RING_OSC_0_BASEADDR, RING_OSC_SLV_REG2_OFFSET, 0); //Reset
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

	xil_printf("Characterizing Ring Oscillator\r\n");

	//xil_printf("Enter a time to run the ring oscillator (divisible by 15 seconds): ");

	seconds = CHARACTERIZE_TIME;

	determineIntervals(seconds);

	//Configuring the timer
	XTmrCtr_SetOptions(timer0ptr, XPAR_XPS_TIMER_0_DEVICE_ID, (XTC_AUTO_RELOAD_OPTION|XTC_INT_MODE_OPTION|XTC_DOWN_COUNT_OPTION));
	XTmrCtr_SetResetValue(timer0ptr, XPAR_XPS_TIMER_0_DEVICE_ID, INTERVAL);

	RING_OSC_mWriteReg(XPAR_RING_OSC_1_BASEADDR, RING_OSC_SLV_REG2_OFFSET, 0xFFFFFFFF);
	RING_OSC_mWriteReg(XPAR_RING_OSC_2_BASEADDR, RING_OSC_SLV_REG2_OFFSET, 0xFFFFFFFF);

	XTmrCtr_Start(timer0ptr, XPAR_XPS_TIMER_0_DEVICE_ID);

	xil_printf("Starting ring oscillator...\r\n");

	while (!done)
	{
		//Waiting for the timer interrupt
	}

	printAllValues();
	//Write the disable to the ring oscillators!
	Xil_Out32(XPAR_RING_OSC_1_BASEADDR, 0x0);
	Xil_Out32(XPAR_RING_OSC_2_BASEADDR, 0x0);
	exit(1);
	return 0;
}

void printAllValues()
{
	int i;

	xil_printf("Test Region\r\n");
	for (i = 0; i < RELOAD; i++)
	{
		printValue(values[i][0]);
	}

	xil_printf("Control Region\r\n");
	for (i = 0; i < RELOAD; i++)
	{
		printValue(values[i][1]);
	}
	xil_printf("done.");
}

void printValue(unsigned long value)
{
	char buf[MAX_BUF];
	uint16_t digitIndex = 0;

	while (value != 0)
	{
		uint16_t digit = value % 10;
		buf[digitIndex] = '0' + digit;
		value -= digit;
		value /= 10;
		digitIndex++;
	}
	buf[digitIndex] = '\0';
	// Print out in backwards order.
	int i;
	for (i=digitIndex-1; i>=0; i--)
	{
		xil_printf("%c", buf[i]);
	}
	xil_printf("\r\n");
}

void determineIntervals(int seconds)
{
	numIntervals = seconds / 15;
}

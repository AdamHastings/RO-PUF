#include <stdio.h>
#include <xparameters.h>

// Date:       Version:  Developer:  Notes:
// 2014/01/06  1.0       aschmidt    Created and works with HW v1.0

#define SW_VERSION         "1.0"
#define UART_TX_FIFO_EMPTY 0x00000004
#define SW_RESET           0x00000001
#define DUT_UART_REQUEST   0x00000002
#define DUT_UART_GRANTED   0x00000004
#define DUT_UART_RELEASE   0x00000008
#define DUT_TEST_DONE      0x00000010

typedef struct {
  unsigned int ctrl_reg;
  unsigned int status_reg;
  unsigned int ver_reg;
}control_core_regs;


int main() {
  char c;
  unsigned int hw_ver_reg;
  unsigned int hw_major_ver;
  unsigned int hw_minor_ver;
  control_core_regs *core_regs = (control_core_regs*)(XPAR_DUT_CONTROL_CORE_0_BASEADDR);
  unsigned int *uart_status    = (unsigned int*)(XPAR_UARTLITE_0_BASEADDR + 0x8);

  hw_ver_reg   = core_regs->ver_reg;
  hw_major_ver = hw_ver_reg >> 16;
  hw_minor_ver = (0x0000FFFF & hw_ver_reg);

  /* Assert DUT_UART_REQUEST to get access to Minicom */
  core_regs->ctrl_reg = DUT_UART_REQUEST;

  /* Wait until PUF Power Board Grants access to Minicom */
  while ((core_regs->status_reg & DUT_UART_GRANTED) != DUT_UART_GRANTED) ;

  xil_printf("****************************************************\n\r");
  xil_printf("**      Starting V4LX60 DUT Test Application      **\n\r");
  xil_printf("**               SW Version: %s                  **\n\r",SW_VERSION);
  xil_printf("**               HW Version: %d.%d                  **\n\r",hw_major_ver, hw_minor_ver);
  xil_printf("****************************************************\n\r");

  xil_printf("Testing V4LX60 FPGA with MicroBlaze\n\r");
  xil_printf("Press ENTER to Continue\n\r");
  c = getchar();
  xil_printf("Hello World: %c\n\r", c);

  xil_printf("Status Register (0x%08x) = 0x%08x\n\r", &core_regs->status_reg, core_regs->status_reg);

  /* Set Test Done */
  xil_printf("Press ENTER to set DUT Test Done\n\r");
  c = getchar();
  core_regs->ctrl_reg |= DUT_TEST_DONE;
  xil_printf("Status Register (0x%08x) = 0x%08x\n\r", &core_regs->status_reg, core_regs->status_reg);

  /* Release Minicom */
  xil_printf("Test Complete, Preparing to Release Minicom Access\n\r");
  xil_printf("\n\r");
  xil_printf("****************************************************\n\r");
  xil_printf("**      Finished V4LX60 DUT Test Application      **\n\r");
  xil_printf("****************************************************\n\r");
  xil_printf("\n\r");
  xil_printf("\n\r");
  fflush(stdout);

  /* Wait until UART finishes printing */
  while ((*uart_status & UART_TX_FIFO_EMPTY) != UART_TX_FIFO_EMPTY) ;
  core_regs->ctrl_reg = 0x2;

  /* Release Minicom */
  core_regs->ctrl_reg = DUT_TEST_DONE | DUT_UART_RELEASE;

  return 0;
}

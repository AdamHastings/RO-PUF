`timescale 1ns/100ps

import my_package::*;

interface ahb_if(input bit HCLK);
   logic [20:0]   HADDR;
   logic 	  HWRITE;
   logic [1:0] 	  HTRANS;
   logic [7:0] 	  HWDATA;
   logic [7:0] 	  HRDATA;

   clocking cb @(posedge HCLK);
      output 	  HADDR;
      output 	  HWRITE;
      output 	  HTRANS;
      output 	  HWDATA;
      input 	  HRDATA;
   endclocking
      
   modport TEST(clocking cb);
   modport DUT(input HCLK,
	       input HADDR,
	       input  HWRITE,
	       input  HTRANS,
	       input  HWDATA,
	       output HRDATA);
   
endinterface // ahb_if   
   
interface sram_if();
   logic 	  CE_b;
   logic 	  WE_b;
   logic 	  OE_b;
   logic [20:0]   A;
   wire [7:0] 	  DQ;
endinterface // sram_if

module top();

   bit clk;
   wire rst;
   
   always #10ns clk = ~clk;
 
   ahb_if ahb_bus(clk);
   sram_if sram_bus();

   //Test Program
   test test_bench(ahb_bus.TEST, rst);
   //DUT
   sram_control sram_ctl(ahb_bus, sram_bus, rst);
   //Memory Model
   async memory(.CE_b(sram_bus.CE_b),
		.WE_b(sram_bus.WE_b),
		.OE_b(sram_bus.OE_b),
		.A(sram_bus.A),
		.DQ(sram_bus.DQ));
      
endmodule 
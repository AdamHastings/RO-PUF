`default_nettype none
interface SPM_IF #(ADDRESS_WIDTH=8) (input bit clk);
   
   bit rst; // Active low asynchronous reset
   bit [8-1: 0] data_out; // Read data from memory unit
   logic [ADDRESS_WIDTH-1: 0] address;  // Address to read/write 
   logic [8-1: 0] data_in;  // Write data to memory unit 
   logic  	  write;    // Write flag to memory unit 

   clocking cb @(posedge clk);
      output 		rst;
      output 		data_out;
      input 		address;
      input 		data_in;
      input 		write;
   endclocking // cb

   modport TEST(clocking cb);
   modport DUT(input rst, data_out,
	       output address, data_in, write);
endinterface
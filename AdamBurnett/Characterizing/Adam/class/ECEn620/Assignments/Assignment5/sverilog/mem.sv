`default_nettype none
`timescale 1ns/100ps 

interface mem_if(input bit clk);
   logic write;
   logic read;
   logic [7:0]  data_in;
   logic [15:0] address;
   logic [8:0]  data_out; 
   
   clocking cb @(posedge clk); 
     output write; 
     output read;
     output address;
     input data_out; 
     output data_in;
   endclocking
   
   modport TEST(clocking cb); 
   modport DUT(input clk, write, read, address, data_in, parity, 
               output data_out); 
      
   read_write_sig_assertion: assert property (@(posedge clk) !(cb.read==1 && cb.write==1)); 
         
        
   ///////////////////////////
   //    Even Parity Calc.  //
   ///////////////////////////
   function logic parity(logic [7:0] din); 
     return (^din); 
   endfunction
   
endinterface

module my_mem(mem_if.DUT mem_bus);

   // Declare a 9-bit associative array using the logic data type
   logic [8:0] mem_array[shortint];

   always @(posedge mem_bus.clk) begin
      if (mem_bus.write) begin
        mem_array[mem_bus.address] = {mem_bus.parity(mem_bus.data_in), mem_bus.data_in};
      end else if (mem_bus.read)
        mem_bus.data_out =  #75ns mem_array[mem_bus.address];
   end

endmodule
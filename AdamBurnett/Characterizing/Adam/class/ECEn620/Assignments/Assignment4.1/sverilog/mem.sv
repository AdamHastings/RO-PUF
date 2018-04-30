`default_nettype none

interface mem_if(input bit clk);
   logic write;
   logic read;
   logic [7:0] data_in;
   logic [15:0] address;
   logic [8:0] data_out; 
   
   clocking cb @(posedge clk); 
     output write;
     output read;
     output data_in; 
     output address;
     input data_out;
   endclocking     
   
   ///////////////////////////
   //     Read Function     //
   ///////////////////////////
   //Note: does not return read value    
   function void mread(logic [15:0] addr);
    read = 1;
    address = addr;
   endfunction 
   
   ///////////////////////////
   //    Write Function     //
   ///////////////////////////
   function void mwrite(logic [15:0] addr, logic [7:0] data);
    write = 1;
    data_in = data;
    address = addr;
   endfunction       
      
   ///////////////////////////
   //Test Read Write Checker//
   ///////////////////////////
   always @(posedge clk) ReadWriteChecker(read, write); 
    
   ///////////////////////////
   //   Read Write Checker  //
   ///////////////////////////
   int rdwr_error_count;
   function  logic ReadWriteChecker(logic rd, logic wr);
     if(rd == 1 && wr == 1) begin
       $display("Error@%d: Read and Write asserted at the same time\n", $time);
       rdwr_error_count++; 
     end 
   endfunction 
        
   ///////////////////////////
   //    Even Parity Calc.  //
   ///////////////////////////
   function logic parity(logic [7:0] din); 
     return (^din); 
   endfunction
   
endinterface


module my_mem(mem_if mem_bus);

   // Declare a 9-bit associative array using the logic data type
   logic [8:0] mem_array[shortint];

   always @(posedge mem_bus.clk) begin
      //$display("Clk Event mem_bus.write=%d time: %d", mem_bus.write, $time);
      if (mem_bus.write) begin
        //$display("Writing: %04x to %04x\n", {ev_parity(mem_bus.data_in), mem_bus.data_in}, mem_bus.address);  
        mem_array[mem_bus.address] = {mem_bus.parity(mem_bus.data_in), mem_bus.data_in};
      end else if (mem_bus.read)
        mem_bus.data_out =  mem_array[mem_bus.address];
   end

endmodule
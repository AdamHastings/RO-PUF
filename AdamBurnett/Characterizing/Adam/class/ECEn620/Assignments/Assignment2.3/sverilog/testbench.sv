`default_nettype none
`timescale 1ns/100ps 
module testbench (); 

  logic clk;
  logic write;
  logic read;
  logic [7:0] data_in;
  logic [15:0] address;
  logic [8:0] data_out; 
    
  int error_counter; 
    
  typedef struct {
    shortint address; 
    byte data_to_write;
    byte expected_read;
    byte actual_read; 
  } rd_wr_transaction; 
  
  rd_wr_transaction RdWrArray[]; 
  shortint addr;
  byte data;

  initial begin
    clk = 0;
    error_counter = 0; 
    read = 0;
    RdWrArray = new[6];
    
    foreach(RdWrArray[i]) begin
      addr = $random;
      data = $random;
      RdWrArray[i].address = addr; 
      RdWrArray[i].data_to_write = data;
      RdWrArray[i].expected_read = {^data, data};
    end 
    
    foreach(RdWrArray[i]) begin
      write_to_memory(RdWrArray[i].address, RdWrArray[i].data_to_write);
    end 
       
    //Randomize the Ordering for the reads.    
    RdWrArray.shuffle();    
       
    foreach(RdWrArray[i]) begin
      @(negedge clk)
      read = 1; 
      address = RdWrArray[i].address;
      @(posedge clk)
      #1
      RdWrArray[i].actual_read = data_out;
      if(RdWrArray[i].expected_read != RdWrArray[i].actual_read) begin
        $display("Found Error: Expected: %03X Actual: %03X", RdWrArray[i].expected_read, RdWrArray[i].actual_read); 
        error_counter = error_counter + 1; 
      end      
    end
   
   $display("Print Read Values");
   foreach(RdWrArray[i]) begin
    $display("\t%03X", RdWrArray[i].actual_read);
   end 
   
   $display("Error Count: %d", error_counter); 
    
  end  
  
  task write_to_memory(input shortint addr, byte data);
    write = 1;
    data_in = 0;
    address = addr; 
    @(posedge clk);
     write = 0; 
    @(negedge clk); 
  endtask;
  
  always begin
    #5 clk = ~clk;
  end 
     
  my_mem mem(clk,
	     write,
	     read,
	     data_in,
	     address,
	     data_out);
   
endmodule
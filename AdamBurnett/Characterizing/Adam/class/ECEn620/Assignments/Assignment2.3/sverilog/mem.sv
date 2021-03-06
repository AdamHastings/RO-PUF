`default_nettype none
  module my_mem(clk,
		write,
		read,
		data_in,
		address,
		data_out);

input  logic clk;
input  logic write;
input  logic read;
input  logic [7:0] data_in;
input  logic [15:0] address;
output logic [8:0] data_out;

   // Declare a 9-bit associative array using the logic data type
   logic [8:0] mem_array[shortint];

   always @(posedge clk) begin
      if (write)
        mem_array[address] = {^data_in, data_in};
      else if (read)
        data_out =  mem_array[address];
   end

endmodule
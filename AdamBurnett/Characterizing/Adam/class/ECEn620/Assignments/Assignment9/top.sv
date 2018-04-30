`default_nettype none
  
module top();
   bit clk = 0;
   always #5 clk = ~clk;

   string instr;
   string state;
   
   always_comb begin
      case($root.top.DUT.Processor.instruction[7:4])
	NOP: instr = "NOP";
	ADD: instr = "ADD";
	SUB: instr = "SUB";
	AND: instr = "AND";
	NOT: instr = "NOT";
	RD:  instr = "RD";
	WR: instr = "WR";
	BR: instr = "BR";
	BRZ: instr = "BRZ";
	RDI: instr = "RDI";
	HALT: instr = "HALT";
      endcase // case (I.byte0[7:0]
   end
   
    always_comb begin
      case($root.top.DUT.Controller.state)
	0: state = "idle";
	1: state = "fet1";
	2: state = "fet2";
	3: state = "dec";
	4: state = "ex1";
	5: state = "rd1";
	6: state = "rd2";
	7: state = "wr1";
        8: state = "wr2";
	9: state = "br1";
	10: state = "br2";
	11: state = "halt";
	default state = "unknown";
      endcase 
   end

   
   SPM_IF mem_bus(clk);
      
   test TEST(mem_bus);
   RISC_SPM DUT(.clk(clk), 
		.rst(mem_bus.rst),
		.data_out(mem_bus.data_out),
		.address(mem_bus.address),
		.data_in(mem_bus.data_in),
		.write(mem_bus.write));       
endmodule


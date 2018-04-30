
class Driver_cbs #(ADDRESS_WIDTH=8);
   virtual task pre_inst(ref Instruction #(ADDRESS_WIDTH) I);
   endtask // pre_inst
   virtual task post_inst(ref Instruction #(ADDRESS_WIDTH) I);
   endtask // post_inst
   
endclass // Driver_cbs

class Driver #(ADDRESS_WIDTH=8);
   virtual SPM_IF #(ADDRESS_WIDTH).TEST dut_if;
   Driver_cbs #(ADDRESS_WIDTH) callbacks[$];
   ScoreBoard #(ADDRESS_WIDTH) Scb;
   mailbox #(Instruction #(ADDRESS_WIDTH)) mbx;
   event   handshake;
   
   function new (virtual SPM_IF #(ADDRESS_WIDTH).TEST dif,
		 mailbox #(Instruction #(ADDRESS_WIDTH)) mb, event hs);
      dut_if = dif;
      mbx = mb;
      Scb = new ();
      handshake = hs;
   endfunction // new
      
   function automatic void initialize();
      dut_if.cb.rst <= 1;
      dut_if.cb.data_out <= 8'h0;
   endfunction      
   
   task automatic reset();
      initialize();
      @dut_if.cb;
      dut_if.cb.rst <= 1;
      @dut_if.cb;
      @dut_if.cb;
      dut_if.cb.rst <= 0;
      repeat (4) @dut_if.cb;
      dut_if.cb.rst <= 1;
      repeat (1) @dut_if.cb;
   endtask      
     
   function automatic string getOpcode(Instruction #(ADDRESS_WIDTH) I);
      case(I.byte0[7:4]) 
	NOP: return "NOP";
	ADD: return "ADD";
	SUB: return "SUB";
	AND: return "AND";
	NOT: return "NOT";
	RD:  return "RD";
	WR:  return "WR";
	BR:  return "BR";
	BRZ: return "BRZ";
	RDI: return "RDI";
	HALT: return "HALT";
      endcase // case (I.byte0[7:4])
   endfunction // string
   
   
   task automatic run(int count);
     Instruction #(ADDRESS_WIDTH) I;
      int i = 0;
      
     repeat(count) begin
	mbx.get(I);
	foreach(callbacks[i]) callbacks[i].pre_inst(I);
	sendInstruction(I,i);
	foreach(callbacks[i]) callbacks[i].post_inst(I);
	->handshake;
	i++;
     end // repeat (count)

      if ($root.top.DUT.Controller.state == 4'd11) begin
	 $display("Error: Processor has halted");
      end
      
   endtask // run

   task automatic sendInstruction(input Instruction #(ADDRESS_WIDTH) I, int i);
      	$display("@%0d: Starting Instr #%0d :: %s", $time, i, getOpcode(I));
	//Fetch 1
	Scb.fetch1();
	@dut_if.cb;
	
	//Fetch 2: Get and Drive I.byte0
  	
	Scb.fetch2(I);
        
	dut_if.cb.data_out <= I.byte0;
	@dut_if.cb;
	//Decode:	
	Scb.check_address(dut_if.cb.address);	
        Scb.check_pc($root.top.DUT.Processor.PC_count);
	Scb.check_ir($root.top.DUT.Processor.instruction);
	
	Scb.decode(I,8'hff);

	case (I.byte0[7:4])
	  NOP: doNOP(I);
	  
	  ADD, SUB, AND: doAddSubAnd(I);
	  NOT: doNot(I);
	  RD, RDI: doRdRdi(I,8'hff);
	  WR: doWr(I);
	  BR, BRZ, HALT: doBrBrzHalt(I);
	endcase // case (I.byte0[7:4])
   endtask // sendInstruction
   
   
   task automatic doAddSubAnd(input Instruction #(ADDRESS_WIDTH) I);
      $display("Decoded AddSubAnd Instr");
      //Leave Decode State
      @dut_if.cb;
      //Leave S_ext1
      @dut_if.cb;
      Scb.check_regs($root.top.DUT.Processor.R0_out,
		     $root.top.DUT.Processor.R1_out,
		     $root.top.DUT.Processor.R2_out, 
		     $root.top.DUT.Processor.R3_out);
   endtask // AddSubAnd

   task automatic doNOP(input Instruction #(ADDRESS_WIDTH) I);
      $display("Decoded NOP");
      @dut_if.cb;
   endtask // doNOP
   
   
   task automatic doNot(input Instruction #(ADDRESS_WIDTH) I);
      $display("Decoded NOT");
      //Leave Decode State
      @dut_if.cb;
      Scb.check_regs($root.top.DUT.Processor.R0_out,
		     $root.top.DUT.Processor.R1_out,
		     $root.top.DUT.Processor.R2_out, 
		     $root.top.DUT.Processor.R3_out);
   endtask // doNot

   task automatic doRdRdi(input Instruction #(ADDRESS_WIDTH) I, byte rdval);
      $display("Decoded RdRdi Instr");
      //Leave Decode State
      @dut_if.cb;
      dut_if.cb.data_out <= I.byte1;
      
      if(I.byte0[7:4] == RD) begin
	 //Leave RD1
	 @dut_if.cb;
	 dut_if.cb.data_out <= rdval;
	 Scb.check_address(dut_if.cb.address);
	 //Leave RD2
	 @dut_if.cb;
	 Scb.check_address(dut_if.cb.address);
      end
      else begin //RDI 
	//Leave RD1
	@dut_if.cb;	 
	Scb.check_address(dut_if.cb.address);
      end // else: !if(I.byte0[7:4] == RD)
      Scb.check_regs($root.top.DUT.Processor.R0_out,
		     $root.top.DUT.Processor.R1_out,
		     $root.top.DUT.Processor.R2_out, 
		     $root.top.DUT.Processor.R3_out);
   endtask // doRdRdi

   task automatic doWr(input Instruction #(ADDRESS_WIDTH) I);
      $display("Decoded Wr");
      //Leave Decode State 
      @dut_if.cb;
      //Leave WR1
      dut_if.cb.data_out <= I.byte1;
      @dut_if.cb;
      Scb.check_address(dut_if.cb.address);      
      //Leave WR2
      @dut_if.cb;
      Scb.check_address(dut_if.cb.address);
      Scb.check_dataIn(dut_if.cb.data_in);
   endtask
    
   task automatic doBrBrzHalt(input Instruction #(ADDRESS_WIDTH) I);
      $display("Error: Encountered Opcode %d", I.byte0[7:4]);
      $display("This Opcode is not yet supported by the Test Bench Env");    
      $finish;
   endtask
endclass;
 
      
   
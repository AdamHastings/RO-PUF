
class Driver;
   virtual SPM_IF.TEST dut_if;
   ScoreBoard Scb;
   mailbox #(Instruction) mbx;
   event   handshake;
   
   function new (virtual SPM_IF.TEST dif,
		 mailbox #(Instruction) mb, event hs);
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
     
   function automatic string getOpcode(Instruction I);
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
     Instruction I;
      int i = 0;
      
     repeat(count) begin

	mbx.get(I);	
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
	  ADD, SUB, AND: doAddSubAnd(I);
	  NOT: doNot(I);
	  RD, RDI: doRdRdi(I,8'hff);
	  WR: doWr(I);
	  BR, BRZ, HALT: doBrBrzHalt(I);
	endcase	     

	->handshake;
	i++;
     end // repeat (count)

      if ($root.top.DUT.Controller.state == 4'd11) begin
	 $display("Error: Processor has halted");
      end
      
   endtask // run

   task automatic doAddSubAnd(input Instruction I);
      //$display("Decoded AddSubAnd Instr");
      //Leave Decode State
      @dut_if.cb;
      //Leave S_ext1
      @dut_if.cb;
      Scb.check_regs($root.top.DUT.Processor.R0_out,
		     $root.top.DUT.Processor.R1_out,
		     $root.top.DUT.Processor.R2_out, 
		     $root.top.DUT.Processor.R3_out);
   endtask // AddSubAnd

   task automatic doNot(input Instruction I);
      //$display("Decoded NOT");
      //Leave Decode State
      @dut_if.cb;
      Scb.check_regs($root.top.DUT.Processor.R0_out,
		     $root.top.DUT.Processor.R1_out,
		     $root.top.DUT.Processor.R2_out, 
		     $root.top.DUT.Processor.R3_out);
   endtask // doNot

   task automatic doRdRdi(input Instruction I, byte rdval);
      //$display("Decoded RdRdi Instr");
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

   task automatic doWr(input Instruction I);
      //$display("Decoded Wr");
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
    
   task automatic doBrBrzHalt(input Instruction I);
      $display("Error: Encountered Opcode %d", I.byte0[7:4]);
      $display("This Opcode is not yet supported by the Test Bench Env");    
      $finish;
   endtask
endclass;
 
      
   
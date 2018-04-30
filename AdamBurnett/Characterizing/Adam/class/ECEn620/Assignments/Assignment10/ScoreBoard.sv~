class ScoreBoard;
   int ErrorCounter;
   
   bit [7:0] pc;
   bit [7:0] ir;
   bit [7:0] r[4];
   byte DataInQueue[$];
   byte AddressQueue[$];
   
   function new ();
      ErrorCounter = 0;
      pc = 0;
      ir = 0;
      r[0]=0; r[1]=0; r[2]=0; r[3]=0;
   endfunction // new

   function automatic void incr_pc();
      pc++;
      $display("incr pc: %02h", pc);
   endfunction // incr_pc

   function automatic void update_pc(int val);
      pc = val;
   endfunction // update_pc

   function automatic void update_ir(Instruction I);
      ir = I.byte0;
   endfunction // update_ir

   function automatic void update_addrq(input bit [7:0] v);
      AddressQueue.push_back(v);
      $display("Updating Address Queue: %02h %p", v, AddressQueue);
      
   endfunction // update_addrq
   
   
   function automatic void fetch1();
      //The value of the pc should next appear on the
      // address line.
      $display("@%t:fetch1: %02h", $time, pc);
      update_addrq(pc);
   endfunction // fetch1			      
   
   function automatic void fetch2(Instruction I);
      $display("@%t:fetch2", $time);
      update_ir(I);
      incr_pc();
   endfunction // fetch2

   function automatic void readbyte2(Instruction I);
      $display("readbyte2 I.byte1=%02x", I.byte1);
      update_addrq(pc);
      update_addrq(I.byte1);    
      incr_pc();
   endfunction // readbyte2
            
   function automatic void decode(Instruction I, bit [7:0] rdval);
      
      bit [3:0] opcode = I.byte0[7:4];
      bit [1:0] src = I.byte0[3:2];
      bit [1:0] dst = I.byte0[1:0];
      $display("@%0d SB: Decode", $time);  
      case (I.byte0[7:4])
	NOP: ;
	ADD: r[dst] = r[src] + r[dst];
	SUB: r[dst] = r[dst] - r[src];
	AND: r[dst] = r[src] & r[dst];
	NOT: r[dst] = ~r[src];
	RD: begin  
	   r[dst] = rdval;
	   readbyte2(I);
	end
	RDI:begin
	   update_addrq(pc);
	   incr_pc();
	   r[dst] = I.byte1;	   
	end
	WR: begin
	   readbyte2(I);
	   DataInQueue.push_back(r[src]);
	end
	BR,BRZ,HALT: ;
      endcase // case (I.byte0)
   endfunction // decode

   function automatic void check_pc(bit [7:0] cpc);
      compare_value("PC", cpc, pc);
   endfunction // check_pc

   function automatic void check_ir(bit [7:0] cir);
      compare_value("IR", cir, ir);
   endfunction // check_ir

   function automatic void check_regs(bit [7:0] cr0,
				 bit [7:0] cr1,
				 bit [7:0] cr2,
				 bit [7:0] cr3);
      compare_value("r0", cr0, r[0]);
      compare_value("r1", cr1, r[1]);
      compare_value("r2", cr2, r[2]);
      compare_value("r3", cr3, r[3]);
   endfunction // check_regs

   function automatic void check_address(bit [7:0] caddress);
      compare_qvalue("address", "AddressQueue", caddress, AddressQueue);
   endfunction // check_address

   function automatic void check_dataIn(bit [7:0] cdataIn);
      compare_qvalue("cdataIn", "DataInQueue", cdataIn, DataInQueue);
   endfunction // check_address
   
   function automatic void compare_qvalue(string name, string qname, bit [7:0] actual, ref byte queue[$]);
      $display("Checking Q=%p", queue);
      if(queue.size() == 0)
	$display("Error: %s is empty!", qname);
      else
	compare_value(name, actual, queue.pop_front());
   endfunction // compare_qvalue
   
   function automatic void compare_value(string name, bit [7:0] actual, bit [7:0] expected);
      if(actual != expected) begin
	 $display("@%08d: Error: Found Unexpected Value for %s: Expected: %02X Actual: %02X", $time, name, expected, actual);
	 ErrorCounter++;
      end
      //else
	//$display("@%08d: %s is correct", $time, name);
      
   endfunction // compare_value
   
	     
	   
	
   
endclass
import TbEnvPkg::*;

Instruction cur_inst;
covergroup Cov();
   
   opcodes:coverpoint cur_inst.opcode {
      //1. All opcodes have been executed.
      bins ops[] = {[0:6], 9};
      illegal_bins bad_ops[] = {[7:8],[10:15]};
      //4. All opcodes have been preceeded and followed all other opcodes
      bins op_order[] = (0,1,2,3,4,5,6,9=>0,1,2,3,4,5,6,9);
   }
   
   srcs:coverpoint cur_inst.src {
      bins sregs[] = {[0:3]};
   }
   
   dsts:coverpoint cur_inst.dst {
      bins dregs[] = {[0:3]};		   
   }

   //2. The source for every opcode that has a source has been 0,1,2,3
   opcode_src:cross opcodes, srcs {
      ignore_bins no_src = binsof(opcodes.ops) intersect {0,5,9};
      ignore_bins trans = binsof(opcodes.op_order);
   }

   //3. The destination for every opcode that has a dst has been 0,1,2,3
   opcode_dst:cross opcodes, dsts {
      ignore_bins no_dst = binsof(opcodes.ops) intersect {0,6,9};
      ignore_bins trans = binsof(opcodes.op_order);
   }

   //5. Opcodes with both source and destination have had all combs of srcs and dsts
   opcode_src_dst:cross opcodes, srcs, dsts { 
      ignore_bins no_src_dst = binsof(opcodes.ops) intersect {0,5,6,9};
      ignore_bins trans = binsof(opcodes.op_order);
   }
   
   
   addresses: coverpoint cur_inst.byte1 iff (cur_inst.opcode != NOP &&
					cur_inst.opcode != ADD &&
					cur_inst.opcode != SUB &&
					cur_inst.opcode != AND &&
					cur_inst.opcode != NOT &&
					cur_inst.opcode != HALT){
      bins addrs[] = {[0:255]};		       
   }

   WrRdRdiMem:cross opcodes, addresses {
      //6 & 7: All mem locations written
      ignore_bins non_read_write = binsof(opcodes.ops) intersect {0,1,2,3,4};
      ignore_bins trans = binsof(opcodes.op_order);
   }
endgroup // Cov

class Driver_cbs_cov extends Driver_cbs;
   Cov ck;
   function new();
      ck=new();
   endfunction // new
   
   virtual task pre_inst(ref Instruction I);
      cur_inst = I;
      ck.sample();
   endtask // pre_inst
endclass // Driver_cbs_cov

  
program automatic test(SPM_IF.TEST mem_bus);
   initial begin
      static virtual SPM_IF.TEST vMemBus = mem_bus;
      Driver_cbs_cov cov_callback;
      Environment E;
      cov_callback = new();   
      E = new ();
      E.build(vMemBus);
      E.D.callbacks.push_back(cov_callback);
      E.run(50);

      $display("Test Complete found %d Errors.", E.D.Scb.ErrorCounter);
   end
endprogram // test

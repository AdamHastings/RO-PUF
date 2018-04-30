import my_package::*;

`define SV_RAND_CHECK(r) \
   do begin \
      if (!(r)) begin \
	 $display("%s:%0d: Randomization failed \"%s\"", \
		  `__FILE__, `__LINE__, `"r`");\
	 $finish; \
      end \
   end while(0)

program automatic test(ahb_if.TEST ahb_bus, output bit rst);
   AHB_CMD_PIPELINE AhbCmdPipe;
   AHB_SCOREBOARD Scoreboard;
   
   AHB_TRANSACTION T,A,D0, D1;
   
 
   initial begin
      AhbCmdPipe = new();
      Scoreboard = new();
      
      //Inject 10 Write to Memory
      for(int i=0; i<10; i++) begin
	 T = new();
	 `SV_RAND_CHECK(T.randomize() with {HWRITE == 1; HTRANS == NONSEQ; rst == 0; HADDR inside {[0:4]};});
	 AhbCmdPipe.inject(T);
      end

      //Inject Idle to Prevent Simulation model Error
      AhbCmdPipe.inject(AhbCmdPipe.Idle);
      
      //Inject 10 Read From Memory
      for(int i=0; i<10; i++) begin
	 T = new();
	 `SV_RAND_CHECK(T.randomize() with {HWRITE == 0; HTRANS == NONSEQ; rst == 0; HADDR inside {[0:4]};});
	 AhbCmdPipe.inject(T);
      end

      //Inject 10 Random AHB Transactions
      for(int i=0; i<10; i++) begin
	 T = new();
	 `SV_RAND_CHECK(T.randomize());
	 AhbCmdPipe.inject(T);
      end
      
      //Reset the Controller
      rst <= 1;
      @ahb_bus.cb;
      @ahb_bus.cb;
      @ahb_bus.cb;
      rst <= 0; 
      @ahb_bus.cb;
      
      //Main Test Loop
      for(int i=0; i<31; i++) begin
         //Advance the Driver Pipeline
	 AhbCmdPipe.advance();
	 A = AhbCmdPipe.AddressPhase;
         D0 = AhbCmdPipe.DataPhase0;
	 D1 = AhbCmdPipe.DataPhase1;
	 //$display("Address Phase @ time %d", $time);
	 //Apply/Receive SRAM Controller Stimulus
	 apply_receive_stimulus(A,D0,D1);
	 //Update Scoreboard and Check Read Data
	 if(rst == 0)
	  Scoreboard.update(D0);
	 if(D0.rst == 0 && D1.rst == 0)
          Scoreboard.check(D1);
	 //Advance the Clock
	 @ahb_bus.cb;
      end
         
      @ahb_bus.cb;
      @ahb_bus.cb;


      AHB_TRANSACTION::print_coverage_stats();
      
      
      Scoreboard.print_error_count();
      
      for(int i=0; i<5; i++) begin
	 //$display("mem_arry0: %d %d", i, $root/top/memory/mem_array0);         
      end
      
   end

   final begin
      $display("Program Complete");   
   end

   function void apply_receive_stimulus(ref AHB_TRANSACTION A,
					ref AHB_TRANSACTION D0,
					ref AHB_TRANSACTION D1);
      rst <= A.rst;
      
      //Apply Address Phase 
      ahb_bus.cb.HADDR <= A.HADDR;
      ahb_bus.cb.HTRANS<= A.HTRANS;
      ahb_bus.cb.HWRITE<= A.HWRITE;
      //Apply/Receive Data Phase
      ahb_bus.cb.HWDATA<= D0.HWDATA;
      D1.HRDATA = ahb_bus.cb.HRDATA;
        
   endfunction // apply_stimulus
   
   
endprogram // test
   
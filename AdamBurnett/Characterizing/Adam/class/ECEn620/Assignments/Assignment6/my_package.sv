package my_package;



enum {READ=0,WRITE=1} HWRITE_VALUE;
enum {IDLE=0,NONSEQ=2} HTRANS_VALUE;
   
   
class AHB_TRANSACTION;
   static int next_id = 0;
   static int stat_haddr[bit [20:0]];
   static int stat_htrans[bit [1:0]];
   static int stat_hwrite[bit];
   static int stat_rst_count=0;
   
   int id;
      
   rand bit [20:0] HADDR;
   rand bit [1:0] 	HTRANS;
   rand bit  	HWRITE;
   rand bit [7:0] 	HWDATA;   
   logic [7:0] 	HRDATA;
  
   rand bit 	rst;

   constraint cHADDR {HADDR dist {[0:4]:/40, [5:2097146]:/20, [2097147:2097151]:/40};};
   constraint cHTRANS {HTRANS == NONSEQ || HTRANS == IDLE;};
   constraint cRST {rst dist {0:=90, 1:=10};};
      
   function new();
      this.id = next_id++;
   endfunction // new

   function void post_randomize();
      if(this.rst)
        stat_rst_count++;
      stat_htrans[this.HTRANS]++;
      stat_haddr[this.HADDR]++;
      stat_hwrite[this.HWRITE]++;      
   endfunction // post_randomize
      
   function print();
      $display("AHB Transaction Id=%d HADDR=%d HWRITE=%d HWDATA=%d HRDATA=%d", this.id, this.HADDR, this.HWRITE, this.HWDATA, this.HRDATA);
   endfunction // print

   static function void print_coverage_stats();
      
      
      
      $display("Printing Coverage Stats");
      
      $display("\tReset Coverage: %0d of %0d transactions", stat_rst_count, next_id);
      
      $display("\tHTRANS Coverage (0=IDLE, 2=NONSEQ):");
      foreach(stat_htrans[i]) begin
	 $display("\t\t%0d: %0d", i,stat_htrans[i]);   
      end
      
      $display("\tHADDR Coverage:");
      foreach(stat_haddr[i]) begin
	 $display("\t\t%0d: %0d", i,stat_haddr[i]);   
      end
      
      $display("\tHWRITE Coverage:");
      foreach(stat_hwrite[i]) begin
	 $display("\t\t%0d: %0d", i,stat_hwrite[i]);
   
      end
   endfunction
   
endclass // AHBTransaction

class AHB_SCOREBOARD;
   int 		ErrorCount;
    
   AHB_TRANSACTION MemoryModel[bit [20:0]];

   function new ();
      ErrorCount = 0;
   endfunction

   function print_error_count();
      $display("Scoreboard Error Count @ time %0d: %0d", $time, this.ErrorCount);
   endfunction // print_error_count
     
   function void update(AHB_TRANSACTION T);
      if(T.HWRITE == WRITE && T.HTRANS != IDLE) begin
	 $display("Updating Scoreboard");
	 T.print();
	 this.MemoryModel[T.HADDR] = T;
      end
   endfunction // update_scoreboard

   function void check(AHB_TRANSACTION T);
      
      if(T.HWRITE == READ && T.HTRANS != IDLE) begin
	 $display("Checking:");
	 
	 if(this.MemoryModel.exists(T.HADDR)) begin
	    if(this.MemoryModel[T.HADDR].HWDATA != T.HRDATA) begin
	       ErrorCount++;
	       T.print();
	       $display("Unexpected Read Error @ time=%0d: Read Address: %0d Read Value: %0d Expected Read Value: %0d", $time, T.HADDR, T.HRDATA, MemoryModel[T.HADDR].HWDATA);
	    end
	 else
	   $display("Scoreboard Check Passed.");   
	 end
      end
   endfunction // check_results
endclass // ScoreBoard
   
class AHB_CMD_PIPELINE;
      
   AHB_TRANSACTION Idle;
   AHB_TRANSACTION AddressPhase;
   AHB_TRANSACTION DataPhase0;
   AHB_TRANSACTION DataPhase1;
   AHB_TRANSACTION CmdPipeline[$];
         
   function new();
      
      //Initialize Idle Packet
      Idle = new();
      Idle.HADDR = 0;
      Idle.HTRANS = 0;
      Idle.HWRITE = 1;
      Idle.HWDATA = 0;
      Idle.rst = 0;
      //Initialize Current Address and Data Packets
      //  
      AddressPhase = Idle;
      DataPhase0 = Idle;
      DataPhase1 = Idle;

   endfunction // new

   function void inject(AHB_TRANSACTION T);
      this.CmdPipeline.push_back(T);
   endfunction // enqueue_trans

   function void advance();
         
      DataPhase1 = DataPhase0;
      DataPhase0 = AddressPhase;
      if(CmdPipeline.size() > 0) 
	AddressPhase = CmdPipeline.pop_front();
      else
	AddressPhase = Idle;
      $display("Pipeline Advancing @ %d", $time());
      AddressPhase.print();
      DataPhase0.print();
      DataPhase1.print();
   endfunction // advance
   
endclass // AHB_DRIVER
   
endpackage // my_package
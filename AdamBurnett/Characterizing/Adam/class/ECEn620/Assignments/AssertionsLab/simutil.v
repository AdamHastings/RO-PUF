// This File: SIMUTIL.v
// Author:    Cliff Cummings
//            cliffc@sunburst-design.com
//            www.sunburst-design.com
// Last Modified: 07/05/2007
// Copyright 1996-2007 Cliff Cummings
//
// Version: 2.5 - Commented out Cadence SHM waveform dump commands
// Version: 2.4 - Modified Cadence SHM waveform dump commands
// Version: 2.3 - Modified STOPSIM to capture count values before they 
//                change at the end of the timestep
// Version: 2.2 - Changed $display & $fdiplay to $strobe & $fstrobe
//                Changed printMINTYPMAX to msg_MINTYPMAX
//                msg_MINTYPMAX is now called at the end of the tests
//                Created msg_tests_finish task to handle test reporting
// Version: 2.1 - Changed `cycle macro name to `CYCLE
//                Changed LOGFILE parameter name to SIMUTIL_LOGFILE
//                Added CLOCK_STYLE2 test for asymmetrical clock generation
//                Added TIME0_CLOCK_VALUE, legal vals are 0 (default) or 1
//                Changed clocking logic - free-running `CYCLE/2 is default
//                Added RC (Return Code - 0=PASS, 1=FAIL)
// Version: 2.0 - Added +define+FSDB for Debussy waveform dumping
// Version: 1.9 - Changed "type" to "sim_dly_type" in the printMINTYPMAX 
//                task. "type" is a new keyword in SystemVerilog
// Version: 1.8 - The LOGFILE is now called SIMUTIL.log
//                Added a FINISH task that finishes by default and 
//                stops when +define+STOP is invoked
// Version: 1.7 - Modified clk-forever block. Set clk = 1'b0 first
//                changed parameter: half_cycle1 to low_pulse_width
//                changed parameter: half_cycle2 to high_pulse_width
// Version: 1.6 - Added "clk" output with default 100ns cycle
//                parameters for half_cycle1 and half_cycle2
//                +define+cycle="<new value>" to change the cycle
//                Obsoleted INC_TESTCNT task
//                Changed +define+UT  to Veritools Undertow ($dumpvars)
//                Added   +define+UTX to Veritools Undertow ($vtdumpvars)
// Version: 1.5 - Removed START_TEST_LOGGER task
//                All test counters are now global variables
//                +define+LOG to write a LogFile.log
//                parameters for LOGFILE and FIELDWIDTH
//
// Permission is granted to use this SIMUTIL.v file
// as long as this header remains attached.
//---------------------------------------------------
//
// Simulation Utility Tasks
// parameter SIMUTIL_LOGFILE for the log file name
//   Verilog-1995 Use: #("filename")
//   Verilog-2001 Use: #(.SIMUTIL_LOGFILE("filename"))
//   to change the log file name
// +define+LOG to enable log file logging
//
// +define+CYCLE="<new value>" to change the cycle
// +define+TIME0_CLOCK_VALUE="1" to set clk=1 at time 0
//   (Legal values are 0 and 1)
// +define+CLOCK_STYLE2 for asymmetric clock pulses
// parameters for low_pulse_width and high_pulse_width if 
//   asymmetric clock pulses are required
//
// STARTSIM
// STOPSIM
// FINISH             +define+STOP (to $stop, else $finish)
// TESTS_FINISH       +define+RUN  (to $finish, else $stop)
//---------------------------------------------------
// Test Count, Pass and Error counter increment tasks
// ----------------
// INC_TESTCNT // SIMUTIL.v v1.6: This task is obsolete
// PASS        // SIMUTIL.v v1.6: increments the vector count
// ERROR       // SIMUTIL.v v1.6: increments the vector count
//---------------------------------------------------
// Simulation Utility Functions
// ----------------
// OpenFile
//---------------------------------------------------
// Dumpfile Options
// ----------------
// +define+SHM         // Cadence C-Waves or SimWave
// +define+SSCAN4      // SignalScan v4.0
// +define+SSCAN       // SignalScan v5.0
// +define+UT          // Veritools Undertow ($dumpvars)
// +define+UTX         // Veritools Undertow ($vtdumpvars)
// +define+VPD         // VirSim
// +define+VCD         // IEEE1364 VCD File
//---------------------------------------------------

`timescale 1ns/1ns
module SIMUTIL (clk);
  output clk;
  reg    clk;

  parameter SIMUTIL_INFORM_MSG_EN = 1;

  // These integers are global test counters & Return Code flag
  integer TESTNUM, TESTERR_CNT, TESTPAS_CNT, VECT_CNT, PASS_CNT, ERROR_CNT;
  reg     RC;
  event   genmsg; // triggers 1/2 CYCLE before $finish

  // These integers are file handles for log files
  integer LogFile, LF; 

  parameter SIMUTIL_LOGFILE = "SIMUTIL.log";
  parameter FIELDWIDTH = 15;

  // These are the vector capture-playback default file names
  parameter PAT_FILE = "vectors.pat";
  parameter EXP_FILE = "vectors.exp";
  parameter CNT_FILE = "vectors.f";

  // default return code is 1 (Fail)
  initial begin
    TESTERR_CNT = 0;
    TESTPAS_CNT = 0;
    RC = 1; // default return code is 1 (Fail)
  end

  //---------------------------------------------------
  // GENVEC opens files and creates a SAVE_VECTOR_CNT task
  //---------------------------------------------------
  `ifdef GENVEC
    integer su_stim_file;
    integer su_output_file;
    integer vcnt_file;

    initial begin
      su_stim_file   = T.OpenFile(PAT_FILE);
      su_output_file = T.OpenFile(EXP_FILE);
      vcnt_file      = T.OpenFile(CNT_FILE);
    end

    task SAVE_VECTOR_CNT;
      input [31:0] vcnt;
      $fstrobe(vcnt_file, "+define+MAXVCNT=\%d\"", vcnt);
    endtask
  `endif

  //---------------------------------------------------
  // START of Clock Generation
  //---------------------------------------------------
  `ifdef CYCLE
  `else // if `CYCLE not already defined, define it
    `define CYCLE 100
  `endif

  `ifdef TIME0_CLOCK_VALUE
  `else // if `TIME0_CLOCK_VALUE not already defined, define it to be 0
    `define TIME0_CLOCK_VALUE 0
  `endif

  //---------------------------------------------------
  // Test for legal time0 clock values
  //---------------------------------------------------
  initial
    if ((`TIME0_CLOCK_VALUE != 0) && (`TIME0_CLOCK_VALUE != 1)) begin
      $strobe("%m: ILLEGAL TIME0_CLOCK_VALUE=%b  -- must be 0 or 1",
               `TIME0_CLOCK_VALUE);
      $finish;
    end

  `ifdef CLOCK_STYLE2
    parameter low_pulse_width  = `CYCLE/2;
    parameter high_pusle_width = `CYCLE - low_pulse_width;
  
    initial begin
      if (`TIME0_CLOCK_VALUE == 0);
        clk <= 1'b0; #( low_pulse_width); // clk low
      else begin
        clk <= 1'b1; #(high_pusle_width); // clk high
        clk  = 1'b0; #( low_pulse_width); // clk low
      end
      forever begin
        clk = 1'b1; #(high_pusle_width); // clk high
        clk = 1'b0; #( low_pulse_width); // clk low
      end
    end
  `else
    //---------------------------------------------------
    // by default, use the following free-running clock 
    //---------------------------------------------------
    initial begin
      // NBA to ensure that any @(negedge clk) at time 0 triggers
      clk <= `TIME0_CLOCK_VALUE;
      forever #(`CYCLE/2) clk = ~clk;
    end
  `endif
  //---------------------------------------------------
  // END of Clock Generation
  //---------------------------------------------------

  initial begin
    LF = 1;   // The Log File is just STDOUT
    if ($test$plusargs("LOG")) begin
      LogFile = OpenFile(SIMUTIL_LOGFILE);
      LF = LogFile | 1;  // SIMUTIL_LOGFILE and STDOUT
    end
  end

  task STARTSIM;
    begin
      if (TESTNUM === 32'bx) TESTNUM = 1;
      else                   TESTNUM = TESTNUM + 1;
      VECT_CNT = 0; PASS_CNT = 0; ERROR_CNT = 0;
    end
  endtask

  task STOPSIM;
    integer ec, pc, vc, tn;
    begin // capture the count values before the end of the timestep
      {ec,pc,vc,tn}={ERROR_CNT, PASS_CNT, VECT_CNT, TESTNUM};
      if ((ERROR_CNT === 0)  && (VECT_CNT  !== 0)) begin
        TESTPAS_CNT = TESTPAS_CNT + 1;
        $fstrobe(LF, "\n   TEST PASSED - test #%0d:", tn,
                     " %0d vectors - %0d passed\n", vc, pc);
      end
      else begin
        TESTERR_CNT = TESTERR_CNT + 1;
        $fstrobe(LF, "\n***TEST FAILED - test #%0d:", tn,
                     " %0d vectors - %0d passed - %0d failed\n", vc, pc, ec);
      end
    end
  endtask

  task FINISH;
    begin
      @(clk);
      `ifdef GENVEC
        $fstrobe(LF, "\nWARNING: TEST VECTOR GENERATION ",
                       "WAS ENABLED (+define+GENVEC)\n");
      `else
        msg_tests_finish;
      `endif
      -> genmsg; // send genmsg event before $finish
      `ifdef STOP
        @(clk) $stop(2);
      `else
        @(clk) $fclose(LF);
               $finish(2);
      `endif
    end
  endtask

  task TESTS_FINISH;
    begin
      `ifdef RUN
        #1 $fclose(LF);
           $finish(2);
      `else
        #1 $stop(2);
      `endif
    end
  endtask

  task PASS;
    begin
      VECT_CNT  = VECT_CNT + 1;
      PASS_CNT  = PASS_CNT + 1;
    end
  endtask

  task ERROR;
    begin
      VECT_CNT  = VECT_CNT + 1;
      ERROR_CNT = ERROR_CNT + 1;
    end
  endtask

  function integer OpenFile;
    input [(32*8):1] FileName;    //up to 32 ASCII characters
    begin
      OpenFile = $fopen(FileName);
      if (OpenFile == 0) begin
        $fwrite(LF,"\n*** Could not open %0s file ***\n\n", FileName);
        $finish(2);
      end
    end
  endfunction

  initial 
    if ($test$plusargs("NS1"))
      $timeformat(-9,1,"ns",FIELDWIDTH);
    else if ($test$plusargs("NS2"))
      $timeformat(-9,2,"ns",FIELDWIDTH);
    else
      $timeformat(-9,0,"ns",FIELDWIDTH);

  task msg_MINTYPMAX;
    input [7*8:1] sim_dly_type;
    if ($test$plusargs("NO_SIMUTIL_MSG"));
    else $fstrobe(LF,
         "\n//--------------------------------------------------------\n",
           "// SIMUTIL Inform: Ran simulation with %0s delays\n",
                                                       sim_dly_type,
           "//--------------------------------------------------------\n");
  endtask

  task msg_tests_finish;
    reg [5*8:1] test; // string-variable for "test" or "tests"
    begin
      if (SIMUTIL_INFORM_MSG_EN == 1)
        if      ($test$plusargs("maxdelays")) msg_MINTYPMAX("MAXIMUM");
        else if ($test$plusargs("mindelays")) msg_MINTYPMAX("MINIMUM");
        else                                  msg_MINTYPMAX("TYPICAL");

      if (TESTNUM==1) test = "test";  // simgular
      else            test = "tests"; // plural
      if (TESTERR_CNT || !TESTPAS_CNT) begin
        RC = 1;
        $fstrobe(LF, "\n***TEST SUITE FAILED - %0d %0s", TESTNUM, test,
                     " - %0d passed - %0d failed",
                         TESTPAS_CNT, TESTERR_CNT);
      end
      else begin
        RC = 0;
        $fstrobe(LF, "\n***TEST SUITE PASSED - %0d %0s", TESTNUM, test,
                     " - %0d passed",
                         TESTPAS_CNT);
      end
    end
  endtask

  task V2K1_test;
    input [ 80*8:1] rptfile;
    input [ 80*8:1] model;
    input [160*8:1] info;
    integer         rpt;
    begin
      rpt = OpenFile(rptfile);
      $strobe("V2K1 - %0s - testing %0s", model, info);
      @(genmsg)
      if (RC==0) $fstrobe(rpt,"PASSED - %0s - V2K1 - test %0s\n", model, info);
      else       $fstrobe(rpt,"COMPILED - %0s - V2K1 - test %0s", model, info,
                              ", but some tests gave incorrect results\n");
    end
  endtask

  task V95_test;
    input [ 80*8:1] rptfile;
    input [ 80*8:1] model;
    input [160*8:1] info;
    integer         rpt;
    begin
      rpt = OpenFile(rptfile);
      $strobe("V95 - %0s - testing %0s", model, info);
      @(genmsg)
      if (RC==0) $fstrobe(rpt,"PASSED - %0s - V95 - test %0s\n", model, info);
      else       $fstrobe(rpt,"COMPILED - %0s - V95 - test %0s", model, info,
                              ", but some tests gave incorrect results\n");
    end
  endtask

  //---------------------------------------------
  // Cadence SHM-file option
  // This code must be copied into the top-level 
  //   testbench and uncommented
  //---------------------------------------------
//  `ifdef SHM
//    initial begin
//      $shm_open;
//      $shm_probe("ACTSFM");
//    end
//  `endif

  //---------------------------------------------
  // Signal Scan-dumpfile option (version 4)
  //---------------------------------------------
  `ifdef SSCAN4
    initial $recordvars("drivers");        //sscan v4.0
  `endif

  //---------------------------------------------
  // Signal Scan-dumpfile option (version 5)
  //---------------------------------------------
  `ifdef SSCAN
    initial begin
      $recordsetup("design=tb", "compress"); // sscan v5.0
      $recordvars;                    
    end
  `endif

  //---------------------------------------------
  // Veritools Undertow-dumpfile option
  //---------------------------------------------
  `ifdef UT
    initial $dumpvars;
  `endif

  //---------------------------------------------
  // Veritools Undertow-compressed dumpfile option
  //---------------------------------------------
  `ifdef UTX
    initial $vtDumpvars;
  `endif

  //---------------------------------------------
  // VCS VirSim dumpfile options
  // To view dumpfile: vcs -RPP
  //---------------------------------------------
  `ifdef VPD
    initial $vcdpluson;
  `endif


  //---------------------------------------------
  // Debussy dumpfile options
  // For Co-Design SystemSim use: +FSDB
  //   -pli <codesign_install_dir>/bin/arch/<arch_os>/fsdbwriter.so
  //---------------------------------------------
  `ifdef FSDB
    initial $fsdbDumpvars;
  `endif

  //---------------------------------------------
  // IEEE Verilog VCD-dumpfile option
  //---------------------------------------------
  `ifdef VCD
    initial begin
      $dumpfile("dump.vcd");
      $dumpvars;
    end
  `endif
endmodule
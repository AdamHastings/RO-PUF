interface arb_if(input bit clk);
   logic req;
   logic en;
   logic grant;

   clocking cb @(posedge clk);
      output req;
      output en;
      input  grant;
   endclocking
   
   modport Test (clocking cb);
   modport Dut (input req, en,
		output grant);
endinterface
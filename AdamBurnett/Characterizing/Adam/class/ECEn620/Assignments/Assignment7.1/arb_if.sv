interface arb_if();
   logic req;
   logic en;
   logic grant;

   modport Test (output req, en,
		 input grant);
   modport Dut (input req, en,
		output grant);
endinterface
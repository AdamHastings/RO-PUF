`include "arb_if.sv"
module top(); 
   //Clock Generator
   integer errors = 0;
   
   bit clk;
   wire rst;
   always #10ns clk = ~clk;

   arb_if arb0(), arb1(), arb2();
   arb_if darb0(), darb1(), darb2();
      
   test  t(clk, 
           rst, 
           arb0.Test, 
           arb1.Test, 
           arb2.Test,
	   darb0.Test,
	   darb1.Test,
	   darb2.Test,
	   errors);
   
   golden_arb golden(.clk(clk), 
                     .reset(rst), 
                     .req0(arb0.req),
                     .req1(arb1.req),
		     .req2(arb2.req),
		     .en0(arb0.en),
		     .en1(arb1.en),
		     .en2(arb2.en),
		     .grant0(arb0.grant),
		     .grant1(arb1.grant),
		     .grant2(arb2.grant)
		    );

   arbiter arb(.clk(clk), 
               .reset(rst), 
                     .req0(darb0.req),
                     .req1(darb1.req),
		     .req2(darb2.req),
		     .en0(darb0.en),
		     .en1(darb1.en),
		     .en2(darb2.en),
		     .grant0(darb0.grant),
		     .grant1(darb1.grant),
		     .grant2(darb2.grant)
		    );
   always @ (negedge clk) begin
      if(arb0.grant != darb0.grant) begin
	$display("@%0d: Grant0 Mismatch!!", $time);
	 errors++;
      end
      if(arb1.grant != darb1.grant) begin
	$display("@%0d: Grant1 Mismatch!!", $time);
	 errors++;
      end
      if(arb2.grant != darb2.grant) begin
	$display("@%0d: Grant2 Mismatch!!", $time);
	 errors++;
      end 
   end

   
endmodule // top

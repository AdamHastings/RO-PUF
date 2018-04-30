import tb_pkg::*;

`define SV_RAND_CHECK(r) \
   do begin \
      if (!(r)) begin \
	 $display("%s:%0d: Randomization failed \"%s\"", \
		  `__FILE__, `__LINE__, `"r`");\
	 $finish; \
      end \
   end while(0)
     
program automatic test( output bit    rst, 
				     arb_if a0,
				     arb_if a1,
				     arb_if a2,
				     arb_if da0,
				     arb_if da1,
			             arb_if da2,
			input int errors);

  `include "Driver.sv"   
  initial begin
     int count = 100;

     //Declare Genorators, Drivers and Mailboxes
     Generator G;
     Driver D;
 
     mailbox mbx;

     //Instanstantiate Generators and Drivers
     mbx = new();
     G = new(mbx);
     D = new(mbx);     

     //Reset the Circuit
     a0.cb.req <= 0; a1.cb.req <= 0;  a2.cb.req <= 0;
     a0.cb.en <= 0;  a1.cb.en <= 0;   a2.cb.en <= 0;

     da0.cb.req <= 0; da1.cb.req <= 0;  da2.cb.req <= 0;
     da0.cb.en <= 0;  da1.cb.en <= 0;   da2.cb.en <= 0;
         
     rst = 1;
     @(a0.cb)
     @(a0.cb)
     @(a0.cb)
     rst = 0;
     @(a0.cb)
     //Start Genorators and Drivers
     fork
	G.run(3*count);
	D.run(count);	
     join	
     
     $display("Ran for %0d cycles", count);     
     $display("Found %0d Errors.", errors);
     
  end

endprogram
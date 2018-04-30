import tb_pkg::*;

`define SV_RAND_CHECK(r) \
   do begin \
      if (!(r)) begin \
	 $display("%s:%0d: Randomization failed \"%s\"", \
		  `__FILE__, `__LINE__, `"r`");\
	 $finish; \
      end \
   end while(0)

program automatic test(input bit clk, 
                       output bit    rst, 
				     arb_if a0,
				     arb_if a1,
				     arb_if a2,
				     arb_if da0,
				     arb_if da1,
				     arb_if da2,
		       
		       input integer errors);
  
  initial begin
     Transaction t0, t1, t2;
     int n = 100;
          
     //*************************
     //      Directed Test
     //*************************
     rst = 1;
     da0.req = 0;
     da1.req = 0; 
     da2.req = 0;
     da0.en = 0;  
     da1.en = 0; 
     da2.en = 0;
     a0.req = 0;
     a1.req = 0; 
     a2.req = 0;
     a0.en = 0;  
     a1.en = 0; 
     a2.en = 0;
     @(posedge clk);
     @(negedge clk);
     rst = 0;    
     @(posedge clk);
     da0.req = 1;
     da1.req = 1; 
     da2.req = 1;  
     da0.en = 1;  
     da1.en = 1; 
     da2.en = 1;
     a0.req = 1;
     a1.req = 1; 
     a2.req = 1;  
     a0.en = 1;  
     a1.en = 1; 
     a2.en = 1;
     @(posedge clk);
     @(posedge clk);
     @(posedge clk);
     da0.req = 0;
     a0.req = 0;
     @(posedge clk);
     @(posedge clk);
     @(posedge clk);	
     da1.req = 0;
     da2.req = 0;
     a1.req = 0;
     a2.req = 0;
     @(posedge clk);     
     @(posedge clk);     
     @(posedge clk);

     //*************************
     //      Random Tests
     //*************************
        
     fork
	repeat (n) begin
	   t0 = new();
	   `SV_RAND_CHECK(t0.randomize());
	   a0.req = t0.req;
	   a0.en  = t0.en;
	   da0.req= t0.req;
	   da0.en = t0.en;
	   @(posedge clk);
	end
	repeat (n) begin
	   t1 = new();
	   `SV_RAND_CHECK(t1.randomize());
	   a1.req = t1.req;
	   a1.en  = t1.en;
	   da1.req= t1.req;
	   da1.en = t1.en;
	   @(posedge clk);
	end
	repeat (n) begin
	   t2 = new();
	   `SV_RAND_CHECK(t2.randomize());
	   a2.req = t2.req;
	   a2.en  = t2.en;
	   da2.req= t2.req;
	   da2.en = t2.en;
	   @(posedge clk);
	end 
     join

     @(posedge clk);     
     @(posedge clk);     
     @(posedge clk);

     $display("Ran for %0d cycles", n);
     
     $display("Found %0d Errors.", errors);
     
  end

endprogram
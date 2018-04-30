// Model for a 3-port arbiter using a round robin approach
// to issuing grant. 
// 1 Priority is maintained in a round robin format, i.e. port 0->port 1->port 2->port 0â€¦   
// 2 At reset the priority is port 0.
// 3 Priority will be incremented whenever any grant is asserted
// 4 Grant is combinatorial
// 5 If a port with priority is not enabled or not currently requesting the next port will 
// be given the grant given that port is enabled and requesting the bus, and so on.
`default_nettype none
 module golden_arb( input wire clk,
	         input wire reset,
		 input wire req0,
		 input wire req1,
		 input wire req2,
		 input wire en0,
		 input wire en1,
		 input wire en2,
		 output reg grant0,
		 output reg grant1,
		 output reg grant2);

   typedef enum 	    { P0, P1, P2 } PRIORITY;
   
   PRIORITY CurrentPriority, NextPriority;
   logic 		    grant_asserted;

   //Grant Logic
   always_comb begin
      grant0 = 0;
      grant1 = 0;
      grant2 = 0;
      case (CurrentPriority)
	P0:
	  if(req0 && en0)
	    grant0 = 1;
	  else if (req1 && en1)
	    grant1 = 1;
	  else if (req2 && en2)
	    grant2 = 1;
	P1:
	  if (req1 && en1)
	    grant1 = 1;
	  else if (req2 && en2)
	    grant2 = 1;
	  else if(req0 && en0)
	    grant0 = 1;
	P2:
	  if (req2 && en2)
	    grant2 = 1;
	  else if(req0 && en0)
	    grant0 = 1;
	  else if (req1 && en1)
	    grant1 = 1;
      endcase
   end 
 
   //Next Priority logic
   always_comb begin
    grant_asserted = (grant0 || grant1 || grant2);
    unique case (CurrentPriority)
      P0:
	if(grant_asserted)
	  NextPriority = P1;
        else
	  NextPriority = P0;
      P1:
	if(grant_asserted)
	  NextPriority = P2;
        else
	  NextPriority = P1;
      P2:
	if(grant_asserted)
	  NextPriority = P0;
        else
	  NextPriority = P2;      
    endcase
   end

   //Priority Register
   always_ff @(posedge clk iff reset == 0 or posedge reset) begin
    if (reset == 1)
      CurrentPriority <= P0;
    else   
      CurrentPriority <= NextPriority;
   end
endmodule
import tb_pkg::*;

class Driver;
   mailbox #(Transaction) mbx;
   
   function new (mailbox #(Transaction) m);
      mbx = m;
   endfunction // new

   task run(input int count);
      Transaction tr0, tr1, tr2;
      //Initialize the transaction in case
      // the mailbox is empty on the first try.
      tr0 = new(); tr1 = new(); tr2 = new();       
      repeat (count) begin
	 //If the Mailbox is empty disable
	 //  the port.
	 if(!mbx.try_get(tr0)) begin
	   tr0.en = 0;
	   //$display("No transaction in mailbox");
	 end
	 if(!mbx.try_get(tr1)) begin
	   tr1.en = 0;
           //$display("No transaction in mailbox");
	 end
	  if(!mbx.try_get(tr2)) begin
	   tr2.en = 0;
	   //$display("No transaction in mailbox");
	 end
	 //Drive Golden and DUT Signals
	 fork
	    begin
	       a0.cb.req <= tr0.req;
	       a0.cb.en <= tr0.en;
	       @(a0.cb);
	    end
	    begin
	       a1.cb.req <= tr1.req;
	       a1.cb.en <= tr1.en;
	       @(a1.cb);
	    end
	    begin
	       a2.cb.req <= tr2.req;
	       a2.cb.en <= tr2.en;
	       @(a2.cb);
	    end
	    begin
	       da0.cb.req <= tr0.req;
	       da0.cb.en <= tr0.en;
	       @(da0.cb);
	    end
	    begin
	       da1.cb.req <= tr1.req;
	       da1.cb.en <= tr1.en;
	       @(da1.cb);
	    end
	    begin
	       da2.cb.req <= tr2.req;
	       da2.cb.en <= tr2.en;
	       @(da2.cb);
	    end	    	  
	 join 	 
      end      
   endtask // run
     
endclass
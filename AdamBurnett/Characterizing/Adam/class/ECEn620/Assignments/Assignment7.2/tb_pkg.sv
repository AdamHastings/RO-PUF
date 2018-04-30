package tb_pkg;
   
class Transaction;
   rand bit req;
   rand bit en;

   /*constraint creq_dist {
      req dist { 0 :/ 30, 1 :/ 70};
   }
   
   constraint cen_dist { 
      en dist { 0 :/ 5, 1 :/95 };
   }*/
   
endclass // Transaction

class Generator;
   
   mailbox #(Transaction) mbx;
   
   function new (mailbox #(Transaction) m);
      mbx = m;
   endfunction // new

   task automatic run (int count);
      Transaction tr;      
      repeat (count) begin
	 //$display("Generating Transaction");
	 tr = new();
	 tr.randomize();
	 mbx.put(tr);	 
      end
   endtask; 
   
endclass
   
endpackage
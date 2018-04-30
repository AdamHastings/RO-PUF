class Agent;
   mailbox #(Instruction) mbx0, mbx1;

   function new (mailbox #(Instruction) mb0, 
	         mailbox #(Instruction) mb1);
      mbx0 = mb0;
      mbx1 = mb1;
   endfunction // new

   task automatic run(int count);
      Instruction I;
      repeat (count) begin
	 mbx0.get(I);
	 mbx1.put(I);
      end
   endtask // run

endclass // Agent

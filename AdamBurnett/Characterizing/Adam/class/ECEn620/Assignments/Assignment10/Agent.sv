class Agent #(ADDRESS_WIDTH=8);
   mailbox #(Instruction #(ADDRESS_WIDTH)) mbx0, mbx1;

   function new (mailbox #(Instruction #(ADDRESS_WIDTH)) mb0, 
	         mailbox #(Instruction #(ADDRESS_WIDTH)) mb1);
      mbx0 = mb0;
      mbx1 = mb1;
   endfunction // new

   task automatic run(int count);
      Instruction #(ADDRESS_WIDTH) I;
      repeat (count) begin
	 mbx0.get(I);
	 mbx1.put(I);
      end
   endtask // run

endclass // Agent

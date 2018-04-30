class Environment #(ADDRESS_WIDTH=8);
   Generator #(ADDRESS_WIDTH) G;
   Agent #(ADDRESS_WIDTH) A;
   Driver #(ADDRESS_WIDTH) D;   
   event DtoG_hs;
   
   mailbox #(Instruction #(ADDRESS_WIDTH)) GtoA, AtoD;
      
   function new ();
     
   endfunction // new

   task automatic build(virtual SPM_IF #(ADDRESS_WIDTH).TEST vSpm_if);
      GtoA = new ();
      AtoD = new ();
      
      G = new (GtoA, DtoG_hs);
      A = new (GtoA, AtoD);
      D = new (vSpm_if, AtoD, DtoG_hs);
      D.reset();
   endtask; // build

   task automatic run(int count);
      fork
	 G.run(count);
	 A.run(count);
	 D.run(count);
      join
   endtask // run
   
   
endclass // Environment
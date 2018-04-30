class Environment;
   Generator G;
   Agent A;
   Driver D;   
   event DtoG_hs;
   
   mailbox #(Instruction) GtoA, AtoD;
      
   function new ();
     
   endfunction // new

   task automatic build(virtual SPM_IF.TEST vSpm_if);
      GtoA = new ();
      AtoD = new ();
      
      G = new (GtoA, DtoG_hs);
      A = new (GtoA, AtoD);
      D = new (vSpm_if, AtoD, DtoG_hs);
      D.reset();
   endtask; // build

   task automatic run(int count);
      fork
	 G.run(10);
	 A.run(10);
	 D.run(10);
      join
   endtask // run
   
   
endclass // Environment
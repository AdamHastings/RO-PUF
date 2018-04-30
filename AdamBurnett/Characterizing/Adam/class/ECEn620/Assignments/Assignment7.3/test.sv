
import TbEnvPkg::*;
  
program automatic test(SPM_IF.TEST mem_bus);
   initial begin
      static virtual SPM_IF.TEST vMemBus = mem_bus;
            
      Environment E;
      E = new ();
      E.build(vMemBus);
      E.run(10);

      $display("Test Complete found %d Errors.", E.D.Scb.ErrorCounter);
   end
endprogram // test

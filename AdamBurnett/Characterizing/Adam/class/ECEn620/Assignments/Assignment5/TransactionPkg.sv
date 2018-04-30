package TransactionPkg;
  
  class Transaction;
    static int error = 0;
    static int current_id = 0;
    int id; 
    shortint address;
    byte data_to_write;
    bit [8:0] expected_read;
    bit [8:0] actual_read;
    
    function new(); 
      //Assign new id
      this.id = current_id++; 
      //Assign Random Address
      this.address = $random; 
      //Assign Random Data
      this.data_to_write = $random;
      this.expected_read = {^this.data_to_write, this.data_to_write};
    endfunction      
    
    function void print_actual_read();
      $display("Transaction %d: @ time %d actual read from address %04X is %02X", this.id, $time, this.address, this.actual_read); 
    endfunction
    
    function void check();
      if( this.expected_read != this.actual_read) begin
        $display("Transaction %d Check Error @ time %d: Read value %02X from address %04X, expected %02X", this.id, $time, this.actual_read, this.address, this.expected_read); 
        error++; 
      end      
    endfunction
    
    function Transaction copy();
      
      Transaction ret = new(); 
      ret.address = this.address; 
      ret.data_to_write = this.data_to_write;
      ret.expected_read = this.expected_read;
      ret.actual_read = this.actual_read;
      return ret;
      
    endfunction
    
    static function void print_error_count();
      $display("Total Transaction Errors @ time %d: %d", $time, error); 
    endfunction
        
  endclass
  
endpackage

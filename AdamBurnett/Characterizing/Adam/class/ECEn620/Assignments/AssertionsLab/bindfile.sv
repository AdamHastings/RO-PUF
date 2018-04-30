module bindfile(input clk, rst_n,
		input full, empty, read, write,
		input [4:0] cnt,
                input [3:0] rptr, [3:0] wptr);
        
  `ifndef ASSERT_MACROS

   `define ASSERT_MACROS
  
   `define assert_clk(arg, ck=clk) \
     assert property (@(posedge ck) disable iff (!rst_n) arg)

   `define assert_async_rst(arg, ck=clk) \
     assert property (@(posedge ck) arg)
       
  `endif

   ERROR_FIFO_RESET_SHOULD_CAUSE_EMPTY1_FULL0_RPTR0_WPTR0_CNT0:
     `assert_async_rst(!rst_n |-> (rptr==0 && wptr==0 && empty==1 && full==0 && cnt==0));
   ERROR_FIFO_SHOULD_BE_FULL:
     `assert_clk(cnt>15 |-> full);
   ERROR_FIFO_SHOULD_NOT_BE_FULL:
     `assert_clk(cnt<16 |-> !full);
   ERROR_FIFO_DID_NOT_GO_FULL:
     `assert_clk(cnt==15 && write && !read |-> ##1 full);
   ERROR_FIFO_SHOULD_BE_EMPTY:
     `assert_clk(cnt==0 |-> empty);
   ERROR_FIFO_SHOULD_NOT_BE_EMPTY:
     `assert_clk(cnt>0 |-> !empty);
   ERROR_FIFO_DID_NOT_GO_EMPTY:
     `assert_clk(cnt==1 && read && !write |-> ##1 empty);
   ERROR_FIFO_FULL_WRITE_CAUSED_WPTR_TO_CHANGE:
     `assert_clk((full && write && !read) |-> ##1 $stable(wptr));
   ERROR_FIFO_FULL_WRITE_CAUSED_FULL_FLAG_TO_CHANGE:
     `assert_clk((full && write && !read) |-> ##1 $stable(full));
   ERROR_FIFO_EMPTY_READ_CAUSED_EMPTY_FLAG_TO_CHANGE:
     `assert_clk((empty && read && !write) |-> ##1 $stable(empty));
   ERROR_FIFO_EMPTY_READ_CAUSED_RPTR_TO_CHANGE:
     `assert_clk((empty && read && !write) |-> ##1 $stable(rptr));
   ERROR_FIFO_WORD_COUNTER_IS_NEGATIVE:
     `assert_clk((cnt >=0));
   ERROR_FIFO_READWRITE_ILLEGAL_FIFO_FULL_OR_EMPTY:
     `assert_clk(read && write && !full && !empty |-> ##1 $stable(full) && $stable(empty));

endmodule // bindfile
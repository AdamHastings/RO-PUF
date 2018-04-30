library verilog;
use verilog.vl_types.all;
entity test is
    port(
        clk             : in     vl_logic;
        rst             : out    vl_logic;
        errors          : in     integer
    );
end test;

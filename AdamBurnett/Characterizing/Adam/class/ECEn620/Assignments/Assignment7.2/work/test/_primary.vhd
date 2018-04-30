library verilog;
use verilog.vl_types.all;
entity test is
    port(
        rst             : out    vl_logic;
        errors          : in     integer
    );
end test;

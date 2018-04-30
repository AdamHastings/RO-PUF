library verilog;
use verilog.vl_types.all;
entity lc3 is
    port(
        clk             : in     vl_logic;
        rst             : in     vl_logic;
        memory_dout     : in     vl_logic_vector(15 downto 0);
        memory_addr     : out    vl_logic_vector(15 downto 0);
        memory_din      : out    vl_logic_vector(15 downto 0);
        memWE           : out    vl_logic
    );
end lc3;

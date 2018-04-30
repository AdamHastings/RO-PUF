library verilog;
use verilog.vl_types.all;
entity fifo1 is
    port(
        dout            : out    vl_logic_vector(7 downto 0);
        full            : out    vl_logic;
        empty           : out    vl_logic;
        write           : in     vl_logic;
        read            : in     vl_logic;
        clk             : in     vl_logic;
        rst_n           : in     vl_logic;
        din             : in     vl_logic_vector(7 downto 0)
    );
end fifo1;

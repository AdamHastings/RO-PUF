library verilog;
use verilog.vl_types.all;
entity bindfile is
    port(
        clk             : in     vl_logic;
        rst_n           : in     vl_logic;
        full            : in     vl_logic;
        empty           : in     vl_logic;
        read            : in     vl_logic;
        write           : in     vl_logic;
        cnt             : in     vl_logic_vector(4 downto 0);
        rptr            : in     vl_logic_vector(3 downto 0);
        wptr            : in     vl_logic_vector(3 downto 0)
    );
end bindfile;

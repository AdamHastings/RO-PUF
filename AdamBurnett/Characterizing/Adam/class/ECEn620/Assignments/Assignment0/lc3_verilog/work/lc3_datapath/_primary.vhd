library verilog;
use verilog.vl_types.all;
entity lc3_datapath is
    port(
        clk             : in     vl_logic;
        rst             : in     vl_logic;
        IR_OUT          : out    vl_logic_vector(15 downto 0);
        N_OUT           : out    vl_logic;
        Z_OUT           : out    vl_logic;
        P_OUT           : out    vl_logic;
        aluControl      : in     vl_logic_vector(1 downto 0);
        enaALU          : in     vl_logic;
        SR1             : in     vl_logic_vector(2 downto 0);
        SR2             : in     vl_logic_vector(2 downto 0);
        DR              : in     vl_logic_vector(2 downto 0);
        regWE           : in     vl_logic;
        selPC           : in     vl_logic_vector(1 downto 0);
        enaMARM         : in     vl_logic;
        selMAR          : in     vl_logic;
        selEAB1         : in     vl_logic;
        selEAB2         : in     vl_logic_vector(1 downto 0);
        enaPC           : in     vl_logic;
        ldPC            : in     vl_logic;
        ldIR            : in     vl_logic;
        ldMAR           : in     vl_logic;
        ldMDR           : in     vl_logic;
        selMDR          : in     vl_logic;
        flagWE          : in     vl_logic;
        enaMDR          : in     vl_logic;
        memory_din      : out    vl_logic_vector(15 downto 0);
        memory_dout     : in     vl_logic_vector(15 downto 0);
        memory_addr     : out    vl_logic_vector(15 downto 0)
    );
end lc3_datapath;

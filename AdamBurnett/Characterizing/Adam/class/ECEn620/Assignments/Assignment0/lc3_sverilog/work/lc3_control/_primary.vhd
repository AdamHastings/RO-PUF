library verilog;
use verilog.vl_types.all;
entity lc3_control is
    port(
        clk             : in     vl_logic;
        rst             : in     vl_logic;
        IR              : in     vl_logic_vector(15 downto 0);
        N               : in     vl_logic;
        Z               : in     vl_logic;
        P               : in     vl_logic;
        aluControl      : out    vl_logic_vector(1 downto 0);
        enaALU          : out    vl_logic;
        SR1             : out    vl_logic_vector(2 downto 0);
        SR2             : out    vl_logic_vector(2 downto 0);
        DR              : out    vl_logic_vector(2 downto 0);
        logicWE         : out    vl_logic;
        selPC           : out    vl_logic_vector(1 downto 0);
        enaMARM         : out    vl_logic;
        selMAR          : out    vl_logic;
        selEAB1         : out    vl_logic;
        selEAB2         : out    vl_logic_vector(1 downto 0);
        enaPC           : out    vl_logic;
        ldPC            : out    vl_logic;
        ldIR            : out    vl_logic;
        ldMAR           : out    vl_logic;
        ldMDR           : out    vl_logic;
        selMDR          : out    vl_logic;
        memWE           : out    vl_logic;
        flagWE          : out    vl_logic;
        enaMDR          : out    vl_logic
    );
end lc3_control;

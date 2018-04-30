library verilog;
use verilog.vl_types.all;
entity SIMUTIL is
    generic(
        SIMUTIL_INFORM_MSG_EN: integer := 1;
        SIMUTIL_LOGFILE : string  := "SIMUTIL.log";
        FIELDWIDTH      : integer := 15;
        PAT_FILE        : string  := "vectors.pat";
        EXP_FILE        : string  := "vectors.exp";
        CNT_FILE        : string  := "vectors.f"
    );
    port(
        clk             : out    vl_logic
    );
    attribute mti_svvh_generic_type : integer;
    attribute mti_svvh_generic_type of SIMUTIL_INFORM_MSG_EN : constant is 1;
    attribute mti_svvh_generic_type of SIMUTIL_LOGFILE : constant is 1;
    attribute mti_svvh_generic_type of FIELDWIDTH : constant is 1;
    attribute mti_svvh_generic_type of PAT_FILE : constant is 1;
    attribute mti_svvh_generic_type of EXP_FILE : constant is 1;
    attribute mti_svvh_generic_type of CNT_FILE : constant is 1;
end SIMUTIL;

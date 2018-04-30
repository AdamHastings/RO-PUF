library verilog;
use verilog.vl_types.all;
entity lc3_testbench is
    generic(
        BR              : vl_logic_vector(0 to 3) := (Hi0, Hi0, Hi0, Hi0);
        ADD             : vl_logic_vector(0 to 3) := (Hi0, Hi0, Hi0, Hi1);
        LD              : vl_logic_vector(0 to 3) := (Hi0, Hi0, Hi1, Hi0);
        ST              : vl_logic_vector(0 to 3) := (Hi0, Hi0, Hi1, Hi1);
        JSR             : vl_logic_vector(0 to 3) := (Hi0, Hi1, Hi0, Hi0);
        \AND\           : vl_logic_vector(0 to 3) := (Hi0, Hi1, Hi0, Hi1);
        LDR             : vl_logic_vector(0 to 3) := (Hi0, Hi1, Hi1, Hi0);
        STR             : vl_logic_vector(0 to 3) := (Hi0, Hi1, Hi1, Hi1);
        RTI             : vl_logic_vector(0 to 3) := (Hi1, Hi0, Hi0, Hi0);
        \NOT\           : vl_logic_vector(0 to 3) := (Hi1, Hi0, Hi0, Hi1);
        LDI             : vl_logic_vector(0 to 3) := (Hi1, Hi0, Hi1, Hi0);
        STI             : vl_logic_vector(0 to 3) := (Hi1, Hi0, Hi1, Hi1);
        JMP             : vl_logic_vector(0 to 3) := (Hi1, Hi1, Hi0, Hi0);
        RES             : vl_logic_vector(0 to 3) := (Hi1, Hi1, Hi0, Hi1);
        LEA             : vl_logic_vector(0 to 3) := (Hi1, Hi1, Hi1, Hi0);
        TRAP            : vl_logic_vector(0 to 3) := (Hi1, Hi1, Hi1, Hi1)
    );
    attribute mti_svvh_generic_type : integer;
    attribute mti_svvh_generic_type of BR : constant is 1;
    attribute mti_svvh_generic_type of ADD : constant is 1;
    attribute mti_svvh_generic_type of LD : constant is 1;
    attribute mti_svvh_generic_type of ST : constant is 1;
    attribute mti_svvh_generic_type of JSR : constant is 1;
    attribute mti_svvh_generic_type of \AND\ : constant is 1;
    attribute mti_svvh_generic_type of LDR : constant is 1;
    attribute mti_svvh_generic_type of STR : constant is 1;
    attribute mti_svvh_generic_type of RTI : constant is 1;
    attribute mti_svvh_generic_type of \NOT\ : constant is 1;
    attribute mti_svvh_generic_type of LDI : constant is 1;
    attribute mti_svvh_generic_type of STI : constant is 1;
    attribute mti_svvh_generic_type of JMP : constant is 1;
    attribute mti_svvh_generic_type of RES : constant is 1;
    attribute mti_svvh_generic_type of LEA : constant is 1;
    attribute mti_svvh_generic_type of TRAP : constant is 1;
end lc3_testbench;

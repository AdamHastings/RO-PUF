library verilog;
use verilog.vl_types.all;
entity top is
    generic(
        ADDRESS_WIDTH   : integer := 9
    );
    attribute mti_svvh_generic_type : integer;
    attribute mti_svvh_generic_type of ADDRESS_WIDTH : constant is 1;
end top;

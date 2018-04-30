-------------------------------------------------------------------------------
-- Copyright (c) 2015 Xilinx, Inc.
-- All Rights Reserved
-------------------------------------------------------------------------------
--   ____  ____
--  /   /\/   /
-- /___/  \  /    Vendor     : Xilinx
-- \   \   \/     Version    : 14.4
--  \   \         Application: Xilinx CORE Generator
--  /   /         Filename   : ul_ila.vho
-- /___/   /\     Timestamp  : Wed Jan 28 14:50:14 EST 2015
-- \   \  /  \
--  \___\/\___\
--
-- Design Name: ISE Instantiation template
-- Component Identifier: xilinx.com:ip:chipscope_ila:1.05.a
-------------------------------------------------------------------------------
-- The following code must appear in the VHDL architecture header:

------------- Begin Cut here for COMPONENT Declaration ------ COMP_TAG
component ul_ila
  PORT (
    CONTROL : INOUT STD_LOGIC_VECTOR(35 DOWNTO 0);
    CLK : IN STD_LOGIC;
    TRIG0 : IN STD_LOGIC_VECTOR(31 DOWNTO 0);
    TRIG1 : IN STD_LOGIC_VECTOR(31 DOWNTO 0);
    TRIG2 : IN STD_LOGIC_VECTOR(31 DOWNTO 0);
    TRIG3 : IN STD_LOGIC_VECTOR(31 DOWNTO 0);
    TRIG4 : IN STD_LOGIC_VECTOR(31 DOWNTO 0);
    TRIG5 : IN STD_LOGIC_VECTOR(7 DOWNTO 0);
    TRIG6 : IN STD_LOGIC_VECTOR(15 DOWNTO 0));

end component;

-- COMP_TAG_END ------ End COMPONENT Declaration ------------
-- The following code must appear in the VHDL architecture
-- body. Substitute your own instance name and net names.
------------- Begin Cut here for INSTANTIATION Template ----- INST_TAG

your_instance_name : ul_ila
  port map (
    CONTROL => CONTROL,
    CLK => CLK,
    TRIG0 => TRIG0,
    TRIG1 => TRIG1,
    TRIG2 => TRIG2,
    TRIG3 => TRIG3,
    TRIG4 => TRIG4,
    TRIG5 => TRIG5,
    TRIG6 => TRIG6);

-- INST_TAG_END ------ End INSTANTIATION Template ------------

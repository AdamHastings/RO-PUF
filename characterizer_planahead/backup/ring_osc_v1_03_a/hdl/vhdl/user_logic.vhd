------------------------------------------------------------------------------
-- user_logic.vhd - entity/architecture pair
------------------------------------------------------------------------------
--
-- ***************************************************************************
-- ** Copyright (c) 1995-2012 Xilinx, Inc.  All rights reserved.            **
-- **                                                                       **
-- ** Xilinx, Inc.                                                          **
-- ** XILINX IS PROVIDING THIS DESIGN, CODE, OR INFORMATION "AS IS"         **
-- ** AS A COURTESY TO YOU, SOLELY FOR USE IN DEVELOPING PROGRAMS AND       **
-- ** SOLUTIONS FOR XILINX DEVICES.  BY PROVIDING THIS DESIGN, CODE,        **
-- ** OR INFORMATION AS ONE POSSIBLE IMPLEMENTATION OF THIS FEATURE,        **
-- ** APPLICATION OR STANDARD, XILINX IS MAKING NO REPRESENTATION           **
-- ** THAT THIS IMPLEMENTATION IS FREE FROM ANY CLAIMS OF INFRINGEMENT,     **
-- ** AND YOU ARE RESPONSIBLE FOR OBTAINING ANY RIGHTS YOU MAY REQUIRE      **
-- ** FOR YOUR IMPLEMENTATION.  XILINX EXPRESSLY DISCLAIMS ANY              **
-- ** WARRANTY WHATSOEVER WITH RESPECT TO THE ADEQUACY OF THE               **
-- ** IMPLEMENTATION, INCLUDING BUT NOT LIMITED TO ANY WARRANTIES OR        **
-- ** REPRESENTATIONS THAT THIS IMPLEMENTATION IS FREE FROM CLAIMS OF       **
-- ** INFRINGEMENT, IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS       **
-- ** FOR A PARTICULAR PURPOSE.                                             **
-- **                                                                       **
-- ***************************************************************************
--
------------------------------------------------------------------------------
-- Filename:          user_logic.vhd
-- Version:           1.03.a
-- Description:       User logic.
-- Date:              Tue Jun 09 14:43:09 2015 (by Create and Import Peripheral Wizard)
-- VHDL Standard:     VHDL'93
------------------------------------------------------------------------------
-- Naming Conventions:
--   active low signals:                    "*_n"
--   clock signals:                         "clk", "clk_div#", "clk_#x"
--   reset signals:                         "rst", "rst_n"
--   generics:                              "C_*"
--   user defined types:                    "*_TYPE"
--   state machine next state:              "*_ns"
--   state machine current state:           "*_cs"
--   combinatorial signals:                 "*_com"
--   pipelined or register delay signals:   "*_d#"
--   counter signals:                       "*cnt*"
--   clock enable signals:                  "*_ce"
--   internal version of output port:       "*_i"
--   device pins:                           "*_pin"
--   ports:                                 "- Names begin with Uppercase"
--   processes:                             "*_PROCESS"
--   component instantiations:              "<ENTITY_>I_<#|FUNC>"
------------------------------------------------------------------------------

-- DO NOT EDIT BELOW THIS LINE --------------------
library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

library proc_common_v3_00_a;
use proc_common_v3_00_a.proc_common_pkg.all;

-- DO NOT EDIT ABOVE THIS LINE --------------------

--USER libraries added here

------------------------------------------------------------------------------
-- Entity section
------------------------------------------------------------------------------
-- Definition of Generics:
--   C_SLV_DWIDTH                 -- Slave interface data bus width
--   C_NUM_REG                    -- Number of software accessible registers
--
-- Definition of Ports:
--   Bus2IP_Clk                   -- Bus to IP clock
--   Bus2IP_Reset                 -- Bus to IP reset
--   Bus2IP_Data                  -- Bus to IP data bus
--   Bus2IP_BE                    -- Bus to IP byte enables
--   Bus2IP_RdCE                  -- Bus to IP read chip enable
--   Bus2IP_WrCE                  -- Bus to IP write chip enable
--   IP2Bus_Data                  -- IP to Bus data bus
--   IP2Bus_RdAck                 -- IP to Bus read transfer acknowledgement
--   IP2Bus_WrAck                 -- IP to Bus write transfer acknowledgement
--   IP2Bus_Error                 -- IP to Bus error response
------------------------------------------------------------------------------

entity user_logic is
  generic
  (
    -- ADD USER GENERICS BELOW THIS LINE ---------------
    --USER generics added here
    -- ADD USER GENERICS ABOVE THIS LINE ---------------

    -- DO NOT EDIT BELOW THIS LINE ---------------------
    -- Bus protocol parameters, do not add to or delete
    C_SLV_DWIDTH                   : integer              := 32;
    C_NUM_REG                      : integer              := 8
    -- DO NOT EDIT ABOVE THIS LINE ---------------------
  );
  port
  (
    -- ADD USER PORTS BELOW THIS LINE ------------------
    --USER ports added here
	ring_en						   : out std_logic_vector(6 downto 1);
    -- ADD USER PORTS ABOVE THIS LINE ------------------

    -- DO NOT EDIT BELOW THIS LINE ---------------------
    -- Bus protocol ports, do not add to or delete
    Bus2IP_Clk                     : in  std_logic;
    Bus2IP_Reset                   : in  std_logic;
    Bus2IP_Data                    : in  std_logic_vector(0 to C_SLV_DWIDTH-1);
    Bus2IP_BE                      : in  std_logic_vector(0 to C_SLV_DWIDTH/8-1);
    Bus2IP_RdCE                    : in  std_logic_vector(0 to C_NUM_REG-1);
    Bus2IP_WrCE                    : in  std_logic_vector(0 to C_NUM_REG-1);
    IP2Bus_Data                    : out std_logic_vector(0 to C_SLV_DWIDTH-1);
    IP2Bus_RdAck                   : out std_logic;
    IP2Bus_WrAck                   : out std_logic;
    IP2Bus_Error                   : out std_logic
    -- DO NOT EDIT ABOVE THIS LINE ---------------------
  );

  attribute MAX_FANOUT : string;
  attribute SIGIS : string;

  attribute SIGIS of Bus2IP_Clk    : signal is "CLK";
  attribute SIGIS of Bus2IP_Reset  : signal is "RST";

end entity user_logic;

------------------------------------------------------------------------------
-- Architecture section
------------------------------------------------------------------------------

architecture IMP of user_logic is
  --USER signal declarations added here, as needed for user logic
  --Inverter component
  component inverter is
  port(
	Y : out std_logic;
	G : in std_logic;
	G1 : in std_logic;
	G2 : in std_logic;
	G4 : in std_logic
  );
  end component;
  
  type counts_array is array (1 to 6) of std_logic_vector(31 downto 0);
  type slv_reg_array is array (0 to 7) of std_logic_vector(0 to C_SLV_DWIDTH-1);
  
  signal ring_osc 						: std_logic_vector(6 downto 1) := (others => '0');
  signal ring_osc_buf					: std_logic_vector(6 downto 1);
  
  signal count							: counts_array;
  signal hundred_count					: counts_array;
  
  attribute S : string;
  attribute S of ring_osc : signal is "TRUE";
  attribute S of ring_osc_buf : signal is "TRUE";
  attribute S of count : signal is "TRUE";
  ------------------------------------------
  -- Signals for user logic slave model s/w accessible register example
  ------------------------------------------
  signal slv_reg0                       : std_logic_vector(0 to C_SLV_DWIDTH-1);
  signal slv_reg1                       : std_logic_vector(0 to C_SLV_DWIDTH-1);
  signal slv_reg2                       : std_logic_vector(0 to C_SLV_DWIDTH-1);
  signal slv_reg3                       : std_logic_vector(0 to C_SLV_DWIDTH-1);
  signal slv_reg4                       : std_logic_vector(0 to C_SLV_DWIDTH-1);
  signal slv_reg5                       : std_logic_vector(0 to C_SLV_DWIDTH-1);
  signal slv_reg6                       : std_logic_vector(0 to C_SLV_DWIDTH-1);
  signal slv_reg7                       : std_logic_vector(0 to C_SLV_DWIDTH-1);
  signal slv_reg_write_sel              : std_logic_vector(0 to 7);
  signal slv_reg_read_sel               : std_logic_vector(0 to 7);
  signal slv_ip2bus_data                : std_logic_vector(0 to C_SLV_DWIDTH-1);
  signal slv_read_ack                   : std_logic;
  signal slv_write_ack                  : std_logic;

  alias en								: std_logic is slv_reg0(0);
  alias ring_osc_en						: std_logic is slv_reg0(1);
begin

  --USER logic implementation added here
  --slv_reg0 stores the enable of the counter and the ring oscillator
  --slv_reg1-6 stores the count value for each ring oscillator
  --USER logic implementation added here
  INVERTER_GEN : for i in 1 to 6 generate
    ring_osc_1 : inverter
    port map(
      Y  => ring_osc(i),
      G => '1',
      G1 => '1',
      G2 => '1',
      G4 => '1'
    );
  end generate INVERTER_GEN;
	
  process (ring_osc_buf, en)
  begin
    if(en = '0') then -- Basic reset if no enable.
	  count <= (others=>(others=>'0'));
	  hundred_count <= (others=>(others=>'0'));
    elsif (ring_osc_buf(1)'event and ring_osc_buf(1) = '1') then
	  hundred_count(1) <= std_logic_vector(unsigned(hundred_count(1)) + 1);
	  if (to_integer(unsigned(hundred_count(1))) = 100) then
		count(1) <= std_logic_vector(unsigned(count(1)) + 1);
		hundred_count(1) <= (others=>'0');
	  end if;
	elsif (ring_osc_buf(2)'event and ring_osc_buf(2) = '1') then
	  hundred_count(2) <= std_logic_vector(unsigned(hundred_count(2)) + 1);
	  if (to_integer(unsigned(hundred_count(2))) = 100) then
		count(2) <= std_logic_vector(unsigned(count(2)) + 1);
		hundred_count(2) <= (others=>'0');
	  end if;
	elsif (ring_osc_buf(3)'event and ring_osc_buf(3) = '1') then
	  hundred_count(3) <= std_logic_vector(unsigned(hundred_count(3)) + 1);
	  if (to_integer(unsigned(hundred_count(3))) = 100) then
		count(3) <= std_logic_vector(unsigned(count(3)) + 1);
		hundred_count(3) <= (others=>'0');
	  end if;
	elsif (ring_osc_buf(4)'event and ring_osc_buf(4) = '1') then
	  hundred_count(4) <= std_logic_vector(unsigned(hundred_count(4)) + 1);
	  if (to_integer(unsigned(hundred_count(4))) = 100) then
		count(4) <= std_logic_vector(unsigned(count(4)) + 1);
		hundred_count(4) <= (others=>'0');
	  end if;
	elsif (ring_osc_buf(5)'event and ring_osc_buf(5) = '1') then
	  hundred_count(5) <= std_logic_vector(unsigned(hundred_count(5)) + 1);
	  if (to_integer(unsigned(hundred_count(5))) = 100) then
		count(5) <= std_logic_vector(unsigned(count(5)) + 1);
		hundred_count(5) <= (others=>'0');
	  end if;
	elsif (ring_osc_buf(6)'event and ring_osc_buf(6) = '1') then
	  hundred_count(6) <= std_logic_vector(unsigned(hundred_count(6)) + 1);
	  if (to_integer(unsigned(hundred_count(6))) = 100) then
		count(6) <= std_logic_vector(unsigned(count(6)) + 1);
		hundred_count(6) <= (others=>'0');
	  end if;
    end if;
  end process;
  
  slv_reg1 <= count(1);
  slv_reg2 <= count(2);
  slv_reg3 <= count(3);
  slv_reg4 <= count(4);
  slv_reg5 <= count(5);
  slv_reg6 <= count(6);
  
  --There is a nicer way to do this I'm sure
  ring_osc_buf(1) <= ring_osc(1) and ring_osc_en; --enable and gate
  ring_osc_buf(2) <= ring_osc(2) and ring_osc_en;
  ring_osc_buf(3) <= ring_osc(3) and ring_osc_en;
  ring_osc_buf(4) <= ring_osc(4) and ring_osc_en;
  ring_osc_buf(5) <= ring_osc(5) and ring_osc_en;
  ring_osc_buf(6) <= ring_osc(6) and ring_osc_en;
  
  ring_en <= ring_osc;
  ------------------------------------------
  -- Example code to read/write user logic slave model s/w accessible registers
  -- 
  -- Note:
  -- The example code presented here is to show you one way of reading/writing
  -- software accessible registers implemented in the user logic slave model.
  -- Each bit of the Bus2IP_WrCE/Bus2IP_RdCE signals is configured to correspond
  -- to one software accessible register by the top level template. For example,
  -- if you have four 32 bit software accessible registers in the user logic,
  -- you are basically operating on the following memory mapped registers:
  -- 
  --    Bus2IP_WrCE/Bus2IP_RdCE   Memory Mapped Register
  --                     "1000"   C_BASEADDR + 0x0
  --                     "0100"   C_BASEADDR + 0x4
  --                     "0010"   C_BASEADDR + 0x8
  --                     "0001"   C_BASEADDR + 0xC
  -- 
  ------------------------------------------
  slv_reg_write_sel <= Bus2IP_WrCE(0 to 7);
  slv_reg_read_sel  <= Bus2IP_RdCE(0 to 7);
  slv_write_ack     <= Bus2IP_WrCE(0) or Bus2IP_WrCE(1) or Bus2IP_WrCE(2) or Bus2IP_WrCE(3) or Bus2IP_WrCE(4) or Bus2IP_WrCE(5) or Bus2IP_WrCE(6) or Bus2IP_WrCE(7);
  slv_read_ack      <= Bus2IP_RdCE(0) or Bus2IP_RdCE(1) or Bus2IP_RdCE(2) or Bus2IP_RdCE(3) or Bus2IP_RdCE(4) or Bus2IP_RdCE(5) or Bus2IP_RdCE(6) or Bus2IP_RdCE(7);

  -- implement slave model software accessible register(s)
  SLAVE_REG_WRITE_PROC : process( Bus2IP_Clk ) is
  begin

    if Bus2IP_Clk'event and Bus2IP_Clk = '1' then
      if Bus2IP_Reset = '1' then
        slv_reg0 <= (others => '0');
        --slv_reg1 <= (others => '0');
        --slv_reg2 <= (others => '0');
        --slv_reg3 <= (others => '0');
        --slv_reg4 <= (others => '0');
        --slv_reg5 <= (others => '0');
        --slv_reg6 <= (others => '0');
        --slv_reg7 <= (others => '0');
      else
        case slv_reg_write_sel is
          when "00000001" =>
            for byte_index in 0 to (C_SLV_DWIDTH/8)-1 loop
              if ( Bus2IP_BE(byte_index) = '1' ) then
                slv_reg7(byte_index*8 to byte_index*8+7) <= Bus2IP_Data(byte_index*8 to byte_index*8+7);
              end if;
            end loop;
          when others => null;
        end case;
      end if;
    end if;

  end process SLAVE_REG_WRITE_PROC;

  -- implement slave model software accessible register(s) read mux
  SLAVE_REG_READ_PROC : process( slv_reg_read_sel, slv_reg0, slv_reg1, slv_reg2, slv_reg3, slv_reg4, slv_reg5, slv_reg6, slv_reg7 ) is
  begin

    case slv_reg_read_sel is
      when "10000000" => slv_ip2bus_data <= slv_reg0;
      when "01000000" => slv_ip2bus_data <= slv_reg1;
      when "00100000" => slv_ip2bus_data <= slv_reg2;
      when "00010000" => slv_ip2bus_data <= slv_reg3;
      when "00001000" => slv_ip2bus_data <= slv_reg4;
      when "00000100" => slv_ip2bus_data <= slv_reg5;
      when "00000010" => slv_ip2bus_data <= slv_reg6;
      when "00000001" => slv_ip2bus_data <= slv_reg7;
      when others => slv_ip2bus_data <= (others => '0');
    end case;

  end process SLAVE_REG_READ_PROC;

  ------------------------------------------
  -- Example code to drive IP to Bus signals
  ------------------------------------------
  IP2Bus_Data  <= slv_ip2bus_data when slv_read_ack = '1' else
                  (others => '0');

  IP2Bus_WrAck <= slv_write_ack;
  IP2Bus_RdAck <= slv_read_ack;
  IP2Bus_Error <= '0';

end IMP;

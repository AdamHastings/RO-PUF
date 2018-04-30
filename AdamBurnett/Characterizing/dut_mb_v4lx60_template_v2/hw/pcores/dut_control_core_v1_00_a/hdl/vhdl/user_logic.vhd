-------------------------------------------------------------------------------
-- Title      : USER_LOGIC
-- Project    : UART_PPC_V4FX60_TEST
-------------------------------------------------------------------------------
-- File       : user_logic.vhd
-- Author     : Andrew Schmidt  <aschmidt@itag4.east.isi.edu>
-- Company    : Information Sciences Institute
-- Created    : 2014-01-06
-- Last update: 2014-03-09
-- Platform   : 
-- Standard   : VHDL'87
-------------------------------------------------------------------------------
-- Description: 
-------------------------------------------------------------------------------
-- Copyright (c) 2014 
-------------------------------------------------------------------------------
-- Revisions  :
-- Date        Version  Author  Description
-- 2014-02-28  2.0      aschmidt    Added Heartbeat and release_ack signal
-- 2014-01-06  1.0      aschmidt    Created
-------------------------------------------------------------------------------

library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_arith.all;
use ieee.std_logic_unsigned.all;

entity user_logic is
  generic
    (
      C_INCLUDE_UL_CHIPSCOPE : boolean := false;
      C_SLV_DWIDTH           : integer := 32
      );
  port
    (
      -- ChipScope Control
      dut_ila_ctrl         : in  std_logic_vector(35 downto 0) := (others => '0');
      -- DUT UART Access Signals
      dut_uart_request     : out std_logic;
      dut_uart_release     : out std_logic;
      dut_uart_granted_ack : in  std_logic;
      dut_uart_release_ack : in  std_logic;
      -- DUT Heartbeat Signal
      dut_heartbeat        : out std_logic_vector(7 downto 0);
      -- DUT's Test Done Signal
      dut_test_done        : out std_logic;
      -- Software Controlled Reset to DUT
      dut_rst              : in  std_logic;
      -- IPIC Signals
      Bus2IP_Clk           : in  std_logic;
      Bus2IP_Reset         : in  std_logic;
      Bus2IP_Addr          : in  std_logic_vector(0 to 31);
      Bus2IP_CS            : in  std_logic;
      Bus2IP_RNW           : in  std_logic;
      Bus2IP_Data          : in  std_logic_vector(0 to C_SLV_DWIDTH-1);
      Bus2IP_BE            : in  std_logic_vector(0 to C_SLV_DWIDTH/8-1);
      IP2Bus_Data          : out std_logic_vector(0 to C_SLV_DWIDTH-1);
      IP2Bus_RdAck         : out std_logic;
      IP2Bus_WrAck         : out std_logic;
      IP2Bus_Error         : out std_logic
      );
end entity user_logic;

------------------------------------------------------------------------------
-- Architecture Section
------------------------------------------------------------------------------
architecture IMP of user_logic is
  -----------------------------------------------------------------------------
  -- Constants
  -----------------------------------------------------------------------------
  constant C_DUT_HEARTBEAT_HIGH       : integer := 15;
  constant C_DUT_HEARTBEAT_LOW        : integer := 8;
  constant C_UNUSED_BIT_7             : integer := 7;
  constant C_UNUSED_BIT_6             : integer := 6;
  constant C_DUT_TEST_DONE_BIT        : integer := 5;
  constant C_DUT_UART_RELEASE_ACK_BIT : integer := 4;
  constant C_DUT_UART_RELEASE_BIT     : integer := 3;
  constant C_DUT_UART_GRANTED_ACK_BIT : integer := 2;
  constant C_DUT_UART_REQUEST_BIT     : integer := 1;
  constant C_RESET_BIT                : integer := 0;
  -- Hardware Version: Currently 2.0
  constant C_HW_MAJOR_VERSION         : integer := 2;
  constant C_HW_MINOR_VERSION         : integer := 0;

  -----------------------------------------------------------------------------
  -- Registers and Internal signals
  -----------------------------------------------------------------------------
  signal ctrl_reg           : std_logic_vector(C_SLV_DWIDTH-1 downto 0) := (others => '0');
  signal status_reg         : std_logic_vector(C_SLV_DWIDTH-1 downto 0) := (others => '0');
  signal ver_reg            : std_logic_vector(C_SLV_DWIDTH-1 downto 0) := (others => '0');
  signal dut_heartbeat_i    : std_logic_vector(7 downto 0)              := (others => '0');
  signal dut_test_done_i    : std_logic                                 := '0';
  signal dut_uart_request_i : std_logic                                 := '0';
  signal dut_uart_release_i : std_logic                                 := '0';

  -----------------------------------------------------------------------------
  -- ChipScope UL ILA Component Declaration
  -----------------------------------------------------------------------------
  component ul_ila
    port (
      control : in std_logic_vector(35 downto 0);
      clk     : in std_logic;
      trig0   : in std_logic_vector(31 downto 0);
      trig1   : in std_logic_vector(31 downto 0);
      trig2   : in std_logic_vector(31 downto 0);
      trig3   : in std_logic_vector(31 downto 0);
      trig4   : in std_logic_vector(31 downto 0);
      trig5   : in std_logic_vector(7 downto 0);
      trig6   : in std_logic_vector(15 downto 0));
  end component;

-------------------------------------------------------------------------------
-- BEGIN
-------------------------------------------------------------------------------
begin
  -----------------------------------------------------------------------------
  -- Top-Level Output
  -----------------------------------------------------------------------------
  IP2Bus_WrAck     <= Bus2IP_CS and not(Bus2IP_RNW);
  IP2Bus_RdAck     <= Bus2IP_CS and Bus2IP_RNW;
  IP2Bus_Error     <= '0';
  dut_test_done    <= dut_test_done_i;
  dut_uart_request <= dut_uart_request_i;
  dut_uart_release <= dut_uart_release_i;
  dut_heartbeat    <= dut_heartbeat_i;

  -----------------------------------------------------------------------------
  -- Control Register
  -----------------------------------------------------------------------------
  dut_test_done_i    <= ctrl_reg(C_DUT_TEST_DONE_BIT);
  dut_uart_request_i <= ctrl_reg(C_DUT_UART_REQUEST_BIT);
  dut_uart_release_i <= ctrl_reg(C_DUT_UART_RELEASE_BIT);

  -----------------------------------------------------------------------------
  -- Status Register
  -----------------------------------------------------------------------------
  -- AGS: TODO - Not actually using DUT_RST yet!
  status_reg(31 downto 16)                                    <= (others => '0');
  status_reg(C_DUT_HEARTBEAT_HIGH downto C_DUT_HEARTBEAT_LOW) <= dut_heartbeat_i;
  status_reg(C_UNUSED_BIT_7)                                  <= '0';
  status_reg(C_UNUSED_BIT_6)                                  <= '0';
  status_reg(C_DUT_TEST_DONE_BIT)                             <= dut_test_done_i;
  status_reg(C_DUT_UART_RELEASE_ACK_BIT)                      <= dut_uart_release_ack;
  status_reg(C_DUT_UART_RELEASE_BIT)                          <= dut_uart_release_i;
  status_reg(C_DUT_UART_GRANTED_ACK_BIT)                      <= dut_uart_granted_ack;
  status_reg(C_DUT_UART_REQUEST_BIT)                          <= dut_uart_request_i;
  status_reg(C_RESET_BIT)                                     <= dut_rst;

  -----------------------------------------------------------------------------
  -- Version Register
  -----------------------------------------------------------------------------
  ver_reg <= conv_std_logic_vector(C_HW_MAJOR_VERSION, 16) &
             conv_std_logic_vector(C_HW_MINOR_VERSION, 16);

  -----------------------------------------------------------------------------
  -- PROCESS: SLAVE_REG_WRITE_PROC
  -- PURPOSE: Enable Processor Write to Slave Registers
  -----------------------------------------------------------------------------
  SLAVE_REG_WRITE_PROC : process(Bus2IP_Clk) is
  begin
    if ((Bus2IP_Clk'event) and (Bus2IP_Clk = '1')) then
      if (Bus2IP_Reset = '1') then
        ctrl_reg <= (others => '0');
        --status_reg <= (others => '0');
        --ver_reg <= (others => '0');        
      elsif ((Bus2IP_RNW = '0') and (Bus2IP_CS = '1')) then
        case (Bus2IP_Addr(26 to 29)) is
          when x"0"   => ctrl_reg <= Bus2IP_Data;
                         --when x"1"   => status_reg <= Bus2IP_Data;
                         --when x"2"   => ver_reg <= Bus2IP_Data;                         
          when others => null;
        end case;
      end if;
    end if;
  end process SLAVE_REG_WRITE_PROC;

  -----------------------------------------------------------------------------
  -- PROCESS: SLAVE_REG_READ_PROC
  -- PURPOSE: Enable Processor Read from Slave Registers
  -----------------------------------------------------------------------------
  SLAVE_REG_READ_PROC : process(Bus2IP_Addr, ctrl_reg, status_reg, ver_reg) is
  begin
    case (Bus2IP_Addr(26 to 29)) is
      when x"0"   => IP2Bus_Data <= ctrl_reg;
      when x"1"   => IP2Bus_Data <= status_reg;
      when x"2"   => IP2Bus_Data <= ver_reg;
      when others => IP2Bus_Data <= (others => '0');
    end case;
  end process SLAVE_REG_READ_PROC;

  -----------------------------------------------------------------------------
  -- PROCESS: HEARTBEAT_PROC
  -- PURPOSE: Increment dut_heartbeat when not in reset
  -----------------------------------------------------------------------------
  HEARTBEAT_PROC : process(Bus2IP_Clk) is
  begin
    if ((Bus2IP_Clk = '1') and (Bus2IP_Clk'event)) then
      if (Bus2IP_Reset = '1') then
        dut_heartbeat_i <= (others => '0');
      else
        dut_heartbeat_i <= dut_heartbeat_i + 1;
      end if;
    end if;
  end process HEARTBEAT_PROC;

  -----------------------------------------------------------------------------
  -- ChipScope ILA Component Instance
  -----------------------------------------------------------------------------
  CHIPSCOPE_UL_ILA_GEN : if (C_INCLUDE_UL_CHIPSCOPE) generate
    ul_ila_i : ul_ila
      port map (
        control   => dut_ila_ctrl,
        clk       => Bus2IP_Clk,
        trig0     => Bus2IP_Addr,       -- 32
        trig1     => Bus2IP_Data,       -- 32
        trig2     => ctrl_reg,          -- 32
        trig3     => status_reg,        -- 32
        trig4     => ver_reg,           -- 32
        trig5     => dut_heartbeat_i,   -- 8
        trig6(0)  => dut_test_done_i,   -- 16
        trig6(1)  => dut_uart_request_i,
        trig6(2)  => dut_uart_release_i,
        trig6(3)  => dut_uart_granted_ack,
        trig6(4)  => dut_uart_release_ack,
        trig6(5)  => dut_rst,
        trig6(6)  => Bus2IP_Reset,
        trig6(7)  => Bus2IP_CS,
        trig6(8)  => Bus2IP_RNW,
        trig6(9)  => '0',
        trig6(10) => '0',
        trig6(11) => '0',
        trig6(12) => '0',
        trig6(13) => '0',
        trig6(14) => '0',
        trig6(15) => '0'
        );
  end generate CHIPSCOPE_UL_ILA_GEN;
  
end IMP;
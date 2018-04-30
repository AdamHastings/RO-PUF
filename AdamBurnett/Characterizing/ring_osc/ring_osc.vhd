----------------------------------------------------------------------------------
-- Company: 
-- Engineer: 
-- 
-- Create Date:    14:12:34 11/21/2014 
-- Design Name: 
-- Module Name:    ring_osc - Behavioral 
-- Project Name: 
-- Target Devices: 
-- Tool versions: 
-- Description: 
--
-- Dependencies: 
--
-- Revision: 
-- Revision 0.01 - File Created
-- Additional Comments: 
--
----------------------------------------------------------------------------------
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use ieee.numeric_std.all;

-- Uncomment the following library declaration if using
-- arithmetic functions with Signed or Unsigned values
--use IEEE.NUMERIC_STD.ALL;

-- Uncomment the following library declaration if instantiating
-- any Xilinx primitives in this code.
--library UNISIM;
--use UNISIM.VComponents.all;

entity ring_osc is
end ring_osc;

architecture Behavioral of ring_osc is

signal ring_osc 						: std_logic_vector(70 downto 0) := (others => '0');
signal count 							: unsigned(70 downto 0) := (others => '0');
signal slv_reg0                       : std_logic_vector(0 to 31);
signal slv_reg1                       : std_logic_vector(0 to 31);
signal Bus2IP_Reset					:	std_logic := '0';

alias count_en						: std_logic is slv_reg1(0);
alias reset							: std_logic is slv_reg1(1);

begin

ring_oscillator:
for i in 1 to 70 generate
	ring_osc(i) <= not(ring_osc(i-1));
end generate;

process(Bus2IP_Reset, ring_osc(2), reset)
begin
	if (Bus2IP_Reset = '1' or reset = '1') then
		count <= (others => '0');
		slv_reg0 <= (others => '0');
	elsif (ring_osc(2)'event and ring_osc(2) = '1' and Bus2IP_Reset = '0' and count_en = '1') then
		count <= count + 1;
		slv_reg0 <= std_logic_vector(count);
	end if;
end process;

ring_osc(0) <= ring_osc(70);


end Behavioral;


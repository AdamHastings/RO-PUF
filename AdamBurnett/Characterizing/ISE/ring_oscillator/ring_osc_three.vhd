library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.NUMERIC_STD.ALL;

entity ring_oscillator_three is
	generic(NUM_INVERTERS 			: integer := 51);
end ring_oscillator_three;

architecture Behavioral of ring_oscillator_three is
component inverter is
port(
	Y : out std_logic;
	G1 : in std_logic
);
end component;

signal ring_osc						: std_logic_vector(NUM_INVERTERS-1 downto 0);
signal en								: std_logic := '1';

alias OSC_CLK_BIT						: std_logic is ring_osc(2);

begin
	ring_osc_gen:
	for i in 1 to ring_osc'high generate
	begin
		ring_oscillator : inverter
		port map(
			Y  => ring_osc(i),
			G1 => ring_osc(i-1)
			);
	end generate ring_osc_gen;
	
	--One last inverter between first and last bits
	ring_osc_zero : inverter
		port map(
			Y	=> ring_osc(0),
			G1 => ring_osc(ring_osc'high)
		);

end Behavioral;
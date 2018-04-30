package provide fork 1.0
package require critcl

fpga -f E:/Adam/Characterizing/Thesis/characterizer_LT_row_50_OMUX7.bit
connect mb mdm
set pid [fork]
switch $pid {
	-1 {
		puts "Fork attempt #$i failed."
	}
	0 {
		terminal -jtaguart_server
		puts "I am child process #$i."
		exit
	}
	default {
		puts "The parent just spawned child process #$i."
		dow E:/Adam/Characterizing/characterizer_planahead/workspace/characterizer/Debug/characterizer.elf
		run
	}
}

while {1}
{
}



fpga -f E:/Adam/Characterizing/Thesis/characterizer_LT_row_50_OMUX7.bit
connect mb mdm
terminal -jtaguart_server
while {1} {update idletasks}

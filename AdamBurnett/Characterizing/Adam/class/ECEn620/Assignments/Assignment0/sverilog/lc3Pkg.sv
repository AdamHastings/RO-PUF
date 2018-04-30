package lc3Pkg;
  typedef enum { FETCH0, FETCH1, FETCH2, 
         DECODE, 
         BRANCH0, 
         ADD0, 
         STORE0, STORE1, STORE2, 
         JSR0, JSR1,
	 JSRR0,
	 LDR0,
	 RTI0,
	 TRAP0,
	 RET0,
         AND0, 
         NOT0, 
         JMP0, 
         LD0, LD1, LD2} ControlStates; 
  enum { BR, ADD, LD, ST,
         JSR, JSRR, AND, LDR, STR,
         RTI, NOT, LDI, STI, RET,
         JMP, RES, LEA, TRAP } Opcodes;  
endpackage
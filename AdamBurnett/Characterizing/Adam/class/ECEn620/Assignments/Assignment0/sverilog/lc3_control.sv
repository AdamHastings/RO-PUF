`default_nettype none
//`include "lc3Pkg.sv"

import lc3Pkg::*;

module lc3_control ( clk, rst, 
                     IR, N, Z, P,  
                     aluControl, enaALU, SR1, SR2,
                     DR, logicWE, selPC, enaMARM, selMAR,
		                 selEAB1, selEAB2, enaPC, ldPC, ldIR,
	                   ldMAR, ldMDR, selMDR, memWE, flagWE, enaMDR); 

input logic clk;
input logic rst;

input logic [15:0] IR;
input logic N;
input logic Z; 
input logic P; 

//Output
output logic [1:0] aluControl = 2'b00; 
output logic [2:0] SR1 = 3'b000;
output logic [2:0] SR2 = 3'b000;
output logic [2:0] DR = 3'b000;

output logic enaALU = 1'b0;
output logic enaPC = 1'b0;
output logic enaMDR = 1'b0;
output logic enaMARM = 1'b0;

output logic [1:0] selPC = 2'b00;
output logic selMAR = 1'b0;
output logic selEAB1 = 1'b0;
output logic [1:0] selEAB2 = 2'b00;
output logic selMDR = 1'b0;

output logic ldPC = 1'b0;
output logic ldIR = 1'b0;
output logic ldMAR = 1'b0;
output logic ldMDR = 1'b0;

output logic memWE = 1'b0;
output logic flagWE = 1'b0;
output logic logicWE = 1'b0;


ControlStates CurrentState; 
ControlStates NextState; 
logic branch_enable; 


assign  branch_enable = ((N == IR[11]) || (Z == IR[10]) || (P == IR[9])) ? 1'b1 : 1'b0; 

always_ff @ (posedge clk iff rst == 0 or posedge rst) begin
 if(rst)
  CurrentState <= FETCH0; 
 else  
  CurrentState <= NextState; 
end

always_comb begin 
  //Tristate Signals
  enaALU <= 1'b0; enaMARM <= 1'b0;
  enaPC <= 1'b0; enaMDR <= 1'b0;

  //Register Load Signals  
  ldPC <= 1'b0; ldIR <= 1'b0;
  ldMAR <= 1'b0; ldMDR <= 1'b0;

  //MUX Select Signal
  selPC <= 2'b00; selMAR <= 1'b0;
  selEAB1 <= 1'b0; selEAB2 <= 2'b00;
  selMDR <= 1'b0; 

  //Write Enable Signals 
  flagWE <= 1'b0; 
  memWE <= 1'b0;
  logicWE <= 1'b0;  

  //Control Signals 
  aluControl <= 2'b00; 
  SR1 <= 3'b000;
  SR2 <= 3'b000;
  DR <= 3'b000;

  unique case (CurrentState)
    FETCH0: begin
      NextState <= FETCH1;
      //Load PC ADDRESS
      enaPC <= 1'b1; ldMAR <= 1'b1; 
    end
    FETCH1: begin
      NextState <= FETCH2;
      //READ Instruction From Memory
      selMDR<=1'b1; ldMDR<=1'b1;
      //Incremente Program Counter
      selPC<=2'b00;
      ldPC<=1'b1;  
    end
    FETCH2: begin
      NextState <= DECODE;
      //Load Instruction Register
      enaMDR <= 1'b1; ldIR <= 1'b1;       
    end
    DECODE: begin 
      unique case (IR[15:12])  //AND, ADD, NOT, JSR, BR, LD, ST, JMP.
        BR:  NextState <= BRANCH0;//**//
        ADD: NextState <= ADD0;   //**//
        LD:  NextState <= LD0;  //**// 
        ST:  NextState <= STORE0; //**//
        JSR: NextState <= JSR0;   //**//
        AND: NextState <= AND0;   //**//
        LDR: NextState <= FETCH0;
        STR: NextState <= FETCH0; 
        RTI: NextState <= FETCH0; 
        NOT: NextState <= NOT0;   //**//
        LDI: NextState <= FETCH0; 
        STI: NextState <= FETCH0;  
        JMP: NextState <= JMP0;   //
        RES: NextState <=  FETCH0; 
        LEA:  NextState <= FETCH0; 
        TRAP: NextState <= FETCH0; 
       endcase
    end 
    BRANCH0:  begin
     //Select ADDER inputs
     selEAB1 <= 1'b0; 
     selEAB2 <= 2'b10;
     //Load the New PC Value (if Branch Condition Met)
     ldPC <= branch_enable; 
     selPC <= 2'b01;
     NextState <= FETCH0;
    end 
    LD0: begin
     //Load MAR
     selEAB2 <= 2'b10; 
     selEAB1 <= 1'b0; 
     selMAR <= 1'b0; 
     enaMARM <= 1'b1; 
     ldMAR <= 1'b1; 
     NextState <= LD1;
    end
    LD1: begin
     //Load MDR
     selMDR <= 1'b1; 
     ldMDR <= 1'b1;

     NextState <= LD2;
    end   
    LD2: begin
     //Write to Register File 
     DR <= IR[11:9]; 
     logicWE <= 1'b1; 
     enaMDR <= 1'b1; 
     NextState <= FETCH0;
    end 
    NOT0: begin 
     aluControl <= 2'b11;
     enaALU <= 1'b1;
     SR1 <= IR[8:6]; 
     DR <= IR[11:9]; 
     logicWE <= 1'b1;  
     NextState <= FETCH0;
    end 
    ADD0: begin 
     aluControl <= 2'b01;
     enaALU <= 1'b1;
     SR1 <= IR[8:6];
     SR2 <= IR[2:0];  
     DR <= IR[11:9]; 
     logicWE <= 1'b1;  
     flagWE <= 1'b1; 
     NextState <= FETCH0;
    end
    AND0: begin 
     aluControl <= 2'b10;
     enaALU <= 1'b1;
     SR1 <= IR[8:6];
     SR2 <= IR[2:0];  
     DR <= IR[11:9]; 
     logicWE <= 1'b1;  
     NextState <= FETCH0;
    end
    STORE0: begin
     //Load the MDR 
     SR1 <= IR[11:9];
     aluControl <= 2'b00;
     enaALU <= 1'b1;
     selMDR <= 1'b0; 
     ldMDR <= 1'b1; 
     NextState <= STORE1;
    end  
    STORE1: begin
     //Load the MAR 
     selEAB1 <= 1'b0; 
     selEAB2 <= 2'b10; 
     selMAR <= 1'b0; 
     enaMARM <= 1'b1; 
     ldMAR <= 1'b1;
     NextState <= STORE2;
    end  
    STORE2: begin 
     memWE <= 1'b1; 
     NextState <= FETCH0;
   end 
   JSR0: begin 
     DR <= 3'b111; 
     enaPC <= 1'b1; 
     logicWE <= 1'b1; 
     NextState <= JSR1;
   end  
   JSR1: begin 
     selEAB1 <= 1'b0; 
     selEAB2 <= 2'b11; 
     selPC <= 2'b01; 
     ldPC <= 1'b1; 
     NextState <= FETCH0;
   end  
   JMP0: begin 
     SR1 <= IR[8:6]; 
     selEAB1 <= 1'b1; 
     selEAB2 <= 2'b00; 
     selPC <= 2'b01; 
     ldPC <= 1'b1; 
     NextState <= FETCH0; 
   end
  endcase
end  


endmodule


proc runClk { numOfCycles } {
  run [expr "$numOfCycles*20" ]ns 
}

proc checkValue { signal radix expectedValue } {
   set currentValue [examine -radix $radix $signal] 
   if { $currentValue == $expectedValue } {
     echo "    $signal has expected value ($expectedValue)"
   } else {
     echo "    Found Bug: $signal is not $expectedValue: Current Value $currentValue" 
   }
}

proc getAllOnesDecimal { bitwidth } {
  return [expr "int(pow(2,$bitwidth)-1)"]
}

proc testRegister { reg regInput we bitwidth} {
  echo "Testing Register: $reg"  
  
  #Reset Register to Zero
  change $reg $bitwidth'd0
  #Set Input High
  set ALLONES [getAllOnesDecimal $bitwidth]
  force -freeze $regInput 10#$ALLONES
  #Set write enable low
  force -freeze $we 0 
  
  ##Ensure that the register doesn't change 
  ##  without the correct write enable
  runClk 1
  ##Register should still be zero
  checkValue $reg hex 0000
  
  ##Set the write enable
  noforce $we
  force -freeze $we 1
  ##Run for 1 clock cycle
  runClk 1
  ##Register should have been updated now. 
  checkValue $reg unsigned $ALLONES
  ## Reset Write Enable Signal 
  noforce $we
  force -freeze $we 0
  runClk 1 
  
  noforce $we
  noforce $regInput
}  

proc textMultiplexor { mux inputs select bitwidth } {
    echo "Testing Multiplexor: $mux"
    
    #Force All Mux Inputs to Zero
    foreach input $inputs {
      force -freeze $input 10#0
    }
    runClk 1
     
    #Select Each Mux Input and Determine if it is connected
    set i 0
    set ALLONES [getAllOnesDecimal $bitwidth]
    foreach input $inputs {
      echo "  Testing Input $i: $input "
      noforce $input
      force -freeze $input 10#$ALLONES
      noforce $select
      force -freeze $select 10#$i
      
      run 10 ps
      checkValue $mux unsigned $ALLONES
      runClk 1
      noforce $input
      force -freeze $input 10#0
   
      incr i
    }
    
    foreach input $inputs {
      noforce $input 
    }
}


proc initState { } {
 set state(PC) [examine -radix hex "sim:/lc3/lc3_datapath/PC"]
 set state(IR) [examine -radix hex "sim:/lc3/lc3_datapath/IR"]
 set state(MAR) [examine -radix hex "sim:/lc3/lc3_datapath/MAR"]
 set state(MDR) [examine -radix hex "sim:/lc3/lc3_datapath/MDR"]
 set state(N) [examine -radix hex "sim:/lc3/lc3_datapath/N"]
 set state(Z) [examine -radix hex "sim:/lc3/lc3_datapath/Z"]
 set state(P) [examine -radix hex "sim:/lc3/lc3_datapath/P"]
 
 for {set i 0 } { $i < 8 } { incr i } {
   set state(REGFILE\[$i\]) [examine -radix hex "sim:/lc3/lc3_datapath/REGFILE\[$i\]"]
 }
 
 for {set i 0 } { $i < 256 } { incr i } {
   set state(MEMORY\[$i\]) [examine -radix hex "sim:/lc3/lc3_datapath/MEMORY\[$i\]"]
 }

 return state
}

proc calcBranchEnable { IRb } {
  set n [getBit $IRb 16 11]
  set z [getBit $IRb 16 10]
  set p [getBit $IRb 16 9]
  
  set N [examine "sim:/lc3/lc3_datapath/N"]
  set Z [examine "sim:/lc3/lc3_datapath/Z"]
  set P [examine "sim:/lc3/lc3_datapath/P"]
  
  if { $N == $n || $Z == $z || $P == $p} {
     return "true"; 
  } else {
     return "false";
  }
}

proc getBit { signalval bitwidth bit } {
   set stringIndex [expr "$bitwidth - 1 - $bit"]
   return [string index $signalval $stringIndex]   
}

proc calcSignExtension { IRb bitwidth bit} {
   set sign [getBit $IRb $bitwidth $bit ]
   set upper_index [expr "$bitwidth - 1 - $bit"]
  
   for { set i 0 } { $i < $upper_index } { incr i } {
     set IRb [string replace IRb $i $i $sign] 
   }
   
   return IRb
}

proc bin2dec { bin bitwidth} {
  set dec 0
  for { set i 0 } { i < $bitwidth } { incr i } {
    set curBit  [ getBit $bin $bitwidth $bit ]
    set curWeight [ expr "$curBit * int(pow(2, $i)) ]
    if { $i == [ expr "$bitwidth - 1"] } {
      set curWeight [ expr "-$curWeight"]
    }
    set dec [ expr "$dec + $curWeight ]   
  }
  return dec
}

proc updateState { currentState } {
    
    set curPC $currentState(PC)
    set nextPC [expr "curPC + 1"] 
    set nextMAR $curPC
    set nextMDR [examine -radix hex "sim:/lc3/lc3_datapath/MEMORY\[$curPC\]"]
    set nextIR $nextMDR
    set nextIRb [examine "sim:/lc3/lc3_datapath/MEMORY\[$curPC\]"]
        
    set opcode [string index nextIR 0]
    switch opcode {
      0 { #BR#
        set ben [ calcBranchEnable $nextIR ]
        if { ben == "true" } {
          set sext [ calcSignExtension $IRb 16 8 ]
        }
      }
      1 { #ADD#
      }  
      2 { #LD#
      }  
      3 { #ST#
      }  
      4 { #JSR#
      }  
      5 { #AND#
      }  
      6 { #LDR
      }  
      7 { #STR
      }  
      8 { #RTI
      }  
      9 { #NOT#
      }  
      a { #LDI
      }  
      b { #STI
      }  
      c { #JMP#
      }  
      d { #RESERVED
      }  
      e { #LEA
      }
      f { #TRAP
      }       
    }
    return currentState
}

proc check_lc3_state { expectedState } {
  
  checkValue "sim:/lc3/lc3_datapath/PC"  $expectedState(PC)
  checkValue "sim:/lc3/lc3_datapath/IC" hex $expectedState(IR)
  checkValue "sim:/lc3/lc3_datapath/MAR" hex $expectedState(MAR)
  checkValue "sim:/lc3/lc3_datapath/MDR" hex $expectedState(MDR)
  checkValue "sim:/lc3/lc3_datapath/N" hex $expectedState(N)
  checkValue "sim:/lc3/lc3_datapath/Z" hex $expectedState(Z)
  checkValue "sim:/lc3/lc3_datapath/P" hex $expectedState(P)
  
  for {set i 0 } { $i < 8 } { incr i } {
   checkValue "sim:/lc3/lc3_datapath/REGFILE\[$i\]" hex state(REGFILE\[$i\]) 
  }
 
  for {set i 0 } { $i < 256 } { incr i } {
   checkValue "sim:/lc3/lc3_datapath/MEMORY\[$i\]" hex state(MEMORY\[$i\]) 
  }
  
}


proc run_until_signal_has_value { signal radix value} { 
   set i 0
   set currentValue [examine -radix $radix $signal]
   
   while { $value != $currentValue } {
     if { [expr "$i % 5"] == "0" } {
       echo "Executed $i Cycles"
     }
     incr i
     runClk 1
   }
}


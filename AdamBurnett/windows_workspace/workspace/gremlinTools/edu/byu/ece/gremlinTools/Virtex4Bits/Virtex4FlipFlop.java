package edu.byu.ece.gremlinTools.Virtex4Bits;

import edu.byu.ece.gremlinTools.xilinxToolbox.V4XilinxToolbox;
import edu.byu.ece.rapidSmith.bitstreamTools.configuration.FPGA;
import edu.byu.ece.rapidSmith.design.Attribute;
import edu.byu.ece.rapidSmith.design.Instance;
import edu.byu.ece.rapidSmith.device.Tile;


public class Virtex4FlipFlop {
   
   private V4ConfigurationBit InitBit; 
   private V4ConfigurationBit SR_BIT; 
   //private V4ConfigurationBit FFLatchBit; 
   private Tile T; 
   private Instance XDLInstance;
   
   private Attribute InitAttribute;
   private Attribute FFLatchAttribute; 
   private Attribute SR_ATTR; 
   private Virtex4Slice Slice; 
  
   public Virtex4FlipFlop(int X, int Y, String Letter, FPGA fpga, Instance I,  Tile T, V4XilinxToolbox TB, Virtex4Slice S){
	  this.T = T; 
	  this.XDLInstance = I; 
	  setInitBit(X,Y,Letter, fpga, T, TB);
	  setSRATTRandBits( X, Y, Letter, fpga, I, T, TB);
	  this.FFLatchAttribute = new Attribute("FF" + Letter, "", "#OFF");
	  this.XDLInstance.addAttribute(FFLatchAttribute);
	  this.setSlice(S); 
   } 
   
   public void setSR_ATTR(String AttrValue){
	   if(AttrValue.equals("#OFF")){
		   SR_BIT.SetBitValueInBitStream(0, T);
	   } else if(AttrValue.equals("SRHIGH")){
		   SR_BIT.SetBitValueInBitStream(0, T);
	   } else if(AttrValue.equals("SRLOW")){
		   SR_BIT.SetBitValueInBitStream(1, T);
	   } else {
   		  System.out.println("Invalid Value for SRATTR!");
		System.exit(1);
	  }
	   SR_ATTR.setValue(AttrValue);
   }
   
   public void setSRATTRandBits(int X, int Y, String Letter, FPGA fpga, Instance I, Tile T, V4XilinxToolbox TB){
	   String LetterErrorMsg = "V4 FF:Letter Choices are X and Y";
	   String XErrorMsg      = "V4 FF:X Must be 0 or 1";
	   String YErrorMsg      = "V4 FF:Y Must be 0 or 1";
	   
	  this.SR_ATTR = new Attribute("FF" + Letter + "_SR_ATTR", "", "#OFF");
	  I.addAttribute(SR_ATTR);
	  switch(Letter.charAt(0)){
	   case 'X':
		   switch(X){
		    case 0: 
		    	switch (Y) {
		          case 0: SR_BIT = new V4ConfigurationBit (fpga, new Bit(20, 8, 128), T, TB); break;
		          case 1: SR_BIT = new V4ConfigurationBit (fpga, new Bit(20, 7, 32), T, TB); break;
		          default: System.out.println(YErrorMsg); break;
		    	} break;
		    case 1: 
		    	switch (Y) {
		    	  case 0: SR_BIT = new V4ConfigurationBit (fpga, new Bit(20, 8, 64), T, TB); break; 
		    	  case 1: SR_BIT = new V4ConfigurationBit (fpga, new Bit(20, 7, 64), T, TB); break;
		    	  default: System.out.println(YErrorMsg); break; 
		    	} break;
		    default : System.out.println(XErrorMsg); break;
		   } break;
	   case 'Y':   
		   switch(X){
		    case 0: 
		    	switch (Y) {
		          case 0: SR_BIT = new V4ConfigurationBit (fpga, new Bit(20, 5, 2), T, TB); break;
		          case 1: SR_BIT = new V4ConfigurationBit (fpga, new Bit(20, 2, 2), T, TB); break;
		          default: System.out.println(YErrorMsg); break;
		    	} break;
		    case 1: 
		    	switch (Y) {
		    	  case 0: SR_BIT = new V4ConfigurationBit (fpga, new Bit(20, 5, 4), T, TB); break; 
		    	  case 1: SR_BIT = new V4ConfigurationBit (fpga, new Bit(20, 2, 4), T, TB); break;
		    	  default: System.out.println(YErrorMsg); break;
		    	} break;
		    default : System.out.println(XErrorMsg);break;
		   }  break;
	  default: System.out.println(LetterErrorMsg); break;
	  }
	  SR_BIT.SetBitValueInBitStream(0, T);
   }
  
  /*
   * Possible Values: "#OFF" | "#INIT1" | "INIT0"
   */
   public void setInitAttributeValue(String AttrValue){
	   if(AttrValue.equals("#OFF")){
		   InitBit.SetBitValueInBitStream(1, InitBit.getTile());
	   } else if(AttrValue.equals("#INIT1")){
		   InitBit.SetBitValueInBitStream(0, InitBit.getTile());
	   } else if(AttrValue.equals("#INIT0")){
		   InitBit.SetBitValueInBitStream(1, InitBit.getTile());
	   } else {
		   System.out.println(AttrValue + " is not a proper Value for this Attribute");
	       System.exit(1);
	   }
	   
	   InitAttribute.setValue(AttrValue);
   }
   
   public Attribute getFFLatchAttribute(){
	   return this.FFLatchAttribute; 
   }
   
   public String getInitAttributValue(){
	  return InitAttribute.getValue();
   }
   
   public V4ConfigurationBit getInitBit(){
	   return InitBit; 
   }
   //FPGA fpga, Bit B, Tile T, V4XilinxToolbox TB
   private void setInitBit(int X, int Y, String Letter, FPGA fpga,  Tile T, V4XilinxToolbox TB){
	   String LetterErrorMsg = "V4 FF:Letter Choices are X and Y";
	   String XErrorMsg      = "V4 FF:X Must be 0 or 1";
	   String YErrorMsg      = "V4 FF:Y Must be 0 or 1";
		
	   this.InitAttribute = new Attribute("FF" + Letter + "_INIT_ATTR", "", "#OFF");
	   this.XDLInstance.addAttribute(InitAttribute);
	   
	   switch(Letter.charAt(0)){
		   case 'X':
			   switch(X){
			    case 0: 
			    	switch (Y) {
			          case 0: InitBit = new V4ConfigurationBit (fpga, new Bit(20, 8, 2), T, TB); break;
			          case 1: InitBit = new V4ConfigurationBit (fpga, new Bit(20, 7, 2), T, TB); break;
			          default: System.out.println(YErrorMsg); break;
			    	} break;
			    case 1: 
			    	switch (Y) {
			    	  case 0: InitBit = new V4ConfigurationBit (fpga, new Bit(20, 8, 4), T, TB); break; 
			    	  case 1: InitBit = new V4ConfigurationBit (fpga, new Bit(20, 7, 4), T, TB); break;
			    	  default: System.out.println(YErrorMsg); break; 
			    	} break;
			    default : System.out.println(XErrorMsg); break;
			   } break;
		   case 'Y':   
			   switch(X){
			    case 0: 
			    	switch (Y) {
			          case 0: InitBit = new V4ConfigurationBit (fpga, new Bit(20, 6, 32), T, TB); break;
			          case 1: InitBit = new V4ConfigurationBit (fpga, new Bit(20, 3, 32), T, TB); break;
			          default: System.out.println(YErrorMsg); break;
			    	} break;
			    case 1: 
			    	switch (Y) {
			    	  case 0: InitBit = new V4ConfigurationBit (fpga, new Bit(20, 6, 64), T, TB); break; 
			    	  case 1: InitBit = new V4ConfigurationBit (fpga, new Bit(20, 3, 64), T, TB); break;
			    	  default: System.out.println(YErrorMsg); break;
			    	} break;
			    default : System.out.println(XErrorMsg);break;
			   }  break;
		  default: System.out.println(LetterErrorMsg); break;
		 	 
	 }	//End Switch Statement
	 //Set the Init Bit According to the Attribute
	 this.setInitAttributeValue("#OFF");
   }

public boolean Verify() {
	if(VerifyInit() &&
	   VerifySR()){
		return true;
	}
	
	return false;
}

private boolean VerifySR() {
		
    int CurrentValue = SR_BIT.getValue();
	
	if(CurrentValue == 0 && (SR_ATTR.getValue().equals("#OFF") || SR_ATTR.getValue().equals("SRHIGH")) ){
		return true; 
	} 
	
	if(CurrentValue == 1 && SR_ATTR.getValue().equals("SRLOW")){
		return true; 
	}
	
	System.out.println("VerifySR Failed.");
	return false;
}

private boolean VerifyInit() {
	

	int CurrentValue = InitBit.getValue();
	
	if(CurrentValue == 0 && InitAttribute.getValue().equals("#INIT1")){
		return true; 
	} 
	
	if(CurrentValue == 1 && (InitAttribute.getValue().equals("#OFF") || InitAttribute.getValue().equals("#INIT0"))){
		return true; 
	}
	
	System.out.println("VerifyInit Failed. BitValue " + CurrentValue + " " + InitAttribute);
	return false;
}

public void setSlice(Virtex4Slice slice) {
	Slice = slice;
}

public Virtex4Slice getSlice() {
	return Slice;
}
   
}

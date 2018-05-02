package edu.byu.ece.gremlinTools.Virtex4Bits;

import edu.byu.ece.gremlinTools.xilinxToolbox.V4XilinxToolbox;
import edu.byu.ece.rapidSmith.bitstreamTools.configuration.FPGA;
import edu.byu.ece.rapidSmith.design.Attribute;
import edu.byu.ece.rapidSmith.design.Design;
import edu.byu.ece.rapidSmith.design.Instance;
import edu.byu.ece.rapidSmith.device.PrimitiveSite;
import edu.byu.ece.rapidSmith.device.PrimitiveType;
import edu.byu.ece.rapidSmith.device.Tile;

public class Virtex4Slice {
    
	private int X; 
    private int Y; 
   
    
    private Virtex4Lut LutG;
    private Virtex4Lut LutF; 
    
    private Virtex4FlipFlop FFX;
    private Virtex4FlipFlop FFY;
    
    private Instance XDLInstance;
    private PrimitiveSite Site; 
    private PrimitiveType Type; 
    
    private Design design; 
    private FPGA fpga; 
    private Tile T;
    private V4XilinxToolbox TB; 
    
    private DXMUX DXMUX0; 
    private DYMUX DYMUX0; 
    
    private V4ConfigurationBit FFLatchBit; //This bit is the same for both X and Y the Attribute is stored in the FF OBJECT  
    private V4ConfigurationBit BXINV;  
    private V4ConfigurationBit CLKINV; 
    private V4ConfigurationBit SRINV; 
    
    private V4ConfigurationBit SYNC_BIT; 
    
    private Attribute BXINV_ATTR; 
    private Attribute CLKINV_ATTR; 
    private Attribute SYNC_ATTR; 
    private Attribute SRINV_ATTR;
    private Attribute SRFFMUX; 
    //Slice Level Attributes Not Represented By a Bit
    private Attribute XUSED; 
    private Attribute YUSED; 
        
    public Virtex4Slice(int x, int y, FPGA fpga, Design design, Tile T, V4XilinxToolbox TB){
    	
    	this.fpga = fpga; 
    	this.T = T; 
    	this.TB = TB; 
    	this.design = design; 
    	
    	setX(x); 
    	setY(y);
    	
    	setType();
    	
    	CreateInstanceAndSetPrimitiveSite();
    	    	
    	setLutG(new Virtex4Lut(getX(), getY(), "G", fpga, XDLInstance, T, TB, this));
    	setLutF(new Virtex4Lut(getX(), getY(), "F", fpga, XDLInstance, T, TB, this));
    	
    	setFFX(new Virtex4FlipFlop(getX(),getY(),"X", fpga, XDLInstance,  T, TB, this));
    	setFFY(new Virtex4FlipFlop(getX(),getY(),"Y", fpga, XDLInstance,  T, TB, this));
    	
    	setFFLatchBit(fpga, T, TB);
    	setUpBXINV();
    	setUpCLKINV();
    	setUpSRINV();
    	setUnRepresentedSliceAttributes();
    	
    	setDXMUX0(new DXMUX(X, Y, this.design, XDLInstance, this.T, this.fpga, this.TB));
    	setDYMUX0(new DYMUX(X, Y, this.design, XDLInstance, this.T, this.fpga, this.TB));
    	
    	setSYNC();
    	
    }
    
	public void setSRINV_ATTRValue(String AttrValue){
    	
    	if(AttrValue.equals("#OFF")){
    		SRINV.SetBitValueInBitStream(0, T);
    		 SRFFMUX.setValue("#OFF");
    	} else if(AttrValue.equals("SR")){
    		SRINV.SetBitValueInBitStream(1, T);
    		if(Type.equals(PrimitiveType.SLICEM)){
    		  SRFFMUX.setValue("0");	
    		}
    	} else if(AttrValue.equals("SR_B")){
    		SRINV.SetBitValueInBitStream(0, T);
    		if(Type.equals(PrimitiveType.SLICEM)){
    		  SRFFMUX.setValue("0");	
    		}
    	} else {
    		System.out.println("Invalid Value for SRINV!");
    		System.exit(1);
    	}
    	
    	SRINV_ATTR.setValue(AttrValue);
    }
  
    
    private void setUpSRINV() {
    	SRINV_ATTR = new Attribute("SRINV", "", "#OFF");
    	XDLInstance.addAttribute(SRINV_ATTR);
    	
    	if(Type.equals(PrimitiveType.SLICEM)){
    		SRFFMUX = new Attribute("SRFFMUX", "", "#OFF");
    		XDLInstance.addAttribute(SRFFMUX);
    	} 
    	
    	switch(X){
		case 0:
			switch(Y){
				case 0:	SRINV = new V4ConfigurationBit (fpga, new Bit(18, 5, 128), T, TB); break;
		        case 1: SRINV = new V4ConfigurationBit (fpga, new Bit(18, 4, 1), T, TB); break; 
			    default:System.out.println("Invalid Y Value for setSYNC"); break; 
			} break; 
		case 1: 
			switch(Y){
				case 0:	SRINV = new V4ConfigurationBit (fpga, new Bit(18, 5, 32), T, TB); break;
		        case 1: SRINV = new V4ConfigurationBit (fpga, new Bit(20, 5, 64), T, TB); break; 
			    default:System.out.println("Invalid Y Value for setSYNC"); break; 
		} break;
		default:System.out.println("Invalid X Value for setUpCLKINV"); break; 
	   }
    	
    	SRINV.SetBitValueInBitStream(0, T);
	}

	public void setSYNC_ATTR_Value(String AttrValue){
    	
    	if(AttrValue.equals("#OFF")){
    		SYNC_BIT.SetBitValueInBitStream(0, T);
    	} else if(AttrValue.equals("ASYNC")){
    		SYNC_BIT.SetBitValueInBitStream(0, T);
    	} else if(AttrValue.equals("SYNC")){
    		SYNC_BIT.SetBitValueInBitStream(1, T);
    	} else {
    		System.out.println("Invalid Value for SYNC_ATTR!");
    		System.exit(1);
    	}
    	
    	SYNC_ATTR.setValue(AttrValue);
    }
    
    private void setSYNC(){
    	SYNC_ATTR = new Attribute("SYNC_ATTR", "", "#OFF");
    	XDLInstance.addAttribute(SYNC_ATTR);
    	switch(X){
		case 0:
			switch(Y){
				case 0:	SYNC_BIT = new V4ConfigurationBit (fpga, new Bit(20, 5, 32), T, TB); break;
		        case 1: SYNC_BIT = new V4ConfigurationBit (fpga, new Bit(20, 2, 32), T, TB); break; 
			    default:System.out.println("Invalid Y Value for setSYNC"); break; 
			} break; 
		case 1: 
			switch(Y){
				case 0:	SYNC_BIT = new V4ConfigurationBit (fpga, new Bit(20, 5, 64), T, TB); break;
		        case 1: SYNC_BIT = new V4ConfigurationBit (fpga, new Bit(20, 2, 64), T, TB); break; 
			    default:System.out.println("Invalid Y Value for setSYNC"); break; 
		} break;
		default:System.out.println("Invalid X Value for setUpCLKINV"); break; 
	   }
    	
      
    }
    
    public void setCLKINV_ATTR(String AttrValue){
    	//SLICE_X0Y0 CLKINV #OFF  1 18 5 4 0 0
    	//SLICE_X0Y0 CLKINV CLK   1 18 5 4 1 0
        //SLICE_X0Y0 CLKINV CLK_B 1 18 5 4 0 0
    	//SLICE_X0Y1 CLKINV #OFF  1 18 0 1 0 0
    	//SLICE_X0Y1 CLKINV CLK   1 18 0 1 1 0
    	//SLICE_X0Y1 CLKINV CLK_B 1 18 0 1 0 0
    	//SLICE_X1Y0 CLKINV #OFF  1 18 5 16 0 0
    	//SLICE_X1Y0 CLKINV CLK   1 18 5 16 1 0
    	//SLICE_X1Y0 CLKINV CLK_B 1 18 5 16 0 0
    	//SLICE_X1Y1 CLKINV #OFF  1 18 0 32 0 0
    	//SLICE_X1Y1 CLKINV CLK   1 18 0 32 1 0
    	//SLICE_X1Y1 CLKINV CLK_B 1 18 0 32 0 0
        
    	if(AttrValue.equals("#OFF")){
    		CLKINV.SetBitValueInBitStream(0, T);
    	} else if(AttrValue.equals("CLK")){
    		CLKINV.SetBitValueInBitStream(1, T);
    	} else if(AttrValue.equals("CLK_B")){
    		CLKINV.SetBitValueInBitStream(0, T);
    	} else {
    		System.out.println("Invalid String Passed for this Attribute!");
    		System.exit(1);
    	}
    	
    	CLKINV_ATTR.setValue(AttrValue);
    }
    
    private void setUpCLKINV() {
    	
    	CLKINV_ATTR = new Attribute("CLKINV", "", "#OFF");
   	    XDLInstance.addAttribute(CLKINV_ATTR);
    	switch(X){
			case 0:
				switch(Y){
					case 0:	CLKINV = new V4ConfigurationBit (fpga, new Bit(18, 5, 4), T, TB); break;
			        case 1: CLKINV = new V4ConfigurationBit (fpga, new Bit(18, 0, 1), T, TB); break; 
				    default:System.out.println("Invalid Y Value for setUpCLKINV"); break; 
				} break; 
			case 1: 
				switch(Y){
					case 0:	CLKINV = new V4ConfigurationBit (fpga, new Bit(18, 5, 16), T, TB); break;
			        case 1: CLKINV = new V4ConfigurationBit (fpga, new Bit(18, 0, 32), T, TB); break; 
				    default:System.out.println("Invalid Y Value for setUpCLKINV"); break; 
			} break;
			default:System.out.println("Invalid X Value for setUpCLKINV"); break; 
		}
		
    	 CLKINV.SetBitValueInBitStream(0, T);
	}

	public void setBXINV_ATTR(String AttrValue){
    	if(AttrValue.equals("#OFF")){
    		BXINV.SetBitValueInBitStream(0, T);
    	} else if(AttrValue.equals("BX")){
    		BXINV.SetBitValueInBitStream(1, T);
    	} else if(AttrValue.equals("BX_B")){
    		BXINV.SetBitValueInBitStream(0, T);
    	} else {
    		System.out.println("Invalid String Passed for this Attribute!");
    		System.exit(1);
    	}
    	
    	BXINV_ATTR.setValue(AttrValue);
    }
    
    private void setUpBXINV() {
         
    	/*SLICE_X0Y0 BXINV #OFF 1 18 4 16 0 0
		  SLICE_X0Y0 BXINV BX   1 18 4 16 1 0
		  SLICE_X0Y0 BXINV BX_B 1 18 4 16 0 0
		  SLICE_X0Y1 BXINV #OFF 1 18 4 32 0 0
		  SLICE_X0Y1 BXINV BX   1 18 4 32 1 0
		  SLICE_X0Y1 BXINV BX_B 1 18 4 32 0 0
		  SLICE_X1Y0 BXINV #OFF 1 18 1 4 0 0
		  SLICE_X1Y0 BXINV BX   1 18 1 4 1 0
		  SLICE_X1Y0 BXINV BX_B 1 18 1 4 0 0
		  SLICE_X1Y1 BXINV #OFF 1 18 1 8 0 0
		  SLICE_X1Y1 BXINV BX   1 18 1 8 1 0
		  SLICE_X1Y1 BXINV BX_B 1 18 1 8 0 0*/
    	 BXINV_ATTR = new Attribute("BXINV", "", "#OFF");
    	 XDLInstance.addAttribute(BXINV_ATTR);
    	 
    	 String XErrorMsg      = "V4 FF:X Must be 0 or 1";
  	     String YErrorMsg      = "V4 FF:Y Must be 0 or 1";
    	
		   switch(X){
		    case 0: 
		    	switch (Y) {
		    	              //SLICE_X0Y0 BXINV #OFF 1 18 4 16 0 0
		          case 0: BXINV = new V4ConfigurationBit (fpga, new Bit(18, 4, 16), T, TB); break;
		          case 1: BXINV = new V4ConfigurationBit (fpga, new Bit(18, 4, 32), T, TB); break;
		          default: System.out.println(YErrorMsg); break;
		    	} break;
		    case 1: 
		    	switch (Y) {
		    	  case 0: BXINV = new V4ConfigurationBit (fpga, new Bit(18, 1, 4), T, TB); break; 
		    	  case 1: BXINV = new V4ConfigurationBit (fpga, new Bit(18, 1, 8), T, TB); break;
		    	  default: System.out.println(YErrorMsg); break; 
		    	} break;
		    default : System.out.println(XErrorMsg); break;
		   } 	
		    
		   BXINV.SetBitValueInBitStream(0, T);
		   	
	}

	public Instance getXDLInstance(){
    	return XDLInstance;
    }
    
    private void setUnRepresentedSliceAttributes() {
		XUSED = new Attribute("XUSED","", "#OFF");
		YUSED = new Attribute("YUSED","", "#OFF");
		XDLInstance.addAttribute(XUSED);
		XDLInstance.addAttribute(YUSED);
	}
    
    public void setXUsedAttribute(String AttrValue){
       if(AttrValue.equals("#OFF")){
    	   XUSED.setValue(AttrValue);
       } else if(AttrValue.equals("0")){
    	   XUSED.setValue("0");
       }
    }
    
    public void setYUsedAttribute(String AttrValue){
        if(AttrValue.equals("#OFF")){
     	   YUSED.setValue(AttrValue);
        } else if(AttrValue.equals("0")){
     	   YUSED.setValue("0");
        }
     }
    
	private void setType() {
		if(X==0){
		  this.Type = PrimitiveType.SLICEM;
		} else {
		  this.Type = PrimitiveType.SLICEL;
		}
	}

	private void CreateInstanceAndSetPrimitiveSite() {
        this.XDLInstance = new Instance("Virtex4" + Type + "X"+X+"Y"+Y+"Tile"+T, Type);
        System.out.println("X, Y, Size " + X + " " + Y + " " + T.getPrimitiveSites().length);
        this.XDLInstance.place(T.getPrimitiveSites()[2*X+Y]);
        design.addInstance(this.XDLInstance);
	}

	public static Virtex4Slice getSliceFromPrimitiveSite(FPGA fpga, Design design, PrimitiveSite P,  V4XilinxToolbox TB){
    	if(P.getType() == PrimitiveType.SLICEL ||
    	   P.getType() == PrimitiveType.SLICEM){
    	  
    	   Instance I = new Instance();
    	   I.place(P);
    	   
    	   return new Virtex4Slice(I.getInstanceX()%2, I.getInstanceY()%2, fpga, design, I.getTile(), TB);
    	} 	    
    	return null; 
    }
    
			
    public static Virtex4Slice getSliceFromInstance(FPGA fpga, Design design, Instance I, V4XilinxToolbox TB){
       if(I.getPrimitiveSite().getType() == PrimitiveType.SLICEL ||
    	  I.getPrimitiveSite().getType() == PrimitiveType.SLICEM){
    
    	   return new Virtex4Slice(I.getInstanceX()%2, I.getInstanceY()%2, fpga, design, I.getTile(), TB);
       }
       else return null;
    }
    
	public int getY() {
		return Y;
	}


	private void setY(int y) {
		this.Y = y;
	}

	public void setX(int x) {
		this.X = x;
	}

	public int getX() {
		return this.X;
	}

	public PrimitiveType getTYPE() {
		return Type;
	}

	public void setLutG(Virtex4Lut lutG) {
		this.LutG = lutG;
	}

	public Virtex4Lut getLutG() {
		return LutG;
	}

	public void setLutF(Virtex4Lut lutF) {
		this.LutF = lutF;
	}

	public Virtex4Lut getLutF() {
		return LutF;
	}

	public void setFFX(Virtex4FlipFlop FFX) {
		this.FFX = FFX;
	}

	public Virtex4FlipFlop getFFX() {
		return FFX;
	}

	public void setFFY(Virtex4FlipFlop FFY) {
		this.FFY = FFY;
	}

	public Virtex4FlipFlop getFFY() {
		return FFY;
	}
	
	public static int ConvertSliceX(int X){
		return X % 2; 
	}
	
	public static int ConvertSliceY(int Y){
		return Y % 2; 
	}
	
	
	/*
	 * Set FlipFlop/Latch Value
	 * (#FF|#LATCH|#OFF)
	 */
	   
	public void setFFLatchAttributValue(String AttrValue){
	   
	   if(AttrValue.equals("#OFF")){
		   FFLatchBit.SetBitValueInBitStream(0, T);
	   } else if(AttrValue.contains("#LATCH")){
		   FFLatchBit.SetBitValueInBitStream(1, T);
	   } else if(AttrValue.contains("#FF")){
		   FFLatchBit.SetBitValueInBitStream(0, T);
	   } else {
		   System.out.println(AttrValue + " is not a proper Value for this Attribute");
	       System.exit(1);
	   }
	   
	   FFX.getFFLatchAttribute().setValue(AttrValue);
	   FFY.getFFLatchAttribute().setValue(AttrValue);
   }
	
	private void setFFLatchBit(FPGA fpga, Tile T, V4XilinxToolbox TB){
	   	   
		   String XErrorMsg      = "V4 FF:X Must be 0 or 1";
		   String YErrorMsg      = "V4 FF:Y Must be 0 or 1";
			   
	   	   switch(X){
			    case 0:     	
			    	switch (Y) {
			    	  //SLICE_X0Y0 FFX #FF    1 20 9 32 0 0
					  //SLICE_X0Y0 FFX #LATCH 1 20 9 32 1 0
					  //SLICE_X0Y0 FFX #OFF   1 20 9 32 0 0
			          case 0: FFLatchBit = new V4ConfigurationBit (fpga, new Bit(20, 9, 32), T, TB); break;
			          //SLICE_X0Y1 FFX #FF    1 20 0 32 0 0
					  //SLICE_X0Y1 FFX #LATCH 1 20 0 32 1 0
					  //SLICE_X0Y1 FFX #OFF   1 20 0 32 0 0      					          
			          case 1: FFLatchBit = new V4ConfigurationBit (fpga, new Bit(20, 0, 32), T, TB); break;
			          default: System.out.println(YErrorMsg); break;
			    	} break;
			    case 1: 
			    	switch (Y) {
			    	  //SLICE_X1Y0 FFX #FF    1 20 4 2 0 0
					  //SLICE_X1Y0 FFX #LATCH 1 20 4 2 1 0
					  //SLICE_X1Y0 FFX #OFF   1 20 4 2 0 0
			    	  case 0: FFLatchBit = new V4ConfigurationBit (fpga, new Bit(20, 4, 2), T, TB); break; 
			    	  //SLICE_X1Y1 FFX #FF    1 20 1 2 0 0
					  //SLICE_X1Y1 FFX #LATCH 1 20 1 2 1 0
					  //SLICE_X1Y1 FFX #OFF   1 20 1 2 0 0
			    	  case 1: FFLatchBit = new V4ConfigurationBit (fpga, new Bit(20, 1, 2), T, TB); break;
			    	  default: System.out.println(YErrorMsg); break; 
			    	} break;
			    default : System.out.println(XErrorMsg); break;
			   } 
			
		 	//Set the Attributes Correctly
	   	   
	   }

	private void setDXMUX0(DXMUX dXMUX0) {
		DXMUX0 = dXMUX0;
	}

	public DXMUX getDXMUX0() {
		return DXMUX0;
	}

	private void setDYMUX0(DYMUX dYMUX0) {
		DYMUX0 = dYMUX0;
	}

	public DYMUX getDYMUX0() {
		return DYMUX0;
	}
	
	public boolean VerifySlice(){
		//FFLatchBit;   
	    // BXINV;  
	    // CLKINV; 
	    // SRINV; 
	    //   SYNC_BIT;
		
		// BXINV_ATTR; 
	    // CLKINV_ATTR; 
	    // SYNC_ATTR; 
	    // SRINV_ATTR;
	    // SRFFMUX; 
		
		if( VerifyFFLatch() &&
			VerifyBXINV()   &&
			VerifyCLKINV()  &&
			VerifySRINV()   &&
			VerifySYNC()    &&
			DXMUX0.Verify() &&
			DYMUX0.Verify() &&
			FFX.Verify()    &&
			FFY.Verify()    &&
			LutG.Verify()   &&
			LutF.Verify()){
			return true;
		}
		
		return false;
	}

	private boolean VerifySYNC() {
	
		int CurrentValue = SYNC_BIT.getValue();
		
		if(CurrentValue == 0 && (SYNC_ATTR.getValue().equals("#OFF") || SYNC_ATTR.getValue().equals("ASYNC"))){
			return true;
		} else if(CurrentValue == 1 && SYNC_ATTR.getValue().equals("SYNC")){
			return true; 
		}
        
		System.out.println("VerifySYNC Failed.");
		return false;
	}

	private boolean VerifySRINV() {
		 int CurrentValue = SRINV.getValue();
	        
	     if(CurrentValue == 0 && (SRINV_ATTR.getValue().equals("OFF") || SRINV_ATTR.getValue().equals("SR_B"))){
	       	return true;
	     } else if(CurrentValue == 1 && (SRINV_ATTR.getValue().equals("SR"))){
	       	return true;
	     }
	     
	     System.out.println("VerifySRINV Failed. BitValue: " + CurrentValue + " " + SRINV_ATTR);
		 return false;
	}

	private boolean VerifyCLKINV() {
		 int CurrentValue = CLKINV.getValue();
	        
	     if(CurrentValue == 0 && (CLKINV_ATTR.getValue().equals("OFF") || CLKINV_ATTR.getValue().equals("CLK_B"))){
	       	return true;
	     } else if(CurrentValue == 1 && (CLKINV_ATTR.getValue().equals("CLK"))){
	       	return true;
	     }
	    
	     System.out.println("VerifyCLKINV Failed.");
		 return false;
	}

	private boolean VerifyBXINV() {
				
        int CurrentValue = BXINV.getValue();
        
        if(CurrentValue == 0 && (BXINV_ATTR.getValue().equals("OFF") || BXINV_ATTR.getValue().equals("BX_B"))){
        	return true;
        } else if(CurrentValue == 1 && (BXINV_ATTR.getValue().equals("BX"))){
        	return true;
        }
        
        System.out.println("VerifyBXINV Failed. BitValue: " + CurrentValue + " " + BXINV_ATTR);
		return false;
	}

	private boolean VerifyFFLatch() {
		int CurrentValue = FFLatchBit.getValue();
		String FFXINITATTRVALUE = FFX.getFFLatchAttribute().getValue();
		String FFYINITATTRVALUE = FFY.getFFLatchAttribute().getValue();
		
		//Zero is #OFF or Flip-Flop
		if(CurrentValue == 0 && (FFXINITATTRVALUE.equals("#OFF") || FFXINITATTRVALUE.equals("#FF")) 
				             && (FFYINITATTRVALUE.equals("#OFF") || FFYINITATTRVALUE.equals("#FF"))){
			return true; 
		} else if(CurrentValue == 1 && (FFXINITATTRVALUE.equals("#LATCH"))  
				                    && (FFYINITATTRVALUE.equals("#LATCH"))){
			return true;
		}
		
		System.out.println("VerifyFFLatch Failed.");
		return false;
	}
}

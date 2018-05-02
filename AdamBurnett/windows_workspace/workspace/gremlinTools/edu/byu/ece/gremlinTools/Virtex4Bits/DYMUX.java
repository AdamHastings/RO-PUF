package edu.byu.ece.gremlinTools.Virtex4Bits;

import edu.byu.ece.gremlinTools.xilinxToolbox.V4XilinxToolbox;
import edu.byu.ece.rapidSmith.bitstreamTools.configuration.FPGA;
import edu.byu.ece.rapidSmith.design.Attribute;
import edu.byu.ece.rapidSmith.design.Design;
import edu.byu.ece.rapidSmith.design.Instance;
import edu.byu.ece.rapidSmith.device.Tile;


public class DYMUX {
	Attribute DYMUX_ATTR;
    Instance XDLInstance; 
    
    V4ConfigurationBit  Bit0;
    V4ConfigurationBit  Bit1;
    V4ConfigurationBit  Bit2;
    V4ConfigurationBit  Bit3;
    V4ConfigurationBit  Bit4;
    
    int X; 
    int Y; 
    
    FPGA fpga;
    Tile T; 
    Design design; 
    V4XilinxToolbox TB; 
    
    public DYMUX(int X, int Y, Design design, Instance XDLInstance, Tile T, FPGA fpga, V4XilinxToolbox TB){
    	
    	DYMUX_ATTR = new Attribute("DYMUX","","#OFF");
    	this.XDLInstance = XDLInstance; 
    	XDLInstance.addAttribute(DYMUX_ATTR);
    	this.fpga = fpga; 
    	this.TB   = TB; 
    	this.T = T; 
    	this.X = X; 
    	this.Y = Y; 
    	
    	setBits();
    	setDYMUX_ATTR("#OFF");
    }
    
    public void setDYMUX_ATTR(String AttrValue){
    	if(AttrValue.equals("#OFF")){
    		Bit4.SetBitValueInBitStream(0, T);
    		Bit3.SetBitValueInBitStream(0, T);
    		Bit2.SetBitValueInBitStream(0, T);
    		Bit1.SetBitValueInBitStream(0, T);
    		Bit0.SetBitValueInBitStream(1, T);		   		
    	} else if(AttrValue.equals("YMUX")){
    		Bit4.SetBitValueInBitStream(1, T);
    		Bit3.SetBitValueInBitStream(0, T);
    		Bit2.SetBitValueInBitStream(1, T);
    		Bit1.SetBitValueInBitStream(0, T);
    		Bit0.SetBitValueInBitStream(1, T);	
    	} else if(AttrValue.equals("Y")){
    		Bit4.SetBitValueInBitStream(0, T);
    		Bit3.SetBitValueInBitStream(0, T);
    		Bit2.SetBitValueInBitStream(0, T);
    		Bit1.SetBitValueInBitStream(0, T);
    		Bit0.SetBitValueInBitStream(0, T);	
    	} else if(AttrValue.equals("YB")){
    		Bit4.SetBitValueInBitStream(0, T);
    		Bit3.SetBitValueInBitStream(1, T);
    		Bit2.SetBitValueInBitStream(0, T);
    		Bit1.SetBitValueInBitStream(0, T);
    		Bit0.SetBitValueInBitStream(1, T);	
    	} else if(AttrValue.equals("BY")){
    		Bit4.SetBitValueInBitStream(0, T);
    		Bit3.SetBitValueInBitStream(0, T);
    		Bit2.SetBitValueInBitStream(0, T);
    		Bit1.SetBitValueInBitStream(1, T);
    		Bit0.SetBitValueInBitStream(1, T);	
    	} else {
    		System.out.println("DXMUX setDXMUX: Invalid Value for DXMUX_ATTR");
    		System.exit(1);
    	}
    	
    	DYMUX_ATTR.setValue(AttrValue);
    }
    
	private void setBits() {
		
		switch(X){
		case 0:
			switch(Y){
			case 0:
				Bit4 = new V4ConfigurationBit (fpga, new Bit(18, 8, 4), T, TB);
				Bit3 = new V4ConfigurationBit (fpga, new Bit(18, 9, 1), T, TB);
				Bit2 = new V4ConfigurationBit (fpga, new Bit(18, 9, 2), T, TB);
				Bit1 = new V4ConfigurationBit (fpga, new Bit(18, 9, 128), T, TB);
				Bit0 = new V4ConfigurationBit (fpga, new Bit(20, 5, 1), T, TB);
				break; 
			case 1:
				Bit4 = new V4ConfigurationBit (fpga, new Bit(18, 3, 32), T, TB);
				Bit3 = new V4ConfigurationBit (fpga, new Bit(18, 2, 4), T, TB);
				Bit2 = new V4ConfigurationBit (fpga, new Bit(18, 2, 16), T, TB);
				Bit1 = new V4ConfigurationBit (fpga, new Bit(18, 3, 4), T, TB);
				Bit0 = new V4ConfigurationBit (fpga, new Bit(20, 2, 16), T, TB);
				break;
			default: System.out.println("DYMUX setBits(): Invalid Y Value");
			}
			break; 
		case 1:
			switch(Y){
			case 0:
				Bit4 = new V4ConfigurationBit (fpga, new Bit(18, 7, 32), T, TB);
				Bit3 = new V4ConfigurationBit (fpga, new Bit(18, 7, 16), T, TB);
				Bit2 = new V4ConfigurationBit (fpga, new Bit(18, 7, 128), T, TB);
				Bit1 = new V4ConfigurationBit (fpga, new Bit(18, 7, 64), T, TB);
				Bit0 = new V4ConfigurationBit (fpga, new Bit(20, 6, 128), T, TB);
				break;
			case 1: 
				Bit4 = new V4ConfigurationBit (fpga, new Bit(18, 3, 1), T, TB);
				Bit3 = new V4ConfigurationBit (fpga, new Bit(18, 2, 64), T, TB);
				Bit2 = new V4ConfigurationBit (fpga, new Bit(18, 2, 128), T, TB);
				Bit1 = new V4ConfigurationBit (fpga, new Bit(18, 2, 32), T, TB);
				Bit0 = new V4ConfigurationBit (fpga, new Bit(20, 3, 128), T, TB);
				break;
			default: 
			}
			break; 
		default: System.out.println("DYMUX setbits(): Invalid Value for Y.");
		}
	}

	public boolean Verify() {
		int bit4 = Bit4.getValue();
    	int bit3 = Bit3.getValue();
    	int bit2 = Bit2.getValue();
    	int bit1 = Bit1.getValue();
    	int bit0 = Bit0.getValue();
    	
    	if( bit4 == 0 && bit3 == 0 && bit2 == 0 && bit1 == 0 && bit0 == 1 && 
    		DYMUX_ATTR.getValue().equals("#OFF")){
    		return true;
    	}
    	
    	if( bit4 == 1 && bit3 == 0 && bit2 == 1 && bit1 == 0 && bit0 == 1 && 
        	DYMUX_ATTR.getValue().equals("YMUX")){
        	return true;
        }
    	
    	if( bit4 == 0 && bit3 == 0 && bit2 == 0 && bit1 == 0 && bit0 == 0 && 
        	DYMUX_ATTR.getValue().equals("Y")){
        	return true;
        }
    	
    	if( bit4 == 0 && bit3 == 1 && bit2 == 0 && bit1 == 0 && bit0 == 1 && 
        	DYMUX_ATTR.getValue().equals("YB")){
        	return true;
        }
    	
    	if( bit4 == 0 && bit3 == 0 && bit2 == 0 && bit1 == 1 && bit0 == 1 && 
        	DYMUX_ATTR.getValue().equals("BY")){
        	return true;
        }
    	System.out.println("VerifyDYMUX Failed.");
    	return false; 
	}
}

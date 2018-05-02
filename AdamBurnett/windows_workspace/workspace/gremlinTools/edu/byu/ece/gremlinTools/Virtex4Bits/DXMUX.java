package edu.byu.ece.gremlinTools.Virtex4Bits;

import edu.byu.ece.gremlinTools.xilinxToolbox.V4XilinxToolbox;
import edu.byu.ece.rapidSmith.bitstreamTools.configuration.FPGA;
import edu.byu.ece.rapidSmith.design.Attribute;
import edu.byu.ece.rapidSmith.design.Design;
import edu.byu.ece.rapidSmith.design.Instance;
import edu.byu.ece.rapidSmith.device.Tile;

public class DXMUX {
    
	Attribute DXMUX_ATTR;
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
    
    public DXMUX(int X, int Y, Design design, Instance XDLInstance, Tile T, FPGA fpga, V4XilinxToolbox TB){
    	
    	DXMUX_ATTR = new Attribute("DXMUX","","#OFF");
    	this.XDLInstance = XDLInstance; 
    	XDLInstance.addAttribute(DXMUX_ATTR);
    	this.fpga = fpga; 
    	this.TB   = TB; 
    	this.T = T; 
    	this.X = X; 
    	this.Y = Y; 
    	
    	setBits();
    	setDXMUX_ATTR("#OFF");
    }
    
    public boolean Verify(){
    	int bit4 = Bit4.getValue();
    	int bit3 = Bit3.getValue();
    	int bit2 = Bit2.getValue();
    	int bit1 = Bit1.getValue();
    	int bit0 = Bit0.getValue();
    	
    	if( bit4 == 0 && bit3 == 0 && bit2 == 0 && bit1 == 0 && bit0 == 1 && 
    		DXMUX_ATTR.getValue().equals("#OFF")){
    		return true;
    	}
    	
    	if( bit4 == 1 && bit3 == 0 && bit2 == 1 && bit1 == 0 && bit0 == 1 && 
        	DXMUX_ATTR.getValue().equals("XMUX")){
        	return true;
        }
    	
    	if( bit4 == 0 && bit3 == 0 && bit2 == 0 && bit1 == 0 && bit0 == 0 && 
        	DXMUX_ATTR.getValue().equals("X")){
        	return true;
        }
    	
    	if( bit4 == 0 && bit3 == 1 && bit2 == 0 && bit1 == 0 && bit0 == 1 && 
        	DXMUX_ATTR.getValue().equals("XB")){
        	return true;
        }
    	
    	if( bit4 == 0 && bit3 == 0 && bit2 == 0 && bit1 == 1 && bit0 == 1 && 
        	DXMUX_ATTR.getValue().equals("BX")){
        	return true;
        }
    	
    	System.out.println("VerifyDXMUX Failed.");
    	return false; 
    }
    
    public void setDXMUX_ATTR(String AttrValue){
    	if(AttrValue.equals("#OFF")){
    		Bit4.SetBitValueInBitStream(0, T);
    		Bit3.SetBitValueInBitStream(0, T);
    		Bit2.SetBitValueInBitStream(0, T);
    		Bit1.SetBitValueInBitStream(0, T);
    		Bit0.SetBitValueInBitStream(1, T);		   		
    	} else if(AttrValue.equals("XMUX")){
    		Bit4.SetBitValueInBitStream(1, T);
    		Bit3.SetBitValueInBitStream(0, T);
    		Bit2.SetBitValueInBitStream(1, T);
    		Bit1.SetBitValueInBitStream(0, T);
    		Bit0.SetBitValueInBitStream(1, T);	
    	} else if(AttrValue.equals("X")){
    		Bit4.SetBitValueInBitStream(0, T);
    		Bit3.SetBitValueInBitStream(0, T);
    		Bit2.SetBitValueInBitStream(0, T);
    		Bit1.SetBitValueInBitStream(0, T);
    		Bit0.SetBitValueInBitStream(0, T);	
    	} else if(AttrValue.equals("XB")){
    		Bit4.SetBitValueInBitStream(0, T);
    		Bit3.SetBitValueInBitStream(1, T);
    		Bit2.SetBitValueInBitStream(0, T);
    		Bit1.SetBitValueInBitStream(0, T);
    		Bit0.SetBitValueInBitStream(1, T);	
    	} else if(AttrValue.equals("BX")){
    		Bit4.SetBitValueInBitStream(0, T);
    		Bit3.SetBitValueInBitStream(0, T);
    		Bit2.SetBitValueInBitStream(0, T);
    		Bit1.SetBitValueInBitStream(1, T);
    		Bit0.SetBitValueInBitStream(1, T);	
    	} else {
    		System.out.println("DXMUX setDXMUX: Invalid Value for DXMUX_ATTR=" + AttrValue);
    		System.exit(1);
    	}
    	
    	DXMUX_ATTR.setValue(AttrValue);
    }
    
	private void setBits() {
		
		switch(X){
		case 0:
			switch(Y){
			case 0:
				Bit4 = new V4ConfigurationBit (fpga, new Bit(18, 8, 1), T, TB);
				Bit3 = new V4ConfigurationBit (fpga, new Bit(18, 8, 32), T, TB);
				Bit2 = new V4ConfigurationBit (fpga, new Bit(18, 8, 128), T, TB);
				Bit1 = new V4ConfigurationBit (fpga, new Bit(18, 9, 64), T, TB);
				Bit0 = new V4ConfigurationBit (fpga, new Bit(20, 8, 16), T, TB);
				break;
			case 1:
				Bit4 = new V4ConfigurationBit (fpga, new Bit(18, 2, 1), T, TB);
				Bit3 = new V4ConfigurationBit (fpga, new Bit(18, 2, 2), T, TB);
				Bit2 = new V4ConfigurationBit (fpga, new Bit(18, 2, 8), T, TB);
				Bit1 = new V4ConfigurationBit (fpga, new Bit(18, 3, 128), T, TB);
				Bit0 = new V4ConfigurationBit (fpga, new Bit(20, 7, 16), T, TB);
				break;
			default: System.out.println("DXMUX setBits(): Invalid Y Value X0Y" + Y);
			}
			break; 
		case 1:
			switch(Y){
			case 0:
				Bit4 = new V4ConfigurationBit (fpga, new Bit(18, 9, 4), T, TB);
				Bit3 = new V4ConfigurationBit (fpga, new Bit(18, 9, 8), T, TB);
				Bit2 = new V4ConfigurationBit (fpga, new Bit(18, 9, 16), T, TB);
				Bit1 = new V4ConfigurationBit (fpga, new Bit(18, 9, 32), T, TB);
				Bit0 = new V4ConfigurationBit (fpga, new Bit(20, 8, 8), T, TB);
				break;
			case 1: 
				Bit4 = new V4ConfigurationBit (fpga, new Bit(18, 7, 1), T, TB);
				Bit3 = new V4ConfigurationBit (fpga, new Bit(18, 7, 4), T, TB);
				Bit2 = new V4ConfigurationBit (fpga, new Bit(18, 7, 8), T, TB);
				Bit1 = new V4ConfigurationBit (fpga, new Bit(18, 7, 2), T, TB);
				Bit0 = new V4ConfigurationBit (fpga, new Bit(20, 7, 8), T, TB);
				break;
			default: System.out.println("DXMUX setBits(): Invalid Y Value X1Y" + Y);
			}
			break; 
		default: System.out.println("DXMUX setbits(): Invalid Value for X.");
		}
	}
}

package edu.byu.ece.gremlinTools.Virtex4Bits;

import java.util.ArrayList;

import edu.byu.ece.gremlinTools.xilinxToolbox.V4XilinxToolbox;
import edu.byu.ece.rapidSmith.bitstreamTools.configuration.FPGA;
import edu.byu.ece.rapidSmith.design.Attribute;
import edu.byu.ece.rapidSmith.design.Instance;
import edu.byu.ece.rapidSmith.device.Tile;


public class Virtex4Lut {
   private ArrayList<V4ConfigurationBit> Bits; 
   private FPGA fpga;
   private Tile T; 
   private V4XilinxToolbox TB; 
   private Instance XDLInstance; 
   private Attribute LutAttribute;
   private String Letter; 
   private Virtex4Slice Slice; 
   
   public String GetLetter(){
	   return this.Letter; 
   }
   public ArrayList<V4ConfigurationBit> GetBits(){
	   return Bits; 
   }
   
   public void ClearLutContents(){
	   //System.out.println("Clearing " + Bits.size() + " Bits.");
	   for(V4ConfigurationBit B : Bits){
		  // System.out.println(" B Tile : " + B.getTile() + " " + B.getFAR() + " " + B);
		   B.SetBitValueInBitStream(0, B.getTile());
	   }
	   this.LutAttribute.setPhysicalName(Letter);
	   this.LutAttribute.setLogicalName(XDLInstance.getName()+ "." +Letter);
	   this.LutAttribute.setValue("#LUT:D=0x0000");
   }
   
   public void setLutBit(int value, int index){
	   V4ConfigurationBit B = Bits.get(index);
	   //System.out.println("Changing Bit " + B.toString());
	   B.SetBitValueInBitStream(value, T);
   }
   
   public void AssertLutContent(){
	   for(V4ConfigurationBit B : Bits){
		   B.SetBitValueInBitStream(1, B.getTile());
	   }
	   this.LutAttribute.setPhysicalName(Letter);
	   this.LutAttribute.setLogicalName(XDLInstance.getName()+ "." +Letter);
	   this.LutAttribute.setValue("#LUT:D=0x1111");
   }
   
   public Virtex4Lut(int X, int Y, String Letter, FPGA fpga, Instance XDLInstance,  Tile T, V4XilinxToolbox TB, Virtex4Slice S){
	   Bits = new ArrayList<V4ConfigurationBit>(); 
	   this.fpga = fpga;
	   this.T = T;
	   this.TB = TB; 
	   this.XDLInstance = XDLInstance; 
	   this.Letter = Letter;
	   this.LutAttribute = new Attribute(Letter, "", "#OFF");
	   this.XDLInstance.addAttribute(LutAttribute);
	   this.Slice = S; 
	   if(X == 0 && Y == 0 && Letter.equals("G")){	   
	       Bits.add(new V4ConfigurationBit(fpga, new Bit(21, 6, 2), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(21, 6, 4), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(21, 6, 8), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(21, 6, 16), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(21, 6, 32), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(21, 6, 64), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(21, 6, 128), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(21, 5, 1), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(21, 5, 2), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(21, 5, 4), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(21, 5, 8), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(21, 5, 16), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(21, 5, 32), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(21, 5, 64), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(21, 5, 128), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(21, 4, 1), T, TB));
	   }
	   else if(X==0 && Y ==0 && Letter.equals("F")){
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(21,9,1), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(21,9,2), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(21,9,4), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(21,9,8), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(21,9,16), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(21,9,32), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(21,9,64), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(21,9,128), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(21,8,1), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(21,8,2), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(21,8,4), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(21,8,8), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(21,8,16), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(21,8,32), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(21,8,64), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(21,8,128), T, TB));
	   } else if(X==0 && Y == 1 && Letter.equals("G")){
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(21,3,2), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(21,3,4), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(21,3,8), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(21,3,16), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(21,3,32), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(21,3,64), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(21,3,128), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(21,2,1), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(21,2,2), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(21,2,4), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(21,2,8), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(21,2,16), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(21,2,32), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(21,2,64), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(21,2,128), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(21,1,1), T, TB));
	   } else if(X==0 && Y == 1 && Letter.equals("F")){
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(21,0,1), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(21,0,2), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(21,0,4), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(21,0,8), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(21,0,16), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(21,0,32), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(21,0,64), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(21,0,128), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(21,7,1), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(21,7,2), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(21,7,4), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(21,7,8), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(21,7,16), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(21,7,32), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(21,7,64), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(21,7,128), T, TB));
	   } else if(X==1 && Y == 0 && Letter.equals("G")){
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(19, 6, 2), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(19, 6, 4), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(19, 6, 8), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(19, 6, 16), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(19, 6, 32), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(19, 6, 64), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(19, 6, 128), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(19, 5, 1), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(19, 5, 2), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(19, 5, 4), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(19, 5, 8), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(19, 5, 16), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(19, 5, 32), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(19, 5, 64), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(19, 5, 128), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(19, 4, 1), T, TB));   
	   }else if(X==1 && Y == 0 && Letter.equals("F")){
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(19, 9, 1), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(19, 9, 2), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(19, 9, 4), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(19, 9, 8), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(19, 9, 16), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(19, 9, 32), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(19, 9, 64), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(19, 9, 128), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(19, 8, 1), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(19, 8, 2), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(19, 8, 4), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(19, 8, 8), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(19, 8, 16), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(19, 8, 32), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(19, 8, 64), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(19, 8, 128), T, TB));   
	   }else if(X==1 && Y == 1 && Letter.equals("G")){
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(19, 3, 2), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(19, 3, 4), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(19, 3, 8), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(19, 3, 16), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(19, 3, 32), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(19, 3, 64), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(19, 3, 128), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(19, 2, 1), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(19, 2, 2), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(19, 2, 4), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(19, 2, 8), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(19, 2, 16), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(19, 2, 32), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(19, 2, 64), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(19, 2, 128), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(19, 1, 1), T, TB)); 
	   }else if(X==1 && Y == 1 && Letter.equals("F")){
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(19, 0, 1), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(19, 0, 2), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(19, 0, 4), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(19, 0, 8), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(19, 0, 16), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(19, 0, 32), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(19, 0, 64), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(19, 0, 128), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(19, 7, 1), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(19, 7, 2), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(19, 7, 4), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(19, 7, 8), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(19, 7, 16), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(19, 7, 32), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(19, 7, 64), T, TB));
		   Bits.add(new V4ConfigurationBit(fpga, new Bit(19, 7, 128), T, TB));
   	   }	   
   }

public boolean Verify() {
	int CurrentValue = Bits.get(0).getValue();
	
	System.out.println("Verifying LUT " + CurrentValue + "##################################");
	
	for(V4ConfigurationBit B : Bits){
		if(CurrentValue != B.getValue()){
			System.out.println("VerifyLUT Failed in Comp.");
			return false;
		}
	}
	
	if(CurrentValue == 0 && (LutAttribute.getValue().equals("#LUT:D=0x0000") || LutAttribute.getValue().equals("#OFF"))){
		return true;
	}
	
	if(CurrentValue == 1 && LutAttribute.getValue().equals("#LUT:D=0x1111")){
		return true;
	}
	
	System.out.println("VerifyLUT. " + CurrentValue + " " + LutAttribute);
	return false;
}

public void setSlice(Virtex4Slice slice) {
	Slice = slice;
}

public Virtex4Slice getSlice() {
	return Slice;
}
   
   
}

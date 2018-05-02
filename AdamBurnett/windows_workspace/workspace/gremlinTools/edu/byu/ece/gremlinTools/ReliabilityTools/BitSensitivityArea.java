package edu.byu.ece.gremlinTools.ReliabilityTools;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import edu.byu.ece.gremlinTools.Virtex4Bits.Virtex4SliceBits;
import edu.byu.ece.gremlinTools.Virtex4Bits.Virtex4SwitchBox;
import edu.byu.ece.gremlinTools.xilinxToolbox.V4XilinxToolbox;
import edu.byu.ece.rapidSmith.bitstreamTools.configuration.FPGA;
import edu.byu.ece.rapidSmith.design.Attribute;
import edu.byu.ece.rapidSmith.design.Design;
import edu.byu.ece.rapidSmith.design.Instance;
import edu.byu.ece.rapidSmith.design.Net;
import edu.byu.ece.rapidSmith.design.PIP;
import edu.byu.ece.rapidSmith.device.PrimitiveType;
import edu.byu.ece.rapidSmith.device.TileType;
import edu.byu.ece.rapidSmith.device.WireEnumerator;
import edu.byu.ece.rapidSmith.device.helper.WireExpressions;

public class BitSensitivityArea {
   
   protected Design design; 
   protected FPGA fpga; 
   protected V4XilinxToolbox TB;
   protected Virtex4SwitchBox SB; 
   protected WireExpressions WEx; 
   protected WireEnumerator We; 
   
   protected Virtex4SliceBits SliceX0Y0;
   protected Virtex4SliceBits SliceX0Y1;
   protected Virtex4SliceBits SliceX1Y0;
   protected Virtex4SliceBits SliceX1Y1;
   
   private int primaryBits;
   //Number of Configuration Bits in Routing Switches
   private final int DOUBLE_BITS = 8; //4X4 = 8  Bits
   private final int HEX_BITS = 7;    //4X3 = 7  Bits
   private final int LONG_BITS = 20;  //2X9 + Buffer Bit + Other Buffer Bit = 20 Bits
   private final int IMUX_BITS = 10;  //4X6 = 10 Bits
   private final int OMUX_BITS = 13;  //4X9 = 13 Bits
   private final int BOUNCE_BITS=8;   //4X4 = 8
   private final int CLKSRCE_B=8;     //4X4 = 8
   
   public BitSensitivityArea(Design design, FPGA fpga, V4XilinxToolbox TB, Virtex4SliceBits S00, Virtex4SliceBits S01, Virtex4SliceBits S10, Virtex4SliceBits S11){
	   this.design = design; 
	   this.fpga = fpga; 
	   this.TB   = TB; 
	   this.WEx  = new WireExpressions();
	   We = design.getWireEnumerator(); 
	   SB = new Virtex4SwitchBox(fpga, design, TB);
	   
	   SliceX0Y0 = S00; 
	   SliceX0Y1 = S01; 
	   SliceX1Y0 = S10; 
	   SliceX1Y1 = S11; 
	   
	   primaryBits = 0;
   }
   
   public int calcLogicArea(Collection<Instance> Instances){
	   int totalBits = 0; 
	   
	   for(Instance I: Instances){
		  if(I.getType() == PrimitiveType.SLICEL || I.getType() == PrimitiveType.SLICEM) { 
			   for(Attribute A : I.getAttributes()){
				   if(!A.getValue().equals("#OFF")){
					   Virtex4SliceBits S = getSlice(I);
					   if(S.getBits(new Attribute(A.getPhysicalName(), "", "#OFF"))!= null){
						   totalBits += S.getBits(new Attribute(A.getPhysicalName(), "", "#OFF")).size();
					   } else {
						   if(A.getPhysicalName().equals("_GND_SOURCE")||A.getPhysicalName().equals("_VCC_SOURCE")){
							   totalBits+=1; 
						   }
						  // System.out.println(A);
					   }
				   }
			   }
		  } else if (I.getType() == PrimitiveType.DSP48) {
			  // for now just add an offset
			  // the value we choose is (#frames - routing frames) * 1312 / 2
			  // the "/2" is a general way of saying - we don't use every bit in the frame
			  totalBits += (21 - 17) * 1312 / (8 * 2);
		  } else if (I.getType() == PrimitiveType.IOB) {
			  totalBits += (30 - 17) * 1312 / (32 * 10);
		  }
	   }
	   return totalBits; 
   }
   
   public void reportArea(){
	int Int_Bits =  calcInterconnectArea(design.getNets()); 
   	int Logic_Bits =calcLogicArea(design.getInstances());
   	System.out.println("---------------------------------------------------");
   	System.out.println("                 System Area Report                ");
   	System.out.println("---------------------------------------------------");
   	
   	System.out.println("Interconnect: " + Int_Bits + " Bits");
   	System.out.println("Primary Interconnect: " + primaryBits + " Bits");
   	System.out.println("Logic:        " + Logic_Bits + " Bits");
   	System.out.println("Total:        " + (Logic_Bits+Int_Bits) + " Bits");
   }
   
   public Virtex4SliceBits getSlice(Instance I){
	   int X = I.getInstanceX()%2;
	   int Y = I.getInstanceY()%2; 
	   
	   if(X == 0 && Y==0){
		   return SliceX0Y0;
	   } else if(X == 0 && Y==1){
		   return SliceX0Y1;
	   } else if(X == 1 && Y==0){
		   return SliceX1Y0; 
	   } else if(X == 1 && Y==1){
		   return SliceX1Y1; 
	   }  else {
		   return null; 
	   }
   }
   
   public void calcComponentBitArea(Component C){
	   C.setBitArea(calcComponentInterconnectArea(C) + calcComponentLogicArea(C)); 
   }
   
   public int calcComponentInterconnectArea(Component C){
	   int ia = calcInterconnectArea(C.getNets());
	   C.setInterconnectArea(ia);
	   C.setPrimaryBitArea(primaryBits);
	   return ia;
   }
   
   public int calcComponentLogicArea(Component C){
	   int la = calcLogicArea(C.getInstances()); 
	   return la;
   }
   
   public int calcInterconnectArea(Collection<Net> Nets){
	   
	   int totalBits = 0; 
	   primaryBits=0;  
	   HashSet<TileType> UnaccountedForTileType = new HashSet<TileType>();
	   for(Net N: Nets){
		   for(PIP P:N.getPIPs()){
			   if(P.getTile().getType().equals(TileType.INT) || P.getTile().getType().equals(TileType.INT_SO)){
				  switch (WEx.getWireType(We.getWireName(P.getEndWire()))) {
					  case DOUBLE :
						  totalBits += DOUBLE_BITS; 
						  primaryBits += 2; 
						  break; 
					  case HEX : 
						  totalBits += HEX_BITS;
						  primaryBits += 2;
						  break; 
					  case LONG :
						  totalBits += LONG_BITS;
						  primaryBits += 3;
						  break; 
					  case OMUX :
						  totalBits += OMUX_BITS;
						  primaryBits += 2;
						  break; 
					  case BOUNCE:
						  totalBits += BOUNCE_BITS;
						  primaryBits += 2;
						  break; 
					  case INT_SINK: 
						  primaryBits += 2;
						  if(We.getWireName(P.getEndWire()).startsWith("IMUX")){
							  totalBits += IMUX_BITS;
						  } else {
							  totalBits += CLKSRCE_B;
						  }
						  break; 
					  default:
						  
						  break; 
				  }
			   } else if(!P.getTile().getType().equals(TileType.CLB)){
				   UnaccountedForTileType.add(P.getTile().getType()); 
			   }
		   }
		   
	   }
	   
	   //Iterator<TileType> I = UnaccountedForTileType.iterator();
	   //while(I.hasNext()){
		//   System.out.println(I.next());
	   //}
	   return totalBits; 
   }
   
   
}

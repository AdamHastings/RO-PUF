package edu.byu.ece.gremlinTools.Virtex4Bits;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;


import XDLDesign.Utils.XDLDesignUtils;

import com.trolltech.qt.gui.QFileDialog;

import edu.byu.ece.gremlinTools.WireMux.WireMux;
import edu.byu.ece.gremlinTools.bitstreamDatabase.VerifyDB;
import edu.byu.ece.gremlinTools.xilinxToolbox.V4XilinxToolbox;
import edu.byu.ece.gremlinTools.xilinxToolbox.XilinxToolboxLookup;
import edu.byu.ece.rapidSmith.bitstreamTools.configuration.FPGA;
import edu.byu.ece.rapidSmith.bitstreamTools.configurationSpecification.DeviceLookup;
import edu.byu.ece.rapidSmith.design.Attribute;
import edu.byu.ece.rapidSmith.design.Design;
import edu.byu.ece.rapidSmith.design.Instance;
import edu.byu.ece.rapidSmith.design.Net;
import edu.byu.ece.rapidSmith.design.PIP;
import edu.byu.ece.rapidSmith.device.PrimitiveType;
import edu.byu.ece.rapidSmith.device.Tile;

public class Virtex4Bitgen {
   	
	private Design design;
	private FPGA fpga; 
	private V4XilinxToolbox TB; 
	
	private Virtex4SliceBits SliceX0Y0; 
	private Virtex4SliceBits SliceX0Y1; 
	private Virtex4SliceBits SliceX1Y0; 
	private Virtex4SliceBits SliceX1Y1;
	
	private Virtex4SwitchBox SB; 
	private WireMux [] OutputMux;
	private HashSet<Tile> AllIntTilesCorrespondingToUsedCLBTiles; 
	
	private String blankfilename;
	
	public Virtex4Bitgen(String DesignFileName){
		
		if(DesignFileName == null){
		  System.out.println("Invalid XDL File, bitfile not generated."); 
		  System.exit(1);
		} else if(!DesignFileName.endsWith(".xdl")){
		  System.out.println("Invalid XDL File, bitfile not generated."); 
		  System.exit(1);	
		}
			
		design = new Design();
		design.loadXDLFile(DesignFileName);
		String partname = design.getPartName();
		String shortPartName = null;
		if(partname.equals("xc4vlx60ff668-12")){
			blankfilename = "Z:\\FPGA\\Gremlin\\PUF_TestBoardExperiments\\blank.bit";
			shortPartName = "XC4VLX60";
			//Switch to Blank XC4VFX12
			//blankfilename = "Z:\\FPGA\\Gremlin\\PUF_TestBoardExperiments\\Verification\\V4EthernetICAP_VerifyCLB\\top_replaced_routed.bit";
		} else {
			System.out.println("This part is not Supported.");
			System.out.println("Add Support for Additional V4 Parts by Creating a Blank Bitstream and adding it to this if/else.");
		    System.exit(1);
		}
				
		fpga = new FPGA(DeviceLookup.lookupPartV4V5V6(shortPartName));
   	    TB = (V4XilinxToolbox) XilinxToolboxLookup.toolboxLookup(fpga.getDeviceSpecification());
		
		SliceX0Y0 = new Virtex4SliceBits(0, 0, PrimitiveType.SLICEM, fpga, design.getDevice(), TB);
		SliceX0Y1 = new Virtex4SliceBits(0, 1, PrimitiveType.SLICEM, fpga, design.getDevice(), TB);
		SliceX1Y0 = new Virtex4SliceBits(1, 0, PrimitiveType.SLICEL, fpga, design.getDevice(), TB);
		SliceX1Y1 = new Virtex4SliceBits(1, 1, PrimitiveType.SLICEL, fpga, design.getDevice(), TB);
	    
	    SB = new Virtex4SwitchBox(fpga, design, TB);
	    SB.setTiles(design.getDevice().getTile(5, 7), design.getDevice().getTile(5, 8));
		AllIntTilesCorrespondingToUsedCLBTiles = new HashSet<Tile>();
		
		for(Instance I : design.getInstances()){
		   AllIntTilesCorrespondingToUsedCLBTiles.add(XDLDesignUtils.getMatchingIntTile(I.getTile(), design.getDevice()));	
		}
		populateOutputMux(); 
		System.out.println("Done Initializing");
	}
	
	public void setBitinAllIntTiles(V4ConfigurationBit B, int value){
		Iterator<Tile> Tiles = getAllIntTilesIterator();
		while(Tiles.hasNext()){
			Tile T = Tiles.next(); 
			//B.setTile(T);
			//int i = B.getValue();
			B.SetBitValueInBitStream(value,T);
		}
	}
	
	public Iterator<Tile> getAllIntTilesIterator(){
		return AllIntTilesCorrespondingToUsedCLBTiles.iterator();
	}
	
	public WireMux getWireMux(String Wire){
		return SB.getWireMux(Wire);
	}
	
	public FPGA getFPGA(){
		return fpga;
	}
	public V4XilinxToolbox getTB(){
		return TB; 
	}
	public Design getDesign(){
		return design; 
	}
	public Virtex4SwitchBox getSB(){
		return SB; 
	}
	
	private void populateOutputMux() {
		
		OutputMux = new WireMux[16];
	    
		//Load the WireMux
	    for(int i=0; i<16; i++){
	    	OutputMux[i]=new WireMux("OMUX" + i);
	    	OutputMux[i].PopulateTable(false);
	    	//OutputMux[i].TransposeTable();
	    }
	 
	}
   
	public void setColumnBitByTile(String WireName, int max, Tile T){
		if(max > 0 && max <= 16){
			int wire = design.getWireEnumerator().getWireEnum(WireName);
			if(wire != -1)
			{
					for(int i=0; i<max; i++){
				       Bit B = OutputMux[i].getTable().getPip(wire).getColumnBit();	
				       V4ConfigurationBit V4B = new V4ConfigurationBit(fpga,B,T,TB);
				       V4B.SetBitValueInBitStream(1, T);
					}
			
			} else {
				System.out.println("Virtex4Bitgen.setColumnBit(): Invalid Wire Passed");
				System.exit(1);
			}
		}
	}
	
	public void setColumnBit(String WireName, int max){
		if(max > 0 && max <= 16){
				
			ArrayList<Tile> Tiles = XDLDesignUtils.getAllCLBTiles(design.getDevice());
			
			int wire = design.getWireEnumerator().getWireEnum(WireName);
			if(wire != -1)
			{
				for(Tile T : Tiles){
				  if(T.getTileYCoordinate() != 127){	
					for(int i=0; i<max; i++){
				       Bit B = OutputMux[i].getTable().getPip(wire).getColumnBit();	
				       V4ConfigurationBit V4B = new V4ConfigurationBit(fpga,B,T,TB);
				       V4B.SetBitValueInBitStream(1, T);
					}
				  }
				}
			} else {
				System.out.println("Virtex4Bitgen.setColumnBit(): Invalid Wire Passed");
				System.exit(1);
			}
		} else {
			System.out.println("Max Value Too High. " + max);
		}
	}
	
	public void generateBitstream(){
		
		
		XDLDesignUtils.loadBitstream(fpga, blankfilename);
		System.out.println("This part is : " +design.getDevice().getPartName());
			
		
		//Generate Slices
		int i = 0;
		for(Instance I : design.getInstances()){
		   //if(i%10 == 0 ) System.out.println(".");
		   configureSlices(I);
		}
		
		for(Net N : design.getNets()){
		   setNet(N);	
	  	}
		//Todo Generate Nets
		
		verify();
		
    }
     
	private void setNet(Net n) {
		SB.ActivatePIPs(n.getPIPs());
	}

	public void writeBitstream(String BitFilename){
		System.out.println("Writing Bitstream!");
		XDLDesignUtils.writeBitstreamToFile(fpga, BitFilename);
		System.out.println("Done.");
	}

	private void configureSlices(Instance I){
	
	if(I.getPrimitiveSite().getType() == PrimitiveType.SLICEL ||
	   I.getPrimitiveSite().getType() == PrimitiveType.SLICEM)
    {
	  	int X = I.getInstanceX()%2;
	  	int Y = I.getInstanceY()%2; 
	  	
	  	for(Attribute A : I.getAttributes()){
	  		setBitstreamBits(X, Y, A, I.getTile());
	  	}
    }
}

private void setBitstreamBits(int x, int y, Attribute a, Tile t) {
	   int switchValue = 10*x + y;
	   
	   Virtex4SliceBits Current = null; 
	   
	   switch(switchValue){
	     case 0: Current = SliceX0Y0; break;
	     case 1: Current = SliceX0Y1; break;
	     case 10:Current = SliceX1Y0; break;
	     case 11:Current = SliceX1Y1; break; 
	   }
	   
	   if(Current ==null){
	      System.out.println("Warning Invalid Value Passed to setBitstreamBits");
	   }
	   
	   Current.setAttributeBits(a, t);
	   
	} 
  

   private boolean verify(){
	   //Verify Slice Attributes
	   for(Instance I : design.getInstances()){
		   if(SliceX0Y0.verifyInstance(I) == false || SliceX0Y1.verifyInstance(I) == false ||
		      SliceX1Y0.verifyInstance(I) == false || SliceX1Y1.verifyInstance(I) == false)
			  return false; 
	   }
	   //Add Verify Nets
	   
	   System.out.println("Verify Completed Successfully.");
	   return true;
   }
   /*private void verifyAttribute(Attribute a, Tile tile, int x, int y) {
       int switchValue = 10*x + y;
	   
	   Virtex4SliceBits Current = null; 
	   
	   switch(switchValue){
	     case 0: Current = SliceX0Y0; break;
	     case 1: Current = SliceX0Y1; break;
	     case 10:Current = SliceX1Y0; break;
	     case 11:Current = SliceX1Y1; break; 
	   }
	   
	   ArrayList<V4ConfigurationBit> BitList = Current.getBits(a);
	   if(BitList!=null){
		   for(V4ConfigurationBit B : BitList){
			   int curval = B.getValue();
			   int correctvalue = B.getAttributeValue();
			   if(curval !=correctvalue){
				   System.out.println("Attribute: " + a + " is not set correctly.");
				   System.exit(1);
			   }
		   }
	   }
   }*/

   public void ModifyLut(String Letter, String Contents, Instance I){
       
	   int switchValue = 10*(I.getInstanceX()%2) + (I.getInstanceY()%2);
	   
	   Virtex4SliceBits Current = null; 
	   
	   switch(switchValue){
	     case 0: Current = SliceX0Y0; break;
	     case 1: Current = SliceX0Y1; break;
	     case 10:Current = SliceX1Y0; break;
	     case 11:Current = SliceX1Y1; break; 
	   }
	   
	   Current.setLutBits(Letter, Contents, I.getTile());
   }
   public void ModifyAllLutContents(String Contents) {
		
		int contents = Integer.parseInt(Contents, 16);
		ArrayList<Tile> CLBTiles = XDLDesignUtils.getAllCLBTiles(design.getDevice());		
		int i; 
		
		for(Tile T : CLBTiles){
			SliceX0Y0.setLutBits("G", Contents, T);
			SliceX0Y0.setLutBits("F", Contents, T);
			SliceX0Y1.setLutBits("G", Contents, T);
			SliceX0Y1.setLutBits("F", Contents, T);
			SliceX1Y0.setLutBits("G", Contents, T);
			SliceX1Y0.setLutBits("F", Contents, T);
			SliceX1Y1.setLutBits("G", Contents, T);
			SliceX1Y1.setLutBits("F", Contents, T);
		}
		/*for(Tile T : CLBTiles){
			i=0; 
			for(V4ConfigurationBit B : SliceX0Y0.getBits(new Attribute(Letter, "", "#LUT:D=1"))){
				int mask = (0x1 << (i++));
				int bit = (contents & mask);
				System.out.println("bit " + bit + "contents " + Integer.toHexString(contents) + " " + i + " " + mask);
				B.SetBitValueInBitStream((bit > 0) ? 1:0,  T);
			}
			i=0; 
			for(V4ConfigurationBit B : SliceX0Y1.getBits(new Attribute(Letter, "", "#LUT:D=1"))){
				int mask = (0x1 << (i++));
				int bit = (contents & mask);
				System.out.println("bit " + bit + "contents " + Integer.toHexString(contents) + " " + i + " " + mask);
				B.SetBitValueInBitStream((bit > 0) ? 1:0,  T);	
			}
			i=0; 
			ArrayList<V4ConfigurationBit> Debug1 = SliceX1Y0.getBits(new Attribute("F", "", "<eqn>D=1")); 
			ArrayList<V4ConfigurationBit> Debug = SliceX1Y0.getBits(new Attribute(Letter, "", "#LUT:D=1")); 
			for(V4ConfigurationBit B : Debug){
				int mask = (0x1 << (i++));
				int bit = (contents & mask);
				System.out.println("bit " + bit + "contents " + Integer.toHexString(contents) + " " + i + " " + mask);
				B.SetBitValueInBitStream((bit > 0) ? 1:0,  T);
			}
			i=0; 
			for(V4ConfigurationBit B : SliceX1Y1.getBits(new Attribute(Letter, "", "#LUT:D=1"))){
				int mask = (0x1 << (i++));
				int bit = (contents & mask);
				System.out.println("bit " + bit + "contents " + Integer.toHexString(contents) + " " + i + " " + mask);
				B.SetBitValueInBitStream((bit > 0) ? 1:0,  T);	
			}
		}	*/
		System.out.println("Contents: " + contents );
	}
   
   public static void main(String [] Args){
	   System.out.println("Generating Bitstream.");
	
	   Virtex4Bitgen V4BG = new Virtex4Bitgen(Args[0]);
	   V4BG.generateBitstream();
	   V4BG.writeBitstream(Args[1]);
	   
   }
   
}

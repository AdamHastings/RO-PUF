package edu.byu.ece.gremlinTools.Virtex4Bits;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import XDLDesign.Utils.XDLDesignUtils;

import edu.byu.ece.gremlinTools.bitstreamDatabase.BitstreamDB;
import edu.byu.ece.gremlinTools.xilinxToolbox.V4XilinxToolbox;
import edu.byu.ece.rapidSmith.bitstreamTools.configuration.FPGA;
import edu.byu.ece.rapidSmith.design.Attribute;
import edu.byu.ece.rapidSmith.design.Design;
import edu.byu.ece.rapidSmith.design.Instance;
import edu.byu.ece.rapidSmith.device.Device;
import edu.byu.ece.rapidSmith.device.PrimitiveType;
import edu.byu.ece.rapidSmith.device.Tile;
import edu.byu.ece.rapidSmith.primitiveDefs.Element;
import edu.byu.ece.rapidSmith.primitiveDefs.PrimitiveDef;
import edu.byu.ece.rapidSmith.primitiveDefs.PrimitiveDefList;
import edu.byu.ece.rapidSmith.util.FileTools;

public class Virtex4SliceBits {
    private  int X;  // 0 or 1 
    private  int Y;  // 0 or 1
    private  PrimitiveType SliceType;
    private  HashMap<String, ArrayList<V4ConfigurationBit>> Database;
    private  HashMap<Bit, ArrayList<String>> BitsToAttributeMap; 
    private  FPGA fpga; 
    private  V4XilinxToolbox TB; 
    private  Device device; 
    
    public int getX(){
    	return X;
    }
    public int getY(){
    	return Y;
    }
    
    public HashMap<Bit, ArrayList<String>> getBitsToAttributeMap(){
    	return BitsToAttributeMap; 
    }
    public Virtex4SliceBits(int x, int y, PrimitiveType sliceType, FPGA fpga, Device device, V4XilinxToolbox TB){
    	
    	if((x != 1 && x != 0) || (y != 1 && y != 0)){
    		System.out.println("Virtex4SliceBits Constructor: Invalid value for x or y");
    	}
    	    		
    	if(sliceType == PrimitiveType.SLICEL &&  x != 1 || sliceType == PrimitiveType.SLICEM &&  x != 0 ){
    		System.out.println("Virtex4SliceBits Constructor: Invalid x value for " + sliceType);
    	}
    	
    	
    	X = x;
    	Y = y; 
    	SliceType = sliceType;
    	
    	this.fpga = fpga; 
    	this.TB   = TB; 
        this.device = device; 
        
    	Database = new HashMap<String, ArrayList<V4ConfigurationBit>>(); 
    	BitsToAttributeMap = new HashMap<Bit, ArrayList<String>>();
    	loadDatabase(); 
    	createBitsToAttributeMap(); 
    }
    
    public ArrayList<String> getAttributeNameFromBit(Bit B){
    	return BitsToAttributeMap.get(B);
    }
    private void createBitsToAttributeMap(){
    	for(String S : Database.keySet()){
    		System.out.println(S);
    		if(S.startsWith("SLICEWE1USED") || S.startsWith("SLICEWE0USED")){
    			System.out.println("Stop Here");
    		}
    		ArrayList<V4ConfigurationBit> Bits = Database.get(S); 
    		for(V4ConfigurationBit B: Bits){
    			ArrayList<String> Attributes;
    			if((Attributes = BitsToAttributeMap.get(B)) == null){
    				Attributes = new ArrayList<String>();
    			}
    			Attributes.add(S);
    			BitsToAttributeMap.put(B, Attributes);
    		}
    	}
    }
    
    
	private void loadDatabase() {
		
		PrimitiveDefList defs = FileTools.loadPrimitiveDefs("xc4vlx60");
		PrimitiveDef SliceDef = defs.getPrimitiveDef(SliceType);
		
		ArrayList<Attribute> AttributeList = new ArrayList<Attribute>();
		
		for(Element E :SliceDef.getElements()){
					
			//System.out.println(E);
		    
			if(E.getCfgOptions() != null){
 				//System.out.println(E.getName());
				
				//Add the #OFF Attribute
				
				AttributeList.add(new Attribute(E.getName(), "", "#OFF")); 				
 				for(String S :E.getCfgOptions()){
				  //System.out.println("\t" + S);
 				 
 				  if(E.getName().equals("F") || E.getName().equals("G")){
 					  System.out.println("Before: " + S);
 					  if(X==0 && S.startsWith("#LUT"))
 					   S = S.substring(0, 5) + "D=1";
 					  else if(X == 0 && S.startsWith("#RAM") || X == 0 && S.startsWith("#ROM"))
 					   S = S.substring(0, 5) + "D=0xFFFF";
 					  else
 					   S= "#LUT:D=1";
 				  }
 				 Attribute A = new Attribute(E.getName(), "", S);
 				 if(A.getPhysicalName().equals("SLICEWE1USED") || A.getPhysicalName().equals("SLICEWE0USED")){
 					 A.setValue("1");                                                         
 				 }
 				 System.out.println("Attribute A: " + A);
 				 // AttributeList.add(new Attribute(E.getName(), "", S));
 				 AttributeList.add(A);
 				}
			}
		}
		
		BitstreamDB DB = new BitstreamDB(); 
		DB.openConnection();
		
		for(Attribute A : AttributeList){
			System.out.println(A);
		   
			//Open Connection to Raptor
			//String QuerySLICE_X0Y0F = "SELECT * FROM " + "Virtex4LogicBits WHERE AttrName = \"F\" AND AttrValue = \"#LUT:D=1\" AND  PrimLocName = \"SLICE_X0Y0\"";
			//String X = "SELECT * FROM " + "Virtex4LogicBits WHERE  AttrValue = \"Y\" AND AttrName = \"DYMUX\" AND  PrimLocName = \"SLICE_X" + x + "Y" + y + "\"";
			ArrayList<V4ConfigurationBit> Bitlist = new ArrayList<V4ConfigurationBit>();
			if(A.getPhysicalName().equals("SLICEWE1USED") || A.getPhysicalName().equals("SLICEWE0USED")){
				System.out.println("Found Missing Attribute!");
			}
			String Query = "SELECT * FROM " + "Virtex4LogicBits WHERE  " +
			               "AttrName = \"" + A.getPhysicalName() + "\" "  +
					       "AND AttrValue = \"" + A.getValue() + "\" " +
					       "AND PrimLocName = \"SLICE_X" + X + "Y" + Y + "\"";
			
			ResultSet rs = DB.makeQuery(Query);
			
			try {
				while(rs.next()){
					System.out.println(rs.getString(1) + " " + rs.getString(2) + " " + rs.getString(3) + " " + rs.getString(4) + " " + rs.getString(5) + " " + rs.getString(6) + " " + rs.getString(7) + " " + rs.getString(8) + " " + rs.getString(9));
				    
					int minor =      Integer.parseInt(rs.getString(5));
				    int byteOffset = Integer.parseInt(rs.getString(6));
				    int mask =       Integer.parseInt(rs.getString(7));
				    int properValue =Integer.parseInt(rs.getString(8)); 
				    if((minor == 21 && byteOffset == 1 && mask == 16) ||(minor == 20 && byteOffset == 7 && mask == 1) || 
				       (minor == 20 && byteOffset == 7 && mask == 128)||(minor == 20 && byteOffset == 0 && mask == 64)){
				    	System.out.println("Found Missing Bit!");
				    }
				    System.out.println("ProperValue : "  + properValue);
				    Bitlist.add(new V4ConfigurationBit(fpga, new Bit(minor, byteOffset, mask, properValue), device.getTile("CLB_X1Y0"), TB));
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(Database.put(A.toString(), Bitlist)!=null){
				System.out.println("Attribute Replaced: :( " + A.toString());
			}
			
			
			 //if(A.getPhysicalName().equals("F") || A.getPhysicalName().equals("G")){
			 // System.in.read();
			//}
		}
		DB.closeConnection();
	}
	public HashMap<String, ArrayList<V4ConfigurationBit>> getDataBase(){
		return Database; 
	}
	public boolean verifyInstance(Instance I){
		int IX = I.getInstanceX() %2;
		int IY = I.getInstanceY() %2; 
		if( IX == X && IY == Y){
			for(Attribute A : I.getAttributes()){
				ArrayList<V4ConfigurationBit> Bits = Database.get(A);
		        if(Bits != null){
		        	for(V4ConfigurationBit B: Bits){
		        		if(!B.verifyBit(I.getTile())){
		        			System.out.println("Attribute " + A + " from instance " + I);
		        			return false; 
		        		}
		        	}
		        } 
			}
		}
		return true; 
	}
	
	public void setAttributeBits(String PhysicalName, String Value, Tile T){
		setAttributeBits(new Attribute(PhysicalName, "", Value), T);
	}
	
	public ArrayList<V4ConfigurationBit> getBits(Attribute A) {
		return Database.get(A.toString());
	}
	
	public void setAttributeBits(Attribute A, Tile T){
		System.out.println("Setting Attribute Bits");
		ArrayList<V4ConfigurationBit> Bits = Database.get(A.toString());
		
		//Handle the LUT Equations
		if(A.getPhysicalName().equals("F") || A.getPhysicalName().equals("G")){
			//If using with the Slice GUI all Lut Contents must be set with TEXT 
			if(A.getValue().equals("#OFF")){
				this.setLutBits(A.getPhysicalName(), "0000", T); 
			} else if(A.getValue().equals("#LUT:D=1") || A.getValue().equals("ON")){
				this.setLutBits(A.getPhysicalName(), "FFFF", T);
			} else {
				int length = A.getValue().length(); 
				String Contents = A.getValue().substring(length-4, length); //Last Four Characters
				this.setLutBits(A.getPhysicalName(), Contents, T);
			}
		} else if(Bits != null)	{
			if(Bits.size() == 0){
				//System.out.println("Virtex4SliceBits: This Attribute/Value Pair has contains no configuration bits");
			}
			for(V4ConfigurationBit B : Bits){
				B.SetBitValueInBitStream(B.getAttributeValue(), T);
			}
		} else {
			//System.out.println("Virtex4SliceBits: Invalid Attribute Selected");
			//System.exit(1);
		}
	}
	
	public void setLutBits(String Letter, String Contents, Tile T){
		
		Attribute A = new Attribute(Letter, "", "#LUT:D=1");
		int contents = Integer.parseInt(Contents, 16);
		
		ArrayList<V4ConfigurationBit> Bits = getBits(A);
		int i = 0; 
		for(V4ConfigurationBit B : Bits){
			int mask = (0x1 << (i++));
			int bit = (contents & mask);
			//System.out.println("bit " + bit + "contents " + Integer.toHexString(contents) + " " + i + " " + mask);
			B.SetBitValueInBitStream((bit > 0) ? 1:0, T);	
		}
	}
	
	public static void main(String Args[]){
		
		Design design = new Design();
	  	design.setNCDVersion("v3.2");
		design.setPartName("xc4vlx60ff668-12");
		
		Device device = design.getDevice();
		FPGA fpga = XDLDesignUtils.getVirtexFPGA("XC4VLX60");
		V4XilinxToolbox TB = XDLDesignUtils.getV4Toolbox(fpga);
		Tile T = design.getDevice().getTile("CLB_X1Y1");	
		Virtex4SliceBits V =  new Virtex4SliceBits(0, 0, PrimitiveType.SLICEM, fpga, device, TB);
		Virtex4SliceBits V1 = new Virtex4SliceBits(0, 1, PrimitiveType.SLICEM, fpga, device, TB);
		Virtex4SliceBits V2 = new Virtex4SliceBits(1, 0, PrimitiveType.SLICEL, fpga, device, TB);
		Virtex4SliceBits V3 = new Virtex4SliceBits(1, 1, PrimitiveType.SLICEL, fpga, device, TB);
		Attribute A =  new Attribute("CY0G", "", "0");
		System.out.print(V2.getBits(A));
		
		Bit B = new Bit(21, 9, 1);
		V4ConfigurationBit V4B = new V4ConfigurationBit(fpga, B, T, TB); 
		
		System.out.println("\n Returned Value: " + V.getAttributeNameFromBit(V4B));
		System.out.println("\n Returned Value: " + V1.getAttributeNameFromBit(V4B));
		
		int duplicateBits=0; 
		for(Bit D: V.getBitsToAttributeMap().keySet()){
			ArrayList<String> Names = V.getAttributeNameFromBit(D);
			
			if(Names.size()>1){
				for(String S :Names){
					System.out.println("" + D+ " " + S);
				}
				duplicateBits++;
			}
 		}
		
		System.out.println("Bits In more than One attribute: " + duplicateBits);
		//Virtex4SliceBits V3 = new Virtex4SliceBits(1, 1, PrimitiveType.SLICEL, fpga, device, TB);
		
		
		for(String S: V.getDataBase().keySet()){
			System.out.println(S);
		}
	}
}

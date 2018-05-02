package edu.byu.ece.gremlinTools.bitstreamDatabase;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

//import edu.byu.ece.gremlinTools.xilinxToolbox.XilinxToolbox;
import edu.byu.ece.gremlinTools.xilinxToolbox.V4XilinxToolbox;
import edu.byu.ece.gremlinTools.xilinxToolbox.V5XilinxToolbox;
import edu.byu.ece.gremlinTools.xilinxToolbox.XilinxToolboxLookup;
import edu.byu.ece.rapidSmith.bitstreamTools.configuration.FPGA;
import edu.byu.ece.rapidSmith.bitstreamTools.configuration.FrameAddressRegister;
import edu.byu.ece.rapidSmith.design.Attribute;
import edu.byu.ece.rapidSmith.design.Design;
import edu.byu.ece.rapidSmith.design.Instance;
import edu.byu.ece.rapidSmith.design.Net;
import edu.byu.ece.rapidSmith.design.PIP;
import edu.byu.ece.rapidSmith.device.Tile;
import edu.byu.ece.rapidSmith.device.WireEnumerator;


// TODO This class still needs to be converted over
public class VerifyDB {

	private FPGA fpga;
	private Design design;
	private V5XilinxToolbox toolbox;  //changed to v5 and also on line 36
	private BitstreamDB db;
	private String deviceFamilyName;
	public VerifyDB(FPGA fpga, Design design){
		this.fpga = fpga;
		this.design = design;
		this.toolbox = (V5XilinxToolbox) XilinxToolboxLookup.toolboxLookup(fpga.getDeviceSpecification());
		this.db = new BitstreamDB();
		deviceFamilyName = fpga.getDeviceSpecification().getDeviceFamily();
	}

	public void VerifyBitstream() throws SQLException{
		ResultSet results, results2 = null;
// Open connection to MySQL database with bitstream information get each instance in design  
int errors=0, tests=0, Itests=0, Ierrors=0, Ttests=0, Terrors=0, ndbBits=0;
       	String Tile,PrimLocName,Primitive_Def;

// specific frame test       	
    /*   	
       	System.out.println("Starting connection");
       	int far1=1051675;
		System.out.println(FrameAddressRegister.toString(fpga.getDeviceSpecification(), far1));
			toolbox.printFrame(fpga.getFrame(far1).getData());
       	*/
       	
       	db.openConnection();
    	for (Instance Instance : design.getInstances())
    	{	
    		Ierrors=0;Itests=0;
    		if(Instance.getName().contains("SLICE"))
    		{System.exit(0);}
       		for(Attribute Abs:Instance.getAttributes())
       		{  errors=0;tests=0; 
/*  
 * Virtex 5 Database Ordering and 
 * Table col name = Prim_def Tile  PrimLocName  AttrName  AttrValue NumBits MinorAddr Offset Mask BitValue SigBit Dependency Status Origin
 *  '1'=Primitive_Def '2'=Tile;'3'=PrimLoc;'4'=Attribute;'5'=Attribute value;'6'=#of config bits;'7'=MinorAddr;
 *  '8'=Offset '9'=Mask '10'=Value '11'=Sigbit '12'=Dependancies '13'=Status '14'=Origin 
 * entering query you need "(Table col name) = \' value \'"  (Additional limits on the query add AND)
 */ 
 //      			Tile = Instance.getTile().toString().split("_")[0]+""+"_BOT";
 // 			PrimLocName = (Instance.getPrimitiveSiteName().toString().split("_")[0]+"_ADV_X0Y0");
       			Primitive_Def =Instance.getType().toString();
//    			results = db.makeLogicQuery("Primitive_Def = \'"+Primitive_Def+"\' AND Tile = \'"+Tile+"\' AND PrimLocName = \'"+PrimLocName+"\'AND AttrName = \'"+Abs.getPhysicalName()+"\' AND AttrValue = \'"+Abs.getValue()+"\'");   			
       			results = db.makeLogicQuery("Primitive_Def = \'"+Primitive_Def+"\'AND AttrName = \'"+Abs.getPhysicalName()+"\' AND AttrValue = \'"+Abs.getValue()+"\'");
       		//	System.out.println(results.getRow());
       			int i=1;    			
    			results.next();
    		//	System.out.println(results.getRow());
/*
 * No bit Value in Database 
 * verify== false then value in bitstream = zero  
 */
    			if(results.getRow()==0){System.out.println("No Bits in Database");i=0; tests+=1;ndbBits+=1;}
    			
    			for(;i!=0;){	
    				tests+=1;
//    			System.out.println(results.getString(1)+" "+results.getString(2)+" "+results.getString(3)+" "+results.getString(4)+" "+results.getString(5)+" "+results.getString(6)+" "+results.getString(7)+" "+results.getString(8)+" "+results.getString(9)+" "+results.getString(10)+" "+results.getString(11)+" "+results.getString(12)+" "+results.getString(13)+" "+results.getString(14));
    			System.out.println("Attr="+results.getString(4)+" AttrVal="+results.getString(5)+" #ofConfigBits="
    					+results.getString(6)+" Minor="+results.getString(7)+" Offset="+results.getString(8)
    					+" Mask="+results.getString(9)+" Value="+results.getString(10));

    			    int minor = Integer.parseInt(results.getString(7).trim());
    				int offset = Integer.parseInt(results.getString(8).trim());
    				int mask = Integer.parseInt(results.getString(9).trim());
    				boolean verify=false;
    				verify=verifyAttrBit(minor, offset, mask, Instance.getTile()); 			

    				
    				if((verify==false && Integer.parseInt(results.getString(10))==0) || (verify==true && Integer.parseInt(results.getString(10))==1))
    					{int far = toolbox.getFARFromTile(fpga.getDeviceSpecification(), Instance.getTile(), minor);
    				//	far=far+128;
    					System.out.println("the far = "+far);
    					System.out.println(FrameAddressRegister.toString(fpga.getDeviceSpecification(), far));
       					toolbox.printFrame(fpga.getFrame(far).getData());
       					System.out.println("Correct in DataBase, Database bit Value is "+Integer.parseInt(results.
       							getString(10).trim())+" and bitstream gives a Value of"+((verify)?1:0));//*//*verified*/
       							}
    				else{
    				errors+=1;
       					int far = toolbox.getFARFromTile(fpga.getDeviceSpecification(), Instance.getTile(), minor);
       				//	far=far+128;
       					System.out.println(FrameAddressRegister.toString(fpga.getDeviceSpecification(), far));
       					toolbox.printFrame(fpga.getFrame(far).getData());		       			
       					System.out.println("Error in DataBase, Database bit Value is "+Integer.parseInt(results.
       							getString(10).trim())+" and bitstream gives a Value of"+((verify)?1:0));
    					}				

    				results.next();
    				if(results.getRow()==0){i=0;}    	

    			}			
    			System.out.println("errors="+errors +"out of"+tests+" tests");
    			System.out.println();
    			Ierrors +=errors; Itests+=tests;
       		}
       		Terrors +=Ierrors; Ttests+=Itests;
       		System.out.println("Total errors in Instance = "+Terrors +" out of "+ Ttests+" " +"Tests");
       		System.out.println();	System.out.println();
       	}
    	db.closeConnection();
       System.out.println("Total errors in Design = "+Terrors +" out of "+ Ttests+" Tests and number of queries with no bits in DataBase ="+ndbBits);
	//}
/*
 * End of code that checks and Verifies the attributes of each Instances in the bitstream with our database 
 */
       
//  TODO Code below was design for V4 have not check it compatibility with V5 yet
			
//		ResultSet 
		results= null;
	//	, results2 = null;
       	
		// Open connection to MySQL database with bitstream information
		db.openConnection();
		WireEnumerator We = design.getWireEnumerator(); 
		// Verify PIPs in XDL design with bits in bitstream
		for(Net net : design.getNets()){
			for(PIP pip : net.getPIPs()){
				results = db.makeRoutingPIPMatchQuery(deviceFamilyName, "clb", pip, design.getWireEnumerator());
				if(We.getWireName(pip.getStartWire()).equals("LV0") || We.getWireName(pip.getEndWire()).equals("LV18")){
					results2 = db.makeRoutingPIPMatchQuery(deviceFamilyName, 
							"clb", new PIP(null, pip.getEndWire(), pip.getStartWire()), design.getWireEnumerator()); 
							
				}
				try {
					boolean reverseLongLinePIPisOK = false;
					if(results2 != null && results2.next()){
						reverseLongLinePIPisOK = true;
						if(!verifyPIPBit(results2.getInt(6), results2.getInt(7), results2.getInt(8), pip.getTile())){
							reverseLongLinePIPisOK = false;
						}
					}
					
					if(results.next() && !reverseLongLinePIPisOK){
						do{ // There was a matching PIP
							// Let's verify it with the bitstream
							if(!verifyPIPBit(results.getInt(6), results.getInt(7), results.getInt(8), pip.getTile())){
								System.out.println("PIP DATABASE WRONG: " + 
									results.getString(1) + " " + results.getString(2) + " " + 
									results.getString(3) + " " + results.getString(4) + " " + 
									results.getString(5) + " " + results.getString(6) + " " + 
									results.getString(7) + " " + results.getString(8));
								System.out.println(pip);
								try {
									System.in.read();
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}while(results.next());
					}
					else{
						// There was no match
						//System.out.print("PIP NOT FOUND: " + pip.toString());
					}
					results.close();
				} 					
				catch (SQLException e) {
					System.out.println("SQLException while checking PIPs");
					db.printSQLExceptionMessages(e);
					System.exit(1);
				}
			}
		}
	}
		
	public boolean verifyPIPBit(int minor, int offset, int mask, Tile tile){
		int far = toolbox.getFARFromTile(fpga.getDeviceSpecification(), tile, minor);
		int frameBitNumber = toolbox.getFrameBitNumber(
				FrameAddressRegister.getTopBottomFromAddress(fpga.getDeviceSpecification(), far),
				toolbox.getTileNumber(tile), offset, mask);
		
		if(fpga.getFrame(far).getData().getBit(frameBitNumber) != 1){
			System.out.println("FAR=" + FrameAddressRegister.toString(fpga.getDeviceSpecification(), far));
			toolbox.printFrame(fpga.getFrame(far).getData());
		}
		
		return fpga.getFrame(far).getData().getBit(frameBitNumber) == 1;
	}
	
	/*   
	 *  These method returns the value of the Bit in a given frame without print the frame incase in is correct.
	 */
	public boolean verifyAttrBit(int minor, int offset, int mask, Tile tile){
		int far = toolbox.getFARFromTile(fpga.getDeviceSpecification(), tile, minor);
		System.out.println("TESTING2 offset="+offset+" mask="+mask);
		int frameBitNumber = toolbox.getFrameBitNumber(
				FrameAddressRegister.getTopBottomFromAddress(fpga.getDeviceSpecification(), far),
				toolbox.getTileNumber(tile), offset, mask,fpga.getDeviceSpecification(),tile); 
		System.out.println("TESTING Far="+far+" and framebitNum="+frameBitNumber);
		return fpga.getFrame(far).getData().getBit(frameBitNumber) == 1;
	}
	
	/*
	public void printAll(Bitstream b, int frame){
		FPGA v4 = b.GetFPGA();
		int[] x_tiles = v4.getBRAMConfigColumns();

		for(XDL_Instance inst: this.instList){
			if(inst.getTile().getType().equals("CLB") && inst.getInstanceX()%2==1 && inst.getInstanceY()%2==0){
				Block blk =v4.getConfigurationBlock(inst.getTile(), x_tiles); 
				Frame f = blk.Get(frame);
				for(int i=0; i < 10; i++){
					int tmp = f.GetByte(i, inst.getTile().getYCoord(), blk.GetTopBottom()==0);
					System.out.print(this.binaryLUT[(tmp >> 4) & 0xf] + this.binaryLUT[tmp & 0xf]);
				}
				for(XDL_Attribute attr: inst.getAttributeList()){
					if(attr.getPhysicalName().equals("DYMUX")){
						System.out.print(attr.getValue() + " ");
						System.out.println(inst.getTile().toString()+ " " + blk.GetColumnIndex() + " " + blk.GetRowIndex() + " " + blk.GetTopBottom());
					}
				}
			}
		}
	}*/
	/*
		public void dumpBitsAndXDL(Bitstream b){
			Set<XDL_Instance> set = new HashSet<XDL_Instance>();
			FPGA v4 = b.GetFPGA();

			int[] x_tiles = v4.getBRAMConfigColumns(); // Configuration columns with BRAM
			// Get all the unique CLBs
			for (XDL_Instance inst: this.instList){
				if(inst.getTile().getType().equals("CLB")){
					set.add(inst);
				}
			}
			
			// For each CLB, spill all the bits and XDL Attributes
			for(XDL_Instance inst: set){
				// Find all SLICES in the instance list in this tile
				XDL_InstanceList list = new XDL_InstanceList();
				for (XDL_Instance match: this.instList){
					if(inst.getTile().equals(match.getTile())){
						list.add(match);
					}
				}
				for(int i = 18; i < 22; i++){
					Block blk = v4.getConfigurationBlock(inst.getTile(), x_tiles);
					Frame f = blk.Get(i);
					for(int j=0; j < 10; j++){
						int tmp = f.GetByte(j, inst.getTile().getYCoord(), blk.GetTopBottom()==0);
						System.out.print(this.binaryLUT[(tmp >> 4) & 0xf] + this.binaryLUT[tmp & 0xf]);
					}
				}
				
//				for(XDL_Instance slice : list){
//					if(slice.getInstanceX()%2==0 && slice.getInstanceY()%2==0){
//						System.out.print("SLICE_X0Y0,");
//						for(XDL_Attribute attr: slice.getAttributeList()){
//							System.out.print(attr.getPhysicalName()+"::"+attr.getValue()+",");
//						}
//					}
//				}
//				for(XDL_Instance slice : list){
//					if(slice.getInstanceX()%2==0 && slice.getInstanceY()%2==1){
//						System.out.print("SLICE_X0Y1,");
//						for(XDL_Attribute attr: slice.getAttributeList()){
//							System.out.print(attr.getPhysicalName()+"::"+attr.getValue()+",");
//						}
//					}
//				}
//				for(XDL_Instance slice : list){
//					if(slice.getInstanceX()%2==1 && slice.getInstanceY()%2==0){
//						System.out.print("SLICE_X1Y0,");
//						for(XDL_Attribute attr: slice.getAttributeList()){
//							System.out.print(attr.getPhysicalName()+"::"+attr.getValue()+",");
//						}
//					}
//				}
				
		
				for(XDL_Instance slice : list){
					if(slice.getInstanceX()%2==1 && slice.getInstanceY()%2==1){
						System.out.print("SLICE_X1Y1,");
						for(XDL_Attribute attr: slice.getAttributeList()){
							System.out.print(attr.getPhysicalName()+"::"+attr.getValue()+",");
						}
					}
				}
				System.out.println("");
			}
		}*/
	
	

	
	/**
	 * This function is designed to verify our data in the MySQL database that maps XDL constructs to bits.
	 * Currently this function only verifies PIPs in INT tiles and SLICEs.  It prints to standard out any 
	 * discrepancies found in the database compared with this XDL_Design and Bitstream b.
	 * @param b The bitstream to be used to verify against this XDL_Design
	 */
	/*
	public void verifyBitstream(Bitstream b){
		FPGA v4 = b.GetFPGA();
		int[] x_tiles = v4.getBRAMConfigColumns(); // Configuration columns with BRAM
		int[] so_columns = v4.getSOConfigColumns(); // SO Configuration Columns
		ResultSet result = null;
		Block blk = null;
		Boolean validBit = false;
		String query = "";
		
		// Establish connection to bitstream database
		if(!db.openConnection()){
			System.out.println("Could not open bitstream database.");
			System.exit(1);
		}
		
		// Verify All the PIPs
		for(XDL_Net net : this.netList){
			for(XDL_PIP pip : net.getPIPs()){
				if(pip.getTile().getType().equals(XDL_TileType.INT) && !v4.isINT_SO(pip.getTile().getXCoord(), so_columns, 7) ){					

					query = "SELECT * FROM virtex4bits WHERE Tile = \"clb\" AND Source = \"" + 
					pip.getWire0() + "\" AND ConnType = \"" + 
					pip.getDirection() + "\" AND Destination = \"" + pip.getWire1() + "\"";
					result = db.makeQuery(query);
		
					try {
						if(result.next()){
							do{ // There was a matching PIP
								// Let's verify it with the bitstream
								blk = v4.getConfigurationBlock(pip.getTile(), x_tiles);
								//System.out.println(pip.getTile());
								validBit = v4.verifyBit(blk, pip.getTile().getYCoord(), result.getInt(6), result.getInt(7), result.getInt(8), 1);
								if(validBit == false){
									System.out.println("PIP DATABASE WRONG: " + result.getString(1) + " " + result.getString(2) + " " + 
										result.getString(3) + " " + result.getString(4) + " " + result.getString(5) + " " + 
										result.getString(6) + " " + result.getString(7) + " " + result.getString(8));
									System.out.println(pip);
									System.exit(1);
								}
							}while(result.next());
						}
						else{
							// There was no match
							System.out.print("PIP NOT FOUND: " + pip.toString());
						}
						result.close();
					} 					
					catch (SQLException e) {
						System.out.println("SQLException while checking PIPs");
						db.printSQLExceptionMessages(e);
						System.exit(1);
					}
				}
			}
		}
		
		// Verify the SLICE instances
		for(XDL_Instance inst : this.instList){
			if(inst.getTile().getType().equals(XDL_TileType.CLB)){
				String slice_type = "SLICE_X" + inst.getInstanceX()%2 + "Y" + inst.getInstanceY()%2;
				for(XDL_Attribute attr : inst.getAttributeList()){
					
					//////////////   ADDED CODE   /////////////////
					int[] lutBits = null;
					String attribute_value = attr.getValue();
					if(attribute_value.startsWith("#LUT:")) {
						attribute_value = "#LUT:D=1";
						lutBits = attr.getLutBits();
					}
					if(attribute_value.startsWith("#RAM:")) {
						attribute_value = "#RAM:D=0xFFFF";
						lutBits = attr.getMemBits();
					}
					if(attribute_value.startsWith("#ROM:")) {
						attribute_value = "#ROM:D=0xFFFF";
						lutBits = attr.getMemBits();
					}
					///////////////// FOR EVALUATION ////////////////
					//if(!slice_type.startsWith("SLICE_X0Y0"))
					//	slice_type = "";
					/////////////////////////////////////////////////
					
					query = "SELECT * FROM virtex4logicbits WHERE PrimName = \""+ slice_type +
							"\" AND AttrName = \""+ attr.getPhysicalName() +"\" AND AttrValue = \""+ attribute_value + "\"";

					result = db.makeQuery(query);
					try {
						if(result.next()){
							do {
								boolean invertBit = false;
								int tempBit = result.getInt(8);
								// Check for dependencies
								if(result.getString(9) != null && !result.getString(9).equals("")) {
									//Check for LUT equations and RAM or ROM configurations
									if((attr.getPhysicalName().equals("F") || attr.getPhysicalName().equals("G")) && 
										(attr.getValue().startsWith("#LUT:") || (attr.getValue().startsWith("#RAM:")) ||
										(attr.getValue().startsWith("#ROM:")))) {
											tempBit = lutBits[Integer.parseInt(result.getString(9))];
									}
									else {
										String[] dependencies = result.getString(9).split("!");
										for(String s : dependencies) {
											String[] attr_value_pair = s.split("-");
											// Cycle through the attribute list to see if the dependencies are correct 
											for(XDL_Attribute attr_again : inst.getAttributeList()){
												if(attr_again.getPhysicalName().equals(attr_value_pair[0])){
													if(!attr_again.getValue().equals(attr_value_pair[1]))
														invertBit = true;
												}	
											}										
										}
										// If Dependencies are not correct, invert the bit
										if(invertBit) {
											if(tempBit == 0)
												tempBit = 1;
											else
												tempBit = 0;
										}
									}
								}
								/////////////// SPECIAL CASE RAM BITS ////////////////////
								//if((attr.getPhysicalName().equals("F") || attr.getPhysicalName().equals("G")) && 
								//		(attr.getValue().startsWith("#RAM:"))) {
								//	continue;
								//}
								//////////////////////////////////////////////////////////
								
								// Perform Bit Verification
								blk = v4.getConfigurationBlock(inst.getTile(), x_tiles);
								validBit = v4.verifyBit(blk, inst.getTile().getYCoord(), result.getInt(5), result.getInt(6), result.getInt(7), tempBit);
								
								if(validBit == false){
									//if(!result.getString(3).equals("#OFF")){
									;	System.out.println("LOGIC DATABASE WRONG: " + inst.getTile() +" "+ result.getString(1) + " " + result.getString(2) + " " + 
											result.getString(3) + " " + result.getString(4) + " " + result.getString(5) + " " + 
											result.getString(6) + " " + result.getString(7) + " " + result.getString(8) + " " + result.getString(9));
										//System.exit(1);
									//}
								}
								else{
									;//System.out.println("MATCH: " + inst.getTile()+ " " + inst.getInstanceLocation() +" "+ attr);
								}
							}while(result.next());
						}
						else{
							// There was no match
							;//System.out.println("NO MATCH: " + inst.getTile()+ " " + inst.getInstanceLocation() +" "+ attr);
						}
						result.close();
					} catch (NumberFormatException e) {
						System.out.println("Number Format Exception while checking SLICEs");
						e.printStackTrace();
						System.exit(1);
					} catch (SQLException e) {
						System.out.println("SQLException while checking SLICEs");
						db.printSQLExceptionMessages(e);
						System.exit(1);
					}
				}
			}
		}
		// Close the connection to the database
		db.closeConnection();
	}*/
	
	/**
	 * This function will find a specified bit in Bitstream b.  The bit of interest is defined by type, frame, 
	 * byteOffset, mask and its value by bitValue.  Any matches are printed to stdout.
	 * @param b The bitstream that should be searched
	 * @param type The configuration block type where the bit of interest resides
	 * @param frame The minor address of the bit of interest
	 * @param byteOffset The byteOffset of the bit of interest
	 * @param mask the bit mask defining the bit of interest within a byte
	 * @param bitValue The value of the bit of interest 
	 */
	/*
		public void findBitMatch(Bitstream b, BlockType type, int frame, int byteOffset, int mask, int bitValue){
			FPGA v4 = b.GetFPGA();
			//int[] x_tiles = v4.getBRAMConfigColumns(); // Configuration columns with BRAM
			
			for (ArrayList<Block> blk_columns :v4.getTopLogic()){
				for(Block blk : blk_columns){
					if(blk.GetBlockType().equals(type)){
						for(int i=0; i < 16; i++){
							if(v4.verifyBit(blk, i, frame, byteOffset, mask, bitValue)){
								System.out.printf("Match Found: FAR=0x%08X, TOP, %s, ROW=%d, COL=%d, TILE=%d"+this.newLine+"",blk.getBlockFAR() + i,type.toString(), blk.GetRowIndex(),blk.GetColumnIndex(),i);
							}
						}
					}
				}
			}

			for (ArrayList<Block> blk_columns :v4.getBottomLogic()){
				for(Block blk : blk_columns){
					if(blk.GetBlockType().equals(type)){
						for(int i=0; i < 16; i++){
							if(v4.verifyBit(blk, i, frame, byteOffset, mask, bitValue)){
								System.out.printf("Match Found: FAR=0x%08X, BOTTOM, %s, ROW=%d, COL=%d, TILE=%d"+this.newLine+"",blk.getBlockFAR() + i,type.toString(), blk.GetRowIndex(),blk.GetColumnIndex(),i);
							}
						}
					}
				}
			}		
			
//			for(XDL_Instance inst: this.instList){
//				if(inst.getTile().getType().equals("CLB") && inst.getInstanceX()%2==1 && inst.getInstanceY()%2==1){
//					Block blk = v4.getConfigurationBlock(inst.getTile(), x_tiles);
//					for(int i=0; i < 16; i++){
//						if(b.GetFPGA().verifyBit(blk, i, frame, byteOffset, mask, bitValue)){
//							//System.out.println(""+this.newLine+"**Bit Match Found, frame="+frame+", byteOffset="+byteOffset+", mask="+mask+"**");
//							//System.out.println("CLB Y="+i+"%16"+this.newLine+"" + blk.Get(frame).toString(blk, i, blk.getBlockFAR()+frame));
//							//for(XDL_Attribute attr : inst.getAttributeList()){
//							//	System.out.print(attr.getPhysicalName() + "::" + attr.getValue() + " ");
//							//}
//							//System.out.println();
//						}
//					}				
//				}
//			}
		}*/
}
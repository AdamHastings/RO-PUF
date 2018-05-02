package edu.byu.ece.gremlinTools.xilinxToolbox;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import edu.byu.ece.gremlinTools.bitstreamDatabase.BitstreamDB;
import edu.byu.ece.rapidSmith.bitstreamTools.configuration.FPGA;
import edu.byu.ece.rapidSmith.bitstreamTools.configuration.FrameAddressRegister;
import edu.byu.ece.rapidSmith.bitstreamTools.configuration.FrameData;
import edu.byu.ece.rapidSmith.bitstreamTools.configurationSpecification.BlockSubType;
import edu.byu.ece.rapidSmith.bitstreamTools.configurationSpecification.V4ConfigurationSpecification;
import edu.byu.ece.rapidSmith.bitstreamTools.configurationSpecification.XilinxConfigurationSpecification;
import edu.byu.ece.rapidSmith.design.Design;
import edu.byu.ece.rapidSmith.design.PIP;
import edu.byu.ece.rapidSmith.device.Device;
import edu.byu.ece.rapidSmith.device.TileType;
import edu.byu.ece.rapidSmith.device.Tile;
import edu.byu.ece.rapidSmith.device.WireEnumerator;
import edu.byu.ece.gremlinTools.Virtex4Bits.Bit;
public class V4XilinxToolbox extends AbstractXilinxToolbox{

    private static V4XilinxToolbox _singleton = null;
    private ArrayList<Integer> bramIndicies = null;
    private XilinxConfigurationSpecification bramSpec = null;
    private int clkColumnIndex = -1;
	private final int[] bramBlockTypes = {1,2,4,6};
	private int [] configColumnIndexArray = null; 
	private int [] BlockType0XIndexArray = null;
	private int [] BlockType1XIndexArray = null;
	// Bitstream PIP extraction
	private HashSet<PIP> pips;
	private BitstreamDB db;    
    private ArrayList<PIP> possiblePIPSubsets;
   
    /**
     * This variable was generated automatically by V4GeneratePIPSubsetTable.java
     */
 
	private V4XilinxToolbox() {
		pips = new HashSet<PIP>();
		db = new BitstreamDB();
		//W = D.getWireEnumerator(); 
    }
    
    public static V4XilinxToolbox getSharedInstance() {
        if (_singleton == null) {
            _singleton = new V4XilinxToolbox();
        }
        return _singleton;
    }
	
	private int[] byteLUT = {0,1,2,3,4,5,6,7,2,3,
							 8,9,6,7,0,1,8,9,4,5,
							 0,1,2,3,4,5,6,7,2,3,
							 8,9,6,7,0,1,8,9,4,5,
							 0,1,2,3,4,5,6,7,2,3,
							 8,9,6,7,0,1,8,9,4,5,
							 0,1,2,3,4,5,6,7,2,3,
							 8,9,6,7,0,1,8,9,4,5,
							 -1,-1,-1,-1,
							 0,1,2,3,4,5,6,7,2,3,
							 8,9,6,7,0,1,8,9,4,5,
							 0,1,2,3,4,5,6,7,2,3,
							 8,9,6,7,0,1,8,9,4,5,
							 0,1,2,3,4,5,6,7,2,3,
							 8,9,6,7,0,1,8,9,4,5,
							 0,1,2,3,4,5,6,7,2,3,
							 8,9,6,7,0,1,8,9,4,5};
	
	private int[] tileLUT = {15,15,15,15,15,15,15,15,14,14,
			                 15,15,14,14,14,14,14,14,14,14,
			                 13,13,13,13,13,13,13,13,12,12,
			                 13,13,12,12,12,12,12,12,12,12,
			                 11,11,11,11,11,11,11,11,10,10,
			                 11,11,10,10,10,10,10,10,10,10,
			                 9,9,9,9,9,9,9,9,8,8,
			                 9,9,8,8,8,8,8,8,8,8,
			                 -1,-1,-1,-1,
			                 7,7,7,7,7,7,7,7,6,6,
			                 7,7,6,6,6,6,6,6,6,6,
			                 5,5,5,5,5,5,5,5,4,4,
			                 5,5,4,4,4,4,4,4,4,4,
			                 3,3,3,3,3,3,3,3,2,2,
			                 3,3,2,2,2,2,2,2,2,2,
			                 1,1,1,1,1,1,1,1,0,0,
			                 1,1,0,0,0,0,0,0,0,0};
	
	private int[][] tileEvenOddLUT  = {{14,15,8,9,18,19,12,13,16,17}, // Even tiles byte offset
										{0,1,2,3,4,5,6,7,10,11}}; // odd tiles byte offset
	
	private int[] tileFrameByteLUT = {144,144,124,124,104,104,84,84,60,60,40,40,20,20,0,0};
	
	@Override
	public int getMask(int topBottom, int frameBitNumber, boolean isClkColumn) {
		/*if((frameBitNumber > 639 && frameBitNumber < 671) && !isClkColumn){
			return 1 << 7 - (frameBitNumber % 8);
		}*/
		if(topBottom==1 || (frameBitNumber > 639 && frameBitNumber < 671)){
			return 1 << 7 - (frameBitNumber % 8);
		}
		else{
			return 1 << frameBitNumber % 8;
		}
	}
	
	@Override
	public boolean isClkColumn(XilinxConfigurationSpecification spec,
			FrameAddressRegister far) {
		return spec.getBlockSubtype(spec, far).equals(V4ConfigurationSpecification.CLK);
	}
	
	@Override
	public int getFrameBitNumber(int topBottom, int tileNumber, int byteOffset, int mask) {
		if(byteOffset > 10){
			// We are in the CLK_HROW column
			if(topBottom==1 || (byteOffset > 79 && byteOffset < 84)){
				return (byteOffset*8) + (7 - Integer.numberOfTrailingZeros(mask));				
			}
			else{
				return 1311 - ((byteOffset*8) + (7 - Integer.numberOfTrailingZeros(mask)));
			}

		}
		if(tileNumber == -1){
			// If we are in the center word, its a special case
			if(topBottom==1)
				return 640 + (byteOffset*8) + (7 - Integer.numberOfTrailingZeros(mask));
			else{
				return 640 + (byteOffset*8) + (Integer.numberOfTrailingZeros(mask));
			}
		}
		int frameBitNumber = (tileFrameByteLUT[tileNumber] +
							  tileEvenOddLUT[tileNumber % 2][byteOffset])*8 + 
							  7 - Integer.numberOfTrailingZeros(mask);
		if(topBottom==1){			
			return frameBitNumber;
		}
		else{
			return 1311 - frameBitNumber;			
		}
	}

	@Override
	public int getByteOffset(int topBottom, int frameBitNumber, boolean isClkColumn) {
		if(frameBitNumber > 639 && frameBitNumber < 672){
			if(isClkColumn)
				return frameBitNumber/8;
			else
				return 2;
		}
		if(isClkColumn){
			if(topBottom == 1){
				return frameBitNumber/8;
			}
			else {
				return 163 - frameBitNumber/8;
			}
			
		}
		
		if(topBottom==1){
			return byteLUT[frameBitNumber / 8];
		}
		else{
			//System.out.println("Frame Bit Number: " + frameBitNumber );
			return byteLUT[163 - frameBitNumber / 8];
		}
	}
	
	
	public int getTileNumber(Tile T) {
		return T.getTileYCoordinate() % 16;
	}
	
	@Override
	public int getTileNumber(int topBottom, int frameBitNumber) {
		int byteNumber = frameBitNumber / 8;
		if(topBottom==1){
			return tileLUT[byteNumber];
		}
		else{
			return tileLUT[163 - byteNumber];
		}
	}

	@Override
	public int getTileXCoordinate(XilinxConfigurationSpecification spec, FrameAddressRegister far) {
		
		
		InitConfigColumnIndexArray(spec);
	
		
		if(far.getBlockType() == 0){
			return BlockType0XIndexArray[far.getColumn()];
		}
		else
	      return BlockType1XIndexArray[far.getColumn()];
		/*
		int tileX = 0;
		
		// Initialize some helper arrays
		checkSpecAndBRAM(spec);
		
		// BRAM blockTypes have different column numbers
		if(isBRAMBlockType(far.getBlockType())){
			if(far.getColumn()>7){
				System.out.println("Bad News!");
			}
			if(bramIndicies.get(far.getColumn()) > clkColumnIndex)
				return bramIndicies.get(far.getColumn())-1;
			else
				return bramIndicies.get(far.getColumn());
		}

		// If we are here, we know its a logic blockType
		tileX = far.getColumn();
		for(int i : bramIndicies){
			if(i <= tileX){
				tileX++;
			}
			else{
				break;
			}
		}
		
		if(far.getColumn() >= clkColumnIndex){
			tileX--;
		}
		return tileX;*/
	}




	private boolean isBRAMBlockType(int blockType){
		for(int i : bramBlockTypes){
			if(i == blockType)
				return true;
		}
		return false;
	}
	
	
	public Tile getIntTile(XilinxConfigurationSpecification spec, FrameAddressRegister far, Bit B, Device device){
		
		InitConfigColumnIndexArray(spec); 
		
		int x = getTileXCoordinate(spec, far);
		int y = getTileYCoordinate(spec, far, B.getBitNumber());
	    	
		String TileName = "INT_X" + x + "Y" + y;
		Tile T = null; 
		if((T =device.getTile(TileName)) == null){
			System.out.println(TileName);
		}
		return device.getTile(TileName);
	}
	private void checkSpecAndBRAM(XilinxConfigurationSpecification spec) {
		if(bramSpec != spec){
			bramSpec = spec;
			int i = 0;
			bramIndicies = new ArrayList<Integer>();
			for(BlockSubType b : spec.getOverallColumnLayout()){
				if(b.equals(V4ConfigurationSpecification.BRAMINTERCONNECT)){
					bramIndicies.add(i);
				}
				if(b.equals(V4ConfigurationSpecification.CLK)){
					clkColumnIndex = i - bramIndicies.size();
				}
				i++;
			}
		}
	}
	
	@Override
	public int getTileYCoordinate(XilinxConfigurationSpecification spec, FrameAddressRegister far, int bitNumber) {
		int y = 0;
		int tileNumber = getTileNumber(far.getTopBottom(), bitNumber);
		if(far.getTopBottom()==0){
			y = (spec.getTopNumberOfRows()*16) + far.getRow()*16; 
		}
		else{
			y = ((spec.getBottomNumberOfRows()-1) - far.getRow())*16;
		}
		if (tileNumber == -1 || isClkColumn(spec, far)){
			// Special case when in the center hclk 
			tileNumber = 7;
		}
		return y + tileNumber;
	}
	
	/**
	 * Examines the configured frames of the FPGA and extracts all of the known PIPs.
	 * @return The list of PIPs found from the configured FPGA object.
	 */
	/*public ArrayList<PIP> extractPIPs(FPGA fpga, Design design){
		List<Integer> data;
		FrameAddressRegister far = fpga.getFAR();
		XilinxConfigurationSpecification spec = fpga.getDeviceSpecification();
		possiblePIPSubsets = new ArrayList<PIP>();
		far.initFAR();
		int bitNumber;
		int word;
		int mask = 0x80000000;
		db.openConnection();
		while(far.validFARAddress()){
			data = fpga.getCurrentFrame().getData().getAllFrameWords();
			for(int i=0; i < data.size(); i++){
				if((word = data.get(i)) != 0){
					bitNumber = i*32;
					for(int j=0; j < 32; j++){
						if((mask & word) == mask){
							if(!(bitNumber > 659 && bitNumber < 672)){
								//System.out.printf("0x%08x %d\r\n",far.getAddress(),bitNumber);
								checkPIPBit(far, bitNumber, spec, design, fpga);								
							}

						}
						bitNumber++;
						word = word << 1;
					}
				}
			}
			//System.out.println("FAR: " + far.getTopBottom() + " " + far.getBlockType() + " " + far.getRow() + " " + far.getColumn() + " " + far.getMinor());
			far.incrementFAR();
		}

		// Create master PIP list
		ArrayList<PIP> pipList = new ArrayList<PIP>();
		pipList.addAll(pips);
		
		
		
		for(PIP p : possiblePIPSubsets){
			PIP q = pipSubsets.get(new PIP(null, p.getStartWire(), p.getEndWire()));
			q.setTile(p.getTile());
			pipList.remove(q);
		}
		return pipList;
	}*/
	
	/**
	 * Check if this is the right block type.
	 * @param far The frameAddressRegister object containing the FAR for the FPGA
	 * @param bitNumber The absolute bit number into the frame we are checking 
	 * @param spec The Xilinx part specification
	 * @param fpga The actual FPGA object that contains the configuration frame information.
	 */
	private void InitConfigColumnIndexArray(XilinxConfigurationSpecification spec){
		if(spec != bramSpec){
			bramSpec = spec;
			List<BlockSubType> columnLayout = spec.getOverallColumnLayout();
			int [] temp = new int[columnLayout.size()];
			BlockType0XIndexArray = new int[columnLayout.size()];
			BlockType1XIndexArray = new int[columnLayout.size()]; // This is way to big
			int TileIndex = 0;      //X Coordinate of the InterConnect Tile
			int ConfigColIndex = 0; //Configuration Column Index
			int bramIndex = 0;      //BRAM Column Index
			int whichblock = -1; 
			for(BlockSubType T: columnLayout){
				//System.out.println(T.toString() + " " + TileIndex + " " + bramIndex + " " + ConfigColIndex);
				if(T.toString().equals("CLK") || T.equals("LOGIC_OVERHEAD")){
					 BlockType0XIndexArray[ConfigColIndex] =-9999;
					 ConfigColIndex++;
					
				} else {       
					if(T.toString().equals("BRAMINTERCONNECT")){
						temp[TileIndex] = bramIndex;
						BlockType0XIndexArray[ConfigColIndex] = -TileIndex; 
						BlockType1XIndexArray[bramIndex] = TileIndex; 
						bramIndex++;
					} else {
						BlockType0XIndexArray[ConfigColIndex] = TileIndex; 
						temp[TileIndex] = ConfigColIndex; 
						ConfigColIndex++;
					}
					TileIndex++; 
				}
				
			}
			
			this.configColumnIndexArray = temp;
			System.out.println("\n configColu: ");
			for(int i: configColumnIndexArray){
				System.out.print(i + " ");
			}
			
			System.out.println("\n BlockType0: ");
			for(int i: BlockType0XIndexArray){
				System.out.print(i + " ");
			}
			
			System.out.println("\n BlockType1: ");
			for(int i: BlockType1XIndexArray){
				System.out.print(i + " ");
			}
			System.out.println("Done.");
		}
	}
	
	
	
	private void checkPIPBit(FrameAddressRegister far, int bitNumber, XilinxConfigurationSpecification spec, Design design, FPGA fpga){
		BlockSubType blk = spec.getBlockSubtype(spec, far);
		
		if(blk.equals(V4ConfigurationSpecification.CLB)){
			if(bitNumber >> 5 == 20){
				checkForMatchingPIP(far, bitNumber, spec, fpga,  design, "\"hclk\"");
			}
			else{
				checkForMatchingPIP(far, bitNumber, spec, fpga,  design, "\"clb\"");
			}
			
		}
		if(blk.equals(V4ConfigurationSpecification.CLK)){
			checkForMatchingPIP(far, bitNumber, spec, fpga,  design, "\"clk_hrow\"");
		}
		else if(blk.equals(V4ConfigurationSpecification.LOGIC_OVERHEAD) ||
				blk.equals(V4ConfigurationSpecification.BRAMOVERHEAD)){
			return;
		}
		else{ // Other LOGIC BLOCK TYPES
			if(bitNumber >> 5 == 20){
				checkForMatchingPIP(far, bitNumber, spec, fpga, design, "\"hclk\"");
			}
			else{
				checkForMatchingPIP(far, bitNumber, spec, fpga, design, "\"int_so\"");
			}
		}
	}
	private void checkForMatchingPIP(FrameAddressRegister far, int bitNumber, 
								XilinxConfigurationSpecification spec, FPGA fpga, Design design, String intType){
		ResultSet rs;
		ResultSet rs2;
		//System.out.println(" " + far.getMinor() + " " + getByteOffset(far.getTopBottom(),bitNumber,isClkColumn(spec, far)) + " " + getMask(far.getTopBottom(),bitNumber,isClkColumn(spec, far)));
		
		// This will find all the compatible PIPs with this particular bit
		rs = db.makeRoutingQuery(spec.getDeviceFamily(), 
				intType, 
				far.getMinor(),
				getByteOffset(far.getTopBottom(),bitNumber,isClkColumn(spec, far)), 
				getMask(far.getTopBottom(),bitNumber,isClkColumn(spec, far)));
		try {
			// For each of those compatible pips, we need to check if each one has all its bits turned on
			while(rs.next()){
				PIP P = new PIP(null, design.getWireEnumerator().getWireEnum(rs.getString(2)), design.getWireEnumerator().getWireEnum(rs.getString(4)));
				rs2 = db.makeRoutingPIPMatchQuery(spec.getDeviceFamily(), intType,	P, design.getWireEnumerator());
				//System.out.println("  " + rs.getString(2) + " " + rs.getString(3) + " " + rs.getString(4));
				boolean aMatch = true;
				while(rs2.next()){
					try{
					if(!resultBitIsOn(fpga,far,bitNumber,rs2.getInt(6),rs2.getInt(7),rs2.getInt(8))){
						aMatch = false;
						break;
					}
					}catch(java.lang.ArrayIndexOutOfBoundsException e){
						aMatch = false;
						break;
					}
				}
				if(aMatch){
					Tile tile = null;
					if(rs.getString(1).equals("clb") || rs.getString(1).equals("int_so")){
						String tileName = TileType.INT + "_X" + getTileXCoordinate(spec, far) + "Y" + getTileYCoordinate(spec, far, bitNumber);
						tile = design.getDevice().getTile(tileName); 												
					}
					else if(rs.getString(1).equals("hclk")){
						String tileName = TileType.HCLK + "_X" + getTileXCoordinate(spec, far) + "Y" + getTileYCoordinate(spec, far, bitNumber);
						tile = design.getDevice().getTile(tileName); 		
					}
					else if(rs.getString(1).equals("clk_hrow")){
						String tileName = TileType.CLK_HROW + "_X" + getTileXCoordinate(spec, far) + "Y" + getTileYCoordinate(spec, far, bitNumber);
						tile = design.getDevice().getTile(tileName); 												
					}
					
					//System.out.println("  " + tile.toString() + " " + rs.getString(1) + " " + rs.getString(2) + " " +  rs.getString(3)+ " " +
					//		rs.getString(4) + " " + rs.getInt(5) + " " + rs.getInt(6) + " " + rs.getInt(7) + " " + rs.getInt(8));
					
					PIP p = new PIP(tile, design.getWireEnumerator().getWireEnum(rs.getString(2)),  design.getWireEnumerator().getWireEnum(rs.getString(4))); 
					
					pips.add(p);
					
					// This pip could possibly have other pips which use a subset of bits 
					// that this one does.  These need to be removed, but will have to be 
					// done after the entire bitstream is checked.
					if(rs.getInt(5) == 3){
						possiblePIPSubsets.add(p);
					}
				}
			}
		} 
		catch (SQLException e) {
			db.printSQLExceptionMessages(e);
			e.printStackTrace();
		}
	}
	
/*	public Tile TileToXDLTile(Tile X){
	
		Tile N = new Tile(); 
		
		N.setType(X.getType());
	   
		N.setXCoord(X.getTileXCoordinate());
		N.setYCoord(X.getTileYCoordinate());
	
		return N; 
	}*/
	
	private boolean resultBitIsOn(FPGA fpga, FrameAddressRegister far, int bitNumber, int minor, int byteOffset, int mask){
		int bitFAR = ((~fpga.getDeviceSpecification().getMinorMask()) & far.getAddress()) | minor; 
		int tileNumber = getTileNumber(far.getTopBottom(), bitNumber);
		int index = getFrameBitNumber(far.getTopBottom(), tileNumber, byteOffset, mask);
		//System.out.println("   index=" + index + " tileNumber=" + tileNumber + " bit=" + fpga.getFrame(bitFAR).getData().getBit(index) + " minor=" + minor + " offset=" + byteOffset + " mask=" + mask);
		return 1 == fpga.getFrame(bitFAR).getData().getBit(index);
	}
	
	
	@Override
	public int getTopBottom(XilinxConfigurationSpecification spec, Tile tile){
		int row = tile.getTileYCoordinate() / 16;
		
		if(row < spec.getBottomNumberOfRows()){
			// Bottom
			return 1; 
		}
		else{
			// Top
			return 0;
		}
	}
	
	public ArrayList<Bit> LookUpPIPBits(PIP P, Design D){
		
		ArrayList<Bit> Bits = new ArrayList<Bit>(); 
		ResultSet rs; 
		
		db.openConnection();
		
			
		rs = db.makeRoutingPIPMatchQuery("Virtex4", "clb", P, D.getWireEnumerator());
	    
		try {
			
			while(rs.next()){
				
				int minor = Integer.parseInt(rs.getString(6));
				int offset = Integer.parseInt(rs.getString(7));
				int mask  = Integer.parseInt(rs.getString(8));
				//System.out.println(rs.getString(1) + " " + rs.getString(2) + " " + rs.getString(3)+ " " + rs.getString(4)+ " " + rs.getString(5) + " " + rs.getString(6) + " " + rs.getString(7) + " " + rs.getString(8));
			    Bits.add(new Bit(minor, offset, mask));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		db.closeConnection();
		return Bits; 
	}
		
	@Override
	public int getConfigurationRow(XilinxConfigurationSpecification spec, Tile tile){
		int row = tile.getTileYCoordinate() / 16;
		
		if(row < spec.getBottomNumberOfRows()){
			// Bottom
			row = spec.getBottomNumberOfRows() - row - 1;
		}
		else{
			// Top
			row = row - spec.getBottomNumberOfRows();
		}
		return row;
	}
	
	@Override
	public int getConfigurationColumn(XilinxConfigurationSpecification spec, Tile tile){
		InitConfigColumnIndexArray(spec);
		
		return this.configColumnIndexArray[tile.getTileXCoordinate()];
	}
	
	
	
	@Override
	public void printFrame(FrameData data){

	}
}

package edu.byu.ece.gremlinTools.xilinxToolbox;

import java.util.HashSet;

import edu.byu.ece.rapidSmith.bitstreamTools.configuration.FrameAddressRegister;
import edu.byu.ece.rapidSmith.bitstreamTools.configuration.FrameData;
import edu.byu.ece.rapidSmith.bitstreamTools.configurationSpecification.BlockSubType;
import edu.byu.ece.rapidSmith.bitstreamTools.configurationSpecification.V5ConfigurationSpecification;
import edu.byu.ece.rapidSmith.bitstreamTools.configurationSpecification.XilinxConfigurationSpecification;
import edu.byu.ece.rapidSmith.device.Tile;


public class V5XilinxToolbox extends AbstractXilinxToolbox{

	private int clkColumnIndex = -1;
	
	public int getClkColumnIndex(XilinxConfigurationSpecification spec){
		if(clkColumnIndex != -1){
			return clkColumnIndex;
		}
		int i=0;
		for(BlockSubType blk : spec.getBlockSubTypeLayout(V5ConfigurationSpecification.LOGIC_INTERCONNECT_BLOCKTYPE)){
			if(blk.equals(V5ConfigurationSpecification.CLK)){
				clkColumnIndex = i;
				return clkColumnIndex;
			}
			i++;
		}
		System.out.println("ERROR: Could not find clk column block type!");
		return -1;
	}
	
	@Override
	public boolean isClkColumn(XilinxConfigurationSpecification spec,
			FrameAddressRegister far) {
		return spec.getBlockSubtype(spec, far).equals(V5ConfigurationSpecification.CLK);
	}
	
	@Override
	public int getMask(int topBottom, int frameBitNumber, boolean isClkColumn) {
		return 1 << 7 - (frameBitNumber % 8);
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
		if(frameBitNumber > 671){
			return ((frameBitNumber-32) % (8*8)) / 8;	
		}
		else{
			return (frameBitNumber % (8*8)) / 8;
		}
		
	}
	
	@Override
	public int getTopBottom(XilinxConfigurationSpecification spec, Tile tile){
		int row = tile.getTileYCoordinate() / 20;
		
		if(row < spec.getBottomNumberOfRows()){
			// Bottom
			return 1; 
		}
		else{
			// Top
			return 0;
		}
	}
	
	@Override
	public int getConfigurationRow(XilinxConfigurationSpecification spec, Tile tile){
		int row = tile.getTileYCoordinate() / 20;
		System.out.println("row="+row);
		System.out.println("tile="+tile);
		///*
		String ti =tile.getName();
		String PCIE="PCIE";
		int i1= ti.indexOf(PCIE);
		if(i1!=-1)
		{
			return row=0;
		}
		//*/
		
		if(row < spec.getBottomNumberOfRows()){
			// Bottom
			row = (spec.getBottomNumberOfRows()-1) - row;
		}
		else{
			// Top
			row = row - spec.getBottomNumberOfRows();
		}
		return row;
	}
	
	@Override
	public int getConfigurationColumn(XilinxConfigurationSpecification spec, Tile tile){
		System.out.println(tile.getTileXCoordinate()+"=tile spec="+getClkColumnIndex(spec));
		
//todo verify with different fpgas
		
	//	System.out.println("Spec!!!!!="+spec+"tile="+tile);
		int index1 = tile.getName().indexOf("PPC_B");
		if(index1!=-1)
		{   System.out.println("Not verifible at the moment  top verse bot issue and col=10 verse col=23");
			return 23;
		}
		
		
//todo verify this algorithm		
		if(tile.getTileXCoordinate() > (2*getClkColumnIndex(spec)+10))
		{System.out.println("Clk Element like SYSMON Error is Possible"); 
		return (getClkColumnIndex(spec));
		}//  takes care of the elements like SYSMON,...

		if(tile.getTileXCoordinate() >= getClkColumnIndex(spec)){
			return tile.getTileXCoordinate() + 1;
		}
		return tile.getTileXCoordinate();
	}
	
	@Override
	public int getFARFromTile(XilinxConfigurationSpecification spec, Tile tile, int minor){
		int topBottom = getTopBottom(spec, tile);
		int row = getConfigurationRow(spec, tile);
		int column = getConfigurationColumn(spec, tile);
		System.out.println(" t/b="+topBottom+"0 row="+row+" col="+column+" minor="+minor);  //"spec="+spec+
		//System.out.println(FrameAddressRegister.createFAR(spec, topBottom, 0, row, column, minor));
		return FrameAddressRegister.createFAR(spec, topBottom, 0, row, column, minor);
	}
	
	@Override 
	public int getTileNumber(Tile tile){
System.out.println("Tile is this ="+tile);
		return tile.getTileYCoordinate() % 20;
	}
//todo find out if this is acceptable	
	
	public int getFrameBitNumber(int topBottom, int tileNumber, int byteOffset, int mask,XilinxConfigurationSpecification spec,Tile tile)
	{  //will this run in case of CRC64
		//if(identy of crc32)
		/*	4 case lowest possibility (byteOffset*8 + (Integer.numberOfLeadingZeros(mask)-24));
		 *         2 pos   416+(byteOffset*8 + (Integer.numberOfLeadingZeros(mask)-24));
		 *         3 pos   1311-416-(byteOffset*8 + (Integer.numberOfLeadingZeros(mask)-24));
		 *         4 pos   1311-(byteOffset*8 + (Integer.numberOfLeadingZeros(mask)-24));
		 * */
		//if (identy of crc64)
		//          2 cases lowest (byteOffset*8 + (Integer.numberOfLeadingZeros(mask)-24));
		//            1311 -(byteOffset*8 + (Integer.numberOfLeadingZeros(mask)-24));
		//if (identy GTP/GTX) return frameBitNumber = byteOffset*8 + (Integer.numberOfLeadingZeros(mask)-24);}
		int frameBitNumber = 0;
		//System.out.println(spec.getBlockTypeInstances());
		//System.out.println("working"+tile.getPrimitiveSites().length);
		
		System.out.println("getFrameBitNumber method: " +
                		   " topBottom = "  + topBottom +
                		   " tileNumber = " +tileNumber +
                		   " offset = "     +byteOffset +
                		   " mask = "       +mask       +
//                		   " spec = "       +spec       +
                		   " tile = "       + tile					
		                  );
// the checks to see if it is a PCIE which use entire frames for a single 
//PCIE or if it is in the clk spine which can;'t be any anyother locations
		String tilename = tile.getName();
		int index1 = tilename.indexOf("PCIE");
		
		//GTP/GTX		
		int index2= tilename.indexOf("GT3_X");
		int index3= tilename.indexOf("GTX_X");
		
		if ((index3!=-1)||(index2!=-1)||(index1!=-1)|| (byteOffset>=80 && byteOffset<=83))
		{
			int numOff=7;
			int maskT=mask;
			for(;maskT!=1;numOff--)
			{maskT=maskT>>1;
		System.out.println("mask="+maskT+" and offset="+numOff);
			}
			System.out.println("mask="+maskT+" and offset="+numOff);
			frameBitNumber = (byteOffset*8)+numOff;
			System.out.println(frameBitNumber);
			return frameBitNumber;			
		}

		
		/*
		if(index1!=-1){
			System.out.println("CLK in tile weird problem start here");
			int numOff=7;
			int maskT=mask;
			for(;maskT!=1;numOff--)
			{maskT=maskT>>1;
		System.out.println("mask="+maskT+" and offset="+numOff);
			}
			System.out.println("mask="+maskT+" and offset="+numOff);
			frameBitNumber = (byteOffset*8)+numOff;
			System.out.println(frameBitNumber);
			return frameBitNumber;
			//works for BUFR
			
			
		}*/
		if(tileNumber > 9){
			frameBitNumber = 32 + tileNumber*64 + byteOffset*8 + (Integer.numberOfLeadingZeros(mask)-24);
		}
		else{
			frameBitNumber = tileNumber*64 + byteOffset*8 + (Integer.numberOfLeadingZeros(mask)-24);
		}
		// I am sure there are errors here this is an invalid state below.
		if(frameBitNumber>1312){System.out.println("overcalculation");frameBitNumber = byteOffset*8 + (Integer.numberOfLeadingZeros(mask)-24);}
	//	System.out.println("here we go!!!");
		return frameBitNumber;
	}
	
	@Override
	public int getFrameBitNumber(int topBottom, int tileNumber, int byteOffset, int mask) {
		int frameBitNumber = 0;
		
		if(tileNumber > 9){
			frameBitNumber = 32 + tileNumber*64 + byteOffset*8 + (Integer.numberOfLeadingZeros(mask)-24);
		}
		else{
			frameBitNumber = tileNumber*64 + byteOffset*8 + (Integer.numberOfLeadingZeros(mask)-24);
		}		
		//System.out.println("frameBitNumber=" + frameBitNumber + " tileNumber=" + tileNumber +" byteOffset=" +  byteOffset + " mask=" + mask);
		return frameBitNumber;
	}
	
	@Override
	public void printFrame(FrameData data){
		StringBuilder s = new StringBuilder();
		String tmp;
		int index;
		for(Integer i : data.getAllFrameWords()){
			tmp = Integer.toHexString(i);
			while(tmp.length() < 8){
				tmp = "0" + tmp;
			}
			s.append(tmp);
		}
		
		StringBuilder binaryString = new StringBuilder();
		String[] binaryLUT = {"0000","0001","0010","0011",
							  "0100","0101","0110","0111",
							  "1000","1001","1010","1011",
							  "1100","1101","1110","1111"};
		HashSet<Integer> cr = new HashSet<Integer>();
		cr.add(15); cr.add(31); cr.add(47); cr.add(63); cr.add(79);
		cr.add(95); cr.add(111); cr.add(127); cr.add(143); cr.add(159);
		cr.add(167); cr.add(183); cr.add(199); cr.add(215); cr.add(231);
		cr.add(247); cr.add(263); cr.add(279); cr.add(295); cr.add(311);
		
		for(int i=0; i < s.length(); i++){
			tmp = (String) s.subSequence(i, i+1);
			index = Integer.valueOf(tmp, 16);
			binaryString.append(binaryLUT[index]);
			if(i % 2 != 0){
				binaryString.append(" ");
			}
			if(cr.contains(i)){
				binaryString.append("\n");
			}
		}
		
		System.out.println(binaryString.toString());
	}
}

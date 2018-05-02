package edu.byu.ece.gremlinTools.xilinxToolbox;


import java.util.ArrayList;

import edu.byu.ece.rapidSmith.bitstreamTools.configuration.FPGA;
import edu.byu.ece.rapidSmith.bitstreamTools.configuration.FrameAddressRegister;
import edu.byu.ece.rapidSmith.bitstreamTools.configuration.FrameData;
import edu.byu.ece.rapidSmith.bitstreamTools.configurationSpecification.XilinxConfigurationSpecification;
import edu.byu.ece.rapidSmith.design.PIP;
import edu.byu.ece.rapidSmith.device.Tile;


public class AbstractXilinxToolbox implements XilinxToolbox{

	public int getByteOffset(int topBottom, int frameBitNumber, boolean isClkColumn) {
		// TODO Auto-generated method stub
		return -1;
	}

	public int getFrameBitNumber(int topBottom, int tileNumber, int byteOffset,
			int mask) {
		// TODO Auto-generated method stub
		return -1;
	}

	public int getMask(int topBottom, int frameBitNumber, boolean isClkColumn) {
		if(topBottom==1){
			return 1 << 7 - (frameBitNumber % 8);
		}
		else{
			return 1 << frameBitNumber % 8;
		}
	}

	public boolean isClkColumn(XilinxConfigurationSpecification spec,
			FrameAddressRegister far) {
		return false;
	}
	
	public int getTileNumber(int topBottom, int frameBitNumber) {
		return -1;
	}

	public int getTileXCoordinate(XilinxConfigurationSpecification spec,
			FrameAddressRegister far) {
		return -1;
	}

	public int getTileYCoordinate(XilinxConfigurationSpecification spec,
			FrameAddressRegister far, int bitNumber) {
		return -1;
	}

	public ArrayList<PIP> extractPIPs(FPGA fpga) {
		return null;
	}
	
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
	
	public int getConfigurationRow(XilinxConfigurationSpecification spec, Tile tile){
		int row = tile.getTileYCoordinate() / 20;
		          
		if(row < spec.getBottomNumberOfRows()){
			// Bottom
			row = spec.getBottomNumberOfRows() - row;
		}
		else{
			// Top
			row = row - spec.getBottomNumberOfRows();
		}
		return row;
	}
	
	public int getConfigurationColumn(XilinxConfigurationSpecification spec, Tile tile){
		return tile.getTileXCoordinate();
	}
	
	
	public int getFARFromTile(XilinxConfigurationSpecification spec, Tile tile, int minor){
		int topBottom = getTopBottom(spec, tile);
		int row = getConfigurationRow(spec, tile);
		int column = getConfigurationColumn(spec, tile);
		return FrameAddressRegister.createFAR(spec, topBottom, 0, row, column, minor);
	}
	
	public int getTileNumber(Tile tile){
		return tile.getTileYCoordinate() % 20;
	}
	
	public void printFrame(FrameData data){
		
	}
}

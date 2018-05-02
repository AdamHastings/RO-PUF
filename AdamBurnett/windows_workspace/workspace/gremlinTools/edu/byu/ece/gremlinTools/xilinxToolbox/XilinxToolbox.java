package edu.byu.ece.gremlinTools.xilinxToolbox;


import java.util.ArrayList;

import edu.byu.ece.rapidSmith.bitstreamTools.configuration.FPGA;
import edu.byu.ece.rapidSmith.bitstreamTools.configuration.FrameAddressRegister;
import edu.byu.ece.rapidSmith.bitstreamTools.configuration.FrameData;
import edu.byu.ece.rapidSmith.bitstreamTools.configurationSpecification.XilinxConfigurationSpecification;
import edu.byu.ece.rapidSmith.design.PIP;
import edu.byu.ece.rapidSmith.device.Tile;

public interface XilinxToolbox {

	public int getFrameBitNumber(int topBottom, int tileNumber, int byteOffset, int mask);
	
	public int getMask(int topBottom, int frameBitNumber, boolean isClkColumn);
	
	public boolean isClkColumn(XilinxConfigurationSpecification spec, FrameAddressRegister far);
	
	public int getByteOffset(int topBottom, int frameBitNumber, boolean isClkColumn);
	
	public int getTileNumber(int topBottom, int frameBitNumber);
	
	public int getTileXCoordinate(XilinxConfigurationSpecification spec, FrameAddressRegister far);
	
	public int getTileYCoordinate(XilinxConfigurationSpecification spec, FrameAddressRegister far, int bitNumber);
	
	public ArrayList<PIP> extractPIPs(FPGA fpga);
	
	public int getTopBottom(XilinxConfigurationSpecification spec, Tile tile);
	
	public int getConfigurationRow(XilinxConfigurationSpecification spec, Tile tile);
	
	public int getConfigurationColumn(XilinxConfigurationSpecification spec, Tile tile);
	
	public int getFARFromTile(XilinxConfigurationSpecification spec, Tile tile, int minor);
	
	public int getTileNumber(Tile tile);
	
	public void printFrame(FrameData data);
}

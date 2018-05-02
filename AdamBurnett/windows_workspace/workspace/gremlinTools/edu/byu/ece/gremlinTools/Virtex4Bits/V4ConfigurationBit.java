package edu.byu.ece.gremlinTools.Virtex4Bits;


import edu.byu.ece.gremlinTools.xilinxToolbox.*;

import edu.byu.ece.rapidSmith.bitstreamTools.configuration.FPGA;
import edu.byu.ece.rapidSmith.bitstreamTools.configuration.FrameAddressRegister;
import edu.byu.ece.rapidSmith.device.Device;
import edu.byu.ece.rapidSmith.device.Tile;


//This Type of Bit is Tied To a Particular FPGA and Tile and Have a Particular Value Connected With a Given Bit-Stream

public class V4ConfigurationBit extends Bit {
    
	//A Reference to the FPGA to which this bit Belongs
	private FPGA fpga; 
	//The Frame Address of the Frame that Stores this Configuration Bit
	private FrameAddressRegister FAR; 
	//The Tile to which this Configuration Bit Belongs.
	private Tile T; 
    //A Reference to a V4 Toolbox Providing required functionality
	private V4XilinxToolbox TB;
	private int AttributeValue;
    
	public V4ConfigurationBit(Bit B) {
	
		
		//Ensure that these are set.
		setBitNumber(B.getBitNumber());
	    setBitNumberType(B.getBitNumberType());
	    setMask(B.getMask());
	    setMinor_address(B.getMinorAddress());
	    setOffset(B.getOffset());  
	}
	
	public V4ConfigurationBit(FPGA fpga, Bit B, int far, V4XilinxToolbox TB, Device device){
		FAR = new FrameAddressRegister(fpga.getDeviceSpecification(), far);
		T = TB.getIntTile(fpga.getDeviceSpecification(), FAR, B, device);
		
		//if(T==null)
		//	System.out.println("Bad News!");
		this.TB = TB;
		setFpga(fpga);
		//setTile(T);
			
		setMinor_address(B.getMinorAddress());
		
		//setFAR(T);
		
		AttributeValue = B.getProperValue(); 
		if(B.getBitNumber() == -1){
			setMask(B.getMask());
			setOffset(B.getOffset());
			
			setBitNumber(CalculateBitNumber());
			setBitNumberType(BitNumberType.Gremlin);
		} else {
		  setBitNumber(B.getBitNumber());
		  setBitNumberType(B.getBitNumberType());
				  
		  setMask(TB.getMask(FAR.getTopBottom(), getBitNumber(), TB.isClkColumn(fpga.getDeviceSpecification(), FAR)));
		  setOffset(TB.getByteOffset(FAR.getTopBottom(), getBitNumber(), TB.isClkColumn(fpga.getDeviceSpecification(), FAR)));
		}
	}
	
	public V4ConfigurationBit(FPGA fpga, Bit B, Tile T, V4XilinxToolbox TB){
		this.TB = TB;
		setFpga(fpga);
		setTile(T);
				
		if(FAR == null){
		  FAR = new FrameAddressRegister(fpga.getDeviceSpecification());
		}
		setMinor_address(B.getMinorAddress());
		setFAR(T);
		
		AttributeValue = B.getProperValue(); 
		if(B.getBitNumber() == -1){
			setMask(B.getMask());
			setOffset(B.getOffset());
			
			setBitNumber(CalculateBitNumber());
			setBitNumberType(BitNumberType.Gremlin);
		} else {
		  setBitNumber(B.getBitNumber());
		  setBitNumberType(B.getBitNumberType());
				  
		  setMask(TB.getMask(FAR.getTopBottom(), getBitNumber(), TB.isClkColumn(fpga.getDeviceSpecification(), FAR)));
		  setOffset(TB.getByteOffset(FAR.getTopBottom(), getBitNumber(), TB.isClkColumn(fpga.getDeviceSpecification(), FAR)));
		}
		
	}
    
	
	public int getAttributeValue(){
		return AttributeValue; 
	}
	
	public void setFpga(FPGA fpga) {
		this.fpga = fpga;
	}

	public FPGA getFpga() {
		return fpga;
	}

	public void setFAR(Tile T) {
	  if(T != null){
		FAR.setFAR(TB.getFARFromTile(fpga.getDeviceSpecification(), T, this.getMinorAddress()));
	  }
	}

	public FrameAddressRegister getFAR() {
		return FAR;
	}

	public void setTile(Tile t) {
		if(this.FAR == null){
			this.FAR = new FrameAddressRegister(fpga.getDeviceSpecification());
		}
		setFAR(t);
		T = t;
		setBitNumber(CalculateBitNumber()); 
		
	}

	public Tile getTile() {
		return T;
	}
	
	public int CalculateBitNumber(){
		return TB.getFrameBitNumber(FAR.getTopBottom(), TB.getTileNumber(T), getOffset(), getMask());
	}
	
	public void SetBitValueInBitStream(int NewValue, Tile T) {
        
		if(NewValue == 0 || NewValue == 1) {
			setTile(T);
			//System.out.println("Bit #: " + getBit_number());
			//System.out.println(fpga.getFrame(FAR));
			fpga.getFrame(FAR.getAddress()).getData().setBit(getBitNumber(), NewValue);
			//System.out.println(fpga.getFrame(FAR));
		}
		else {
			System.out.println("Invalid Bitstream Assignment.");
		}
	}
    
	public boolean verifyBit(Tile T){
		this.setTile(T);
		if(this.getAttributeValue() == this.getValue()){
			return true;
		} else {
			return false; 
		}
	}
	
	public void setToolbox(V4XilinxToolbox TB){
		this.TB = TB; 
	}

	public int getValue() {
		System.out.println(FAR);
		System.out.println(fpga.getFrame(FAR.getAddress()).toString());
		return fpga.getFrame(FAR.getAddress()).getData().getBit(getBitNumber());
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("FPGA: "+fpga+"\n");
		sb.append("FAR: "+FAR+"\n");
		sb.append("TILE: "+T+"\n");
		sb.append("AttributeValue: "+AttributeValue+"\n");
		
		return sb.toString();
	}
}

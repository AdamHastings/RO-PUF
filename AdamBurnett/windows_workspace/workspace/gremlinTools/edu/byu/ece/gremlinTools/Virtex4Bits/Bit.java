package edu.byu.ece.gremlinTools.Virtex4Bits;

import java.io.Serializable;

import edu.byu.ece.gremlinTools.xilinxToolbox.V4XilinxToolbox;
import edu.byu.ece.rapidSmith.bitstreamTools.configuration.FrameAddressRegister;

public class Bit implements Serializable{
    	
	private int bitNumber; 
	private BitNumberType bitNumberType; 
	private int minorAddress;
    private int offset;
    private int mask; 
    private int properValue; //The appropriate value to configure to for a given attribute
    
    public Bit(int far_address, int bitNumber, BitNumberType BNT){
    	setBitNumberType(BNT);
    	setBitNumber(bitNumber);
    	setMinor_address(far_address & 0x3F);
    	setOffset(-1);
    	setMask(-1);
    }
    
    public Bit(int minor_address, int offset, int mask, int properValue){
    	setMinor_address(minor_address);
    	setOffset(offset);
    	setMask(mask);
    	setBitNumber(-1);
    	setProperValue(properValue);
    }
    
    
    public Bit(int minor_address, int offset, int mask){
    	setMinor_address(minor_address);
    	setOffset(offset);
    	setMask(mask);
    	setBitNumber(-1);
    }
    
    //Constructor for Configuration Bit Class
    public Bit(){
      
    }
    
    public int hashCode() {
    	return (this.offset + this.mask)^(minorAddress);
    }
	public boolean equals(Object B){
		
		if(B != null){
		    Bit	C = (Bit) B; 
			
			if(C.minorAddress == this.minorAddress && 
			   C.offset == this.offset &&
			   C.mask == this.mask) {
				return true; 
			}
			else
			    return false;
		}
		return false; 
	}


	public void setMinor_address(int minor_address) {
		this.minorAddress = minor_address;
	}


	public int getMinorAddress() {
		return minorAddress;
	}


	public void setOffset(int offset) {
		this.offset = offset;
	}


	public int getOffset() {
		return offset;
	}


	public void setMask(int mask) {
		this.mask = mask;
	}


	public int getMask() {
		return mask;
	}
     
	public String toString(){
		return this.minorAddress + "" + this.offset + "" + this.mask + "" ;
	}


	public void setBitNumber(int bitNumber) {
		
		this.bitNumber = bitNumber; 
	}

	public int getBitNumber() {
		return bitNumber;
	}
	
    public int getGremlinBitNumber(){
    	
    	if(getBitNumberType() == BitNumberType.Xilinx){
    		return TranslateBitNumber(bitNumber);
    	}
    	else
    		return bitNumber;
    }
	

    public int getXilinxBitNumber(){
    	
    	if(getBitNumberType() == BitNumberType.Gremlin){
    		return TranslateBitNumber(bitNumber);
    	}
    	else
    		return bitNumber;
    }
    
	private int TranslateBitNumber(int OriginalBitNumber){
		
	    int original_word_index = OriginalBitNumber / 32;
	    int original_bit_index  = OriginalBitNumber % 32;
		
	    int new_bit_index = 31 - original_bit_index;
	    
	    return  original_word_index * 32 + new_bit_index;  
	}
    	public void setBitNumberType(BitNumberType bitNumberType) {
		this.bitNumberType = bitNumberType;
	}

	public BitNumberType getBitNumberType() {
		return bitNumberType;
	}

	public void setProperValue(int properValue) {
		this.properValue = properValue;
	}

	public int getProperValue() {
		return properValue;
	}
	
	
}

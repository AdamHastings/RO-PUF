package edu.byu.ece.gremlinTools.Virtex4Bits;

import java.io.Serializable;
import java.util.ArrayList;

import edu.byu.ece.rapidSmith.design.PIP;


public class Pip implements Serializable{
     
	private Bit row_bit; 
    private Bit column_bit;
    private Bit buffer_bit; 
    
    private ArrayList<Bit> Bits;
         
    private PIP pip;
    
    public Pip (PIP P) {
    	setPip(P);
    	Bits = new ArrayList<Bit>();
    }
    
    public Pip (PIP P, Bit row_bit, Bit column_bit) {
    	setPip(P);
    	setRowBit(row_bit);
    	setColumnBit(column_bit);
    	buffer_bit = null; 
    	
    }
    
    public void setBufferBit(Bit B){
    	buffer_bit = B; 
    }
    
    public boolean equals(Object O ){
    	Pip Other = (Pip) O;
    	
    	if(Other.getPip().equals(this.getPip()))
     	  return true;
    	else
    	  return false;
    }
    
	public String toString(){
	  if(buffer_bit == null)
		return pip.getStartWire()+ ":" + this.row_bit + ":" + this.column_bit;
	  else 
		return pip.getStartWire()+ ":" + this.buffer_bit + ":" + this.row_bit + ":" + this.column_bit;  
	}

	public void setPip(PIP pip) {
		this.pip = pip;
	}

	public PIP getPip() {
		return pip;
	}

	public void addBit(Bit bit) {
		Bits.add(bit);
	}

	public ArrayList<Bit> getBits() {
		return Bits;
	}


	public void setRowBit(Bit row_bit) {
		this.row_bit = row_bit;
	}

    public Bit getBufferBit(){
    	return buffer_bit; 
    }
	public Bit getRowBit() {
		return row_bit;
	}


	public void setColumnBit(Bit column_bit) {
		this.column_bit = column_bit;
	}


	public Bit getColumnBit() {
		return column_bit;
	}


	public void TransposePip() {
       //Transpose the row and column bit
	   if(buffer_bit == null){
		 Bit tmp = column_bit;
         column_bit = row_bit; 
         row_bit = tmp; 
	   }
	}
     
     
}

package edu.byu.ece.gremlinTools.WireMux;

import java.util.ArrayList;
import java.util.Iterator;

import edu.byu.ece.gremlinTools.Virtex4Bits.Bit;
import edu.byu.ece.gremlinTools.Virtex4Bits.Pip;
import edu.byu.ece.rapidSmith.device.WireEnumerator;



public class Group {
   
   private Bit bit;
   private ArrayList<Pip> groupPips;
  
  
   public Group(Bit bit){
	   this.setBit(bit);
	   this.groupPips = new ArrayList<Pip>();
   }  
   
   public ArrayList<Pip> getGroupPips(){
	   return this.groupPips;
   }
   
   public void addPip(Pip P){
	  
	  this.getGroupPips().add(P);
		     
   }
   
   public void setBit(Bit bit) {
		this.bit = bit;
   }

	public Bit getBit() {
		return bit;
	}
	
	public String toString(WireEnumerator W){
		String S;
		S = this.bit.toString() + "  ";
		for(Pip P:this.groupPips){
			String T = " ";
			for(int i=0; i< 15 - P.getPip().getStartWireName(W).length(); i++)
				T += " ";
			S += P.getPip().getStartWireName(W) + T; 
		}
		return S;
	}
}

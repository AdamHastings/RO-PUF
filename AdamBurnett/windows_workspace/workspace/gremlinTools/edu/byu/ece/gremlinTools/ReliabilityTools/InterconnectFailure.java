package edu.byu.ece.gremlinTools.ReliabilityTools;

import edu.byu.ece.gremlinTools.Virtex4Bits.V4ConfigurationBit;
import edu.byu.ece.rapidSmith.design.Net;
import edu.byu.ece.rapidSmith.design.PIP;
import edu.byu.ece.rapidSmith.device.WireEnumerator;

public class InterconnectFailure {
   
   private Net N;
   private PIP P;
   private PIP SEUConnection;
   private PIP ConnectionFromOtherNet; 
   private BitType BT; 
   private V4ConfigurationBit B;
   private InterconnectFailureType IFT; 
   
    
   public InterconnectFailure(){
	   N = null;
	   P = null;
	   BT = null;
	   IFT = null; 
	   B = null;
	   SEUConnection = null; 
	   ConnectionFromOtherNet = null; 
   }
   
   public String toString(WireEnumerator we){
	   String S=""; 
	   if(IFT != null)
		   S += "Interconnect Failure Type: " + IFT.toString() + "\n";
	   if(N!= null)
		   S += "Original Net: " + N.getName() + "\n";
	   if(P != null)
		   S += "\tOriginal Connection: " + P.toString(we);
	   if(SEUConnection != null)
		   S += "\tConnection Created By SEU: " + SEUConnection.toString(we);
	   if(ConnectionFromOtherNet != null)
		   S += "\tConnection From Other Net: " + ConnectionFromOtherNet.toString(we);
	   if(BT!= null)
		   S += "\tRow/Column Bit? " + BT.toString() + "\n"; 
	   if(B!=null)
		   S += "\tConfiguration Bit: " + B.toString() + "\n";
	   return S;
   }
   
   public void setNet(Net n){
	   N = n; 
   }
   
   public void setPIP(PIP p){
	   P = p; 
   }
   
   public void setRowOrColumnBit(BitType b){
	   BT = b; 
   }
   
   public void setSensitiveBit(V4ConfigurationBit b){
	   B = b; 
   }
   
   public void setInterconnectFailureType(InterconnectFailureType i){
	   IFT = i; 
   }
   
   public void setConnectionFromOtherNet(PIP p){
	   ConnectionFromOtherNet = p; 
   }
   public PIP getConnectionFromOtherNet(){
	   return ConnectionFromOtherNet;
   }
   public Net getNet(){
	   return N; 
   }
   
   public PIP getPIP(){
	   return P; 
   }
   
   public BitType getRowOrColumn(){
	   return BT; 
   }
   
   public V4ConfigurationBit getSensitiveBit(){
	   return B; 
   }
   
   public InterconnectFailureType getInterconnectFailureType(){
	   return IFT; 
   }

   public void setSEUConnection(PIP sEUConnection) {
	  SEUConnection = sEUConnection;
   }

   public PIP getSEUConnection() {
		return SEUConnection;
   }
}

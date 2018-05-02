package edu.byu.ece.gremlinTools.BackArcs;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import edu.byu.ece.rapidSmith.device.Device;
import edu.byu.ece.rapidSmith.device.Tile;
import edu.byu.ece.rapidSmith.device.WireConnection;
import edu.byu.ece.rapidSmith.device.WireEnumerator;
import edu.byu.ece.rapidSmith.device.helper.HashPool;
import edu.byu.ece.rapidSmith.util.FamilyType;
import edu.byu.ece.rapidSmith.util.FileTools;

/*
 *  This class reads in a device file and re-creates all the reverse arcs between tiles. 
 */


public class InterTileReverseArcs {
   
	private HashMap<Tile, HashMap<Integer,ArrayList<WireConnection>>> TileToReverseArcsMap;
   
   public InterTileReverseArcs(Device device, WireEnumerator we){
	  System.out.println("Generating Device BackArcs Database");
	  
	  TileToReverseArcsMap = new HashMap<Tile, HashMap<Integer, ArrayList<WireConnection>>>();
  
	  HashPool<WireConnection> UniqueBackArcs = new HashPool<WireConnection>();
	  HashPool<ArrayList<WireConnection>> UniqueBackArcArrays = new HashPool<ArrayList<WireConnection>>();
	  
	  //Intialize the HashMap
	  for(Tile T_Array[]:device.getTiles()){
		 for(Tile T:T_Array){
			 TileToReverseArcsMap.put(T, new HashMap<Integer,ArrayList<WireConnection>>());
		 }
	  }
	  
	  long startTime = System.currentTimeMillis();
	  //Populate the HashMap
	  int num_back_arcs = 0; 
	  for(Tile T_Array[]:device.getTiles()){
			 for(Tile T:T_Array){
				 
				if(T.getWireHashMap() !=null){ 
					Set<Integer> wires = T.getWires(); 	
				 for(Integer wire : wires){
					 WireConnection [] Connections = T.getWireConnections(wire);
					 for(WireConnection WC : Connections){
						 //If the Connection goes to a different Tile
						 if(!WC.getTile(T).equals(T)){
						    //If this wire is not in the has map add it along with an array list of wire connections
							if(TileToReverseArcsMap.get(WC.getTile(T)).get(WC.getWire()) == null){
								ArrayList<WireConnection> newArrayList = new ArrayList<WireConnection>();
								TileToReverseArcsMap.get(WC.getTile(T)).put(WC.getWire(), newArrayList);								
							}
							 
							WireConnection newWC = reverseAndCreateNewWireConnection(WC, wire, T);
							WireConnection w;
							
							//See if an identical wire connection exists already, if it does use the wire connection that already exists
							if((UniqueBackArcs.contains(newWC))){
								WireConnection UniqueWC = UniqueBackArcs.find(newWC);
								TileToReverseArcsMap.get(WC.getTile(T)).get(WC.getWire()).add(UniqueWC);
							} else {
								UniqueBackArcs.add(newWC);
								TileToReverseArcsMap.get(WC.getTile(T)).get(WC.getWire()).add(newWC);
							}
                        
							num_back_arcs++;		 		
						 }
					 }
					
				 }
				}
			 }
	  }
	  
	//Rebuild the BackArcs Hashmap by matching the Arrays
		 for(Tile T: TileToReverseArcsMap.keySet()){
			 HashMap<Integer, ArrayList<WireConnection>> TileBackArcs = TileToReverseArcsMap.get(T);
			 for(Integer wire : TileBackArcs.keySet()){
				 ArrayList<WireConnection> OrignalAL = TileBackArcs.get(wire);
			     if(UniqueBackArcArrays.contains(OrignalAL)){
			    	 ArrayList<WireConnection> UniqueArray = UniqueBackArcArrays.find(OrignalAL);
			    	 TileToReverseArcsMap.get(T).put(wire, UniqueArray); 
			     } else {
			    	 UniqueBackArcArrays.add(OrignalAL);
			     }
			 }			 
		 }
		 
		 long endTime = System.currentTimeMillis();
		 System.out.println("Number of Back ARCs created " + num_back_arcs);
		 System.out.println("Time elasped " + (endTime-startTime) + "ms");
		 System.out.println("Validating...");
		 int numValidated = 0; 
		 //Loop through all back arcs and make sure they point to a real 
		 int numIterated = 0; 
		 for(Tile T: TileToReverseArcsMap.keySet()){
			 HashMap<Integer, ArrayList<WireConnection>> TileBackArcs = TileToReverseArcsMap.get(T);
			 for(Integer wire : TileBackArcs.keySet()){
				 ArrayList<WireConnection> TileBackArcsArray = TileBackArcs.get(wire);
				 for(WireConnection wc : TileBackArcsArray){
							 
					 Tile begTile = wc.getTile(T);
					 Integer begWire = wc.getWire();
					 numIterated++;
					 if(begTile.getWireConnections(begWire) != null){
					     //Find Original Connection 
						 boolean foundConnection = false;
						 for(WireConnection ww : begTile.getWireConnections(begWire)){
							if(ww.getWire() == wire && ww.getTile(begTile).equals(T)){
								foundConnection = true;
								numValidated++;
							}
						 }
						 
						 if(!foundConnection){
							 System.out.println("Did not verify connection backArc for Tile " + T.toString() + " and wire connection " + wc.toString(we) + " and beg Tile " + begTile.toString() + " and begwire " +  we.getWireName(begWire) + " is invalid!");
			
						     System.exit(1);
						 }
					 } else {
						 System.out.println("Original Connection Does not exist! BackArc for Tile " + T.toString() + " and wire connection " + wc.toString(we)  + " and beg Tile " + begTile.toString() + " and begwire " +  we.getWireName(begWire) + " is invalid!");
						 System.exit(1);
					 }
				   }
				 }
		 }
		 System.out.println("Number of Back Arcs iterated... " + numIterated);
		 System.out.println("Validated " + numValidated + " back Arcs!");
		  
   }
   
	private static WireConnection reverseAndCreateNewWireConnection(
		WireConnection wC, Integer wire, Tile currTile) {
			return new WireConnection(wire,-1*wC.getRowOffset(), -1*wC.getColumnOffset(),wC.isPIP());
	}			
				
   public ArrayList<WireConnection> getBackArc(Tile T, Integer wire){
	   return TileToReverseArcsMap.get(T).get(wire);
   }
   
   public HashMap<Tile, HashMap<Integer,ArrayList<WireConnection>>> getBackArcsMap(){
	   return TileToReverseArcsMap; 
   }
   
   public static void main(String [] args){
	   
	   String deviceFileName = "Z:\\javaworkspace2\\rapidSmith\\devices\\virtex4\\xc4vfx60ff1152_db.dat "; 
	   
	   Device device = new Device(); 
	   WireEnumerator we = FileTools.loadWireEnumerator(FamilyType.VIRTEX4);
	   device.readDeviceFromCompactFile(deviceFileName);
	   
	   InterTileReverseArcs ReverseArcsDB = new InterTileReverseArcs(device, we);
	   
	   int totalInterTileConnections = 0; 
	   for(Tile [] Rows : device.getTiles()){
		   for(Tile T : Rows){
			   for(Integer wire : T.getWires()){
				   for(WireConnection wc : T.getWireConnections(wire)){
					   if(!T.equals(wc.getTile(T))){
						   totalInterTileConnections++; 
						   boolean foundConnection = false; 
						   for(WireConnection rev_rc: ReverseArcsDB.getBackArc(wc.getTile(T), wc.getWire())){
							   if(isReverseConnection(wire, wc, T, wc.getWire(), rev_rc, wc.getTile(T))){
								  foundConnection=true;
								  break; 
							   }
						   }
						   
						   if(!foundConnection){
							   System.out.println("Did not find the reverse connection in ReverseArcDB!! " + wc.toString(we)); 
						   }
					   }
				   }
			   }
		   }
	   }
	  
   }

private static boolean isReverseConnection(Integer startwire,     WireConnection wc,     Tile wc_start, 
		                                   Integer rev_startwire, WireConnection rev_wc, Tile rev_start) {
	
	Tile wc_end  = wc.getTile(wc_start);
	Tile rev_end = rev_wc.getTile(rev_start);
	
	if(wc_end.equals(rev_start) && rev_end.equals(wc_start) && rev_wc.getWire() == startwire && wc.getWire() == rev_startwire && wc.isPIP() == rev_wc.isPIP()){
		return true;
	}
	
	return false;
}
}

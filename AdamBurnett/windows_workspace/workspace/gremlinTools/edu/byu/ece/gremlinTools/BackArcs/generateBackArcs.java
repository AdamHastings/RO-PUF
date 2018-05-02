package edu.byu.ece.gremlinTools.BackArcs;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

import edu.byu.ece.rapidSmith.device.Device;
import edu.byu.ece.rapidSmith.device.Tile;
import edu.byu.ece.rapidSmith.device.WireConnection;
import edu.byu.ece.rapidSmith.device.WireEnumerator;
import edu.byu.ece.rapidSmith.device.WireType;
import edu.byu.ece.rapidSmith.device.helper.HashPool;
import edu.byu.ece.rapidSmith.device.helper.WireArray;
import edu.byu.ece.rapidSmith.util.FamilyType;
import edu.byu.ece.rapidSmith.util.FileTools;

class generateBackArcs {
     
	 public static void main(String [] args){
		 
		 String deviceFileName = args[0]; 
		 String outputFilename   = args[1];
		 
		 System.out.println("Loading device " + deviceFileName + "... ");
		 Device device = new Device(); 
		 WireEnumerator we = FileTools.loadWireEnumerator(FamilyType.VIRTEX4);
         
		 long startTime = System.currentTimeMillis();
		 device.readDeviceFromCompactFile(deviceFileName);
		 
		 
		 HashMap<Tile, HashMap<Integer,ArrayList<WireConnection>>> BackArcs = new HashMap<Tile, HashMap<Integer, ArrayList<WireConnection>>>();

		 HashPool<WireConnection> UniqueBackArcs = new HashPool<WireConnection>();
		 HashPool<ArrayList<WireConnection>> UniqueBackArcArrays = new HashPool<ArrayList<WireConnection>>();
		 
		 //HashSet<WireConnection> MasterWireConnectionSet = new HashSet<WireConnection>(); 
		 //ArrayList<WireConnection> MasterWireConnectionList = new ArrayList<WireConnection>();
		 //ArrayList<ArrayList<WireConnection>> MasterArrayListList = new ArrayList<ArrayList<WireConnection>>();
		 //Create ArrayLists
		 for(Tile T_Array[]:device.getTiles()){
			 for(Tile T:T_Array){
				 BackArcs.put(T, new HashMap<Integer,ArrayList<WireConnection>>());
			 }
		 }
		 
		 int num_back_arcs =0;
		 int replacements =0;
		 String DEBUGWIRE = "LV24";
		 int num_debugwire = 0; 
		 int num_debugwireReplacements =0 ;
		 //Iterate Through Tiles and Find all back arcs
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
							if(BackArcs.get(WC.getTile(T)).get(WC.getWire()) == null){
								ArrayList<WireConnection> newArrayList = new ArrayList<WireConnection>();
								BackArcs.get(WC.getTile(T)).put(WC.getWire(), newArrayList);								
							}
							 
							WireConnection newWC = reverseAndCreateNewWireConnection(WC, wire, T);
							WireConnection w;
							if(newWC==null){
								System.out.println("Null New Connection");
							}
							//See if an identical wire connection exists already, if it does use the wire connection that already exists
							if((UniqueBackArcs.contains(newWC))){
								WireConnection UniqueWC = UniqueBackArcs.find(newWC);
								BackArcs.get(WC.getTile(T)).get(WC.getWire()).add(UniqueWC);
							} else {
								UniqueBackArcs.add(newWC);
								BackArcs.get(WC.getTile(T)).get(WC.getWire()).add(newWC);
							}
                           
							num_back_arcs++;		 		
						 }
					 }
					
				 }
				 
				}
			 }
		 }
		 
		 System.out.println("Matching Lists");
		 	 
		 //Rebuild the BackArcs Hashmap by matching the Arrays
		 for(Tile T: BackArcs.keySet()){
			 HashMap<Integer, ArrayList<WireConnection>> TileBackArcs = BackArcs.get(T);
			 for(Integer wire : TileBackArcs.keySet()){
				 ArrayList<WireConnection> OrignalAL = TileBackArcs.get(wire);
			     if(UniqueBackArcArrays.contains(OrignalAL)){
			    	 ArrayList<WireConnection> UniqueArray = UniqueBackArcArrays.find(OrignalAL);
			    	 BackArcs.get(T).put(wire, UniqueArray); 
			     } else {
			    	 UniqueBackArcArrays.add(OrignalAL);
			     }
			 }			 
		 }
		 long endTime = System.currentTimeMillis();
	      
		 System.out.println("Number of Back ARCs created " + num_back_arcs + " with " + replacements + " replacemetns");
		 System.out.println("Time elasped " + (endTime-startTime) + "ms");
		 System.out.println("number of debug wires "+ num_debugwire + " number of replacements " + num_debugwireReplacements);
		 System.out.println("Validating...");
		 int numValidated = 0; 
		 //Loop through all back arcs and make sure they point to a real 
		 int numIterated = 0; 
		 for(Tile T: BackArcs.keySet()){
			 HashMap<Integer, ArrayList<WireConnection>> TileBackArcs = BackArcs.get(T);
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
		 
		 
		 try{
			 ObjectOutput out = new ObjectOutputStream(new FileOutputStream(outputFilename));
			 out.writeObject(BackArcs);
			 out.close(); 
		 } catch (IOException e) {
		 }

		 
	 }
    
	private static boolean OriginalArrayList(ArrayList<WireConnection> wCA,
			ArrayList<ArrayList<WireConnection>> masterMasterArrayListList) {
		
		    for(ArrayList<WireConnection> WCAL : masterMasterArrayListList){
		    	if(ArraysEqual(WCAL, wCA)){
		    		return false; 
		    	}
		    }
		return true;
	}

	private static boolean ArraysEqual(ArrayList<WireConnection> wCAL,
			ArrayList<WireConnection> wCA) {
		
		if(wCAL.size() != wCA.size()){
			return false; 
		}
		for(WireConnection W : wCAL){
			for(WireConnection WD : wCA){
				if(!W.equals(WD)){
					return false; 
				}
			}
		}
		return true;
	}

	private static WireConnection WireConnectionListContains(ArrayList<WireConnection> WCA, WireConnection WC){ 
		for(WireConnection W: WCA){
			if(W.equals(WC)){
				return W; 
			}
		}
		return null;
	}
	 
	private static WireConnection reverseAndCreateNewWireConnection(
			WireConnection wC, Integer wire, Tile currTile) {
		return new WireConnection(wire,-1*wC.getRowOffset(), -1*wC.getColumnOffset(),wC.isPIP());
	}
}

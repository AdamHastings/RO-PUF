package edu.byu.ece.gremlinTools.Virtex4Bits;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

import edu.byu.ece.rapidSmith.design.Design;
import edu.byu.ece.rapidSmith.design.Instance;
import edu.byu.ece.rapidSmith.design.Net;
import edu.byu.ece.rapidSmith.design.NetType;
import edu.byu.ece.rapidSmith.design.PIP;
import edu.byu.ece.rapidSmith.design.Pin;
import edu.byu.ece.rapidSmith.device.Device;
import edu.byu.ece.rapidSmith.device.PrimitiveSite;
import edu.byu.ece.rapidSmith.device.PrimitiveType;
import edu.byu.ece.rapidSmith.device.Tile;
import edu.byu.ece.rapidSmith.device.TileType;
import edu.byu.ece.rapidSmith.device.WireConnection;
import edu.byu.ece.rapidSmith.device.WireEnumerator;
import edu.byu.ece.rapidSmith.router.Node;

public class Virtex4SwitchBoxRouter {
	public Virtex4SwitchBox SB; 
	public boolean BFSPerformed;
	public HashMap<Integer, JNode> SwitchBoxGraph;
	public HashMap<Integer, JNode> ReachableNodes;
	public HashMap<Tile, HashSet<Integer>> usedWires;
    public Design design; 
	public Virtex4SwitchBoxRouter(Virtex4SwitchBox SB) {
		this.SB = SB; 
		this.design = SB.getDesign(); 
		this.BFSPerformed = false; 
   	    this.SwitchBoxGraph = new HashMap<Integer, JNode>();
   	    this.ReachableNodes = new HashMap<Integer, JNode>();
   	    this.usedWires = new HashMap<Tile, HashSet<Integer>>();
   	    this.generateUsedRoutingResources();
	}
	
	
	 public void regenerateUsedRoutingResources(){
    	 this.usedWires.clear(); 
    	 this.SB.getDesignPIPs().clear(); 
    	 this.generateUsedRoutingResources();
    	 
     }
     
     
     
     private void generateUsedRoutingResources(){
    	 //System.out.println("Generating Used Resources Data Structures.");
         for(Net N : SB.getDesign().getNets()){
        	 for(PIP P : N.getPIPs()){
        		 //Load All PIPs into the BitCache
        		 //getPIPBits(P);
        		 PIP tmp = SB.getDesignPIPs().put(P, P);
        		 if(tmp != null && !P.equals(tmp) ){
        			 System.out.println("Used Design PIP in HashMap was replaced.");
        		 }
        		 
        		 Tile T = P.getTile();
        		 HashSet<Integer> wires; 
         		 
        		 if((wires = usedWires.get(T)) == null){
        			wires = new HashSet<Integer>();
        			if(usedWires.put(T, wires) != null){
        				System.out.println("Generate Used Wire Map: A tile Key Has Been replaced.");
        			}
        		 }
         		 
        		 wires.add(P.getStartWire());
        		 wires.add(P.getEndWire());
        		 
        	 }
         }
     }
     
     public void AddNodesToQueue(Queue<JNode> Q, JNode N){
 		
  		WireConnection [] Wires = N.getConnections();
 		int NodesAdded = 0;
 		int curLevel = N.getLevel() + 1;
 		
 		//System.out.println("\n Add Nodes To Queue: Adding Nodes: Level "+ curLevel);
 		//System.out.println(" Add Nodes To Queue: Current Node: "+ D.getWireEnumerator().getWireName(N.getWire()));
 		//if(N.getWire() == design.getWireEnumerator().getWireEnum("IMUX_B3")){
  		//	System.out.println("Hello");
  	    //}
	 		if(Wires != null){
	 		for(int i=0; i < Wires.length; i++){
	 			
	 			JNode NextNode = SwitchBoxGraph.get(Wires[i].getWire());
	 		   // System.out.println("Not Added Yet: " + NextNode.toString(design.getWireEnumerator()));
	 			if( NextNode != null && ((Wires[i].getColumnOffset() == 0 && Wires[i].getRowOffset() == 0) || (NextNode.getTile().getType() == TileType.CLB) || N.getTile().getType() == TileType.CLB)){
	 				if(!NextNode.getVisited() && !NextNode.isUsed()){
	 				//	System.out.println( "Add Nodes To Queue:"+ design.getWireEnumerator().getWireName(Wires[i].getWire()) + " " + curLevel);
	 				//	System.out.println("Add Nodes To Queue:"+  Wires[i].getRowOffset() + " " + Wires[i].getColumnOffset());
	 					
	 					Q.add(NextNode);
	 					NextNode.setVisited(true);
	 					NextNode.setParent((Node) N);
	 					NextNode.setLevel(curLevel);
	 					NodesAdded++; 
	 				}
	 			}
	 			
	 		} //End For Loop
	 	}	//System.out.println("Added " + NodesAdded + " at level " + curLevel + "\n");
 	}
 	
 	public void ProcessNodeEarly(JNode N){
 		//Set JNode as Visited 
 	
 		if(ReachableNodes.put(N.getWire(), N)!= null){
 			System.out.println("Process Node Early: A reachable Node Has been replaced on the HashMap.");
 		}
 	}
 	
     public void ProcessNodeLate(JNode N){
 		
 	}
     
     private void BFS(int StartWire){
  		
  		Queue<JNode> Q = new LinkedList<JNode>();
  		
  		JNode StartNode;
  		
  		if((StartNode = SwitchBoxGraph.get(StartWire)) == null){
  			System.out.println("BFS: Invalid Start Wire!!");
  			System.exit(1);
  		} else {
  	    	Q.add(StartNode);    
  		}
  	    
  		StartNode.setLevel(0);
  		StartNode.setParent(null);
  		StartNode.visited = true; 
  		while(Q.isEmpty() == false){
  			
  			JNode N = Q.peek(); 
  			ProcessNodeEarly(N); 
  			AddNodesToQueue(Q, N);
  			Q.remove(); 
  		}
  		
  		//System.gc();
  	}
     
     /*
      *  A Bridging Short is one in which one of the two Wires in the Sink PIP is a Reachable
      *  Node of the Source PIP.  
      */
     public ArrayList<PIP> CreateBridgingShort(int Source, int Sink){ 
     	 
     	boolean rsUsed = false; 
     	//Reset SwitchBox Graph
     	this.ResetBFS();
     	
     	//Set Sink as Unused
     	//System.out.println("Source : " + design.getWireEnumerator().getWireName(Source) + " Sink " + design.getWireEnumerator().getWireName(Sink));
     	JNode L = this.SwitchBoxGraph.get(Sink);
     	
     	if(L.isUsed()){
 	      L.setUsed(false);
 	      rsUsed = true; 
     	}
     	
     	//Perform BFS on Source Wire to find Reachable Nodes
     	this.BFS(Source);
     	
     	//Is Sink Reachable by Source
     	JNode N; 
     	if(( N = this.ReachableNodes.get(Sink)) == null ){
     		//System.out.println("Bridging Short Could not be Created from " + SB.getDesign().getWireEnumerator().getWireName(Source) + " to " + SB.getDesign().getWireEnumerator().getWireName(Sink));
     		return new ArrayList<PIP>(); 
     	}
     	//System.out.println("Bridging Short Created from "  + SB.getDesign().getWireEnumerator().getWireName(Source) + " to " + SB.getDesign().getWireEnumerator().getWireName(Sink));
     	//If the Wire is part of a Net Set this node as used again. 
     	if(rsUsed){
     		L.setUsed(true);
     	}
     	
     	return ExtractPIPs(N);
     }
      
     /*
      * A Distant Short is a short where the Short is not created by directly connecting the two Nets
      * but by Connecting Each Source to the same Distant Wire and Creating a Short when one source is high
      * and the other is low.
      * 
      * The Sources and Sink must all be Different Wires. The Sink must be a reachable Node.
      */
     
     public HashMap<Integer, ArrayList<PIP>> CreateDistantShort(int Source0, int Source1, int Sink){
     	
     	if(Source0 == Source1 || Source0 == Sink || Source1 == Sink){
     		System.out.println("Invalid Parameters for Distant Short: " + Source0 + " " + Source1 + " " + Sink);
     	    return null;
     	}
     	
     	HashMap<Integer, ArrayList<PIP>> Results = new HashMap<Integer, ArrayList<PIP>>();
     	
          	
     	ArrayList<PIP> Source0ToSink = CreateBridgingShort(Source0, Sink);
     	ArrayList<PIP> Source1ToSink = CreateBridgingShort(Source1, Sink);
     	
     	if(Source0ToSink == null || Source1ToSink == null){
     		System.out.println("Distant Short Could Not be Created");
     		return null;
     	}
     	
     	Results.put(Source0, Source0ToSink);
     	Results.put(Source1, Source1ToSink);
     	
     	return Results; 
     }
     private void CreateSwitchBoxGraph(){
    	   
         ArrayList<PIP> PIPs = SB.getIntTile().getPIPs();  
         PIPs.addAll(SB.getCLBTile().getPIPs());
         
         SwitchBoxGraph.clear();
         
         //Generate Node for Each PIP
    	   for(PIP P : PIPs){
    		   //Create One Node For Each PIP
    		   //System.out.println(P.getEndWireName(design.getWireEnumerator()));
    		   if(SwitchBoxGraph.containsKey(P.getStartWire()) == false){
    			   //Add a Node for this wire
    			   
    			   if((SwitchBoxGraph.put(P.getStartWire(), new JNode(P.getTile(), P.getStartWire(), null, 0))) != null){
    				   System.out.println("A Mapped Node has been replaced");
    			   }
    		   }
    		   
    		   if(SwitchBoxGraph.containsKey(P.getEndWire()) == false){
    			   if((SwitchBoxGraph.put(P.getEndWire(), new JNode(P.getTile(), P.getEndWire(), null, 0))) != null){
    				   System.out.println("A Mapped Node has been replaced");
    			   }
    		   }
    		  
    	   }
    	   
    	   //Set Nodes of Used Wires to Visited
    	   HashSet<Integer> UsedWireNodes = new HashSet<Integer>();
    	   HashSet<Integer> INTUsedWires = this.usedWires.get(SB.getIntTile());
    	   HashSet<Integer> CLBUsedWires  = this.usedWires.get(SB.getCLBTile()); 
    	   
    	   if(INTUsedWires == null){
    		   System.out.println(SB.getIntTile() + " Has No Used Routing PIPs in it.");
    	   } else {
    	       UsedWireNodes.addAll(INTUsedWires);
    	   }
    	   
    	   if(CLBUsedWires == null){
    		   System.out.println(SB.getCLBTile() + " Has No Used Routing PIPs in it.");
    	   } else {
    	       UsedWireNodes.addAll(CLBUsedWires);
    	   }
    	   
    	   if(UsedWireNodes != null){
	    	   for(Integer I : UsedWireNodes){
	    		  if(I>=0){
	    		   JNode N = this.SwitchBoxGraph.get(I);
	    		   N.setUsed(true);
	    		  }
	    	   }
    	   }
       }
     public HashMap<Integer, JNode> getGraph(){
    	 return this.SwitchBoxGraph; 
     }
     
 public ArrayList<PIP> getPathPIPs(int startWire, int endWire){
    	 
    	 JNode B;
    	 //Perform a search for all Reachable Nodes
    	 if(this.BFSPerformed == false){
    	   BFS(startWire);
    	   this.BFSPerformed = true; 
    	 }
    	 
    	 //Is this Node Reachable??
    	 if((B = ReachableNodes.get(endWire)) == null){
    		 System.out.println("End Wire Cannot be Reached.");
    		 return null; 
    	 }
    	 
    	 return ExtractPIPs(B); 
     }
          
     private ArrayList<PIP> ExtractPIPs(JNode EndNode){
    	
    	ArrayList<PIP> Pips = new ArrayList<PIP>(); 
    	if(EndNode!=null){ 
    	 JNode B, A;
               
    	 B = EndNode; 
		 while(B.getLevel() != 0){
			 A = (JNode) B.getParent();
			 if(B.getTile().equals(A.getTile())){
			  // System.out.println("A Wire: " + design.getWireEnumerator().getWireName((A.getWire())) + " B Wire: " + design.getWireEnumerator().getWireName(B.getWire()));
			  Tile T;
			  if(design.getWireEnumerator().getWireName(A.getWire()).contains("PINWIRE") || design.getWireEnumerator().getWireName(B.getWire()).contains("PINWIRE"))
			     T = this.SB.getCLBTile();
			  else
				 T = this.SB.getIntTile();
			  
			  Pips.add(new PIP(T, A.getWire(), B.getWire()));
			 }
			 B = A; 
			// System.out.println(Pips.size());
			 
		 }
		 
		 return Pips;
    	}
    	else{
    		System.out.println("Sent Extract Nodes a Null Node?");
    		System.exit(1);
    	}
    	return Pips;
     }
     
     
     public HashMap<Integer, JNode> getReachableNodes(){
    	 return this.ReachableNodes; 
     }
    
     //This Function Sets the Tile and Creates a new Switchbox Graph For the SwitchBox
     public void setTiles(Tile IntTile, Tile CLBTile){
    	 
    	 this.SB.setTiles(IntTile, CLBTile); 
    	 this.BFSPerformed = false; 
    	 this.regenerateUsedRoutingResources();
    	 CreateSwitchBoxGraph();
     }
     
     public void searchReachableNodes(int startWire){
    	 if(BFSPerformed == true)
    	   this.ResetBFS();
    	 
    	 this.BFS(startWire);
     }
     
     public void ResetBFS(){
     
    	for(Integer I : SwitchBoxGraph.keySet()){
    	   JNode N = SwitchBoxGraph.get(I);
    	   N.setVisited(false);
    	   N.setParent(null);
        }
        
    	this.BFSPerformed = false;
    	this.ReachableNodes.clear();
     }
     
     public ArrayList<PIP> LongBFS(int StartWire, int EndWire, Tile StartTile, Device device){
    	 Queue<JNode> Q = new LinkedList<JNode>();
   		
   		JNode StartNode = new JNode(StartTile, StartWire, null, 0);
   			
   	    Q.add(StartNode);    
   		
   	    
   		while(Q.isEmpty() == false){
   			
   			JNode N = Q.peek(); 
   			
   			if(N.getWire() == EndWire){
   				return ExtractPIPs(N);
   			}
   			AddNodesToQueueLong(Q, N, device);
   			Q.remove(); 
   		}
   		
   		return null;
     }
     
  

	public void AddNodesToQueueLong(Queue<JNode> Q, JNode N, Device device){
  		
   		WireConnection [] Wires = N.getConnections();
  		int NodesAdded = 0;
  		int curLevel = N.getLevel() + 1;
  		
  		//System.out.println("\n Add Nodes To Queue: Adding Nodes: Level "+ curLevel);
  		//System.out.println(" Add Nodes To Queue: Current Node: "+ D.getWireEnumerator().getWireName(N.getWire()));
 
  		
  		if(Wires != null){
	  		for(int i=0; i < Wires.length; i++){
	  			
	  			int wire = Wires[i].getWire();
	  		
	  			Tile T = Wires[i].getTile( N.getTile());
	  			JNode NextNode =  new JNode(T, Wires[i].getWire(), null, 0);
	  		  		
	  			if(NextNode != null && (T.getType() == TileType.CLB || T.getType() == TileType.INT )){
	  				if(!NextNode.getVisited() && !NextNode.isUsed()){
	  					//System.out.println( "Add Nodes To Queue:"+ design.getWireEnumerator().getWireName(Wires[i].getWire()));
	  					//ystem.out.println("Add Nodes To Queue:"+  Wires[i].getRowOffset() + " " + Wires[i].getColumnOffset());
	  					
	  					Q.add(NextNode);
	  					NextNode.setVisited(true);
	  					NextNode.setParent((Node) N);
	  					NextNode.setLevel(curLevel);
	  					NodesAdded++; 
	  					  					
	  				}
	  			}
	  			
	  		} //End For Loop
  		}
  		//System.out.println("Added " + NodesAdded + " at level " + curLevel + "\n");
  	}
	
	public void printReachableNodes(){
		
		for(Integer I : ReachableNodes.keySet()){
			JNode J = ReachableNodes.get(I);
			System.out.println(J.toString(design.getWireEnumerator()));
		}
	}
	
	public Net GroundLutInputs(Tile IntTile, Design design, Instance SliceX0Y0,  Instance SliceX0Y1, Instance SliceX1Y0, Instance SliceX1Y1){
		
		Net GroundNet = new Net("Ground" + IntTile, NetType.GND);
		PrimitiveSite TieOff = IntTile.getPrimitiveSites()[0];
		Instance TieOffInst = new Instance("TieOff" + IntTile, PrimitiveType.TIEOFF); 
		TieOffInst.place(TieOff);
		Pin StartPin = new Pin(true, "HARD0", TieOffInst);
		design.addInstance(TieOffInst);
		GroundNet.addPin(StartPin);
		
		GroundNet.addPin(new Pin(false, "F1", SliceX0Y0));
		GroundNet.addPin(new Pin(false, "F2", SliceX0Y0));
		GroundNet.addPin(new Pin(false, "F3", SliceX0Y0));
		GroundNet.addPin(new Pin(false, "F4", SliceX0Y0));
		GroundNet.addPin(new Pin(false, "G1", SliceX0Y0));
		GroundNet.addPin(new Pin(false, "G2", SliceX0Y0));
		GroundNet.addPin(new Pin(false, "G3", SliceX0Y0));
		GroundNet.addPin(new Pin(false, "G4", SliceX0Y0));
		
		GroundNet.addPin(new Pin(false, "F1", SliceX0Y1));
		GroundNet.addPin(new Pin(false, "F2", SliceX0Y1));
		GroundNet.addPin(new Pin(false, "F3", SliceX0Y1));
		GroundNet.addPin(new Pin(false, "F4", SliceX0Y1));
		GroundNet.addPin(new Pin(false, "G1", SliceX0Y1));
		GroundNet.addPin(new Pin(false, "G2", SliceX0Y1));
		GroundNet.addPin(new Pin(false, "G3", SliceX0Y1));
		GroundNet.addPin(new Pin(false, "G4", SliceX0Y1));
		
		GroundNet.addPin(new Pin(false, "F1", SliceX1Y0));
		GroundNet.addPin(new Pin(false, "F2", SliceX1Y0));
		GroundNet.addPin(new Pin(false, "F3", SliceX1Y0));
		GroundNet.addPin(new Pin(false, "F4", SliceX1Y0));
		GroundNet.addPin(new Pin(false, "G1", SliceX1Y0));
		GroundNet.addPin(new Pin(false, "G2", SliceX1Y0));
		GroundNet.addPin(new Pin(false, "G3", SliceX1Y0));
		GroundNet.addPin(new Pin(false, "G4", SliceX1Y0));
		
		GroundNet.addPin(new Pin(false, "F1", SliceX1Y1));
		GroundNet.addPin(new Pin(false, "F2", SliceX1Y1));
		GroundNet.addPin(new Pin(false, "F3", SliceX1Y1));
		GroundNet.addPin(new Pin(false, "F4", SliceX1Y1));
		GroundNet.addPin(new Pin(false, "G1", SliceX1Y1));
		GroundNet.addPin(new Pin(false, "G2", SliceX1Y1));
		GroundNet.addPin(new Pin(false, "G3", SliceX1Y1));
		GroundNet.addPin(new Pin(false, "G4", SliceX1Y1));
		
		WireEnumerator We = design.getWireEnumerator();
		
		GroundNet.getPIPs().addAll(CreateBridgingShort(We.getWireEnum("GND_WIRE"), We.getWireEnum("BOUNCE0")));
		GroundNet.getPIPs().addAll(CreateBridgingShort(We.getWireEnum("GND_WIRE"), We.getWireEnum("BOUNCE1")));
		GroundNet.getPIPs().addAll(CreateBridgingShort(We.getWireEnum("GND_WIRE"), We.getWireEnum("BOUNCE2")));
		GroundNet.getPIPs().addAll(CreateBridgingShort(We.getWireEnum("GND_WIRE"), We.getWireEnum("BOUNCE3")));
		
		GroundNet.getPIPs().addAll(this.CreateBridgingShort(We.getWireEnum("BOUNCE3"), We.getWireEnum("G1_PINWIRE0")));
		GroundNet.getPIPs().addAll(this.CreateBridgingShort(We.getWireEnum("BOUNCE2"), We.getWireEnum("G2_PINWIRE0")));
		GroundNet.getPIPs().addAll(this.CreateBridgingShort(We.getWireEnum("BOUNCE1"), We.getWireEnum("G3_PINWIRE0")));
		GroundNet.getPIPs().addAll(this.CreateBridgingShort(We.getWireEnum("BOUNCE0"), We.getWireEnum("G4_PINWIRE0")));
		
		GroundNet.getPIPs().addAll(this.CreateBridgingShort(We.getWireEnum("BOUNCE3"), We.getWireEnum("F1_PINWIRE0")));
		GroundNet.getPIPs().addAll(this.CreateBridgingShort(We.getWireEnum("BOUNCE2"), We.getWireEnum("F2_PINWIRE0")));
		GroundNet.getPIPs().addAll(this.CreateBridgingShort(We.getWireEnum("BOUNCE1"), We.getWireEnum("F3_PINWIRE0")));
		GroundNet.getPIPs().addAll(this.CreateBridgingShort(We.getWireEnum("BOUNCE0"), We.getWireEnum("F4_PINWIRE0")));
		
		GroundNet.getPIPs().addAll(this.CreateBridgingShort(We.getWireEnum("BOUNCE3"), We.getWireEnum("G1_PINWIRE1")));
		GroundNet.getPIPs().addAll(this.CreateBridgingShort(We.getWireEnum("BOUNCE2"), We.getWireEnum("G2_PINWIRE1")));
		GroundNet.getPIPs().addAll(this.CreateBridgingShort(We.getWireEnum("BOUNCE1"), We.getWireEnum("G3_PINWIRE1")));
		GroundNet.getPIPs().addAll(this.CreateBridgingShort(We.getWireEnum("BOUNCE0"), We.getWireEnum("G4_PINWIRE1")));
		 
		GroundNet.getPIPs().addAll(this.CreateBridgingShort(We.getWireEnum("BOUNCE3"), We.getWireEnum("F1_PINWIRE1")));
		GroundNet.getPIPs().addAll(this.CreateBridgingShort(We.getWireEnum("BOUNCE2"), We.getWireEnum("F2_PINWIRE1")));
		GroundNet.getPIPs().addAll(this.CreateBridgingShort(We.getWireEnum("BOUNCE1"), We.getWireEnum("F3_PINWIRE1")));
		GroundNet.getPIPs().addAll(this.CreateBridgingShort(We.getWireEnum("BOUNCE0"), We.getWireEnum("F4_PINWIRE1")));
		
		GroundNet.getPIPs().addAll(this.CreateBridgingShort(We.getWireEnum("BOUNCE0"), We.getWireEnum("G1_PINWIRE2")));
		GroundNet.getPIPs().addAll(this.CreateBridgingShort(We.getWireEnum("BOUNCE1"), We.getWireEnum("G2_PINWIRE2")));
		GroundNet.getPIPs().addAll(this.CreateBridgingShort(We.getWireEnum("BOUNCE2"), We.getWireEnum("G3_PINWIRE2")));
		GroundNet.getPIPs().addAll(this.CreateBridgingShort(We.getWireEnum("BOUNCE3"), We.getWireEnum("G4_PINWIRE2")));
		
		GroundNet.getPIPs().addAll(this.CreateBridgingShort(We.getWireEnum("BOUNCE0"), We.getWireEnum("F1_PINWIRE2")));
		GroundNet.getPIPs().addAll(this.CreateBridgingShort(We.getWireEnum("BOUNCE1"), We.getWireEnum("F2_PINWIRE2")));
		GroundNet.getPIPs().addAll(this.CreateBridgingShort(We.getWireEnum("BOUNCE2"), We.getWireEnum("F3_PINWIRE2")));
		GroundNet.getPIPs().addAll(this.CreateBridgingShort(We.getWireEnum("BOUNCE3"), We.getWireEnum("F4_PINWIRE2")));
		
		GroundNet.getPIPs().addAll(this.CreateBridgingShort(We.getWireEnum("BOUNCE0"), We.getWireEnum("G1_PINWIRE3")));
		GroundNet.getPIPs().addAll(this.CreateBridgingShort(We.getWireEnum("BOUNCE1"), We.getWireEnum("G2_PINWIRE3")));
		GroundNet.getPIPs().addAll(this.CreateBridgingShort(We.getWireEnum("BOUNCE2"), We.getWireEnum("G3_PINWIRE3")));
		GroundNet.getPIPs().addAll(this.CreateBridgingShort(We.getWireEnum("BOUNCE3"), We.getWireEnum("G4_PINWIRE3")));
		
		GroundNet.getPIPs().addAll(this.CreateBridgingShort(We.getWireEnum("BOUNCE0"), We.getWireEnum("F1_PINWIRE3")));		
		GroundNet.getPIPs().addAll(this.CreateBridgingShort(We.getWireEnum("BOUNCE1"), We.getWireEnum("F2_PINWIRE3")));
		GroundNet.getPIPs().addAll(this.CreateBridgingShort(We.getWireEnum("BOUNCE2"), We.getWireEnum("F3_PINWIRE3")));
		GroundNet.getPIPs().addAll(this.CreateBridgingShort(We.getWireEnum("BOUNCE3"), We.getWireEnum("F4_PINWIRE3")));
						
		return GroundNet;
	}
}
package XDLDesign.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import edu.byu.ece.gremlinTools.xilinxToolbox.V4XilinxToolbox;
import edu.byu.ece.gremlinTools.xilinxToolbox.XilinxToolboxLookup;
import edu.byu.ece.rapidSmith.bitstreamTools.bitstream.Bitstream;
import edu.byu.ece.rapidSmith.bitstreamTools.bitstream.BitstreamHeader;
import edu.byu.ece.rapidSmith.bitstreamTools.bitstream.BitstreamParseException;
import edu.byu.ece.rapidSmith.bitstreamTools.bitstream.BitstreamParser;
import edu.byu.ece.rapidSmith.bitstreamTools.configuration.FPGA;
import edu.byu.ece.rapidSmith.bitstreamTools.configurationSpecification.DeviceLookup;
import edu.byu.ece.rapidSmith.design.Attribute;
import edu.byu.ece.rapidSmith.design.Design;
import edu.byu.ece.rapidSmith.design.Instance;
import edu.byu.ece.rapidSmith.design.Module;
import edu.byu.ece.rapidSmith.design.ModuleInstance;
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
import edu.byu.ece.rapidSmith.device.helper.WireArray;
import edu.byu.ece.rapidSmith.router.Node;
import edu.byu.ece.rapidSmith.util.MessageGenerator;

public class XDLDesignUtils {
	
	public static Tile getMatchingIntTile(Tile CLBTile, Device device){
	   if(CLBTile.getType() == TileType.CLB){	
		return device.getTile(CLBTile.getRow(), CLBTile.getColumn() - 1);
	   } else {
		   System.out.println("getMatchingIntTile: Invalid Tile Type " + CLBTile.getType());
		   return null;
	   }
	}
	
	public static Tile getMatchingCLBTile(Tile IntTile, Device device){
		 if(IntTile.getType() == TileType.INT){	
			return device.getTile(IntTile.getRow(), IntTile.getColumn() + 1);
		 } else {
			 System.out.println("getMatchingCLBTile: Invalid Tile Type " + IntTile.getType());
			 return null;  
		 }
	}
	
	public static ArrayList<Tile> getAllINTTiles(Device device){
		ArrayList<Tile> INTTiles = new ArrayList<Tile>();
		
		int rows = device.getRows();
		int cols = device.getColumns(); 
		
		Tile [][] Tiles = device.getTiles();
		
		for(int i=0; i<rows; i++){
			for(int j=0; j<cols; j++){
				Tile T = Tiles[i][j];
				if(T.getType() == TileType.INT){
					INTTiles.add(T);
				}
			}
		}
		return INTTiles; 
	}
	
	public static ArrayList<Tile> getAllCLBTiles(Device device){
		ArrayList<Tile> CLBTiles = new ArrayList<Tile>();
		
		int rows = device.getRows();
		int cols = device.getColumns(); 
		
		Tile [][] Tiles = device.getTiles();
		
		for(int i=0; i<rows; i++){
			for(int j=0; j<cols; j++){
				Tile T = Tiles[i][j];
				if(T.getType() == TileType.CLB){
					CLBTiles.add(T);
				}
			}
		}
		
		//System.out.println("CLBTiles: " + CLBTiles.size());
		
		return CLBTiles; 
	}
	
	//Place HardMacros On all CLB Sites
	public static void PlaceHardMacrosOnAllCLBTiles(Design D, Module HardMacro){
		
		Device dev = D.getDevice(); 
		Tile [][] Tiles = dev.getTiles(); 
		
		int HardMacroIndex = 0; 	
			
		for(int i=0; i < dev.getRows(); i++){
			for(int j=0; j< dev.getColumns(); j++){
				
				String name = Tiles[i][j].getName();
			    
				PrimitiveSite [] Sites = Tiles[i][j].getPrimitiveSites();
				
				if(name.startsWith("CLB") && Sites != null && Sites.length >= 4 && Sites[3].getName().startsWith("SLICE")){
					ModuleInstance MI = D.createModuleInstance("HardMacro" + HardMacroIndex, HardMacro);
					MI.place(Sites[3], dev);
					HardMacroIndex++;
				}
				
				
			}
		}
		System.out.println("\tPlaced " + HardMacroIndex + " Hard Macros.");
	
	}
	
	//D:       is the RapidSmith Design that the IOB will be added to. 
	//LocSite: is the FPGA Pin that the IOB will be added to.
	//isInput: Answers the Question: This IOB will be used as an Input. 
	//InstName:Name the XDL Instance will be given. 
	//NetName : The Name of the Net Connected to this Instance.
	// Most of this Function was copied from Marc. 
	
	public static void CreateBUFG(Design design, String LocSite, String InstanceName){
		
		Device device = design.getDevice(); 
		PrimitiveSite bufgSite = device.getPrimitiveSite(LocSite);
		
		Instance I = new Instance(InstanceName, PrimitiveType.BUFG);
		I.place(bufgSite);
		I.addAttribute(new Attribute("GCLK_BUFFER", InstanceName, ""));
		design.addInstance(I);
	}
	
	public static void CreateClockInput(String IOBLocSite, String BUFGLocSite, Design design){
		
		String IOB_INSTANCE_NAME = "CLK_IOB";
		String CLK_IBUF_NET_NAME = "CLK_IBUF_NET";
		String BUFG_INSTANCE_NAME = "BUFG_INST";
		String CLK_NET_NAME =  "CLK_NET";
		
		CreateIOB(design, IOBLocSite, true, IOB_INSTANCE_NAME, CLK_IBUF_NET_NAME);
		CreateBUFG(design, BUFGLocSite, BUFG_INSTANCE_NAME);
		
		Instance BUFG_INST = design.getInstance(BUFG_INSTANCE_NAME);
				
		Net CLK_IBUF_NET = design.getNet(CLK_IBUF_NET_NAME);
		CLK_IBUF_NET.addPin(new Pin(false, "I0", BUFG_INST));
		
		Net CLK_NET = new Net(CLK_NET_NAME, NetType.WIRE);
		CLK_NET.addPin(new Pin(true, "O", BUFG_INST)); 
		design.addNet(CLK_NET);
	}
	
	public static void CreateIOB(Design D, String LocSite, boolean isInput, String InstName, String NetName){
		   
		   Device dev = D.getDevice(); 
		   
	  	   PrimitiveSite iobSite = dev.getPrimitiveSite(LocSite);
							
		   Net iobNet = new Net(NetName, NetType.WIRE);
			
			
			Instance iobInst = new Instance(InstName, PrimitiveType.IOB);
			
			iobInst.addAttribute("DIFFI_INUSED", "", "#OFF");
			iobInst.addAttribute("DIFF_TERM", "", "#OFF");
			iobInst.addAttribute("DRIVEATTRBOX", "", (isInput)? "#OFF" : "12");
			iobInst.addAttribute("DRIVE_0MA", "", "#OFF");
			iobInst.addAttribute("GTSATTRBOX", "", "#OFF");
			iobInst.addAttribute("INBUFUSED", "", (isInput)? "0" : "#OFF");
			iobInst.addAttribute("IOATTRBOX", "", "LVCMOS25");
			iobInst.addAttribute("OUSED", "", (isInput)? "#OFF" : "0");
			iobInst.addAttribute("PADOUTUSED", "", "#OFF");
			iobInst.addAttribute("PULL", "", "#OFF");
			iobInst.addAttribute("SLEW", "", (isInput)? "#OFF" : "SLOW");
			iobInst.addAttribute("TUSED", "", "#OFF");
			iobInst.addAttribute((isInput)? "INBUF" : "OUTBUF",NetName , "");
			iobInst.addAttribute("PAD",InstName , "");
			
			Pin iobPin = new Pin(isInput, (isInput)? "I" : "O", iobInst);
			iobNet.addPin(iobPin);
			iobInst.addToNetList(iobNet);
			
			Net belsigNet = new Net(InstName, NetType.WIRE);
			belsigNet.addAttribute("_BELSIG", "PAD,PAD,"+InstName, InstName);
			
			D.addInstance(iobInst);
			iobInst.place(iobSite);
			D.addNet(iobNet);
			D.addNet(belsigNet);
			
			D.addAttribute("_DESIGN_PROP", "", "PIN_INFO:"+InstName+":/"+D.getName()+"/PACKED/"+D.getName()+"/"+InstName+"/"+InstName+"/PAD:"+ ((isInput) ? "INPUT":"Output")+":"+ "0" + ":"+"B"+"("+"0"+"\\:0)");
		
		
		//D.addAttribute("_DESIGN_PROP", "", "BUS_INFO:"+"1"+":"+"OUTPUT"+":"+blkName+"("+"0"+":0)");
		
		
	}
	
	public static void loadBitstream(FPGA fpga, String Filename){
		try {
			fpga.configureBitstream(BitstreamParser.parseBitstream(Filename));
		} catch (BitstreamParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
	}
	
	//EffortLevel (high|std)
	public static boolean RunXilinxRouter(String InputFilename, String OutputFilename, String EffortLevel){
		
		String command = "par -w -p -rl " + EffortLevel + " " + InputFilename + " " + OutputFilename; 
		
		try {
			Process P = Runtime.getRuntime().exec(command);
			
			if(P.waitFor() != 0){
				MessageGenerator.briefError("Par execution Failed.");
				return false;
			}
			if(P.exitValue() != 0){
				MessageGenerator.briefError("Par did not Exit Correctly.");
				return false; 
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return false;
		}
		return true; 
	}
	

	public static boolean RunXilinxBitgen(String InputFilename, String OutputFilename, String Opts){
		
		String command = "bitgen " + Opts + " " + InputFilename + " " + OutputFilename; 
		
		try {
			Process P = Runtime.getRuntime().exec(command);
			
			if(P.waitFor() != 0){
				MessageGenerator.briefError("Bitgen execution Failed.");
				return false;
			}
			if(P.exitValue() != 0){
				MessageGenerator.briefError("Bitgen did not Exit Correctly.");
				return false; 
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return false;
		}
		return true; 
	}
	
    public static void writeBitstreamToFile(FPGA local_fpga, String PathToNewBitStream){
		
		Bitstream BS = local_fpga.getDeviceSpecification().getBitstreamGenerator().createFullBitstream(local_fpga, 
				                  new BitstreamHeader("", local_fpga.getDeviceSpecification().getDeviceName(), "", ""));
		try {
			FileOutputStream out = new FileOutputStream(new File(PathToNewBitStream));
			try {
				BS.outputBitstream(out);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
	
		}
	}
    
    public static V4XilinxToolbox getV4Toolbox(FPGA fpga){
    	return (V4XilinxToolbox) XilinxToolboxLookup.toolboxLookup(fpga.getDeviceSpecification());
    }
	
    
    public static FPGA getVirtexFPGA(String Partname){
    	return new FPGA(DeviceLookup.lookupPartV4V5V6(Partname));
    }
    
    public static void PruneInstances(Design design, int x0, int y0, int x1, int y1) {
        //This function prunes all of the instances outside the of the specified box
 		//x0, y0 the upper left  coordinates of the bounding box
 		//x1, y1 the lower right coordinates of the bounding box
 		ArrayList<Instance> RemoveList = new ArrayList<Instance>(); 
 		int i =0;
 		for(Instance I: design.getInstances()){
 			Tile T = I.getTile();
 			int x = T.getTileXCoordinate();
 			int y = T.getTileYCoordinate();
 			if(y == 74 && x == 23){
 				System.out.println("This one should be in the box.");
 			}
 			if(!(x >= x0 && x <= x1 && y <= y0 && y >= y1)){ //Not inside the box
 				RemoveList.add(I); //Avoiding a Concurrent Modification Exception
 			} else {
 				i++; 
 			}
 		}
 		System.out.println("" + i);
 		for(Instance I : RemoveList){
 			design.getInstances().remove(I);
 		}
 	}
    
    
    //This function will verify and correct the start and end wires of Bi-directional PIPs, assuming that all other PIPs are correct.
    //This function will only return false if the PIPs were not able to be corrected
    public static boolean verifyBidirectionalPIPs(Design design, boolean debug){
    	
    	ArrayList<Net> NetsToVerify = new ArrayList<Net>();
    	HashSet<PIP> VisitedPIPs = new HashSet<PIP>();
    	for(Net N: design.getNets()){
    		for(PIP P: N.getPIPs()){
    			if(isV4BidirectionalPIP(P, design.getWireEnumerator())){
    				NetsToVerify.add(N);
    				break; 
    			}
    		}
    	}
	    
    	for(Net N: NetsToVerify){
	    	WireEnumerator we = design.getWireEnumerator(); 
			
	    	ArrayList<PIP> StartPIPs = new ArrayList<PIP>();
			Pin Source = N.getSource();
			int startwire = design.getDevice().getPrimitiveExternalPin(Source);
			//PIP startpip = null; 
			//Detect Bi-Directional PIPs
	    	int bidirectionalPIPs = 0; 
			for(PIP P: N.getPIPs()){
	    		if(P.getStartWire()==startwire && Source.getTile().equals(P.getTile())){
	    			StartPIPs.add(P); 
	    		}  	
	    		if(isV4BidirectionalPIP(P, we)){
	    			bidirectionalPIPs++; 
	    		}
	    	}
			
			if(bidirectionalPIPs > 0){
				//Find and correct bi-directional PIPs using DFS
				if(StartPIPs.size() > 0){
					VisitedPIPs.clear();
					if(debug && N.getName().equals("DDR2_SDRAM/DDR2_SDRAM/mpmc_core_0/gen_v4_ddr2_phy.mpmc_phy_if_0/gen_pattern_compare[6].u_pattern_compare/rst_r1")){
						System.out.println("Found the Net!");
					}
					int CorrectedBiLines = 0;
					int PipsVisited = 0;
					
					//In BRAMs PIPs there may be more than one start pip
					for(PIP startpip: StartPIPs){
						CorrectedBiLines += XDLDesignUtils.NetDFS(startpip, N.getPIPs(), VisitedPIPs, 0, we, debug);
						PipsVisited = VisitedPIPs.size();
					}
					VisitedPIPs.clear();
					//If the PIPs have been corrected Properly the DFS should be able to traverse the PIPs without Correcting any PIPs
					int ShouldBeZero = 0; 
					for(PIP startpip: StartPIPs){
						ShouldBeZero += XDLDesignUtils.NetDFS(startpip, N.getPIPs(), VisitedPIPs, 0, we, debug);
					}
					System.out.println(N.getName());
					
					if(debug){	
						System.out.println("First Run: Total PIPs: " + N.getPIPs().size() + " PIPsTraversed: " + PipsVisited + " Corrected Bidirectional PIPs: " + CorrectedBiLines + " Total Bidirectional PIPs: " + bidirectionalPIPs );
						System.out.println("Verify Run: Total PIPs: " + N.getPIPs().size() + " PIPsTraversed: " + PipsVisited + " Corrected Bidirectional PIPs: " + ShouldBeZero + " Total Bidirectional PIPs: " + bidirectionalPIPs );
					 }
					if(PipsVisited != N.getPIPs().size() || VisitedPIPs.size() != N.getPIPs().size()){ 
						System.out.println("Verify Bi-directional PIPs: DFS did not visit correct number of nodes");
						System.exit(1);
					} else if(ShouldBeZero != 0){
						System.out.println("Verify Bi-directional PIPs: Bi-directional PIPs not corrected Properly");
						System.exit(1);
					} else if(CorrectedBiLines>bidirectionalPIPs){
						System.out.println("Verify Bi-directional PIPs: Corrected More Bidirectional PIPs than actually exist");
						System.exit(1);					
					} 
				} else {
					System.out.println("VerifyBiDirectionalPIPs: Couldnt Find Starting PIP.");
					System.exit(1);
				}
			}
	    }
		
		return true; 
    }
    
    public static boolean isV4BidirectionalWire(int wire, WireEnumerator we){
    	 HashSet<Integer> BIDIRECT = new HashSet<Integer>();
         BIDIRECT.add(we.getWireEnum("LV0"));
         BIDIRECT.add(we.getWireEnum("LV24"));
         BIDIRECT.add(we.getWireEnum("LH0"));
         BIDIRECT.add(we.getWireEnum("LH24"));
         
         if(BIDIRECT.contains(wire)){
       	  return true; 
         }
         return false; 
    }
    
    public static boolean isV4BidirectionalPIP(PIP P, WireEnumerator we){
      HashSet<Integer> BIDIRECT = new HashSet<Integer>();
      BIDIRECT.add(we.getWireEnum("LV0"));
      BIDIRECT.add(we.getWireEnum("LV24"));
      BIDIRECT.add(we.getWireEnum("LH0"));
      BIDIRECT.add(we.getWireEnum("LH24"));
      
      if(BIDIRECT.contains(P.getStartWire()) && BIDIRECT.contains(P.getEndWire())){
    	  return true; 
      }
      
      return false; 
    }
    
    //Returns number of Bidirectional PIPs Corrected
    //Assumptions: 1. Non-Bidirectional PIPs are always correct.
    //             2. Bidirectional PIPs may not be correct, if incorrect swap the start and end wires. 
    //             3. Edges exist between the EndWire of one PIP and the startwire of another.
    //             4. A intra-tile edge the EndWire of the start PIP will match the start wire of the other. 
    //             5. A inter-tile edge matches the WireConnection
    
    public static int NetDFS(PIP StartPIP, ArrayList<PIP> AllPIPs, HashSet<PIP> VisitedPIPs, int level, WireEnumerator we, boolean debug){
    	Node EndWireNode = new Node(StartPIP.getTile(), StartPIP.getEndWire());
    	WireConnection [] forwardConnections = EndWireNode.getConnections(); 
    	ArrayList<PIP> PIPsAtThisLevel = new ArrayList<PIP>();
    	int corrected = 0;
    	
    	//Gather the New Edges
    	String Tabs = "";
    	if(debug){
	    	for(int i=0; i<=level; i++){
	    		Tabs += "\t";
	    	}
	    	System.out.println(Tabs + "Starting Level: " + level + " with " + StartPIP.toString(we));
	    	System.out.println(Tabs + "Adding PIPs:");
    	}
    	
    	if(!VisitedPIPs.contains(StartPIP)){
    		VisitedPIPs.add(StartPIP);
    		ArrayList<PIP> PossiblePIPs = new ArrayList<PIP>();
    		
    		//Find all PIPs that may be the next connection
    		if(forwardConnections!=null){ //If the PIP has no wire connections then we are done
    			//HashSet<WireConnection> connections = new HashSet<WireConnection>();
    			for(WireConnection wc : forwardConnections){
	    			for(PIP P: AllPIPs){			
	        			if(!VisitedPIPs.contains(P) && !P.equals(StartPIP) && (wc.getWire() == P.getStartWire() || wc.getWire() == P.getEndWire()) && wc.getTile(StartPIP.getTile()).equals(P.getTile()) ){
	        				PossiblePIPs.add(P);
	        			}
	        		}
	    			//connections.add(wc);
				}
    		
	    		for(PIP P: PossiblePIPs){
	    			//Same Tile Correct PIP (including correct Bidirectional PIPs) Case: Any PIPs in the same tile as StartPIP should have a StartWire that Matches the EndWire
	    			//This should also remove any correct Bidirectional PIPs within the same tile. 
	    			if(StartPIP.getEndWire() == P.getStartWire() && StartPIP.getTile().equals(P.getTile())){
	    				if(debug){
	    					System.out.println(Tabs + "Correct Same Tile Case: ");
	    					System.out.print(Tabs + P.toString(we));
	    				}
	    				PIPsAtThisLevel.add(P);
	    			}
	    		}
	    		PossiblePIPs.removeAll(PIPsAtThisLevel);    		
	    		for(PIP P: PossiblePIPs){
	    			//Different Tile (including correct Bidirectional PIPs) Case: 
	    			for(WireConnection wc : forwardConnections){
	    				//System.out.println(Tabs + "wc.getWire(): " +we.getWireName(wc.getWire()) + " p.getstart(): " + we.getWireName(P.getStartWire()) + " StartPIP.getTile(): " + StartPIP.getTile() + " P.getTIle(): " + P.getTile() + " wc.getTile():" + wc.getTile(StartPIP.getTile()));
		    			if(wc.getWire() == P.getStartWire() && wc.getTile(StartPIP.getTile()).equals(P.getTile()) && !StartPIP.getTile().equals(P.getTile())){
		    			  if(debug){
		    				System.out.println(Tabs + "Correct Different Tile Case: ");
		    				System.out.print(Tabs + P.toString(we));
		    			  }
		    				PIPsAtThisLevel.add(P); 
		    			}
	    			}
	    		}
	    		PossiblePIPs.removeAll(PIPsAtThisLevel);
	    		//At this point all PIPs should be incorrect bidirectional PIPs or correct bidirectional PIPs 
	    		// that are not directly routed to.
	    		for(PIP P: PossiblePIPs){
	    			if(isV4BidirectionalPIP(P, we) && P.getEndWire() == StartPIP.getEndWire() && P.getTile().equals(StartPIP.getTile())){
	    				swapWires(P);
	    				corrected++;
	    				PIPsAtThisLevel.add(P);
	    				if(debug){
	    				    System.out.println(Tabs +  "Incorrect Bidirectional PIP Same Tile Case: ");
	    				    System.out.print(Tabs + P.toString(we));
	    				}
		    		}
	    			
	    		}
	    		
	    		PossiblePIPs.removeAll(PIPsAtThisLevel);
	    		for(PIP P: PossiblePIPs){
	    			//Different Tile (including correct Bidirectional PIPs) Case: 
	    			for(WireConnection wc : forwardConnections){
		    			if(isV4BidirectionalPIP(P,we) && wc.getWire() == P.getEndWire() && wc.getTile(StartPIP.getTile()).equals(P.getTile()) && !StartPIP.getTile().equals(P.getTile())){
		    			  swapWires(P);
		    			  corrected++;
		    			  PIPsAtThisLevel.add(P);
		    			  if(debug){
		    				System.out.println(Tabs + "Incorrect Bidirectional PIP Different Tile Case: ");
		    				System.out.print(Tabs + P.toString(we));
		    			  }
		    			   
		    			}
	    			}
	    		}
	    		PossiblePIPs.removeAll(PIPsAtThisLevel);
	    		
    		}
    		
    	 /*  	for(PIP P: AllPIPs){
    	   	  if(!P.equals(StartPIP)){	
	    		if(P.getStartWire() == StartPIP.getEndWire() && StartPIP.getTile().equals(P.getTile())){ 
	    			//Same Node Case
	    			System.out.println(Tabs + "Same Node Case:");
	    			System.out.print(Tabs + P.toString(we));
	    			PIPsAtThisLevel.add(P); 
	    		} else if(isV4BidirectionalPIP(P, we) && P.getEndWire() == StartPIP.getEndWire() && StartPIP.getTile().equals(P.getTile())){
	    			//Correct the Bidirectional node and flag to add to PIPs
	    			swapWires(P);
	    			System.out.println(Tabs + "Corrected Bidirectional Line Case: ");
	    			System.out.print(Tabs + P.toString(we));
	    			PIPsAtThisLevel.add(P);
	    			corrected++; 
	    		}  else if(forwardConnections != null){
	    			//Connecting PIP
	    			for(WireConnection wc : forwardConnections){
	    				if(wc.getWire() == P.getStartWire() && wc.getTile(EndWire.getTile()).equals(P.getTile())){
	    					System.out.println(Tabs + "Forward Connection: " );
	    					System.out.print(Tabs + P.toString(we));
	    					PIPsAtThisLevel.add(P);
	    					break; 
	    				} else if(isV4BidirectionalPIP(P, we) && 
	    						  wc.getWire() == P.getEndWire() && 
	    						  wc.getTile(EndWire.getTile()).equals(P.getTile())){
	    					System.out.println(Tabs + "Corrected Forward Connection Case: ");
	    					swapWires(P);
	    					System.out.print(Tabs + P.toString(we));
	    	    			PIPsAtThisLevel.add(P);
	    	    			corrected++; 
	    	    			break;
	    				}
	    			}
	    		}
	    	  	
	    			
	    	}
    	   	}*/
    	} 
    	 
		for(PIP P: PIPsAtThisLevel){
			System.out.println(Tabs + "Decending into " + P.toString(we));
			corrected += NetDFS(P,AllPIPs, VisitedPIPs, level+1, we, debug);
		}
		
		return corrected; 
    }
    
    public static void swapWires(PIP P){
    	int temp = P.getStartWire();
		P.setStartWire(P.getEndWire());
		P.setEndWire(temp);	
    }
    //This function returns all the PIPs within a Net require to connect the Source Pin to the given end Pin returns null
    //if no path is found, This algorithm probably not work with any bi-directional connections.
    public static ArrayList<PIP> getPIPsFromSourceToSingleEndPin(Net N, Pin EndPin, Design design){
    	
    	WireEnumerator we = design.getWireEnumerator(); 
    			
    	if(N.getPins().contains(EndPin)){
    	    ArrayList<PIP> requiredPIPs = new ArrayList<PIP>();
            ArrayList<Node> StartWireNodes = new ArrayList<Node>();
            ArrayList<Node> EndWireNodes = new ArrayList<Node>();
            
            Node SourceNode = new Node(N.getSourceTile(), design.getDevice().getPrimitiveExternalPin(N.getSource()), null, 0);
            Node SinkNode   = new Node(EndPin.getTile(),  design.getDevice().getPrimitiveExternalPin(EndPin), null, 0);
            //System.out.println("Source Node: " + SourceNode.toString(design.getWireEnumerator()) +  " SinkNode " + SinkNode.toString(design.getWireEnumerator()));
            //The Goal is to reconstruct the graph that created the net.
            //We start by reconnecting the PIPs as nodes (note how the Parent of the EndWire is the start wire).
            
            //Create Nodes from PIPs 
            for(PIP P : N.getPIPs()){            	
            	Node StartWire = new Node(P.getTile(), P.getStartWire(), null, 0); 
            	Node EndWire   = new Node(P.getTile(), P.getEndWire(), StartWire, 0);
                StartWireNodes.add(StartWire);
                EndWireNodes.add(EndWire);   
            }
            
            //Some Connections appear as both start and end wires we need to remove them
            ArrayList<Node> RemoveFromStartWires = new ArrayList<Node>();
            ArrayList<Node> RemoveFromEndWires = new ArrayList<Node>();
            
            for(Node S: StartWireNodes){
            //	System.out.println("StartWire: " + S.toString(design.getWireEnumerator())+ "\n");
            	for(Node E: EndWireNodes){
            	//	System.out.println("EndWire: " + E.toString(design.getWireEnumerator()));
            		if(S.equals(E)){
            			S.setParent(E.getParent());
            			RemoveFromStartWires.add(S);
            			RemoveFromEndWires.add(E);
            		}
            	}
            	System.out.println("\n");
            }
            EndWireNodes.removeAll(RemoveFromEndWires);
            StartWireNodes.removeAll(RemoveFromStartWires);
            EndWireNodes.addAll(RemoveFromStartWires);
           
            //Identify Parent Nodes of StartWires 
            for(Node S: StartWireNodes){
            	boolean foundChild = false;
            	for(Node E: EndWireNodes){ 
            		if(E.getConnections() != null){ //End Nodes do not have any connections
	            		for(WireConnection W : E.getConnections()){
	            			if((W.getWire() == S.getWire()) && W.getTile(E.getTile()).equals(S.getTile())){
	            				S.setParent(E);
	            				foundChild = true; 
	            				break; 
	            			}
	            		}
	            		if(foundChild) break;
            		}
            	}
            	//The Source node should be the only node without a Parent
            	if(!foundChild && !(S.getWire() == SourceNode.getWire())) {
            		System.out.println("Could Not identify Parent of " + S.toString(design.getWireEnumerator()));
            		return null;
            	}
            }
            
            //Find the SinkNode with Parent 
            for(Node E :EndWireNodes){
            	if(E.equals(SinkNode)){
            		SinkNode = E;
            		break; 
            	}
            }
            
            //Start from the SinkNode and identify the PIPs that should be in the Final Net
           
            Node R = SinkNode;
            ArrayList<Node> usedNodes = new ArrayList<Node>();
            while(R.getParent()!= null){
            	usedNodes.add(R);
            	//System.out.println(R.toString(design.getWireEnumerator()));
            	R = R.getParent(); 
            }
            //Add the Source
            usedNodes.add(R);
            
            Node Q = SinkNode; 
            while(Q.getParent()!= null) {
            	for(PIP P : N.getPIPs()){
            		//System.out.println("PIP: " + P.toString(design.getWireEnumerator()));
            		if((P.getStartWire() == Q.getWire() && P.getTile().equals(Q.getTile()) || (P.getEndWire() == Q.getWire() && P.getTile().equals(Q.getTile())))){
            			if(!requiredPIPs.contains(P) && pipContainedWithinNodes(usedNodes, P)){
            				requiredPIPs.add(P);
            			}
            		}
            	}
            	Q = Q.getParent(); 
            } 
            
    		return requiredPIPs;
    	}
    	
    	System.out.println("Net " + N.getName() + " does not contain end pin " +  EndPin.getName());
    	return null; 
    }
    
    private static boolean pipContainedWithinNodes(ArrayList<Node> nodes, PIP p){
    	boolean startwireFound = false; 
    	boolean endwireFound   = false; 
    	for(Node N : nodes){
    	    if(p.getStartWire() == N.getWire() && p.getTile() == N.getTile()){
    	    	startwireFound = true;
    	    }
    	    
    	    if(p.getEndWire() == N.getWire() && p.getTile() == N.getTile()){
    	    	endwireFound = true;
    	    }
    	    
    	}
    	
    	return (startwireFound && endwireFound);
    }
}

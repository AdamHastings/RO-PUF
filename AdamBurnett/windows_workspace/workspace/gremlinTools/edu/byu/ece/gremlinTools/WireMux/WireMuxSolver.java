package edu.byu.ece.gremlinTools.WireMux;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;

import XDLDesign.Utils.XDLDesignUtils;
import edu.byu.ece.gremlinTools.Virtex4Bits.Bit;
import edu.byu.ece.gremlinTools.Virtex4Bits.Pip;
import edu.byu.ece.gremlinTools.xilinxToolbox.V4XilinxToolbox;
import edu.byu.ece.rapidSmith.bitstreamTools.configuration.FPGA;
import edu.byu.ece.rapidSmith.design.Design;
import edu.byu.ece.rapidSmith.design.PIP;
import edu.byu.ece.rapidSmith.device.Device;
import edu.byu.ece.rapidSmith.device.Tile;
import edu.byu.ece.rapidSmith.device.WireConnection;
import edu.byu.ece.rapidSmith.device.WireEnumerator;
import edu.byu.ece.rapidSmith.router.Node;
import edu.byu.ece.rapidSmith.router.Router;

public class WireMuxSolver {
   
	//The Goal of this program is to solve assign bits as either Row Bits or Column Bits For each mux in a CLB Switch box; 
	//And then save the information in a way it can be loaded by a switchbox object
	public static void main(String Args[]){
		
		System.out.println("Welcome to the WireMux Solver");
		
		Design design = new Design();
	  	design.setNCDVersion("v3.2");
		design.setPartName("xc4vlx60ff668-12");
		
		Device device = design.getDevice(); 
		FPGA fpga = XDLDesignUtils.getVirtexFPGA("XC4VLX60");
		V4XilinxToolbox TB = XDLDesignUtils.getV4Toolbox(fpga);
		
		String RootDirectory = "/net/fpga1/users/joshuas2/FPGA/Gremlin/PUF_TestBoardExperiments/LeakagePower/";
		String ISEDirectory  = RootDirectory + "VerifyRowAndColumnDesign/";
		String ExperimentDir = "";
		String TestCircuitFoler = "";
		
		String TestDesignXDLFilename = ISEDirectory + "test1.xdl";
		String RoutedXDLFilename     = ISEDirectory + "RoutedTest1.xdl";
		String TestBitstreamFilename = ISEDirectory + "test1.bit"; 
		String WireMuxTableFile = ""; 
		
		WireEnumerator WE = design.getWireEnumerator(); 
		//Load the Blank Bitstream
		
		
		//********************************************************
		// Create a the Test Design and Initial BitStream
		//********************************************************
				
		//Save the Test Bit Stream
		
		//********************************************************
		// Create a List of all the Wires in the SwitchBox
		//********************************************************
		
	    ArrayList<String> WireMuxList = new ArrayList<String>();
		
	    //Add all the OMUXES
	    //WireMuxList.add("BYP_INT_B7");
//	    WireMuxList.add("OMUX8");
//	    WireMuxList.add("OMUX9"); 
//	    WireMuxList.add("OMUX10");
//	    WireMuxList.add("OMUX11"); 
//	    WireMuxList.add("OMUX12"); 
//	    WireMuxList.add("OMUX13"); 
//	    WireMuxList.add("OMUX14"); 
//	    WireMuxList.add("OMUX15");
	   
	    WireMuxList.add("IMUX_B8");
	    WireMuxList.add("IMUX_B9");
	    WireMuxList.add("IMUX_B10");
	    WireMuxList.add("IMUX_B12");
	    WireMuxList.add("IMUX_B13");
	    WireMuxList.add("IMUX_B14");
	    WireMuxList.add("IMUX_B16");
	    WireMuxList.add("IMUX_B17");
	    WireMuxList.add("IMUX_B18");
	    WireMuxList.add("IMUX_B20");
	    WireMuxList.add("IMUX_B21");
	    WireMuxList.add("IMUX_B22");
	    WireMuxList.add("IMUX_B24");
	    WireMuxList.add("IMUX_B25");
	    WireMuxList.add("IMUX_B26");
	    WireMuxList.add("IMUX_B28");
	    WireMuxList.add("IMUX_B29");
	    WireMuxList.add("IMUX_B30");
	    
	    
	    
//	    WireMuxList.add("S2END2");
//	    WireMuxList.add("S2END6");
	    
	    
	    
	    
	    
	    //  for(int i = 0; i < 16; i++){
	   //    WireMuxList.add("OMUX" + i);  
		//}
	   // for(int i =0; i <32; i++){
		//  WireMuxList.add("IMUX_B" + i);
	   // }
	    //Add the Remaining Wire to the List
	    Tile CenterTile = device.getTile(5,7);
        
	    	    
	    //for(PIP P : CenterTile.getPIPs()){
	    	//String WN = P.getEndWireName(WE);
	    	//Ensure that the wire is not on the list and that 
        	//if(!WireMuxList.contains(WN) && !WN.contains("END") && WN.contains("MID")){
        	 //   WireMuxList.add(WN);
        	//}	
        //}		
        
        	    
	    //********************************************************
	    // Create A file to Serialize all the Objects into
	    //********************************************************
		
	    //The Code is Good it is being shut down Temporarily for testing
	   /* FileOutputStream fos = null;
	    ObjectOutputStream out = null; 
	    
	    try {
	       fos = new FileOutputStream(WireMuxTableFile);
	       out = new ObjectOutputStream(fos);
	    } catch (IOException ex){
	      ex.printStackTrace(); 
	    }
	    */
	    //********************************************************
	    // Iterate through all pips and solve for the Row and 
	    // Column Bits and Serialize the Results 
	    //********************************************************
	
	    ArrayList<String> VerifiedWires = new ArrayList<String>(); 
	    ArrayList<String> NonVerifiedWires = new ArrayList<String>(); 
	    HashSet<WireConnection> DriveWires = new HashSet<WireConnection>();
	    for(String WireName : WireMuxList){
	      
	    	//loads all the Bits and Creates the Table
	    	WireMux Mux = new WireMux(WireName);
	    	Mux.PopulateTable(true); 
	    	//Mux.TransposeTable();
	    	Mux.PrintTable();
	    	
	    	if (Mux.getTable() != null)
	    	{
	    	Node N = new Node(CenterTile, Mux.getTable().getDestWire());
	    	for(WireConnection wc : N.getConnections()){
	    		if(!DriveWires.contains(wc)){
	    			DriveWires.add(wc);
	    		} else {
	    			System.out.println("Warning : Two Nodes have Connection to the Same Wire: " + wc.toString(design.getWireEnumerator()));
	    		}
	    	}
	    
	    	System.out.println("");
	    //	System.exit(1);
	    /*	System.out.println("Before Transpose...");
	    	Mux.PrintTable();
	    	Mux.TransposeTable();
	    	System.out.println("After Transpose...");
	    	Mux.PrintTable();
	    	Mux.TransposeTable();
	    	System.out.println("Transpose Back...");
	       	Mux.PrintTable(); 
	       	System.exit(0);*/
	    	
	    	if(Mux.validWireTable() == true){
		    	//Verify the Row and Column Bits
		    	//if(Mux.VerifyRowColumnBits2(design, fpga, TB, TestBitstreamFilename) == true){ 
			    	
		  	        //Serialize the Muxes Table
			    	//Mux.SerializeTable(out);
		    	    //Add to Verified Wires List
			    ///	VerifiedWires.add(WireName);
		    	//} else {
		    	//  System.out.println("VerifyRowColumnBits was unable to Verify the Row and Column Bits"); 
		     	//  NonVerifiedWires.add(WireName);
		    	//}
	    	} else {
	    		NonVerifiedWires.add(WireName);
	    	}	
	    	}
	    	//break;
	    }//End Major Loop
	    
		
	    
	    System.out.println("Done!");
	    
	   
		
	}
}

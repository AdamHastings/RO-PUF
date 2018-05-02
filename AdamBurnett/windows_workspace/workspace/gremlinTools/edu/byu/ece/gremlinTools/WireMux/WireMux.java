package edu.byu.ece.gremlinTools.WireMux;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import edu.byu.ece.gremlinTools.Virtex4Bits.Bit;
import edu.byu.ece.gremlinTools.Virtex4Bits.ImpactInterface;
import edu.byu.ece.gremlinTools.Virtex4Bits.Pip;
import edu.byu.ece.gremlinTools.Virtex4Bits.V4ConfigurationBit;
import edu.byu.ece.gremlinTools.Virtex4Bits.Virtex4CLB;
import edu.byu.ece.gremlinTools.Virtex4Bits.Virtex4Lut;
import edu.byu.ece.gremlinTools.Virtex4Bits.Virtex4Slice;
import edu.byu.ece.gremlinTools.Virtex4Bits.Virtex4SwitchBox;
import edu.byu.ece.gremlinTools.Virtex4Bits.Virtex4SwitchBoxRouter;
import edu.byu.ece.gremlinTools.bitstreamDatabase.*;
import edu.byu.ece.gremlinTools.xilinxToolbox.V4XilinxToolbox;
import edu.byu.ece.rapidSmith.bitstreamTools.configuration.FPGA;
import edu.byu.ece.rapidSmith.design.Design;
import edu.byu.ece.rapidSmith.design.Instance;
import edu.byu.ece.rapidSmith.design.Net;
import edu.byu.ece.rapidSmith.design.NetType;
import edu.byu.ece.rapidSmith.design.PIP;
import edu.byu.ece.rapidSmith.design.Pin;
import edu.byu.ece.rapidSmith.device.Device;
import edu.byu.ece.rapidSmith.device.PrimitiveType;
import edu.byu.ece.rapidSmith.device.Tile;
import edu.byu.ece.rapidSmith.device.WireEnumerator;
import edu.byu.ece.rapidSmith.router.Router;
import edu.byu.ece.rapidSmith.util.FileConverter;
import edu.byu.ece.rapidSmith.util.MessageGenerator;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.io.File;

import XDLDesign.Utils.XDLDesignUtils;

public class WireMux implements Comparable{
    private String DestName;
    private WireMuxTable Table;
    private ArrayList<Group> Groups;
	private ArrayList<Pip> Pips; 
	private HashSet<Bit> BitSet;
	//private String[][] Table ;
	private int TableRows; 
	private int TableCols; 
	
	public WireMux(String DestName) {
    	setDestName(DestName);
    	this.Pips =new ArrayList<Pip>();
    	this.Groups = new ArrayList<Group>();
    	this.BitSet = new HashSet<Bit>();
    	
	}
    
	public WireMuxTable getTable() {return Table;};
	
    public void setDestName(String destName) {
		DestName = destName;
	}
	
    public String getDestName() {
		return DestName;
	}
	
    public void AddGroup(Group G){
    	this.Groups.add(G);
    }
    
	public ArrayList<Group> getGroups() {
		return Groups;
	}
    
	public void printPipMux(){
		 Table.printTable(); 
		/*for(int i=0; i<TableRows; i++){
			 for(int j=0; j<TableCols; j++)
			   System.out.print(Table[i][j]);
			 System.out.print("\n");
		  }*/
	}
	
	public boolean validWireTable() { 
	
		if(Table==null){
			return false; 
		} else {
			return true; 
		}
	}
    
	public void PopulateTable(boolean transposed){
    	BitstreamDB DB = new  BitstreamDB();
    	String query="";
    	DB.openConnection();
    	ResultSet rs = null;
    	
    	HashSet<Bit> BitSet0 = new HashSet<Bit>();
    	HashSet<Bit> BitSet1 = new HashSet<Bit>();
    	
    	Design D = new Design();
    	D.setNCDVersion("v3.2");
		D.setPartName("xc4vlx60ff668-12");
    	
    	//System.out.println("Populating PipMux");
    	
    	Tile T = D.getDevice().getTile(5,7);
    	// 1. Perform A Database Search on DestWire
    	//    Get all the PIPs that With The Search Wire as the Destination
    	if(DestName.startsWith("L")){
    		query = "SELECT * FROM  Virtex4Bits WHERE " + 
	          "Tile IN (\"int\",\"clb\") AND " +
	          "Destination = \"" + this.DestName + "\"";	
    	} else {
    	  query = "SELECT * FROM  Virtex4Bits WHERE " + 
    	          "Tile IN (\"int\",\"clb\",\"int_so\") AND " +
    	          "Destination = \"" + this.DestName + "\"";
    	}
    	//System.out.println("Making Query " + query);
        rs = DB.makeQuery(query);
    	
        if(DestName.equals("IMUX_B18")){
    		PIP P = new PIP();
    		P.setStartWire(D.getWireEnumerator().getWireEnum("BEST_LOGIC_OUTS6"));
    		P.setEndWire(D.getWireEnumerator().getWireEnum("IMUX_B18"));
    		P.setTile(T);
    		
    		Pip PP = new Pip(P);
    		Bit B0 = new Bit(11,7,1);
    		Bit B1 = new Bit(13,7,8);
    		BitSet.add(B0);
    		BitSet.add(B1);
    		PP.addBit(B0);
    		PP.addBit(B1);
    		Pips.add(PP);
    	}
        //Retrieve Bits From Database and Store in Pip Objects
        try {
			while(rs.next()){
//				System.out.println( rs.getString(1) + " " + rs.getString(2) +
//						" "+ rs.getString(3) + " " + rs.getString(4) +  " " + rs.getString(5) +
//						 " " + rs.getString(6) +  " " + rs.getString(7) +  " " + rs.getString(8));
				 
				
				//Create an XDL PIP
				PIP XDLP = new PIP();
				
				WireEnumerator W = D.getWireEnumerator();
				XDLP.setStartWire(W.getWireEnum(rs.getString(2)));
				XDLP.setEndWire(W.getWireEnum(rs.getString(4)));
				XDLP.setTile(T); 
				//Save the Bits Minor, Offset, and Bitmask
			    Bit B = new Bit(Integer.parseInt(rs.getString(6)), 
			    		        Integer.parseInt(rs.getString(7)), 
			    		        Integer.parseInt(rs.getString(8)));
				B.setProperValue(1);
			    //Add it To the BitSet to Prevent Duplicates
			    BitSet.add(B);
			    Pip P = new Pip(XDLP);
		         
			    
			    //If the PIP Already Exists add the bit to the already existing PIP
				boolean foundPip = false; 
				for(Pip pp : Pips){
					if(pp.equals(P)){
						//Add Bit to already Existing Pip
						pp.addBit(B);
						foundPip = true;
						break; 
					}
				}
				
				//Otherwise add the Bit to the New PIP and add the PIP to the List of PIPs
				if(foundPip == false){
					P.addBit(B);
					Pips.add(P);
				}
				
				
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
		//System.out.println("Size of Pips List " + Pips.size());
    	//System.out.println("Size of BitSet " + BitSet.size());
		if(BitSet.size() == 0){
			System.out.println("No Bits Returned for " + this.DestName);
			this.Table = null; 
		}
		else { 
			this.Table = new WireMuxTable(D.getWireEnumerator().getWireEnum(DestName), BitSet,  Pips, D.getWireEnumerator(), transposed);
		}
    	/*//Create A Group For Each PIP
    	for(Bit Q: BitSet){
    		
    		Group G = new Group(Q);
    		
    		//Add all the PIPs that Contain this Bit
    		for(Pip pp: Pips){
    			for(Bit q: pp.getBits()){
    				if(Q.equals(q)){
    					G.addPip(pp);
    				}
    			}
    		}
    		
    		
    		//If the Bit has Not been Added to a Set
    		if(!BitSet0.contains(G.getBit()) && !BitSet1.contains(G.getBit())){
    		   //Add the Bit to the First Set
    		   BitSet0.add(G.getBit());
    		   //Add the Other Bits in the Group to the Other Set
    		   for(Pip ppp: G.getGroupPips()){
    			   for(Bit bb : ppp.getBits()){
    				   if(!G.getBit().equals(bb)){
    					   BitSet1.add(bb);
    				   }
    			   }
    		   }
    		}
    		  		
    		//Add this Group to the List of Groups
    		this.AddGroup(G);
    		
    	}
	    
    	//System.out.println("Size of BitSet0 " + BitSet0.size());
    	//System.out.println("Size of BitSet1 " + BitSet1.size());
    	
    	Bit [] BitSet0Array = BitSet0.toArray(new Bit[BitSet0.size()]); 
    	Bit [] BitSet1Array = BitSet1.toArray(new Bit[BitSet1.size()]);
    	this.TableRows = BitSet0.size() + 1; 
    	this.TableCols = BitSet1.size() + 1; 
    	
    	String RightMostCellFormat = "%1$-7s";
		String CellFormat = "%1$-24s";
		Table = new String[BitSet0.size()+1][BitSet1.size()+1];
		
		//Top Row
		Table[0][0] = String.format(RightMostCellFormat, DestName);
		
		for(int i=1; i <= BitSet1Array.length; i++)
		{
		   String S = String.format(CellFormat, BitSet1Array[i-1].toString());
		   Table[0][i] =  S;
		}
	    
	    //Remaining Rows
	    
		for(int i=1; i<=BitSet0Array.length; i++)
		{
		  Bit BitSet0Bit = BitSet0Array[i-1];
		  Group BitSet0Group = findBitSet0Group(BitSet0Bit);
		  for(int j=0; j<=BitSet1Array.length; j++)
		  {
			  
			  if(j==0)
			  {
				  String S = String.format(RightMostCellFormat, BitSet0Bit.toString());
				  Table[i][j] =  S;
			  }
			  else{
				  Bit BitSet1Bit = BitSet1Array[j-1];
				  
				  //Find Pip with Matching Bit
				  Pip P = findPipInGroup(BitSet0Group, BitSet1Bit);
				  
				  String S= String.format(CellFormat,D.getWireEnumerator().getWireName(P.getPip().getStartWire()));
				  Table[i][j] = S; 
				  
			  }
			  
		  }
		  
		  
		 
		}
		
		*/
		DB.closeConnection();
    }

	public boolean VerifyRowColumnBits2(Design design, FPGA fpga, V4XilinxToolbox TB, String BlankBitstreamFilename){
		
		Device device = design.getDevice(); 
		ArrayList<Tile> CLBTiles = XDLDesignUtils.getAllCLBTiles(device);
		ArrayList<Virtex4CLB> TestSites = new ArrayList<Virtex4CLB>(); 
		int i = 0;
		XDLDesignUtils.loadBitstream(fpga, BlankBitstreamFilename);
		//Create 5000 CLBs
		for(Tile T: CLBTiles){
			TestSites.add(new Virtex4CLB(fpga, design, T, TB));
			i++;
			if(i > 3000){
				break;
			}
		}
		
		//Setup CLBs Correctly
		SetupCLBs(TestSites, design, fpga, TB);
		//Router
		Router R = new Router(design);
		R.routeDesign();
		//Add Shorts
		AddShorts(TestSites, design, fpga, TB);
		
		//Create Bitstream 1 (Assume Rol/Column Bits Correct)
		CreateBitstream(1, TestSites, design, fpga, TB);
		XDLDesignUtils.writeBitstreamToFile(fpga, "AssumeCorrect1.bit");
		
		CreateBitstream(2, TestSites, design, fpga, TB); 
		XDLDesignUtils.writeBitstreamToFile(fpga, "AssumeCorrect2.bit");
		
		CreateBitstream(3, TestSites, design, fpga, TB); 
		XDLDesignUtils.writeBitstreamToFile(fpga, "AssumeIncorrect3.bit");

		CreateBitstream(4, TestSites, design, fpga, TB); 
		XDLDesignUtils.writeBitstreamToFile(fpga, "AssumeIncorrect4.bit");

		//Download BitStream
		ImpactInterface.DownloadBitStream("AssumeCorrect1.bit");
	    float AssumeCorrect1 = readfromLabview();	
		
	    ImpactInterface.DownloadBitStream("AssumeCorrect2.bit");
	    float AssumeCorrect2 = readfromLabview();	
		
	    ImpactInterface.DownloadBitStream("AssumeCorrect1.bit");
	    float AssumeIncorrect3 = readfromLabview();	
			    
		ImpactInterface.DownloadBitStream("AssumeIncorrect.bit");
		float AssumeIncorrect4 = readfromLabview();
		
		System.out.println("AssumeCorrect1 " + AssumeCorrect1 + " AssumeCorrect2 " + AssumeCorrect2);
		System.out.println("AssumeIncorrect3 " + AssumeIncorrect3 + " AssumeIncorrect4 " + AssumeIncorrect4);
		ImpactInterface.DownloadBitStream(BlankBitstreamFilename);
		//Shorts are indicated by a short being less than 1.20 V, there should only be one shorted design out of 4 
		if(AssumeCorrect1 > 1.20 && AssumeCorrect2 < 1.20 && AssumeIncorrect3 > 1.20 && AssumeIncorrect4 > 1.20){
			System.out.println("Current Configuration is correct.");
		} else if(AssumeCorrect1 > 1.20 && AssumeCorrect2 > 1.20 && AssumeIncorrect3 > 1.20 && AssumeIncorrect4 < 1.20){
			System.out.println("Current Configuraiton is incorrect, swapping the mux table");
		    Table.TransposeTable();
		} else {
			System.out.println("Inconclusive Results...");
			System.out.println("AssumeCorrect " + AssumeCorrect1 + " AssumeCorrect " + AssumeCorrect2);
			System.out.println("AssumeIncorrect " + AssumeIncorrect3 + " AssumeIncorrect " + AssumeIncorrect4);
			//Write Experiment Results file 
		}
		
		return true;
	}
	
	private float readfromLabview() {
		float data = 0; 
		
		//Run Labview
		String command = "sh /net/fpga1/users/joshuas2/javaworkspace/SwitchBoxGrouper/SendReadVoltageCommand.sh";  
	       
		try {
			System.out.println("Running Command. " + command);
			Process P = Runtime.getRuntime().exec(command);
		   
		    
			if(P.waitFor() != 0){
				MessageGenerator.briefError("Impact did not run correctly.");
				System.exit(1);
			}
			if(P.exitValue() != 0){
				MessageGenerator.briefError("Impact did not exit correctly.");
				System.exit(1); 
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		//Give Labview some time to complete
		try {
			System.out.println("Sleeping 10 Seconds");
			Thread.sleep(1000, 0);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		//Read file
		File F = new File("/net/fpga1/users/joshuas2/LabviewReading.txt");
		try {
			//FileInputStream fis = new FileInputStream(F);
			FileReader FR = new FileReader(F);
			BufferedReader  BF = new BufferedReader(FR);
			//BufferedInputStream  bis = new BufferedInputStream(fis);
			//DataInputStream dis = new DataInputStream(bis);
			
			data = Float.parseFloat(BF.readLine());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//Return Value
        catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return data;
	}

	private void AddShorts(ArrayList<Virtex4CLB> TestSites, Design design,
			FPGA fpga, V4XilinxToolbox TB) {
		
		Virtex4SwitchBox SB = new Virtex4SwitchBox(fpga, design,TB);
		Virtex4SwitchBoxRouter SBR = new Virtex4SwitchBoxRouter(SB);
		WireEnumerator We = design.getWireEnumerator();
        //Create and Activate New Shorts
		Virtex4CLB TemplateCLB = TestSites.get(0);
		
		
		SB.setTiles(TemplateCLB.getINTTile(), TemplateCLB.getCLBTile());	
		SBR.setTiles(TemplateCLB.getINTTile(), TemplateCLB.getCLBTile());
		
		HashMap<Integer, ArrayList<PIP>> Short0 = SBR.CreateDistantShort(We.getWireEnum("X_PINWIRE0"), We.getWireEnum("X_PINWIRE1"), We.getWireEnum(DestName));
	    HashMap<Integer, ArrayList<PIP>> Short1 = SBR.CreateDistantShort(We.getWireEnum("Y_PINWIRE2"), We.getWireEnum("Y_PINWIRE3"), We.getWireEnum(DestName));
	    
		for(Virtex4CLB  CLB: TestSites){
			
			Tile IntTile = CLB.getINTTile();
			
		    Virtex4Slice SliceX0Y0 = CLB.getSLICEX0Y0();
			Virtex4Slice SliceX0Y1 = CLB.getSLICEX0Y1();
			Virtex4Slice SliceX1Y0 = CLB.getSLICEX1Y0();
			Virtex4Slice SliceX1Y1 = CLB.getSLICEX1Y1();

		    Net BEST_LOGIC_OUTS0 = new Net("BEST_LOGIC_OUTS0"+IntTile.toString(), NetType.WIRE);
		    Net BEST_LOGIC_OUTS1 = new Net("BEST_LOGIC_OUTS1"+IntTile.toString(), NetType.WIRE);
		    Net BEST_LOGIC_OUTS6 = new Net("BEST_LOGIC_OUTS6"+IntTile.toString(), NetType.WIRE);
		    Net BEST_LOGIC_OUTS7 = new Net("BEST_LOGIC_OUTS7"+IntTile.toString(), NetType.WIRE);         
		    		       
		    //Add Start Pins 
		    BEST_LOGIC_OUTS0.addPin(new Pin(true, "X", SliceX0Y0.getXDLInstance()));
		    BEST_LOGIC_OUTS1.addPin(new Pin(true, "X", SliceX1Y0.getXDLInstance()));
		    BEST_LOGIC_OUTS6.addPin(new Pin(true, "Y", SliceX0Y1.getXDLInstance()));
		    BEST_LOGIC_OUTS7.addPin(new Pin(true, "Y", SliceX1Y1.getXDLInstance()));
		    
		    //Modify Pips
		    for(PIP P :Short0.get(We.getWireEnum("X_PINWIRE0"))){
		    	P.setTile(IntTile);
		    }
		    for(PIP P :Short0.get(We.getWireEnum("X_PINWIRE1"))){
		    	P.setTile(IntTile);
		    }
		    for(PIP P :Short1.get(We.getWireEnum("Y_PINWIRE2"))){
		    	P.setTile(IntTile);
		    }
		    for(PIP P :Short1.get(We.getWireEnum("Y_PINWIRE3"))){
		    	P.setTile(IntTile);
		    }
		    
		    //Add Initial PIPs
		    BEST_LOGIC_OUTS0.getPIPs().addAll(Short0.get(We.getWireEnum("X_PINWIRE0"))); 	
		    BEST_LOGIC_OUTS1.getPIPs().addAll(Short0.get(We.getWireEnum("X_PINWIRE1")));
		    BEST_LOGIC_OUTS6.getPIPs().addAll(Short1.get(We.getWireEnum("Y_PINWIRE2")));
		    BEST_LOGIC_OUTS7.getPIPs().addAll(Short1.get(We.getWireEnum("Y_PINWIRE3")));
		        	
		    SB.ActivatePIPs(BEST_LOGIC_OUTS0.getPIPs());
		    SB.ActivatePIPs(BEST_LOGIC_OUTS1.getPIPs());
		    SB.ActivatePIPs(BEST_LOGIC_OUTS6.getPIPs());
		    SB.ActivatePIPs(BEST_LOGIC_OUTS7.getPIPs());
		       
		    //Add the Nets to the Design
		    design.addNet(BEST_LOGIC_OUTS0);
		    design.addNet(BEST_LOGIC_OUTS1);
		    design.addNet(BEST_LOGIC_OUTS6);
		    design.addNet(BEST_LOGIC_OUTS7);
            
		    if( VerifyNet(BEST_LOGIC_OUTS0, SB) && VerifyNet(BEST_LOGIC_OUTS1, SB) &&
		        VerifyNet(BEST_LOGIC_OUTS6, SB) && VerifyNet(BEST_LOGIC_OUTS7, SB)){
		        System.out.println("All Added Nets Verified!!");
		    } else {
		        System.out.println("A Net did not Verify.");
		        System.exit(1);
		    }
		     
		    
		      

		}
	}

	private void CreateBitstream(int num, ArrayList<Virtex4CLB> TestSites, Design design,
			FPGA fpga, V4XilinxToolbox TB) {
		
		//Bitstream 1 : Assume Current Table Correct; Shorting bits 3 and 4 (If Correct Configuration Should cause Short, if Incorrect No Short) 
		//Bitstream 2 : Assume Current Table Correct; Shorting bits 3 and 4 (If Correct Configuration Should not cause Short, if Incorrect No Short)
		//Bitstream 3 : Assume Current Table Incorrect; Shorting bits 1 and 2 (If Correct Configuration Should cause Short, if Incorrect No Short) 
		//Bitstream 4 : Assume Current Table Incorrect; Shorting bits 1 and 2 (If Correct Configuration Should not cause Short, if Incorrect No Short)
	
		ArrayList<Pip> PipsOfInterest = new ArrayList<Pip>();
	    
		for(Virtex4CLB CLB : TestSites){
			PipsOfInterest.clear();
			Net BEST_LOGIC_OUTS0 = design.getNet("BEST_LOGIC_OUTS0" + CLB.getINTTile().toString());
			Net BEST_LOGIC_OUTS1 = design.getNet("BEST_LOGIC_OUTS1" + CLB.getINTTile().toString());
			Net BEST_LOGIC_OUTS6 = design.getNet("BEST_LOGIC_OUTS6" + CLB.getINTTile().toString());
			Net BEST_LOGIC_OUTS7 = design.getNet("BEST_LOGIC_OUTS7" + CLB.getINTTile().toString());
			
			AddPipsOfInterst(PipsOfInterest, BEST_LOGIC_OUTS0);
			AddPipsOfInterst(PipsOfInterest, BEST_LOGIC_OUTS1);
			AddPipsOfInterst(PipsOfInterest, BEST_LOGIC_OUTS6);
			AddPipsOfInterst(PipsOfInterest, BEST_LOGIC_OUTS7);
	         	
		    if(PipsOfInterest.size() > 4){
		        System.out.println("To many Pips were added");
		        System.exit(1);
		    }
		         	
		    //Order the pips in PipsOfInterest such that i0, i1, i2, i3 are found at indexes 0,1,2,3 respectively
		    OrderPips(PipsOfInterest);
		    
		    //Table.printTable();
		    Pip i0 = PipsOfInterest.get(0);
		    Pip i1 = PipsOfInterest.get(1);
		    Pip i2 = PipsOfInterest.get(2);
		    Pip i3 = PipsOfInterest.get(3);
		        
		    //design.saveXDLFile("Debug.xdl");
		    //FileConverter.convertXDL2NCD("Debug.xdl");
		    //Set the Lut Contents Correctly
		    //System.out.println("i0 " + i0.getPip().toString(design.getWireEnumerator()) + " Rowbit: " +i0.getRowBit() + " Colbit " + i0.getColumnBit());
		    //System.out.println("i1 " + i1.getPip().toString(design.getWireEnumerator()) + " Rowbit: " +i1.getRowBit() + " Colbit " + i1.getColumnBit());
		    //System.out.println("i2 " + i2.getPip().toString(design.getWireEnumerator()) + " Rowbit: " +i2.getRowBit() + " Colbit " + i2.getColumnBit());
		    //System.out.println("i3 " + i3.getPip().toString(design.getWireEnumerator()) + " Rowbit: " +i3.getRowBit() + " Colbit " + i3.getColumnBit());
		    
		    //CLB.getSLICEX0Y0().getLutF().AssertLutContent();//BEST_LOGIC_OUTS0
		    //CLB.getSLICEX0Y1().getLutG().AssertLutContent();//BEST_LOGIC_OUTS6
		    //CLB.getSLICEX1Y0().getLutF().ClearLutContents();//BEST_LOGIC_OUTS1
		    //CLB.getSLICEX1Y1().getLutG().ClearLutContents();//BEST_LOGIC_OUTS7
		    //SetOriginLutContents( i0, "Assert", CLB.getSLICEX0Y0(), CLB.getSLICEX0Y1(), CLB.getSLICEX1Y0(), CLB.getSLICEX1Y1(), design);
		    //SetOriginLutContents( i1, "Assert",  CLB.getSLICEX0Y0(), CLB.getSLICEX0Y1(), CLB.getSLICEX1Y0(), CLB.getSLICEX1Y1(), design);
		    //SetOriginLutContents( i2, "Clear", CLB.getSLICEX0Y0(), CLB.getSLICEX0Y1(), CLB.getSLICEX1Y0(), CLB.getSLICEX1Y1(), design);
		    //SetOriginLutContents( i3, "Clear",  CLB.getSLICEX0Y0(), CLB.getSLICEX0Y1(), CLB.getSLICEX1Y0(), CLB.getSLICEX1Y1(), design);    	
		
		    //Set the Column Bits and See if they Short
		    V4ConfigurationBit bit1 = new V4ConfigurationBit(fpga, i0.getRowBit(), i0.getPip().getTile(), TB);
		    V4ConfigurationBit bit2 = new V4ConfigurationBit(fpga, i3.getRowBit(), i3.getPip().getTile(), TB);
		    V4ConfigurationBit bit3 = new V4ConfigurationBit(fpga, i0.getColumnBit(), i0.getPip().getTile(), TB);
		    V4ConfigurationBit bit4 = new V4ConfigurationBit(fpga, i3.getColumnBit(), i3.getPip().getTile(), TB);
            
			switch(num){
			 case 1:
			    bit1.SetBitValueInBitStream(0, CLB.getINTTile());
			    bit2.SetBitValueInBitStream(0, CLB.getINTTile());
			    bit3.SetBitValueInBitStream(1, CLB.getINTTile());
			    bit4.SetBitValueInBitStream(1, CLB.getINTTile());
			      
			    SetOriginLutContents( i0, "Assert", CLB.getSLICEX0Y0(), CLB.getSLICEX0Y1(), CLB.getSLICEX1Y0(), CLB.getSLICEX1Y1(), design);
			    SetOriginLutContents( i1, "Assert",  CLB.getSLICEX0Y0(), CLB.getSLICEX0Y1(), CLB.getSLICEX1Y0(), CLB.getSLICEX1Y1(), design);
			    SetOriginLutContents( i2, "Clear", CLB.getSLICEX0Y0(), CLB.getSLICEX0Y1(), CLB.getSLICEX1Y0(), CLB.getSLICEX1Y1(), design);
			    SetOriginLutContents( i3, "Clear",  CLB.getSLICEX0Y0(), CLB.getSLICEX0Y1(), CLB.getSLICEX1Y0(), CLB.getSLICEX1Y1(), design);    	
			    break;
			 case 2: 
			    bit1.SetBitValueInBitStream(0, CLB.getINTTile());
			    bit2.SetBitValueInBitStream(0, CLB.getINTTile());
			    bit3.SetBitValueInBitStream(1, CLB.getINTTile());
			    bit4.SetBitValueInBitStream(1, CLB.getINTTile());
			    
			    SetOriginLutContents( i0, "Assert", CLB.getSLICEX0Y0(), CLB.getSLICEX0Y1(), CLB.getSLICEX1Y0(), CLB.getSLICEX1Y1(), design);
			    SetOriginLutContents( i1, "Clear",  CLB.getSLICEX0Y0(), CLB.getSLICEX0Y1(), CLB.getSLICEX1Y0(), CLB.getSLICEX1Y1(), design);
			    SetOriginLutContents( i2, "Assert", CLB.getSLICEX0Y0(), CLB.getSLICEX0Y1(), CLB.getSLICEX1Y0(), CLB.getSLICEX1Y1(), design);
			    SetOriginLutContents( i3, "Clear",  CLB.getSLICEX0Y0(), CLB.getSLICEX0Y1(), CLB.getSLICEX1Y0(), CLB.getSLICEX1Y1(), design);    	
			    break;
			 case 3: 
				bit1.SetBitValueInBitStream(1, CLB.getINTTile());
				bit2.SetBitValueInBitStream(1, CLB.getINTTile());
				bit3.SetBitValueInBitStream(0, CLB.getINTTile());
				bit4.SetBitValueInBitStream(0, CLB.getINTTile());
				   
				SetOriginLutContents( i0, "Assert", CLB.getSLICEX0Y0(), CLB.getSLICEX0Y1(), CLB.getSLICEX1Y0(), CLB.getSLICEX1Y1(), design);
				SetOriginLutContents( i1, "Clear",  CLB.getSLICEX0Y0(), CLB.getSLICEX0Y1(), CLB.getSLICEX1Y0(), CLB.getSLICEX1Y1(), design);
				SetOriginLutContents( i2, "Assert", CLB.getSLICEX0Y0(), CLB.getSLICEX0Y1(), CLB.getSLICEX1Y0(), CLB.getSLICEX1Y1(), design);
				SetOriginLutContents( i3, "Clear",  CLB.getSLICEX0Y0(), CLB.getSLICEX0Y1(), CLB.getSLICEX1Y0(), CLB.getSLICEX1Y1(), design);    	
				break; 
			 case 4: 
				bit1.SetBitValueInBitStream(1, CLB.getINTTile());
				bit2.SetBitValueInBitStream(1, CLB.getINTTile());
				bit3.SetBitValueInBitStream(0, CLB.getINTTile());
				bit4.SetBitValueInBitStream(0, CLB.getINTTile());
				    
				SetOriginLutContents( i0, "Assert", CLB.getSLICEX0Y0(), CLB.getSLICEX0Y1(), CLB.getSLICEX1Y0(), CLB.getSLICEX1Y1(), design);
				SetOriginLutContents( i1, "Assert",  CLB.getSLICEX0Y0(), CLB.getSLICEX0Y1(), CLB.getSLICEX1Y0(), CLB.getSLICEX1Y1(), design);
				SetOriginLutContents( i2, "Clear", CLB.getSLICEX0Y0(), CLB.getSLICEX0Y1(), CLB.getSLICEX1Y0(), CLB.getSLICEX1Y1(), design);
				SetOriginLutContents( i3, "Clear",  CLB.getSLICEX0Y0(), CLB.getSLICEX0Y1(), CLB.getSLICEX1Y0(), CLB.getSLICEX1Y1(), design);    	
				break; 
		      default : System.out.println("Invalid Bitstream Number...");
			            System.exit(1);
			            break; 
			}
			
		}
	}

	private void SetupCLBs(ArrayList<Virtex4CLB> TestSites, Design design, FPGA fpga, V4XilinxToolbox TB) {
		
		Net GroundNet = new Net("Global_Logic0", NetType.GND); 
		design.addNet(GroundNet);
				
		for(Virtex4CLB CLB : TestSites){
			
			
		    Virtex4Slice SliceX0Y0 = CLB.getSLICEX0Y0();
			Virtex4Slice SliceX0Y1 = CLB.getSLICEX0Y1();
			Virtex4Slice SliceX1Y0 = CLB.getSLICEX1Y0();
			Virtex4Slice SliceX1Y1 = CLB.getSLICEX1Y1();
		   	
			SliceX0Y0.setXUsedAttribute("0");
			SliceX0Y1.setYUsedAttribute("0");
			SliceX1Y0.setXUsedAttribute("0");
			SliceX1Y1.setYUsedAttribute("0");
			
			GroundLutInputs(SliceX0Y0, GroundNet, "F");
			GroundLutInputs(SliceX0Y1, GroundNet, "G");
			GroundLutInputs(SliceX1Y0, GroundNet, "F");
			GroundLutInputs(SliceX1Y1, GroundNet, "G");
		}
	}

	private Pip findPipInGroup(Group bitSet0Group, Bit bitSet1Bit) {
		for(Pip P: bitSet0Group.getGroupPips()){
			for(Bit B : P.getBits()){
				if(B.equals(bitSet1Bit)){
					return P; 
				}
			}
		}
		return null;
	}

	private Group findBitSet0Group(Bit bitSet0Bit) {
		
		for(Group G : Groups){
			if(G.getBit().equals(bitSet0Bit)){
				return G; 
			}
		}
		
		return null; 
	}

	public boolean VerifyRowColumnBits(Design design, FPGA fpga, V4XilinxToolbox TB, String BlankBitstreamFilename) {
		
		System.out.println("Verifying Row/Column Bits for PIP Table " + this.DestName);
		
		Device device = design.getDevice(); 
				
		Tile CenterTile = device.getTile(5,7);
		System.out.println(CenterTile.getType());
		
		//Load a Blank Bitstream
		XDLDesignUtils.loadBitstream(fpga, BlankBitstreamFilename); 
		
		//Test Tiles
		//These tiles are in identical spots in the Fabric
		Tile INT_Test0 = device.getTile(76,46);
		Tile CLB_Test0 = device.getTile(76,47);
		Tile INT_Test1 = device.getTile(94,46);
		Tile CLB_Test1 = device.getTile(94,47);
		Tile INT_Test2 = device.getTile(112,46);
		Tile CLB_Test2 = device.getTile(112,47);
		Tile INT_Test3 = device.getTile(130,46);
		Tile CLB_Test3 = device.getTile(130,47);
		
		Virtex4CLB TestSite0 = new Virtex4CLB(fpga, design, CLB_Test0, TB);
		Virtex4CLB TestSite1 = new Virtex4CLB(fpga, design, CLB_Test1, TB);
		Virtex4CLB TestSite2 = new Virtex4CLB(fpga, design, CLB_Test2, TB);
		Virtex4CLB TestSite3 = new Virtex4CLB(fpga, design, CLB_Test3, TB);
		//Setup Slices and Routing 
		//Add Clock IOB 
		
		System.out.println("Creating Test Design Framework!"); 
		//XDLDesignUtils.CreateIOB(design, "C13", true, "CLK_IOB", "CLK_NET");	
		XDLDesignUtils.CreateClockInput("C13", "BUFGCTRL_X0Y31", design);
		design.addNet(new Net("Global_Logic0", NetType.GND));
		
		Virtex4Slice FinalSlice0 = SetupSlicesInitialNets(0, TestSite0, INT_Test0, CLB_Test0, design, fpga, TB);
		Virtex4Slice FinalSlice1 = SetupSlicesInitialNets(1, TestSite1, INT_Test1, CLB_Test1, design, fpga, TB);
		Virtex4Slice FinalSlice2 = SetupSlicesInitialNets(2, TestSite2, INT_Test2, CLB_Test2, design, fpga, TB);
		Virtex4Slice FinalSlice3 = SetupSlicesInitialNets(3, TestSite3, INT_Test3, CLB_Test3, design, fpga, TB);
		
		if(FinalSlice0.VerifySlice() == false){
			System.out.println("FinalSlice0 did not Verify.");
			System.exit(1);
		}
		
		if(FinalSlice1.VerifySlice() == false){
			System.out.println("FinalSlice1 did not Verify.");
			System.exit(1);
		}
		
		if(FinalSlice2.VerifySlice() == false){
			System.out.println("FinalSlice2 did not Verify.");
			System.exit(1);
		}
		
		if(FinalSlice3.VerifySlice() == false){
			System.out.println("FinalSlice3 did not Verify.");
			System.exit(1);
		}
		
		
		System.out.println("\tSaving Test Design Framework to XDL...");
		design.saveXDLFile("Test.xdl");	
		System.out.println("\tConverting Test Design Framework to NCD...");
		FileConverter.convertXDL2NCD("Test.xdl");
		System.out.println("\tRouting Test Design Framework...");
		XDLDesignUtils.RunXilinxRouter("Test.ncd", "RoutedTest.ncd", "high");
		System.out.println("\tCreating Test Design Framework Bitstream.."); //So I don't have to mess with the clock bits
		XDLDesignUtils.RunXilinxBitgen("RoutedTest.ncd", "RoutedTest.bit", " -d -w -m");
		System.out.println("\tLoading Test Design Framework Bitstream...");
		XDLDesignUtils.loadBitstream(fpga, "RoutedTest.bit");
		System.out.println("\tVerifying New Bitstream...");
		
		//SetValues that Have become Corrupted by Bitgen
		FinalSlice0.setBXINV_ATTR("BX");
		FinalSlice1.setBXINV_ATTR("BX");
		FinalSlice2.setBXINV_ATTR("BX");
		FinalSlice3.setBXINV_ATTR("BX");
		
		FinalSlice0.getDXMUX0().setDXMUX_ATTR("BX");
		FinalSlice1.getDXMUX0().setDXMUX_ATTR("BX");
		FinalSlice2.getDXMUX0().setDXMUX_ATTR("BX");
		FinalSlice3.getDXMUX0().setDXMUX_ATTR("BX");
		
		
		//Verify That the Bitstream is the Way I want it
		if(FinalSlice0.VerifySlice() == false){
			System.out.println("FinalSlice0 did not Verify.");
			System.exit(1);
		}
		
		if(FinalSlice1.VerifySlice() == false){
			System.out.println("FinalSlice1 did not Verify.");
			System.exit(1);
		}
		
		if(FinalSlice2.VerifySlice() == false){
			System.out.println("FinalSlice2 did not Verify.");
			System.exit(1);
		}
		
		if(FinalSlice3.VerifySlice() == false){
			System.out.println("FinalSlice3 did not Verify.");
			System.exit(1);
		}
		
		Design routedDesign = new Design();
		System.out.println("\tCreating and Loading Routed XDL File...");
		FileConverter.convertNCD2XDL("RoutedTest.ncd");
		routedDesign.loadXDLFile("RoutedTest.xdl");
		
		System.out.println("Adding the New Routes to the Original Design");
		
		//Update to the Routed Design... 
		  //1. Add the New Ground Nets and Remove the Original
		  design.removeNet("Global_Logic0");
		  for(Net N:routedDesign.getNets()){
			if(N.getType().equals(NetType.GND)){
				System.out.println("Adding Net " + N);
				design.addNet(N);
			}
		  }
		  
		  //2. Add the TieOff Instances
		  for(Instance I : routedDesign.getInstances()){
			  if(I.getPrimitiveSite().getType().equals(PrimitiveType.TIEOFF)){
				  design.addInstance(I);
			  }
		  }
		  
		  //3. Add Routed Pips to the CLK_NET
		  design.getNet("CLK_NET").getPIPs().addAll(routedDesign.getNet("CLK_NET").getPIPs());
		
		  //Verify Current Configuration
		  //Todo Pass the Final Slice into this function!! 
		  System.out.println("Completing the New Design");
		  ArrayList<Pip> TestPips0 = CompleteDesign(0, TestSite0, FinalSlice0, INT_Test0, CLB_Test0, design, fpga, TB);
		  ArrayList<Pip> TestPips1 = CompleteDesign(0, TestSite1, FinalSlice1, INT_Test1, CLB_Test1, design, fpga, TB);
		  ArrayList<Pip> TestPips2 = CompleteDesign(0, TestSite2, FinalSlice2, INT_Test2, CLB_Test2, design, fpga, TB);
		  ArrayList<Pip> TestPips3 = CompleteDesign(0, TestSite3, FinalSlice3, INT_Test3, CLB_Test3, design, fpga, TB);
		 
		  design.saveXDLFile("Debug.xdl");
	      FileConverter.convertXDL2NCD("Debug.xdl");
	      
	      setAssumeRowColumnBitsCorrect(1, TestPips0, fpga, design, TB);
	      setAssumeRowColumnBitsCorrect(1, TestPips1, fpga, design, TB);
	      setAssumeRowColumnBitsCorrect(1, TestPips2, fpga, design, TB);
	      setAssumeRowColumnBitsCorrect(1, TestPips3, fpga, design, TB);
		   
		  ImpactInterface.DownloadBitStream(fpga);
		  ImpactInterface.SendCaptureCommand();
		  ImpactInterface.ReadBackBitstream(fpga, "RoutedTest.bit", "SwitchBoxGrouper");
		  
		  System.out.println("TestSite0 ReadBack Value " + FinalSlice0.getFFX().getInitBit().getValue());
		  System.out.println("TestSite1 ReadBack Value " + FinalSlice1.getFFX().getInitBit().getValue());
		  System.out.println("TestSite2 ReadBack Value " + FinalSlice2.getFFX().getInitBit().getValue());
		  System.out.println("TestSite3 ReadBack Value " + FinalSlice3.getFFX().getInitBit().getValue());
		  
		  setAssumeRowColumnBitsCorrect(2, TestPips0, fpga, design, TB);
	      setAssumeRowColumnBitsCorrect(2, TestPips1, fpga, design, TB);
	      setAssumeRowColumnBitsCorrect(2, TestPips2, fpga, design, TB);
	      setAssumeRowColumnBitsCorrect(2, TestPips3, fpga, design, TB);
		   
		  ImpactInterface.DownloadBitStream(fpga);
		  ImpactInterface.SendCaptureCommand();
		  ImpactInterface.ReadBackBitstream(fpga, "RoutedTest.bit", "SwitchBoxGrouper");
		  
		  System.out.println("TestSite0 ReadBack Value " + FinalSlice0.getFFX().getInitBit().getValue());
		  System.out.println("TestSite1 ReadBack Value " + FinalSlice1.getFFX().getInitBit().getValue());
		  System.out.println("TestSite2 ReadBack Value " + FinalSlice2.getFFX().getInitBit().getValue());
		  System.out.println("TestSite3 ReadBack Value " + FinalSlice3.getFFX().getInitBit().getValue());
		  
		  
		  setAssumeRowColumnBitsNotCorrect(1, TestPips0, fpga,design, TB);
		  setAssumeRowColumnBitsNotCorrect(1, TestPips1, fpga,design, TB);
		  setAssumeRowColumnBitsNotCorrect(1, TestPips2, fpga,design, TB);
		  setAssumeRowColumnBitsNotCorrect(1, TestPips3, fpga,design, TB);
		  
		  ImpactInterface.DownloadBitStream(fpga);
		  ImpactInterface.SendCaptureCommand();
		  ImpactInterface.ReadBackBitstream(fpga, "RoutedTest.bit", "SwitchBoxGrouper");
		  
		  System.out.println("TestSite0 ReadBack Value " + FinalSlice0.getFFX().getInitBit().getValue());
		  System.out.println("TestSite1 ReadBack Value " + FinalSlice1.getFFX().getInitBit().getValue());
		  System.out.println("TestSite2 ReadBack Value " + FinalSlice2.getFFX().getInitBit().getValue());
		  System.out.println("TestSite3 ReadBack Value " + FinalSlice3.getFFX().getInitBit().getValue());
		  
		  setAssumeRowColumnBitsNotCorrect(2, TestPips0, fpga,design, TB);
		  setAssumeRowColumnBitsNotCorrect(2, TestPips1, fpga,design, TB);
		  setAssumeRowColumnBitsNotCorrect(2, TestPips2, fpga,design, TB);
		  setAssumeRowColumnBitsNotCorrect(2, TestPips3, fpga,design, TB);
		  
		  ImpactInterface.DownloadBitStream(fpga);
		  ImpactInterface.SendCaptureCommand();
		  ImpactInterface.ReadBackBitstream(fpga, "RoutedTest.bit", "SwitchBoxGrouper");
		  
		  System.out.println("TestSite0 ReadBack Value " + FinalSlice0.getFFX().getInitBit().getValue());
		  System.out.println("TestSite1 ReadBack Value " + FinalSlice1.getFFX().getInitBit().getValue());
		  System.out.println("TestSite2 ReadBack Value " + FinalSlice2.getFFX().getInitBit().getValue());
		  System.out.println("TestSite3 ReadBack Value " + FinalSlice3.getFFX().getInitBit().getValue());
		  
		  return true; 
	}
    
private ArrayList<V4ConfigurationBit> setAssumeRowColumnBitsNotCorrect(int row, ArrayList<Pip> TestPips, FPGA fpga, Design design, V4XilinxToolbox TB) {
		
		Pip i0 = TestPips.get(0);
	    Pip i1 = TestPips.get(1);
	    Pip i2 = TestPips.get(2);
	    Pip i3 = TestPips.get(3); 
		
	    V4ConfigurationBit bit1 = new V4ConfigurationBit(fpga, i0.getRowBit(), i0.getPip().getTile(), TB);
	    V4ConfigurationBit bit2 = new V4ConfigurationBit(fpga, i3.getRowBit(), i3.getPip().getTile(), TB);
	    V4ConfigurationBit bit3 = new V4ConfigurationBit(fpga, i0.getColumnBit(), i0.getPip().getTile(), TB);
	    V4ConfigurationBit bit4 = new V4ConfigurationBit(fpga, i3.getColumnBit(), i3.getPip().getTile(), TB);
	   
	    Virtex4SwitchBox SB = new Virtex4SwitchBox(fpga, design,TB);
		SB.setTiles(i0.getPip().getTile(), XDLDesignUtils.getMatchingCLBTile(i0.getPip().getTile(), design.getDevice()));
	    
	    //i0 and i3 are driven by a '1' while i1 and i2 are driven by a '0'

	    //Assume that Row/Column Bits are not correct
	      //  i0  i2
	      //  i1  i3
	    
	    //Since Shorted Nets exhibit an ORing Behavior we know that the output of 
	    //  both rows should be 1
	    
	    //Check Row 3 //Enable Bit 1,3,4 Disable Bit 4 
	    if(row == 1){
		    bit3.SetBitValueInBitStream(1, i0.getPip().getTile());
		    bit4.SetBitValueInBitStream(0, i0.getPip().getTile());
	    } else if(row ==2){
	    	bit3.SetBitValueInBitStream(0, i0.getPip().getTile());
		    bit4.SetBitValueInBitStream(1, i0.getPip().getTile());
	    }
	    
	    bit1.SetBitValueInBitStream(1, i0.getPip().getTile());
	    bit2.SetBitValueInBitStream(1, i0.getPip().getTile());
	    
	    ArrayList<V4ConfigurationBit> Bits = new ArrayList<V4ConfigurationBit>(); 
		Bits.add(bit1);
		Bits.add(bit2);
		Bits.add(bit3);
		Bits.add(bit4);
	    return Bits;
	}
	
	
	private ArrayList<V4ConfigurationBit> setAssumeRowColumnBitsCorrect(int row, ArrayList<Pip> TestPips, FPGA fpga, Design design, V4XilinxToolbox TB) {
		
		Pip i0 = TestPips.get(0);
	    Pip i1 = TestPips.get(1);
	    Pip i2 = TestPips.get(2);
	    Pip i3 = TestPips.get(3); 
		
	    V4ConfigurationBit bit1 = new V4ConfigurationBit(fpga, i0.getRowBit(), i0.getPip().getTile(), TB);
	    V4ConfigurationBit bit2 = new V4ConfigurationBit(fpga, i3.getRowBit(), i3.getPip().getTile(), TB);
	    V4ConfigurationBit bit3 = new V4ConfigurationBit(fpga, i0.getColumnBit(), i0.getPip().getTile(), TB);
	    V4ConfigurationBit bit4 = new V4ConfigurationBit(fpga, i3.getColumnBit(), i3.getPip().getTile(), TB);
	   
	    Virtex4SwitchBox SB = new Virtex4SwitchBox(fpga, design,TB);
		SB.setTiles(i0.getPip().getTile(), XDLDesignUtils.getMatchingCLBTile(i0.getPip().getTile(), design.getDevice()));
	    
	    //i0 and i3 are driven by a '1' while i1 and i2 are driven by a '0'

	    //Assume that Row/Column Bits are correct
	      //  i0  i1
	      //  i2  i3
	    
	    //Since Shorted Nets exhibit an ORing Behavior we know that the output of 
	    //  both rows should be 1
	    
	    //Check Row 1 //Enable Bit 1,3,4 Disable Bit 2 
		if(row == 1){
		    bit1.SetBitValueInBitStream(1, i0.getPip().getTile());
		    bit2.SetBitValueInBitStream(0, i0.getPip().getTile());
		} else if(row == 2){
			bit1.SetBitValueInBitStream(0, i0.getPip().getTile());
		    bit2.SetBitValueInBitStream(1, i0.getPip().getTile());
		}
	    bit3.SetBitValueInBitStream(1, i0.getPip().getTile());
	    bit4.SetBitValueInBitStream(1, i0.getPip().getTile());
	    
	    ArrayList<V4ConfigurationBit> Bits = new ArrayList<V4ConfigurationBit>(); 
		Bits.add(bit1);
		Bits.add(bit2);
		Bits.add(bit3);
		Bits.add(bit4);
	    return Bits;
	}

	private Virtex4Slice SetupSlicesInitialNets(int num,  Virtex4CLB TestSite, Tile IntTile, Tile CLBTile, Design design, FPGA fpga, V4XilinxToolbox TB){
		   //Initialize Slices	
		   /*
			   *    Set SLICEX0Y0 LUT F to Output '1' 
			   *    Set SLICEX0Y1 LUT F to Output '0' 
			   *    Set SLICEX0Y1 LUT G to Output '1' 
			   *    Set SLICEX1Y1 LUT G to Output '0' 
		   */
			Table.SetTile(IntTile);
		   	
			for(Virtex4Slice S : TestSite.getSliceArrayList()){
				this.GroundLutInputs(S, design.getNet("Global_Logic0"), "Both");
			}
				   
			Virtex4SwitchBox SB = new Virtex4SwitchBox(fpga, design,TB);
			SB.setTiles(IntTile, CLBTile);	
			Virtex4SwitchBoxRouter SBR = new Virtex4SwitchBoxRouter(SB);
			SBR.setTiles(IntTile, CLBTile);		
			WireEnumerator We = design.getWireEnumerator();  
			
			//design.addNet(SBR.GroundLutInputs(IntTile, design, SliceX0Y0.getXDLInstance(), SliceX0Y1.getXDLInstance(), SliceX1Y0.getXDLInstance(), SliceX1Y1.getXDLInstance()));
		    ArrayList<PIP> X_PINWIRE0_TO_DESTWIRE;	
			ArrayList<PIP> FinishRoute;		
		      
			X_PINWIRE0_TO_DESTWIRE = SBR.CreateBridgingShort(We.getWireEnum("X_PINWIRE0"), We.getWireEnum(DestName));
		   		      
		    Pin SinkPin; 
		        
		    FinishRoute = SBR.LongBFS(We.getWireEnum(DestName),  We.getWireEnum("BX_PINWIRE0"), IntTile, design.getDevice());
		        
		    //Create the EndSlice 
		        
		    //1. Find the Final Tile
		    Tile FinalTile = null; 
		    for(PIP P : FinishRoute){
		      	if(P.getEndWireName(We).equals("BX_PINWIRE0")){
		       		FinalTile = P.getTile();
		       	}
		    }
		        
		    Virtex4Slice FinalSliceX0Y0 = new Virtex4Slice(0, 0, fpga, design, FinalTile, TB);
		        
		    FinalSliceX0Y0.setFFLatchAttributValue("#FF");
		    FinalSliceX0Y0.setBXINV_ATTR("BX");
		    FinalSliceX0Y0.getDXMUX0().setDXMUX_ATTR("BX");
		    FinalSliceX0Y0.setCLKINV_ATTR("CLK");
		    FinalSliceX0Y0.getFFX().setInitAttributeValue("#INIT0");
		    FinalSliceX0Y0.getFFY().setInitAttributeValue("#INIT0");
		    FinalSliceX0Y0.setSYNC_ATTR_Value("ASYNC");
		    FinalSliceX0Y0.setSRINV_ATTRValue("SR");
		    FinalSliceX0Y0.getFFX().setSR_ATTR("SRLOW");
		    FinalSliceX0Y0.getFFY().setSR_ATTR("SRLOW");
		    //Ground the SR pin
		    design.getNet("Global_Logic0").addPin(new Pin(false, "SR", FinalSliceX0Y0.getXDLInstance()));
		    
		    Net CLK_NET = design.getNet("CLK_NET");
		    CLK_NET.addPin(new Pin(false, "CLK", FinalSliceX0Y0.getXDLInstance()));
		    
		    SinkPin = new Pin(false, "BX", FinalSliceX0Y0.getXDLInstance());  
		
			
		    //Notice That BEST_LOGIC_OUTS0 is not added here
		    Net BEST_LOGIC_OUTS0 = new Net("BEST_LOGIC_OUTS0"+IntTile.toString(), NetType.WIRE);
		    BEST_LOGIC_OUTS0.addPin(SinkPin);
		    BEST_LOGIC_OUTS0.addPin(new Pin(true, "X", TestSite.getSLICEX0Y0().getXDLInstance()));
		    BEST_LOGIC_OUTS0.getPIPs().addAll(X_PINWIRE0_TO_DESTWIRE);//.get(We.getWireEnum("X_PINWIRE0")));
			BEST_LOGIC_OUTS0.getPIPs().addAll(FinishRoute);
	       
			return FinalSliceX0Y0;
	}
	
	
	
	private ArrayList<Pip> CompleteDesign(int num, Virtex4CLB TestSite, Virtex4Slice FinalSlice, Tile IntTile, Tile CLBTile, Design design, FPGA fpga, V4XilinxToolbox TB) {
		
		String SinkWire  = "BX_PINWIRE0";
	    boolean CompletedRoute = true;
	    
	    Virtex4SwitchBox SB = new Virtex4SwitchBox(fpga, design,TB);
		SB.setTiles(IntTile, CLBTile);	
		Virtex4SwitchBoxRouter SBR = new Virtex4SwitchBoxRouter(SB);
		SBR.setTiles(IntTile, CLBTile);		
		WireEnumerator We = design.getWireEnumerator(); 
	        
		ArrayList<Pip> PipsOfInterest = new ArrayList<Pip>();
		//Start From: 
		//BEST_LOGIC_OUTS0 -> Origin is X_PINWIRE0 SLICEX0Y0
		//BEST_LOGIC_OUTS1 -> Origin is X_PINWIRE2 SLICEX1Y0
		//BEST_LOGIC_OUTS6 -> Origin is Y_PINWIRE1 SLICEX0Y1
		//BEST_LOGIC_OUTS7 -> Origin is Y_PINWIRE3 SLICEX1Y1
	   Pin SinkPin = new Pin(false, "BX", FinalSlice.getXDLInstance());
		//Create a Route from each of the Previous Wires to the To the Wire of Interest
       HashMap<Integer, ArrayList<PIP>> Short0 = SBR.CreateDistantShort(We.getWireEnum("X_PINWIRE0"), We.getWireEnum("X_PINWIRE1"), We.getWireEnum(DestName));
       HashMap<Integer, ArrayList<PIP>> Short1 = SBR.CreateDistantShort(We.getWireEnum("Y_PINWIRE2"), We.getWireEnum("Y_PINWIRE3"), We.getWireEnum(DestName));
               
       Net BEST_LOGIC_OUTS0 = new Net("BEST_LOGIC_OUTS0"+IntTile.toString(), NetType.WIRE);
       Net BEST_LOGIC_OUTS1 = new Net("BEST_LOGIC_OUTS1"+IntTile.toString(), NetType.WIRE);
       Net BEST_LOGIC_OUTS6 = new Net("BEST_LOGIC_OUTS6"+IntTile.toString(), NetType.WIRE);
       Net BEST_LOGIC_OUTS7 = new Net("BEST_LOGIC_OUTS7"+IntTile.toString(), NetType.WIRE);         
       
       ArrayList<PIP> FinishRoute = SBR.LongBFS(We.getWireEnum(DestName),  We.getWireEnum("BX_PINWIRE0"), IntTile, design.getDevice());
       
       //Add Sink Pin 
       BEST_LOGIC_OUTS0.addPin(SinkPin);
       BEST_LOGIC_OUTS1.addPin(SinkPin);
       BEST_LOGIC_OUTS6.addPin(SinkPin);
       BEST_LOGIC_OUTS7.addPin(SinkPin);
        	
       //Add Start Pins 
       BEST_LOGIC_OUTS0.addPin(new Pin(true, "X", TestSite.getSLICEX0Y0().getXDLInstance()));
       BEST_LOGIC_OUTS1.addPin(new Pin(true, "X", TestSite.getSLICEX1Y0().getXDLInstance()));
       BEST_LOGIC_OUTS6.addPin(new Pin(true, "Y", TestSite.getSLICEX0Y1().getXDLInstance()));
       BEST_LOGIC_OUTS7.addPin(new Pin(true, "Y", TestSite.getSLICEX1Y1().getXDLInstance()));
        	
       //Add Initial PIPs
       BEST_LOGIC_OUTS0.getPIPs().addAll(Short0.get(We.getWireEnum("X_PINWIRE0"))); 	
       BEST_LOGIC_OUTS1.getPIPs().addAll(Short0.get(We.getWireEnum("X_PINWIRE1")));
       BEST_LOGIC_OUTS6.getPIPs().addAll(Short1.get(We.getWireEnum("Y_PINWIRE2")));
       BEST_LOGIC_OUTS7.getPIPs().addAll(Short1.get(We.getWireEnum("Y_PINWIRE3")));
        	
       //Complete Route PIPs
       
       BEST_LOGIC_OUTS0.getPIPs().addAll(FinishRoute);
       BEST_LOGIC_OUTS1.getPIPs().addAll(FinishRoute);
       BEST_LOGIC_OUTS6.getPIPs().addAll(FinishRoute);
       BEST_LOGIC_OUTS7.getPIPs().addAll(FinishRoute);
       
       SB.ActivatePIPs(BEST_LOGIC_OUTS0.getPIPs());
       SB.ActivatePIPs(BEST_LOGIC_OUTS1.getPIPs());
       SB.ActivatePIPs(BEST_LOGIC_OUTS6.getPIPs());
       SB.ActivatePIPs(BEST_LOGIC_OUTS7.getPIPs());
       
       //Add the Nets to the Design
       design.addNet(BEST_LOGIC_OUTS0);
       design.addNet(BEST_LOGIC_OUTS1);
       design.addNet(BEST_LOGIC_OUTS6);
       design.addNet(BEST_LOGIC_OUTS7);
       
       if( VerifyNet(BEST_LOGIC_OUTS0, SB) && VerifyNet(BEST_LOGIC_OUTS1, SB) &&
           VerifyNet(BEST_LOGIC_OUTS6, SB) && VerifyNet(BEST_LOGIC_OUTS7, SB)){
    	   System.out.println("All Added Nets Verified!!");
       } else {
    	   System.out.println("A Net did not Verify.");
    	   System.exit(1);
       }
       //design.saveXDLFile("Debug.xdl");
       //FileConverter.convertXDL2NCD("Debug.xdl");
       AddPipsOfInterst(PipsOfInterest, BEST_LOGIC_OUTS0);
       AddPipsOfInterst(PipsOfInterest, BEST_LOGIC_OUTS1);
       AddPipsOfInterst(PipsOfInterest, BEST_LOGIC_OUTS6);
       AddPipsOfInterst(PipsOfInterest, BEST_LOGIC_OUTS7);
        	
       if(PipsOfInterest.size() > 4){
       	System.out.println("To many Pips were added");
        System.exit(1);
       }
        	
       //Order the pips in PipsOfInterest such that i0, i1, i2, i3 are found at indexes 0,1,2,3 respectively
       OrderPips(PipsOfInterest);
            
       Pip i0 = PipsOfInterest.get(0);
       Pip i1 = PipsOfInterest.get(1);
       Pip i2 = PipsOfInterest.get(2);
       Pip i3 = PipsOfInterest.get(3);
       
       //design.saveXDLFile("Debug.xdl");
       //FileConverter.convertXDL2NCD("Debug.xdl");
       //Set the Lut Contents Correctly
       SetOriginLutContents( i0, "Assert", TestSite.getSLICEX0Y0(), TestSite.getSLICEX0Y1(), TestSite.getSLICEX1Y0(), TestSite.getSLICEX1Y1(), design);
       SetOriginLutContents( i1, "Clear",  TestSite.getSLICEX0Y0(), TestSite.getSLICEX0Y1(), TestSite.getSLICEX1Y0(), TestSite.getSLICEX1Y1(), design);
       SetOriginLutContents( i2, "Clear",  TestSite.getSLICEX0Y0(), TestSite.getSLICEX0Y1(), TestSite.getSLICEX1Y0(), TestSite.getSLICEX1Y1(), design);
       SetOriginLutContents( i3, "Assert", TestSite.getSLICEX0Y0(), TestSite.getSLICEX0Y1(), TestSite.getSLICEX1Y0(), TestSite.getSLICEX1Y1(), design);    	
        	        	
       return PipsOfInterest; 
       
	}


	public boolean VerifyNet(Net N, Virtex4SwitchBox SB) {
			
		for(PIP P: N.getPIPs() ){
			if(!SB.VerifyPIP(P, 1)){
				return false;
			}
		}
		
		return true; 
	}

	private void SetOriginLutContents(Pip i, String Cmd, Virtex4Slice SliceX0Y0, Virtex4Slice SliceX0Y1,Virtex4Slice SliceX1Y0, Virtex4Slice SliceX1Y1, Design design) {
        
		Virtex4Slice S;  
		Virtex4Lut   L = null; 
		
		Net N = findNet(i, design);
		S = SelectSource(N, SliceX0Y0,SliceX0Y1,SliceX1Y0,SliceX1Y1);
		
		if(N.getSource().getName().equals("X")){
		   L = S.getLutF();
		   S.setXUsedAttribute("0");
		} else if(N.getSource().getName().equals("Y")){
		   L = S.getLutG();
		   S.setYUsedAttribute("0");
		} 
		
		if(L != null) {
			if(Cmd.equals("Assert")){
				L.AssertLutContent(); 
			} else if (Cmd.equals("Clear")) {
				L.ClearLutContents(); 
			} else {
				System.out.println("Invalid Cmd for SetOrigin Lut Contents");
				System.exit(1); 
			}
		} else {
			System.out.println("L not set!");
			System.exit(1);
		}	

		
	}

	private Virtex4Slice SelectSource(Net N, Virtex4Slice SliceX0Y0, Virtex4Slice SliceX0Y1, Virtex4Slice SliceX1Y0,Virtex4Slice SliceX1Y1) {
		
		int X = N.getSource().getInstance().getInstanceX();
		int Y = N.getSource().getInstance().getInstanceY();
		
        X = X%2; 
        Y = Y%2;
	    if(X==0 && Y==0){
	    	return SliceX0Y0; 
	    } else if(X==0 && Y==1){
	    	return SliceX0Y1; 
	    } else if(X==1 && Y==0){
	    	return SliceX1Y0;
	    } else if(X==1 && Y==1){
	    	return SliceX1Y1; 
	    } 
		  
		System.out.println("Net Does Not Map to any of the Selected Sources");
		System.exit(1);
		return null;
	}

	private Net findNet(Pip P, Design design) {
        //This function searches for the Net in the Design that contains the Pip P and Returns it.
		PIP p = P.getPip(); 
		
		for(Net N : design.getNets()){
			for(PIP Q : N.getPIPs()){
				//System.out.println("Comparing " + p.toString(design.getWireEnumerator()) + " " + Q.toString(design.getWireEnumerator()));
				if(Q.equals(p)){
					//System.out.println("Found p-Q Match!");
					return N; 
				}
			}
		}
		System.out.println("findNet: The Correct Net Was Not Found!");
		System.exit(1);
		return null;
	}
	
	public boolean isBufferBit(Bit B){
		Bit BB = Table.getPip(0,0).getBufferBit();
		if(B != null && BB != null && B.equals(BB)){
			return true; 
		}
		return false; 
	}
	
	private void OrderPips(ArrayList<Pip> PipsOfInterest) {
        
		//The Pips are ordered making the assumption that the Row and Column bits are correct.
		//Assign the First Pip to be i0
		Pip i0 = PipsOfInterest.get(0);
        Pip i1=null,i2=null,i3=null; 
		
		for(Pip P : PipsOfInterest){
			if(!P.getRowBit().equals(i0.getRowBit()) && !P.getColumnBit().equals(i0.getColumnBit())){
				//Assign the Pip With no Matchings Bits to be i3
                i3 = P; 	
			} else if(P.getRowBit().equals(i0.getRowBit()) || !P.getColumnBit().equals(i0.getColumnBit())){
				//Assign the Pip with the Matching Row Bit to i1
				i1 = P; 
			}  else if(!P.getRowBit().equals(i0.getRowBit()) || P.getColumnBit().equals(i0.getColumnBit())){
				//Assign the Pip with the Matching Row Bit to i1
				i2 = P; 
			}  
		}	
		
		if(i0 == null || i1 == null || i2 == null || i3 == null){
			System.out.println("Error: i0, i1, i2, i3 not set" + i0 + " " + i1 + " " + i2 + " " + i3);
			System.exit(1);
		}
		
		//Arrange the Pips into the ArrayList
        PipsOfInterest.set(0, i0);
        PipsOfInterest.set(1, i1);
        PipsOfInterest.set(2, i2);
        PipsOfInterest.set(3, i3);
	}
    public void TransposeTable(){
    	Table.TransposeTable(); 
    }
    
    public void PrintTable(){
    	if (this.Table != null)
		{
    		Table.printTable(); 
		}
    	else
    	{
    		System.out.println("Nothing returned!");
    	}
    }
	private void AddPipsOfInterst(ArrayList<Pip> PipsOfInterest, Net N) {
       	
		for(PIP P: N.getPIPs()){
       		if(P.getEndWire() == Table.getDestWire()){
       			PipsOfInterest.add(Table.getPip(P.getStartWire()));
       			Table.SetTile(P.getTile());
       		}
       	}
	}
    public int compareTo(Object other){
    	WireMux O = (WireMux) other;
    	return this.getDestName().compareTo(O.getDestName());
    }
	public void SerializeTable(ObjectOutputStream out) {
		// TODO Auto-generated method stub
		
	}
	
	public void GroundLutInputs(Virtex4Slice Slice, Net GroundNet, String ForGorBoth){
		
		if(GroundNet != null && Slice != null){
			if(ForGorBoth.equals("F") || ForGorBoth.equals("Both")){
				for(int i = 1; i <=4; i++)
				  GroundNet.addPin(new Pin(false, "F"+i, Slice.getXDLInstance()));	
			}
            
            if(ForGorBoth.equals("G") || ForGorBoth.equals("Both")){
            	for(int i = 1; i <=4; i++)
  				  GroundNet.addPin(new Pin(false, "G"+i, Slice.getXDLInstance()));
			}
		}
	}
}

package edu.byu.ece.gremlinTools.ReliabilityTools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;
import java.util.StringTokenizer;

import edu.byu.ece.gremlinTools.Virtex4Bits.Bit;
import edu.byu.ece.gremlinTools.Virtex4Bits.BitNumberType;
import edu.byu.ece.gremlinTools.Virtex4Bits.V4ConfigurationBit;
import edu.byu.ece.gremlinTools.bitstreamDatabase.BitstreamDB;
import edu.byu.ece.gremlinTools.xilinxToolbox.V4XilinxToolbox;
import edu.byu.ece.gremlinTools.xilinxToolbox.XilinxToolboxLookup;
import edu.byu.ece.rapidSmith.bitstreamTools.configuration.FPGA;
import edu.byu.ece.rapidSmith.bitstreamTools.configuration.FrameAddressRegister;
import edu.byu.ece.rapidSmith.bitstreamTools.configurationSpecification.DeviceLookup;
import edu.byu.ece.rapidSmith.design.Design;

public class ReliabilityAnalyzer {
    protected Design design; 
    protected FPGA fpga; 
    protected V4XilinxToolbox TB; 
    
    
    protected BitstreamDB DB; 
    
    protected InterconnectAnalyzer IA; 
    protected LogicAnalyzer LA; 
    protected BitSensitivityArea BSA; 
    
    public ReliabilityAnalyzer(String XDLFilename) {
    	_xdlFile = XDLFilename;
    	System.out.println("Loading Database...");
    	design = new Design(); 
    	design.loadXDLFile(XDLFilename);
    	
    	fpga = new FPGA(DeviceLookup.lookupPartV4V5V6("XC4VFX60")); 
		TB = (V4XilinxToolbox) XilinxToolboxLookup.toolboxLookup(fpga.getDeviceSpecification());
		
		IA = new InterconnectAnalyzer(design, fpga, TB);
		LA = new LogicAnalyzer(design, fpga, TB); 		
		
		BSA = new BitSensitivityArea(design, fpga, TB, LA.getSliceX0Y0(), LA.getSliceX0Y1(), LA.getSliceX1Y0(), LA.getSliceX1Y1());
    }
    
    public void reportDesignArea(){
    	BSA.reportArea(); 
   }
    
    public ArrayList<V4ConfigurationBit> AnalyzeBits(Collection<V4ConfigurationBit> Bits){
    	
    	ArrayList<V4ConfigurationBit> RemainingBits; 
    	
    	RemainingBits = IA.AnalyzeBits(Bits); 
    	System.out.println("After Interconnect " + RemainingBits.size());
    	RemainingBits = LA.AnalyzeBits(RemainingBits);
    	System.out.println("After Logic " + RemainingBits.size());
    	System.out.println("Checking Remaing Bits against database... + ");
        int remainingBitsInDataBase = checkRemainingBits(RemainingBits);
    	System.out.println("Remaining Bits Found in Database: " + remainingBitsInDataBase);
    	return RemainingBits; 
    }
    
    public ArrayList<V4ConfigurationBit> parseBitList(String Filename){
    	
    	ArrayList<V4ConfigurationBit> Bits = new ArrayList<V4ConfigurationBit>(); 
    	 int dutbits=0;
		 int icapbits=0;
		 int allbits =0;
    	final File fFile = new File(Filename);
		
    	Scanner s = null;
		try {
			s = new Scanner(fFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try{
			int i=0;
			
			 while(s.hasNextLine()){
			     
			   //	System.out.println(s.nextLine());	
				// System.out.println("Line: " + i);
			   i++;
			   
			       if(s.hasNext() == false)
			    	   break;
			        //if(i==2) System.out.println("Loop2 printing line: " + s.nextLine());
			        //while(s.next().isEmpty());
			       
		    		Scanner FarLine = new Scanner(s.nextLine());
		    		String d = s.nextLine();
		    		Scanner DutLine = new Scanner(d);
			   		Scanner IcapLine= new Scanner(s.nextLine());
			   		Scanner AllLine = new Scanner(s.nextLine());
			   		//System.out.println(d);			   		
			   		//System.out.println(FarLine.next()); //"Address:"
			   		//System.out.println(FarLine.next()); //"Address:"
			   		FarLine.next();
			   		FarLine.next();
			   		FrameAddressRegister FAR = new FrameAddressRegister(fpga.getDeviceSpecification());
			   		FAR.setFAR(FarLine.nextInt(16));
			   		FarLine.close();
			   		  
			   		//System.out.println(DutLine.next()); //"Sensitivities"
			   		//System.out.println(DutLine.next());
			   		DutLine.next();
			   		DutLine.next();
			   		
			   		while(DutLine.hasNext())
			   		{
			   			//SensitivityType.DESIGN_UNDER_TEST
			   			//FailedBits.add(new BitInfo(DutLine.nextInt(), , FAR.getAddress(), fpga.getDeviceSpecification(), toolbox));
			   		   	
			   			Bit B = new Bit(FAR.getAddress(),DutLine.nextInt(), BitNumberType.Gremlin);
			   			IdentifyMaskOffSet(B, FAR, TB);
			   			if(FAR.getBlockType() != 2){
			   			 Bits.add(new V4ConfigurationBit(fpga, B, FAR.getAddress(), TB, design.getDevice()));
			   			 dutbits++; 
			   			}
			   		   
			   		}
			   		DutLine.close();
			   		   
			   		//System.out.println(IcapLine.next());
			   		//System.out.println(IcapLine.next());
			   		IcapLine.next();
			   		IcapLine.next();
			   		while(IcapLine.hasNext())
			   		{
			   				
				   			Bit B = new Bit(FAR.getAddress(),IcapLine.nextInt(), BitNumberType.Gremlin);
				   			IdentifyMaskOffSet(B, FAR, TB);
				   		  if(FAR.getBlockType() != 2){
				   			Bits.add(new V4ConfigurationBit(fpga, B, FAR.getAddress(), TB, design.getDevice()));
			   			    icapbits++;
				   		  }
			   			//FailedBits.add(new BitInfo(IcapLine.nextInt(), SensitivityType.ICAP, FAR.getAddress(), fpga.getDeviceSpecification(), toolbox));
			   			//tmp.addBit(IcapLine.nextInt(), SensitivityType.ICAP, fpga.getDeviceSpecification(), toolbox);
			   		}
			   		IcapLine.close();
			   		
			   		//System.out.println("All Line Info");
			   		//System.out.println(AllLine.next());
			   		//System.out.println(AllLine.next());
			   		//System.out.println(AllLine.hasNext());
			   		AllLine.next();
			   		AllLine.next();
			   		while(AllLine.hasNextInt())
			   		{
				   		
				   			Bit B = new Bit(FAR.getAddress(),AllLine.nextInt(), BitNumberType.Gremlin);
				   			IdentifyMaskOffSet(B, FAR, TB);
				   			if(FAR.getBlockType() != 2){
				   			  Bits.add(new V4ConfigurationBit(fpga, B, FAR.getAddress(), TB, design.getDevice()));
				   			  allbits++;
				   			}
				   		//Bits.add(new V4ConfigurationBit(fpga, new Bit(FAR.getAddress(),AllLine.nextInt(), BitNumberType.Gremlin),  TB, design.getDevice()));
			   			//FailedBits.add(new BitInfo(AllLine.nextInt(), SensitivityType.ALL, FAR.getAddress(), fpga.getDeviceSpecification(), toolbox));
			   			//tmp.addBit(AllLine.nextInt(), SensitivityType.ALL, fpga.getDeviceSpecification(), toolbox);
			   		}
			   		AllLine.close();
			   		 		   
			   				  		
			   		if(s.hasNext()) s.nextLine();
			   	} 
		   } finally{
			   s.close();
		   }
		   System.out.println("Dut Bits " + dutbits);
		   System.out.println("Icap Bits " + icapbits);
		   System.out.println("All Bits " + allbits);
		   return Bits;
    	
		
    }
    
    protected void IdentifyMaskOffSet(Bit B, FrameAddressRegister FAR, V4XilinxToolbox toolbox)
	{
	   boolean isClkColumn = toolbox.isClkColumn(fpga.getDeviceSpecification(), FAR);
       
	   B.setMask(toolbox.getMask(FAR.getTopBottom(), B.getBitNumber(), isClkColumn));
       B.setOffset(toolbox.getByteOffset(FAR.getTopBottom(), B.getBitNumber(), isClkColumn));
      
	}
    
    
    //This function checks to see if the remaining bits are actually in the database
    public static int LookUpQueryCountResults(BitstreamDB DB, String Query){
    	//System.out.println(Query);
    	ResultSet rs = DB.makeQuery(Query);
        int numResults=0;
    	try {
			while(rs.next()){
				//System.out.println(rs.getString(1) + " " + rs.getString(2) + " " + rs.getString(3) + " " + rs.getString(4) + " " + rs.getString(5) + " " + rs.getString(6) + " " + rs.getString(7) + " " + rs.getString(8));
				numResults++; 
				//System.out.println("Bits.add(new Bit(" +  rs.getString(5) + ", " + rs.getString(6) + ", " + rs.getString(7) + "));");
			}
			rs.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return numResults;
    }
    
    protected boolean BitInDataBase(V4ConfigurationBit B){
    	int MinorAddr = B.getMinorAddress(); 
    	int Offset = B.getOffset(); 
    	int Mask = B.getMask(); 
    	String Query = "SELECT * FROM " + "Virtex4Bits WHERE MinorAddr = \"" + MinorAddr + "\" " 
    	                                + "AND Offset = \"" + Offset + "\"" + " AND Mask = \"" + Mask +"\"";
    	//System.out.println("numResults " + LookUpQueryCountResults(DB, Query));
    	//System.out.println(B.getTile());
    	int routing = LookUpQueryCountResults(DB, Query);
    	
    	Query = "SELECT * FROM " + "Virtex4LogicBits WHERE MinorAddr = \"" + MinorAddr + "\" " 
        + "AND Offset = \"" + Offset + "\"" + " AND Mask = \"" + Mask +"\"";
    	
    	int logic = LookUpQueryCountResults(DB, Query);
    	
    	if((routing+logic) == 0){
    		return false;
    	}
    	
    	return true; 
    	 
    }
    
    public int checkRemainingBits(ArrayList<V4ConfigurationBit> Bits){
    	DB = new BitstreamDB(); 
    	DB.openConnection();
    	
    	int remainingBitsInDatabase=0;
    	for(V4ConfigurationBit B : Bits){
    		if(BitInDataBase(B)){
    			remainingBitsInDatabase++;
    		}
    	}
    	
    	return remainingBitsInDatabase;
    }
    public ArrayList<V4ConfigurationBit> parseNathan(String Filename){
    	String line=null;
    	ArrayList<V4ConfigurationBit> Bits = new ArrayList<V4ConfigurationBit>();
    	try {
			BufferedReader br = new BufferedReader(new FileReader(Filename));
			while((line=br.readLine())!= null){
				StringTokenizer st = new StringTokenizer(line);
				int far = Integer.parseInt(st.nextToken(), 16);
				int bitnum = Integer.parseInt(st.nextToken());
				FrameAddressRegister FAR = new FrameAddressRegister(fpga.getDeviceSpecification(), far);
				Bit B = new Bit(far,bitnum, BitNumberType.Gremlin);
	   			IdentifyMaskOffSet(B, FAR, TB);
	   			if(FAR.getBlockType() != 2){
		   			  Bits.add(new V4ConfigurationBit(fpga, B, FAR.getAddress(), TB, design.getDevice()));	
		   		}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    		
    	return Bits; 
    }
    
    public void writeNathanReportToFile(String NetReportFilename, String InstanceReportFilename){
    	 IA.writeNathanReport(NetReportFilename);
    	 LA.writeNathanReport(InstanceReportFilename);
    	 
    }
    
    public static void main(String args []) {
    	_parseArgs(args);
    	ReliabilityAnalyzer RA = new ReliabilityAnalyzer(_xdlFile);
        
        System.out.println("Parsing Sensitive Bitlist...");
        //ArrayList<V4ConfigurationBit> Bits = RA.parseNathan(_farFile); 
        ArrayList<V4ConfigurationBit> Bits = RA.parseBitList(_farFile); 
        System.out.println("Total Bits: " + Bits.size());
        System.out.println("Analyzing Routing Failures...");
        ArrayList<V4ConfigurationBit> BitsNotAnalyzed = RA.AnalyzeBits(Bits);
        System.out.println("Remaining Bits: " + BitsNotAnalyzed.size());
        RA.printReports(); 
        RA.writeNathanReportToFile(_netoutFile, _instoutFile);
        RA.reportDesignArea();
    }

	
    public void printReports() {
		IA.printReport();
		LA.printReport(); 
	}
    
	/**
	 * Parses the command-line arguments
	 * 
	 * @param args
	 */	
	protected static void _parseArgs(String[] args) {
		if (args.length < 2) {
			System.out.println(_usage());
			System.exit(-1);
		}
		
		if (!args[0].contains(".xdl")) {
			System.out.println("XDL file must be first");
			System.out.println(_usage());
			System.exit(-1);
		}
		_xdlFile = args[0];
		_farFile = args[1];
		
		for (int i = 2; i < args.length; i++) {
			if (args[i].equals("-h") || args[i].equals("-help")) {
				System.out.println(_usage());
				System.exit(0);
			} else if (args[i].equals("-oisnt")) {
				i++;
				_instoutFile = args[i];
			} else if (args[i].equals("-onet")) {
				i++;
				_netoutFile = args[i];
			} else {
				System.out.println(_usage());
				System.exit(-1);				
			}
		}
	}

	protected static String _usage() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("ReliabilityAnalyzer Usage: java edu.byu.ece.gremlinTools.ReliabilityTools.ReliabilityAnalyzer <xdl file> <xrtc file> [options]\n");
		sb.append("options:\n");
		sb.append("\t-h | -help \t Print this message\n");
		sb.append("\t-onet <filename> \t Specify the output file name for nets.\n");
		sb.append("\t-oinst <filename> \t Specify the output file name for instances.\n");
		sb.append("\n");
		
		return sb.toString();
	}	
	
	protected static String _farFile;
	protected static String _xdlFile;
	protected static String _instoutFile = "instresults.dat";
	protected static String _netoutFile = "netresults.dat";
}

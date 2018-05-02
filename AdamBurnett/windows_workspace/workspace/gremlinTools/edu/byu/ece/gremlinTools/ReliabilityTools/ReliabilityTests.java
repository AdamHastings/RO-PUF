package edu.byu.ece.gremlinTools.ReliabilityTools;

import java.util.ArrayList;

import edu.byu.ece.gremlinTools.Virtex4Bits.V4ConfigurationBit;

public class ReliabilityTests {
   public static void main(String [] args){
	   
	   String XDLFilename = "Z:\\Drop_Box\\nathanStuffDWC.tar\\nathanStuffDWC\\dut_basic_routed.xdl";
	   String FarFilename = "Z:\\Drop_Box\\nathanStuffDWC.tar\\nathanStuffDWC\\farbits.tmp";
	   String NetOutFile  = "Z:\\Drop_Box\\nathanStuffDWC.tar\\nathanStuffDWC\\farbitsDWCInstanceResults.txt";
	   String instOutFile = "Z:\\Drop_Box\\nathanStuffDWC.tar\\nathanStuffDWC\\farbitsDWCNetResults.txt";
	   
	   ReliabilityAnalyzer RA = new ReliabilityAnalyzer(XDLFilename);
       
       System.out.println("Parsing Sensitive Bitlist...");
       ArrayList<V4ConfigurationBit> Bits = RA.parseNathan(FarFilename); 
      // ArrayList<V4ConfigurationBit> Bits = RA.parseBitList(BitListFilename); 
       System.out.println("Total Bits: " + Bits.size());
       System.out.println("Analyzing Routing Failures...");
       ArrayList<V4ConfigurationBit> BitsNotAnalyzed = RA.AnalyzeBits(Bits);
       System.out.println("Remaining Bits: " + BitsNotAnalyzed.size());
       RA.printReports(); 
       RA.writeNathanReportToFile(NetOutFile, instOutFile);
   }
}

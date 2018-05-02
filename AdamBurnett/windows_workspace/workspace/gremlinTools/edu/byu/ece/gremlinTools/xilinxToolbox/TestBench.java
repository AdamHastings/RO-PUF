package edu.byu.ece.gremlinTools.xilinxToolbox;

import java.io.FileNotFoundException;

import edu.byu.ece.gremlinTools.bitstreamDatabase.BitstreamDB;
import edu.byu.ece.rapidSmith.design.Design;

public class TestBench {

	public static void main(String[] args) throws FileNotFoundException{
		Design D = new Design();
        D.loadXDLFile(args[0]);
		D.saveXDLFile(("outputXDL.xdl"));
		
		XDLManipulator.removeFakePIPs(D, new BitstreamDB());
		D.saveXDLFile(("outputXDL_fakePIPsRemoved.xdl"));
		
		XDLManipulator.unrouteDesign(D);
		D.saveXDLFile(("outputXDL_unrouted.xdl"));
	}
}

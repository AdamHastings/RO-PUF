package edu.byu.ece.gremlinTools.xilinxToolbox;

import java.util.ArrayList;

import edu.byu.ece.gremlinTools.bitstreamDatabase.BitstreamDB;
import edu.byu.ece.rapidSmith.bitstreamTools.configurationSpecification.DeviceLookup;
import edu.byu.ece.rapidSmith.design.Design;
import edu.byu.ece.rapidSmith.design.Net;
import edu.byu.ece.rapidSmith.design.PIP;

public class XDLManipulator {
	
	public static Design removeFakePIPs(Design design, BitstreamDB db){
		db.openConnection();
		ArrayList<PIP> pipsToRemove = new ArrayList<PIP>();
		String familyName = DeviceLookup.lookupPartV4V5V6(DeviceLookup.getRootDeviceName(design.getDevice().getPartName())).getDeviceFamily();
		for(Net net : design.getNets()){
			pipsToRemove.clear();
			for(PIP pip : net.getPIPs()){
				if(!db.containsPIP(pip,familyName)){
					pipsToRemove.add(pip);
				}
			}
			net.getPIPs().removeAll(pipsToRemove);
		}
		db.closeConnection();
		return design;
	}
	
	public static Design unrouteDesign(Design design){
		for(Net net : design.getNets()){
			net.getPIPs().clear();
		}
		return design;
	}
}

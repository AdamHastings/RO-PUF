import java.io.File;
import java.util.ArrayList;

import edu.byu.ece.rapidSmith.bitstreamTools.configuration.FPGA;
import edu.byu.ece.rapidSmith.design.Design;
import edu.byu.ece.rapidSmith.design.Instance;
import edu.byu.ece.rapidSmith.design.Net;
import edu.byu.ece.rapidSmith.device.Device;
import edu.byu.ece.rapidSmith.device.PrimitiveSite;
import edu.byu.ece.rapidSmith.device.PrimitiveType;
import edu.byu.ece.rapidSmith.device.Tile;
import edu.byu.ece.rapidSmith.util.FileConverter;
import edu.byu.ece.rapidSmith.util.FileTools;

/*
 * This class places the hard macro tiles evenly within a specified range.
 * This is used because Xilinx does not allow RLOC constraints on hard
 * macros.
 */
public class DACPlacer {
	Design design;
	public String hardMacroName;
	public int CLB_X_LIMIT, CLB_Y_LIMIT;

	int x_index;
	int y_index;
	int x_space;
	int y_space;
	
	static int hmCount = 0;
//	Device dev;
	
	public DACPlacer() {
		design = new Design();
//		dev = FileTools.loadDevice("XC4VLX60");
	}
	
	public void loadDesign(String xdlFilename) {
		File f = new File("xdlFile");
		//check if the xdlfile already exists for project, generate it otherwise
		if(!f.exists())
		{
			String ncdFilename = xdlFilename.substring(0, xdlFilename.lastIndexOf("."));
			ncdFilename = ncdFilename.concat(".ncd");
//			System.out.println(ncdFilename);
			FileConverter.convertNCD2XDL(ncdFilename);
		}
    	design.loadXDLFile(xdlFilename);
	}
	
	public void unplaceAll() {
		ArrayList<Instance> instanceList = new ArrayList<Instance>();
		instanceList.addAll(design.getInstances());
		for(Instance i : instanceList) {
			i.unPlace();
		}
	}
	
	public void unrouteAll() {
		ArrayList<Net> netList = new ArrayList<Net>();
		netList.addAll(design.getNets());
		for(Net n : netList) {
			n.unroute();
		}
	}
	
	public void placeHardMacros() {
		//collect all hard macro instances
		ArrayList<Instance> hmInstances = new ArrayList<Instance>();
		for(Instance i : design.getInstances()) {
			if(i.getName().contains(hardMacroName))
				hmInstances.add(i);
		}
		
//		//debug statements
//		for(Instance i : hmInstances) {
//			System.out.println(i.getName() + "\tX:" + i.getInstanceX() + "\tY:" + i.getInstanceY() + "\t" + i.getPrimitiveSiteName());
//			PrimitiveSite ps = i.getPrimitiveSite();
//			System.out.println("\t" + ps.getType() + "\t" + ps.getTile().getName());
//			for(PrimitiveSite ps2 : ps.getTile().getPrimitiveSites()) {
//				System.out.println("\t\t" + ps2.getInstanceX() + ":" + ps2.getInstanceY() + "\t" + ps2.toString() + "\t" + ps2.getType());
//			}
//		}
		
		//place short hard macros spaced out from each other until we run out
		//of hard macros to place
		for(Instance inst : hmInstances) {
			Tile clbTile = getNextClbTileForPlacement();
			System.out.println(inst.getName() + "\t" + clbTile.getName());
			System.out.println("Placing macro instance...");
			placeMacroInstance(inst, clbTile);
			hmCount++;
			System.out.println("done");
		}
	}
	
	private Tile getNextClbTileForPlacement() {
		String tileName = "CLB_X" + x_index + "Y" + y_index;
//		System.out.println("\t\ttileName: " + tileName);
		Tile clbTile = design.getDevice().getTile(tileName);
		
		boolean findAnotherTile = true;
		while(findAnotherTile) {
			if(y_index < 0) {
				x_index = CLB_X_LIMIT;
				y_index = CLB_Y_LIMIT;
			}
			else if(x_index < 0) {
				x_index = CLB_X_LIMIT;
				y_index-=y_space;
			}
			else {
				x_index-=x_space;
			}
			tileName = "CLB_X" + x_index + "Y" + y_index;
			clbTile = design.getDevice().getTile(tileName);
			// check that none of the slices are used in this CLB tile
			boolean allSlicesUnoccupied = true;
			if(clbTile != null) {
				for(PrimitiveSite ps : clbTile.getPrimitiveSites()) {
					if(design.isPrimitiveSiteUsed(ps)) {
						allSlicesUnoccupied = false;
					}
				}
			}
			
			if(clbTile != null && allSlicesUnoccupied)
				findAnotherTile = false;
		}
//		//make sure that all of the slices are unoccupied in this tile
//		PrimitiveSite[] primitive_sites = clbTile.getPrimitiveSites();
//		for(PrimitiveSite ps : primitive_sites) {
//			if(design.isPrimitiveSiteUsed(ps)) {	//get another tile
//				clbTile = getNextClbTileForPlacement();
//			}
//		}
		return clbTile;
	}
	
	private void placeMacroInstance(Instance inst, Tile clbTile) {
		PrimitiveSite slice_x0y0 = getOriginSlice(clbTile);
		inst.place(slice_x0y0);
		//System.out.println("Instance placed");
		String baseName = inst.getName().substring(0, inst.getName().lastIndexOf("_")+1);
		//System.out.println("Got base name");
		Instance x0y1 = design.getInstance(baseName + "1");
		//System.out.println("Got instance");
//		System.out.println("name: " + x0y1.getName());
			x0y1.place(design.getDevice().getPrimitiveSite("SLICE_X" + slice_x0y0.getInstanceX() + "Y" + (slice_x0y0.getInstanceY()+1)));
		Instance x1y0 = design.getInstance(baseName + "2");
		System.out.println("Place 2");
			x1y0.place(design.getDevice().getPrimitiveSite("SLICE_X" + (slice_x0y0.getInstanceX()+1) + "Y" + slice_x0y0.getInstanceY()));
		Instance x1y1 = design.getInstance(baseName + "3");
		System.out.println("Place 3");
			x1y1.place(design.getDevice().getPrimitiveSite("SLICE_X" + (slice_x0y0.getInstanceX()+1) + "Y" + (slice_x0y0.getInstanceY()+1)));
		System.out.println("Placed!");
	}
	
	private PrimitiveSite getOriginSlice(Tile clbTile) {
		for(PrimitiveSite slice : clbTile.getPrimitiveSites()) {
//			System.out.println("\tslice: " + slice.getName() + "\t" + slice.getType());
			if(getSliceRelativeY(slice) == 0) {
				if(getSliceRelativeX(slice) == 0) {
					return slice;
				}
			}
		}
		return null;
	}

	private int getSliceRelativeX(PrimitiveSite slice) {
		PrimitiveType type = slice.getType();
		if(!(type.equals(PrimitiveType.SLICE) || type.equals(PrimitiveType.SLICEL) || type.equals(PrimitiveType.SLICEM))) {
			System.err.println("Error @ getSliceRelativeX(PrimitiveSite slice): PrimitiveSite is not a SLICE!");
			System.exit(1);
		}
		
		return slice.getInstanceX()%2;
	}
	private int getSliceRelativeY(PrimitiveSite slice) {
		PrimitiveType type = slice.getType();
		if(!(type.equals(PrimitiveType.SLICE) || type.equals(PrimitiveType.SLICEL) || type.equals(PrimitiveType.SLICEM))) {
			System.err.println("Error @ getSliceRelativeY(PrimitiveSite slice): PrimitiveSite is not a SLICE!");
			System.exit(1);
		}
		
		return slice.getInstanceY()%2;
	}
	
	public void setHardMacroName(String hardMacroName) {
		this.hardMacroName = hardMacroName;
	}

	private void writeDesign(String fileName) {
		design.saveXDLFile(fileName);
		FileConverter.convertXDL2NCD(fileName);
	}
	
	public static void main(String[] args) {
//		String path = "C:\\Users\\danny\\Desktop\\thesis\\thesisDesigns\\ultimates\\short_design_basepowerlike_srl\\";
		String path = args[0];
		String filename = args[1];
		String xdlFilename = path + filename + ".xdl";
		//String hardMacroName = "DAC_PART/$COMP_0";
		String hardMacroName = "DAC_PART/$COMP_0";
		
		DACPlacer dp = new DACPlacer();
		dp.CLB_X_LIMIT = 60;
		dp.CLB_Y_LIMIT = 127;
		dp.x_index = dp.CLB_X_LIMIT;
		dp.y_index = dp.CLB_Y_LIMIT;
		dp.x_space = 1;
		dp.y_space = 1;
		
		dp.setHardMacroName(hardMacroName);
		
		System.out.print("Loading design...");
		dp.loadDesign(xdlFilename);
		System.out.println("done.");
		
		System.out.println("Unrouting all hard macros...");
		dp.unrouteAll();
		System.out.println("Done Unrouting all hard macros");
		System.out.print("Unplacing hard macro instances...");
		dp.unplaceAllHardMacros();
		System.out.println("done.");
		dp.placeHardMacros();
		System.out.print("done.\nWriting placed design...");
		dp.writeDesign(path + filename + ".xdl");	//writes the design as .ncd file
		System.out.println("done.");
		System.out.println("Hard macro count: " + Integer.toString(hmCount));
	}

	private void unplaceAllHardMacros() {
		for(Instance i : design.getInstances()) {
			if(i.getName().contains(hardMacroName))
			{
				System.out.println("found one!");
				i.unPlace();
			}
		}
	}
}

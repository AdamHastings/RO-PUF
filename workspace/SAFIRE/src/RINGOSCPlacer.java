import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import edu.byu.ece.rapidSmith.design.Design;
import edu.byu.ece.rapidSmith.design.Instance;
import edu.byu.ece.rapidSmith.design.Net;
import edu.byu.ece.rapidSmith.device.PrimitiveSite;
import edu.byu.ece.rapidSmith.device.PrimitiveType;
import edu.byu.ece.rapidSmith.device.Tile;
import edu.byu.ece.rapidSmith.util.FileConverter;

public class RINGOSCPlacer {

	Design design;
	public String name;
	public int CLB_X_LIMIT, CLB_Y_LIMIT;
	static ArrayList<Instance> ringInstances = new ArrayList<Instance>();
	
	int x_index;
	int y_index;
	int x_space;
	int y_space;
	
	
	public RINGOSCPlacer() {
		design = new Design();
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
	
	private void writeDesign(String fileName) {
		design.saveXDLFile(fileName);
		FileConverter.convertXDL2NCD(fileName);
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public static void main(String[] args) {
	
		if (args.length == 0)
		{
			System.out.println("Include a path and filename when running, exiting!");
			System.exit(0);
		}
		String path = args[0];
		String filename = args[1];
		int position = Integer.parseInt(args[2]);
		if (position < 0 || position > 127)
		{
			System.out.println("Invalid position specified, exiting!");
			System.exit(10);
		}
		String xdlFilename = path + filename + ".xdl";
		String name = "ring_osc";
		
		RINGOSCPlacer placer = new RINGOSCPlacer();
		placer.CLB_X_LIMIT = 60; //51
		placer.CLB_Y_LIMIT = 127;
		placer.x_index = 60;//placer.CLB_X_LIMIT;
		placer.y_index = position;//placer.CLB_Y_LIMIT;
		placer.x_space = 1;
		placer.y_space = 1;
		
		placer.setName(name);
		
		System.out.print("Loading design...");
		placer.loadDesign(xdlFilename);
		System.out.println("done.");
		
		//find all tiles that contain instances of our ring oscillator
		//unplace the instances and keep track of them in ringInstances
		placer.unplaceRings("ring_osc<");
		//ring oscillator instances are now placed in ringInstances and they need to be sorted
		Collections.sort(ringInstances, new instanceCompare());
		//System.out.println("Unrouting all instances...");
		//placer.unrouteAll();
		//System.out.println("Done Unrouting all instances");
		//System.out.print("Unplacing hard macro instances...");
		System.out.println("done.");
		placer.placeRingOsc();
		System.out.print("done.\nWriting placed design...");
		placer.writeDesign(path + filename + ".xdl");	//writes the design as .ncd file
		System.out.println("done.");
	}

	private void unplaceRings(String string) {
		// TODO Auto-generated method stub
		String string2 = "PLBV46_SLAVE_SINGLE_I";
		String string3 = "slv_reg";
		ArrayList<Instance> instanceList = new ArrayList<Instance>();
		instanceList.addAll(design.getInstances());
		for(Instance i : instanceList) {
			String name = i.getName();
			if (name.contains(string) && !name.contains(string2) && !name.contains(string3))
			{
				System.out.println("this happened!");
				i.unPlace();
				ringInstances.add(i);
			}
		}
	}

	private void placeRingOsc() {
		for(Instance inst : ringInstances) {
			Tile clbTile = getNextClbTileForPlacement();
			System.out.println(inst.getName() + "\t" + clbTile.getName());
			System.out.println("Placing macro instance...");
			placeRingInstance(inst, clbTile);
			System.out.println("done");
		}
	}

	private void placeRingInstance(Instance inst, Tile clbTile) {
		PrimitiveSite slice_x0y0 = getOriginSlice(clbTile);
		inst.place(slice_x0y0);
		//System.out.println("Instance placed");
//		String baseName = inst.getName().substring(0, inst.getName().lastIndexOf("_")+1);
//		//System.out.println("Got base name");
//		Instance x0y1 = design.getInstance(baseName + "1");
//		//System.out.println("Got instance");
////		System.out.println("name: " + x0y1.getName());
//			x0y1.place(design.getDevice().getPrimitiveSite("SLICE_X" + slice_x0y0.getInstanceX() + "Y" + (slice_x0y0.getInstanceY()+1)));
//		Instance x1y0 = design.getInstance(baseName + "2");
//		System.out.println("Place 2");
//			x1y0.place(design.getDevice().getPrimitiveSite("SLICE_X" + (slice_x0y0.getInstanceX()+1) + "Y" + slice_x0y0.getInstanceY()));
//		Instance x1y1 = design.getInstance(baseName + "3");
//		System.out.println("Place 3");
//			x1y1.place(design.getDevice().getPrimitiveSite("SLICE_X" + (slice_x0y0.getInstanceX()+1) + "Y" + (slice_x0y0.getInstanceY()+1)));
//		System.out.println("Placed!");
		
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
		return slice.getInstanceX()%2;
	}

	private int getSliceRelativeY(PrimitiveSite slice) {
		PrimitiveType type = slice.getType();
		return slice.getInstanceY()%2;
	}

	private Tile getNextClbTileForPlacement() {
		String tileName = "CLB_X" + x_index + "Y" + y_index;
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
		//x_index-=x_space;
		//System.out.println("PLACED ON TILE: " + tileName);
		return clbTile;
	}

//	private void unplaceAllRingOscInstances() {
//		for(Instance i : design.getInstances()) {
//			if(i.getName().contains(name))
//			{
//				System.out.println("found one!");
//				i.unPlace();
//			}
//		}
//	}


	private void unplaceAll() {
		ArrayList<Instance> instanceList = new ArrayList<Instance>();
		instanceList.addAll(design.getInstances());
		for(Instance i : instanceList) {
			i.unPlace();
		}
		
	}
	
}

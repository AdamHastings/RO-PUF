package edu.byu.ece.gremlinTools.ReliabilityTools;

import java.util.ArrayList;
import java.util.HashMap;

import edu.byu.ece.gremlinTools.Virtex4Bits.Pair;
import edu.byu.ece.gremlinTools.Virtex4Bits.V4ConfigurationBit;
import edu.byu.ece.gremlinTools.Virtex4Bits.Virtex4SliceBits;
import edu.byu.ece.gremlinTools.xilinxToolbox.V4XilinxToolbox;
import edu.byu.ece.gremlinTools.xilinxToolbox.XilinxToolboxLookup;
import edu.byu.ece.rapidSmith.bitstreamTools.configuration.FPGA;
import edu.byu.ece.rapidSmith.bitstreamTools.configurationSpecification.DeviceLookup;
import edu.byu.ece.rapidSmith.design.Attribute;
import edu.byu.ece.rapidSmith.design.Design;
import edu.byu.ece.rapidSmith.design.Instance;
import edu.byu.ece.rapidSmith.device.Device;
import edu.byu.ece.rapidSmith.device.PrimitiveSite;
import edu.byu.ece.rapidSmith.device.PrimitiveType;
import edu.byu.ece.rapidSmith.device.Tile;
import edu.byu.ece.rapidSmith.primitiveDefs.Connection;
import edu.byu.ece.rapidSmith.primitiveDefs.Element;
import edu.byu.ece.rapidSmith.primitiveDefs.PrimitiveDef;
import edu.byu.ece.rapidSmith.primitiveDefs.PrimitiveDefList;
import edu.byu.ece.rapidSmith.util.FileTools;

public class SliceDependencies {
	
	// Globals
	private PrimitiveDefList def;
	private FPGA fpga;
	private Device device;
	private Design design;
	private HashMap<Tile, ArrayList<Instance>> testhash; // used for Debugging
															// purposes
	private String arg; // used for Debugging purposes
	private HashMap<String, String> SliceLinputs;
	private HashMap<String, String> SliceMinputs;
	private int max;
	private int longPath;
	private int longestTakenPath;
	private boolean kill;
	
	public SliceDependencies(Design d) {
		this.design = d;
		this.device = d.getDevice();
		this.def = FileTools.loadPrimitiveDefs(device.getExactFamilyType());
		this.longPath = 0;
		this.longestTakenPath = 0;
		getHashmapIns();
		this.kill = false;
	}
	
	/***
	 * The main method.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// SliceDependencies s = new SliceDependencies();
		// s.test();
	}
	
	/* debug functions */
	/**
	 * Prints All the values of the Dgraph in the List
	 * 
	 * @param query
	 */
	private void printquery(ArrayList<Dgraph> query, HashMap<Tile, ArrayList<Instance>> hashIn) {
		msg("");
		for (Dgraph graph : query) {
			// if(!(graph.tile==null)){
			// msg(getSliceInstance(hashIn.get(graph.tile), graph.X, graph.Y));}
			System.out.print("Index:" + graph.curIndex + " Previous DG:" + graph.index + " X" + graph.X + "Y" + graph.Y
					+ " " + graph.Componet + " " + graph.Value + " Tile: " + graph.tile + " Nextsize="
					+ graph.next.length + "[");
			for (Dgraph d : graph.next) {
				System.out.print(d.Componet + " " + d.Value + "; ");
			}
			msg("]");
		}
	}
	
	/**
	 * Prints an object to the cmd line
	 * 
	 * @param o
	 */
	private void msg(Object o) {
		System.out.println(o);
	}
	
	/**
	 * End the program
	 */
	private void end() {
		System.exit(0);
	}
	
	/**
	 * The Main test function for Debugging Problems
	 */
	public void test() {
		loadPrimitiveDefList("xc4vlx15sf363.xdlrc");
		arg = "../../IOBTESTING_HL/Schematic1a.xdl";
		design = new Design();
		design.loadXDLFile(arg);
		
		fpga = new FPGA(DeviceLookup.lookupPartV4V5V6("XC4VLX15"));
		V4XilinxToolbox TB = (V4XilinxToolbox) XilinxToolboxLookup.toolboxLookup(fpga.getDeviceSpecification());
		
		// test input values
		Attribute at = new Attribute("DYMUX", "", "#OFF");
		Virtex4SliceBits sbit = new Virtex4SliceBits(0, 0, PrimitiveType.SLICEL, fpga, device, TB);
		ArrayList<V4ConfigurationBit> bits = sbit.getBits(at);
		
		testhash = createTestDesignHashMap();
		Tile t = (Tile) testhash.keySet().toArray()[9];
		
		AffectedAttributes(sbit, bits.get(0), testhash, at, t); // Returns
																// ArrayList of
																// affected
																// components
		
		System.out.println("Done");
		// end();
		// used to help create the inputHash
		// getHashmapIns();
		// HashMap<String,ArrayList<V4ConfigurationBit>> hashVal =
		// InputHash(at.getPhysicalName().toString(),sbit);
		
	}
	
	/**
	 * test method in order to create valid inputs for testing includes dummy
	 * instances
	 */
	private HashMap<Tile, ArrayList<Instance>> createTestDesignHashMap() {
		HashMap<Tile, ArrayList<Instance>> hash = new HashMap<Tile, ArrayList<Instance>>();
		for (Tile[] t : device.getTiles()) {
			for (Tile tile : t) {
				ArrayList<Instance> array = new ArrayList<Instance>();
				if (tile.getPrimitiveSites() != null)
					for (PrimitiveSite site : tile.getPrimitiveSites()) {
						for (String inst : design.getInstanceMap().keySet()) {
							if (design.getInstanceMap().get(inst).getPrimitiveSiteName() == site.toString()) {
								array.add(design.getInstance(inst));
								hash.put(tile, array);
							}
						}
					}
			}
		}
		return hash;
	}
	
	/* Support Methods */
	/**
	 * Creates a list of the Input for components in a SLICEL or SLICEM
	 */
	private void getHashmapIns() {
		SliceLinputs = new HashMap<String, String>();
		SliceMinputs = new HashMap<String, String>();
		String key = "";
		// From observation with SliceL and SliceM there is only one element
		// associated with each index backwards connection
		for (Element elem : def.getPrimitiveDef(PrimitiveType.SLICEL).getElements()) {
			for (Connection conn : elem.getConnections()) {
				if (!conn.isForwardConnection()) {
					key = (conn.getElement0() + " " + conn.getPin0());
					SliceLinputs.put(key, conn.getElement1() + " " + conn.getPin1());
				}
			}
		}
		// SliceM inputs to elements
		for (Element elem : def.getPrimitiveDef(PrimitiveType.SLICEM).getElements()) {
			for (Connection conn : elem.getConnections()) {
				if (!conn.isForwardConnection()) {
					key = (conn.getElement0() + " " + conn.getPin0());
					SliceMinputs.put(key, conn.getElement1() + " " + conn.getPin1());
				}
			}
		}
	}
	
	/**
	 * A method to just take the x,y coordinate from a primitive. i.e.
	 * SLICEM_x0y1 to x0y1
	 * 
	 * @param clb
	 * @return
	 */
	private String getLocationSuffix(String clb) {
		return clb = clb.substring(clb.indexOf("_") + 1);
	}
	
	/**
	 * This loads the device and primitive_def list.
	 * 
	 * @param xdlrc
	 */
	public void loadPrimitiveDefList(String xdlrc) {
		device = FileTools.loadDevice(xdlrc.substring(0, xdlrc.indexOf(".")));
		def = FileTools.loadPrimitiveDefs(device.getExactFamilyType());
	}
	
	/**
	 * This method is used to build the a node and pointer to it children. This
	 * builds each piece of the dependency graph. An assuption made for CLB is
	 * that the X and Y values refer to its' SLICE location.
	 * 
	 * @param NextComp
	 * @return
	 */
	private Dgraph[] buildDgraph(Dgraph parent, ArrayList<String> NextComp, HashMap<Tile, ArrayList<Instance>> hashIn) {
		Dgraph[] DgraphL = null;
		if (NextComp != null) {
			DgraphL = new Dgraph[NextComp.size()];
			int index = 0;
			for (String comp : NextComp) {
				Dgraph dg = new Dgraph();
				dg.parent = parent;
				dg.next = null;
				// msg("Testing "+comp+""+Integer.parseInt(comp.substring(1,
				// comp.indexOf("Y"))));
				dg.X = Integer.parseInt(comp.substring(1, comp.indexOf("Y")));
				dg.Y = Integer.parseInt(comp.substring(comp.indexOf("Y") + 1, comp.indexOf("_")));
				dg.Componet = comp.substring(comp.indexOf("_") + 1, comp.indexOf(" "));
				dg.Value = comp.substring(comp.indexOf(" ") + 1, comp.length());
				if ((((parent.Y % 2 == 0) && ((parent.Y + 1 == dg.Y) || (parent.Y == dg.Y))) || 
						((parent.Y % 2 == 1) && ((parent.Y - 1 == dg.Y) || (parent.Y == dg.Y))))) {
					dg.tile = parent.tile;
				} else {
					dg.tile = null;
					String nextCLB = "CLB_X" + parent.tile.getTileXCoordinate() + "Y" + (dg.Y / 2);
					;
					for (Tile t : hashIn.keySet()) {
						if (t.getName().contains(nextCLB)) {
							dg.tile = t;
							break;
						}
					}
				}
				if (!(dg.tile == null)) {
					dg.InstanceInDesign = getSliceInstance(hashIn.get(dg.tile), dg.X, dg.Y);
				}
				DgraphL[index] = dg;
				index++;
			}
		}
		return DgraphL;
	}
	
	/* Builds A CLB Dependency HashMap */
	/**
	 * It uses any Slice in the CLB and builds the 4 slices in the CLB and
	 * builds a CLB block and it interconnections between other blocks. The
	 * input is a String of the SliceName i.e. Slice_X1_Y15 This is specific to
	 * the Virtex 4 architecture.
	 * 
	 * @param loc
	 * @return
	 */
	private HashMap<String, ArrayList<String>> buildCLBV4(String loc) {
		HashMap<String, ArrayList<String>> hashx0y0 = new HashMap<String, ArrayList<String>>();
		HashMap<String, ArrayList<String>> hashx0y1 = new HashMap<String, ArrayList<String>>();
		HashMap<String, ArrayList<String>> hashx1y0 = new HashMap<String, ArrayList<String>>();
		HashMap<String, ArrayList<String>> hashx1y1 = new HashMap<String, ArrayList<String>>();
		int x = Integer.parseInt(loc.substring(1, loc.indexOf("Y")));
		int y = Integer.parseInt(loc.substring(loc.indexOf("Y") + 1, loc.length()));
		
		if (x % 2 == 1) {
			x -= 1;
		}
		if (y % 2 == 1) {
			y -= 1;
		}
		
		String location0 = "X" + (x) + "Y" + (y);
		String location1 = "X" + (x) + "Y" + (y + 1);
		String location2 = "X" + (x + 1) + "Y" + (y);
		String location3 = "X" + (x + 1) + "Y" + (y + 1);
		
		PrimitiveDef SliceL = def.getPrimitiveDef(PrimitiveType.SLICEL);
		PrimitiveDef SliceM = def.getPrimitiveDef(PrimitiveType.SLICEM);
		
		getHashCLBcompmap(SliceM, hashx0y0, location0);
		getHashCLBcompmap(SliceM, hashx0y1, location1);
		getHashCLBcompmap(SliceL, hashx1y0, location2);
		getHashCLBcompmap(SliceL, hashx1y1, location3);
		// building dependancyList based on no pip block between slices
		/*
		 * x0y0 -> x0y1 cOUTUSED -> cyinit cin BYINVOUTUSED -> SLICEWE1USED
		 * FXUSED -> F6MUX 1
		 */
		hashx0y0.get(location0 + "_COUTUSED").remove(location0 + "_COUT COUT");
		hashx0y0.get(location0 + "_COUTUSED").add(location1 + "_" + "CYINIT CIN");
		hashx0y0.get(location0 + "_BYINVOUTUSED").remove(location0 + "_BYINVOUT BYINVOUT");
		hashx0y0.get(location0 + "_BYINVOUTUSED").add(location1 + "_" + "SLICEWE1USED 0");
		hashx0y0.get(location0 + "_FXUSED").remove(location0 + "_FX FX");
		hashx0y0.get(location0 + "_FXUSED").add(location1 + "_" + "F6MUX 1");
		/*
		 * x0y0 ->x0y0 byoutUSED -> SLICEWE1USED f5USED -> F6MUX 1
		 */
		hashx0y0.get(location0 + "_BYOUTUSED").remove(location0 + "_BYOUT BYOUT");
		hashx0y0.get(location0 + "_BYOUTUSED").add(location0 + "_" + "SLICEWE1USED 0");
		hashx0y0.get(location0 + "_F5USED").remove(location0 + "_F5 F5");
		hashx0y0.get(location0 + "_F5USED").add(location0 + "_" + "F6MUX 1");
		/*
		 * need x0y1 ->x0y0 DIGUSED -> DIGMUX ALTDIG Shiftoutused-> DIGMUX
		 * shiftin f5used-> f6mux 0
		 */
		hashx0y1.get(location1 + "_DIGUSED").remove(location1 + "_DIG DIG");
		hashx0y1.get(location1 + "_DIGUSED").add(location0 + "_" + "DIG_MUX DIG");
		hashx0y1.get(location1 + "_SHIFTOUTUSED").remove(location1 + "_SHIFTOUT SHIFTOUT");
		hashx0y1.get(location1 + "_SHIFTOUTUSED").add(location0 + "_" + "DIG_MUX SHIFTOUTUSED");
		hashx0y1.get(location1 + "_F5USED").remove(location1 + "_F5 F5");
		hashx0y1.get(location1 + "_F5USED").add(location0 + "_" + "F6MUX 0");
		/*
		 * need x0y1 ->x1y1 FXUSED ->F6MUX 1
		 */
		hashx0y1.get(location1 + "_FXUSED").remove(location1 + "_FX FX");
		hashx0y1.get(location1 + "_FXUSED").add(location3 + "_" + "F6MUX 1");
		/*
		 * need x1y0 ->x0y1 FXUSED->F6MUX 0
		 */
		hashx1y0.get(location2 + "_FXUSED").remove(location2 + "_FX FX");
		hashx1y0.get(location2 + "_FXUSED").add(location1 + "_" + "F6MUX 0");
		/*
		 * need x1y0 ->x1y0 F5USED->F6MUX 1
		 */
		hashx1y0.get(location2 + "_F5USED").remove(location2 + "_F5 F5");
		hashx1y0.get(location2 + "_F5USED").add(location2 + "_" + "F6MUX 1");
		/*
		 * need x1y0 ->x1y1 coutUSED->cyinit cin
		 */
		hashx1y0.get(location2 + "_COUTUSED").remove(location2 + "_COUT COUT");
		hashx1y0.get(location2 + "_COUTUSED").add(location3 + "_" + "CYINIT CIN");
		/*
		 * need x1y1 ->x1y0 f5->F6MUX 0
		 */
		hashx1y1.get(location3 + "_F5USED").remove(location3 + "_F5 F5");
		hashx1y1.get(location3 + "_F5USED").add(location2 + "_" + "F6MUX 0");
		// dependency connections to other slices done with no bit assumption
		// between slices
		// dependency connection to other CLB blocks
		// to do find max val and location value formula
		int max = device.getTile(0, 4).getTileYCoordinate(); // CLB MaxLocation
		int maxSlice = max * 2 + 1; // SliceL/SliceM MaxYCoordinate
		if (y > maxSlice || y < 0) // checks to see if out of bounds
		{
			System.out.println("Error outofbounds");
		} else if (y == (maxSlice - 1)) // upper boundary case
		{
			// System.out.println("Top case");
			hashx0y1.get(location1 + "_FXUSED").remove(location1 + "_FX FX");
			hashx0y1.get(location1 + "_FXUSED").add("X" + (x + 1) + "Y" + (y - 1) + "_" + "F6MUX 0");
		} else if (y == 0) // lower bounder case
		{
			// System.out.println("bottom case");
			
			hashx0y1.get(location1 + "_COUTUSED").remove(location1 + "_COUT COUT");
			hashx0y1.get(location1 + "_COUTUSED").add("X" + (x) + "Y" + (y + 2) + "_" + "CYINIT CIN");
			hashx1y1.get(location3 + "_COUTUSED").remove(location3 + "_COUT COUT");
			hashx1y1.get(location3 + "_COUTUSED").add("X" + (x + 1) + "Y" + (y + 2) + "_" + "CYINIT CIN");
		} else // middle case
		{
			// System.out.println("Middle Case");
			hashx0y1.get(location1 + "_FXUSED").remove(location1 + "_FX FX");
			hashx0y1.get(location1 + "_FXUSED").add("X" + (x + 1) + "Y" + (y - 1) + "_" + "F6MUX 0");
			hashx0y1.get(location1 + "_COUTUSED").remove(location1 + "_COUT COUT");
			hashx0y1.get(location1 + "_COUTUSED").add("X" + (x) + "Y" + (y + 2) + "_" + "CYINIT CIN");
			hashx1y1.get(location3 + "_COUTUSED").remove(location3 + "_COUT COUT");
			hashx1y1.get(location3 + "_COUTUSED").add("X" + (x + 1) + "Y" + (y + 2) + "_" + "CYINIT CIN");
		}
		hashx0y0.putAll(hashx0y1);
		hashx0y0.putAll(hashx1y0);
		hashx0y0.putAll(hashx1y1);
		return hashx0y0;
	}
	
	/**
	 * Supports the buildCLBV4 function by building a slice and its' dependency.
	 * It takes the primitive_def and finds it forward connections to build a
	 * forward flow dependency.
	 * 
	 * @param k
	 * @param hash
	 * @param location
	 */
	private void getHashCLBcompmap(PrimitiveDef k, HashMap<String, ArrayList<String>> hash, String location) {
		for (Element elem : k.getElements()) {
			ArrayList<String> arr = new ArrayList<String>();
			for (Connection conn : elem.getConnections()) {
				String key = "";
				if (conn.isForwardConnection()) {
					key = location + "_" + conn.getElement0();
					arr.add(location + "_" + conn.getElement1() + " " + conn.getPin1());
					hash.put(key, arr);
				}
			}
		}
	}
	
	/**
	 * This method checks the database for v4 slice in order to find the
	 * attribute default equivalent value.
	 * 
	 * @param attr
	 * @return
	 */
	private String checkDBForDefaultValue(Attribute attr) {
		if (attr.getPhysicalName().equals("XBMUX") || attr.getPhysicalName().equals("YBMUX")
				|| attr.getPhysicalName().equals("CY0G") || attr.getPhysicalName().equals("CY0F")) {
			return "1";
		} else if (attr.getPhysicalName().equals("F") || attr.getPhysicalName().equals("G")) {
			return "0";
		} else if (attr.getPhysicalName().equals("DYMUX") || attr.getPhysicalName().equals("DXMUX")
				|| attr.getPhysicalName().equals("DIF_MUX") || attr.getPhysicalName().equals("DIG_MUX")
				|| attr.getPhysicalName().equals("REVUSED")) {
			return "#OFF";
		} else if (attr.getPhysicalName().contains("USED")) {
			return null;
		} else if (attr.getPhysicalName().contains("FF")) {
			if (attr.getPhysicalName().contains("INIT")) {
				return "INIT1";
			} else if (attr.getPhysicalName().contains("SR")) {
				return "SRHIGH";
			} else {
				return "#FF";
			}
		} else if (attr.getPhysicalName().contains("SYNC")) {
			return "ASYNC";
		} else if (attr.getPhysicalName().contains("_ATTR")) {
			return "DUAL_PORT";
		} else if (attr.getPhysicalName().equals("GYMUX")) {
			return "FX";
		} else if (attr.getPhysicalName().equals("FXMUX")) {
			return "F5";
		} else if (attr.getPhysicalName().equals("CYINIT")) {
			return "BX";
		} else if (attr.getPhysicalName().contains("INV")) {
			msg(attr.getPhysicalName().substring(0, attr.getPhysicalName().indexOf("I") - 1) + "_B");
			
			return attr.getPhysicalName().substring(0, attr.getPhysicalName().indexOf("I") - 1) + "_B";
		} else {
			return null;
		}
	}
	
	// Support to the object method
	/**
	 * This method find the slice Instance from and array of Instance in a clb
	 */
	private Instance getSliceInstance(ArrayList<Instance> array, int x, int y) {
		Instance inst = null;
		if (array == null) {
			// msg("null input");
			return null;
		}
		for (Instance prim : array) {
			if (x % 2 == prim.getInstanceX() % 2 && y % 2 == prim.getInstanceY() % 2)
				inst = prim;
		}
		return inst;
	}
	
	/**
	 * 
	 * Return true if the path is a used path
	 * 
	 * @param d
	 * @param hashIn
	 * @return
	 */
	private boolean DPcheck(Dgraph d, HashMap<Tile, ArrayList<Instance>> hashIn) {
		Attribute at = new Attribute("", "", "");
		at.setPhysicalName(d.Componet);
		at.setValue(d.Value);
		Instance temp;
		String Val = "";
		boolean kill1 = false;
		temp = getSliceInstance(hashIn.get(d.tile), d.X, d.Y);
		if (d.parent.Componet.equals("REVUSED") && d.parent.parent == null
				&& !temp.getAttribute("BYINV").getValue().equals("#OFF")) {
			kill1 = true;
		}
		for (Dgraph findVal : d.parent.next) {
			if (findVal.Componet.equals(d.Componet)) {
				Val = findVal.Value;
				break;
			}
		}
		if (temp == null) {
			return false;
		} else if (d.Componet.equals("F5MUX") || d.Componet.equals("F6MUX") || d.Componet.equals("CYMUXF")
				|| d.Componet.equals("XORF") || d.Componet.equals("CYMUXG") || d.Componet.equals("XORG")
				|| !temp.hasAttribute(d.Componet)) {
			if (d.next == null) {
				return false;
			}
			for (Dgraph dn : d.next) {
				if (DPcheck(dn, hashIn)) {
					return true;
				}
			}
		} else if (d.Componet.equals("WSGEN")) { // special case possible logic
													// error here
			if (temp.getAttribute("SLICEWE0USED").getValue().equals("1")
					|| temp.getAttribute("SLICEWE1USED").getValue().equals("1")) {
				return true;
			} else {
				if (temp.getAttribute("F").getValue().contains("#RAM")
						|| temp.getAttribute("F_ATTR").getValue().equals("SHIFT_REG")
						|| temp.getAttribute("G").getValue().contains("#RAM")
						|| temp.getAttribute("G_ATTR").getValue().equals("SHIFT_REG")) {
					return true;
				} else {
					return false;
				}
			}
		} else if (temp.getAttribute(d.Componet).getValue().equals("#OFF")) {
			if (d.Componet.contains("FF")) {
				return false;
			}
			if (d.Componet.contains("SRFFMUX")) {
				if (!temp.getAttribute("FFX").getValue().equals("#OFF")
						|| !temp.getAttribute("FFX").getValue().equals("#OFF")) {
					return true;
				}
				return false;
			} else if (checkDBForDefaultValue(at) == null) {
				return false;
			} else if (checkDBForDefaultValue(at).equals("#OFF")) {
				return false;
			} else if (checkDBForDefaultValue(at).equals(Val)) {
				if (d.next == null) {
					return false;
				}
				for (Dgraph dn : d.next) {
					if (DPcheck(dn, hashIn)) {
						return true;
					}
				}
			}
		} else if (d.Componet.equals("F")
				&& d.parent.Componet.equals("DIF_MUX")
				&& (temp.getAttribute("F_ATTR").getValue().equals("SHIFT_REG") || temp.getAttribute("F").getValue()
						.contains("#RAM"))) {
			return true;
		} else if (d != null
				&& d.Componet != null
				&& temp != null
				&& temp.getAttribute("G") != null
				&& temp.getAttribute("G").getValue() != null
				&& temp.getAttribute("G_ATTR") != null
				&& temp.getAttribute("G_ATTR").getValue() != null
				&& d.Componet.equals("G")
				&& d.parent.Componet.equals("DIG_MUX")
				&& (temp.getAttribute("G_ATTR").getValue().equals("SHIFT_REG") || temp.getAttribute("G").getValue()
						.contains("#RAM"))) {
			return true;
		} else if (d.Componet.contains("USED") && temp.getAttribute(d.Componet).getValue().equals("0")) {
			return true;
		} else if (d.Componet.contains("SRFFMUX")) {
			for (Dgraph dn : d.next) {
				if (DPcheck(dn, hashIn)) {
					return true;
				}
			}
			return false;
		} else if (temp.getAttribute(d.Componet).getValue().equals(Val)) {
			return true;
		} else if (d.Componet.contains("FF")) {
			if (!temp.getAttribute("FFX").getValue().equals("#OFF") || !temp.getAttribute("FFY").equals("#OFF")) {
				return true;
			}
			return false;
		}
		return false;
	}
	
	/**
	 * This returns a list of the immediate component that are affect by the
	 * change in the design and influence the design.
	 * 
	 * @param dlist
	 * @param hashIn
	 * @param startindex
	 * @return
	 */
	private ArrayList<Pair<Instance, Attribute>> DP(ArrayList<Dgraph> dlist, HashMap<Tile, ArrayList<Instance>> hashIn,
			int startindex) {
		ArrayList<Pair<Instance, Attribute>> array = new ArrayList<Pair<Instance, Attribute>>();
		
		for (Dgraph d : dlist) {
			if (d.index > startindex) {
				break;
			} else if (d.index == startindex) // looks only at the directly
												// related path
			{
				Attribute at = new Attribute("", "", "");
				at.setPhysicalName(d.Componet);
				at.setValue(d.Value);
				Instance temp;
				temp = getSliceInstance(hashIn.get(d.tile), d.X, d.Y);
				
				if (DPcheck(d, hashIn)) {
					Pair<Instance, Attribute> nwpair = new Pair<Instance, Attribute>(temp,
							temp.getAttribute(d.Componet));
					array.add(nwpair);
				}
			}
		}
		return array;
	}
	
	/**
	 * This method return if the true if it is in the design or a valid path
	 */
	private boolean inDesign(Dgraph dg, HashMap<Tile, ArrayList<Instance>> hashIn, Attribute attr) { // non
																										// configurable
																										// parts
		if (attr.getPhysicalName().equals("REVUSED")) {
			if (dg.InstanceInDesign == null) {
				return false;
			} else if (dg.InstanceInDesign.hasAttribute("F") || dg.InstanceInDesign.hasAttribute("G")) {
				if (dg.parent == null) {
					if (dg.InstanceInDesign.getAttribute("BYINV").getValue().equals("0")) {
						return true;
					} else {
						return false;
					}
				} else if (dg.InstanceInDesign.hasAttribute("REVUSED")) {
					if (!dg.InstanceInDesign.getAttribute("REVUSED").getValue().equals("#OFF")) {
						return true;
					}
				}
				return false;
			}
		}
		if (attr.getPhysicalName().equals("CYINIT")) {
			if (dg.parent.Componet.equals("BXINV")) {
				if (dg.InstanceInDesign == null) {
					return true;
				} else if (dg.InstanceInDesign.getAttribute("CYINIT") != null
						&& dg.InstanceInDesign.getAttribute("CYINIT").getValue() != null
						&& !dg.InstanceInDesign.getAttribute("CYINIT").getValue().equals("CIN")) {
					return true;
				} else {
					return false;
				}
			} else if (dg.parent.Componet.equals("COUTUSED")) {
				if (dg.InstanceInDesign == null) {
					return false;
				} else if (dg.InstanceInDesign.getAttribute("CYINIT") == null) {
					return false;
				} else if (dg.InstanceInDesign.getAttribute("CYINIT").getValue().equals("CIN")) {
					return true;
				}
				return false;
			}
		}
		
		if (attr.getPhysicalName().equals("FFX")) {
			if (dg.InstanceInDesign != null && dg.InstanceInDesign.getAttribute("FFX") != null) {
				if (!dg.InstanceInDesign.getAttribute("FFX").getValue().equals("#OFF")) {
					return true;
				}
			}
			return false;
		}
		if (attr.getPhysicalName().equals("FFY")) {
			if (dg.InstanceInDesign != null && dg.InstanceInDesign.getAttribute("FFY") != null) {
				if (!dg.InstanceInDesign.getAttribute("FFY").getValue().equals("#OFF")) {
					return true;
				}
			}
			return false;
		}
		if (attr.getPhysicalName().equals("DXMUX")) {
			if (dg.InstanceInDesign == null) {
				return false;
			} else if (dg.InstanceInDesign.getAttribute("FFX") == null) {
				return false;
			} else if (dg.InstanceInDesign.getAttribute("FFX").getValue().equals("#OFF")) {
				return false;
			} else {
				return true;
			}
		}
		if (attr.getPhysicalName().equals("DYMUX")) {
			if (dg.InstanceInDesign == null) {
				return false;
			} else if (dg.InstanceInDesign.getAttribute("FFY") == null) {
				return false;
			} else if (dg.InstanceInDesign.getAttribute("FFY").getValue().equals("#OFF")) {
				return false;
			} else {
				return true;
			}
		}
		// assumption that SRFFMUX is not configurable based on database
		if (attr.getPhysicalName().equals("CEINV") || attr.getPhysicalName().equals("CLKINV")
				|| attr.getPhysicalName().equals("SRINV") || attr.getPhysicalName().equals("SRFFMUX")) {
			if (dg.InstanceInDesign == null) {
				return false;
			} else if ((dg.InstanceInDesign.getAttribute("FFX") != null
					&& dg.InstanceInDesign.getAttribute("FFY") != null
					&& dg.InstanceInDesign.getAttribute("FFX").getValue() != null && dg.InstanceInDesign.getAttribute(
					"FFY").getValue() != null)) {
				
				if (dg.InstanceInDesign.getAttribute("FFX").getValue().equals("#OFF")
						|| !dg.InstanceInDesign.getAttribute("FFY").getValue().equals("#OFF")) {
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		}
		if (attr.getPhysicalName().equals("SLICEWE0USED") || attr.getPhysicalName().equals("SLICEWE1USED")) {
			if (dg.InstanceInDesign == null) {
				return false;
			} else if (dg.InstanceInDesign.getAttribute("F") == null) {
				return false;
			}// finds a dummy instances
			else if (dg.InstanceInDesign.getAttribute("F").getValue().contains("#RAM")
					|| dg.InstanceInDesign.getAttribute("G").getValue().contains("#RAM")) {
				return true;
			} else if (dg.InstanceInDesign.getAttribute("F_ATTR") != null) {
				if (dg.InstanceInDesign.getAttribute("F_ATTR").getValue().equals("SHIFT_REG")) {
					return true;
				}
				return false;
			} else if (dg.InstanceInDesign.getAttribute("G_ATTR") != null) {
				if (dg.InstanceInDesign.getAttribute("G_ATTR").getValue().equals("SHIFT_REG")) {
					return true;
				}
				return false;
			} else {
				return false;
			}
		}
		if (attr.getPhysicalName().equals("WSGEN")) {
			if (dg.InstanceInDesign == null) {
				return false;
			} else if (dg.InstanceInDesign.getAttribute("F") == null) {
				return false;
			}// finds a dummy instances
			else if (dg.InstanceInDesign.getAttribute("F").getValue().contains("#RAM")
					|| dg.InstanceInDesign.getAttribute("G").getValue().contains("#RAM")) {
				return true;
			} else if (dg.InstanceInDesign.getAttribute("F_ATTR") != null) {
				if (dg.InstanceInDesign.getAttribute("F_ATTR").getValue().equals("SHIFT_REG")) {
					return true;
				}
				return false;
			} else if (dg.InstanceInDesign.getAttribute("G_ATTR") != null) {
				if (dg.InstanceInDesign.getAttribute("G_ATTR").getValue().equals("SHIFT_REG")) {
					return true;
				}
				return false;
			} else {
				return false;
			}
		}
		if (attr.getPhysicalName().equals("G") || attr.getPhysicalName().equals("F")) {
			if (dg.parent != null && dg.InstanceInDesign != null) {
				if (dg.InstanceInDesign.getAttributeValue(dg.Componet) != null)
					if (!dg.InstanceInDesign.getAttributeValue(dg.Componet).contains("#RAM")
							&& !dg.InstanceInDesign.getAttributeValue(dg.Componet).contains("#ROM")) {
						return false;
					}
			}
			return true;
		}
		if (attr.getPhysicalName().equals("DIF_MUX") && dg.InstanceInDesign != null) {
			if (dg.InstanceInDesign.hasAttribute("DIF_MUX")) {
				
				if (dg.InstanceInDesign.getAttribute("DIF_MUX").getValue().equals(dg.Value)) {
					return true;
				}
			}
			return false;
		}
		if (attr.getPhysicalName().equals("DIG_MUX") & dg.InstanceInDesign != null) {
			if (dg.InstanceInDesign.hasAttribute("DIG_MUX")) {
				if (!dg.InstanceInDesign.getAttribute("DIG_MUX").equals("#OFF")) {
					return true;
				}
			}
			return false;
		}
		if (attr.getPhysicalName().contains("XORG")) {
			if (dg.InstanceInDesign == null) {
				return false;
			} else if (dg.InstanceInDesign.hasAttribute("GYMUX")) {
				if (dg.InstanceInDesign.getAttribute("GYMUX").getValue().equals("GXOR")) {
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		}
		if (attr.getPhysicalName().contains("XORF")) {
			if (dg.InstanceInDesign == null) {
				return false;
			} else if (dg.InstanceInDesign.hasAttribute("FXMUX")) {
				if (dg.InstanceInDesign.getAttribute("FXMUX").getValue().equals("FXOR")) {
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		}
		if (attr.getPhysicalName().contains("F5MUX")) {
			if (dg.InstanceInDesign == null) {
				if (dg.parent.Componet.equals("G")) {
					return true;
				} else {
					return false;
				}
			} else {
				if (dg.InstanceInDesign.hasAttribute("BXINV")) {
					if (!dg.InstanceInDesign.getAttribute("BXINV").equals("#OFF")) {
						if (dg.InstanceInDesign.hasAttribute("F") || dg.InstanceInDesign.hasAttribute("G")) {
							return true;
						} else {
							return false;
						}
					} else {
						if (dg.parent.Componet.equals("BXINV")) {
							if (dg.InstanceInDesign.hasAttribute("F")) {
								return true;
							} else {
								return false;
							}
						} else {
							if (dg.InstanceInDesign.hasAttribute("G")) {
								return true;
							} else {
								return false;
							}
						}
					}
				} else if (dg.parent.Componet.equals("BXINV")) {
					if (dg.InstanceInDesign.hasAttribute("F")) {
						return true;
					} else {
						return false;
					}
				} else {
					if (dg.InstanceInDesign.hasAttribute("G")) {
						return true;
					} else {
						return false;
					}
				}
			}
		}
		if (attr.getPhysicalName().contains("F6MUX")) {
			if (dg.InstanceInDesign == null) {
				// msg("Parent of f6mux "+dg.parent.Componet);
				if (dg.parent.Componet.equals("BYINV")) {
					if (dg.Y == max) {
						return true;
					}
					if (dg.Y % 2 == 0) {
						Instance tempInst = getSliceInstance(hashIn.get(dg.tile), dg.X, dg.Y + 1);
						if (tempInst == null) {
							return false;
						} else if (tempInst.hasAttribute("BXINV")) {
							if (tempInst.hasAttribute("F") || tempInst.hasAttribute("G")) {
								return true;
							}
							return false;
						} else if (!tempInst.hasAttribute("BXINV")) {
							if (tempInst.hasAttribute("G")) {
								return true;
							}
							return false;
						} else {
							msg("Error State");
							end();
						}
					} else if (dg.X % 2 == 0) {
						Instance tempInst = getSliceInstance(hashIn.get(dg.tile), dg.X, dg.Y - 1);
						if (tempInst == null) {
							return false;
						} else if (tempInst.hasAttribute("FXUSED")) {
							return true;
						} else {
							return false;
						}
					} else {
						Instance tempInst = getSliceInstance(hashIn.get(dg.tile), dg.X - 1, dg.Y);
						if (tempInst == null) {
							return false;
						} else if (tempInst.hasAttribute("BXINV")) {
							if (tempInst.hasAttribute("F") || tempInst.hasAttribute("G")) {
								return true;
							}
							return false;
						} else if (!tempInst.hasAttribute("BXINV")) {
							if (tempInst.hasAttribute("G")) {
								return true;
							}
							return false;
						} else {
							msg("Error State");
							end();
						}
					}
				} else // parent is not BYINV therefore check FXINB
				{
					if (dg.Value.equals("FXINA")) {
						return false;
					} else {
						return true;
					}
				}
			} else {
				if (dg.InstanceInDesign.hasAttribute("BYINV")) {
					return true;
				} else if (dg.parent.Componet.equals("BYINV")) {
					if (dg.Value.equals("FXINB")) {
						return false;
					} else {
						return true;
					}
				} else {
					if (dg.Value.equals("FXINA")) {
						return false;
					} else {
						return true;
					}
				}
			}
		}
		if (attr.getPhysicalName().contains("CYMUXG")) {
			if (dg.InstanceInDesign == null) {
				if (dg.parent.Componet.equals("CY0G")) {
					return true;
				} else {
					return false;
				}
			} else if (dg.parent.Componet.equals("G") || dg.parent.Componet.equals("G_ATTR")) {
				return true;
			} else if (dg.parent.Componet.equals("CYMUXF")) {
				if (dg.InstanceInDesign.hasAttribute("G")) {
					if (!dg.InstanceInDesign.getAttribute("G").equals("#OFF")) {
						return true;
					}
				}
				return false;
			} else if (dg.parent.Componet.equals("CY0G")) {
				return true;
			} else {
				return false;
			}
		}
		if (attr.getPhysicalName().contains("CYMUXF")) {
			if (dg.InstanceInDesign == null) {
				if (dg.parent.Componet.equals("CY0F") || dg.parent.Componet.equals("F")) {
					return true;
				} else {
					return false;
				}
			} else if (dg.parent.Componet.equals("F") || dg.parent.Componet.equals("F_ATTR")) {
				return true;
			} else if (dg.parent.Componet.equals("CYINIT")) {
				if (dg.InstanceInDesign.hasAttribute("F")) {
					if (!dg.InstanceInDesign.getAttribute("F").equals("#OFF")) {
						return true;
					}
				}
				return false;
			} else if (dg.parent.Componet.equals("CY0F")) {
				return true;
			} else {
				msg(dg.Componet + " attr: " + attr.getPhysicalName() + " CurIter: " + longPath);
				msg("invalid state");
				end();
			}
		}
		
		ArrayList<Instance> inst = hashIn.get(dg.tile);
		if (inst == null) {
			if (checkDBForDefaultValue(attr) == null) // not in database
			{
				return true;
			} else if (dg.parent.Componet.equals(checkDBForDefaultValue(attr))) {
				return true;
			} else {
				return false;
			}
		}
		
		Instance ins = getSliceInstance(hashIn.get(dg.tile), dg.X, dg.Y);
		if (checkDBForDefaultValue(attr) == null) // not in database
		{
			return true;
		}
		if (ins == null) {
			if (dg.parent.Componet.equals(checkDBForDefaultValue(attr))) {
				return true;
			} else {
				return false;
			}
		} else {
			if (ins.hasAttribute(dg.Componet)) {
				if (ins.getAttribute(dg.Componet).getValue().equals(dg.parent.Value)) {
					return true;
				} else {
					return false;
				}
			} else if (dg.parent.Componet.equals(checkDBForDefaultValue(attr))) {
				return true;
			} else {
				return false;
			}
		}
	}
	
	/* Object Methods */
	/**
	 * This method is the heart for finding the Affect Components when a
	 * component configuration bits are changed in a Virtex 4 CLB. This returns
	 * a list of components thats values could have changed because of the
	 * upset.
	 * 
	 * @param sbit
	 * @param bit
	 * @param hashIn
	 * @param attr
	 * @param tile
	 * @return
	 */
	public ArrayList<Pair<Instance, Attribute>> AffectedAttributes(Virtex4SliceBits sbit, V4ConfigurationBit bit,
			HashMap<Tile, ArrayList<Instance>> hashIn, Attribute attr, Tile tile) {
		
		if (tile == null) {
			msg("Error Tile is null");
			return null;
		}
		ArrayList<Dgraph> query = new ArrayList<Dgraph>();
		longPath++;
		device = tile.getDevice();
		max = (device.getTile(0, 1).getTileYCoordinate() * 2) + 1;
		PrimitiveSite pri = bit.getTile().getPrimitiveSites()[0]; // can be done
																	// since
																	// site
																	// array is
																	// sorted
																	// and that
																	// the
																	// lowest
																	// corner in
																	// a clb
		int xstart = pri.getInstanceX() + sbit.getX(); // slice x coordinate
		int ystart = pri.getInstanceY() + sbit.getY(); // slice y coordinate
		String locationName = "X" + xstart + "Y" + ystart; // get the slice
															// locations name
		HashMap<String, ArrayList<String>> hash = buildCLBV4(locationName); // contains
																			// a
																			// list
																			// of
																			// the
																			// attributes
																			// in
																			// clb
																			// at
																			// tile
																			// location
		ArrayList<String> k = new ArrayList<String>();
		
		// Special Cases G_ATTR F_ATTR these change the luts behavior right?
		if (attr.getPhysicalName().equals("G_ATTR")) {
			k = (hash.get(locationName + "_G"));
		} else if (attr.getPhysicalName().equals("F_ATTR")) {
			k = (hash.get(locationName + "_F"));
		} else if (attr.getPhysicalName().equals("FFY_SR_ATTR")) {
			k = (hash.get(locationName + "_FFY"));
		} else if (attr.getPhysicalName().equals("FFX_SR_ATTR")) {
			k = (hash.get(locationName + "_FFX"));
		} else if (attr.getPhysicalName().equals("SYNC_ATTR")) {
			Instance inS = getSliceInstance(hashIn.get(tile), xstart, ystart);
			if (inS == null) {
				return null;
			} else if (inS != null &&  inS.getAttribute("FFX") != null && inS.getAttribute("FFX").getValue() != null && inS.getAttribute("FFX").getValue().equals("#OFF")
					&& inS.getAttribute("FFY") != null && inS.getAttribute("FFY").getValue() != null &&inS.getAttribute("FFY").getValue().equals("#OFF")) {
				return null;
			} else {
				ArrayList<Pair<Instance, Attribute>> Sync = new ArrayList<Pair<Instance, Attribute>>();
				if (inS != null && inS.getAttribute("FFX") != null && inS.getAttribute("FFX").getValue() != null && !inS.getAttribute("FFX").getValue().equals("#OFF")) {
					Pair<Instance, Attribute> p = new Pair<Instance, Attribute>(inS, inS.getAttribute("FFX"));
					Sync.add(p);
				}
				if (inS != null && inS.getAttribute("FFY") != null && inS.getAttribute("FFY").getValue() != null && !inS.getAttribute("FFY").getValue().equals("#OFF")) {
					Pair<Instance, Attribute> p = new Pair<Instance, Attribute>(inS, inS.getAttribute("FFY"));
					Sync.add(p);
				}
				return Sync;
			}
		} else {
			k = (hash.get(locationName + "_" + attr.getPhysicalName().toString()));
		}
		Dgraph dg = new Dgraph();
		dg.index = -1;
		dg.X = xstart;
		dg.Y = ystart;
		dg.Componet = attr.getPhysicalName();
		dg.Value = attr.getValue();
		dg.parent = null;
		dg.tile = tile;
		if (hashIn.get(tile) == null) {
			dg.InstanceInDesign = null;
		} else {
			dg.InstanceInDesign = getSliceInstance(hashIn.get(tile), xstart, ystart);
		}
		dg.next = buildDgraph(dg, k, hashIn);
		dg.curIndex = 0;
		query.add(dg);
		int querySize = query.size();
		
		// builds the dependency graph
		for (int queryIndex = 0; queryIndex < querySize; queryIndex++) {
			Dgraph ndg = query.get(queryIndex);
			if (ndg != null && ndg.next != null) {
				for (Dgraph ndgite : ndg.next) {
					if (ndgite == null)
						break;
					String tempie = ((String) hash.keySet().toArray()[0]); // reference
																			// to
																			// see
																			// if
																			// need
																			// to
																			// rebuild
																			// reference
																			// table
					int hashRef = Integer.parseInt(tempie.substring(tempie.indexOf("Y") + 1, tempie.indexOf("_")));// y
																													// value
					Dgraph newdg = new Dgraph();
					newdg = ndgite;
					
					if (((hashRef % 2 == 0) && ((hashRef > newdg.Y) || (hashRef + 1 < newdg.Y)))
							|| ((hashRef % 2 == 1) && ((hashRef < newdg.Y) || (hashRef - 1 > newdg.Y)))) {
						hash = buildCLBV4("X" + newdg.X + "Y" + newdg.Y);
					}
					
					k = (hash.get("X" + newdg.X + "Y" + newdg.Y + "_" + newdg.Componet.toString()));
					if (k != null) {
						newdg.next = buildDgraph(newdg, k, hashIn);
						newdg.index = queryIndex;
						Attribute ak = new Attribute("", "", "");
						ak.setPhysicalName(newdg.Componet);
						ak.setValue(newdg.Value);
						if (inDesign(newdg, hashIn, ak)) {
							newdg.curIndex = querySize;
							querySize++;
							query.add(newdg);
						}
					}
					if (25000 == queryIndex) {
						msg("Debug to prevent Memory overflow " + k);
						end();
					}
				}
			}
		}
		ArrayList<Pair<Instance, Attribute>> apairs = new ArrayList<Pair<Instance, Attribute>>();
		apairs = DP(query, hashIn, 0);
		
		boolean debug = false;
		;
		/*
		 * if(longPath==4||longPath==6||longPath==37||longPath==38||longPath==116
		 * ||longPath==118
		 * ||longPath==119||longPath==120||longPath==121||longPath
		 * ==122||longPath==143||longPath==150
		 * ||longPath==397||longPath==549||longPath
		 * ==554||longPath==561||longPath==622||longPath==676
		 * ||longPath==679||longPath
		 * ==678||longPath==5142||longPath==5139||longPath==5145||longPath==5151
		 * ||longPath==5153)
		 */
		String testing = "DEBUG OFF";
		if (attr.getPhysicalName().equals(testing)) // &&getSliceInstance(hashIn.get(tile),
													// xstart,
													// ystart).getAttribute("BYINV").equals("#OFF"))
		{
			debug = true;
		}
		if (debug == true) {
			// Debug
			msg("");
			msg("   Tile: " + tile.getName() + " v4slicebits: X:" + sbit.getX() + " Y:" + sbit.getY()
					+ " v4configbits: tile " + bit.getTile() + " " + bit.getTile().getPrimitiveSites().length
					+ " Attribute: " + attr + " init loc X" + pri.getInstanceX() + "Y" + pri.getInstanceY());
			printquery(query, hashIn);
			for (Pair<Instance, Attribute> pa : apairs) {
				msg("Instance " + pa.Left.getPrimitiveSiteName() + " Attribute " + pa.Right.getPhysicalName()
						+ " AttributeValue" + pa.Left.getAttributeValue(pa.Right.getPhysicalName()));
			}
		}
		int count = 0;
		for (Dgraph d = query.get(query.size() - 1); d != null;) {
			count++;
			if (d.index == -1) {
				break;
			}
			d = query.get(d.index);
		}
		if (count > longestTakenPath) {
			longestTakenPath = count;
		}
		
		// if(longPath==21389)//3000)//4492)//9418)//13418)//17221)//21390)
		// {msg("End of Testing strings!");end();}
		
		// if(attr.getPhysicalName().equals(testing)&&
		// dg.InstanceInDesign!=null){
		// if(dg.InstanceInDesign.getAttribute("SRFFMUX")!=null)
		// if(!dg.InstanceInDesign.getAttribute("SRFFMUX").getValue().equals("#OFF")){
		// if(apairs.size()!=0){
		// end();
		// }
		// }
		return apairs;
	}
}

/*
 * These function seem to be obsolete
 * 
 * private ArrayList<Pair<Instance,Attribute>> DP(ArrayList<Dgraph>
 * dlist,HashMap<Tile,ArrayList<Instance>> hashIn, int startindex) {
 * ArrayList<Pair<Instance,Attribute>> array=new
 * ArrayList<Pair<Instance,Attribute>>(); boolean revTest=false; for(Dgraph
 * d:dlist) { if(d.index>startindex){break;} else if(d.index==startindex)
 * //looks only at the directly related path { revTest=false; Attribute at = new
 * Attribute("","",""); at.setPhysicalName(d.Componet); at.setValue(d.Value);
 * Instance temp; String Val=""; for(Dgraph
 * findVal:d.parent.next){if(findVal.Componet
 * .equals(d.Componet)){Val=findVal.Value;break;}}
 * 
 * temp=getSliceInstance(hashIn.get(d.tile),d.X,d.Y);
 * if(d.Componet.equals("FFX")||d.Componet.equals("FFY")){ if(d.parent!=null &&
 * temp!=null){
 * if(d.parent.Componet.equals("REVUSED")||d.parent.Componet.equals("CEINV")||
 * d.
 * parent.Componet.equals("SRINV")||d.parent.Componet.equals("CLKINV")){revTest
 * =true;}}} else{revTest=false; } if(revTest){ Pair<Instance,Attribute> nwpair
 * = new Pair<Instance,Attribute>(temp,temp.getAttribute(d.Componet));
 * array.add(nwpair); } else if(temp==null){ //
 * msg("attr: "+at+" DB: "+checkDBForDefaultValue
 * (at)+" Curr D comp "+d.Componet+" par "+d.parent.Componet);
 * 
 * if(checkDBForDefaultValue(at)==null){ ArrayList<Pair<Instance,Attribute>>
 * arrayL= DP(dlist,hashIn, d.curIndex); for(Pair<Instance,Attribute> p:arrayL)
 * {array.add(p);} } else if(checkDBForDefaultValue(at).equals("OFF")){} else if
 * (checkDBForDefaultValue(at).equals(d.parent.Componet)) //could affect design
 * { ArrayList<Pair<Instance,Attribute>> arrayL= DP(dlist,hashIn, d.curIndex);
 * for(Pair<Instance,Attribute> p:arrayL) {array.add(p);} }
 * 
 * } else if(d.Componet.equals("FFX")){
 * if(!temp.getAttribute("FFX").equals("OFF")){ Pair<Instance,Attribute> nwpair
 * = new Pair<Instance,Attribute>(temp,temp.getAttribute(d.Componet));
 * array.add(nwpair); } } else if(d.Componet.equals("FFY")){
 * if(!temp.getAttribute("FFY").equals("OFF")){ Pair<Instance,Attribute> nwpair
 * = new Pair<Instance,Attribute>(temp,temp.getAttribute(d.Componet));
 * array.add(nwpair); } } else if(temp.hasAttribute(d.Componet)){ //has the
 * componet in the design
 * if(temp.getAttribute(d.Componet).getValue().equals("#OFF")){
 * if(checkDBForDefaultValue(at)==null){ ArrayList<Pair<Instance,Attribute>>
 * arrayL= DP(dlist,hashIn, d.curIndex); for(Pair<Instance,Attribute> p:arrayL)
 * {array.add(p);} } else if(checkDBForDefaultValue(at).equals("OFF")){} //error
 * is here else if (checkDBForDefaultValue(at).equals(Val)) {
 * if(d.Componet.equals("DXMUX") &&
 * temp.getAttribute("FFX").getValue().equals("OFF")){} else
 * if(d.Componet.equals("DYMUX")&&
 * temp.getAttribute("FFY").getValue().equals("OFF")){} else
 * if(d.Componet.equals("CYINIT")){ if(DP(dlist,hashIn, d.curIndex)!=null){
 * Pair<Instance,Attribute> nwpair = new
 * Pair<Instance,Attribute>(temp,temp.getAttribute(d.Componet));
 * array.add(nwpair); } } else if(DP(dlist,hashIn, d.curIndex)!=null){
 * Pair<Instance,Attribute> nwpair = new
 * Pair<Instance,Attribute>(temp,temp.getAttribute(d.Componet));
 * array.add(nwpair); } } } else
 * if(temp.getAttribute(d.Componet).getPhysicalName().equals("DIG_MUX")){
 * if(d.parent.Componet.equals("BYINV") &&
 * temp.getAttribute(d.Componet).getValue().equals("BY")){
 * Pair<Instance,Attribute> nwpair = new
 * Pair<Instance,Attribute>(temp,temp.getAttribute(d.Componet));
 * array.add(nwpair); } if(d.parent.Componet.equals("DIGUSED") &&
 * temp.getAttribute(d.Componet).getValue().equals("ALTDIG")){
 * if(getSliceInstance
 * (hashIn.get(d.tile),d.parent.X,d.parent.Y).getAttribute(d.parent
 * .Componet).getValue().equals("0")){ Pair<Instance,Attribute> nwpair = new
 * Pair<Instance,Attribute>(temp,temp.getAttribute(d.Componet));
 * array.add(nwpair); } } if(d.parent.Componet.equals("SHIFTOUTUSED") &&
 * temp.getAttribute(d.Componet).getValue().equals("SHIFTIN")){
 * if(getSliceInstance
 * (hashIn.get(d.tile),d.parent.X,d.parent.Y).getAttribute(d.parent
 * .Componet).getValue().equals("0")){ Pair<Instance,Attribute> nwpair = new
 * Pair<Instance,Attribute>(temp,temp.getAttribute(d.Componet));
 * array.add(nwpair); } } } else
 * if(temp.getAttribute(d.Componet).getPhysicalName().equals("DIF_MUX")){
 * if(d.parent.Componet.equals("DIG_MUX") &&
 * temp.getAttribute(d.Componet).getValue().equals("ALTDIF")){
 * if(temp.getAttribute
 * ("F").getValue().contains("#RAM")||temp.getAttribute("F_ATTR"
 * ).equals("SHIFT_REG")){ Pair<Instance,Attribute> nwpair = new
 * Pair<Instance,Attribute>(temp,temp.getAttribute(d.Componet));
 * array.add(nwpair); } } else if(d.parent.Componet.equals("G") &&
 * temp.getAttribute(d.Componet).getValue().equals("SHIFTIN")){
 * if(temp.getAttribute
 * ("F").getValue().contains("#RAM")||temp.getAttribute("F_ATTR"
 * ).equals("SHIFT_REG")){ Pair<Instance,Attribute> nwpair = new
 * Pair<Instance,Attribute>(temp,temp.getAttribute(d.Componet));
 * array.add(nwpair); } } else if(d.parent.Componet.equals("BXINV") &&
 * temp.getAttribute(d.Componet).getValue().equals("BX")) {
 * if(temp.getAttribute(
 * "F").getValue().contains("#RAM")||temp.getAttribute("F_ATTR"
 * ).equals("SHIFT_REG")){ Pair<Instance,Attribute> nwpair = new
 * Pair<Instance,Attribute>(temp,temp.getAttribute(d.Componet));
 * array.add(nwpair); } } } else
 * if(temp.getAttribute(d.Componet).getPhysicalName().equals("G")){
 * if(temp.getAttribute
 * (d.Componet).getValue().contains("#RAM")||temp.getAttribute
 * ("G_ATTR").equals("SHIFT_REG")){ Pair<Instance,Attribute> nwpair = new
 * Pair<Instance,Attribute>(temp,temp.getAttribute(d.Componet));
 * array.add(nwpair); } } else
 * if(temp.getAttribute(d.Componet).getPhysicalName().equals("F")){
 * if(temp.getAttribute
 * (d.Componet).getValue().contains("#RAM")||temp.getAttribute
 * ("F_ATTR").equals("SHIFT_REG")){ Pair<Instance,Attribute> nwpair = new
 * Pair<Instance,Attribute>(temp,temp.getAttribute(d.Componet));
 * array.add(nwpair); } } else
 * if(temp.getAttribute(d.Componet).getPhysicalName().equals("DYMUX")){ //
 * msg(temp.getAttribute(d.Componet)+" "+d.parent.Componet);end();
 * if(d.parent.Componet.equals("BYINV") &&
 * temp.getAttribute(d.Componet).getValue().equals("BY")){
 * Pair<Instance,Attribute> nwpair = new
 * Pair<Instance,Attribute>(temp,temp.getAttribute(d.Componet));
 * array.add(nwpair); } else if(d.parent.Componet.equals("G") &&
 * temp.getAttribute(d.Componet).getValue().equals("Y")){
 * Pair<Instance,Attribute> nwpair = new
 * Pair<Instance,Attribute>(temp,temp.getAttribute(d.Componet));
 * array.add(nwpair); } else if(d.parent.Componet.equals("GYMUX") &&
 * temp.getAttribute(d.Componet).getValue().equals("YMUX")){
 * Pair<Instance,Attribute> nwpair = new
 * Pair<Instance,Attribute>(temp,temp.getAttribute(d.Componet));
 * array.add(nwpair); } else if(d.parent.Componet.equals("YBMUX") &&
 * temp.getAttribute(d.Componet).getValue().equals("YB")){
 * Pair<Instance,Attribute> nwpair = new
 * Pair<Instance,Attribute>(temp,temp.getAttribute(d.Componet));
 * array.add(nwpair); } } else
 * if(temp.getAttribute(d.Componet).getValue().equals(d.Value)) {
 * if(d.Componet.contains("USED") && !d.Componet.contains("W")) {
 * if((d.Componet.contains("X")||d.Componet.contains("Y"))&&
 * temp.getAttribute(d.Componet).getValue().equals("0")) {
 * Pair<Instance,Attribute> nwpair = new
 * Pair<Instance,Attribute>(temp,temp.getAttribute(d.Componet));
 * array.add(nwpair); } } } } else
 * if(d.Componet.equals("XORF")||(d.Componet.equals("XORG"))) {
 * ArrayList<Pair<Instance,Attribute>> arrayL= DP(dlist,hashIn, d.curIndex);
 * for(Pair<Instance,Attribute> p:arrayL) {array.add(p);} } else
 * if(d.Componet.equals("F5MUX")){ ArrayList<Pair<Instance,Attribute>> arrayL=
 * DP(dlist,hashIn, d.curIndex); for(Pair<Instance,Attribute> p:arrayL)
 * {array.add(p);} } else if(d.Componet.equals("F6MUX")){
 * ArrayList<Pair<Instance,Attribute>> arrayL= DP(dlist,hashIn, d.curIndex);
 * for(Pair<Instance,Attribute> p:arrayL) {array.add(p);} } else
 * if(d.Componet.equals("CYMUXF")){ ArrayList<Pair<Instance,Attribute>> arrayL=
 * DP(dlist,hashIn, d.curIndex); for(Pair<Instance,Attribute> p:arrayL)
 * {array.add(p);} } else if(d.Componet.equals("CYMUXG")){
 * ArrayList<Pair<Instance,Attribute>> arrayL= DP(dlist,hashIn, d.curIndex);
 * for(Pair<Instance,Attribute> p:arrayL) {array.add(p);} } else
 * if(d.Componet.contains("USED") && !d.Componet.contains("W")){ //probally
 * doesn't get here test more if(d.Componet.contains("BYOUT") && d.Y%2==0){
 * if(temp.hasAttribute("SLICEWE1USED")){ Pair<Instance,Attribute> nwpair = new
 * Pair<Instance,Attribute>(temp,temp.getAttribute("SLICEWE1USED"));
 * array.add(nwpair); } } else if(d.Componet.contains("BYINV") && d.Y%2==0){
 * temp =getSliceInstance(hashIn.get(d.tile),d.X,d.Y+1); if(temp==null){} else
 * if(temp.hasAttribute("SLICEWE1USED")) { Pair<Instance,Attribute> nwpair = new
 * Pair<Instance,Attribute>(temp,temp.getAttribute("SLICEWE1USED"));
 * array.add(nwpair);
 * 
 * } } else if(d.InstanceInDesign.getAttribute(d.Componet)==null){} else
 * if(d.InstanceInDesign.getAttribute(d.Componet).getValue().equals("0")){
 * Pair<Instance,Attribute> nwpair = new
 * Pair<Instance,Attribute>(temp,temp.getAttribute("SLICEWE1USED"));
 * array.add(nwpair); } else{ msg("ErrorState"+longPath+" "+d.Componet
 * +" "+d.curIndex); end(); } } } else{} } return array; }
 * 
 * 
 * 
 * 
 * //Debug Checks private void verifyHashMap(HashMap<String,ArrayList<String>>
 * hash) { System.out.println(hash.size()+" "+hash.keySet()); } private void
 * verifyIOMap(String s, ArrayList<String> list) { System.out.println(s +
 * " "+list.size()+" "+list.toString()); } //Calculates the Dependencies in a
 * primitive using the xdlrc to associate the outputs private void
 * getHashmap(PrimitiveDef k,HashMap<String, ArrayList<String>> hash) { for
 * (Element elem:k.getElements()) { boolean inVal =true; ArrayList<String> arr
 * =new ArrayList<String>(); for(Connection conn:elem.getConnections()) { String
 * key = ""; if(conn.isForwardConnection()) { if (inVal) { key =
 * (conn.getElement0()); inVal=false; }
 * arr.add(conn.getElement1()+" "+conn.getPin1()); hash.put(key, arr); } } } }
 * 
 * //method to return the Dependency for SliceL public HashMap<String,
 * ArrayList<String>> V4dependancySliceLMap() { PrimitiveDef k=
 * def.getPrimitiveDef(PrimitiveType.SLICEL); HashMap<String, ArrayList<String>>
 * hashL =new HashMap<String, ArrayList<String>>(); getHashmap( k,hashL); return
 * hashL; } //method to return the Dependency for SliceM public HashMap<String,
 * ArrayList<String>> V4dependancySliceMMap() { PrimitiveDef k=
 * def.getPrimitiveDef(PrimitiveType.SLICEM); HashMap<String, ArrayList<String>>
 * hashM =new HashMap<String, ArrayList<String>>(); getHashmap( k,hashM); return
 * hashM; } //method for a generic primitive dependency map public
 * HashMap<String, ArrayList<String>> V4dependancyMap(PrimitiveDef k) {
 * HashMap<String, ArrayList<String>> hash =new HashMap<String,
 * ArrayList<String>>(); getHashmap( k,hash); return hash; } //method for
 * returning the list of dependency for a given list public ArrayList<String>
 * getAttributeDependencies(PrimitiveDef k,String componentName) {
 * ArrayList<String> list = new ArrayList<String>(); list =
 * V4dependancyMap(k).get(componentName); return list; } //testing method
 * 
 * @SuppressWarnings("unchecked") private
 * HashMap<String,ArrayList<V4ConfigurationBit>> InputHash (String attr,
 * Virtex4SliceBits sbit) { HashMap<String, ArrayList<V4ConfigurationBit>> hash
 * = new HashMap<String, ArrayList<V4ConfigurationBit>>(); boolean cnt =true;
 * String key=""; for(String k:SliceLinputs.keySet()) {
 * if(k.substring(0,k.indexOf(" ")).equals(attr)) { if (cnt==true) { cnt=false;
 * key=((k.substring(0, k.indexOf(" "))+"::#OFF"));
 * hash.put(key,(ArrayList<V4ConfigurationBit>) (sbit.getDB().get(key))); //
 * msg(key+" "+hash.put(key,(ArrayList<V4ConfigurationBit>)
 * (sbit.getDB().get(key)))); } key=((k.substring(0,
 * k.indexOf(" "))+"::"+k.substring(k.indexOf(" ")+1)));
 * hash.put(key,(ArrayList<V4ConfigurationBit>) (sbit.getDB().get(key)));
 * //msg(key+" "+hash.put(key,(ArrayList<V4ConfigurationBit>)
 * (sbit.getDB().get(key)))); } } return hash; }
 */

/*
 * possible use to find out what the bit upset changed to Attribute a =null;
 * //Assuption is bit given is the changed configuration help find the attribute
 * error in question Test:for(String Choices:sbit.getAttributeNameFromBit(bit))
 * { a = new Attribute(Choices.substring(0,Choices.indexOf("::") ), "",
 * Choices.substring(Choices.indexOf("::")+2)); for(V4ConfigurationBit
 * v4conf:sbit.getBits(a))//assumption same ordering for bits {
 * if(v4conf==bit){msg("Match found");
 * msg("Attr ="+Choices+" Vals"+sbit.getBits(a));
 * msg(v4conf+" Value="+v4conf.getAttributeValue()); break Test; } } }
 */

/**
 * Attribute a = new Attribute("","",""); a.setPhysicalName("BXINV");
 * msg(a.getPhysicalName()+" "+checkDBForDefaultValue(a));
 * a.setPhysicalName("BYINV");
 * msg(a.getPhysicalName()+" "+checkDBForDefaultValue(a));
 * a.setPhysicalName("CEINV");
 * msg(a.getPhysicalName()+" "+checkDBForDefaultValue(a));
 * a.setPhysicalName("CLKINV");
 * msg(a.getPhysicalName()+" "+checkDBForDefaultValue(a));
 * a.setPhysicalName("CY0F");
 * msg(a.getPhysicalName()+" "+checkDBForDefaultValue(a));
 * a.setPhysicalName("CY0G");
 * msg(a.getPhysicalName()+" "+checkDBForDefaultValue(a));
 * a.setPhysicalName("DIF_MUX");
 * msg(a.getPhysicalName()+" "+checkDBForDefaultValue(a));
 * a.setPhysicalName("DIG_MUX");
 * msg(a.getPhysicalName()+" "+checkDBForDefaultValue(a));
 * a.setPhysicalName("DXMUX");
 * msg(a.getPhysicalName()+" "+checkDBForDefaultValue(a));
 * a.setPhysicalName("DYMUX");
 * msg(a.getPhysicalName()+" "+checkDBForDefaultValue(a));
 * a.setPhysicalName("F");
 * msg(a.getPhysicalName()+" "+checkDBForDefaultValue(a));
 * a.setPhysicalName("FFX");
 * msg(a.getPhysicalName()+" "+checkDBForDefaultValue(a));
 * a.setPhysicalName("FFX_INIT_ATTR");
 * msg(a.getPhysicalName()+" "+checkDBForDefaultValue(a));
 * a.setPhysicalName("FFX_SR_ATTR");
 * msg(a.getPhysicalName()+" "+checkDBForDefaultValue(a));
 * a.setPhysicalName("FFY");
 * msg(a.getPhysicalName()+" "+checkDBForDefaultValue(a));
 * a.setPhysicalName("FFY_INIT_ATTR");
 * msg(a.getPhysicalName()+" "+checkDBForDefaultValue(a));
 * a.setPhysicalName("FFY_SR_ATTR");
 * msg(a.getPhysicalName()+" "+checkDBForDefaultValue(a));
 * a.setPhysicalName("FXMUX");
 * msg(a.getPhysicalName()+" "+checkDBForDefaultValue(a));
 * a.setPhysicalName("F_ATTR");
 * msg(a.getPhysicalName()+" "+checkDBForDefaultValue(a));
 * a.setPhysicalName("G");
 * msg(a.getPhysicalName()+" "+checkDBForDefaultValue(a));
 * a.setPhysicalName("GYMUX");
 * msg(a.getPhysicalName()+" "+checkDBForDefaultValue(a));
 * a.setPhysicalName("G_ATTR");
 * msg(a.getPhysicalName()+" "+checkDBForDefaultValue(a));
 * a.setPhysicalName("REVUSED");
 * msg(a.getPhysicalName()+" "+checkDBForDefaultValue(a));
 * a.setPhysicalName("SLICEWE0USED");
 * msg(a.getPhysicalName()+" "+checkDBForDefaultValue(a));
 * a.setPhysicalName("SLICEWE1USED");
 * msg(a.getPhysicalName()+" "+checkDBForDefaultValue(a));
 * a.setPhysicalName("SRINV");
 * msg(a.getPhysicalName()+" "+checkDBForDefaultValue(a));
 * a.setPhysicalName("SYNC_ATTR");
 * msg(a.getPhysicalName()+" "+checkDBForDefaultValue(a));
 * a.setPhysicalName("XBMUX");
 * msg(a.getPhysicalName()+" "+checkDBForDefaultValue(a));
 * a.setPhysicalName("YBMUX");
 * msg(a.getPhysicalName()+" "+checkDBForDefaultValue(a)); end();
 **/

/*
 * old code private ArrayList<Pair<Instance,Attribute>>
 * directProblems(ArrayList<Dgraph> dlist,HashMap<Tile,ArrayList<Instance>>
 * hashIn) { ArrayList<Pair<Instance,Attribute>> array=new
 * ArrayList<Pair<Instance,Attribute>>(); for(Dgraph d:dlist) { Attribute at =
 * new Attribute("","",""); at.setPhysicalName(d.Componet);
 * at.setValue(d.Value); if(d.index!=-1)
 * msg("Parent values "+dlist.get(d.index).Componet+" "+dlist.get(d.index).Value
 * +" Attr "+at + " "+at.getPhysicalName()+" "+at.getValue()+" "+
 * SliceMinputs.get(at.getPhysicalName()+" "+at.getValue())); Instance temp;
 * temp=getSliceInstance(hashIn.get(d.tile),d.X,d.Y);
 * //System.out.print(at+"\t"); if(d.index==-1){} else if(d.index==0)
 * //getSliceInstance(ArrayList<Instance> array, int x , int y) {
 * if(temp.hasAttribute(d.Componet)){ //has the componet in the design
 * if(temp.getAttribute(d.Componet).getValue().equals(d.Value)){
 * Pair<Instance,Attribute> nwpair = new
 * Pair<Instance,Attribute>(temp,temp.getAttribute(d.Componet));
 * array.add(nwpair); } } else if(d.Componet.equals("XORF")) //special xor case
 * always exist so check following element {
 * //msg("FXMUX Attr= "+temp.getAttribute("FXMUX"));
 * if(temp.hasAttribute("FXMUX") &&
 * temp.getAttribute("FXMUX").getValue().equals("FXOR")) {
 * 
 * at.setPhysicalName("FXMUX"); at.setValue("FXOR"); Pair<Instance,Attribute>
 * nwpair = new Pair<Instance,Attribute>(temp,at); array.add(nwpair); } } else
 * if(d.Componet.equals("XORG")) //the other special xor case {
 * if(temp.hasAttribute("GYMUX")) { //
 * msg("GYMUX Attr= "+temp.getAttribute("GYMUX"));
 * if(temp.getAttribute("GYMUX").getValue().equals("GXOR")) {
 * at.setPhysicalName("GYMUX"); at.setValue("GXOR"); Pair<Instance,Attribute>
 * nwpair = new Pair<Instance,Attribute>(temp,at); array.add(nwpair); } } } else
 * if(d.Componet.equals("F5MUX")) {
 * //msg("parentloc of f5mux is :"+dlist.get(d.index
 * ).Componet+" indexof "+d.index); //end(); if(temp.hasAttribute("BXINV"))
 * //can be either inputs to mux { if(temp.hasAttribute("G") ||
 * temp.hasAttribute("F")) { at.setPhysicalName(d.Componet); at.setValue("");
 * Pair<Instance,Attribute> nwpair = new Pair<Instance,Attribute>(temp,at);
 * array.add(nwpair); } } else //uses default of BX_B or zero assuming no input
 * default input is G { if(dlist.get(d.index).Componet.equals("G")) {
 * at.setPhysicalName("F5MUX"); at.setValue("0"); Pair<Instance,Attribute>
 * nwpair = new Pair<Instance,Attribute>(temp,at); array.add(nwpair); } } } else
 * if(d.Componet.equals("F6MUX")){ if(temp.hasAttribute("BYINV")) {
 * 
 * } else //default of 0 since byinv off is defaulted to by_b inverted input {
 * for(Net net:temp.getNetList()) { msg(net.getSource()); }end();
 * 
 * } } else if(d.Componet.equals("CYMUXF")){} else
 * if(d.Componet.equals("CYMUXG")){}
 * 
 * } else{break;} } msg(""); for(Pair<Instance,Attribute> par:array)
 * msg(par.Left.getPrimitiveSiteName()+" "+par.Right); return array; }
 */

/*
 * private String checkDBForDefaultValue(Attribute attr) {
 * if(attr.getPhysicalName().equals("G") ||
 * attr.getPhysicalName().equals("F"))//Default for Luts not found in database {
 * return "0"; }
 * 
 * db= new BitstreamDB(); db.openConnection();
 * 
 * String query =
 * "SELECT * FROM `Virtex4LogicBits` WHERE `PrimLocName` ="+" 'SLICE_X0Y0' AND"
 * +" `AttrName` = '"+ attr.getPhysicalName()+"'"+
 * " AND `AttrValue` = '#OFF' ORDER BY `Virtex4LogicBits`.`AttrValue` ASC ";
 * 
 * String query2 =
 * "SELECT * FROM `Virtex4LogicBits` WHERE `PrimLocName` ="+" 'SLICE_X0Y0' AND"
 * +" `AttrName` = '"+
 * attr.getPhysicalName()+"'"+" ORDER BY `Virtex4LogicBits`.`AttrValue` ASC ";
 * 
 * ResultSet rs = db.makeQuery(query); ArrayList <Pair<String,Integer>> rsPair
 * =new ArrayList<Pair<String,Integer>>();
 * 
 * try { if(!rs.first()) { db.closeConnection(); return null;} rs.first();
 * rsPair.add(new Pair<String,
 * Integer>((rs.getString("MinorAddr")+"_"+rs.getString("Offset")+"_"+
 * rs.getString("Mask")),(int)rs.getInt("BitValue"))); while(rs.next()) {
 * rsPair.add(new Pair<String,
 * Integer>((String)(rs.getString("MinorAddr")+"_"+rs.getString("Offset")+
 * "_"+rs.getString("Mask")),(int)rs.getInt("BitValue"))); } }catch
 * (SQLException e) { e.printStackTrace(); }
 * 
 * HashMap<String,String> hash = new HashMap<String, String>(); rs =
 * db.makeQuery(query2); try { LinkedList<String> InvalidList = new
 * LinkedList<String>(); rs.first(); if(!rs.getString(3).equals("#OFF")){ //
 * msg(
 * "RS2= "+rs.getString(2)+" "+rs.getString(3)+" "+rs.getInt("MinorAddr")+" " //
 * +rs.getInt("Offset")+" "+rs.getInt("Mask")+" "+rs.getInt("BitValue")); String
 * loc = rs.getInt("MinorAddr")+"_"+rs.getInt("Offset")+"_"+rs.getInt("Mask");
 * for(Pair<String,Integer> pa:rsPair) { if(pa.Left.equals(loc)){
 * //msg("#OFF = "+pa.Right+" Cur="+rs.getInt("BitValue"));
 * if(pa.Right==rs.getInt("BitValue")) { hash.put(rs.getString(3), ""); break; }
 * hash.remove(rs.getString(3)); InvalidList.add(rs.getString(3)); break; } } }
 * while(rs.next()) { if(!rs.getString(3).equals("#OFF") &&
 * !InvalidList.contains(rs.getString(3))){ //
 * msg("RS2= "+rs.getString(2)+" "+rs
 * .getString(3)+" "+rs.getInt("MinorAddr")+" " //
 * +rs.getInt("Offset")+" "+rs.getInt("Mask")+" "+rs.getInt("BitValue")); String
 * loc = rs.getInt("MinorAddr")+"_"+rs.getInt("Offset")+"_"+rs.getInt("Mask");
 * 
 * for(Pair<String,Integer> pa:rsPair) { if(pa.Left.equals(loc)) {
 * if(pa.Right==rs.getInt("BitValue")) { hash.put(rs.getString(3), ""); break; }
 * hash.remove(rs.getString(3)); InvalidList.add(rs.getString(3)); break; } } }
 * } } catch (SQLException e) { e.printStackTrace(); } db.closeConnection();
 * 
 * if(hash.isEmpty()){ return "#OFF"; } else{
 * return(hash.keySet().toArray()[0].toString()); } }
 */

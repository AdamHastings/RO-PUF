import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import XDLDesign.Utils.XDLDesignUtils;
import edu.byu.ece.gremlinTools.Virtex4Bits.Virtex4SwitchBox;
import edu.byu.ece.gremlinTools.xilinxToolbox.V4XilinxToolbox;
import edu.byu.ece.gremlinTools.xilinxToolbox.XilinxToolboxLookup;
import edu.byu.ece.rapidSmith.bitstreamTools.configuration.FPGA;
import edu.byu.ece.rapidSmith.bitstreamTools.configurationSpecification.DeviceLookup;
import edu.byu.ece.rapidSmith.design.Design;
import edu.byu.ece.rapidSmith.design.Instance;
import edu.byu.ece.rapidSmith.design.Module;
import edu.byu.ece.rapidSmith.design.ModuleInstance;
import edu.byu.ece.rapidSmith.design.Net;
import edu.byu.ece.rapidSmith.design.NetType;
import edu.byu.ece.rapidSmith.design.PIP;
import edu.byu.ece.rapidSmith.design.Pin;
import edu.byu.ece.rapidSmith.device.PrimitiveSite;
import edu.byu.ece.rapidSmith.device.PrimitiveType;
import edu.byu.ece.rapidSmith.device.Tile;
import edu.byu.ece.rapidSmith.device.WireEnumerator;
import edu.byu.ece.rapidSmith.util.FileConverter;


public class ringOscillatorBuilder {
	public static final String hardMacroFileName = "inverter.xdl";
	public static final int NUM_INVERTERS = 51;
	
	public static void main(String[] args)
	{	
		FPGA fpga = new FPGA(DeviceLookup.lookupPartV4V5V6("XC4VLX60"));
		Design hardDesign = new Design(true);
		String workingDirectory = "E:\\Adam\\Characterizing\\ring_oscillator_hm\\";
		String instancePath = "ring_osc_0/ring_osc_0/USER_LOGIC_I/";
		String hardMacroPath = workingDirectory + "inverter.xdl";
		String designPath = workingDirectory;
		String modifiedDesign = workingDirectory + "system_modified.xdl";
		String userLogicPath = instancePath;
		String ringOscName = "ring_osc_";
		hardDesign.loadXDLFile(hardMacroPath);
		int placementRow = 0;
		int placementColumn = 0;
			
		Design design = new Design(false); //Not a hard macro design
		
		V4XilinxToolbox TB = (V4XilinxToolbox) XilinxToolboxLookup.toolboxLookup(fpga.getDeviceSpecification());
		Virtex4SwitchBox SB = new Virtex4SwitchBox(fpga, design, TB);
		
		ArrayList<PIP> RemovePIPs =new ArrayList<PIP>();
   	    
   	    //DEACTIVATE PIPs
   	    SB.DeactivatePIPs(RemovePIPs);
		
		FileConverter.convertNCD2XDL(designPath + "system.ncd");
		design.loadXDLFile(designPath + "system.xdl");
		
		WireEnumerator We = design.getWireEnumerator();
		
		for (Net n : hardDesign.getModule("ring_oscillator").getNets())
		{
			for (Pin p : n.getPins())
			{
				String pinName = p.toString();
				if (!pinName.contains("G1") && 
					!pinName.contains("G2") &&
					!pinName.contains("G4"))
				{
					hardDesign.removeNet(n);
				}
			}
		}
		
		//Find an instance of the inverter hard macro
		Module inverterMod = hardDesign.getHardMacro();
		
		
		PrimitiveType type = inverterMod.getAnchor().getType();
		
		PrimitiveSite[] sites =
				 design.getDevice().getAllCompatibleSites(type);
		
		//Create num_inverters number of instances of that hard macro
		//placementTemp holds the starting position of the placer
		int placementTemp = 2 * (127 - placementRow) * 52 + 2*placementColumn;
		
		//Find the inverter already placed. Unplace it and place it later with
		//the other instances.
		Instance placedInverter = design.getInstance(instancePath + ringOscName + "2/$COMP_0");
		//Instance placedInverter = design.getModule("inverter").getInstance("$COMP_0");
		//The input to the LUT is changed via Synthesis. Changing it back here:
		
		placedInverter.removeAttribute("G");
		placedInverter.addAttribute("G", "ring_osc_0/ring_osc_0/USER_LOGIC_I/ring_osc_2/$COMP_0.G",
				"#LUT:D=A1*A2*~A3*A4");
		placedInverter.unPlace();
		
		//Add one net for each instance of the inverters
		ArrayList<Net> nets = new ArrayList<Net>();
		
		//Add additional nets (3 per tile) to tie off the other inputs of the LUT
		ArrayList<Net> input1 = new ArrayList<Net>();
		ArrayList<Net> input2 = new ArrayList<Net>();
		ArrayList<Net> input3 = new ArrayList<Net>();
		
		Net enableNet = new Net();
		enableNet = design.getNet(instancePath + "ring_osc_buf1");
		enableNet.setName("net_ring_osc_2");
		for (int i = 0; i < NUM_INVERTERS; i++)
		{
				Net newNet = new Net("net_" + ringOscName + Integer.toString(i), NetType.WIRE);
				Net inputNet1 = new Net("net_" + ringOscName + Integer.toString(i) + "_G1", NetType.VCC);
				Net inputNet2 = new Net("net_" + ringOscName + Integer.toString(i) + "_G2", NetType.VCC);
				Net inputNet3 = new Net("net_" + ringOscName + Integer.toString(i) + "_G4", NetType.VCC);
				if (i == 2)
				{
					nets.add(enableNet);
					input1.add(inputNet1);
					input2.add(inputNet2);
					input3.add(inputNet3);
					design.addNet(inputNet1);
					design.addNet(inputNet2);
					design.addNet(inputNet3);
					continue;
				}

				nets.add(newNet);
				input1.add(inputNet1);
				input2.add(inputNet2);
				input3.add(inputNet3);
				design.addNet(newNet);
				design.addNet(inputNet1);
				design.addNet(inputNet2);
				design.addNet(inputNet3);
		}
		
		ArrayList<Instance> designInstances = new ArrayList<Instance>();
		ArrayList<Tile> tileList = new ArrayList<Tile>();
		ArrayList<Tile> intTileList = new ArrayList<Tile>();
	
		//num_inverters-1 as there already is an inverter instance in the design
		for (int i = 0; i < NUM_INVERTERS; i++)
		{
			Instance inverterInstance;
			if (i == 2) //ignoring the 2nd bit as we assume it's already in the design
			{
				placementTemp++;
				placedInverter.place(sites[placementTemp]);
				System.out.println("PlacementTemp: " + Integer.toString(placementTemp));
				placementTemp++;
				inverterInstance = placedInverter;
			}
			else
			{
				//Create the instance for the inverter
				ModuleInstance mi = design.createModuleInstance(userLogicPath + "ring_osc_" + Integer.toString(i), inverterMod);
				inverterInstance = mi.getInstances().get(0);
				while(!mi.place(sites[placementTemp++], design.getDevice()))
				{
					//System.out.println("Looking for sites...");
				}
				System.out.println("PlacementTemp: " + Integer.toString(placementTemp));
			}
			tileList.add(sites[placementTemp-1].getTile());
			intTileList.add(XDLDesignUtils.getMatchingIntTile(inverterInstance.getTile(),design.getDevice()));
			//System.out.println("Instance placed at: " + Integer.toString(placementTemp));
			designInstances.add(inverterInstance);
		}
		
		//Remove unused nets
		for (int i = 0; i < NUM_INVERTERS; i++)
		{
			design.removeNet(instancePath + ringOscName + Integer.toString(i) + "/$NET_0");
			design.removeNet(instancePath + ringOscName + Integer.toString(i) + "/$NET_1");
		}
		design.removeNet(instancePath + "ring_osc1");
		
		//Remove the unneeded net currently attached to the anchor slice input
		//first, we have to find it from the global logic nets, then remove it
		Net inputNet = new Net();
		for (Net n : design.getNets())
		{
			if (n.getName().contains("GLOBAL_LOGIC1"))
			{
				for (Pin p : n.getPins())
				{
					if (p.toString().contains(instancePath + ringOscName + "2/$COMP_0") &&
						p.toString().contains("G3"))
					{
						//System.out.println(p.toString());
						//delete the net and connect to the pin later
						inputNet = n;
					}
				}
			}	
		}
		design.removeNet(inputNet);
		
		//Rapidsmith currently doesn't have a good way to get the CLB_BUFFER tile needed for routing across the middle of the FPGA
		//There should be a better way to do this.
		Tile[][] tiles = design.getDevice().getTiles();
		Tile bufferTile = new Tile();
		
		for (Tile[] tArray : tiles)
		{
			for (Tile t : tArray)
			{
				if (t.toString().equals("CLB_BUFFER_X30Y" + Integer.toString(placementRow)))
				{
					bufferTile = t;
				}
			}
		}
		
		//Now that all of the instances have been placed, loop through the instances, add pins to the
		//corresponding net, then activate PIPs
		//special routes needed for 3, 7, 11, 25, 39, 43, 47 (hops) and 50 (long line)
		for (int i = 0; i < tileList.size(); i++)
		{
			Instance currentInstance = designInstances.get(i);
			Instance nextInstance = new Instance();
			Tile currentTile = tileList.get(i);
			Tile currentIntTile = intTileList.get(i);
			Tile nextTile = new Tile();
			Tile nextIntTile = new Tile();
			Net currentNet = nets.get(i);
			
			Pin output = new Pin(true, "Y", currentInstance);
			Pin input = null;
			if (i == tileList.size()-1)
			{
				//connect the last pin to the first
				currentNet.addPin(output);
				nextInstance = designInstances.get(0);
				nextTile = tileList.get(0);
				nextIntTile = intTileList.get(0);
				input = new Pin(false, "G3", nextInstance);
				currentNet.addPin(output);
				currentNet.addPin(input);
			}
			else
			{
				nextTile = tileList.get(i+1);
				nextIntTile = intTileList.get(i+1);
				nextInstance = designInstances.get(i+1);
				input = new Pin(false, "G3", nextInstance);
				currentNet.addPin(output);
				currentNet.addPin(input);
			}
			//We've got the instances and tiles we need
			//Activate PIPs
			ArrayList<PIP> pipsToAdd = new ArrayList<PIP>();
			
			//Special case to tie off unused LUT inputs
			//First add the pins to the corresponding nets
			Net currentInput1Net = input1.get(i);
			Net currentInput2Net = input2.get(i);
			Net currentInput3Net = input3.get(i);
				
			Pin G1 = new Pin(false, "G1", currentInstance);
			Pin G2 = new Pin(false, "G2", currentInstance);
			Pin G4 = new Pin(false, "G4", currentInstance);

			PrimitiveType primType = PrimitiveType.TIEOFF;
			Instance tieoff = new Instance("tieoff_" + i, primType);
			design.addInstance(tieoff);
			PrimitiveSite site = currentIntTile.getPrimitiveSites()[0];
			tieoff.place(site);
			
			Pin VCC = new Pin(true, "KEEP1", tieoff);
		
			currentInput1Net.addPin(G1);
			currentInput1Net.addPin(VCC);
			
			currentInput2Net.addPin(G2);
			currentInput2Net.addPin(VCC);
			
			currentInput3Net.addPin(G4);
			currentInput3Net.addPin(VCC);
			
			//Then, add the PIPs needed to connect the pins
			currentInput1Net.addPIP(new PIP(currentTile, We.getWireEnum("IMUX_B0_INT"), We.getWireEnum("G4_PINWIRE0")));
			currentInput1Net.addPIP(new PIP(currentIntTile, We.getWireEnum("KEEP1_WIRE"), We.getWireEnum("IMUX_B0")));
			
			currentInput1Net.addPIP(new PIP(currentTile, We.getWireEnum("IMUX_B2_INT"), We.getWireEnum("G2_PINWIRE0")));
			currentInput1Net.addPIP(new PIP(currentIntTile, We.getWireEnum("KEEP1_WIRE"), We.getWireEnum("IMUX_B2")));
			
			currentInput1Net.addPIP(new PIP(currentTile, We.getWireEnum("IMUX_B3_INT"), We.getWireEnum("G1_PINWIRE0")));
			currentInput1Net.addPIP(new PIP(currentIntTile, We.getWireEnum("KEEP1_WIRE"), We.getWireEnum("IMUX_B3")));
			
			if (i != NUM_INVERTERS-1)
			{
				pipsToAdd.add(new PIP(nextTile, We.getWireEnum("IMUX_B1_INT"), We.getWireEnum("G3_PINWIRE0")));
				pipsToAdd.add(new PIP(currentTile, We.getWireEnum("Y_PINWIRE0"), We.getWireEnum("BEST_LOGIC_OUTS4_INT")));
			}
			switch(i)
			{
				case 3:
				case 7:
				case 11:
				case 39:
				case 43:
				case 47:
					//For the hop case pips
					pipsToAdd.add(new PIP(currentIntTile, We.getWireEnum("BEST_LOGIC_OUTS4"), We.getWireEnum("E2BEG3")));
					pipsToAdd.add(new PIP(nextIntTile, We.getWireEnum("E2END3"), We.getWireEnum("IMUX_B1")));
					break;
				case (NUM_INVERTERS-1):
					break;
				case 25:
					//This is the median of the FPGA case. Use the CLB_BUFFER tile to activate the pip
					pipsToAdd.add(new PIP(currentIntTile, We.getWireEnum("BEST_LOGIC_OUTS4"), We.getWireEnum("E2BEG3")));
					pipsToAdd.add(new PIP(nextIntTile, We.getWireEnum("E2END3"), We.getWireEnum("IMUX_B1")));
					pipsToAdd.add(new PIP(bufferTile, We.getWireEnum("CLB_BUFFER_IE2MID3"), We.getWireEnum("CLB_BUFFER_E2MID3")));
					break;
				default:
					pipsToAdd.add(new PIP(nextIntTile, We.getWireEnum("OMUX_E7"), We.getWireEnum("IMUX_B1")));
					pipsToAdd.add(new PIP(currentIntTile, We.getWireEnum("BEST_LOGIC_OUTS4"), We.getWireEnum("OMUX7")));
			}
			for (PIP p : pipsToAdd)
			{
				//System.out.println(p.toString(We));
				currentNet.addPIP(p);
			}
		}
		System.out.println("Writing design...");
		design.saveXDLFile(modifiedDesign, true);
		FileConverter.convertXDL2NCD(modifiedDesign);
		System.out.println("done.");
	}	
	
}

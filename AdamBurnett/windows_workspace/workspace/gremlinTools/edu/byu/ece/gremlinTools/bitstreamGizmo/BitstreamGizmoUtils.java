package edu.byu.ece.gremlinTools.bitstreamGizmo;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import edu.byu.ece.gremlinTools.bitstream.GremlinBitstream;
import edu.byu.ece.rapidSmith.bitstreamTools.configurationSpecification.DeviceLookup;
import edu.byu.ece.rapidSmith.device.Device;
import edu.byu.ece.rapidSmith.device.PrimitiveSite;
//import edu.byu.ece.rapidSmith.device.SourceNode;
import edu.byu.ece.rapidSmith.device.Tile;
import edu.byu.ece.rapidSmith.device.WireConnection;
import edu.byu.ece.rapidSmith.device.WireEnumerator;
import edu.byu.ece.rapidSmith.device.WireType;
import edu.byu.ece.rapidSmith.primitiveDefs.PrimitiveDefList;
import edu.byu.ece.rapidSmith.util.FileTools;
import edu.byu.ece.rapidSmith.design.Design;
import edu.byu.ece.rapidSmith.design.Instance;
import edu.byu.ece.rapidSmith.design.Net;
import edu.byu.ece.rapidSmith.design.PIP;


public class BitstreamGizmoUtils {

	private Device device;
	private PrimitiveDefList primitives;
	private WireEnumerator wireEnum;
	private String partName;
	private String designName;
	private Design design;
	private HashSet<PIP> pipSet;
	private HashSet<PIP> unusedPIPs;
	HashMap<PIP,Net> netMap;
	private int netCount;
	private int debugCount;
	private HashSet<String> switchBoxSources;
	private HashSet<String> switchBoxSinks;
	private HashMap<String,Instance> instanceMap;
	
	public BitstreamGizmoUtils(){
		pipSet = new HashSet<PIP>();
		unusedPIPs = new HashSet<PIP>();
		wireEnum = new WireEnumerator();
		netCount = 0;
		debugCount = 0;
		netMap = new HashMap<PIP, Net>();
		switchBoxSources = new HashSet<String>();
		switchBoxSinks = new HashSet<String>();
		instanceMap = null;
		
		for(int i=0; i < 8; i++){
			switchBoxSources.add("HALF_OMUX_BOT" + i);
			switchBoxSources.add("HALF_OMUX_TOP" + i);
			switchBoxSources.add("SECONDARY_LOGIC_OUTS" + i);
			switchBoxSources.add("BEST_LOGIC_OUTS" + i);
			switchBoxSinks.add("BYP_INT_B" + i);
		}
		
		switchBoxSources.add("VCC_WIRE");
		switchBoxSources.add("KEEP1_WIRE");
		switchBoxSources.add("GND_WIRE");
		
		for(int i=0; i < 4; i++){
			switchBoxSinks.add("CLK_B" + i);
			switchBoxSinks.add("SR_B" + i);
			switchBoxSinks.add("CE_B" + i);
		}
		for(int i=0; i <32; i++){
			switchBoxSinks.add("IMUX_B" + i);
		}
		
	}
	
	public Design createNetsFromPIPs(ArrayList<PIP> pips, 
			GremlinBitstream bitstream, String speedGrade) {	
		pipSet.addAll(pips);
		unusedPIPs.addAll(pips);
		partName = bitstream.getHeader().getPartName();
		designName = bitstream.getHeader().getSourceNCDFileName();
		// Figure out part and design names
		partName = DeviceLookup.getRootDeviceName(partName) + DeviceLookup.getPackageName(partName);
		partName = partName.toLowerCase();
		designName = designName.substring(0, designName.length()-4);
		speedGrade = getSpeedGrade(speedGrade,bitstream);

		// Load part database
		device = (Device) FileTools.loadFromFile(partName + System.getProperty("file.separator") + partName + "_db.dat");
		primitives = (PrimitiveDefList) FileTools.loadFromFile(partName + System.getProperty("file.separator") + partName + "_primitiveDefs.dat");
		//wireEnum.loadEnumerationsFromFile(partName + System.getProperty("file.separator") + partName + "_wireEnum.dat");
		
		// Create XDL design object
		design = new Design();
		design.setName(designName);
		design.setPartName(partName+speedGrade);
		design.setNCDVersion("v3.2");
		
		// Pick a PIP and try to find everything its connected to
		/*while(pips.size() > 0){
			extractNet(pips);
		}*/

		for(PIP p1 : pips){
			Iterator<PIP> itr2 = pipSet.iterator();
			PIP p2;
			while(itr2.hasNext()){
				p2 = itr2.next();			
				if(p1 != p2 && pipsConnected(p1,p2)){
					//System.out.print("PIP Match!\n");
					//System.out.print("  p1=" + p1);
					//System.out.print("  p2=" + p2);
					if(netMap.get(p1)==null && netMap.get(p2)==null){
						// Create a new net for both pips
						Net net = createNewNet(p1);
						net.getPIPs().add(p2);
						netMap.put(p2, net);
						unusedPIPs.remove(p1);
						unusedPIPs.remove(p2);
					}
					else if(netMap.get(p1)==null || netMap.get(p2)==null){
						// Add the loose pip to the other pip's net
						if(netMap.get(p1)!=null){
							netMap.get(p1).getPIPs().add(p2);
							netMap.put(p2, netMap.get(p1));
							unusedPIPs.remove(p2);

						}
						else{
							netMap.get(p2).getPIPs().add(p1);
							netMap.put(p1, netMap.get(p2));
							unusedPIPs.remove(p1);
						}
					}
					else if(netMap.get(p1)!=null && netMap.get(p2)!=null){
						// Merge two nets
						Net mergedNet = netMap.get(p1);
						Net oldNet = netMap.get(p2);
						if(!mergedNet.equals(oldNet)){					
							
							mergedNet.getPIPs().addAll(oldNet.getPIPs());
											
							for(PIP p : oldNet.getPIPs()){
								netMap.remove(p);
								netMap.put(p, mergedNet);
							}
							netMap.put(p2, mergedNet);
							oldNet.setPIPs(null);
							oldNet = null;
						}
					}
				}
			}
		}
		// When we are all done, some PIPs may be left over, VCC_WIREs...
		Iterator<PIP> unusedItr = unusedPIPs.iterator();
		while(unusedItr.hasNext()){
			PIP p = unusedItr.next();
			if(design.getWireEnumerator().getWireName(p.getStartWire()).equals("VCC_WIRE")){
				createNewNet(p);
			}
		}
		
		// Organize all the nets, formalize names
		HashSet<Net> uniqueNets = new HashSet<Net>();
		uniqueNets.addAll(netMap.values());
		Iterator<Net> itr = uniqueNets.iterator();
		while(itr.hasNext()){
			Net net = itr.next();
			net.setName("RawNet_" + netCount);
			netCount++;
			design.getNets().add(net);
		}
		
		/*
		int j=0;
		for(Integer i : device.getWirePool().keySet()){
			System.out.println(j);
			ArrayList<Wire> tmp = device.getWirePool().get(i);
			for(Wire w : tmp){
				if(w.getRouteThrough() != null){
					System.out.println("\tw=" + w.toString(wireEnum) + " _ROUTETHROUGH: " + w.getRouteThrough().toString(wireEnum));
				}
				else{
					System.out.println("\tw=" + w.toString(wireEnum));
				}
			}
			j++;
		}
		System.exit(1);*/
		
		//addPinsToNets();
		
		//Must We Really Sort??
		//design.getInstances().sort();
		
		return design;
	}
	
	/*private void addPinsToNets() {
		instanceMap = design.getInstanceMap();
		for(Net net : design.getNets()){
			boolean outpinFound = false;
			//Find the source
			for(PIP pip : net.getPIPs()){
				WireEnumerator we = design.getWireEnumerator(); 
				if(!outpinFound && switchBoxSources.contains(pip.getStartWireName(we))){
					Tile t = device.getTile(pip.getTile().toString());	        
					// This is the source
					if(pip.getStartWireName(we).equals("VCC_WIRE") || pip.getStartWireName(we).equals("GND_WIRE") || 
							pip.getStartWireName(we).equals("KEEP1_WIRE")){
						// Its a true switch box source, we will treat it differently
						
						PrimitiveSite match = null;


						match = findMatchingPrimitive(t, pip.getStartWire());

						if(match == null){
							System.out.println("We have a problem with not finding a TIEOFF " +
									"matching primitive in the tile: " + 
									pip.getTile().toString() + " wire=" + ""); // device.getPinsMap().get(wireEnum.getWireEnum(pip.getWire0())));
							System.exit(1);   
						}

						Instance inst = getInstance(match, net);
						                             
						Pin newSource = new Pin(true,device.getPinsMap().get(wireEnum.getWireEnum(pip.getWire0())),inst); 
						net.addPin(newSource);
						
						outpinFound = true;
					}
					else{ // This is a source from an instance not in a switch box
						boolean debugCase = false;
						if(pip.getStartWireName(we).equals("BEST_LOGIC_OUTS0") && pip.getEndWireName(we).equals("OMUX6")){
							debugCase = true;
						}
						t = device.getTile(t.getRow(), t.getColumn()+1);
						HashMap<Integer, SourceNode> wireMap = device.getSwitchBoxSources().get(t);
						PrimitiveSite match = null;
						Integer internalPin = -1;
						Integer currWire = pip.getStartWire();
						SourceNode sourceMatch = null;
						if(t.getSources() != null){
							for(Integer i : t.getSources()){
								if(wireMap.get(i) != null && wireMap.get(i).instancePin != null){
									if(debugCase) System.out.println("  source=" + wireEnum.getWireName(i) + " : " + wireEnum.getWireName(wireMap.get(i).instancePin) + " tile="+ t.getName() );
									if(debugCase && i.intValue() == wireEnum.getWireEnum("IOIS_I0")){
										System.out.println("wireMap.get(i)=" + wireMap.get(i).toString(wireEnum));
									}
									if(wireMap.get(i).wires != null){
										for(Wire w : wireMap.get(i).wires){
											if(debugCase) System.out.println("    " + wireEnum.getWireName(w.getWire()) + " currWire=" + wireEnum.getWireName(currWire));
											if(w.getRouteThrough() != null){
												
												break;
											}
											if(currWire.equals(w.getWire())){
												sourceMatch = wireMap.get(i);
											}
										}
									}
								}
							}							
						}
						if(sourceMatch != null){
							internalPin = sourceMatch.instancePin; 
							//match = findMatchingPrimitive(t, device.getPinsMap().get(internalPin));
							match = findMatchingPrimitive(t, internalPin);
							if(debugCase) System.out.println("sourceMatch=" + sourceMatch.toString(wireEnum));
							if(match == null){
								
								System.out.println("PROBLEM, couldn't find a matching primitive for: ");
								System.out.print(pip.toString());
								System.out.println(" in Tile: " + t.getName());
								for(Integer i : wireMap.keySet()){
									if(wireMap.get(i) != null){
										System.out.println("  " + wireEnum.getWireName(i) + " " + wireMap.get(i));
										if(wireMap.get(i).wires != null){
											for(Wire w : wireMap.get(i).wires){
												System.out.println("   " + w.toString(wireEnum));
											}										
										}
									}
									else{
										System.out.println("  " + wireEnum.getWireName(i));
									}
								}
								System.exit(1);
								
							}
							
							Instance inst = getInstance(match, net); 

							Pin newSource = new Pin(true,wireEnum.getWireName(internalPin),inst);
							net.addPin(newSource);
													
							outpinFound = true;
						}
					}
				}
				
//				else if(switchBoxSinks.contains(pip.getWire1())){
//					Tile t = device.getTile(pip.getTile().toString());
//					Tile tNextDoor = device.getTile(t.getRow(), t.getColumn()+1); 
//					HashMap<Integer, SinkNode> wireMap = device.getSwitchBoxSinks().get(t);
//					Primitive match = null;
//					Integer internalPin = -1;
//					Integer currWire = wireEnum.getWireEnum(pip.getWire1());
//					SinkNode sinkMatch = null;
//					
//					// Find the sinkMatch 
//					if(tNextDoor.getSinks() != null){
//						for(Integer i : tNextDoor.getSinks()){
//							if(wireMap.get(i) != null && wireMap.get(i).pips != null){
//								int testWire = wireMap.get(i).pips[wireMap.get(i).pips.length-1].getEndWire(); 
//								if(wireMap.get(i).switchBoxSink.intValue()==currWire){
//									sinkMatch = wireMap.get(i);
//								}
//							}
//						}							
//					}
//					
//					if(sinkMatch != null){
//						internalPin = sinkMatch.pips[sinkMatch.pips.length-1].getEndWire(); 
//						match = findMatchingPrimitive(tNextDoor, device.getPinsMap().get(internalPin));
//						
//						if(match == null){
//							System.out.println("PROBLEM, couldn't find a matching primitive for: ");
//							System.out.print(pip.toString());
//							System.out.println(" in Tile: " + tNextDoor.getName());
//							for(Integer i : wireMap.keySet()){
//								if(wireMap.get(i) != null){
//									System.out.println("  " + wireEnum.getWireName(i) + " " + wireMap.get(i));
//									if(wireMap.get(i).pips != null){
//										for(PIP p : wireMap.get(i).pips){
//											System.out.println("   " + p.toString(wireEnum));
//										}										
//									}
//								}
//								else{
//									System.out.println("  " + wireEnum.getWireName(i));
//								}
//							}
//							System.exit(1);
//						}
//						
//						XDL_Instance inst = getInstance(match, net, tNextDoor); 
//						
//						XDL_Pin newSink = new XDL_Pin(false,device.getPinsMap().get(internalPin),inst);
//						net.getPins().add(newSink);
//						net.getPins().setSource(newSink);
//					}
//				}
			}
		}
		for(Instance inst : instanceMap.values()){
			design.getInstances().add(inst);
		}
	}

	private PrimitiveSite findMatchingPrimitive(Tile tile, Integer wire){
		PrimitiveSite match = null;
		if(tile.getName().equals("IOIS_LC_X15Y11")){
			System.out.println("tile="+ tile.getName() + " wire=" + wire );
			for(PrimitiveSite p : device.getPrimitiveMap().get(tile)){
				System.out.println("  p=" + p.getName() + " " + p.getType());
				for(String s : p.getPins().keySet()){
					System.out.println("    pin = " + s + " " + wireEnum.getWireName(p.getPins().get(s)));
				}
			}
		}  
		for(PrimitiveSite p : device.getPrimitiveMap().get(tile)){
			if(p.getPins().containsValue(wire)){
				match = p;
				break;
			}
		}
		return match;
	}
	
	/**
	 * This will either create a new instance or return the existing instance based on the 
	 * primitive passed to it.
	 * @param primitive The primitive that we want to convert to an instance
	 * @param net The net that connects to the instance
	 * @param tile The tile where the instance resides
	 * @return The new or existing instance at location primitive.getName()
	 */
	private Instance getInstance(PrimitiveSite primitive, Net net){
		Instance inst = null;
		if(instanceMap.containsKey(primitive.getName())){
			inst = instanceMap.get(primitive.getName()); 
		}
		else{
			// TODO Re-implement this
			//inst = primitives.getNewXDLInstance(primitive.getType());
			inst = new Instance();
			instanceMap.put(primitive.getName(), inst);
		}
		inst.getNetList().add(net);
		
		inst.place(primitive) ;
		return inst;
	}
	
	/**
	 * This method will check if pip1 and pip2 are connected
	 * @param pip1 First PIP to match 
	 * @param pip2 Second PIP to match
	 * @return True if pip1 and pip2 are connected, false otherwise.
	 */
	private boolean pipsConnected(PIP pip1, PIP pip2){
		WireConnection[] connections = pip1.getTile().getWireConnections(pip1.getStartWire());
		int wire0 = pip1.getStartWire();
		int wire1 = pip1.getStartWire();
		int wire2 = pip2.getStartWire();
		if(pip1.getTile().toString().equals(pip2.getTile().toString())){
			if(wire2 == wire1 || wire0 == wire2){
				return true;
			}
		}
		
		// Check if there is a direct PIP connection
		for(WireConnection w : connections){
			if(!w.isPIP()){
				if(wire2 == w.getWire()){
					Tile tile1 = w.getTile(device.getTile(pip1.getTile().toString())); 
					if(tile1 != null && tile1.getName().equals(pip2.getTile().toString())){
						return true;
					}
				}				
			}
		}
		
		connections = pip1.getTile().getWireConnections(pip1.getStartWire());
		// Check if there is a direct PIP connection
		for(WireConnection w : connections){
			if(!w.isPIP()){
				if(wire2 == w.getWire()){
					Tile tile1 = w.getTile(device.getTile(pip1.getTile().toString())); 
					if(tile1 != null && tile1.getName().equals(pip2.getTile().toString())){
						return true;
					}
				}				
			}
		}
		

		// Check if it is a DOUBLE/HEX line
		if(wireEnum.getWireType(wire1).equals(WireType.DOUBLE) && wireEnum.getWireDirection(wire1).equals(wireEnum.getWireDirection(wire2))){
			if(pip1.getStartWireName(wireEnum).contains("BEG") && (pip1.getStartWireName(wireEnum).charAt(pip1.getStartWireName(wireEnum).length()-1) == pip2.getStartWireName(wireEnum).charAt(pip1.getStartWireName(wireEnum).length()-1))){
				boolean xCoordinateConstant = 0 == pip2.getTile().getTileXCoordinate() - pip1.getTile().getTileXCoordinate();
				boolean yCoordinateConstant = 0 == pip2.getTile().getTileYCoordinate() - pip1.getTile().getTileYCoordinate();
				if(pip2.getStartWireName(wireEnum).contains("MID")){
					switch(wireEnum.getWireDirection(wire1)){
						case NORTH:
							return (1 == pip2.getTile().getTileYCoordinate() - pip1.getTile().getTileYCoordinate()) && xCoordinateConstant;
						case SOUTH:
							return (-1 == pip2.getTile().getTileYCoordinate() - pip1.getTile().getTileYCoordinate()) && xCoordinateConstant;
						case EAST:
							return (1 == pip2.getTile().getTileXCoordinate() - pip1.getTile().getTileXCoordinate()) && yCoordinateConstant;
						case WEST:
							return (-1 == pip2.getTile().getTileXCoordinate() - pip1.getTile().getTileXCoordinate()) && yCoordinateConstant;
					}
				}
				else if(pip2.getStartWireName(wireEnum).contains("END")){
					switch(wireEnum.getWireDirection(wire1)){
					case NORTH:
						return (2 == pip2.getTile().getTileYCoordinate() - pip1.getTile().getTileYCoordinate()) && xCoordinateConstant;
					case SOUTH:
						return (-2 == pip2.getTile().getTileYCoordinate() - pip1.getTile().getTileYCoordinate()) && xCoordinateConstant;
					case EAST:
						return (2 == pip2.getTile().getTileXCoordinate() - pip1.getTile().getTileXCoordinate()) && yCoordinateConstant;
					case WEST:
						return (-2 == pip2.getTile().getTileXCoordinate() - pip1.getTile().getTileXCoordinate()) && yCoordinateConstant;
					}
				}
			}
		}
		if(wireEnum.getWireType(wire1).equals(WireType.HEX) && wireEnum.getWireDirection(wire1).equals(wireEnum.getWireDirection(wire2))){
			if(pip1.getEndWireName(wireEnum).contains("BEG") && (pip1.getEndWireName(wireEnum).charAt(pip1.getEndWireName(wireEnum).length()-1) == pip2.getStartWireName(wireEnum).charAt(pip1.getEndWireName(wireEnum).length()-1))){
				boolean xCoordinateConstant = 0 == pip2.getTile().getTileXCoordinate() - pip1.getTile().getTileXCoordinate();
				boolean yCoordinateConstant = 0 == pip2.getTile().getTileYCoordinate() - pip1.getTile().getTileYCoordinate();
				if(pip2.getStartWireName(wireEnum).contains("MID")){
					switch(wireEnum.getWireDirection(wire1)){
					case NORTH:
						return (3 == pip2.getTile().getTileYCoordinate() - pip1.getTile().getTileYCoordinate()) && xCoordinateConstant;
					case SOUTH:
						return (-3 == pip2.getTile().getTileYCoordinate() - pip1.getTile().getTileYCoordinate()) && xCoordinateConstant;
					case EAST:
						return (3 == pip2.getTile().getTileXCoordinate()- pip1.getTile().getTileXCoordinate()) && yCoordinateConstant;
					case WEST:
						return (-3 == pip2.getTile().getTileXCoordinate() - pip1.getTile().getTileXCoordinate()) && yCoordinateConstant;
					}
				}
				else if(pip2.getStartWireName(wireEnum).contains("END")){
					switch(wireEnum.getWireDirection(wire1)){
					case NORTH:
						return (6 == pip2.getTile().getTileYCoordinate() - pip1.getTile().getTileYCoordinate()) && xCoordinateConstant;
					case SOUTH:
						return (-6 == pip2.getTile().getTileYCoordinate() - pip1.getTile().getTileYCoordinate()) && xCoordinateConstant;
					case EAST:
						return (6 == pip2.getTile().getTileXCoordinate() - pip1.getTile().getTileXCoordinate()) && yCoordinateConstant;
					case WEST:
						return (-6 == pip2.getTile().getTileXCoordinate() - pip1.getTile().getTileXCoordinate()) && yCoordinateConstant;
					}
				}
			}			
		}
		if(pip1.getEndWireName(wireEnum).contains("BYP_INT_B") && pip2.getStartWireName(wireEnum).contains("BYP_BOUNCE") && pip1.getTile().toString().equals(pip2.getTile().toString())){
			if(pip1.getEndWireName(wireEnum).charAt(pip1.getEndWireName(wireEnum).length()-1) == pip2.getStartWireName(wireEnum).charAt(pip2.getStartWireName(wireEnum).length()-1)){
				// Could add some code here to add in the fake pip that connects them
				return true;
			}
		}
		
		return false;
	}
	
	private Net createNewNet(PIP p){
		Net net = new Net();
		net.getPIPs().add(p);
		netMap.put(p, net);
		net.setName("debug" + debugCount);
		debugCount++;
		return net;
	}
	
	private static String getSpeedGrade(String speedGrade, GremlinBitstream bitstream){
		String[] validSpeedGrades = DeviceLookup.lookupPartFromBitstream(bitstream).getValidSpeedGrades();
		if(speedGrade == null){
			return validSpeedGrades[0];
		}
		if(!(speedGrade.charAt(0)=='-')){
			speedGrade = "-" + speedGrade;
		}

		for(String test : validSpeedGrades){
			if(test.equals(speedGrade)){
				return speedGrade;
			}
		}
		BitstreamGizmo.failAndExit("bad speed grade: " + speedGrade);
		return null;
	}	
}

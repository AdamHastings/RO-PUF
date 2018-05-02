package edu.byu.ece.gremlinTools.WireMux;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;

import com.csvreader.CsvReader;

import XDLDesign.Utils.XDLDesignUtils;
import edu.byu.ece.gremlinTools.Virtex4Bits.Bit;
import edu.byu.ece.gremlinTools.Virtex4Bits.JNode;
import edu.byu.ece.gremlinTools.Virtex4Bits.Pair;
import edu.byu.ece.gremlinTools.Virtex4Bits.Pip;
import edu.byu.ece.gremlinTools.Virtex4Bits.V4ConfigurationBit;
import edu.byu.ece.gremlinTools.Virtex4Bits.Virtex4CLB;
import edu.byu.ece.gremlinTools.Virtex4Bits.Virtex4Slice;
import edu.byu.ece.gremlinTools.Virtex4Bits.Virtex4SwitchBox;
import edu.byu.ece.gremlinTools.Virtex4Bits.Virtex4SwitchBoxRouter;
import edu.byu.ece.gremlinTools.xilinxToolbox.V4XilinxToolbox;
import edu.byu.ece.rapidSmith.bitstreamTools.configuration.FPGA;
import edu.byu.ece.rapidSmith.design.Design;
import edu.byu.ece.rapidSmith.design.Net;
import edu.byu.ece.rapidSmith.design.NetType;
import edu.byu.ece.rapidSmith.design.PIP;
import edu.byu.ece.rapidSmith.design.Pin;
import edu.byu.ece.rapidSmith.device.Device;
import edu.byu.ece.rapidSmith.device.Tile;
import edu.byu.ece.rapidSmith.device.TileType;
import edu.byu.ece.rapidSmith.device.WireConnection;
import edu.byu.ece.rapidSmith.device.WireEnumerator;
import edu.byu.ece.rapidSmith.util.FileConverter;
import edu.byu.ece.rapidSmith.util.FileTools;

public class ReverseVerifyWireMux {
    	    
    public static ArrayList<String> readFile(String Filename){
    	ArrayList<String> Muxes = new ArrayList<String>(); 
    	File input = new File(Filename);
    	
    	try {
			BufferedReader reader = new BufferedReader(new FileReader(input));
		    String line; 
			while((line=reader.readLine())!=null){
				Muxes.add(line);
			}
    	} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
    	
    	return Muxes; 
    }
    
    public static void writeStringArrayToFile(ArrayList<String> Strings, String Filename){
    	File output = new File(Filename);
    	
    	try {
			BufferedWriter out = new BufferedWriter(new FileWriter(output));
			for(String S : Strings){
				out.write(S);
				out.newLine();
			}
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public static ArrayList<WireMux> instantiateMuxes(ArrayList<String> MuxNames){
    	
    	ArrayList<WireMux> Muxes = new ArrayList<WireMux>();
    	
    	for(String S: MuxNames){
    	    WireMux M = new WireMux(S);
    	    M.PopulateTable(false);
    		Muxes.add(M);
    	}
    	
    	return Muxes;
    }
    
    private static void CreateVisualVerificationFile(ArrayList<WireMux> Muxes, String Filename) {
		
    	Design design = new Design(); 
	  	design.setNCDVersion("v3.2");
		design.setPartName("xc4vlx60ff668-12");
		
		Device device = design.getDevice(); 
		FPGA fpga = XDLDesignUtils.getVirtexFPGA("XC4VLX60");
		V4XilinxToolbox TB = XDLDesignUtils.getV4Toolbox(fpga);
        Virtex4SwitchBox SB = new Virtex4SwitchBox(fpga, design, TB);
        Virtex4SwitchBoxRouter SBR = new Virtex4SwitchBoxRouter(SB);
        
        ArrayList<Virtex4CLB> TestSites = new ArrayList<Virtex4CLB>();
        ArrayList<Net> DummyNets = getNetsToBlockCLBInputs(design);
        int j=0;
        for(Tile T: XDLDesignUtils.getAllCLBTiles(device)){
			TestSites.add(new Virtex4CLB(fpga, design, T, TB));
			j++;
			if(j > 3000){
				break;
			}
		}
        SetupCLBs(TestSites, design, fpga, TB);
        
        ArrayList<Tile> INTTiles = XDLDesignUtils.getAllINTTiles(device);
		
        int i=0;
        int ShortsCreated = 0;
        int even = 0;
        int odd  = 0;
        for(WireMux M: Muxes){
        	Virtex4CLB CLB = TestSites.get(i++);
        	setBlockedInputsTile(DummyNets, CLB.getCLBTile());
        	SBR.setTiles(CLB.getINTTile(), CLB.getCLBTile());
           
        	Net N = CreateReverseEngineeringShorts(M, SBR, design);
        	if(N == null){
        		M.TransposeTable();
        		N = CreateReverseEngineeringShorts(M, SBR, design);
        	}
        	if(N != null){
        		Virtex4Slice S = getMatchingSlice(N, CLB, design.getWireEnumerator());
        		String PinName = getMatchingsPinName(N, design.getWireEnumerator());
        		N.addPin(new Pin(true, PinName,  S.getXDLInstance()));
        		design.addNet(N);
        		ShortsCreated++;
        		if(M.getTable().getRows() == M.getTable().getCols()){
             	   even++;
                } else {
             	   odd++;
                }
        	}
        	/*Pair<Net> Nets = CreateReverseEngineeringShorts(M, SBR, design);	
            if(Nets == null){
            	M.TransposeTable(); //Try Transposed Version
                Nets = CreateReverseEngineeringShorts(M, SBR, design);
            }
        	if(Nets != null){
        	   Virtex4Slice SliceLeft = getMatchingSlice(Nets.getLeft(), CLB, design.getWireEnumerator());
        	   Virtex4Slice SliceRight = getMatchingSlice(Nets.getRight(), CLB,design.getWireEnumerator());
        	   String PinNameLeft = getMatchingsPinName(Nets.getLeft(), design.getWireEnumerator());
        	   String PinNameRight = getMatchingsPinName(Nets.getRight(), design.getWireEnumerator());
               Nets.getLeft().addPin(new Pin(true, PinNameLeft,  SliceLeft.getXDLInstance()));
     		   Nets.getRight().addPin(new Pin(true,PinNameRight, SliceRight.getXDLInstance()));
        	   design.addNet(Nets.getLeft());
               design.addNet(Nets.getRight());
               ShortsCreated++;
               if(M.getTable().getRows() == M.getTable().getCols()){
            	   even++;
               } else {
            	   odd++;
               }
            }
            */
        }
        System.out.println("Created " + ShortsCreated + " successfully.");
	    System.out.println("Created " + even + " even shorts.");
	    System.out.println("Created " + odd + " odd shorts.");
        System.out.println("Saving XDL File");
	    design.saveXDLFile(Filename);
	    System.out.println("Converting to NCD File");
	    FileConverter.convertXDL2NCD(Filename);
	}
    
    private static String getMatchingsPinName(Net N, WireEnumerator We) {
		
    	for(PIP P : N.getPIPs()){
			if(We.getWireName(P.getStartWire()).contains("PINWIRE")){
				if(We.getWireName(P.getStartWire()).startsWith("X")){
					return "X";
				}else
					return "Y";
			}
		}
    	return null;
	}

	private static Virtex4Slice getMatchingSlice(Net N, Virtex4CLB CLB,WireEnumerator We) {
		  
		for(PIP P : N.getPIPs()){
			if(We.getWireName(P.getStartWire()).contains("PINWIRE")){
			   String PinWireName = We.getWireName(P.getStartWire());
			   int slice = Integer.parseInt(PinWireName.substring(PinWireName.length()-1, PinWireName.length()));
			   switch(slice){
				   case 0: return CLB.getSLICEX0Y0(); 
				   case 1: return CLB.getSLICEX1Y0(); 
				   case 2: return CLB.getSLICEX0Y1(); 
				   case 3: return CLB.getSLICEX1Y1(); 
				   default: System.out.println("Error Didn't find correct slice.");
				            return null; 
			   }
			}
		}
		
		return null;
	}

	public static Virtex4CLB findCLB(Tile T, ArrayList<Virtex4CLB> TestSites){
    	for(Virtex4CLB CLB: TestSites){
    		if(T.equals(CLB.getCLBTile())){
    			return CLB; 
    		}
    	}
    	return null;
    }
	
	public static Net FindNet(WireMux WM, int row, Virtex4SwitchBoxRouter SBR, Design design){
		
		ArrayList<String> StartingPinwires = new ArrayList<String>();
		
		StartingPinwires.add("X_PINWIRE0");
		StartingPinwires.add("X_PINWIRE1");
		StartingPinwires.add("X_PINWIRE2");
		StartingPinwires.add("X_PINWIRE3");
		StartingPinwires.add("Y_PINWIRE0");
		StartingPinwires.add("Y_PINWIRE1");
		StartingPinwires.add("Y_PINWIRE2");
		StartingPinwires.add("Y_PINWIRE3");
	    
		for(String S: StartingPinwires){
    	    for(int col = 0; col < WM.getTable().getCols(); col++){
    	       Pip P = WM.getTable().getPip(row, col);
    	       if(P!= null){
		    	   Net N = new Net("LOGIC0" + WM.getDestName() + SBR.SB.getCLBTile().toString(), NetType.WIRE);
		    	   ArrayList<PIP> pips = SBR.CreateBridgingShort(design.getWireEnumerator().getWireEnum(S), P.getPip().getStartWire());
		    	   if(pips.size() > 0){
		    		   N.getPIPs().addAll(pips);
		    		   N.addPIP(P.getPip());
		    		   return N; 
		    	   }
    	       }
    	    }
	    }
		return null;
	}
    public static Pair<Net, Net> FindPair(WireMux WM, int row, Virtex4SwitchBoxRouter SBR, Design design){
		ArrayList<String> StartingPinwires = new ArrayList<String>();
		
		StartingPinwires.add("X_PINWIRE0");
		StartingPinwires.add("X_PINWIRE1");
		StartingPinwires.add("X_PINWIRE2");
		StartingPinwires.add("X_PINWIRE3");
		StartingPinwires.add("Y_PINWIRE0");
		StartingPinwires.add("Y_PINWIRE1");
		StartingPinwires.add("Y_PINWIRE2");
		StartingPinwires.add("Y_PINWIRE3");
	    		
		for(int col1=0; col1<WM.getTable().getCols()-1; col1++){
			
	    	for(int col2=1; col1+col2<WM.getTable().getCols(); col2++ ){
				Pip P1 = WM.getTable().getPip(row, col1);
				Pip P2 = WM.getTable().getPip(row, col1+col2);
				if(P1 == null || P2 == null)
					continue; 
				P1.getPip().setTile(SBR.SB.getIntTile());
				P2.getPip().setTile(SBR.SB.getIntTile());
				
				Net ToP1 = new Net("LOGIC0" + WM.getDestName() + SBR.SB.getCLBTile().toString(), NetType.WIRE);
				Net ToP2 = new Net("LOGIC1" + WM.getDestName() + SBR.SB.getCLBTile().toString(), NetType.WIRE); 
				
				ArrayList<PIP> ToP1Pips = null;
				ArrayList<PIP> ToP2Pips = null;
				boolean foundPair = false;
				
				for(int i=0; i<StartingPinwires.size()-1; i++){
					ToP1Pips = SBR.CreateBridgingShort(design.getWireEnumerator().getWireEnum(StartingPinwires.get(i)), P1.getPip().getStartWire());
					if(ToP1Pips.size()>0){				
					  for(int j=i+1; j<StartingPinwires.size(); j++){
						ToP2Pips = SBR.CreateBridgingShort(design.getWireEnumerator().getWireEnum(StartingPinwires.get(j)), P2.getPip().getStartWire());
					    if(ToP2Pips.size()>0 && VerifyPIPs(ToP1Pips, ToP2Pips)){
					       foundPair = true; 
					       break; 
					    } else {
					    	//Just Incase the last set of Pips do not verify
					    	ToP1Pips.clear();
					    	ToP2Pips.clear();
					    }
					  }
					}
					if(foundPair==true)
						break;
				}
				
				if(ToP1Pips != null){
					if(ToP1Pips.size()>0 && ToP2Pips.size()>0){				        
					  	ToP1.getPIPs().addAll(ToP1Pips);
					  	ToP1.addPIP(P1.getPip());
					   	ToP2.getPIPs().addAll(ToP2Pips);
					   	ToP2.addPIP(P2.getPip());
					   	return new Pair<Net, Net>(ToP1, ToP2);
					}
				}
			}
		}
	    
	    return null; 
}
    
private static boolean VerifyPIPs(ArrayList<PIP> toP1Pips, ArrayList<PIP> toP2Pips) {
	   for(PIP P1 : toP1Pips){
		   for(PIP P2 : toP2Pips){
			   if(P1.getStartWire() == P2.getStartWire() || P1.getStartWire() == P2.getEndWire() ||
				  P1.getEndWire() == P2.getStartWire() || P1.getEndWire() == P2.getEndWire())
				   return false; 
		   }
	   }
	   return true;
	}

private static void SetupCLBs(ArrayList<Virtex4CLB> TestSites, Design design, FPGA fpga, V4XilinxToolbox TB) {
		
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
			
			//GroundLutInputs(SliceX0Y0, GroundNet, "F");
			//GroundLutInputs(SliceX0Y1, GroundNet, "G");
			//GroundLutInputs(SliceX1Y0, GroundNet, "F");
			//GroundLutInputs(SliceX1Y1, GroundNet, "G");
		}
	}
    
    public static Net CreateReverseEngineeringShorts(WireMux WM, Virtex4SwitchBoxRouter SBR, Design design){
		   
		   SBR.regenerateUsedRoutingResources();
		   for(int i=0; i<WM.getTable().getRows(); i++){
			  //Pair Nets = FindPair(WM, i, SBR, design);
			   Net N = FindNet(WM,i,SBR,design);
           if(N != null)
         	  return N; 
		  }
		  
		 return null;
	}
    
    public static ArrayList<Net> getNetsToBlockCLBInputs(Design design){
		ArrayList<Net> DummyNets = new ArrayList<Net>();
		
		//Read in CSV file 
		File PinFile = new File("Z:\\FPGA\\Gremlin\\PUF_TestBoardExperiments\\LeakagePower\\PinwireMappings.csv");
		try {
			CsvReader csv = new CsvReader(new FileReader(PinFile));
			while(csv.readRecord()){
				String PipWire = csv.get(0);
				String PinWire = csv.get(1);
				Net N = new Net("DUMMY_NET_" +PipWire+PinWire,NetType.WIRE);
				PIP P = new PIP();
				P.setStartWire(design.getWireEnumerator().getWireEnum(PipWire));
				P.setEndWire(design.getWireEnumerator().getWireEnum(PinWire));
				N.addPIP(P);
				design.addNet(N);
				DummyNets.add(N);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		//for(Net N:DummyNets){
		//	System.out.println(N.toString(design.getWireEnumerator()));
		//}
		return DummyNets; 
	}
	public static void validatePIPs(ArrayList<Net> Nets, Virtex4SwitchBoxRouter SBR, Design design){
		for(Net N : Nets){
			for(PIP P: N.getPIPs()){
				JNode test = SBR.SwitchBoxGraph.get(P.getStartWire());
				boolean validated = false;
				if(test != null){
					for(WireConnection W : test.getConnections()){
						if(W.getWire() == P.getEndWire()){
							validated = true;
							break; 
						}
					}
				
					if(validated){
						System.out.println(P.toString(design.getWireEnumerator()) + " Validated");
					} else {
						System.out.println(P.toString(design.getWireEnumerator()) + "Not Validated");
					}
				}
			}   
		}
	}
	public static void setBlockedInputsTile(ArrayList<Net> Nets, Tile T){
		for(Net N : Nets){
			for(PIP P : N.getPIPs()){
				P.setTile(T);
			}
		}
	}
	
	private static ArrayList<Virtex4CLB> instantiateTestSites(int num_testsites, FPGA fpga, Design design, Device device, V4XilinxToolbox TB){
		ArrayList<Virtex4CLB> TestSites = new ArrayList<Virtex4CLB>(); 
		
		int j=0;
        for(Tile T: XDLDesignUtils.getAllCLBTiles(device)){
			TestSites.add(new Virtex4CLB(fpga, design, T, TB));
			j++;
			if(j > num_testsites){
				break;
			}
		}
        SetupCLBs(TestSites, design, fpga, TB);
        
        return TestSites;
	}
	
	public static Net cloneNet(Net N, Tile IntTile, Tile ClbTile, Virtex4Slice S){
	   Net C = new Net(N.getName() + ClbTile.toString(), N.getType());
	   
	   for(Pin P : N.getPins()){
		   Pin PC = new Pin(true, P.getName(), S.getXDLInstance());
		   C.addPin(PC);
	   }
	   
	   for(PIP P : N.getPIPs()){
		   PIP PC = new PIP();
		   if(P.getTile().getType() == TileType.INT){
			   PC.setTile(IntTile); 
		   } else
			   PC.setTile(ClbTile);
		   
		   PC.setStartWire(P.getStartWire());
		   PC.setEndWire(P.getEndWire());
		   C.addPIP(PC);
	   }
	   
	   return C;
	}
	
	private static void CreateTestBitstream(Net N, WireMux M, ArrayList<Virtex4CLB> testSites, Virtex4SwitchBoxRouter SBR, FPGA fpga,	Design design, Device device,V4XilinxToolbox TB, String outputFilename) {
		
		//Finish Connecting the Wires (for good Measure)
		for(Virtex4CLB CLB: testSites){
		   Virtex4Slice S = getMatchingSlice(N, CLB, design.getWireEnumerator());
		   String PinName = getMatchingsPinName(N, design.getWireEnumerator());
		   N.addPin(new Pin(true, PinName,  S.getXDLInstance()));
		   
			//Virtex4Slice SliceLeft = getMatchingSlice(nets.getLeft(), CLB, design.getWireEnumerator());
     	   //Virtex4Slice SliceRight = getMatchingSlice(nets.getRight(), CLB,design.getWireEnumerator());
     	   //String PinNameLeft = getMatchingsPinName(nets.getLeft(), design.getWireEnumerator());
     	   //String PinNameRight = getMatchingsPinName(nets.getRight(), design.getWireEnumerator());
           //nets.getLeft().addPin(new Pin(true, PinNameLeft,  SliceLeft.getXDLInstance()));
  		   //nets.getRight().addPin(new Pin(true,PinNameRight, SliceRight.getXDLInstance()));
           
		   Net NewN = cloneNet(N, CLB.getINTTile(), CLB.getCLBTile(), S);
		   design.addNet(NewN);
  		   //Net NewLeft = cloneNet(nets.getLeft(), CLB.getINTTile(), CLB.getCLBTile(), SliceLeft);
  		   //Net NewRight= cloneNet(nets.getRight(),CLB.getINTTile(), CLB.getCLBTile(), SliceRight);
  		   //design.addNet(NewLeft);
           //design.addNet(NewRight);
            
           
           //Set Outputs Correctly
		   if(PinName.equals("X")){
			   S.getLutF().ClearLutContents();
		   } else {
			   S.getLutG().ClearLutContents();
		   }
          /* if(PinNameLeft.equals("X")){
              SliceLeft.getLutF().ClearLutContents();
           } else {
              SliceLeft.getLutG().ClearLutContents();
           }
         
           if(PinNameLeft.equals("X")){
               SliceRight.getLutF().AssertLutContent();
           } else {
               SliceRight.getLutG().AssertLutContent();
           }
           */
           //Activate PIPs
		   for(PIP P: NewN.getPIPs()){
	             if(P.getTile().getType() == TileType.CLB){
	                P.setTile(CLB.getCLBTile());
	             } else {
	                P.setTile(CLB.getINTTile());
	             }
	        	 SBR.SB.ActivatePIP(P);
	       }
		   
		   setWireMuxBits(M, CLB.getINTTile(), fpga, TB);
		   /*
           for(PIP P: NewLeft.getPIPs()){
             if(P.getTile().getType() == TileType.CLB){
                P.setTile(CLB.getCLBTile());
             } else {
                P.setTile(CLB.getINTTile());
             }
        	 SBR.SB.ActivatePIP(P);
           }
           for(PIP P: NewRight.getPIPs()){
               if(P.getTile().getType() == TileType.CLB){
                  P.setTile(CLB.getCLBTile());
               } else {
                  P.setTile(CLB.getINTTile());
               }
          	 SBR.SB.ActivatePIP(P);
             }
             */
		   
           
		   //DeActivate Row Bit of Last PIP
           //Pip P1 = findTargetPIP(NewLeft, M, design.getWireEnumerator());
           //Pip P2 = findTargetPIP(NewRight, M, design.getWireEnumerator());
           
           //V4ConfigurationBit P1RowBit = new V4ConfigurationBit(fpga, P1.getRowBit(), CLB.getINTTile(), TB);
           //V4ConfigurationBit P2RowBit = new V4ConfigurationBit(fpga, P2.getRowBit(), CLB.getINTTile(), TB);
           
           //Disable the Row Bits // The Column Bits Should Both Be on
           /*if(P1.getRowBit().equals(P2.getRowBit()) && !P1.getColumnBit().equals(P2.getColumnBit())){
        	  P1RowBit.SetBitValueInBitStream(0, CLB.getINTTile());
              P2RowBit.SetBitValueInBitStream(0, CLB.getINTTile());
           } else {
             System.out.println("Bad Bit Values! Error Emergency!");
             System.out.println("Row Bits " + P1.getRowBit().toString() + " " + P2.getRowBit().toString());
             System.out.println("Col Bits " + P1.getColumnBit().toString() + " " + P2.getColumnBit().toString());
             System.exit(1);
           }*/
       }
	   
	   //System.out.println("Saving XDL File");
	   //design.saveXDLFile(outputFilename + ".xdl");
	   //System.out.println("Saving Bitstream file");
	   //XDLDesignUtils.writeBitstreamToFile(fpga, outputFilename + ".bit");
	}
	public static void setWireMuxBits(WireMux M, Tile T, FPGA fpga, V4XilinxToolbox TB){
		//Set all row bits to zero
        for(Bit B:M.getTable().getRowBits()){
     	   V4ConfigurationBit Tmp = new V4ConfigurationBit(fpga, B, T, TB);
     	   Tmp.SetBitValueInBitStream(0, T);
        }
		   
        //Set all col bits to one
        for(Bit B:M.getTable().getColBits()){
     	   V4ConfigurationBit Tmp = new V4ConfigurationBit(fpga, B, T, TB);
     	   Tmp.SetBitValueInBitStream(1, T);
        }
	}
	private static Pip findTargetPIP(Net n, WireMux m, WireEnumerator we) {
		
		for(PIP P : n.getPIPs()){
		   if(P.getEndWire() == we.getWireEnum(m.getDestName())){
		      PIP P2 = m.getTable().getPip(P.getStartWire()).getPip();
		      P2.setTile(P.getTile());
			  if(P.equals(P2)){
		    	   return m.getTable().getPip(P.getStartWire());
		      }
		   }
		}
		
		System.out.println("Cannot Find the Target PIP: It really should be here!!");
		System.out.println(n.toString(we));
	    m.PrintTable(); 
		System.exit(1);
		return null;
	}

	private static void CreateTestBitstreams(ArrayList<WireMux> Muxes, boolean DoubleVerify, String BitstreamDirectory, String BlankBitstreamFilename) {
		
		String StorageDirectoryDoubleVerify = "Z:\\FPGA\\Gremlin\\PUF_TestBoardExperiments\\LeakagePower\\ReverseVerifyBitstreamsAndData\\DoubleVerifyTestBitstreams\\";
		String StorageDirectorySingleVerify = "Z:\\FPGA\\Gremlin\\PUF_TestBoardExperiments\\LeakagePower\\ReverseVerifyBitstreamsAndData\\SingleVerifyTestBitstreams\\";
		ArrayList<String> TestBitFileNames = new ArrayList<String>();
	   
		for(WireMux M : Muxes){
			System.out.println("Creating " + M.getDestName() + " Bitstreams.");
			
			Design design = new Design();
			design.setNCDVersion("v3.2");
			design.setPartName("xc4vlx60ff668-12");
			
			Device device = design.getDevice();
				
			FPGA fpga = XDLDesignUtils.getVirtexFPGA("XC4VLX60");
			XDLDesignUtils.loadBitstream(fpga, BlankBitstreamFilename);
			V4XilinxToolbox TB = XDLDesignUtils.getV4Toolbox(fpga);
			
			Virtex4SwitchBox SB = new Virtex4SwitchBox(fpga, design, TB);
	        Virtex4SwitchBoxRouter SBR = new Virtex4SwitchBoxRouter(SB);
	        
	        ArrayList<Net> dummyNets = getNetsToBlockCLBInputs(design);
	   
	        ArrayList<Virtex4CLB> testSites = instantiateTestSites(500, fpga, design, device, TB);
	       
	        String filenameNonTransposed = M.getDestName() + "NonTransposed";
			String outputFilenameNonTransposed = BitstreamDirectory + filenameNonTransposed;
			setBlockedInputsTile(dummyNets, testSites.get(0).getCLBTile());
			SBR.setTiles(testSites.get(0).getINTTile(), testSites.get(0).getCLBTile());
			Net N = CreateReverseEngineeringShorts(M, SBR, design);
			CreateTestBitstream(N, M, testSites, SBR, fpga, design, device, TB, outputFilenameNonTransposed);
			TestBitFileNames.add(StorageDirectoryDoubleVerify+filenameNonTransposed + ".bit");
			
			System.out.println("Saving XDL File");
			design.saveXDLFile(outputFilenameNonTransposed + ".xdl");
			System.out.println("Saving Bitstream file");
			XDLDesignUtils.writeBitstreamToFile(fpga, outputFilenameNonTransposed + ".bit");
			
			M.TransposeTable();
			String filenameTransposed = M.getDestName() + "Transposed";
			String outputFilenameTransposed = BitstreamDirectory +filenameTransposed;
			
			for(Virtex4CLB CLB : testSites){
			    Tile T = CLB.getINTTile();
				setWireMuxBits(M, T, fpga, TB);
			}
			
			TestBitFileNames.add(StorageDirectoryDoubleVerify+filenameTransposed + ".bit");
	        
			System.out.println("Saving XDL File");
			design.saveXDLFile(outputFilenameTransposed + ".xdl");
			System.out.println("Saving Bitstream file");
			XDLDesignUtils.writeBitstreamToFile(fpga, outputFilenameTransposed + ".bit");
			
			//Create a Transposed and Non Transposed Sets of Data Structures
			//NonTranposedSet
			/*
			Design nonTransposedDesign = new Design();
			nonTransposedDesign.setNCDVersion("v3.2");
			nonTransposedDesign.setPartName("xc4vlx60ff668-12");
			
			Device nonTransposedDevice = nonTransposedDesign.getDevice();
				
			FPGA nonTransposedFpga = XDLDesignUtils.getVirtex4FPGA("XC4VLX60");
			XDLDesignUtils.loadBitstream(nonTransposedFpga, BlankBitstreamFilename);
			V4XilinxToolbox nonTransposedTB = XDLDesignUtils.getV4Toolbox(nonTransposedFpga);
			
			Virtex4SwitchBox nonTransposedSB = new Virtex4SwitchBox(nonTransposedFpga, nonTransposedDesign, nonTransposedTB);
	        Virtex4SwitchBoxRouter nonTransposedSBR = new Virtex4SwitchBoxRouter(nonTransposedSB);
	        
	        ArrayList<Net> nonTransposedDummyNets = getNetsToBlockCLBInputs(nonTransposedDesign);
	        
	        ArrayList<Virtex4CLB> nonTransposedTestSites = instantiateTestSites(900, nonTransposedFpga, nonTransposedDesign, nonTransposedDevice, nonTransposedTB);
	        
			//Transposed Set	
			Design transposedDesign = new Design(); 
			transposedDesign.setNCDVersion("v3.2");
			transposedDesign.setPartName("xc4vlx60ff668-12");
			
			Device transposedDevice = transposedDesign.getDevice();
			
			FPGA   transposedFpga = XDLDesignUtils.getVirtex4FPGA("XC4VLX60");
			XDLDesignUtils.loadBitstream(transposedFpga, BlankBitstreamFilename);
			V4XilinxToolbox transposedTB = XDLDesignUtils.getV4Toolbox(transposedFpga);
			
			Virtex4SwitchBox transposedSB = new Virtex4SwitchBox(transposedFpga, transposedDesign, transposedTB);
	        Virtex4SwitchBoxRouter transposedSBR = new Virtex4SwitchBoxRouter(transposedSB);
			
	        ArrayList<Net> transposedDummyNets = getNetsToBlockCLBInputs(transposedDesign);
	           
	        ArrayList<Virtex4CLB> transposedTestSites = instantiateTestSites(900, transposedFpga, transposedDesign, transposedDevice, transposedTB);
	        
			/*if(DoubleVerify == true){
				String filenameNonTransposed = M.getDestName() + "NonTransposed";
				String outputFilenameNonTransposed = BitstreamDirectory + filenameNonTransposed;
				setBlockedInputsTile(nonTransposedDummyNets, nonTransposedTestSites.get(0).getCLBTile());
				nonTransposedSBR.setTiles(nonTransposedTestSites.get(0).getINTTile(), nonTransposedTestSites.get(0).getCLBTile());
				//Pair<Net> nonTransposedNets = CreateReverseEngineeringShorts(M, nonTransposedSBR, nonTransposedDesign);
				Net nonTransposedNets = CreateReverseEngineeringShorts(M, nonTransposedSBR, nonTransposedDesign);
				CreateTestBitstream(nonTransposedNets, M, nonTransposedTestSites, nonTransposedSBR, nonTransposedFpga, nonTransposedDesign, nonTransposedDevice, nonTransposedTB, outputFilenameNonTransposed);
				TestBitFileNames.add(StorageDirectoryDoubleVerify+filenameNonTransposed + ".bit");
				
				M.TransposeTable();
				String filenameTransposed = M.getDestName() + "Transposed";
				String outputFilenameTransposed = BitstreamDirectory +filenameTransposed;
				setBlockedInputsTile(transposedDummyNets, transposedTestSites.get(0).getCLBTile());
				transposedSBR.setTiles(transposedTestSites.get(0).getINTTile(), transposedTestSites.get(0).getCLBTile());
				Net transposedNets = CreateReverseEngineeringShorts(M, transposedSBR, transposedDesign);	
				CreateTestBitstream(transposedNets, M, transposedTestSites, transposedSBR, transposedFpga, transposedDesign, transposedDevice, transposedTB, outputFilenameTransposed);
			    TestBitFileNames.add(StorageDirectoryDoubleVerify+filenameTransposed + ".bit");
			} else {
				String filenameNonTransposed = M.getDestName() + "NonTransposed";
				String outputFilenameNonTransposed = BitstreamDirectory + filenameNonTransposed;
				setBlockedInputsTile(nonTransposedDummyNets, nonTransposedTestSites.get(0).getCLBTile());
				nonTransposedSBR.setTiles(nonTransposedTestSites.get(0).getINTTile(), nonTransposedTestSites.get(0).getCLBTile());
				Net nonTransposedNets = CreateReverseEngineeringShorts(M, nonTransposedSBR, nonTransposedDesign);	
				if(nonTransposedNets != null){
				  CreateTestBitstream(nonTransposedNets, M, nonTransposedTestSites, nonTransposedSBR, nonTransposedFpga, nonTransposedDesign, nonTransposedDevice, nonTransposedTB, outputFilenameNonTransposed);
				  TestBitFileNames.add(StorageDirectorySingleVerify+filenameNonTransposed + ".bit");
				}
				M.TransposeTable();
				String filenameTransposed = M.getDestName() + "Transposed";
				String outputFilenameTransposed = BitstreamDirectory +filenameTransposed;
				setBlockedInputsTile(transposedDummyNets, transposedTestSites.get(0).getCLBTile());
				transposedSBR.setTiles(transposedTestSites.get(0).getINTTile(), transposedTestSites.get(0).getCLBTile());
				Net transposedNets = CreateReverseEngineeringShorts(M, transposedSBR, transposedDesign);	
				if(transposedNets != null){
				  CreateTestBitstream(transposedNets, M, transposedTestSites, transposedSBR, transposedFpga, transposedDesign, transposedDevice, transposedTB, outputFilenameTransposed);
				  TestBitFileNames.add(StorageDirectorySingleVerify+filenameTransposed + ".bit");
				}
			}
			*/	
		}
		
		 writeStringArrayToFile(TestBitFileNames, StorageDirectoryDoubleVerify + "BitstreamList.txt");
		/*if(DoubleVerify){
	   	  writeStringArrayToFile(TestBitFileNames, StorageDirectoryDoubleVerify + "BitstreamList.txt");
		} else {
		  writeStringArrayToFile(TestBitFileNames, StorageDirectorySingleVerify + "BitstreamList.txt");	
		}*/
	}

    public static void main(String args[]){
    	
    	//Filename of List of Muxes that can be double verified
    	String DoubleVerifyFilename = "Z:\\javaworkspace2\\AddShorts\\DoubleVerifyMuxes.txt"; 
    	//Filename of List of Muxes that can be single verified
        String SingleVerifyFilename = "Z:\\javaworkspace2\\AddShorts\\SingleVerifyMuxes.txt"; 
        
        String SingleVisualFilename = "Z:\\javaworkspace2\\AddShorts\\SingleVerifyVisual.xdl";
        String DoubleVisualFilename = "Z:\\javaworkspace2\\AddShorts\\DoubleVerifyVisual.xdl";
        
        String SingleVerifyBitstreamDir = "C:\\Users\\joshuas2\\Desktop\\SingleVerifyTestBitstreams\\";
        String DoubleVerifyBitstreamDir = "C:\\Users\\joshuas2\\Desktop\\DoubleVerifyTestBitstreams\\";
        
        String SingleVerifyResultsFile = "Z:\\FPGA\\Gremlin\\PUF_TestBoardExperiments\\LeakagePower\\ReverseVerifyBitstreamsAndData\\DoubleVerifyTestBitstreams\\DoubleVerifyBitlistResults.txt";
        String DoubleVerifyResultsFile = "Z:\\FPGA\\Gremlin\\PUF_TestBoardExperiments\\LeakagePower\\ReverseVerifyBitstreamsAndData\\SingleVerifyTestBitstreams\\ReverseVerifyResults.txt";
        
        String SingleNetVerifyResultsFile = "Z:\\FPGA\\Gremlin\\PUF_TestBoardExperiments\\LeakagePower\\ReverseVerifyBitstreamsAndData\\SingleNetVerifyTestBitstreams\\BitstreamResults.txt"; 
        String BlankBitstreamFilename = "Z:\\FPGA\\Gremlin\\PUF_TestBoardExperiments\\blank.bit";
        ArrayList<String> SingleVerifyNames = readFile(SingleVerifyFilename);
        ArrayList<String> DoubleVerifyNames = readFile(DoubleVerifyFilename);
        
        ArrayList<WireMux> SingleVerifyMuxes = instantiateMuxes(SingleVerifyNames);
        ArrayList<WireMux> DoubleVerifyMuxes = instantiateMuxes(DoubleVerifyNames);
                
        //CreateVisualVerificationFile(SingleVerifyMuxes,SingleVisualFilename);
        //CreateVisualVerificationFile(DoubleVerifyMuxes,DoubleVisualFilename);
        
        ArrayList<WireMux> SingleVerifyMuxes2 = instantiateMuxes(SingleVerifyNames);
        ArrayList<WireMux> DoubleVerifyMuxes2 = instantiateMuxes(DoubleVerifyNames);
        
        //CreateTestBitstreams(SingleVerifyMuxes2, false, SingleVerifyBitstreamDir, BlankBitstreamFilename);
        //CreateTestBitstreams(DoubleVerifyMuxes2, true,  DoubleVerifyBitstreamDir, BlankBitstreamFilename);
        ArrayList<WireMux> SingleVerifyMuxes3 = instantiateMuxes(SingleVerifyNames);
        ArrayList<WireMux> DoubleVerifyMuxes3 = instantiateMuxes(DoubleVerifyNames);
        
        ParseResults(SingleNetVerifyResultsFile, DoubleVerifyResultsFile, SingleVerifyMuxes3, DoubleVerifyMuxes3);
        
        System.out.println("Done");
    }
    
    public static String removeDirectory(String Filename){
    	Filename = FileTools.removeFileExtension(Filename);
    	int index = Filename.lastIndexOf("\\");
    	Filename = Filename.substring(index+1);
    	System.out.println(Filename);
    	return Filename; 
    }
    
    public static String  getWire(String Filename){
    	
    	Filename = FileTools.removeFileExtension(Filename);
    	if(Filename.contains("NonTransposed"))
    	{
    		return Filename.substring(0, Filename.length()-13);
    	} else if(Filename.contains("Transposed")) {
    		return Filename.substring(0, Filename.length()-10);
    	}
    	
    	return null;
    }
    public static boolean isTransposed(String Filename) {
    	if(Filename.contains("NonTransposed")){
    		return false; 
    	} else {
    		return true; 
    	}
    }
    
	private static void ParseResults(String singleVerifyResultsFile,String doubleVerifyResultsFile,	ArrayList<WireMux> singleVerifyMuxes3,
			ArrayList<WireMux> doubleVerifyMuxes3) {
		
		TreeMap<String, ResultRecord> RecordSet = new TreeMap<String,ResultRecord>();
		ArrayList<String> results = readFile(singleVerifyResultsFile);
		//results.addAll(readFile(doubleVerifyResultsFile));
		
		 
		String [] resultsArray = (String[]) results.toArray(new String [results.size()]);
		for(int i = 0; i < resultsArray.length; i+=2){
			String removedDirectory = removeDirectory(resultsArray[i]);
			
			float reading = Float.parseFloat(resultsArray[i+1]);
			reading = reading * 10; 
			String wire = getWire(removedDirectory);
			System.out.println(wire);
			if(!RecordSet.containsKey(wire)){
				ResultRecord r = new ResultRecord(wire);
				//Record has not been add to set yet
				if(isTransposed(resultsArray[i])){
					r.setTransposedMeasurement(reading);
				} else {
					r.setNonTransposedMeasurement(reading);
				}
				RecordSet.put(wire, r);
			} else {
				ResultRecord r = RecordSet.get(wire);
				if(isTransposed(resultsArray[i])){
					r.setTransposedMeasurement(reading);
				} else {
					r.setNonTransposedMeasurement(reading);
				}
				r.setDoubleVerified(true);
			}
		}
		
		
		ArrayList<String> csv = new ArrayList<String>();
		
		for(String S : RecordSet.keySet()){
			ResultRecord Cur = RecordSet.get(S);
			Cur.evaluate(); 
			csv.add(Cur.toCSV());
			System.out.println(Cur);
		}
		
		writeStringArrayToFile(csv, "Z:\\javaworkspace2\\gremlinTools\\WireMuxTransposeInfo.txt");
	}
   
	
}

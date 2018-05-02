package edu.byu.ece.gremlinTools.ReliabilityTools;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import XDLDesign.Utils.XDLDesignUtils;
import edu.byu.ece.gremlinTools.Virtex4Bits.Pair;
import edu.byu.ece.gremlinTools.Virtex4Bits.V4ConfigurationBit;
import edu.byu.ece.gremlinTools.Virtex4Bits.Virtex4SliceBits;
import edu.byu.ece.gremlinTools.xilinxToolbox.V4XilinxToolbox;
import edu.byu.ece.rapidSmith.bitstreamTools.configuration.FPGA;
import edu.byu.ece.rapidSmith.design.Attribute;
import edu.byu.ece.rapidSmith.design.Design;
import edu.byu.ece.rapidSmith.design.Instance;
import edu.byu.ece.rapidSmith.device.PrimitiveType;
import edu.byu.ece.rapidSmith.device.Tile;
import edu.byu.ece.rapidSmith.device.TileType;

public class LogicAnalyzer {
	private Design design; 
	private FPGA fpga;
    private V4XilinxToolbox TB; 
    private Virtex4SliceBits SliceBitsX0Y0;
    private Virtex4SliceBits SliceBitsX0Y1;
    private Virtex4SliceBits SliceBitsX1Y0;
    private Virtex4SliceBits SliceBitsX1Y1;
    private SliceDependencies SDep; 
    
    private HashMap<Tile, ArrayList<Instance>> TileToInstances; 
    private ArrayList<Pair<V4ConfigurationBit, String>> BitsToInstanceNames;
    protected HashMap<V4ConfigurationBit, String> _instNameHash;
    
    private int UsedInstanceMatches; 
    private int UnusedInstanceMatches;
    private int UnusedInstanceMatchesFound;
    private int UnmatchedInstances; 
    private int BramAttributeFailures; 
    private int dspAttributeFailures; 
    private int iobAttributeFailures; 
    private int DCMMatches;
    private int NoMatchingAttributes;
    private int NoInstancesInTile;
    private int MatchedNoInstancesInTile;
    private int UnusedInstanceMatchesNotFound;
    private int NullTiles;
    public LogicAnalyzer(Design d, FPGA f, V4XilinxToolbox tb){
    	System.out.println("Starting Logic...");
    	design = d; 
    	fpga = f;
    	TB = tb; 
    	
    	SliceBitsX0Y0 = new Virtex4SliceBits(0, 0, PrimitiveType.SLICEM, f, design.getDevice(), tb);
    	SliceBitsX0Y1 = new Virtex4SliceBits(0, 1, PrimitiveType.SLICEM, f, design.getDevice(), tb);
    	SliceBitsX1Y0 = new Virtex4SliceBits(1, 0, PrimitiveType.SLICEL, f, design.getDevice(), tb);
    	SliceBitsX1Y1 = new Virtex4SliceBits(1, 1, PrimitiveType.SLICEL, f, design.getDevice(), tb);
    	TileToInstances = new HashMap<Tile,ArrayList<Instance>>();
    	BitsToInstanceNames = new ArrayList<Pair<V4ConfigurationBit, String>>();
    	_instNameHash = new HashMap<V4ConfigurationBit, String>();
    	SDep = new SliceDependencies(d);
    	createTilesToInstancesMap();
    	UsedInstanceMatches = 0;
    	UnusedInstanceMatches = 0;
    	UnusedInstanceMatchesFound = 0;
    	UnmatchedInstances = 0;
    	BramAttributeFailures = 0; 
    	dspAttributeFailures = 0; 
    	iobAttributeFailures = 0; 
    	DCMMatches = 0; 
    	NoMatchingAttributes =0;
    	NoInstancesInTile =0;
    	MatchedNoInstancesInTile = 0;
    	NullTiles = 0; 
    	UnusedInstanceMatchesNotFound=0;
    }
    public Virtex4SliceBits getSliceX0Y0(){
    	return SliceBitsX0Y0; 
    }
    public Virtex4SliceBits getSliceX0Y1(){
    	return SliceBitsX0Y1; 
    }
    public Virtex4SliceBits getSliceX1Y0(){
    	return SliceBitsX1Y0; 
    }
    public Virtex4SliceBits getSliceX1Y1(){
    	return SliceBitsX1Y1; 
    }
    private void createTilesToInstancesMap() {
    	ArrayList<Instance> Instances = null;
		for(Instance I : design.getInstances()){
			if(( Instances = TileToInstances.get(I.getTile())) == null){
				Instances = new ArrayList<Instance>();
			} 
			Instances.add(I);
			TileToInstances.put(I.getTile(), Instances);
		}
	}
    
    
    public ArrayList<String> getPhysicalNames(ArrayList<String> AttributeNames){    	
    	
    	ArrayList<String> PhysicalNames = new ArrayList<String>(); 
    	for(String Name : AttributeNames){
    		int index = Name.indexOf(":");
    		PhysicalNames.add(Name.substring(0, index));
    	}
    	  
		return PhysicalNames; 
		
    }
    
 
    //This function first checks if the bits belongs to the slice and then decides if the bit was used or could effect
    //the design. 
    
    public boolean checkSliceBit(V4ConfigurationBit B, SensitiveBit sbit, Virtex4SliceBits SliceBits) {
    	ArrayList<Pair<Instance, Attribute>> SliceDepResults; 
    	ArrayList<String> AttributeNames = null; 
    	
    	//Check if the bit maps to the slice if not return false
    	if((AttributeNames = SliceBits.getAttributeNameFromBit(B)) != null) {
    		//boolean foundInstanceMatch = false; 
    		
    		Instance I = getMatchingInstance(B.getTile(), SliceBits);
    		
    		//A bit may map to more than one attribute, to ensure we get complete coverage get the physical names of all attributes
    		ArrayList<String> PhysicalNames = getPhysicalNames(AttributeNames);
    		
    		//If the Instance Exists Check to see if any of the attributes that the bit maps to are used in the design. 
    		if(I != null) {
    			for(String PhysicalName:PhysicalNames) {
    				Attribute A = I.getAttribute(PhysicalName);
    				
    				if(A != null && !A.getValue().equals("#OFF")){
    					UsedInstanceMatches++;
    					BitsToInstanceNames.add(new Pair<V4ConfigurationBit, String>(B, I.getName()));
    					_instNameHash.put(B, I.getName());
    					sbit.setFailType(SensitiveBit.INSTANCE_ATTRIBUTE);
    					sbit.setInstance(I);
    					sbit.setAttribute(A);
    					return true; 
    				}
    			}
    		}		
    		NoMatchingAttributes++;
    		
    		//Since there are not direct matches we need to ask Slice dependencies for any additional matches
    		for(String PhysicalName:PhysicalNames){
    			Attribute A = new Attribute(PhysicalName, "", "");
    			// too many null pointer exceptoins
//    			if (false) {
    				if((SliceDepResults = SDep.AffectedAttributes(SliceBits, B, TileToInstances, A, B.getTile())) != null) {
    					if(SliceDepResults.size() > 0) {
    						UnusedInstanceMatchesFound++;
    						String InstanceName = SliceDepResults.get(0).getLeft().getName();
    						BitsToInstanceNames.add(new Pair<V4ConfigurationBit, String>(B, InstanceName));
    						_instNameHash.put(B, InstanceName);
    						sbit.setFailType(SensitiveBit.INSTANCE_UNUSED);
    						sbit.setInstance(SliceDepResults.get(0).getLeft());
    						return true;
    					}
    				}
//    			}
    		}
    		
    		//If SliceDep can't find the answer count it and return false 
    		UnusedInstanceMatchesNotFound++;	
    		sbit.setFailType(SensitiveBit.INSTANCE_UNUSED_UNFOUND);
    		return false; 
    	}
    	
    	//If the bit is not in this slice return false 
    	return false;
    	
    }    
    
    //This function first checks if the bits belongs to the slice and then decides if the bit was used or could effect
    //the design. 
    
    public boolean checkSliceBits(V4ConfigurationBit B, Virtex4SliceBits SliceBits) {
    	ArrayList<Pair<Instance, Attribute>> SliceDepResults; 
    	ArrayList<String> AttributeNames = null; 
    	
    	//Check if the bit maps to the slice if not return false
    	if((AttributeNames = SliceBits.getAttributeNameFromBit(B)) != null){
        	//boolean foundInstanceMatch = false; 
        	
			Instance I = getMatchingInstance(B.getTile(), SliceBits);
			
			//A bit may map to more than one attribute, to ensure we get complete coverage get the physical names of all attributes
			ArrayList<String> PhysicalNames = getPhysicalNames(AttributeNames);
			
			//If the Instance Exists Check to see if any of the attributes that the bit maps to are used in the design. 
			if(I != null){
			   for(String PhysicalName:PhysicalNames){
				   Attribute A = I.getAttribute(PhysicalName);
				   
				   if(A != null && !A.getValue().equals("#OFF")){
					   UsedInstanceMatches++;
					   BitsToInstanceNames.add(new Pair<V4ConfigurationBit, String>(B, I.getName()));
			        	_instNameHash.put(B, I.getName());
			        	return true; 
				   }
			   }
			}		
			NoMatchingAttributes++;
			
			//Since there are not direct matches we need to ask Slice dependencies for any additional matches
			for(String PhysicalName:PhysicalNames){
				Attribute A = new Attribute(PhysicalName, "", "");
				// too many null pointer exceptoins
				if (false) {
					if((SliceDepResults = SDep.AffectedAttributes(SliceBits, B, TileToInstances, A, B.getTile())) != null){
					    if(SliceDepResults.size()>0){
					    	UnusedInstanceMatchesFound++;
							String InstanceName = SliceDepResults.get(0).getLeft().getName();
							BitsToInstanceNames.add(new Pair<V4ConfigurationBit, String>(B, InstanceName));
				        	_instNameHash.put(B, InstanceName);
					    	return true;
					    }
					}
				}
			}
			
			//If SliceDep can't find the answer count it and return false 
			UnusedInstanceMatchesNotFound++;	
			return false; 
    	}
	  
      //If the bit is not in this slice return false 
      return false;
   
    }
    
    //public void analyzeJustinBits
    //Find the Instance that Matches this slice
    public Instance getMatchingInstance(Tile T, Virtex4SliceBits Slice){
    	
    	ArrayList<Instance> Instances =  TileToInstances.get(T);
    	
    	if(Instances != null){
    		for(Instance I : Instances){
    			if((I.getInstanceX() %2 == Slice.getX()) && ((I.getInstanceY() % 2) == Slice.getY())){
    				return I; 
    			}
    		}
    	}
 
    	return null; 
    }
    public HashMap<V4ConfigurationBit, String> getInstanceNameHash() {
    	return _instNameHash;
    }
    
    public void writeNathanReport(String Filename){
    	try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(Filename));
			for(Pair<V4ConfigurationBit, String> P : BitsToInstanceNames){
				bw.write(P.getLeft().getFAR().getHexAddress() + " " + P.getLeft().getBitNumber() + " " + P.getRight() + "\n");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void printReport(){
    	System.out.println("------------------------------------------------------\n" +
    			           "              Logic Analyzer Report                   \n" +
    			           "------------------------------------------------------\n"
    			          +"UsedInstanceMatches: " + UsedInstanceMatches + "\n" 
    			          +"UnusedInstanceMatches: " + UnusedInstanceMatches +"\n"
    			          +"UnmatchedInstances: " + UnmatchedInstances  + "\n"
    			          +"BramAttributeFailures: " + BramAttributeFailures + "\n" 
    			          +"iobAttributeFailures: " + iobAttributeFailures + "\n" 
    			          +"dspAttributeFailures: " + dspAttributeFailures + "\n" 
    			          +"DCMMatches: " + DCMMatches + "\n"
    			          + "NoInstancesInTile: " + NoInstancesInTile + "\n"
    			          + "NullTiles: " + NullTiles + "\n"
    			          +"NoMatchingAttributes: " + NoMatchingAttributes + "\n" 
    			          +"UnusedInstanceMatchesFound: " + UnusedInstanceMatchesFound + "\n"
    			          +"UnusedInstanceMatchesNotFound: " + UnusedInstanceMatchesNotFound + "\n"
    			          +"Total Logic Matches: " + (UsedInstanceMatches+UnusedInstanceMatchesFound) + "\n"
    			          +"Total Bits Analyzed: " + (UsedInstanceMatches +NoMatchingAttributes + DCMMatches+BramAttributeFailures+iobAttributeFailures+dspAttributeFailures));
    	
    	
    }

    public ArrayList<LEON3Result> AnalyzeLogicBits(TreeMap<LEON3Result, V4ConfigurationBit> xrtcHash, ArrayList<LEON3Result> xrtcList) {
    	ArrayList<LEON3Result> retval = new ArrayList<LEON3Result>();
    	
    	for(LEON3Result xrtc : xrtcList) {
    		V4ConfigurationBit cbit = xrtcHash.get(xrtc);
    		SensitiveBit sbit = new SensitiveBit();
    		sbit.setConfigurationBit(cbit);
    		V4ConfigurationBit rbit = AnalyzeBit(cbit, sbit);
    		xrtc.setSensitiveBit(sbit);
    		if (rbit != null) {
    			retval.add(xrtc);
    		}
    	}

    	return retval;
    }
    
    public V4ConfigurationBit AnalyzeBit(V4ConfigurationBit B, SensitiveBit sbit) {
    	
    	//Does the attribute Corespond to a used slice? 
    	//is the Attribute used in the design?
    	V4ConfigurationBit RemainingBit = null;
    	if (B != null) {
    		if(B.getTile() != null) {
    			if(B.getTile().getType() == TileType.INT_SO) {
    				if (B.getFAR().getBlockType() == 0) {
    					
    					// NB: this if statement is V4 FX60 specific!!
    					if (B.getFAR().getColumn() == 20 || B.getFAR().getColumn() == 39) {
    						dspAttributeFailures++;
    						sbit.setFailType(SensitiveBit.DSP_ATTRIBUTE_FAILURE);
    					} else {
    						iobAttributeFailures++;
    						sbit.setFailType(SensitiveBit.IOB_ATTRIBUTE_FAILURE);
    					}
    				} else {
    					BramAttributeFailures++; 
    					sbit.setFailType(SensitiveBit.BRAM_ATTRIBUTE_FAILURE);
    				}
    				
    			} else if (B.getTile().getType() == TileType.INT_SO_DCM0) {
    				DCMMatches++;
    				sbit.setFailType(SensitiveBit.DCM_FAILURE);
    			} else {
    				B.setTile(XDLDesignUtils.getMatchingCLBTile(B.getTile(), design.getDevice()));       
    				if(!(checkSliceBit(B, sbit, SliceBitsX0Y0) || checkSliceBit(B, sbit, SliceBitsX0Y1) || 
    						checkSliceBit(B, sbit, SliceBitsX1Y0) ||checkSliceBit(B, sbit, SliceBitsX1Y1))) {
    					RemainingBit = B;
    				}
    			}	
    		}	
    		else {
    			//ClockBits
    			NullTiles++;
    			RemainingBit = B;
    		}	   	 
    	}
    	
    	return RemainingBit;
    }
   
    
	public ArrayList<V4ConfigurationBit> AnalyzeBits(ArrayList<V4ConfigurationBit> Bits){
    	
    	//Does the attribute Corespond to a used slice? 
    	//is the Attribute used in the design?
		ArrayList<V4ConfigurationBit> RemainingBits = new ArrayList<V4ConfigurationBit>();
    	for(V4ConfigurationBit B: Bits) {
    		if (B != null) {
	    		if(B.getTile() != null){
	    			if(B.getTile().getType() == TileType.INT_SO){
	    				if (B.getFAR().getBlockType() == 0) {
	    					
	    					// NB: this if statement is V4 FX60 specific!!
	    					if (B.getFAR().getColumn() == 20 || B.getFAR().getColumn() == 39) {
	    						dspAttributeFailures++;
	    					} else {
	    						iobAttributeFailures++;
	    					}
	    				} else {
	    					BramAttributeFailures++; 
	    				}	    				 
	    			} else if (B.getTile().getType() == TileType.INT_SO_DCM0) {
	    				DCMMatches++;
	    			} else {
	    				B.setTile(XDLDesignUtils.getMatchingCLBTile(B.getTile(), design.getDevice()));       
	                    if(!(checkSliceBits(B, SliceBitsX0Y0) || checkSliceBits(B, SliceBitsX0Y1) || checkSliceBits(B, SliceBitsX1Y0) ||checkSliceBits(B, SliceBitsX1Y1))){
	                 	  RemainingBits.add(B);     
	                    }
	                 }	
	    		}	
	    		else {
	    			//ClockBits
	    			NullTiles++; 
	    			RemainingBits.add(B);
	    		}	   	 
	    	}
    	}
    	 
    	return RemainingBits;
    }
}

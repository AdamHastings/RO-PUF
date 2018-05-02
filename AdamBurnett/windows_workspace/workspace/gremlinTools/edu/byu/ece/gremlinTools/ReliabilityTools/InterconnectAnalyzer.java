package edu.byu.ece.gremlinTools.ReliabilityTools;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

import XDLDesign.Utils.XDLDesignUtils;

import edu.byu.ece.gremlinTools.BackArcs.InterTileReverseArcs;
import edu.byu.ece.gremlinTools.Virtex4Bits.Bit;
import edu.byu.ece.gremlinTools.Virtex4Bits.Pair;
import edu.byu.ece.gremlinTools.Virtex4Bits.Pip;
import edu.byu.ece.gremlinTools.Virtex4Bits.V4ConfigurationBit;
import edu.byu.ece.gremlinTools.Virtex4Bits.Virtex4SwitchBox;
import edu.byu.ece.gremlinTools.WireMux.WireMux;
import edu.byu.ece.gremlinTools.bitstreamDatabase.BitstreamDB;
import edu.byu.ece.gremlinTools.xilinxToolbox.V4XilinxToolbox;
import edu.byu.ece.rapidSmith.bitstreamTools.configuration.FPGA;
import edu.byu.ece.rapidSmith.design.Design;
import edu.byu.ece.rapidSmith.design.Net;
import edu.byu.ece.rapidSmith.design.PIP;
import edu.byu.ece.rapidSmith.device.Tile;
import edu.byu.ece.rapidSmith.device.TileType;
import edu.byu.ece.rapidSmith.device.WireConnection;
import edu.byu.ece.rapidSmith.device.WireEnumerator;
import edu.byu.ece.rapidSmith.device.WireType;

public class InterconnectAnalyzer {
    
	private Design design; 
	private FPGA fpga;
    private V4XilinxToolbox TB; 
    private Virtex4SwitchBox SB; 
    private InterTileReverseArcs RevArcs;
    private HashMap<Tile, ArrayList<Net>> TilesToNets; 
    private boolean AnalysisComplete; 
    private ArrayList<V4ConfigurationBit> FlaggedForFurtherAnalysisList;
    BitstreamDB DB; 
    
    private int TotalBitsAnalyzed; 
    private int Open; 
    private int RowShorts; 
    private int ColumnShorts; 
    private int RowBufferShorts;
    private int ColumnBufferShorts; 
    private int PossibleBridgingBufferShorts;
    private int PossibleBridgingColumnShorts; 
    private int PossibleHalfLatch;
    private int LongLineProblem;
    private int FlaggedForFurtherAnalysis;
    private int ClockColumnErrors; 
    private int FurtherAnalysisColumnBit;
    private int FurtherAnalysisRowBit;
    private int FurtherAnalysisUnusedColumn;
    private int FurtherAnalysisUsedColumn;
    private int FurtherAnalysisLongLines;
    private int FurtherAnalysisColumnLongLine;
    private ArrayList<TileType> TerminatingTilesTypes; 
    
    private HashSet<String> ColumnBitWires; 
    private HashSet<String> RowBitWires; 
    
    private HashMap<V4ConfigurationBit, String> BitsToNetNameHashMap;
    private HashMap<PIP, Net> PIPtoNetMap;
    //private ArrayList<Pair<V4ConfigurationBit, String>> BitsToNetNames;  
    private ArrayList<InterconnectFailure> InterconnectFailures; 
    protected HashMap<V4ConfigurationBit, String> _netNameHash;
    public InterconnectAnalyzer(Design d, FPGA f, V4XilinxToolbox t){
    	design = d; 
    	fpga = f;
    	TB = t; 
    	SB = new Virtex4SwitchBox(fpga, design, TB); 
    	RevArcs = new InterTileReverseArcs(d.getDevice(), d.getWireEnumerator());
    	
    	TilesToNets = new HashMap<Tile,ArrayList<Net>>();
    	BitsToNetNameHashMap = new HashMap<V4ConfigurationBit, String>();
    	PIPtoNetMap = new HashMap<PIP, Net>();
    	//BitsToNetNames = new ArrayList<Pair<V4ConfigurationBit, String>>(); 
    	InterconnectFailures = new ArrayList<InterconnectFailure>(); 
    	FlaggedForFurtherAnalysisList = new ArrayList<V4ConfigurationBit>();
    	
    	_netNameHash = new HashMap<V4ConfigurationBit, String>();
    	createTileToNetMap(); 
    	AnalysisComplete = false; 
    	
    	DB = new BitstreamDB(); 
    	DB.openConnection();
    	TotalBitsAnalyzed=0;
    	Open = 0; 
    	RowShorts = 0; 
        ColumnShorts = 0; 
        RowBufferShorts = 0;
        ColumnBufferShorts = 0; 
        PossibleBridgingBufferShorts = 0;
        PossibleBridgingColumnShorts = 0;
        PossibleHalfLatch = 0; 
        LongLineProblem = 0;
        FlaggedForFurtherAnalysis = 0;
        ClockColumnErrors = 0; 
        FurtherAnalysisColumnBit = 0;
        FurtherAnalysisRowBit = 0;
        FurtherAnalysisUnusedColumn = 0;
		FurtherAnalysisUsedColumn  = 0;
		FurtherAnalysisLongLines   = 0;
		FurtherAnalysisColumnLongLine = 0;
		
		TerminatingTilesTypes = new ArrayList<TileType>();
		TerminatingTilesTypes.add(TileType.B_TERM_DSP);
		TerminatingTilesTypes.add(TileType.B_TERM_INT);
		TerminatingTilesTypes.add(TileType.B_TERM_INT_D);
		TerminatingTilesTypes.add(TileType.B_TERM_INT_NOUTURN);
		TerminatingTilesTypes.add(TileType.B_TERM_INT_SLV);
		TerminatingTilesTypes.add(TileType.L_TERM_INT);
		TerminatingTilesTypes.add(TileType.L_TERM_PPC);
		TerminatingTilesTypes.add(TileType.L_TERM_PPC_EXT);
		TerminatingTilesTypes.add(TileType.R_TERM_INT);
		TerminatingTilesTypes.add(TileType.R_TERM_INT_D);
		TerminatingTilesTypes.add(TileType.R_TERM_INT_GTX);
		TerminatingTilesTypes.add(TileType.T_TERM_DSP);
		TerminatingTilesTypes.add(TileType.T_TERM_INT);
		TerminatingTilesTypes.add(TileType.T_TERM_INT_D);
		TerminatingTilesTypes.add(TileType.T_TERM_INT_NOUTURN);
		TerminatingTilesTypes.add(TileType.T_TERM_INT_SLV);
 	     
		//Start and End wires of Bi-directional PIPs may not be correct.
		XDLDesignUtils.verifyBidirectionalPIPs(design, true);
    }
    
   /* private void createPipToNetHashMap(){
    	for(Net N:design.getNets()){
    		for(PIP P : N.getPIPs()){
    			PIPtoNetMap.put(P, N);
    		}
    	}
    }*/
    public ArrayList<InterconnectFailure> getInterconnectFailures(){
    	return InterconnectFailures; 
    }
    
    private void createTileToNetMap() {
		ArrayList<Net> NetList = null; 
    	
    	for(Net N : design.getNets()){
			for(PIP P : N.getPIPs()){
				//See if the ArrayList Already Exists
				if((NetList = TilesToNets.get(P.getTile())) == null){
					NetList = new ArrayList<Net>();
					TilesToNets.put(P.getTile(), NetList);
				}
				//Add the Net to the ArrayList
				NetList.add(N);
			
			}
		}
	}
   
    
    private PIP getPIPUsingWire(Net N, int wire, Tile T){
    	if(N != null){
	    	for(PIP P : N.getPIPs()){
	    		if((P.getStartWire() == wire || P.getEndWire() == wire) && P.getTile() == T){
		    		return P; 
	    		}
	    	}
    	}
    	return null;
    }
    
    private boolean isWireUsedAsEndWire(int wire, Tile T){
          ArrayList<Net> Nets = TilesToNets.get(T);
	      if(Nets != null){
	          for(Net N: Nets){
	        	  for(PIP P: N.getPIPs()){
	        		  if(P.getTile().equals(T) && P.getEndWire() == wire){
	        			  return true; 
	        		  }
	        	  }
	          }
	      }
	      return false;
	      
    }
    
    private boolean isWireUsedAsStartWire(int wire, Tile T){
        ArrayList<Net> Nets = TilesToNets.get(T);
	    if(Nets != null){
	        for(Net N: Nets){
	      	  for(PIP P: N.getPIPs()){
	      		  if(P.getTile().equals(T) && P.getStartWire() == wire){
	      			  return true; 
	      		  }
	      	  }
	        }
	    }
        return false; 
  }
    private PIP getPIPUsingWireByStartWire(Net N, int wire, Tile T){
    	if(N != null){
	    	for(PIP P : N.getPIPs()){
	    		if(P.getStartWire() == wire && P.getTile() == T){
		    		return P; 
	    		}
	    	}
    	}
    	return null; 
    }
    
    private PIP getPIPUsingWireByEndWire(Net N, int wire, Tile T){
    	if(N != null){
	    	for(PIP P : N.getPIPs()){
	    		if(P.getEndWire() == wire && P.getTile() == T){
	    			return P; 
	    		}
	    	}
    	}
    	return null; 
    }
    
    private boolean isWireUsed(int wire, Tile T){
    	if(getNetUsingWire(wire, T) != null){
    		return true;
    	}
    	
    	return false; 
    }
    
    private Net getNetUsingWire(int wire, Tile T){
    	ArrayList<Net> Nets = null; 
    	
    	if((Nets = TilesToNets.get(T))== null){
    		return null;
    	} 
    	
    	for(Net N : Nets){
    		if(getPIPUsingWire(N, wire, T) != null){
    			return N; 
    		}
    	}
    	
    	return null; 
    }
    
    public HashMap<V4ConfigurationBit, String> getNetNameHash() {
    	return _netNameHash;
    }
    
    public void writeInterconnectFailureReport(String Filename){
    	try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(Filename));
			
			for(InterconnectFailure I: InterconnectFailures){
				bw.write(I.toString(design.getWireEnumerator()));
			}
			bw.close(); 
			//for(Pair<V4ConfigurationBit, String> P : BitsToNetNames){
			//	bw.write(P.getLeft().getFAR().getHexAddress() + " " + P.getLeft().getBitNumber() + " " + P.getRight() + "\n");
			//}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
   
      
    public void writeNathanReport(String Filename){
    	try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(Filename));
			
			for(InterconnectFailure I: InterconnectFailures){
				bw.write(I.getSensitiveBit().getFAR().getHexAddress() + " " + I.getSensitiveBit().getBitNumber() + " " + I.getNet().getName());
			}
			//for(Pair<V4ConfigurationBit, String> P : BitsToNetNames){
			//	bw.write(P.getLeft().getFAR().getHexAddress() + " " + P.getLeft().getBitNumber() + " " + P.getRight() + "\n");
			//}
			bw.close(); 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
//    public ArrayList<V4ConfigurationBit> AnalyzeFlaggedForFurtherAnalysisList(){
//    	ArrayList<V4ConfigurationBit> temp = new ArrayList<V4ConfigurationBit>(); 
//    	
//    	for(V4ConfigurationBit B: FlaggedForFurtherAnalysisList){
//    		B.setBitNumber(B.getBitNumber()-1);
//    		temp.add(B);
//    	}
//    	
//    	return AnalyzeBits(temp);
//    	
//    }
    
    
    public ArrayList<LEON3Result> AnalyzeInterconnectBits(TreeMap<LEON3Result, V4ConfigurationBit> xrtcHash) {
    	ArrayList<LEON3Result> retval = new ArrayList<LEON3Result>();
    	
    	for (LEON3Result xrtc : xrtcHash.keySet()) {
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
    	V4ConfigurationBit NonRoutingBit = null;
    	
    	WireEnumerator we = design.getWireEnumerator();
    	
    	sbit.setWireEnumerator(we);
    	
    	if (B != null) {
    		WireMux CurrentMux = null;
    		//Does this bit Belong to the Routing?
    		if(B.getTile() == null) {
    			sbit.setFailType(SensitiveBit.CLOCK_COLUMN_ERROR);
    			ClockColumnErrors++;
    		} else if((CurrentMux = SB.getWireMux(B)) != null ) {
    			TotalBitsAnalyzed++;
    			//Get the Interconnect Tile To Which This Bit Belongs
    			Tile T = B.getTile();// TB.getIntTile(fpga.getDeviceSpecification(), B.getFAR(), B, design.getDevice());
    			//Find the Wire The is being drive by the Switch to which this bit belongs
    			int wire = we.getWireEnum(CurrentMux.getDestName());
	    	    InterconnectFailure IF = new InterconnectFailure();
	    	    IF.setSensitiveBit(B);
	    	    	 
	    	    //If the wire is used as an EndWire that means that there is a used PIP that matches the WireMux Object 
	    	    if(isWireUsedAsEndWire(wire,T)) {
    			
	    	    	//Get Net and PIP
    				Net N = getNetUsingWire(wire, T);
    				PIP P = getPIPUsingWireByEndWire(N, wire, T);
	    	    	     
		    		//Set Inconnect Failure Object
		    		IF.setNet(N);
		    		IF.setPIP(P);
		    		IF.setSEUConnection(findSEUConnection(P, B, CurrentMux));
	    	    		 
	    	    	//Nathans HashMap
    				_netNameHash.put(B, N.getName());
    				sbit.setNet(N);
    				sbit.setPIP(P);
    				
    				//if(we.getWireName(P.getEndWire()).startsWith("L") ){
    				//	 LongLineProblem++; 
    				// }else
    				if(P.getStartWire() == we.getWireEnum("KEEP1_WIRE")) {
    	    			sbit.setFailType(SensitiveBit.POSSIBLE_HALF_LATCH);
    					PossibleHalfLatch++;
	    	    		IF.setInterconnectFailureType(InterconnectFailureType.HALFLATCH);
	    	    			 //Eventually Need to Set Half Latch Interconnect Failures
	    	    	} else if(isOpenFailure(P, CurrentMux, B, we)){
    					//Classify as Open 
	    	    		IF.setInterconnectFailureType(InterconnectFailureType.OPEN);
    	    			sbit.setFailType(SensitiveBit.OPEN);
    					Open++; 
    				} else if (isRowBit(B, CurrentMux)) {
    					//This could be a row short or a buffer short    	
    					//Get the Pip the corresponds to the used route
    					Pip muxPip = CurrentMux.getTable().getPip(P.getStartWire());
    					PIP p = traceRowBit(B, muxPip.getColumnBit(), CurrentMux, T);
	    	    		IF.setRowOrColumnBit(BitType.ROW);
    					if(p == null) {
    						//Classify as Buffer Short
    		    			sbit.setFailType(SensitiveBit.ROW_BUFFER_SHORT);    						
	    	    			IF.setInterconnectFailureType(InterconnectFailureType.BUFFER);
    						RowBufferShorts++;
    					} else {
    						//Classify as a Row Short 
	    	    			IF.setInterconnectFailureType(InterconnectFailureType.BRIDGEING);
	    	    			IF.setConnectionFromOtherNet(p);
    		    			sbit.setFailType(SensitiveBit.ROW_SHORT);    						
    						RowShorts++;
    					}
    				} else if (isColumnBit(B, CurrentMux)) {
    					//This could be a column short, buffer short or a brigding column short
    					Pip muxPip = CurrentMux.getTable().getPip(P.getStartWire());
    					PIP p = traceColumnBit(muxPip.getRowBit(), B, CurrentMux, T);
	    	    		IF.setRowOrColumnBit(BitType.COLUMN);
    					if(p == null){
    						//Buffer short
    		    			sbit.setFailType(SensitiveBit.COLUMN_BUFFER_SHORT);    						
    						ColumnBufferShorts++; 
	    	    			IF.setInterconnectFailureType(InterconnectFailureType.BUFFER);
    					} else {
    						//Column Short
	    	    			IF.setInterconnectFailureType(InterconnectFailureType.BRIDGEING);
	    	    			IF.setConnectionFromOtherNet(p);
    		    			sbit.setFailType(SensitiveBit.COLUMN_SHORT);
    						ColumnShorts++;
    					}
    					
    					findBridgingColumnShorts(B, muxPip, CurrentMux, T);
    				}
    				
	    	    		 InterconnectFailures.add(IF); //Only Add Failures we have Identified
    				} else {
    							
	    	    		 //An SEU that activates an unused long line buffer may cause a failure. 
	    	    		 if(CurrentMux.isBufferBit(B) && we.getWireType(CurrentMux.getTable().getDestWire()).equals(WireType.LONG)){
	    	    			//For Example: LV0 -> N6BEG4
	    	    		    //In this case LV0 is an entrance into the slice. This means that LV0's buffer is off. 
	    	    			//If the buffer is turned on by an SEU it will likely cause a failure. 
	    	    			if(isWireUsedAsStartWire(wire, T)){
	    	    				//Get Net and PIP using Wire
	    	    				Net N = getNetUsingWire(wire, T);
	    	    				PIP P = getPIPUsingWireByStartWire(N, wire, T);
	    	    				IF.setNet(N);
	    	    				IF.setPIP(P);
	    	    				IF.setRowOrColumnBit(BitType.LONGLINEBUFFER);
	    	    				IF.setInterconnectFailureType(InterconnectFailureType.LONGLINEBUFFER);
	    	    				sbit.setFailType(SensitiveBit.LONGLINE_SHORT);
	    	    				
	    	    			} else {
	    	    				 ArrayList<WireConnection> StartPoint = RevArcs.getBackArc(T, wire);
		                         if(StartPoint != null){
		    	    				 for(WireConnection wc : StartPoint){
		                            	 if(isWireUsedAsEndWire(wc.getWire(), wc.getTile(T))){
		                            		IF.setRowOrColumnBit(BitType.LONGLINEBUFFER); 
		                            		IF.setInterconnectFailureType(InterconnectFailureType.LONGLINEBUFFER);
		    	    	    				sbit.setFailType(SensitiveBit.LONGLINE_SHORT);
		                            	 }	
		    	    				 }
		                         }
	    	    			 }
	    	    		} else {
	    	    			 
	    	    			FlaggedForFurtherAnalysis++;
	    	    			FlaggedForFurtherAnalysisList.add(B);
	    	    		 
	    	    			boolean foundMatch = false; 
	    	    			if(isColumnBit(B, CurrentMux)) {
	    	    				FurtherAnalysisColumnBit++;
	    	    				foundMatch = false;
	    	    			 
	    	    				if(CurrentMux.getDestName().startsWith("L")) {
	    	    					sbit.setFailType(SensitiveBit.FURTHER_ANALYSIS_COLUMN_LONGLINE);    					
	    	    					FurtherAnalysisColumnLongLine++; 
	        	    			
	    	    				} else {
	    	    					for(Pip P : CurrentMux.getTable().getColumn(B)) {
		        	    		 
	    	    						PIP p = traceColumnBit(P.getRowBit(), B, CurrentMux, T);
	    	    						if(p != null) {
	    	    							//Unused
	    	    							//System.out.println("Used Column");
	    	    							sbit.setFailType(SensitiveBit.FURTHER_ANALYSIS_USED_COLUMN);    					
	    	    							FurtherAnalysisUsedColumn++;
	    	    							foundMatch = true; 
	    	    							break;
	    	    						} 
	    	    					}
	    	    					if(!foundMatch) {
	    	    						sbit.setFailType(SensitiveBit.FURTHER_ANALYSIS_UNUSED_COLUMN);    					    							
	    	    						FurtherAnalysisUnusedColumn++;
	    	    						//	System.out.println("Unused Column");
	    	    					}
	    	    				}
	    	    			} else if(isRowBit(B, CurrentMux)) {
	    	    				FurtherAnalysisRowBit++;
	    	    				if(CurrentMux.getDestName().startsWith("L")) {
	    	    					sbit.setFailType(SensitiveBit.FURTHER_ANALYSIS_ROW_LONGLINE);    					    						
	    	    					FurtherAnalysisLongLines++;
	    	    				}
	    	    				if(!foundMatch)
	    	    					sbit.setFailType(SensitiveBit.FURTHER_ANALYSIS_ROW_UNUSED_COLUMN);    					    						
	    	    					FurtherAnalysisUnusedColumn++;
	    	    			} else if(isRowBit(B, CurrentMux)) {
	    	    				FurtherAnalysisRowBit++;
	    	    				if(CurrentMux.getDestName().startsWith("L")) {
	    	    					sbit.setFailType(SensitiveBit.FURTHER_ANALYSIS_ROW_LONGLINE);    					    						
	    	    					FurtherAnalysisLongLines++;
	    	    				}
	    	    			}
	    	    		}
	    	    	 }
    	    	} else {
    	    		//System.out.println(B + "is not a routing bit.");
    	    		NonRoutingBit = B;
    	    	}	
    	  	}
    	AnalysisComplete = true; 
    	return NonRoutingBit; 
    }
    
	public ArrayList<V4ConfigurationBit> AnalyzeBits(Collection<V4ConfigurationBit> bits){
    	ArrayList<V4ConfigurationBit> NonRoutingBits = new ArrayList<V4ConfigurationBit>();
		 
    	WireEnumerator we = design.getWireEnumerator();
    	
    	for(V4ConfigurationBit B: bits) {
    		if (B != null) {
	    		WireMux CurrentMux = null;
	    		//Does this bit Belong to the Routing?
	    		if(B.getTile() == null){
	    			ClockColumnErrors++;
	    		}else if((CurrentMux = SB.getWireMux(B)) != null ){
	    	    	TotalBitsAnalyzed++;
	    	    	 //Get the Interconnect Tile To Which This Bit Belongs
	    	    	 Tile T = B.getTile();// TB.getIntTile(fpga.getDeviceSpecification(), B.getFAR(), B, design.getDevice());
	    	    	 //Find the Wire The is being drive by the Switch to which this bit belongs
	    	    	 int wire = we.getWireEnum(CurrentMux.getDestName());
	    	    	 InterconnectFailure IF = new InterconnectFailure();
	    	    	 IF.setSensitiveBit(B);
	    	    	 
	    	    	 //If the wire is used as an EndWire that means that there is a used PIP that matches the WireMux Object 
	    	    	 if(isWireUsedAsEndWire(wire,T)){
	    	    		 
	    	    		 //Get Net and PIP
	    	    		 Net N = getNetUsingWire(wire, T);
	    	    		 PIP P = getPIPUsingWireByEndWire(N, wire, T);   	    		
	    	    	     
	    	    		 //Set Inconnect Failure Object
	    	    		 IF.setNet(N);
	    	    		 IF.setPIP(P);
	    	    		 IF.setSEUConnection(findSEUConnection(P, B, CurrentMux));
	    	    		 
	    	    		 //Nathans HashMap
	    	    		 _netNameHash.put(B, N.getName());
	    	    		 
	    	    		 if(P.getStartWire() == we.getWireEnum("KEEP1_WIRE")){
	    	    			 PossibleHalfLatch++;
	    	    			 IF.setInterconnectFailureType(InterconnectFailureType.HALFLATCH);
	    	    			 //Eventually Need to Set Half Latch Interconnect Failures
	    	    		 } else if(isOpenFailure(P, CurrentMux, B, we)){
	    	    			 //Classify as Open 
	    	    			 IF.setInterconnectFailureType(InterconnectFailureType.OPEN);
	    	    			 Open++; 
	    	    		 } else if (isRowBit(B, CurrentMux)){
	    	    			 //This could be a row short or a buffer short    	
	    	    			 //Get the Pip the corresponds to the used route
	    	    			 Pip muxPip = CurrentMux.getTable().getPip(P.getStartWire());
	    	    			 PIP p = traceRowBit(B, muxPip.getColumnBit(), CurrentMux, T);
	    	    			 IF.setRowOrColumnBit(BitType.ROW);
	    	    			 if(p == null){
	    	    				 //Classify as Buffer Short
	    	    				 IF.setInterconnectFailureType(InterconnectFailureType.BUFFER);
	    	    				 RowBufferShorts++;
	    	    			 } else {
	    	    				 //Classify as a Row Short 
	    	    				 IF.setInterconnectFailureType(InterconnectFailureType.BRIDGEING);
	    	    				 IF.setConnectionFromOtherNet(p);
	    	    				 RowShorts++;
	    	    			 }
	    	    		 } else if (isColumnBit(B, CurrentMux)){
	    	    			 //This could be a column short, buffer short or a brigding column short
	    	    			 Pip muxPip = CurrentMux.getTable().getPip(P.getStartWire());
	    	    			 PIP p = traceColumnBit(muxPip.getRowBit(), B, CurrentMux, T);
	    	    			 IF.setRowOrColumnBit(BitType.COLUMN);
	    	    			 if(p == null){
	    	    				 //Buffer short
	    	    				 ColumnBufferShorts++; 
	    	    				 IF.setInterconnectFailureType(InterconnectFailureType.BUFFER);
	    	    			 } else {
	    	    				 //Column Short
	    	    				 IF.setInterconnectFailureType(InterconnectFailureType.BRIDGEING);
	    	    				 IF.setConnectionFromOtherNet(p);
	    	    				 ColumnShorts++;
	    	    			 }
	    	    			 
	    	    			 findBridgingColumnShorts(B, muxPip, CurrentMux, T);
	    	    		 }
	    	    		 
	    	    		 InterconnectFailures.add(IF); //Only Add Failures we have Identified
	    	    	 } else {
	    	    		 
	    	    		 //An SEU that activates an unused long line buffer may cause a failure. 
	    	    		 if(CurrentMux.isBufferBit(B) && we.getWireType(CurrentMux.getTable().getDestWire()).equals(WireType.LONG)){
	    	    			//For Example: LV0 -> N6BEG4
	    	    		    //In this case LV0 is an entrance into the slice. This means that LV0's buffer is off. 
	    	    			//If the buffer is turned on by an SEU it will likely cause a failure. 
	    	    			if(isWireUsedAsStartWire(wire, T)){
	    	    				//Get Net and PIP using Wire
	    	    				Net N = getNetUsingWire(wire, T);
	    	    				PIP P = getPIPUsingWireByStartWire(N, wire, T);
	    	    				IF.setNet(N);
	    	    				IF.setPIP(P);
	    	    				IF.setRowOrColumnBit(BitType.LONGLINEBUFFER);
	    	    				IF.setInterconnectFailureType(InterconnectFailureType.LONGLINEBUFFER);
		    	    			
	    	    			} else {
	    	    				 ArrayList<WireConnection> StartPoint = RevArcs.getBackArc(T, wire);
		                         if(StartPoint != null){
		    	    				 for(WireConnection wc : StartPoint){
		                            	 if(isWireUsedAsEndWire(wc.getWire(), wc.getTile(T))){
		                            		IF.setRowOrColumnBit(BitType.LONGLINEBUFFER); 
		                            		IF.setInterconnectFailureType(InterconnectFailureType.LONGLINEBUFFER);
		                            	 }
		                             }
		                         }
	    	    			}
	    	    		 } else {
	    	    		 
	    	    		  FlaggedForFurtherAnalysis++;
	    	    		  FlaggedForFurtherAnalysisList.add(B);
	    	    		 
	    	    		  boolean foundMatch = false; 
	    	    		  if(isColumnBit(B, CurrentMux)){
	    	    			 FurtherAnalysisColumnBit++;
	    	    			 foundMatch = false;
	    	    			 
	    	    			 if(CurrentMux.getDestName().startsWith("L")){
	        	    			 FurtherAnalysisColumnLongLine++; 
	        	    			
	        	    		 }else{
		        	    			 for(Pip P : CurrentMux.getTable().getColumn(B)){
		        	    		 
		    	    				 PIP p = traceColumnBit(P.getRowBit(), B, CurrentMux, T);
		    	    				 if(p != null){
		    	    					 //Unused
		    	    					 //System.out.println("Used Column");
		    	    					 FurtherAnalysisUsedColumn++;
		    	    					 foundMatch = true; 
		    	    					 break;
		    	    				 } 
		    	    			 }
		    	    			  if(!foundMatch) {
		    	    				 FurtherAnalysisUnusedColumn++;
		    	    				 //System.out.println("Unused Column");
		    	    			  }
	        	    		 }
	    	    		 } else if(isRowBit(B, CurrentMux)){
	    	    			 FurtherAnalysisRowBit++;
	    	    			 if(CurrentMux.getDestName().startsWith("L")){
	    	    				 FurtherAnalysisLongLines++;
	    	    			 }
	    	    			  if(!foundMatch)
	    	    				 FurtherAnalysisUnusedColumn++;
        	    		 } else if(isRowBit(B, CurrentMux)){
    	    			 FurtherAnalysisRowBit++;
    	    			 if(CurrentMux.getDestName().startsWith("L")){
    	    				 FurtherAnalysisLongLines++;
    	    			 }
    	    		 }
    	    	 }
	    	    	 }
    	    } else {
    	    	//System.out.println(B + "is not a routing bit.");
    	    	NonRoutingBits.add(B);
    	    }	
    	  }
    	}
    	AnalysisComplete = true; 
    	return NonRoutingBits; 
	}
	
	private PIP findSEUConnection(PIP p, V4ConfigurationBit b,	WireMux currentMux) {
		
		int rowOfOrig = currentMux.getTable().getRowNumber(p.getStartWire());
		int colOfOrig = currentMux.getTable().getColumnNumber(p.getStartWire());
		int rowOfSEU=-1;
		int colOfSEU=-1;
				
		if(isColumnBit(b, currentMux)){
		    //if the SEU is a column bit the row of the new connection will be the same as the original
			rowOfSEU = rowOfOrig; 
			colOfSEU = currentMux.getTable().getColumnNumber(b);
		} else if(isRowBit(b,currentMux)){  //row bit
            //if the SEU is a row bit then the columns are the same
			rowOfSEU = currentMux.getTable().getRowNumber(b);
			colOfSEU = colOfOrig; 
		}
		
		PIP seuConnection = null;
		
		if(rowOfSEU > -1 && colOfSEU > -1){
		  seuConnection = new PIP(p.getTile(), currentMux.getTable().getPip(rowOfSEU, colOfSEU).getPip().getStartWire(), p.getEndWire());
		}
		
		return seuConnection;
	}

	public void printReport(){
	        
        System.out.println("--------------------------------------------------------------");
        System.out.println("                Interconnect Analyzer Report                  ");
        System.out.println("--------------------------------------------------------------");
		System.out.println("Open: " + Open + "\n"
				          +"Total Shorts: " + (ColumnShorts + RowShorts) + "\n"
				          +"Row Shorts: " + RowShorts + "\n" 
				          +"Column Shorts: " + ColumnShorts + "\n" 
				          +"Total Buffer Shorts: " + (ColumnBufferShorts + RowBufferShorts)+ "\n"
				          +"Row Buffer Shorts: " + RowBufferShorts + "\n"
				          +"Column Buffer Shorts: " + ColumnBufferShorts + "\n" 
				          +"Possible Bridging Buffer Shorts: " + PossibleBridgingBufferShorts + "\n"
				          +"Possible Bridging Column Shorts: " + PossibleBridgingColumnShorts + "\n"
				          +"Sum of Analyzed Bits: " + (Open + ColumnShorts + RowShorts + ColumnBufferShorts + RowBufferShorts+LongLineProblem) + "\n"
				          +"Total Analyzed Bits: " + TotalBitsAnalyzed + "\n"
				          +"Possible Half Latches: " + PossibleHalfLatch + "\n"
				          +"LongLineProblem: " + LongLineProblem + "\n" 
				          +"Flagged For Further Analysis: " +FlaggedForFurtherAnalysis + "\n" 
				          +"Columns: " +FurtherAnalysisColumnBit + "\n"
				          +"FurtherAnalysisUnusedColumn: " +FurtherAnalysisUnusedColumn + "\n"
				          +"FurtherAnalysisUsedColumn: " +FurtherAnalysisUsedColumn + "\n"
				          +"FurtherAnalysisColumnLongLine: " +FurtherAnalysisColumnLongLine + "\n"
				          +"Rows: " +FurtherAnalysisRowBit+ "\n" 
				          +"FurtherAnalysisLongLines: " +FurtherAnalysisLongLines+ "\n" 
				          +"Clock Column Errors " + ClockColumnErrors );
		
	}

    private void findBridgingColumnShorts(Bit b, Pip muxPip, WireMux mux, Tile T) {
		Bit ColumnBit0 = b; 
		Bit ColumnBit1 = muxPip.getColumnBit();
		
		for(Bit RowBit : mux.getTable().getRowBits()){
			//For All Rows but the Row Designated by the Bit in muxPip
			if(RowBit.equals(muxPip.getRowBit()) == false){
				PIP P0 = traceColumnBit(RowBit, ColumnBit0, mux, T);
				PIP P1 = traceColumnBit(RowBit, ColumnBit1, mux, T);
				if(P0 == null && P1 != null || P0 != null && P1 == null){
					//Possible Bridging BufferShort
					PossibleBridgingBufferShorts++;
				} else if(P0 != null && P1 != null ){
					//Possible Bridging Column Short
					PossibleBridgingColumnShorts++;
				}
			}
		}
	}



	//Returns true if b is a column bit in mux. 
	private boolean isColumnBit(Bit b, WireMux mux) {
		
		for(Bit B:  mux.getTable().getColBits()){
		   if(B.equals(b)){
			   return true;
		   }
		}
		
		return false;
	}


	//Returns true if b is a row bit in mux.  
	private boolean isRowBit(Bit b, WireMux mux) {
		
		for(Bit B:  mux.getTable().getRowBits()){
			   if(B.equals(b)){
				   return true;
			   }
			}
		
		return false;
	}
    
	private Pip findColumnBitInRow(Bit rowBit, Bit columnBit, WireMux mux){
		for(Pip P: mux.getTable().getRow(rowBit)){
			if(P.getColumnBit().equals(columnBit)){
				return P;
			}
		}
		return null; 
	}
	
	private Pip findRowBitInColumn(Bit rowBit, Bit columnBit, WireMux mux){
		for(Pip P: mux.getTable().getColumn(columnBit)){
			if(P.getRowBit().equals(rowBit)){
				return P;
			}
		}
		return null; 
	}
	
	
    private PIP traceRowBit(Bit rowBit, Bit columnBit, WireMux mux, Tile T){
    	
    	Pip P  = findRowBitInColumn(rowBit, columnBit, mux);
    	int wire = P.getPip().getStartWire();    	
    	Net N = getNetUsingWire(wire, T);
    	PIP Result = getPIPUsingWire(N, wire, T);
    	
    	if(Result == null){
    		ArrayList<WireConnection> ba = RevArcs.getBackArc(T, wire); 
    		if(ba!= null){
    			for(WireConnection wc : ba){
    			 // Tile backTile = wc.getTile(T);
    			 	
    			  if(isTerminatingTile(wc.getTile(T))){
    				  //Find the new back arc
    				  for(PIP q :wc.getTile(T).getPIPs()){
    					  if(q.getEndWire() == wc.getWire()){
    						  ArrayList<WireConnection> ba2 = RevArcs.getBackArc(wc.getTile(T), q.getStartWire());
    						  //There should only be one back arc
    						  if(ba2.size() > 1){
    							  System.out.println("Warning: ba2 has more than one back arc.");
    	}
    						  wc = ba2.get(0);
    	
    					  }
    				  }
    			  }
    			  N = getNetUsingWire(wc.getWire(), wc.getTile(T));
    			  Result = getPIPUsingWire(N, wc.getWire(), wc.getTile(T));
    			  if(Result != null) break;
    			}
    		}
    	}
    	return Result;
    }

    private boolean isTerminatingTile(Tile backTile) {
    	for(TileType TT : TerminatingTilesTypes){
    		if(TT.equals(backTile.getType())){
    			return true; 
    		}
    	}
		return false;
	}

    private PIP traceColumnBit(Bit rowBit, Bit columnBit, WireMux mux, Tile T){
    	Pip P  = findColumnBitInRow(rowBit, columnBit, mux);
    	int wire = P.getPip().getStartWire();    	
    	Net N = getNetUsingWire(wire, T);
    	
    	PIP Result = getPIPUsingWire(N, wire, T);
    	
    	if(Result == null){
    		ArrayList<WireConnection> ba = RevArcs.getBackArc(T, wire); 
    		if(ba!= null){
    			for(WireConnection wc : ba){
    			  //Tile backTile = wc.getTile(T);
    			  if(isTerminatingTile(wc.getTile(T))){
    				  //Find the new back arc
    				  for(PIP q :wc.getTile(T).getPIPs()){
    					  if(q.getEndWire() == wc.getWire()){
    						  ArrayList<WireConnection> ba2 = RevArcs.getBackArc(wc.getTile(T), q.getStartWire());
    						  //There should only be one back arc
    						  if(ba2.size() > 1){
    							  System.out.println("Warning: ba2 has more than one back arc.");
    						  }
    						  wc = ba2.get(0);
    						  
    					  }
    				  }
    			  }
    			  N = getNetUsingWire(wc.getWire(), wc.getTile(T));
    			  Result = getPIPUsingWire(N, wc.getWire(), wc.getTile(T));
    			  if(Result != null){
    				  //System.out.println("");
    				  break;
    			  }
    			}
    		}
    	}
    	
    	return Result;
    }
    
	private boolean isOpenFailure(PIP p, WireMux mux, V4ConfigurationBit B, WireEnumerator we) {
		
		//if(p.getStartWire() == we.getWireEnum("KEEP1_WIRE")){
		//	return false; 
		//}
		//System.out.println(design.getWireEnumerator().getWireName(p.getEndWire()));
		//System.out.println(design.getWireEnumerator().getWireName(p.getStartWire()));
		Pip V4Pip = mux.getTable().getPip(p.getStartWire());
		if(V4Pip == null){	
			mux.getTable().printTable();
			System.out.println( p.toString(we));
			System.out.println("Minor Address: " + B.getMinorAddress() + " Byte Offset: " + B.getOffset() + " Bit Mask: " + B.getMask());
		}
		if(B.equals(V4Pip.getRowBit()) || B.equals(V4Pip.getColumnBit())){
			return true; 
		} else if(mux.getDestName().startsWith("L")){
			if(B.equals(V4Pip.getBufferBit())){
				return true;
			}
		}
				
		
		
		return false;
	}
	
	public static int LookUpQueryCountResults(BitstreamDB DB, String Query){
    	System.out.println(Query);
    	ResultSet rs = DB.makeQuery(Query);
        int numResults=0;
    	try {
			while(rs.next()){
				System.out.println(rs.getString(1) + " " + rs.getString(2) + " " + rs.getString(3) + " " + rs.getString(4) + " " + rs.getString(5) + " " + rs.getString(6) + " " + rs.getString(7) + " " + rs.getString(8));
				numResults++; 
				//System.out.println("Bits.add(new Bit(" +  rs.getString(5) + ", " + rs.getString(6) + ", " + rs.getString(7) + "));");
			}
			rs.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return numResults;
    }
	
	public static void main(String args[]){
	
		
		
		
	}
}

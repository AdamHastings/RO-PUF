package edu.byu.ece.gremlinTools.Virtex4Bits;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.TreeMap;

import com.csvreader.CsvReader;

import edu.byu.ece.rapidSmith.bitstreamTools.configuration.FPGA;
import edu.byu.ece.rapidSmith.design.Design;
import edu.byu.ece.rapidSmith.design.Net;
import edu.byu.ece.rapidSmith.design.PIP;
import edu.byu.ece.rapidSmith.device.Tile;
import edu.byu.ece.rapidSmith.device.TileType;
import edu.byu.ece.rapidSmith.device.WireConnection;
import edu.byu.ece.rapidSmith.router.Node;
import edu.byu.ece.gremlinTools.Virtex4Bits.V4ConfigurationBit; 
import edu.byu.ece.gremlinTools.WireMux.WireMux;
import edu.byu.ece.gremlinTools.xilinxToolbox.V4XilinxToolbox;

public class Virtex4SwitchBox {
     
	 private FPGA fpga; 
     private Design design; 
	 private Tile IntTile;
	 private Tile CLBTile; 
     private V4XilinxToolbox TB; 
     private HashMap<PIP,  ArrayList<V4ConfigurationBit>> BitCache; 
     private HashMap<V4ConfigurationBit, HashSet<PIP>> BitGroups; 
     
     private HashMap<Bit, WireMux> BitsToWireMux; 
	 //This is the Set of PIPs that 
     private HashMap<PIP, PIP> DesignPIPs; 
     private HashSet<PIP> ActivatedPIPs;
     
     private TreeMap<String, WireMux> Muxes;
     
     public Virtex4SwitchBox(FPGA fpga, Design design, V4XilinxToolbox TB){
    	 
    	 this.fpga = fpga;
    	 this.design = design; 
    	 IntTile = null;
    	 CLBTile = null; 
    	 this.TB = TB; 
    	 BitCache  = new HashMap<PIP,  ArrayList<V4ConfigurationBit>>(); 
         BitGroups = new HashMap<V4ConfigurationBit, HashSet<PIP>>(); 
    	 
         ActivatedPIPs = new HashSet<PIP>();
    	 DesignPIPs  = new HashMap<PIP, PIP>();
    	 Muxes = new TreeMap<String, WireMux>();
    	 
    	 BitsToWireMux = new HashMap<Bit,WireMux>(); 
    	 populateWireMuxes(); 
         createBitsToWireMuxMap(); 
     }
     
     private void createBitsToWireMuxMap() {
		  		
		for(WireMux M : getWireMuxList()){
			for(Bit B :M.getTable().getColBits()){
				BitsToWireMux.put(B, M);
			}
			
			for(Bit B :M.getTable().getRowBits()){
				BitsToWireMux.put(B, M);
			}
			
			if(M.getDestName().startsWith("L")){
				BitsToWireMux.put(M.getTable().getPip(0, 0).getBufferBit(), M);
			}
		}
	 }
    
    public WireMux getWireMux(Bit B){
    	return BitsToWireMux.get(B);
    }
  
	public ArrayList<WireMux> getWireMuxList(){
    	 ArrayList<WireMux> muxes = new ArrayList<WireMux>();
    	 for(String S: Muxes.keySet()){
    		 muxes.add(Muxes.get(S));
    	 }
    	 return muxes;
     }
     public TreeMap<String,WireMux> getMuxes(){
    	 return Muxes; 
     }
     
     public WireMux getWireMux(String wirename){
    	 return Muxes.get(wirename);
     }
     
     private void populateWireMuxes() {
        try { //Z:\\javaworkspace2\\gremlinTools\\
		CsvReader r = new CsvReader(new FileReader("C:\\Adam\\WireMuxTransposeInfo.txt"));
			//CsvReader r = new CsvReader(new FileReader("/home/nhr2/eclipsework/gremlinTools/SupportFiles/WireMuxTransposeInfo.txt"));
			while(r.readRecord())
			{
				String destMux = r.get(0);
				boolean transposed = Boolean.parseBoolean(r.get(1));
			
				WireMux WM = new WireMux(destMux);
				WM.PopulateTable(transposed);
				Muxes.put(destMux, WM);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 		
	}

	public void setTiles(Tile IntTile, Tile CLBTile){
    	 	 this.IntTile = IntTile; 
    		 this.CLBTile = CLBTile;     	 
     }
     
     public Tile getCLBTile(){
    	 return CLBTile; 
     }
     
     public Tile getIntTile(){
    	 return IntTile; 
     }
     
     public Design getDesign(){
    	 return design; 
     }
     
     public HashMap<PIP, PIP> getDesignPIPs(){
    	 return DesignPIPs; 
     }
     
     public V4ConfigurationBit [] getRowConfigurationBits(String WireMuxName){
    	 WireMux WM = Muxes.get(WireMuxName);
    	 return this.createConfigurationBitArray(WM.getTable().getRowBits()); 	 
     }
     
     public V4ConfigurationBit [] getColConfigurationBits(String WireMuxName){
    	 WireMux WM = Muxes.get(WireMuxName);
    	 return this.createConfigurationBitArray(WM.getTable().getColBits()); 	 
     }
     
     public V4ConfigurationBit [] createConfigurationBitArray(Bit [] Bits){
    	 int i = 0;
    	 V4ConfigurationBit [] CB = new V4ConfigurationBit [Bits.length];
    	 for(Bit B :Bits){
    		 CB[i++] = new V4ConfigurationBit(fpga, B, CLBTile, TB);
    	 }
    	 return CB;
     }
     public V4ConfigurationBit getRowConfigurationBit(String WireMuxName, String StartWire){
    	 WireMux WM = Muxes.get(WireMuxName);
    	 Bit RowBit = WM.getTable().getPip(design.getWireEnumerator().getWireEnum(StartWire)).getRowBit();
    	 return new V4ConfigurationBit(fpga, RowBit, CLBTile, TB);
     }
     
     public V4ConfigurationBit getColumnConfigurationBit(String WireMuxName, String StartWire){
    	 WireMux WM = Muxes.get(WireMuxName);
    	 Bit ColumnBit = WM.getTable().getPip(design.getWireEnumerator().getWireEnum(StartWire)).getColumnBit();
    	 return new V4ConfigurationBit(fpga, ColumnBit, CLBTile, TB);
     }
        
     public ArrayList<V4ConfigurationBit> getPIPBits(PIP P){
    	 
    	 //To Make the SwitchBox Tile Independent Do not Set Tile Value in PIP
    	 PIP NewPIP = new PIP();
    	 NewPIP.setStartWire(P.getStartWire());
    	 NewPIP.setEndWire(P.getEndWire());
    	  
    	 
    	 //Search for PIP
    	 if(BitCache.get(NewPIP) == null){
    	    
    		//We do not have PIP
    		//Lookup Bits in the DB
    		ArrayList<Bit> RawBits;
    		ArrayList<V4ConfigurationBit> V4PIPBits = new ArrayList<V4ConfigurationBit>(); 
    		RawBits = TB.LookUpPIPBits(NewPIP, design);
    		for(Bit B : RawBits){
    			V4ConfigurationBit BB = new V4ConfigurationBit(B);
    			BB.setFpga(fpga);
    			BB.setToolbox(TB);
    			V4PIPBits.add(BB);
    		}
    		
    		//Search Bit Groups
    		for(V4ConfigurationBit B: V4PIPBits){
    			HashSet<PIP> Pips = null; 
    			if((Pips=BitGroups.get(B)) == null){
    			  //Bit Group Does Not Exist
    			    //Create a new BitGroup
    				Pips = new HashSet<PIP>();
    				//Add PIP to BitGroup
    				Pips.add(NewPIP);
    				//Add BitGroup to BitGroups HashMap
    			    if(BitGroups.put(B, Pips) != null){
    			    	System.out.println("OverWrote a Value In BitGroups HashMap");
    			    	System.exit(1);
    			    }
    			} else {
    			  //Bit Group Does Exist
    			    //Add Pip to BitGroup
    				Pips.add(NewPIP);
    			}
    			
    		}
    		
    		if(this.BitCache.put(NewPIP, V4PIPBits) != null){
    			System.out.println("Overwrote PIP in SwitchBox HashMap.");
    		}
    	 }
    	 //We Have PIP.  Done.  
    	 
    	 ArrayList<V4ConfigurationBit> DesiredBits = BitCache.get(NewPIP); 
    	 for(V4ConfigurationBit B : DesiredBits){
    		 B.setFpga(fpga);
    		 B.setTile(P.getTile());
    		 B.setToolbox(TB);
    	 }
    	 
    	 return  DesiredBits; 
     }
     
     public void ActivatePIP(PIP P){
    	  	 
    	 for(V4ConfigurationBit B: getPIPBits(P)){
    	
    		 B.SetBitValueInBitStream(1, P.getTile());
    	 }
    	 
    	 ActivatedPIPs.add(P);
     }
     
     public void ActivatePIPs(ArrayList<PIP> Pips){
    	 for(PIP pip : Pips){
    		 ActivatePIP(pip);
    	 }
     }
     public void DeactivatePIPs(ArrayList<PIP> PIPS){
    	 for(PIP P: PIPS){
    		 DeactivatePIP(P);
    	 }
     }
     
     //This function will work pending the Design Has been verified first.
     public void DeactivatePIP(PIP P){
    	
    	//Look up the PIPs Bits 
    	ArrayList<V4ConfigurationBit> Bits = getPIPBits(P);
    	for(V4ConfigurationBit B: Bits){
    		//Does a Bit Group Exist
    		HashSet<PIP> BitGroupPIPs; 
    		if((BitGroupPIPs = BitGroups.get(B)) != null){
    		  //The Bit Group Exists
    		  for(PIP Q : BitGroupPIPs){
    			  //Since the PIPs in the Bit Groups are Generic We need to Set the Tile.
    			  Q.setTile(P.getTile());    
    			  if(!Q.equals(P) && (ActivatedPIPs.contains(Q) || DesignPIPs.containsKey(Q))){
    				 //This Bit is part of Another Net and Should be left on. 
    			  } else {
    				 //This Bit is not Part of another net and should be turned off.
    				  B.SetBitValueInBitStream(0, P.getTile());
    			  }
    		  }
    		}
    		 
    	 }
     }
     
     //This Function Verifies the PIP is Set to Value
     public boolean VerifyPIP(PIP P, int value){
    	 for(V4ConfigurationBit B: getPIPBits(P)){
    		 B.setTile(P.getTile());
    		 if(B.getValue() != value){
    			 System.out.println("B.getValue: " + B.getValue());
    			 System.out.println(P.toString(design.getWireEnumerator()));
    			 return false; 
    		 }
    	 }
    	 return true; 
     }
      
     
     
    
     
   
 	
}

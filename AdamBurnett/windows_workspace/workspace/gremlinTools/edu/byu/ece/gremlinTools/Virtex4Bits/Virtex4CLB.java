package edu.byu.ece.gremlinTools.Virtex4Bits;

import java.util.ArrayList;

import XDLDesign.Utils.XDLDesignUtils;

import edu.byu.ece.rapidSmith.bitstreamTools.configuration.FPGA;
import edu.byu.ece.rapidSmith.bitstreamTools.configuration.FrameAddressRegister;
import edu.byu.ece.rapidSmith.design.Design;
import edu.byu.ece.rapidSmith.design.Instance;
import edu.byu.ece.rapidSmith.design.ModuleInstance;
import edu.byu.ece.rapidSmith.design.Attribute;
import edu.byu.ece.rapidSmith.device.Tile;
import edu.byu.ece.gremlinTools.xilinxToolbox.V4XilinxToolbox;
import edu.byu.ece.gremlinTools.xilinxToolbox.XilinxToolboxLookup;

public class Virtex4CLB {
   
   private ArrayList<Virtex4Slice> SliceArray; 
   private V4XilinxToolbox TB; 
   private FPGA fpga; 
   private Design design; 
   private Tile CLBTile; 
   private Tile INTTile; 
   public Virtex4CLB(FPGA fpga, Design design, Tile CLBTile, V4XilinxToolbox TB) {
	  
	   SliceArray = new ArrayList<Virtex4Slice>(); 
	   this.TB = TB; 		
	   this.fpga = fpga; 
	   this.design = design; 
	   this.setCLBTile(CLBTile);
	   this.setINTTile(XDLDesignUtils.getMatchingIntTile(CLBTile, design.getDevice()));
	   for(int x=0; x<2; x++){
			for(int y=0; y<2; y++){
				SliceArray.add(new Virtex4Slice(x, y, fpga, design, CLBTile, TB));
			}
		}
		
	   
	}
	
    public ArrayList<Virtex4Slice> getSliceArrayList(){
    	return SliceArray; 
    }
   
	
	public Virtex4Slice getSLICEX0Y0() {
		return SliceArray.get(0);
	}
	
	
	public Virtex4Slice getSLICEX0Y1() {
		return SliceArray.get(1);
	}
	
	
	public Virtex4Slice getSLICEX1Y0() {
		return SliceArray.get(2);
	}
	
	
	public Virtex4Slice getSLICEX1Y1() {
		return SliceArray.get(3);
	}
	
	public ArrayList<Virtex4Lut> getLUTS(){
		ArrayList<Virtex4Lut> Luts = new ArrayList<Virtex4Lut>();
		
		Luts.add(this.getSLICEX0Y0().getLutF());
		Luts.add(this.getSLICEX0Y0().getLutG());
		Luts.add(this.getSLICEX0Y1().getLutF());
		Luts.add(this.getSLICEX0Y1().getLutG());
		Luts.add(this.getSLICEX1Y0().getLutF());
		Luts.add(this.getSLICEX1Y0().getLutG());
		Luts.add(this.getSLICEX1Y1().getLutF());
		Luts.add(this.getSLICEX1Y1().getLutG());
		
		return Luts;
	}
	public Virtex4Slice getSLICE(int x, int y){
		
		String YErrorMsg = "getSLICE: Invalid Y Selection.";
		String XErrorMsg = "getSLICE: Invalid X Selection.";
		
		switch(x){
		 case 0:
			 switch(y) {
			  case 0: return getSLICEX0Y0();  
			  case 1: return getSLICEX0Y1(); 
			  default: System.out.println(YErrorMsg); break;
			 } break;
		 case 1: 
			 switch(y) {
			  case 0: return getSLICEX1Y0();  
			  case 1: return getSLICEX1Y1(); 
			  default: System.out.println(YErrorMsg); break;
			 } break;
		 default: System.out.println(XErrorMsg); break; 
		}
		
		return null;
	}
	
	public ArrayList<V4ConfigurationBit> getFFInitConfigurationBits(FPGA fpga, ModuleInstance MI, V4XilinxToolbox TB) {
		
		ArrayList<V4ConfigurationBit> Bits = new ArrayList<V4ConfigurationBit>();
		
		for(Instance I : MI.getInstances()){
			Tile T = I.getTile(); 
			int x = Virtex4Slice.ConvertSliceX(I.getInstanceX());
 			int y = Virtex4Slice.ConvertSliceY(I.getInstanceY());
			if(I.getPrimitiveSiteName().startsWith("SLICE")){
				for(Attribute A : I.getAttributes()){
					if(A.getPhysicalName().equals("FFX_INIT_ATTR") && (A.getValue().equals("#OFF") == false)){
						Virtex4Slice S = this.getSLICE(x, y);
						Bits.add(new V4ConfigurationBit(fpga, S.getFFX().getInitBit(), T, TB));
					} else if(A.getPhysicalName().equals("FFY_INIT_ATTR") && (A.getValue().equals("#OFF") == false)){
						Virtex4Slice S = this.getSLICE(x, y);
						Bits.add(new V4ConfigurationBit(fpga, S.getFFY().getInitBit(), T, TB));
					}
				}
			}
		}
		return Bits; 
	}
	
	public ArrayList<V4ConfigurationBit> getLUTConfigurationBits(FPGA fpga, ModuleInstance MI, V4XilinxToolbox TB){
		
		ArrayList<V4ConfigurationBit> Bits = new ArrayList<V4ConfigurationBit>();
		for(Instance I: MI.getInstances()){
			int x = Virtex4Slice.ConvertSliceX(I.getInstanceX());
 			int y = Virtex4Slice.ConvertSliceY(I.getInstanceY());
			Tile T = I.getTile(); 
			if(I.getPrimitiveSiteName().startsWith("SLICE")){
	 			
				Virtex4Slice S = this.getSLICE(x, y);
				System.out.println(I.getPrimitiveSiteName());
				for(Bit B : S.getLutF().GetBits()){
				   Bits.add(new V4ConfigurationBit(fpga, B, T, TB));	
				}
				
				for(Bit B : S.getLutG().GetBits()){
					   Bits.add(new V4ConfigurationBit(fpga, B, T, TB));	
				}
			}
		}
		
		return Bits; 
	}

	public void setCLBTile(Tile cLBTile) {
		CLBTile = cLBTile;
	}

	public Tile getCLBTile() {
		return CLBTile;
	}

	public void setINTTile(Tile iNTTile) {
		INTTile = iNTTile;
	}

	public Tile getINTTile() {
		return INTTile;
	}
	
	
}

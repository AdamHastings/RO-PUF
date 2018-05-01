//package PowerMeasurementDesigns;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import XDLDesign.Utils.XDLDesignUtils;
import edu.byu.ece.gremlinTools.Virtex4Bits.Virtex4SliceBits;
import edu.byu.ece.gremlinTools.Virtex4Bits.Virtex4SwitchBox;
import edu.byu.ece.gremlinTools.xilinxToolbox.V4XilinxToolbox;
import edu.byu.ece.gremlinTools.xilinxToolbox.XilinxToolboxLookup;
import edu.byu.ece.rapidSmith.bitstreamTools.configuration.FPGA;
import edu.byu.ece.rapidSmith.bitstreamTools.configurationSpecification.DeviceLookup;
import edu.byu.ece.rapidSmith.design.Attribute;
import edu.byu.ece.rapidSmith.design.Design;
import edu.byu.ece.rapidSmith.design.Instance;
import edu.byu.ece.rapidSmith.design.PIP;
import edu.byu.ece.rapidSmith.device.PrimitiveType;
import edu.byu.ece.rapidSmith.device.Tile;
import edu.byu.ece.rapidSmith.device.WireEnumerator;
import edu.byu.ece.rapidSmith.util.FileConverter;

public class ShortInjector {
    public static void main(String [] args){
    	Design design = new Design();
    	
    	int num_dac_hard_macros;
    	
    	// C:\Users\Bobby\Desktop\SAFIRE\shorts1
    	FileConverter.convertNCD2XDL("E:\\Adam\\ISE\\short_generator_tutorial\\short_payload.ncd");
    	design.loadXDLFile("E:\\Adam\\ISE\\short_generator_tutorial\\short_payload.xdl");
    	
    	//Checking for the number of DAC hard macros
    	String hardMacroName = "DAC_STUB_GEN";
    	File file = new File("E:\\Adam\\ISE\\short_generator_tutorial\\short_payload.xdl");
    	int max = 0;
    	
    	try {
    		Scanner sc = new Scanner(file);
    		while (sc.hasNextLine())
    		{
    			String line = sc.nextLine();
    			if (line.contains("outpin") && line.contains(hardMacroName))
    			{
    				int number = Integer.parseInt(line.split("\\[")[1].split("\\]")[0]);
    				if (number > max)
    				{
    					max = number;
    				}
    			}
    		}
    		sc.close();
    	}
    	catch (FileNotFoundException e)
    	{
    		e.printStackTrace();
    	}
    	
    	num_dac_hard_macros = max+1;
    	
    	Design Template = new Design();
    	//SliceGui generated xdl file
    	Template.loadXDLFile("E:\\Adam\\xilinx\\HardMacroTemplate_ChaosPlanB.xdl");
    	
    	FPGA fpga = new FPGA(DeviceLookup.lookupPartV4V5V6("XC4VLX60"));
   	    
//    	XDLDesignUtils.loadBitstream(fpga, "C:\\Users\\danny\\Desktop\\thesis\\thesisDesigns\\ultimates\\shorts_256_dac\\top.bit");

//    	XDLDesignUtils.loadBitstream(fpga, "C:\\Users\\Bobby\\Desktop\\SAFIRE\\shorts1\\helloworld_top.bit");
    	XDLDesignUtils.loadBitstream(fpga, "E:\\Adam\\ISE\\short_generator_tutorial\\short_payload.bit");
   	    V4XilinxToolbox TB = (V4XilinxToolbox) XilinxToolboxLookup.toolboxLookup(fpga.getDeviceSpecification());
   	    
   	    Virtex4SliceBits SLICEX0Y0 = new Virtex4SliceBits(0, 0, PrimitiveType.SLICEM, fpga, design.getDevice(), TB);
   	    Virtex4SliceBits SLICEX0Y1 = new Virtex4SliceBits(0, 1, PrimitiveType.SLICEM, fpga, design.getDevice(), TB);
   	    Virtex4SliceBits SLICEX1Y0 = new Virtex4SliceBits(1, 0, PrimitiveType.SLICEL, fpga, design.getDevice(), TB);
   	    Virtex4SliceBits SLICEX1Y1 = new Virtex4SliceBits(1, 1, PrimitiveType.SLICEL, fpga, design.getDevice(), TB);
   	    
   	    Virtex4SwitchBox SB = new Virtex4SwitchBox(fpga, design, TB);
   	    
   	    System.out.println("Designs Loaded...");
   	    
   	    ArrayList<PIP> RemovePIPs =new ArrayList<PIP>();
   	    
   	    //DEACTIVATE PIPs
   	    SB.DeactivatePIPs(RemovePIPs);
   	    
   	    /*
		//VIEW AVAILABLE INSTANCES
   	    Set<String> instanceList = new HashSet<String>();
   	    instanceList = Template.getInstanceMap().keySet();
   
   	    if(instanceList == null)
   	    	System.out.println("There are no instances.");
   	    
   	    System.out.print("Writing Template Instance List file...");
   	    try{
   	    	FileWriter fwriter = new FileWriter("Z:\\users\\dsavory\\gremlin\\DAC\\powerShorts\\8bit\\fiveKhzWave\\templateInstanceList.txt");
   	    	BufferedWriter bwriter = new BufferedWriter(fwriter);

   	    	bwriter.write("Printing out instances associated with \"Template\"...\n\n");
   	    	int j = 0;
   	    	for(String s : instanceList){
   	    		bwriter.write("Instance" + j++ + ": " + s +"\n");
   	    	}
   	    	bwriter.close();
   	    }
   	    catch(IOException e){
   	    	System.out.println("There's something wrong with your filename.\n" +
   	    		"Or when you try to close.");
   	    }
   	    System.out.println("done.");
   
   	    //work on design instance file
   	    Set<String> designList = new HashSet<String>();
   	    designList = design.getInstanceMap().keySet();

   	    if(designList == null)
   	    	System.out.println("There are no instances for \"design\".");
   
   	    System.out.print("Writing Design Instance List file...");
   	    try{
   	    	FileWriter fwriter = new FileWriter("Z:\\users\\dsavory\\gremlin\\DAC\\powerShorts\\8bit\\fiveKhzWave\\designInstanceList.txt");
   	    	BufferedWriter bwriter = new BufferedWriter(fwriter);

   	    	bwriter.write("Printing out instances associated with \"Design\"...\n\n");
   	    	int j = 0;
   	    	for(String s : designList){
   	    		bwriter.write("Instance" + j++ + ": " + s +"\n");
   	    	}
   	    	bwriter.close();
   	    }
   	    catch(IOException e){
   	    	System.out.println("Something went wrong writing the file.");
   	    }
   	    System.out.println("done.");
   	    
   	    */
   	    System.out.println("Dummy PIPs removed...");
   	    //Setup SLICES
   	    for(int i = 0; i<num_dac_hard_macros; i++){
   	    	setupSlice(Template.getInstance("md5/clb_verify/SLICEMX0X0/$COMP_1"), design.getInstance("DAC_STUB_GEN["+ i + "].DAC_PART/$COMP_0"), SLICEX0Y0);   	  	
   		    setupSlice(Template.getInstance("md5/clb_verify/SLICEMX0Y1/$COMP_1"), design.getInstance("DAC_STUB_GEN["+ i + "].DAC_PART/$COMP_1"), SLICEX0Y1);   	  	
   		    setupSlice(Template.getInstance("md5/clb_verify/SLICELX1Y0/$COMP_0"), design.getInstance("DAC_STUB_GEN["+ i + "].DAC_PART/$COMP_2"), SLICEX1Y0);   	  	
   		    setupSlice(Template.getInstance("md5/clb_verify/SLICELX1Y1/$COMP_0"), design.getInstance("DAC_STUB_GEN["+ i + "].DAC_PART/$COMP_3"), SLICEX1Y1);
//   	    	setupSlice(Template.getInstance("md5/clb_verify/SLICEMX0X0/$COMP_1"), design.getInstance("THEDAC/DAC_STUB_GEN["+ i + "].DAC_PART_1/$COMP_0"), SLICEX0Y0);   	  	
//   		    setupSlice(Template.getInstance("md5/clb_verify/SLICEMX0Y1/$COMP_1"), design.getInstance("THEDAC/DAC_STUB_GEN["+ i + "].DAC_PART_1/$COMP_1"), SLICEX0Y1);   	  	
//   		    setupSlice(Template.getInstance("md5/clb_verify/SLICELX1Y0/$COMP_0"), design.getInstance("THEDAC/DAC_STUB_GEN["+ i + "].DAC_PART_1/$COMP_2"), SLICEX1Y0);   	  	
//   		    setupSlice(Template.getInstance("md5/clb_verify/SLICELX1Y1/$COMP_0"), design.getInstance("THEDAC/DAC_STUB_GEN["+ i + "].DAC_PART_1/$COMP_3"), SLICEX1Y1);
   	    }
   	    
	    System.out.println("Slices Setup...");
	    //Add Shorts
     
	    
  	//INT Tiles
	ArrayList<Tile> tileList = new ArrayList<Tile>();
	for(int i = 0; i<num_dac_hard_macros; i++){
		// Finding instances of your hard macro in the design.
		tileList.add(XDLDesignUtils.getMatchingIntTile(design.getInstance("DAC_STUB_GEN["+ i + "].DAC_PART/$COMP_0").getTile(),design.getDevice()));
//		tileList.add(XDLDesignUtils.getMatchingIntTile(design.getInstance("shortTile1/$COMP_0").getTile(),design.getDevice()));
	}
    //PIPs
  	WireEnumerator We = design.getWireEnumerator();
  	System.out.println("Modifying Bitstream...");
  	
  	//DAC BIT[0]
  	ArrayList<PIP> pipList = new ArrayList<PIP>();
  	for(Tile tileIterator: tileList){
  		 //Chaos Plan A -- 30 shorts per tile!!!
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS2"), We.getWireEnum("N2BEG1")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS4"), We.getWireEnum("N2BEG1")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS3"), We.getWireEnum("N2BEG3")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS4"), We.getWireEnum("N2BEG3")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS2"), We.getWireEnum("N2BEG4")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS5"), We.getWireEnum("N2BEG4")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS1"), We.getWireEnum("N2BEG5")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS6"), We.getWireEnum("N2BEG5")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS0"), We.getWireEnum("N2BEG6")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS7"), We.getWireEnum("N2BEG6")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS1"), We.getWireEnum("N2BEG8")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS7"), We.getWireEnum("N2BEG8")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS2"), We.getWireEnum("N6BEG1")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS4"), We.getWireEnum("N6BEG1")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS3"), We.getWireEnum("N6BEG3")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS4"), We.getWireEnum("N6BEG3")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS2"), We.getWireEnum("N6BEG4")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS5"), We.getWireEnum("N6BEG4")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS1"), We.getWireEnum("N6BEG5")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS6"), We.getWireEnum("N6BEG5")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS0"), We.getWireEnum("N6BEG6")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS7"), We.getWireEnum("N6BEG6")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS1"), We.getWireEnum("N6BEG8")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS7"), We.getWireEnum("N6BEG8")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS0"), We.getWireEnum("W2BEG8")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS7"), We.getWireEnum("W2BEG8")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS1"), We.getWireEnum("W2BEG7")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS6"), We.getWireEnum("W2BEG7")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS1"), We.getWireEnum("W2BEG5")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS7"), We.getWireEnum("W2BEG5")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS0"), We.getWireEnum("W6BEG8")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS7"), We.getWireEnum("W6BEG8")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS1"), We.getWireEnum("W6BEG7")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS6"), We.getWireEnum("W6BEG7")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS1"), We.getWireEnum("W6BEG5")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS7"), We.getWireEnum("W6BEG5")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS2"), We.getWireEnum("S6BEG1")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS4"), We.getWireEnum("S6BEG1")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS3"), We.getWireEnum("S6BEG3")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS4"), We.getWireEnum("S6BEG3")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS2"), We.getWireEnum("S6BEG4")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS5"), We.getWireEnum("S6BEG4")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS1"), We.getWireEnum("S6BEG5")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS6"), We.getWireEnum("S6BEG5")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS0"), We.getWireEnum("S6BEG6")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS7"), We.getWireEnum("S6BEG6")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS1"), We.getWireEnum("S6BEG8")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS7"), We.getWireEnum("S6BEG8")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS2"), We.getWireEnum("S2BEG1")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS4"), We.getWireEnum("S2BEG1")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS3"), We.getWireEnum("S2BEG3")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS4"), We.getWireEnum("S2BEG3")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS2"), We.getWireEnum("S2BEG4")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS5"), We.getWireEnum("S2BEG4")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS1"), We.getWireEnum("S2BEG5")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS6"), We.getWireEnum("S2BEG5")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS0"), We.getWireEnum("S2BEG6")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS7"), We.getWireEnum("S2BEG6")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS1"), We.getWireEnum("S2BEG8")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS7"), We.getWireEnum("S2BEG8")));
  		
  		//Chaos: Plan B
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS2"), We.getWireEnum("E2BEG3")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS4"), We.getWireEnum("E2BEG3")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS2"), We.getWireEnum("E2BEG1")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS5"), We.getWireEnum("E2BEG1")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS3"), We.getWireEnum("E2BEG0")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS4"), We.getWireEnum("E2BEG0")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS2"), We.getWireEnum("E6BEG3")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS4"), We.getWireEnum("E6BEG3")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS2"), We.getWireEnum("E6BEG1")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS5"), We.getWireEnum("E6BEG1")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS3"), We.getWireEnum("E6BEG0")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS4"), We.getWireEnum("E6BEG0")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS4"), We.getWireEnum("OMUX0")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS3"), We.getWireEnum("OMUX0")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS5"), We.getWireEnum("OMUX0")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS2"), We.getWireEnum("OMUX0")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS1"), We.getWireEnum("OMUX0")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS0"), We.getWireEnum("OMUX0")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS0"), We.getWireEnum("OMUX0")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS1"), We.getWireEnum("OMUX0")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS2"), We.getWireEnum("OMUX0")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS3"), We.getWireEnum("OMUX0")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS4"), We.getWireEnum("OMUX0")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS5"), We.getWireEnum("OMUX0")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS6"), We.getWireEnum("OMUX0")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS7"), We.getWireEnum("OMUX0")));
  		
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS4"), We.getWireEnum("OMUX1")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS3"), We.getWireEnum("OMUX1")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS5"), We.getWireEnum("OMUX1")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS2"), We.getWireEnum("OMUX1")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS1"), We.getWireEnum("OMUX1")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS0"), We.getWireEnum("OMUX1")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS0"), We.getWireEnum("OMUX1")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS1"), We.getWireEnum("OMUX1")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS2"), We.getWireEnum("OMUX1")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS3"), We.getWireEnum("OMUX1")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS4"), We.getWireEnum("OMUX1")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS5"), We.getWireEnum("OMUX1")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS6"), We.getWireEnum("OMUX1")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS7"), We.getWireEnum("OMUX1")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS4"), We.getWireEnum("OMUX2")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS3"), We.getWireEnum("OMUX2")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS5"), We.getWireEnum("OMUX2")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS2"), We.getWireEnum("OMUX2")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS1"), We.getWireEnum("OMUX2")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS0"), We.getWireEnum("OMUX2")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS0"), We.getWireEnum("OMUX2")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS1"), We.getWireEnum("OMUX2")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS2"), We.getWireEnum("OMUX2")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS3"), We.getWireEnum("OMUX2")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS4"), We.getWireEnum("OMUX2")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS5"), We.getWireEnum("OMUX2")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS6"), We.getWireEnum("OMUX2")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS7"), We.getWireEnum("OMUX2")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS4"), We.getWireEnum("OMUX3")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS3"), We.getWireEnum("OMUX3")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS5"), We.getWireEnum("OMUX3")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS2"), We.getWireEnum("OMUX3")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS1"), We.getWireEnum("OMUX3")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS0"), We.getWireEnum("OMUX3")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS0"), We.getWireEnum("OMUX3")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS1"), We.getWireEnum("OMUX3")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS2"), We.getWireEnum("OMUX3")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS3"), We.getWireEnum("OMUX3")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS4"), We.getWireEnum("OMUX3")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS5"), We.getWireEnum("OMUX3")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS6"), We.getWireEnum("OMUX3")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS7"), We.getWireEnum("OMUX3")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS4"), We.getWireEnum("OMUX4")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS3"), We.getWireEnum("OMUX4")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS5"), We.getWireEnum("OMUX4")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS2"), We.getWireEnum("OMUX4")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS1"), We.getWireEnum("OMUX4")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS0"), We.getWireEnum("OMUX4")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS0"), We.getWireEnum("OMUX4")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS1"), We.getWireEnum("OMUX4")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS2"), We.getWireEnum("OMUX4")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS3"), We.getWireEnum("OMUX4")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS4"), We.getWireEnum("OMUX4")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS5"), We.getWireEnum("OMUX4")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS6"), We.getWireEnum("OMUX4")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS7"), We.getWireEnum("OMUX4")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS4"), We.getWireEnum("OMUX5")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS3"), We.getWireEnum("OMUX5")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS5"), We.getWireEnum("OMUX5")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS2"), We.getWireEnum("OMUX5")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS1"), We.getWireEnum("OMUX5")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS0"), We.getWireEnum("OMUX5")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS0"), We.getWireEnum("OMUX5")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS1"), We.getWireEnum("OMUX5")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS2"), We.getWireEnum("OMUX5")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS3"), We.getWireEnum("OMUX5")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS4"), We.getWireEnum("OMUX5")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS5"), We.getWireEnum("OMUX5")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS6"), We.getWireEnum("OMUX5")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS7"), We.getWireEnum("OMUX5")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS4"), We.getWireEnum("OMUX6")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS3"), We.getWireEnum("OMUX6")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS5"), We.getWireEnum("OMUX6")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS2"), We.getWireEnum("OMUX6")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS1"), We.getWireEnum("OMUX6")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS0"), We.getWireEnum("OMUX6")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS0"), We.getWireEnum("OMUX6")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS1"), We.getWireEnum("OMUX6")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS2"), We.getWireEnum("OMUX6")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS3"), We.getWireEnum("OMUX6")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS4"), We.getWireEnum("OMUX6")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS5"), We.getWireEnum("OMUX6")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS6"), We.getWireEnum("OMUX6")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS7"), We.getWireEnum("OMUX6")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS4"), We.getWireEnum("OMUX7")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS3"), We.getWireEnum("OMUX7")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS5"), We.getWireEnum("OMUX7")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS2"), We.getWireEnum("OMUX7")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS1"), We.getWireEnum("OMUX7")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS0"), We.getWireEnum("OMUX7")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS0"), We.getWireEnum("OMUX7")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS1"), We.getWireEnum("OMUX7")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS2"), We.getWireEnum("OMUX7")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS3"), We.getWireEnum("OMUX7")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS4"), We.getWireEnum("OMUX7")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS5"), We.getWireEnum("OMUX7")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS6"), We.getWireEnum("OMUX7")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS7"), We.getWireEnum("OMUX7")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS4"), We.getWireEnum("OMUX8")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS3"), We.getWireEnum("OMUX8")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS5"), We.getWireEnum("OMUX8")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS2"), We.getWireEnum("OMUX8")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS1"), We.getWireEnum("OMUX8")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS0"), We.getWireEnum("OMUX8")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS0"), We.getWireEnum("OMUX8")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS1"), We.getWireEnum("OMUX8")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS2"), We.getWireEnum("OMUX8")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS3"), We.getWireEnum("OMUX8")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS4"), We.getWireEnum("OMUX8")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS5"), We.getWireEnum("OMUX8")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS6"), We.getWireEnum("OMUX8")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS7"), We.getWireEnum("OMUX8")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS4"), We.getWireEnum("OMUX9")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS3"), We.getWireEnum("OMUX9")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS5"), We.getWireEnum("OMUX9")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS2"), We.getWireEnum("OMUX9")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS1"), We.getWireEnum("OMUX9")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS0"), We.getWireEnum("OMUX9")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS0"), We.getWireEnum("OMUX9")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS1"), We.getWireEnum("OMUX9")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS2"), We.getWireEnum("OMUX9")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS3"), We.getWireEnum("OMUX9")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS4"), We.getWireEnum("OMUX9")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS5"), We.getWireEnum("OMUX9")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS6"), We.getWireEnum("OMUX9")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS7"), We.getWireEnum("OMUX9")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS4"), We.getWireEnum("OMUX10")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS3"), We.getWireEnum("OMUX10")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS5"), We.getWireEnum("OMUX10")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS2"), We.getWireEnum("OMUX10")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS1"), We.getWireEnum("OMUX10")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS0"), We.getWireEnum("OMUX10")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS0"), We.getWireEnum("OMUX10")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS1"), We.getWireEnum("OMUX10")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS2"), We.getWireEnum("OMUX10")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS3"), We.getWireEnum("OMUX10")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS4"), We.getWireEnum("OMUX10")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS5"), We.getWireEnum("OMUX10")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS6"), We.getWireEnum("OMUX10")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS7"), We.getWireEnum("OMUX10")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS4"), We.getWireEnum("OMUX11")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS3"), We.getWireEnum("OMUX11")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS5"), We.getWireEnum("OMUX11")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS2"), We.getWireEnum("OMUX11")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS1"), We.getWireEnum("OMUX11")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS0"), We.getWireEnum("OMUX11")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS0"), We.getWireEnum("OMUX11")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS1"), We.getWireEnum("OMUX11")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS2"), We.getWireEnum("OMUX11")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS3"), We.getWireEnum("OMUX11")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS4"), We.getWireEnum("OMUX11")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS5"), We.getWireEnum("OMUX11")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS6"), We.getWireEnum("OMUX11")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS7"), We.getWireEnum("OMUX11")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS4"), We.getWireEnum("OMUX12")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS3"), We.getWireEnum("OMUX12")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS5"), We.getWireEnum("OMUX12")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS2"), We.getWireEnum("OMUX12")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS1"), We.getWireEnum("OMUX12")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS0"), We.getWireEnum("OMUX12")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS0"), We.getWireEnum("OMUX12")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS1"), We.getWireEnum("OMUX12")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS2"), We.getWireEnum("OMUX12")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS3"), We.getWireEnum("OMUX12")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS4"), We.getWireEnum("OMUX12")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS5"), We.getWireEnum("OMUX12")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS6"), We.getWireEnum("OMUX12")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS7"), We.getWireEnum("OMUX12")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS4"), We.getWireEnum("OMUX13")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS3"), We.getWireEnum("OMUX13")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS5"), We.getWireEnum("OMUX13")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS2"), We.getWireEnum("OMUX13")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS1"), We.getWireEnum("OMUX13")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS0"), We.getWireEnum("OMUX13")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS0"), We.getWireEnum("OMUX13")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS1"), We.getWireEnum("OMUX13")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS2"), We.getWireEnum("OMUX13")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS3"), We.getWireEnum("OMUX13")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS4"), We.getWireEnum("OMUX13")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS5"), We.getWireEnum("OMUX13")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS6"), We.getWireEnum("OMUX13")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS7"), We.getWireEnum("OMUX13")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS4"), We.getWireEnum("OMUX14")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS3"), We.getWireEnum("OMUX14")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS5"), We.getWireEnum("OMUX14")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS2"), We.getWireEnum("OMUX14")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS1"), We.getWireEnum("OMUX14")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS0"), We.getWireEnum("OMUX14")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS0"), We.getWireEnum("OMUX14")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS1"), We.getWireEnum("OMUX14")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS2"), We.getWireEnum("OMUX14")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS3"), We.getWireEnum("OMUX14")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS4"), We.getWireEnum("OMUX14")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS5"), We.getWireEnum("OMUX14")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS6"), We.getWireEnum("OMUX14")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS7"), We.getWireEnum("OMUX14")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS4"), We.getWireEnum("OMUX15")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS3"), We.getWireEnum("OMUX15")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS5"), We.getWireEnum("OMUX15")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS2"), We.getWireEnum("OMUX15")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS1"), We.getWireEnum("OMUX15")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS0"), We.getWireEnum("OMUX15")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS0"), We.getWireEnum("OMUX15")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS1"), We.getWireEnum("OMUX15")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS2"), We.getWireEnum("OMUX15")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS3"), We.getWireEnum("OMUX15")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS4"), We.getWireEnum("OMUX15")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS5"), We.getWireEnum("OMUX15")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS6"), We.getWireEnum("OMUX15")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS7"), We.getWireEnum("OMUX15")));
  		
  		//Chaos: Plan C
		//LV / LH
  		pipList.add(new PIP(tileIterator, We.getWireEnum("OMUX2"), We.getWireEnum("LH0")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("OMUX4"), We.getWireEnum("LH0")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("OMUX11"), We.getWireEnum("LH24")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("OMUX13"), We.getWireEnum("LH24")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("OMUX2"), We.getWireEnum("LV0")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("OMUX4"), We.getWireEnum("LV0")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("OMUX11"), We.getWireEnum("LV24")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("OMUX13"), We.getWireEnum("LV24")));
  		
  		//Exhaustive searching
  		pipList.add(new PIP(tileIterator, We.getWireEnum("E6BEG5"), We.getWireEnum("BEST_LOGIC_OUTS1")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("E6BEG5"), We.getWireEnum("BEST_LOGIC_OUTS7")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("E6BEG7"), We.getWireEnum("BEST_LOGIC_OUTS1")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("E6BEG7"), We.getWireEnum("BEST_LOGIC_OUTS6")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("E6BEG8"), We.getWireEnum("BEST_LOGIC_OUTS0")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("E6BEG8"), We.getWireEnum("BEST_LOGIC_OUTS7")));
  		
  		//pipList.add(new PIP(tileIterator, We.getWireEnum("E6BEG6"), We.getWireEnum("OMUX9")));
  		//pipList.add(new PIP(tileIterator, We.getWireEnum("E6BEG9"), We.getWireEnum("OMUX15")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("E2BEG5"), We.getWireEnum("BEST_LOGIC_OUTS1")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("E2BEG5"), We.getWireEnum("BEST_LOGIC_OUTS7")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("E2BEG7"), We.getWireEnum("BEST_LOGIC_OUTS1")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("E2BEG7"), We.getWireEnum("BEST_LOGIC_OUTS6")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("E2BEG8"), We.getWireEnum("BEST_LOGIC_OUTS0")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("E2BEG8"), We.getWireEnum("BEST_LOGIC_OUTS7")));
  		
  		//3 7 11 15 19 23 27 31
  		
  		//IMUX connections skipping already routed locations
  		pipList.add(new PIP(tileIterator, We.getWireEnum("IMUX_B0"), We.getWireEnum("BEST_LOGIC_OUTS3")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("IMUX_B0"), We.getWireEnum("BEST_LOGIC_OUTS4")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("IMUX_B1"), We.getWireEnum("BEST_LOGIC_OUTS2")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("IMUX_B1"), We.getWireEnum("BEST_LOGIC_OUTS5")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("IMUX_B2"), We.getWireEnum("BEST_LOGIC_OUTS1")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("IMUX_B2"), We.getWireEnum("BEST_LOGIC_OUTS6")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("IMUX_B4"), We.getWireEnum("BEST_LOGIC_OUTS3")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("IMUX_B4"), We.getWireEnum("BEST_LOGIC_OUTS4")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("IMUX_B5"), We.getWireEnum("BEST_LOGIC_OUTS2")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("IMUX_B5"), We.getWireEnum("BEST_LOGIC_OUTS5")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("IMUX_B6"), We.getWireEnum("BEST_LOGIC_OUTS1")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("IMUX_B6"), We.getWireEnum("BEST_LOGIC_OUTS6")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("IMUX_B8"), We.getWireEnum("BEST_LOGIC_OUTS3")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("IMUX_B8"), We.getWireEnum("BEST_LOGIC_OUTS4")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("IMUX_B9"), We.getWireEnum("BEST_LOGIC_OUTS2")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("IMUX_B9"), We.getWireEnum("BEST_LOGIC_OUTS5")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("IMUX_B10"), We.getWireEnum("BEST_LOGIC_OUTS1")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("IMUX_B10"), We.getWireEnum("BEST_LOGIC_OUTS6")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("IMUX_B12"), We.getWireEnum("BEST_LOGIC_OUTS3")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("IMUX_B12"), We.getWireEnum("BEST_LOGIC_OUTS4")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("IMUX_B13"), We.getWireEnum("BEST_LOGIC_OUTS2")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("IMUX_B13"), We.getWireEnum("BEST_LOGIC_OUTS5")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("IMUX_B14"), We.getWireEnum("BEST_LOGIC_OUTS1")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("IMUX_B14"), We.getWireEnum("BEST_LOGIC_OUTS6")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("IMUX_B16"), We.getWireEnum("BEST_LOGIC_OUTS3")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("IMUX_B16"), We.getWireEnum("BEST_LOGIC_OUTS4")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("IMUX_B17"), We.getWireEnum("BEST_LOGIC_OUTS2")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("IMUX_B17"), We.getWireEnum("BEST_LOGIC_OUTS5")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("IMUX_B18"), We.getWireEnum("BEST_LOGIC_OUTS1")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("IMUX_B18"), We.getWireEnum("BEST_LOGIC_OUTS6")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("IMUX_B20"), We.getWireEnum("BEST_LOGIC_OUTS3")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("IMUX_B20"), We.getWireEnum("BEST_LOGIC_OUTS4")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("IMUX_B21"), We.getWireEnum("BEST_LOGIC_OUTS2")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("IMUX_B21"), We.getWireEnum("BEST_LOGIC_OUTS5")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("IMUX_B22"), We.getWireEnum("BEST_LOGIC_OUTS1")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("IMUX_B22"), We.getWireEnum("BEST_LOGIC_OUTS6")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("IMUX_B24"), We.getWireEnum("BEST_LOGIC_OUTS3")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("IMUX_B24"), We.getWireEnum("BEST_LOGIC_OUTS4")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("IMUX_B25"), We.getWireEnum("BEST_LOGIC_OUTS2")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("IMUX_B25"), We.getWireEnum("BEST_LOGIC_OUTS5")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("IMUX_B26"), We.getWireEnum("BEST_LOGIC_OUTS1")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("IMUX_B26"), We.getWireEnum("BEST_LOGIC_OUTS6")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("IMUX_B28"), We.getWireEnum("BEST_LOGIC_OUTS3")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("IMUX_B28"), We.getWireEnum("BEST_LOGIC_OUTS4")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("IMUX_B29"), We.getWireEnum("BEST_LOGIC_OUTS2")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("IMUX_B29"), We.getWireEnum("BEST_LOGIC_OUTS5")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("IMUX_B30"), We.getWireEnum("BEST_LOGIC_OUTS1")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("IMUX_B30"), We.getWireEnum("BEST_LOGIC_OUTS6")));
  		
  		
  		//pipList.add(new PIP(tileIterator, We.getWireEnum("E2BEG6"), We.getWireEnum("OMUX9")));
  		//pipList.add(new PIP(tileIterator, We.getWireEnum("E2BEG9"), We.getWireEnum("OMUX15")));
  		
//  		//Inner Long Line PIP connections using shorted PIP matrices as sources
//  		//LH
//  		pipList.add(new PIP(tileIterator, We.getWireEnum("LH18"), We.getWireEnum("N6BEG1")));
//  		pipList.add(new PIP(tileIterator, We.getWireEnum("LH18"), We.getWireEnum("S6BEG1")));
//  		
//  		pipList.add(new PIP(tileIterator, We.getWireEnum("LH12"), We.getWireEnum("W6BEG5")));
//  		pipList.add(new PIP(tileIterator, We.getWireEnum("LH12"), We.getWireEnum("N2BEG4")));
//  		pipList.add(new PIP(tileIterator, We.getWireEnum("LH12"), We.getWireEnum("N2BEG5")));
//  		
//  		pipList.add(new PIP(tileIterator, We.getWireEnum("LH6"), We.getWireEnum("W6BEG7")));
//  		pipList.add(new PIP(tileIterator, We.getWireEnum("LH6"), We.getWireEnum("N6BEG6")));
//  		pipList.add(new PIP(tileIterator, We.getWireEnum("LH6"), We.getWireEnum("S6BEG6")));
//  		
//  		//LV
//  		pipList.add(new PIP(tileIterator, We.getWireEnum("LV6"), We.getWireEnum("S6BEG6")));
//  		pipList.add(new PIP(tileIterator, We.getWireEnum("LV6"), We.getWireEnum("N6BEG6")));
//  		pipList.add(new PIP(tileIterator, We.getWireEnum("LV6"), We.getWireEnum("W6BEG7")));
//  		
//  		pipList.add(new PIP(tileIterator, We.getWireEnum("LV12"), We.getWireEnum("W6BEG5")));
//  		pipList.add(new PIP(tileIterator, We.getWireEnum("LV12"), We.getWireEnum("N2BEG4")));
//  		pipList.add(new PIP(tileIterator, We.getWireEnum("LV12"), We.getWireEnum("N2BEG5")));
//  		
//  		pipList.add(new PIP(tileIterator, We.getWireEnum("LV18"), We.getWireEnum("N6BEG1")));
//  		pipList.add(new PIP(tileIterator, We.getWireEnum("LV18"), We.getWireEnum("S6BEG1")));
		
		 /* //Bouncing!
		pipList.add(new PIP(tileIterator, We.getWireEnum("OMUX_SW5"), We.getWireEnum("IMUX_B3")));
		//pipList.add(new PIP(tileIterator, We.getWireEnum("IMUX_B3"), We.getWireEnum("BEST_LOGIC_OUTS0")));
		pipList.add(new PIP(tileIterator, We.getWireEnum("OMUX_SW5"), We.getWireEnum("IMUX_B7")));
		//pipList.add(new PIP(tileIterator, We.getWireEnum("IMUX_B7"), We.getWireEnum("BEST_LOGIC_OUTS7")));
		pipList.add(new PIP(tileIterator, We.getWireEnum("OMUX_SW5"), We.getWireEnum("IMUX_B11")));
		//pipList.add(new PIP(tileIterator, We.getWireEnum("IMUX_B11"), We.getWireEnum("BEST_LOGIC_OUTS7")));
		pipList.add(new PIP(tileIterator, We.getWireEnum("OMUX_SW5"), We.getWireEnum("IMUX_B15")));
		//pipList.add(new PIP(tileIterator, We.getWireEnum("IMUX_B15"), We.getWireEnum("BEST_LOGIC_OUTS0")));
		pipList.add(new PIP(tileIterator, We.getWireEnum("OMUX_SW5"), We.getWireEnum("IMUX_B19")));
		//pipList.add(new PIP(tileIterator, We.getWireEnum("IMUX_B19"), We.getWireEnum("BEST_LOGIC_OUTS7")));
		pipList.add(new PIP(tileIterator, We.getWireEnum("OMUX_SW5"), We.getWireEnum("IMUX_B23")));
		//pipList.add(new PIP(tileIterator, We.getWireEnum("IMUX_B23"), We.getWireEnum("BEST_LOGIC_OUTS0")));
		pipList.add(new PIP(tileIterator, We.getWireEnum("OMUX_SW5"), We.getWireEnum("IMUX_B27")));
		//pipList.add(new PIP(tileIterator, We.getWireEnum("IMUX_B27"), We.getWireEnum("BEST_LOGIC_OUTS0")));
		pipList.add(new PIP(tileIterator, We.getWireEnum("OMUX_SW5"), We.getWireEnum("IMUX_B31")));
		//pipList.add(new PIP(tileIterator, We.getWireEnum("IMUX_B31"), We.getWireEnum("BEST_LOGIC_OUTS7")));
 */
  		
  		//Plan D (not in use)
//  		pipList.add(new PIP(tileIterator, We.getWireEnum("VCC_WIRE"), We.getWireEnum("BOUNCE0")));
//  		pipList.add(new PIP(tileIterator, We.getWireEnum("GND_WIRE"), We.getWireEnum("BOUNCE0")));
  		
//  		pipList.add(new PIP(tileIterator, We.getWireEnum("KEEP1_WIRE"), We.getWireEnum("BOUNCE1")));
//  		pipList.add(new PIP(tileIterator, We.getWireEnum("GND_WIRE"), We.getWireEnum("BOUNCE1")));
//  		
//  		pipList.add(new PIP(tileIterator, We.getWireEnum("KEEP1_WIRE"), We.getWireEnum("BOUNCE2")));
//  		pipList.add(new PIP(tileIterator, We.getWireEnum("GND_WIRE"), We.getWireEnum("BOUNCE2")));
//  		
//  		pipList.add(new PIP(tileIterator, We.getWireEnum("KEEP1_WIRE"), We.getWireEnum("BOUNCE3")));
//  		pipList.add(new PIP(tileIterator, We.getWireEnum("GND_WIRE"), We.getWireEnum("BOUNCE3")));
  		
  		//GCLKS (not in use)
//  			pipList.add(new PIP(tileIterator, We.getWireEnum("KEEP1_WIRE"), We.getWireEnum("BOUNCE1")));
//  			pipList.add(new PIP(tileIterator, We.getWireEnum("GND_WIRE"), We.getWireEnum("BOUNCE2")));
//  			
//  			
//  			pipList.add(new PIP(tileIterator, We.getWireEnum("BOUNCE1"), We.getWireEnum("GCLK7")));
//  			pipList.add(new PIP(tileIterator, We.getWireEnum("BOUNCE2"), We.getWireEnum("GCLK7")));
//  			
//  			pipList.add(new PIP(tileIterator, We.getWireEnum("BOUNCE1"), We.getWireEnum("GCLK6")));
//  			pipList.add(new PIP(tileIterator, We.getWireEnum("BOUNCE2"), We.getWireEnum("GCLK6")));
//  			
//  			pipList.add(new PIP(tileIterator, We.getWireEnum("BOUNCE1"), We.getWireEnum("GCLK5")));
//  			pipList.add(new PIP(tileIterator, We.getWireEnum("BOUNCE2"), We.getWireEnum("GCLK5")));
//  			
//  			pipList.add(new PIP(tileIterator, We.getWireEnum("BOUNCE1"), We.getWireEnum("GCLK4")));
//  			pipList.add(new PIP(tileIterator, We.getWireEnum("BOUNCE2"), We.getWireEnum("GCLK4")));
//  			
//  			pipList.add(new PIP(tileIterator, We.getWireEnum("BOUNCE1"), We.getWireEnum("GCLK3")));
//  			pipList.add(new PIP(tileIterator, We.getWireEnum("BOUNCE2"), We.getWireEnum("GCLK3")));
//  			
//  			pipList.add(new PIP(tileIterator, We.getWireEnum("BOUNCE1"), We.getWireEnum("GCLK2")));
//  			pipList.add(new PIP(tileIterator, We.getWireEnum("BOUNCE2"), We.getWireEnum("GCLK2")));
//  			
//  			pipList.add(new PIP(tileIterator, We.getWireEnum("BOUNCE1"), We.getWireEnum("GCLK1")));
//  			pipList.add(new PIP(tileIterator, We.getWireEnum("BOUNCE2"), We.getWireEnum("GCLK1")));
//  			
//  			pipList.add(new PIP(tileIterator, We.getWireEnum("BOUNCE1"), We.getWireEnum("GCLK0")));
//  			pipList.add(new PIP(tileIterator, We.getWireEnum("BOUNCE2"), We.getWireEnum("GCLK0")));
  		
//  		pipList.add(new PIP(tileIterator, We.getWireEnum("LH24"), We.getWireEnum("LH0")));
//  		pipList.add(new PIP(tileIterator, We.getWireEnum("LV0"), We.getWireEnum("LH0")));
//  		pipList.add(new PIP(tileIterator, We.getWireEnum("LV24"), We.getWireEnum("LH0")));
//  		
//  		pipList.add(new PIP(tileIterator, We.getWireEnum("LH0"), We.getWireEnum("LH24")));
//  		pipList.add(new PIP(tileIterator, We.getWireEnum("LV0"), We.getWireEnum("LH24")));
//  		pipList.add(new PIP(tileIterator, We.getWireEnum("LV24"), We.getWireEnum("LH24")));
//  		
//  		pipList.add(new PIP(tileIterator, We.getWireEnum("LH0"), We.getWireEnum("LV0")));
//  		pipList.add(new PIP(tileIterator, We.getWireEnum("LH24"), We.getWireEnum("LV0")));
//  		pipList.add(new PIP(tileIterator, We.getWireEnum("LV24"), We.getWireEnum("LV0")));
//  		
//  		pipList.add(new PIP(tileIterator, We.getWireEnum("LH0"), We.getWireEnum("LV24")));
//  		pipList.add(new PIP(tileIterator, We.getWireEnum("LH24"), We.getWireEnum("LV24")));
//  		pipList.add(new PIP(tileIterator, We.getWireEnum("LV0"), We.getWireEnum("LV24")));
  		
  		
//  		pipList.add(new PIP(tileIterator, We.getWireEnum("LV12"), We.getWireEnum("LH0")));
//  		pipList.add(new PIP(tileIterator, We.getWireEnum("LV12"), We.getWireEnum("LH24")));
//  		
//  		pipList.add(new PIP(tileIterator, We.getWireEnum("LH12"), We.getWireEnum("LV0")));
//  		pipList.add(new PIP(tileIterator, We.getWireEnum("LH12"), We.getWireEnum("LV24")));
  		
  		//MORE!
//  		pipList.add(new PIP(tileIterator, We.getWireEnum("N2END2"), We.getWireEnum("LH0")));
//  		pipList.add(new PIP(tileIterator, We.getWireEnum("E2END2"), We.getWireEnum("LH0")));
//  		pipList.add(new PIP(tileIterator, We.getWireEnum("S2END2"), We.getWireEnum("LH0")));
  		
//  		pipList.add(new PIP(tileIterator, We.getWireEnum("N2MID1"), We.getWireEnum("LH0")));
//  		pipList.add(new PIP(tileIterator, We.getWireEnum("E2MID1"), We.getWireEnum("LH0")));
//  		pipList.add(new PIP(tileIterator, We.getWireEnum("S2MID1"), We.getWireEnum("LH0")));
/*
  		//Danny Classic (not in use)
	  	pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS0"), We.getWireEnum("OMUX0")));
	  	pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS4"), We.getWireEnum("OMUX0")));
	  	pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS1"), We.getWireEnum("OMUX1")));
	  	pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS5"), We.getWireEnum("OMUX1")));
	  	
	  	pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS2"), We.getWireEnum("OMUX2")));
	  	pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS0"), We.getWireEnum("OMUX2")));
	  	pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS3"), We.getWireEnum("OMUX3")));
	  	pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS7"), We.getWireEnum("OMUX3")));
	  	
	  	pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS4"), We.getWireEnum("OMUX4")));
	  	pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS2"), We.getWireEnum("OMUX4")));
	  	pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS5"), We.getWireEnum("OMUX5")));
	  	pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS3"), We.getWireEnum("OMUX5")));
	  	
	  	pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS6"), We.getWireEnum("OMUX6")));
	  	pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS4"), We.getWireEnum("OMUX6")));
	  	pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS7"), We.getWireEnum("OMUX7")));
	  	pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS5"), We.getWireEnum("OMUX7")));
	  	
	  	//Double Shorted OMUXs (not in use)
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS7"), We.getWireEnum("OMUX0")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS3"), We.getWireEnum("OMUX0")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS5"), We.getWireEnum("OMUX2")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS1"), We.getWireEnum("OMUX2")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS6"), We.getWireEnum("OMUX4")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS2"), We.getWireEnum("OMUX4")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS4"), We.getWireEnum("OMUX6")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS7"), We.getWireEnum("OMUX6")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("OMUX0"), We.getWireEnum("N2END1")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("OMUX2"), We.getWireEnum("N2END1")));
  		
  		pipList.add(new PIP(tileIterator, We.getWireEnum("OMUX4"), We.getWireEnum("N2END5")));
  		pipList.add(new PIP(tileIterator, We.getWireEnum("OMUX6"), We.getWireEnum("N2END5")));
  		*/
  		//Determine combination that creates larges power consumption (Finding VDD/2) (not in use)
//  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS4"), We.getWireEnum("OMUX0")));
//  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS3"), We.getWireEnum("OMUX0")));
//  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS5"), We.getWireEnum("OMUX0")));
//  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS2"), We.getWireEnum("OMUX0")));
//  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS1"), We.getWireEnum("OMUX0")));
//  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS0"), We.getWireEnum("OMUX0")));
//  		
//  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS2"), We.getWireEnum("OMUX0")));
//  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS1"), We.getWireEnum("OMUX0")));
//  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS3"), We.getWireEnum("OMUX0")));
//  		pipList.add(new PIP(tileIterator, We.getWireEnum("SECONDARY_LOGIC_OUTS0"), We.getWireEnum("OMUX0")));
//  		
//  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS7"), We.getWireEnum("OMUX0")));
//  		pipList.add(new PIP(tileIterator, We.getWireEnum("BEST_LOGIC_OUTS6"), We.getWireEnum("OMUX0")));
  	}
  	
  	for(PIP pipIterator: pipList){
  		SB.ActivatePIP(pipIterator);
  	}
	
    	System.out.print("Printing Bitstreams...");
    	String file1 = "E:\\Adam\\Bitstreams\\chaos_with_zeros_256HM.bit";
    	XDLDesignUtils.writeBitstreamToFile(fpga, file1);
    	System.out.println("done.");
    }
    
    public static void setupSlice(Instance Template, Instance STUB, Virtex4SliceBits SLICE){
    	for(Attribute A: Template.getAttributes()){
    	  if(!(A.getPhysicalName().equals("F")) && !(A.getPhysicalName().equals("G")))
    	  {
    		  SLICE.setAttributeBits(A, STUB.getTile());
    	  }
    	}
    }
}

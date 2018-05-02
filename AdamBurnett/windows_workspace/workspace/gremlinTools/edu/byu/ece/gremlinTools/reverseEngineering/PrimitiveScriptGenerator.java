package edu.byu.ece.gremlinTools.reverseEngineering;

import edu.byu.ece.rapidSmith.device.Device;
import edu.byu.ece.rapidSmith.util.FileTools;
import edu.byu.ece.rapidSmith.device.PrimitiveType;
import edu.byu.ece.rapidSmith.device.Utils;
import edu.byu.ece.rapidSmith.primitiveDefs.Element;
import edu.byu.ece.rapidSmith.primitiveDefs.PrimitiveDefPin;
import edu.byu.ece.rapidSmith.primitiveDefs.PrimitiveDef;
import edu.byu.ece.rapidSmith.primitiveDefs.PrimitiveDefList;
import java.io.*;
import java.util.*;

public class PrimitiveScriptGenerator {
	
	/*	primitive names
	 BSCAN 	 BUFDS 	 BUFG 	 BUFGCTRL 	 BUFIO	 BUFR	 CAPTURE	 CRC32	 CRC64	 DCI
	 DCIRESET	 DCM_ADV	 DSP48E 	 EFUSE_USR	 FIFO36_72_EXP 	 FIFO36_EXP	 FRAME_ECC 
	 GLOBALSIG	 GTP_DUAL	 GTX_DUAL	 ICAP 	 IDELAYCTRL	 ILOGIC 	 IOB	 IOBM
	 IOBS	 IODELAY	 IPAD	 ISERDES	 JTAGPPC	 KEY_CLEAR	 OLOGIC	 OPAD	 OSERDES
	 PCIE	 PLL_ADV	 PMV 	 PMVBRAM 	 PPC440 	 RAMB18X2	 RAMB18X2SDP	 RAMB36SDP_EXP
	 RAMB36_EXP 	 RAMBFIFO18	 RAMBFIFO18_36 	 RAMBFIFO36 	 SLICEL	 SLICEM	 STARTUP	 SYSMON 
	 TEMAC 	 TIEOFF	 USR_ACCESS 
*/	//all primitives listed
	
/*	private static final String[] names = {"BSCAN","BUFDS","BUFG","BUFGCTRL","BUFIO","BUFR","CAPTURE",
			 "CRC32","CRC64","DCI","DCIRESET","DCM_ADV","DSP48E","EFUSE_USR","FIFO36_72_EXP",
			 "FIFO36_EXP","FRAME_ECC","GLOBALSIG","GTP_DUAL","GTX_DUAL","ICAP","IDELAYCTRL",
			 "ILOGIC","IOB","IOBM","IOBS","IODELAY","IPAD","ISERDES","JTAGPPC","KEY_CLEAR",
			 "OLOGIC","OPAD","OSERDES","PCIE","PLL_ADV","PMV","PMVBRAM","PPC440","RAMB18X2",
			 "RAMB18X2SDP","RAMB36SDP_EXP","RAMB36_EXP","RAMBFIFO18","RAMBFIFO18_36","RAMBFIFO36",
			 "SLICEL","SLICEM","STARTUP","SYSMON","TEMAC","TIEOFF","USR_ACCESS"};
*/	
/*	 primitive placement
	"IOBS",placed LIOB_X0Y0 UNB_X0Y0  , "IOBM",placed LIOB_X0Y0 UNB_X0Y1  , "ILOGIC",placed IOI_X0Y0 ILOGIC_X0Y1  ,
	"IODELAY",placed IOI_X0Y0 IODELAY_X0Y1  ,"OLOGIC",placed IOI_X0Y0 OLOGIC_X0Y1  ,"RAMBFIFO36",placed BRAM_X5Y0 RAMB36_X0Y0  ,
	"DSP48E",placed DSP_X8Y0 DSP48_X0Y0  ,"DCM_ADV",placed CMT_X17Y0 DCM_ADV_X0Y0  ,
	"SYSMON",placed CFG_CENTER_X47Y44 SYSMON_X0Y0  ,"IPAD",placed CFG_CENTER_X47Y44 K9  ,
	"DCIRESET",placed CFG_CENTER_X47Y44 DCIRESET  ,"USR_ACCESS",placed CFG_CENTER_X47Y44 USR_ACCESS_SITE  ,
	"ICAP",placed CFG_CENTER_X47Y44 ICAP_X0Y1  ,"TIEOFF",placed INT_X17Y39 TIEOFF_X17Y39  ,
	"KEY_CLEAR",placed CFG_CENTER_X47Y44 KEY_CLEAR  ,"STARTUP",placed CFG_CENTER_X47Y44 STARTUP  ,
	"JTAGPPC",placed CFG_CENTER_X47Y44 JTAGPPC  ,"BSCAN",placed CFG_CENTER_X47Y44 BSCAN_X0Y3  ,
	"BUFGCTRL",placed CLK_BUFGMUX_X47Y44 BUFGCTRL_X0Y15  ,"FRAME_ECC",placed CFG_CENTER_X47Y44 FRAME_ECC  ,
	"IDELAYCTRL",placed HCLK_IOI_BOTCEN_X17Y29 IDELAYCTRL_X1Y1  ,"BUFIO",placed HCLK_IOI_BOTCEN_X17Y29 BUFIO_X1Y5  ,
	"BUFR",placed HCLK_IOI_X31Y9 BUFR_X1Y1  ,
	 */	
	
/*	xc5vlx30ff324
	private String[] placements = {"CFG_CENTER_X47Y44 BSCAN_X0Y3","BUFDS??","BUFG??",
	"CLK_BUFGMUX_X47Y44 BUFGCTRL_X0Y15","HCLK_IOI_BOTCEN_X17Y29 BUFIO_X1Y5","HCLK_IOI_X31Y9 BUFR_X1Y1",
	"CFG_CENTER_X47Y44 CAPTURE","CRC32??","CRC64","CFG_CENTER_X47Y44 DCIRESET","HCLK_IOI_X0Y69 DCI_X0Y3",
	"CMT_X17Y0 DCM_ADV_X0Y0","DSP_X8Y0 DSP48_X0Y0","CFG_CENTER_X47Y44 EFUSE_USR","BRAM_X5Y0 RAMB36_X0Y0",
	"BRAM_X5Y0 RAMB36_X0Y0","CFG_CENTER_X47Y44 FRAME_ECC","HCLK_X1Y69 GLOBALSIG_X1Y3","GTP_DUAL??","GTX_DUAL",
	"CFG_CENTER_X47Y44 ICAP_X0Y1","HCLK_IOI_BOTCEN_X17Y29 IDELAYCTRL_X1Y1","IOI_X0Y0 ILOGIC_X0Y1",
	"IOB??","LIOB_X0Y0 UNB_X0Y1","LIOB_X0Y0 UNB_X0Y0","IOI_X0Y0 IODELAY_X0Y1","CFG_CENTER_X47Y44 K9",
	"ISERDES","CFG_CENTER_X47Y44 JTAGPPC","CFG_CENTER_X47Y44 KEY_CLEAR","IOI_X0Y0 OLOGIC_X0Y1",
	"OPAD??","OSERDES??","PCIE??","CMT_X17Y0 PLL_ADV_X0Y0", "CFG_CENTER_X47Y44 PMV", "HCLK_BRAM_X26Y49 PMVBRAM_X1Y2",
    "PPC440??",
    "BRAM_X5Y0 RAMB36_X0Y0","BRAM_X5Y0 RAMB36_X0Y0",
    "BRAM_X5Y0 RAMB36_X0Y0","BRAM_X5Y0 RAMB36_X0Y0","BRAM_X5Y0 RAMB36_X0Y0","BRAM_X5Y0 RAMB36_X0Y0",
	"BRAM_X5Y0 RAMB36_X0Y0","CLBLM_X34Y70 SLICE_X57Y70","CLBLM_X34Y70 SLICE_X56Y70","CFG_CENTER_X47Y44 STARTUP",
	"CFG_CENTER_X47Y44 SYSMON_X0Y0","TEMAC??","INT_X17Y39 TIEOFF_X17Y39","CFG_CENTER_X47Y44 USR_ACCESS_SITE",
	};
*/

// xc5vfx30tff665  omitting GLOBALSIG and this device does not contain GTP_DUAL
	private static String[] placements={
			"CFG_CENTER_X63Y44 BSCAN_X0Y0",
			"GTX_X49Y9 BUFDS_X0Y0",
			"CLK_BUFGMUX_X63Y44 BUFGCTRL_X0Y0",
			"CLK_BUFGMUX_X63Y44 BUFGCTRL_X0Y0",
			"HCLK_IOI_X0Y9 BUFIO_X0Y0",
			"HCLK_IOI_X0Y9 BUFR_X0Y0",
			"CFG_CENTER_X63Y44 CAPTURE",
			"GTX_X49Y9 CRC32_X0Y0",
			"GTX_X49Y9 CRC64_X0Y0",
			"HCLK_IOI_X0Y9 DCI_X0Y0",
			"CFG_CENTER_X63Y44 DCIRESET",
			"CMT_X24Y0 DCM_ADV_X0Y0",
			"DSP_X32Y0 DSP48_X0Y0",
			"CFG_CENTER_X47Y44 EFUSE_USR",
			"BRAM_X5Y0 RAMB36_X0Y0",
			"BRAM_X5Y0 RAMB36_X0Y0",
			"CFG_CENTER_X63Y44 FRAME_ECC",
			"GTX_X49Y9 GTX_DUAL_X0Y0",
			"CFG_CENTER_X63Y44 ICAP_X0Y0",
			"HCLK_IOI_X0Y9 IDELAYCTRL_X0Y0",
			"IOI_X0Y0 ILOGIC_X0Y0",
			"LIOB_X0Y1 AE13",
			"LIOB_X0Y0 AD13",
			"LIOB_X0Y0 AD14",
			"IOI_X0Y0 IODELAY_X0Y0",
			"LIOB_X0Y1 AF13",
			"IOI_X0Y0 ILOGIC_X0Y0",
			"CFG_CENTER_X63Y44 JTAGPPC",
			"CFG_CENTER_X63Y44 KEY_CLEAR",
			"IOI_X0Y0 OLOGIC_X0Y0",
			"GTX_X49Y9 AA2",
			"IOI_X0Y0 OLOGIC_X0Y0",
			"PCIE_B_X48Y10 PCIE_X0Y0",
			"CMT_X24Y0 PLL_ADV_X0Y0",
			"CFG_CENTER_X47Y44 PMV",
			"HCLK_BRAM_X5Y9 PMVBRAM_X0Y0",
			"PPC_B_X36Y33 PPC440_X0Y0",
			"BRAM_X5Y0 RAMB36_X0Y0",
			"BRAM_X5Y0 RAMB36_X0Y0",
			"BRAM_X5Y0 RAMB36_X0Y0",
			"BRAM_X5Y0 RAMB36_X0Y0",
			"BRAM_X5Y0 RAMB36_X0Y0",
			"BRAM_X5Y0 RAMB36_X0Y0",
			"BRAM_X5Y0 RAMB36_X0Y0",
			"CLBLM_X1Y0 SLICE_X1Y0",
			"CLBLM_X1Y0 SLICE_X0Y0",
			"CFG_CENTER_X63Y44 STARTUP",
			"CFG_CENTER_X63Y44 SYSMON_X0Y0",
			"EMAC_X48Y40 TEMAC_X0Y0",
			"INT_X0Y0 TIEOFF_X0Y0",
			"CFG_CENTER_X63Y44 USR_ACCESS_SITE",
		};
	private static String[]  names = {
		"BSCAN","BUFDS","BUFG","BUFGCTRL","BUFIO","BUFR","CAPTURE","CRC32","CRC64","DCI","DCIRESET", 
		"DCM_ADV","DSP48E","EFUSE_USR","FIFO36_72_EXP","FIFO36_EXP","FRAME_ECC","GTX_DUAL","ICAP",
		"IDELAYCTRL","ILOGIC","IOB","IOBM","IOBS","IODELAY","IPAD","ISERDES","JTAGPPC","KEY_CLEAR", 
		"OLOGIC","OPAD","OSERDES","PCIE","PLL_ADV","PMV","PMVBRAM","PPC440","RAMB18X2","RAMB18X2SDP",
		"RAMB36_EXP","RAMB36SDP_EXP","RAMBFIFO18_36","RAMBFIFO18","RAMBFIFO36","SLICEL","SLICEM",
		"STARTUP","SYSMON","TEMAC","TIEOFF","USR_ACCESS",};
	
	public static final String partname ="xc5vfx30tff665";//"xc5vlx30ff324";
//	public static final String placement = "LIOB_X0Y0 UNB_X0Y0";  
	public static void main(String[] args) throws IOException {
		String first= "";
		String placement="";
		PrimitiveDefList defs = FileTools.loadPrimitiveDefs(partname);

		//creates a GTP_DUAL script
/*		for(int i =0;i<1;i++){
			first="GTP_DUAL";
			placement="GTP_X49Y9 GTP_DUAL_X0Y0";
			PrimitiveDef dsp = defs.getPrimitiveDef(Utils.getInstance().createPrimitiveType("GTP_DUAL"));
			Writer output =null;
	
			File file = new File("Primitive_scripts/"+first+"/"+first+"_script.py");
			if(file.exists()){System.out.println("OK");}
			else{System.out.println("file does not exist");
			File f = new File("Primitive_scripts/"+first);
			try{					
				if(f.mkdir())
					System.out.println("Directory Created");
				else
					System.out.println("Directory is not created");
				}catch(Exception e){
					e.printStackTrace();
				} 
			}
		
			output = new BufferedWriter(new FileWriter(file));
			SETFILE(first,output);
			GENERATEATTR(first,output,dsp);
			GENERATEINOUT(first,output,dsp);
			CREATESTD(first,output,"xc5vsx35tff665", placement);
			output.close();

		}*/
		
		//creates the other primitve scripts
		for(int i =0;i<names.length;i++){
			first=names[i];
			placement=placements[i];
			PrimitiveDef dsp = defs.getPrimitiveDef(Utils.createPrimitiveType(names[i]));
			Writer output =null;
	
			File file = new File("Primitive_scripts/"+first+"/"+first+"_script.py");
			if(file.exists()){}
			else{System.out.println("file does not exist");
			File f = new File("Primitive_scripts/"+first);
			try{
				if(f.mkdir())
					System.out.println("Directory Created");
				else
					System.out.println("Directory is not created");
				}catch(Exception e){
					e.printStackTrace();
				} 
			}
		
			output = new BufferedWriter(new FileWriter(file));
			SETFILE(first,output);
			GENERATEATTR(first,output,dsp);
			GENERATEINOUT(first,output,dsp);
			CREATESTD(first,output,partname, placement);
			output.close();
		}
		System.out.println("Your files has been written");

    }

	//creates the header part in a new file
	private static void SETFILE(String name,Writer output) throws IOException{
		java.util.Date today = new java.util.Date();
		String text = "\n# ================================================================"+'\n'
		+"# edu.byu.ece.gremlinTools.xilinxToolbox.XDLInstanceParameters" +'\n'
		+"#"+ today+'\n'
		+"# "+name+" parameters"+'\n'
		+"# ================================================================"
		+"\n\n"+"import sys, os, time, math"+'\n'+'\n';	
	    output.write(text);  			
		return;
	}
	private static void GENERATEATTR(String name, Writer output, PrimitiveDef dsp)throws IOException{
			
		//toArray add(object o)_
//		ArrayList<String> Primitive = new ArrayList<String>(); 
/*
		int k=0;
		for (Element element : dsp.getElements()) {
			if(element.getCfgOptions()!=null){
				Primitive.add(element.getName());
				Primitive.add("#OFF");
				k+=2;
				for(String option : element.getCfgOptions()){
					if(option.compareTo("<eqn>" )==0 ){
						if(element.getName().contains("5")){
							Primitive.add("#LUT:O5=0");
							Primitive.add("#LUT:O5=1");
							k+=2;
						}
						else if(element.getName().contains("6")){
							Primitive.add("#LUT:O6=0");
							Primitive.add("#LUT:O6=1");
							k+=2;
						}
						else{
							System.out.println("ERROR with translation");	
						}
					}
					else{
						Primitive.add(option);
						k+=1;}
				}
				Primitive.add("-1");
				k++;
			}	
		}	
		int p=0;
		System.out.println("k="+k);
		for(int i=0;i<k-1;i++)
		{
			if((Primitive.get(i)).compareTo("-1")==0){
				System.out.println();
			p=0;
			}
			else if(p==0){
				System.out.println(Primitive.get(i)+", ");
				p=1;
			}
			else
				System.out.print(Primitive.get(i)+", ");
		}
*/
		int max =0;
		int tem=0;
		int numberofAtt=0;
		output.write(name+"_ATTRIBUTES = ( \n");
		for (Element element : dsp.getElements()) {
			if(element.getCfgOptions()!=null){
				tem+=2;
				numberofAtt+=1;
				output.write(" (\""+element.getName()+"\"");
				if(element.getName().length()<4)
					output.write(",\t\t\t\t\t\"#OFF\"");
				else if(element.getName().length()<11)
					output.write(",\t\t\t\t\"#OFF\"");
				else if(element.getName().length()<20)
					output.write(",\t\t\t\"#OFF\"");
				else if(element.getName().length()<27)
					output.write(",\t\t\"#OFF\"");
				else
					output.write(",\t\"#OFF\"");
				for(String option : element.getCfgOptions()){
					tem+=1;
					if(option.compareTo("<eqn>" )==0 ){
						if(element.getName().contains("5"))
							output.write(", \""+"#LUT:O5=0"+"\",\""+"#LUT:O5=1"+"\"");
						else if(element.getName().contains("6"))
							output.write(", \""+"#LUT:O6=0"+"\",\""+"#LUT:O6=1"+"\"");
						else{
							System.out.println("ERROR with translation");
							output.write(", \""+option+"\"");	
						}
					}
					else{
						output.write(", \""+option+"\"");
					}
				}	
				output.write("),"+'\n');
			}
			if(tem>max){max=tem;}
			tem=0;
		}
		//System.out.println(max);
		output.write(")"+"\n\n"+
				"Max_Ele_size = "+max+"\n"+
				"Num_of_Attributes = "+numberofAtt+"\n\n");

	
	}
	private static void GENERATEINOUT(String name, Writer output,PrimitiveDef dsp)throws IOException{
		//inputs and outputs	danger External versus Internal name
		ArrayList<String> outpinList;
		ArrayList<String> inpinList;
		outpinList = new ArrayList<String>();
		inpinList = new ArrayList<String>();
		int innum=0;
		for (PrimitiveDefPin pin : dsp.getPins()) {
			if (pin.isOutput()==false){
				innum+=1;
				inpinList.add(pin.getExternalName());
			}
			else{
				outpinList.add(pin.getExternalName());
			}
		}
		
		output.write(name+"_INPUTS = (");
		if(inpinList.size()!=0){
			if (innum==1)
				output.write("\""+inpinList.get(0)+"\",");
			else
				output.write("\""+inpinList.get(0)+"\"");
		}
		int i=1;
		if(inpinList.size()>1){
			for(inpinList.get(i);i<inpinList.size();i++){
				output.write(",\""+inpinList.get(i)+"\"");
			}
		}
//Output pins processing		
		output.write(")\n\n");
		
		output.write(name+"_OUTPUTS = (");
		if(outpinList.size()!=0 ){
			if(outpinList.size()==1)
				output.write("\""+outpinList.get(0)+"\",");
			else
				output.write("\""+outpinList.get(0)+"\"");
		}
		i=1;
		if(outpinList.size()>1){
			for(outpinList.get(i);i<outpinList.size();i++){
				output.write(",\""+outpinList.get(i)+"\"");
			}
		}
		output.write(")\n\n\n\n");
	}
	private static void CREATESTD(String name,Writer output,String partname, String placement)throws IOException{
	
		String text=	
		"# GENERIC REFERENCE POINTERS \n" +
		"OUTPUTS = "+name+"_OUTPUTS \n" +
		"INPUTS = "+name+"_INPUTS \n" +
		"ATTRIBUTES = "+name+"_ATTRIBUTES \n" +
		"NumELE = Max_Ele_size \n" +
		"NumATT = Num_of_Attributes \n" +
		"inst_name = \"myInst\"\n\n" +
	
    	"def countElementsIn2DList(list):\n"+
		"    count = 0\n"+
		"    for i in range(0,len(list)):\n"+
		"        count += len(list[i])\n"+
		"    return count;\n\n\n"+
		"def createNetString():\n"+
		"    xdl_string = \"\"\n"+
		"    xdl_string += \"net \\\"myNet\\\" ,\\r\\n\" \n\n"+
		"    for i in range(0,len(OUTPUTS)):\n"+
		"        xdl_string += \"    outpin \" + inst_name + \" \" + OUTPUTS[i] + \",\\r\\n\" \n"+
		"    for i in range(0,len(INPUTS)): \n"+
		"        xdl_string += \"    inpin \" + inst_name + \" \" + INPUTS[i] + \",\\r\\n\" \n"+
		"    xdl_string += \";\\r\\n\" \n"+
		"    return xdl_string \n\n\n"+

		"def create_initial_XDL_file():\n"+
		"    xdl_string = \"design \\\" name \\\" "+partname+"-1 v3.2 , cfg \\\"\\\";\\r\\n\\r\\n\" \n"+
		"    xdl_string += \"inst \\\"\"+inst_name+\"\\\" \\\""+name+"\\\",placed "+placement+",\\r\\n  cfg \\\"\"\n"+
		"    for i in range(0,len(ATTRIBUTES)):\n"+
		"        xdl_string += \"\\t\" + ATTRIBUTES[i][0] + \"::\" + ATTRIBUTES[i][2] + \"\\r\\n\"\n"+
		"    xdl_string += \" \\\"\\r\\n;\\r\\n\" \n\n"+
		"    xdl_string += createNetString() \n\n"+
		"    return xdl_string \n"+

		"def create_test_XDL_file(attribute_ndx,attribute_value_ndx): \n"+
		"    xdl_string = \"design \\\"\\\" "+partname+"-1 v3.2 , cfg \\\"\\\";\\r\\n\\r\\n\" \n"+
		"    xdl_string += \"inst \\\"\"+inst_name+\"\\\" \\\""+name+"\\\",placed "+placement+",\\r\\n  cfg \\\"\" \n"+
		"    for i in range(0,len(ATTRIBUTES)): \n"+
		"        if(i == attribute_ndx): \n"+
		"            xdl_string += \"\\t\" + ATTRIBUTES[i][0] + \"::\" + ATTRIBUTES[i][attribute_value_ndx] + \"\\r\\n\" \n"+
		"        else: \n"+
		"            xdl_string += \"\\t\" + ATTRIBUTES[i][0] + \"::\" + ATTRIBUTES[i][2] + \"\\r\\n\" \n"+
		"    xdl_string += \" \\\"\\r\\n;\\r\\n\" \n\n"+
		"    xdl_string += createNetString() \n\n"+
		"    return xdl_string \n\n"+

		"# Start of script\n"+
		"curr_time = time.localtime()\n"+
		"output_file = \"results_%d-%d-%d_%dhr_%dmins_%dsecs.txt\" % (curr_time[1],curr_time[2],curr_time[0],curr_time[3],curr_time[4],curr_time[5]) \n"+
		"base_filename = \"\" \n"+
		"filename = \"\" \n\n\n"+

		"#Create initial blank designs to compare against \n"+
		"base_filename = \"base.xdl\" \n"+
		"f = open(base_filename,\"w\") \n"+
		"f.write(create_initial_XDL_file()) \n"+
		"f.close() \n"+
		"os.system(\'xdl -xdl2ncd -force \' + base_filename) \n"+
		"os.system(\'bitgen -d -w -b %sncd\' % base_filename[0:len(base_filename)-3]) \n"+
		"os.system('touch '+ output_file) \n\n"+

		"# Now test all the different aspects \n"+
		"itrs = (countElementsIn2DList(ATTRIBUTES)) \n"+
		"secsPerIter = 14 \n"+
		"differences = False \n\n"+

		"print \"********************************\" \n"+
		"print \"* Number of trials: \" + str(itrs) \n"+
		"print \"* Estimated Time : %d hours, %d minutes, %d seconds\" % (math.floor(itrs*secsPerIter/3600),math.floor(((itrs*secsPerIter) % 3600)/60), (itrs*secsPerIter) % 60) \n"+
		"print \"********************************\" \n"+
		"for i in range(0,len(ATTRIBUTES)): \n"+
		"    for j in range(1,len(ATTRIBUTES[i])): \n"+
		"        if(j!=2): \n"+
		"            filename = \"test_%s=%s.xdl\" % (ATTRIBUTES[i][0],str(j)) \n"+
		"            f = open(filename,\"w\") \n"+
		"            f.write(create_test_XDL_file(i,j)) \n"+
		"            f.close() \n"+
		"            os.system('xdl -xdl2ncd -force ' + filename) \n"+
		"            os.system('bitgen -d -w -b ' + filename[0:len(filename)-3] + \"ncd\") \n"+
		"            os.system('echo \\\"'+ATTRIBUTES[i][0]+\"=\"+ATTRIBUTES[i][j]+'\\\" >> ' + output_file) \n"+
		"            os.system('java -Xmx1000M edu.byu.ece.gremlinTools.bitstreamGizmo.BitstreamGizmo -i base.bit --diff ' + filename[0:len(filename)-3] + 'bit --ignoreECCBits >> ' + output_file) \n"+
		"        else: \n"+
		"            os.system('echo \\\"'+ATTRIBUTES[i][0]+\"=\"+ATTRIBUTES[i][j]+'\\\" >> ' + output_file)";
	output.write(text);
	
	}
}


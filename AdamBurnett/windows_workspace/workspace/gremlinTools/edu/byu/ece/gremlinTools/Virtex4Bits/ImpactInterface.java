package edu.byu.ece.gremlinTools.Virtex4Bits;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import XDLDesign.Utils.XDLDesignUtils;
import edu.byu.ece.rapidSmith.bitstreamTools.bitstream.Bitstream;
import edu.byu.ece.rapidSmith.bitstreamTools.bitstream.BitstreamException;
import edu.byu.ece.rapidSmith.bitstreamTools.bitstream.BitstreamHeader;
import edu.byu.ece.rapidSmith.bitstreamTools.bitstream.BitstreamParser;
import edu.byu.ece.rapidSmith.bitstreamTools.bitstream.DummySyncData;
import edu.byu.ece.rapidSmith.bitstreamTools.bitstream.Packet;
import edu.byu.ece.rapidSmith.bitstreamTools.bitstream.PacketList;
import edu.byu.ece.rapidSmith.bitstreamTools.bitstream.PacketOpcode;
import edu.byu.ece.rapidSmith.bitstreamTools.bitstream.RegisterType;
import edu.byu.ece.rapidSmith.bitstreamTools.configuration.FPGA;
import edu.byu.ece.rapidSmith.bitstreamTools.configuration.Frame;
import edu.byu.ece.rapidSmith.bitstreamTools.configuration.FrameAddressRegister;
import edu.byu.ece.rapidSmith.bitstreamTools.configuration.FrameData;
import edu.byu.ece.rapidSmith.bitstreamTools.configurationSpecification.BlockSubType;
import edu.byu.ece.rapidSmith.bitstreamTools.configurationSpecification.XilinxConfigurationSpecification;
import edu.byu.ece.rapidSmith.util.MessageGenerator;

public class ImpactInterface {
     
	
	
	//Perform Full Bitstream ReadBack
	  //Send Capture Command
	//Write a Frame
	
	//Peform Readback Verify
	
	public static void ReadBackBitstream(FPGA ReadBackBitstream, String MaskFilename, String CurrentDir){
		
		//Create Batch Command Script
		//Create a Batch File 
		//XDLDesignUtils.writeBitstreamToFile(ReadBackBitstream, "Mask.msk");
		String SetMode = "setmode -bscan\n";
		String SetCable = "setcable -p auto\n";
		String Identify = "identify\n";
		String AssignFile = "assignFile -p 1 -file " + MaskFilename + "\n";
		String Verify =    "Verify -p 1\n";
		String Quit    =    "quit\n";
		
		try {
			  FileWriter fstream = new FileWriter("batchScript.cmd");
			  BufferedWriter out = new BufferedWriter(fstream);
			  out.write(SetMode);
			  out.write(SetCable);
			  out.write(Identify);
			  out.write(AssignFile);
			  out.write(Verify);
			  out.write(Quit);
			  out.close(); 
		} catch (Exception e){
			System.err.println("Error: " + e.getMessage());
		}
		
		//Run Impact
		runImpact("batchScript.cmd");
		//Load ReadBack Bitstream
		loadReadbackBitstream(ReadBackBitstream, "../" + CurrentDir+ "impact.bin");
		
		
	}
	
	public static void SendCaptureCommand(){
		
		BitstreamHeader Header = new BitstreamHeader("CaptureBitstream.ncd", "xc4vlx60ff668-12");
		PacketList Packets = new PacketList();
				
		Packets.add(Packet.buildZeroWordPacket(PacketOpcode.NOP, RegisterType.NONE));
		Packets.add(Packet.buildZeroWordPacket(PacketOpcode.NOP, RegisterType.NONE));
		Packets.add(Packet.buildOneWordPacket(PacketOpcode.WRITE, RegisterType.CMD, 12)); //GCapture Command
		Packets.add(Packet.buildZeroWordPacket(PacketOpcode.NOP, RegisterType.NONE));
		Packets.add(Packet.buildZeroWordPacket(PacketOpcode.NOP, RegisterType.NONE));
		Packets.add(Packet.buildZeroWordPacket(PacketOpcode.NOP, RegisterType.NONE));
		Packets.add(Packet.buildZeroWordPacket(PacketOpcode.NOP, RegisterType.NONE));
	
		for(int i=0; i<1000; i++){
			Packets.add(Packet.buildZeroWordPacket(PacketOpcode.NOP, RegisterType.NONE));	
		}
		
	
		//Packets.add(Packet.)
		Bitstream BS = new Bitstream(Header, DummySyncData.V4_STANDARD_DUMMY_SYNC_DATA, Packets);
	    
		//Write Bitstream to File
		try {
			FileOutputStream out = new FileOutputStream(new File("CaptureBitstream.bit"));
			try {
				BS.outputBitstream(out);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
	
		}
		
		DownloadBitStream("CaptureBitstream.bit");
	}
	
	
	public static void DownloadBitStream(FPGA fpga){
		System.out.println("Writing Bitstream to File");		
		XDLDesignUtils.writeBitstreamToFile(fpga, "Temp.bit");
		DownloadBitStream("Temp.bit");
	}
	
	
	public static void DownloadBitStream(String BitstreamFilename){
		
		//Create a Batch File 
		System.out.println("Create a Batch File");
		String SetMode = "setmode -bscan\n";
		String SetCable = "setcable -p auto\n";
		String Identify = "identify\n";
		String AssignFile = "assignFile -p 1 -file " + BitstreamFilename + "\n";
		String Program =    "program -p 1\n";
		String Quit    =    "quit\n";
		
		try {
			  FileWriter fstream = new FileWriter("batchScript.cmd");
			  BufferedWriter out = new BufferedWriter(fstream);
			  out.write(SetMode);
			  out.write(SetCable);
			  out.write(Identify);
			  out.write(AssignFile);
			  out.write(Program);
			  out.write(Quit);
			  out.close(); 
		} catch (Exception e){
			System.err.println("Error: " + e.getMessage());
		}
		
		System.out.println("Invoking Impact...");
		
		if(!runImpact("batchScript.cmd")){
			System.out.println("Impact Crashed!");
		}
	}
	
	private static boolean runImpact(String BatchFilename){
       String command = "impact -batch " + BatchFilename;  
       
		try {
			
			FileWriter fstream = new FileWriter("export.sh");
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("export XIL_IMPACT_VIRTEX_DUMPBIN=1");
			out.close();
			//Process O = Runtime.getRuntime().exec("sh export.sh");
			Process P = Runtime.getRuntime().exec(command);
			
			if(P.waitFor() != 0){
				MessageGenerator.briefError("Impact did not run correctly.");
				return false;
			}
			if(P.exitValue() != 0){
				MessageGenerator.briefError("Impact did not exit correctly.");
				return false; 
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return false;
		}
		return true; 
	}
	
	public static void loadReadbackBitstream(FPGA fpga, String ReadBackBitStreamFilename){
		
		File BS = new File(ReadBackBitStreamFilename);
		ArrayList<Byte> bytes = new ArrayList<Byte>();
		
		try {
			FileInputStream istream = new FileInputStream(BS);
			
			//Load the File
			int numBytes = istream.available();
	    	
			for(int i = 0; i < numBytes; i++) {
	    		bytes.add((byte)istream.read());
	    	}
	    	istream.close();
	    	
	    	//Create A Full Bitstream From Raw Bitstream
	    	//Bitstream FBS = CreateFullBitstream(bytes);
	    	
	    	//FPGA ReadBackFPGA = new FPGA(fpga.getDeviceSpecification());
	        //ReadBackFPGA.configureBitstream(FBS);
	    	
	        ArrayList<Integer> data = new ArrayList<Integer>(); 
	    	
	    	//for(int i=0; i<bytes.size(); i+=4){
			//	data.add(BitstreamParser.getWordAsInt(bytes, i));
			//}
	    	
	    	int i = 41*4; 
	    	
	    
	    	fpga.setFAR(0x0);
	    	XilinxConfigurationSpecification Spec = fpga.getDeviceSpecification();
	    	do {
	    		BlockSubType BT = Spec.getBlockSubtype(Spec, fpga.getFAR());
	    	
	    		if(BT.getName().equals("LOGIC_OVERHEAD") || BT.getName().equals("BRAMOVERHEAD")){
	    			//System.out.println(BT.getName());
	    			i+=41*4;//Skip the frame
	    		} else {
	    			
	    			//fpga.getFrame(fpga.getFAR()).configure(new FrameData(data.set(i, i+40))); 
	    		    for(int j=0; j<41; j++){
	    			  fpga.getFrame(fpga.getFAR()).getData().setData(j, BitstreamParser.getWordAsInt(bytes, i));
	    		      i+=4; 
	    		    }
	    			
	    		   // System.out.println(i);
	    		}
	    	} while (fpga.incrementFAR());
	    	
	    	//System.out.println(FBS.toXMLString());
	    	//System.in.read();
	    	//for(Frame F : fpga.getAllFrames()){
	    	//	F.reset();
	    	//}
	    	//fpga.configureBitstream(FBS);
	    				
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private static Bitstream CreateFullBitstream(ArrayList<Byte> bytes) {
		
		BitstreamHeader Header = new BitstreamHeader("Readback.ncd", "xc4vlx60ff668-12");
		PacketList Packets = new PacketList();
		
		ArrayList<Integer> data = new ArrayList<Integer>();
		
		for(int i=0; i<bytes.size(); i+=4){
			data.add(BitstreamParser.getWordAsInt(bytes, i));
		}
		System.out.println(data.get(77145+41));
		System.out.println(data.get(77146+41));
		
		Packets.add(Packet.buildZeroWordPacket(PacketOpcode.WRITE, RegisterType.FDRI));
		try {
			Packet P = Packet.buildMultiWordType2Packet(PacketOpcode.WRITE, data);
		    Packets.add(P);
		} catch (BitstreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return new Bitstream(Header, DummySyncData.V4_STANDARD_DUMMY_SYNC_DATA, Packets);
	}
    
	
	private static FrameData getFrameData(ArrayList<Byte> bytes, int start) {
		FrameData f = new FrameData(41);
		
	    for(int k=0; k<41; k++){
	    	f.setData(k, BitstreamParser.getWordAsInt(bytes, start));
	    }
		
	    return f;
	}
}

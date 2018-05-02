package edu.byu.ece.gremlinTools.bitstreamGizmo;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;


import edu.byu.ece.gremlinTools.bitstream.GremlinBitstream;
import edu.byu.ece.gremlinTools.bitstreamDatabase.BitstreamDB;
import edu.byu.ece.gremlinTools.bitstreamDatabase.VerifyDB;
import edu.byu.ece.gremlinTools.xilinxToolbox.XilinxToolbox;
import edu.byu.ece.gremlinTools.xilinxToolbox.XilinxToolboxLookup;
import edu.byu.ece.rapidSmith.bitstreamTools.bitstream.Bitstream;
import edu.byu.ece.rapidSmith.bitstreamTools.bitstream.BitstreamParseException;
import edu.byu.ece.rapidSmith.bitstreamTools.bitstream.BitstreamParser;
import edu.byu.ece.rapidSmith.bitstreamTools.bitstream.BitstreamUtils;
import edu.byu.ece.rapidSmith.bitstreamTools.configuration.FPGA;
import edu.byu.ece.rapidSmith.bitstreamTools.configuration.Frame;
import edu.byu.ece.rapidSmith.bitstreamTools.configuration.FrameAddressRegister;
import edu.byu.ece.rapidSmith.bitstreamTools.configuration.FrameData;
import edu.byu.ece.rapidSmith.bitstreamTools.configurationSpecification.AbstractConfigurationSpecification;
import edu.byu.ece.rapidSmith.bitstreamTools.configurationSpecification.DeviceLookup;
import edu.byu.ece.rapidSmith.bitstreamTools.configurationSpecification.V4ConfigurationSpecification;
import edu.byu.ece.rapidSmith.bitstreamTools.configurationSpecification.XilinxConfigurationSpecification;
import edu.byu.ece.rapidSmith.design.Design;
import edu.byu.ece.rapidSmith.design.PIP;
import edu.byu.ece.rapidSmith.util.FileTools;


import joptsimple.OptionParser;
import joptsimple.OptionSet;

import static java.util.Arrays.asList;

public class BitstreamGizmo {
	/*==== Flag Options ====*/
	/** A flag to duplicate the frame writes in the bitstream */
	private boolean duplicateFrames;
	/** A flag to insert invalid frame writes in the bitstream */
	private boolean insertInvalidFrames;
	/** A flag to compress the bitstream */
	private boolean compressBitstream;
	/** A flag to ignore 12 ECC bits in each frame when comparing bitstreams */
	private boolean ignoreECCBits;
	/** A flag to determine if we should extract an XDL file from the loaded bitstream */
	private boolean extractXDL;
	/** A flag to indicate verboseness */
	private boolean verbose;
	/** A flag to verify database with files */
	private boolean verifyDatabase;
	
	/*==== File Names ====*/
	/** The name of the input XDL file */
	private String xdlInputFileName;
	/** The name of the output XDL file */
	private String xdlOutputFileName;	
	/** The name of the input bitstream (normally .bit) file. */
	private String bitInputFileName;	
	/** The name of the output (.bit) file.*/
	private String bitOutputFileName;
	/** The name of the output ASCII MCS (PROM) file name*/
	private String mcsOutputFileName;
	/** The name of the output FAR file*/
	private String farOutputFileName;
	/** The name of the bitstream to compare against the file called bitInputFileName */
	private String bitCompareFileName;
	/** The name of the output XML file name*/
	private String xmlOutputFileName;
	/** The name of the output TXT file name*/
	private String txtOutputFileName;
	/** The name of the input changeBits file name*/
	private String changeBitsInputFileName;
	/** The name of the input duplicatedFrames (FAR address list in hex) file name */
	private String duplicatedFramesInputFileName;
	/** The name of the input invalid frames (FAR address list in hex) file name */
	private String invalidFramesInputFileName;
	/** The name of the input file containing bits to decode [far] [frame bit number] */
	private String decodeBitsInputFileName;
	/** The name of the output file which expresses the bit in XDL */
	private String decodeBitsOutputFileName;

	/*==== Helper Objects ====*/
	/** A random number generator, used for randomizing the frames.*/
	private Random generator;	
	/** A packaged command line options parser, used for ease of command line parsing.*/
	private OptionParser parser;
	/** Part of the command line options parser. */
	private OptionSet options;		
	/** An XDL Object for loading and saving XDL files */
	private Design design;
	/** This is the specific part we are using */
	private XilinxConfigurationSpecification spec;
	/** The actual Bitstream instance.*/
	private GremlinBitstream bitstream;
	/** A simulated FPGA device */
	private FPGA fpga;
	/** Bitstream Database Object */
	private BitstreamDB db;
	
	/*==== Class Data ====*/
	/** An integer seed used for reproducing the random generator instance from run to run.*/
	private Long seed;
	/** The time the tool begins operation, only used in -v (verbose) mode */
	private static Date startTime;
	/** OS-specific line terminator string */
	private String newLine;
	/** Initial test size of duplicated/invalid frames */
	private static final int testFrameCount = 256;
	/** The desired speed grade for XDL extracted from a bitstream */
	private String extractedSpeedGrade;
	/** User Message to printout during a bitstream diff*/
	private String message;
	
	/**
	 * Constructor for initialization
	 */
	public BitstreamGizmo(){
		duplicateFrames = false;
		insertInvalidFrames = false;
		compressBitstream = false;
		ignoreECCBits = false;
		extractXDL = false;
		verbose = false;
		verifyDatabase = false;
		
		xdlInputFileName = null;
		xdlOutputFileName = null;
		extractedSpeedGrade = null;
		bitInputFileName = null;	
		bitOutputFileName = null;
		mcsOutputFileName = null;
		farOutputFileName = null;
		bitCompareFileName = null;
		xmlOutputFileName = null;
		txtOutputFileName = null;
		changeBitsInputFileName = null;
		duplicatedFramesInputFileName = null;
		invalidFramesInputFileName = null;
		decodeBitsInputFileName = null;
		decodeBitsOutputFileName = null;
		
		generator = null;	
		parser = new OptionParser();
		options = null;
		design = null;
		spec = null;
		db = new BitstreamDB();
		message = "";
		
		seed = 0L;
		startTime = null;
		newLine = System.getProperty("line.separator");
	}
	
	
	/**
	 * This method will take args and use the joptsimple package to parse the arguments.
	 * The arguments will update the private variables of the class accordingly.  If any of 
	 * the arguments are not correct, this method will print an error message and exit.
	 * @param args An array of strings, commonly used as the command line input strings from main().
	 */
	private void parseCommandLine(String[] args){
		try {
			this.options = parser.parse(args);
		}
		catch(Exception e){
			System.out.println(e.getMessage());
			System.exit(1);			
		}
		
		// Print help options
		if(options.has("h")){
			try {
				System.out.println("********************************************************************************");
				System.out.println("********************* BitstreamGizmo ver. 3.0beta - 11/30/2009 *****************");
				System.out.println("********************************************************************************\n");
				parser.printHelpOn(System.out);
				System.out.println("\nOptions -r and -c are mutually exclusive.");
				
			} catch (IOException e) {
				System.out.println(e.getMessage());
				System.exit(1);
			}
			System.exit(0);
		}
		
		// Check for option collision
		if(options.has("r") && options.has("c")){
			System.out.println("Cannot randomize frames and do compression. These options are mutually exclusive.");
			System.exit(1);
		}		
		// Add verbose mode
		if(options.has("v")){
			verbose = true;		
			System.out.println("Verbose mode on");
			startTime = new java.util.Date();
		}
		
		// Duplicate the frames
		if(options.has("d")){duplicateFrames = true;}
		
		// Duplicate frames from a FAR list
		if(options.hasArgument("d")){duplicatedFramesInputFileName = (String)options.valueOf("d");}
		
		// Insert invalid frames
		if(options.has("b")){insertInvalidFrames = true;}
		
		// Insert invalid frames from a FAR list
		if(options.hasArgument("b")){invalidFramesInputFileName = (String)options.valueOf("b");}
		
		// Input XDL file
		if(options.has("a")){xdlInputFileName = (String) options.valueOf("a");}

		// Output XDL file
		if(options.has("e")){this.xdlOutputFileName = (String) options.valueOf("e");}
		
		// Output FAR file
		if(options.has("f")){this.farOutputFileName = (String) options.valueOf("f");}
		
		// Check for input file name
		if(options.has("i")){this.bitInputFileName = (String) options.valueOf("i");}
				
		// Check for output bitstream file name
		if(options.has("o")){this.bitOutputFileName = (String) options.valueOf("o");}	

		// Check for comparison bitstream file name
		if(options.has("diff")){this.bitCompareFileName = (String) options.valueOf("diff");}	
		
		// Check for output ASCII MCS (PROM) file name
		if(options.has("m")){this.mcsOutputFileName = (String) options.valueOf("m");}		

		// Check for output XML file name
		if(options.has("x")){this.xmlOutputFileName = (String) options.valueOf("x");}	

		// Check for output TXT file name
		if(options.has("t")){this.txtOutputFileName = (String) options.valueOf("t");}
		
		// Check for compression flag
		if(options.has("c")){this.compressBitstream = true;}
		
		// Check for decode PIPs flag
		if(options.has("ignoreECCBits")){this.ignoreECCBits = true;}

		// Check for verify database
		if(options.has("verifyDatabase")){this.verifyDatabase = true;}

		// Check for changeBits file name
		if(options.has("changeBits")){
			this.changeBitsInputFileName = (String) options.valueOf("changeBits");
		}
		
		// Check for changeBits file name
		if(options.has("msg")){
			this.message = (String) options.valueOf("msg");
		}
		
		// Check for decodeBits file name
		if(options.has("decodeBits")){
			this.decodeBitsInputFileName = (String) options.valueOf("decodeBits");
		}

		// Check for decodeBitsOutput file name
		if(options.has("decodeBitsOutput")){
			this.decodeBitsOutputFileName = (String) options.valueOf("decodeBitsOutput");
		}
		// Check for extract XDL flag, load optional speed grade preference
		if(options.has("extractXDL")){
			this.extractXDL = true;
			if(options.hasArgument("extractXDL")){
				this.extractedSpeedGrade = (String) options.valueOf("extractXDL");
			}
		}
		
		// Check for randomization
		if(options.has("r")){this.generator = new Random();}
		
		// Check for supplied random seed, if the seed is not supplied, the seed from 
		// created during the initialization of the Random class (from time) is used.
		if(options.has("s")){
			this.seed = (Long) options.valueOf("s"); generator.setSeed(seed);
			if(verbose) System.out.println("seed = " + this.seed);
		}
				
		// Check for part filename
		if(options.has("p")){
			spec = DeviceLookup.lookupPartV4V5V6(DeviceLookup.getRootDeviceName((String) options.valueOf("p")));
			if(spec == null){
				failAndExit("Invalid part name: " + (String) options.valueOf("p"));
			}
			if(this.verbose) System.out.println("Part name: " + spec.getDeviceName());
		}		
	}
	
	/**
	 * This configures the command line options parser, adds specific commands and switches.
	 */
	public void configureParser(){
		parser.acceptsAll(asList("c","compress"), "Compress the bitstream (different method than Xilinx bitgen)");
		parser.acceptsAll(asList("d","duplicate"), "Duplicate the frame writes").withOptionalArg().ofType(String.class);
		parser.acceptsAll(asList("i","input"), "Name of input bit file").withRequiredArg().ofType(String.class);
		parser.acceptsAll(asList("h","?","help"), "Prints this help");
		parser.acceptsAll(asList("o","output"), "Name of output bit file").withRequiredArg().ofType(String.class);
		parser.acceptsAll(asList("v","verbose"), "Verbose mode");
		parser.acceptsAll(asList("r","randomize"), "Randomize frames");
		parser.acceptsAll(asList("s","seed"), "Supply random seed (for repeatability)").withRequiredArg().ofType(Long.class);
		parser.acceptsAll(asList("decodeBits"), "Returns a list of XDL names for bits").withRequiredArg().ofType(String.class);
		parser.acceptsAll(asList("decodeBitsOutput"), "File name to where decoded bit results are stored").withRequiredArg().ofType(String.class);
		parser.acceptsAll(asList("p","part"), "Name of part (ie. XC4VFX12)").withRequiredArg().ofType(String.class);
		parser.acceptsAll(asList("m","mcs"), "Name of bitstream output ASCII MCS (PROM) file").withRequiredArg().ofType(String.class);
		parser.acceptsAll(asList("x","xml"), "Name of bitstream output XML file").withRequiredArg().ofType(String.class);
		parser.acceptsAll(asList("t","txt"), "Name of bitstream output TXT file").withRequiredArg().ofType(String.class);
		parser.acceptsAll(asList("f","far"), "Creates a list file of valid FAR addresses for the device").withRequiredArg().ofType(String.class);
		parser.acceptsAll(asList("b","bad"), "Insert frames with bad (invalid) FAR addresses").withOptionalArg().ofType(String.class);
		parser.acceptsAll(asList("a","loadXDL"), "Load the specified XDL file into memory").withRequiredArg().ofType(String.class);
		parser.acceptsAll(asList("e","saveXDL"), "Save the XDL object in memory to a file").withRequiredArg().ofType(String.class);
		parser.acceptsAll(asList("verifyDatabase"), "Verify XDL/Bitstream Database (requires options -i and -a)");
		parser.acceptsAll(asList("findBit"), "Find bit in bitstream specified by -i").withRequiredArg().ofType(String.class);
		parser.acceptsAll(asList("diff"), "Compares two bitstream and outputs bit differences").withRequiredArg().ofType(String.class);
		parser.acceptsAll(asList("changeBits"), "Changes bits in bitstream to those specified in file").withRequiredArg().ofType(String.class);
		parser.acceptsAll(asList("extractXDL"), "Reads in bitstream (-i), outputs XDL (-e)").withOptionalArg().ofType(String.class);
		parser.acceptsAll(asList("ignoreECCBits"), "Ignores 12 ECC bits of each frame with --diff option").withOptionalArg().ofType(String.class);
		parser.acceptsAll(asList("msg","message"), "Adds a message to front of --diff output").withOptionalArg().ofType(String.class);
	}
	
	/**
	 * Loads the bitstream if a filename has been set/loaded, also sets the part if it is null
	 */
	private void loadBitstream(){
		if(bitInputFileName != null){
			try {
				bitstream = new GremlinBitstream(BitstreamParser.parseBitstream(bitInputFileName));
				if(verbose) System.out.println("Successfully loaded bitstream: " + bitInputFileName);
				if(verbose) System.out.println(bitstream.getHeader().toString());
				
				// If we haven't gotten the part from the program arguments,
				// get the part name from bitstream
				if(spec == null){
					spec = DeviceLookup.lookupPartFromPartnameOrBitstreamExitOnError(null,bitstream);
					if(spec == null){
						failAndExit("The XilinxConfigurationSpecification is null: " + bitstream.getHeader().getPartName());
					}
					if(verbose) System.out.println("Part name set from input bitstream: " + spec.getDeviceName());
				}
				
				// Create a new FPGA instance of type spec
				fpga = new FPGA(spec);
				
				// Load it up with the bitstream we just parsed
				fpga.configureBitstream(bitstream);
				fpga.getFAR().initFAR();
			} catch (BitstreamParseException e) {
				failAndExit("Could not parse bitstream.");
			} catch (IOException e) {
				failAndExit("Problem reading .bit file: " + bitInputFileName);
			}
		}
		else{
			if(verbose) System.out.println("No bitstream loaded.");
		}
	}
	
	/**
	 * Loads the XDL file if a filename has been set/loaded, loads the part name if it is null
	 */	
	private void loadXDLFile(){
		if(xdlInputFileName != null){
				Design design = new Design();
				this.design = design;		
				design.loadXDLFile(xdlInputFileName);
				if(verbose) System.out.println("Successfully loaded XDL file: " + xdlInputFileName);
				if(spec == null){
					// Get part name from bitstream
					spec = DeviceLookup.lookupPartV4V5V6(DeviceLookup.getRootDeviceName(design.getPartName()));
					if(verbose) System.out.println("Part name set from input XDL file: " + spec.getDeviceName());
				}
		
		}
		else{
			if(verbose) System.out.println("No XDL file loaded.");
		}
	}

	/**
	 * Helper method to changeBits().  Does the actual work of changing a bit
	 * in a FrameData object.
	 * @param far The FAR address of the frame to be changed
	 * @param bitNumber The bit number (0 to 1311) of the bit to be changed
	 * @param value The desired value of the bit
	 * @return true if the bit was successfully changed, false otherwise
	 */
	private boolean changeBits(int far, int bitNumber, int value){
		fpga.setFAR(far);
		if(!fpga.getFAR().validFARAddress()){
			// Invalid FAR address
			return false;
		}
		FrameData d = fpga.getFrame(far).getData();
		if(bitNumber < 0 || bitNumber > 1311){
			// Incorrect bit index
			return false;
		}
		return d.setBit(bitNumber,value);
	}
	
	/**
	 * Reads in the file specified by changeBitsInputFileName and parses it.  Its
	 * syntax for each line is [far address (hex)] [bit number (0-1311)] [value (0-1)].
	 */
	private void changeBits() {
		if(changeBitsInputFileName != null){
			BufferedReader in;
			boolean success;
			try {
				in = new BufferedReader(new FileReader(changeBitsInputFileName));
				String line = in.readLine();
				int lineNumber = 0;
				while(line != null){
					lineNumber++;
					String[] tokens = line.split(" ");
					if(tokens.length != 3){
						failAndExit("There was an error parsing file: " +
								changeBitsInputFileName +
								" associated with the --changeBits option, error on line " +
								lineNumber);
					}
					if(verbose){
						System.out.printf("FAR: 0x%x, Bit: %d, Value: %d\n",
								Integer.parseInt(tokens[0], 16), 
								Integer.parseInt(tokens[1]),
								Integer.parseInt(tokens[2]));
					}
					success = changeBits(Integer.parseInt(tokens[0], 16), 
										 Integer.parseInt(tokens[1]), 
										 Integer.parseInt(tokens[2]));
					if(!success){
						failAndExit("Failed updating bit at FAR: " + tokens[0] + ", bit: " +
								tokens[1] + " with value: " + tokens[2] + " on line " +
								lineNumber);
					}
					line = in.readLine();
				}
				in.close();
				
				// Update bitstream with changes
				bitstream.updateType2PacketWithFPGA(fpga);

				
			} 
			catch (FileNotFoundException e) {
				failAndExit("Could not find file:" + changeBitsInputFileName);	
			}
			catch (IOException e){
				failAndExit("Problem reading file:" + changeBitsInputFileName);
			}			
		}
	}

	/**
	 * Duplicates frame writes at the FAR addresses specified in the FAR file or if the file
	 * is not specified, the first 256 valid consecutive frames in the bitstream.
	 */
	private void duplicateFrames() {
		if(duplicateFrames){
			ArrayList<Integer> farAddresses;
			if(fpga != null){
				if(duplicatedFramesInputFileName != null){
					if(verbose){ 
						System.out.println("Duplicating frames from FAR file: " 
								+ duplicatedFramesInputFileName);
					}
					farAddresses = loadFARFile(duplicatedFramesInputFileName);
				}
				else{
					if(verbose) System.out.println("Duplicating first "+
							testFrameCount+" valid frame writes.");
					// Don't duplicate all frames, just the first testFrameCount frames
					farAddresses = new ArrayList<Integer>(testFrameCount);
					farAddresses.addAll(bitstream.getFARAddressList(fpga.getDeviceSpecification()).subList(0, testFrameCount));
				}
				bitstream.duplicateFrames(farAddresses, fpga);
			}
			else{
				failAndExit("Must supply bitstream to duplicate frames.");
			}
		}
	}

	/**
	 * This method will randomize the frame writes and remove the type 2 packet in the current
	 * bitstream.  
	 */
	private void randomizeFrames(){
		if(generator != null){
			if(bitstream != null && fpga != null){
				if(!bitstream.randomizeFrames(generator, fpga)){
					failAndExit("Failed to create randomized frame writes in bitstream.");
				}
				else if(verbose){
					System.out.println("Randomizing frames completed successfully.");
				}
			}
			else{
				failAndExit("Bitstream required to perform randomized frame writes.");
			}
		}
	}

	/**
	 * This will insert invalid frame writes to the bitstream.
	 */
	private void addInvalidFrames() {
		if(insertInvalidFrames){
			if(bitstream != null && fpga != null){
				ArrayList<Integer> farAddresses;
				if(invalidFramesInputFileName != null){
					if(verbose){ 
						System.out.println("Inserting invalid frames from FAR file: " 
								+ invalidFramesInputFileName);
					}
					farAddresses = loadFARFile(invalidFramesInputFileName);
				}
				else{
					if(verbose) System.out.println("Inverting first "+
							testFrameCount+" valid frame addresses and adding them.");
					// Don't duplicate all frames, just the first testFrameCount frames
					farAddresses = new ArrayList<Integer>(testFrameCount);
					ArrayList<Integer> farList = 
						bitstream.getFARAddressList(fpga.getDeviceSpecification());
					for(int i=0; i < testFrameCount; i++){
						farAddresses.add(~farList.get(i));
					}
				}
				bitstream.insertInvalidFrames(farAddresses, fpga);
			}
			else{
				failAndExit("Bitstream required to insert invalid frame writes.");
			}
		}
	}

	/**
	 * TODO This method is not yet finished.  I believe it main body should be implemented in bitstreamTools.
	 */
	private void compressBitstream() {
		if(compressBitstream && bitInputFileName != null && bitOutputFileName != null){
			failAndExit("compressBitstream() not yet implemented.");
		}
	}

	/**
	 * This method will read an ASCII text file with lines containing entries:
	 * [hex FAR value] [bit number (0-1311)] and will then 
	 */
	private void decodeBits(Design design) {
		if(decodeBitsInputFileName != null && decodeBitsOutputFileName != null){
			try {
				if(spec == null){
					failAndExit("To decode bits you must supply a part name -p, or -i (via bitstream)" +
							"or -a (via XDL file)");
				}
				XilinxToolbox toolbox = XilinxToolboxLookup.toolboxLookup(spec);
				BufferedReader in = new BufferedReader(new FileReader(decodeBitsInputFileName));
				BufferedWriter out = new BufferedWriter(new FileWriter(decodeBitsOutputFileName));
				String line = in.readLine();
				FrameAddressRegister far = new FrameAddressRegister(spec);
				int bitNumber = 0;
				int byteOffset = 0;
				int mask = 0;
				int lineNumber = 0;
				ResultSet rs = null;
				String tile;
				String deviceFamilyName = null;
				boolean isCLBSubBlockType = false;
				// Open connection to MySQL database
				db.openConnection();
				// Get the right family to access the right tables in the database
				if(spec.getDeviceFamily().equals(V4ConfigurationSpecification.V4_FAMILY_NAME)){
					deviceFamilyName = V4ConfigurationSpecification.V4_FAMILY_NAME;
				}else{
					failAndExit("Sorry "+spec.getDeviceFamily()+" is not yet implemented for decodeBits.");
				}
				
				// Loop over each entry in the file
				while(line != null){
					lineNumber++;
					String[] tokens = line.split(" ");
					
					if(tokens.length != 2){
						System.out.println("There was an error parsing file: " +
								decodeBitsInputFileName +" associated with the --decodeBit option, " +
								"error on line " + lineNumber);
						System.out.println("Make sure format is in the form: [hex FAR value] " +
								"[decimal bit index (0-1311)]");
						System.out.println("Each line should be its own entry.");
						System.exit(1);
					}
					
					if(this.verbose){ 
						System.out.print("Decoding FAR 0x" + tokens[0] + ", bit " + tokens[1] + " ");
					}
					
					try {
						far.setFAR(Integer.parseInt(tokens[0], 16));
						bitNumber = Integer.parseInt(tokens[1], 10);
						if (bitNumber > (spec.getFrameSize()*32)-1 || bitNumber < 0){
							throw new NumberFormatException();
						}
					} catch (NumberFormatException e) {
						System.out.println("Error parsing file: " + decodeBitsInputFileName +
								" on line " + lineNumber + ".");
						System.out.println("Make sure format is in the form: [hex FAR value] " +
								"[decimal bit index, ex: 0-1311]");
						System.out.println("Each line should be its own entry.");
					}
					
					if(this.verbose){
						// TOP/BOTTOM, BLOCK_TYPE, BLOCK_ROW, BLOCK_COL, MINOR
						System.out.printf("FAR: %s %s row=%02d col=%02d minor=%02d\n",
							AbstractConfigurationSpecification.getTopBottom(far.getTopBottom()),
							AbstractConfigurationSpecification.getBlockSubtype(spec, far.getBlockType(), far.getColumn()),
							far.getRow(),
							far.getColumn(),
							far.getMinor());
					}
					
					byteOffset = toolbox.getByteOffset(far.getTopBottom(), bitNumber,toolbox.isClkColumn(spec, far));
					mask = toolbox.getMask(far.getTopBottom(), bitNumber, toolbox.isClkColumn(spec, far));
					
					// Print line
					out.write("0x" + Integer.toHexString(far.getAddress()) + " " + bitNumber +
							" (" + far.getMinor() + " " + byteOffset + " " + mask + ")" + "\n");

					// Is this a CLB Block?
					if(spec.getBlockSubtype(spec, far).equals(V4ConfigurationSpecification.CLB)){
						isCLBSubBlockType = true;
					}
					else{
						isCLBSubBlockType = false;
					}
					
					// This is likely a CLB logic bit
					if(far.getMinor() > 17 && isCLBSubBlockType){
						rs = db.makeLogicQuery(deviceFamilyName, far.getMinor(), byteOffset, mask);
						tile = "CLB_X" + toolbox.getTileXCoordinate(spec,far) + "Y" + toolbox.getTileYCoordinate(spec, far, bitNumber); 
						if(verbose) System.out.println("  TILE: " + tile + " MINOR: " + far.getMinor() + " BYTEOFFSET: " + byteOffset + " MASK: " + mask);
						try {
							while(rs.next()){							
								out.write(" " + tile + " " + rs.getString(1) + " " + rs.getString(2) + " "+ rs.getString(3) + " "+ rs.getString(10) + "\n");
							}
						} 
						catch (SQLException e) {
							db.printSQLExceptionMessages(e);
							e.printStackTrace();
						}
					}

					/*
					if(!isCLBSubBlockType && 
							toolbox.getBlockSubtype(spec, far).equals(V4ConfigurationSpecification.CLK)){
						String intType = "\"clk_hrow\"";
						rs = db.makeRoutingQuery(deviceFamilyName, intType, far.getMinor(), byteOffset, mask);
						tile = "EMPTY_CLK_X" + toolbox.getTileXCoordinate(spec, far) + "Y" + toolbox.getTileYCoordinate(spec, far, bitNumber);
						if(verbose){ 
							System.out.println("  TILE: " + tile + " MINOR: " + far.getMinor() + " BYTEOFFSET: " +
									byteOffset + " MASK: " + mask);
						}
						try {
							while(rs.next()){
								out.write(" " + tile + " " + rs.getString(1) + " " + rs.getString(2) +
										" "+ rs.getString(3) + " " + rs.getString(4) + "\n");
							}
						} 
						catch (SQLException e) {
							db.printSQLExceptionMessages(e);
						}
					}*/
					
					// Interconnect (INT/INT_SO) block
					if(far.getMinor() <= 18){
						String intType = isCLBSubBlockType ? "\"int\",\"clb\"" : "\"int_so\"";
						rs = db.makeRoutingQuery(deviceFamilyName, intType, far.getMinor(), byteOffset, mask);
						tile = "INT_X" + toolbox.getTileXCoordinate(spec, far) + "Y" + toolbox.getTileYCoordinate(spec, far, bitNumber);
						if(verbose){ 
							System.out.println("  TILE: " + tile + " MINOR: " + far.getMinor() + " BYTEOFFSET: " +
									byteOffset + " MASK: " + mask);
						}
						try {
							while(rs.next()){
								out.write(" " + tile + " " + rs.getString(1) + " " + rs.getString(2) +
										" "+ rs.getString(3) + " " + rs.getString(4) + "\n");
							}
						} 
						catch (SQLException e) {
							db.printSQLExceptionMessages(e);
						}					
					}
										
					// Go to the next line
					line = in.readLine();
				}
				
				// Close everything
				in.close();
				out.close();
				db.closeConnection();
			}
			catch (FileNotFoundException e) {
				System.out.println("Could not find file:" + decodeBitsInputFileName);	
			}
			catch (IOException e){
				System.out.println("Problem reading file:" + decodeBitsInputFileName);
			}
		}
	}

	private void extractXDL(Design design) {
		if(spec != null && fpga != null && extractXDL && xdlOutputFileName != null){
			ArrayList<PIP> pips;
			XilinxToolbox toolbox = XilinxToolboxLookup.toolboxLookup(spec);
			
			
			// TODO Work-In-Progress
			// Extract the PIPs
			pips = toolbox.extractPIPs(fpga);
			
			//DeviceFilesCreator.saveToFile(pips, "testPIPs.dat");
			pips = (ArrayList<PIP>) FileTools.loadFromFile("testPIPs.dat");
			
			// Create Nets
			BitstreamGizmoUtils bgu = new BitstreamGizmoUtils();
			design = bgu.createNetsFromPIPs(pips, bitstream, extractedSpeedGrade);

		}
		else if(extractXDL && (spec == null || fpga == null)){
			failAndExit("To decode a bitstream into XDL, you must supply a bitstream (-i option)");
		}
		else if(extractXDL && xdlOutputFileName == null){
			failAndExit("To decode a bitstream into XDL, you must supply a bitstream (-e option)");
		}
		
	}

	private void findBits() {
		// TODO Auto-generated method stub
		
	}

	private void verifyDatabase() throws SQLException {
		if(verifyDatabase){
			if(this.design != null && this.fpga != null){
				VerifyDB verifyDB = new VerifyDB(fpga, design);
				verifyDB.VerifyBitstream();
			}
			else{
				failAndExit("Error: Need both XDL and corresponding bitstream to verify database.");
			}
		}
	}
	
	/**
	 * Creates a file with a consecutive list of all valid FAR addresses for the part
	 * specified by the variable fpga. All addresses are in hexadecimal
	 */
	private void createFARFile() {
		if(farOutputFileName != null){
			if(spec != null){
				BufferedWriter bw;
				try {
					bw = new BufferedWriter(new FileWriter(farOutputFileName));				
					for(Integer far : bitstream.getFARAddressList(spec)){
						bw.write(BitstreamUtils.toHexString(far) + newLine);
					}
					bw.close();
					
				} catch (IOException e) {
 					failAndExit("Error writing FAR file: " + farOutputFileName);
				}
			}
			else{
				failAndExit("No part name is specified, cannot create FAR file. Try -p option.");
			}
		}
	}	
	
	private ArrayList<Integer> loadFARFile(String fileName){
		ArrayList<Integer> farAddresses = new ArrayList<Integer>();
		try {
			String line;
			BufferedReader in = 
				new BufferedReader(new FileReader(fileName));
			line = in.readLine(); 
			while(line !=null){
				farAddresses.add(Integer.parseInt(line, 16));
			}
			in.close();
			
		} catch (FileNotFoundException e) {
			failAndExit("Could not find FAR file: " + fileName);
		} catch (IOException e) {
			failAndExit("Trouble reading FAR file: " + fileName);
		}
		return farAddresses;
	}
	
	/**
	 * Helper method to compareBitstreams()
	 * @param d1 Frame 1 data
	 * @param d2 Frame 2 data
	 * @param far Current FAR object for the frames
	 */
	private int compareFrame(FrameData d1, FrameData d2, FrameAddressRegister far, Design design){
		int diff;
		int mask = 0x1;
		int frameBitNumber;
		int numOfDifferences = 0;
		XilinxToolbox xTools = XilinxToolboxLookup.toolboxLookup(spec);
		for(int i=0; i < d1.size(); i++){
			if(d1.get(i) != d2.get(i)){
				diff = d1.get(i) ^ d2.get(i);
				mask = 0x1;
				for(int j=0; j < 32; j++){
					if((diff & mask) == mask){
						frameBitNumber = i*32;
						frameBitNumber += 31-j;
						if((!ignoreECCBits) || (ignoreECCBits && !(frameBitNumber > 659 && frameBitNumber < 672))){
							numOfDifferences++;
							if(message.length() != 0){
								System.out.print(
										"FAR=" + Integer.toHexString(far.getAddress()) +
										" Bit=" + frameBitNumber +
										" Msg=" + message +
										" Minor=" + far.getMinor() + 
										" ByteOffset="+xTools.getByteOffset(far.getTopBottom(), frameBitNumber, xTools.isClkColumn(spec, far))+
										" Mask="+xTools.getMask(far.getTopBottom(), frameBitNumber, xTools.isClkColumn(spec, far))+
										" Differs: " + d1.getBit(frameBitNumber) + " " +
										d2.getBit(frameBitNumber) + "\n");															
							}
							else{
								System.out.println(
										far.toString(1) + 
										", Bit=" + frameBitNumber +
										" byteOffset="+xTools.getByteOffset(far.getTopBottom(), frameBitNumber, xTools.isClkColumn(spec, far))+
										" mask="+xTools.getMask(far.getTopBottom(), frameBitNumber, xTools.isClkColumn(spec, far))+
										" differs: " + d1.getBit(frameBitNumber) + " " +
										d2.getBit(frameBitNumber));							
							}
						}
					}
					mask = mask << 1;
				}
			}
		}
		return numOfDifferences;
	}
	
	/**
	 * Compares two bitstreams.  Prints out the differences.
	 */
	private void compareBitstreams(){
		if(bitCompareFileName != null){
			if(bitInputFileName != null){
				try {
					if(verbose) System.out.println("Comparing bitstreams...");
					int numOfDifferences = 0;
					Bitstream compareMe = BitstreamParser.parseBitstream(bitCompareFileName);
					FPGA fpga2 = new FPGA(DeviceLookup.lookupPartFromPartnameOrBitstreamExitOnError(null,compareMe));
					fpga2.configureBitstream(compareMe);
					if(!fpga2.getDeviceSpecification().getDeviceName().equals(spec.getDeviceName())){
						failAndExit("Bitstream parts differ, cannot compare: " 
								+ bitInputFileName + " and " + bitCompareFileName+".");
					}
					FrameAddressRegister far = fpga2.getFAR();
					far.initFAR();
					System.out.println("=== BitstreamGizmo Bitstream Diff between " 
							+ bitInputFileName + " and " + bitCompareFileName + " ===");
					
					// Check each frame sequentially
					Frame frame1;
					Frame frame2;
					FrameData data1;
					FrameData data2;
					do{
						frame1 = fpga.getFrame(far);
						frame2 = fpga2.getFrame(far);
						data1 = frame1.getData();
						data2 = frame2.getData();
						// Check if both frames were configured by the bitstream
						if(frame1.isConfigured() != frame2.isConfigured()){
							System.out.println(far.toString(1) + " not both configured: " +
									frame1.isConfigured() + " " + frame2.isConfigured());
						}
						// Check the actual bits in the frame
						else if(!data1.isEqual(data2)){
							// Make sure frame sizes are the same
							if(data1.size() != data2.size()){
								System.out.println(far.toString(1) + " sizes do not match: " +
										data1.size() + " " + data2.size());
							}
							numOfDifferences += compareFrame(data1,data2,far, design);
						}
						
					}while(far.incrementFAR());
					
					System.out.println("=== BitstreamGizmo Bitstream Diff end, "
							+ numOfDifferences +" differences found ===");
					
				} catch (BitstreamParseException e) {
					failAndExit("Could not parse comparison bitstream: " + bitCompareFileName);
				} catch (IOException e) {
					failAndExit("Could not read from comparison bitstream: " + bitCompareFileName);
				}
			}
			else{
				failAndExit("Cannot diff bitstreams, no input bitstream given. Try -i option.");
			}
		}
	}
	
	/**
	 * Saves the current xdlDesign object to an XDL file
	 */
	private void saveXDLFile() {
		if(xdlOutputFileName != null){
		    design.saveXDLFile(xdlOutputFileName);
			if(verbose) System.out.println("Successfully saved XDL design: " + xdlOutputFileName);
		}
	}


	/**
	 * Saves the current bitstream to a file
	 */
	private void saveBitstream() {
		if(bitOutputFileName != null){
			try {
				BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(bitOutputFileName));
				bitstream.outputBitstream(bos);
				bos.close();
			} catch (FileNotFoundException e) {
				failAndExit("Error writing to file: " + bitOutputFileName);
			} catch (IOException e) {
				failAndExit("Error writing to file: " + bitOutputFileName);
			}
			if(verbose) System.out.println("Successfully saved bitstream: " + bitOutputFileName);
		}
	}
	

	/**
	 * This function will save the current bitstream to an ASCII MCS PROM file
	 */
	private void saveBitstreamToMCS() {
		if(mcsOutputFileName != null){
			try {
				BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(mcsOutputFileName));
				bitstream.writeBitstreamToMCS(bos);
				bos.close();
			} catch (FileNotFoundException e) {
				failAndExit("Error writing to file: " + mcsOutputFileName);
			} catch (IOException e) {
				failAndExit("Error writing to file: " + mcsOutputFileName);
			}
			if(verbose) System.out.println("Successfully saved bitstream to MCS file: " + mcsOutputFileName);
		}
	}
	
	/**
	 * This function will save the current bitstream to an XML file
	 */
	private void saveBitstreamToXML() {
		if(xmlOutputFileName != null){
			try {
				BufferedWriter bw = new BufferedWriter(new FileWriter(xmlOutputFileName));
				bw.write(bitstream.toXMLString());
				bw.close();
			} catch (FileNotFoundException e) {
				failAndExit("Error writing to file: " + xmlOutputFileName);
			} catch (IOException e) {
				failAndExit("Error writing to file: " + xmlOutputFileName);
			}
			if(verbose) System.out.println("Successfully saved bitstream to XML file: " + xmlOutputFileName);
		}
	}

	/**
	 * This function will save the current bitstream to an ASCII text file
	 */
	private void saveBitstreamToTXT() {
		if(txtOutputFileName != null){
			try {
				BufferedWriter bw = new BufferedWriter(new FileWriter(txtOutputFileName));
				bw.write(bitstream.toString());
				bw.close();
			} catch (FileNotFoundException e) {
				failAndExit("Error writing to file: " + txtOutputFileName);
			} catch (IOException e) {
				failAndExit("Error writing to file: " + txtOutputFileName);
			}
			if(verbose) System.out.println("Successfully saved bitstream to TXT file: " + txtOutputFileName);
		}
	}

	
	/**
	 * Easy way to bail out in the case of failure
	 */
	protected static void failAndExit(String s){
		System.out.println(s);
		System.exit(1);
	}
	
	/**
	 * Where it all starts
	 * @param args See -? option 
	 */
	public static void main(String[] args) throws SQLException{
		// Initialize class, parse arguments
		BitstreamGizmo bg = new BitstreamGizmo();
		bg.configureParser();
		bg.parseCommandLine(args);

		/*========== Input File Procedures ==========*/
		// Load Bitstream, if given 
		bg.loadBitstream();
		// Load XDL file, if given
		bg.loadXDLFile();
		/*===========================================*/
		
		/*========= Bitstream Manipulations =========*/
		// Compare two bitstreams, print out differences
		bg.duplicateFrames();
		// Randomize frames in the bitstream
		bg.randomizeFrames();
		// Add bad (invalid) frames to the bitstream
		bg.addInvalidFrames();
		/*===========================================*/		
		
		/*========= FPGA Manipulations ==============*/
		// Change Bits in bitstream based on file
		bg.changeBits();		
		// Compress bitstream via Ben Sellers method
		bg.compressBitstream();
		/*===========================================*/
		
		/*============ Other Procedures =============*/
		// Decode bits from the bitstream
		bg.decodeBits(bg.design);
		// Decode XDL from Bitstream file
		bg.extractXDL(bg.design);
		// Find a particular bit in bitstream set to 1 or 0 
		bg.findBits();
		// Verify Database from XDL file and bitstream given
		bg.verifyDatabase();
		// Compare two bitstreams, print out differences
		bg.compareBitstreams();
		/*===========================================*/
		
		
		/*========= File Output Procedures ==========*/
		// Create a listing of valid FAR addresses in hex
		bg.createFARFile();
		// Save bitstream to a file
		bg.saveBitstream();
		// Save XDL design to a file
		bg.saveXDLFile();
		// Save MCS (PROM) file from current bitstream
		bg.saveBitstreamToMCS();
		// Save XML file from current bitstream
		bg.saveBitstreamToXML();
		// Save TXT file from current bitstream
		bg.saveBitstreamToTXT();
		/*===========================================*/
		
		// The End 
		if(bg.verbose) {
			Date endTime = new Date();
			System.out.println("Start Time:   " + startTime);
			System.out.println("End Time:     " + endTime);
		}
	}
}

package edu.byu.ece.gremlinTools.bitstream;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import edu.byu.ece.rapidSmith.bitstreamTools.bitstream.Bitstream;
import edu.byu.ece.rapidSmith.bitstreamTools.bitstream.BitstreamException;
import edu.byu.ece.rapidSmith.bitstreamTools.bitstream.BitstreamHeader;
import edu.byu.ece.rapidSmith.bitstreamTools.bitstream.DummySyncData;
import edu.byu.ece.rapidSmith.bitstreamTools.bitstream.Packet;
import edu.byu.ece.rapidSmith.bitstreamTools.bitstream.PacketList;
import edu.byu.ece.rapidSmith.bitstreamTools.bitstream.PacketListCRC;
import edu.byu.ece.rapidSmith.bitstreamTools.bitstream.PacketOpcode;
import edu.byu.ece.rapidSmith.bitstreamTools.bitstream.PacketType;
import edu.byu.ece.rapidSmith.bitstreamTools.bitstream.PacketUtils;
import edu.byu.ece.rapidSmith.bitstreamTools.bitstream.RegisterType;
import edu.byu.ece.rapidSmith.bitstreamTools.configuration.FPGA;
import edu.byu.ece.rapidSmith.bitstreamTools.configuration.Frame;
import edu.byu.ece.rapidSmith.bitstreamTools.configuration.FrameAddressRegister;
import edu.byu.ece.rapidSmith.bitstreamTools.configurationSpecification.XilinxConfigurationSpecification;

public class GremlinBitstream extends Bitstream{

    /**
     * Create a bitstream with the given header, dummy word/sync data, and packets.
     * 
     * @param header
     * @param dummySyncData
     * @param packets
     */
	public GremlinBitstream(BitstreamHeader header, DummySyncData dummySyncData, PacketList packets){
		super(header, dummySyncData, packets);		
	}
	
    /**
     * Create a bitstream with the given dummy word/sync data and packets.
     * 
     * @param dummySyncData
     * @param packets
     */
    public GremlinBitstream(DummySyncData dummySyncData, PacketList packets){
    	super(dummySyncData, packets);
    }
    
    /**
     * Constructor that upgrades a regular bitstream to a GremlinBitstream
     * @param b The regular bitstream
     */
    public GremlinBitstream(Bitstream b){
    	super(b.getHeader(),b.getDummySyncData(),b.getPackets());
    }
    
    public boolean updateType2PacketWithFPGA(FPGA fpga){
    	PacketListCRC newPacketList = new PacketListCRC();
    	List<Integer> allData = new ArrayList<Integer>();
    	Iterator<Packet> itr;
    	Packet tmp;
    	if(fpga == null){return false;}

    	// Get all the data from the FPGA
    	for(Frame frame : fpga.getAllFrames()){
    		allData.addAll(frame.getData().getAllFrameWords());
    	}
		
    	// Get to Packet Type 2 
    	itr = advanceIteratorToPacketInsertionPoint(newPacketList);
    	while(itr.hasNext()){
    		tmp = itr.next();
    		if(tmp.getPacketType().equals(PacketType.TWO)){
    			try {
    				// Add the new Type 2 packet
					newPacketList.add(Packet.buildMultiWordType2Packet(PacketOpcode.WRITE, allData));
					// Add the rest of the packets, update CRC regs, and packetList
					finishTransferingLastPackets(itr, newPacketList);
					break;
				} catch (BitstreamException e) {
					return false;
				}
    		}
    		else{
    			newPacketList.add(tmp);
    		}
    	}
    	    	
    	return true;
    }
    
    /**
     * This method will frame writes by creating type 1 packets.  It will duplicate all the 
     * addresses listed in the farAddresses list. Currently, the duplicated frames are placed 
     * before the large type two packet and their data is the same as the data in the 
     * original bitstream.
     * @param farAddresses List of FAR addresses to duplicate.
     * @param fpga The configured FPGA with data to use for the duplicated packets.
     * @return true if the operation was successful, false otherwise.
     */
	public boolean duplicateFrames(ArrayList<Integer> farAddresses, FPGA fpga){		
		ArrayList<Integer> emptyFrameData = new ArrayList<Integer>();
		List<Integer> frameData;
		PacketListCRC newPacketList = new PacketListCRC();
		Iterator<Packet> itr;
		
		if(farAddresses== null || fpga==null){return false;}
		
		// Create empty frame data
		emptyFrameData = createEmptyFrameData(fpga.getDeviceSpecification().getFrameSize());
		
		// Copy over the initial packets, get iterator pointing to insertion point
		itr = advanceIteratorToPacketInsertionPoint(newPacketList);

		// Add all the duplicated frames before the big packet
		for(int far : farAddresses){
			frameData = new ArrayList<Integer>(emptyFrameData.size()*2);
			// Write to the FAR the new address
			newPacketList.add(Packet.buildOneWordPacket(PacketOpcode.WRITE, RegisterType.FAR, far));
			// Add a NOP
			newPacketList.add(PacketUtils.NOP_PACKET);
			// Add the FDRI Packet (the data and dummy frame)
			frameData.addAll(fpga.getFrame(far).getData().getAllFrameWords());
			frameData.addAll(emptyFrameData);
			try {
				newPacketList.add(Packet.buildMultiWordType1Packet(PacketOpcode.WRITE, RegisterType.FDRI, frameData));
			} catch (BitstreamException e) {
				return false;
			}
		}
		// Reset the FAR to 0
		newPacketList.add(Packet.buildOneWordPacket(PacketOpcode.WRITE, RegisterType.FAR, 0));
		
		// Create a new type 1 packet just before the type 2
		newPacketList.add(Packet.buildZeroWordPacket(PacketOpcode.WRITE,RegisterType.FDRI));

		// Finish the rest
		finishTransferingLastPackets(itr, newPacketList);
		
		return true;
	}
	
	public boolean insertInvalidFrames(ArrayList<Integer> farAddresses, FPGA fpga){		
		ArrayList<Integer> emptyFrameData = new ArrayList<Integer>();
		List<Integer> frameData;
		PacketListCRC newPacketList = new PacketListCRC();
		Iterator<Packet> itr;
		
		if(farAddresses== null || fpga==null){return false;}
		
		// Create empty frame data
		emptyFrameData = createEmptyFrameData(fpga.getDeviceSpecification().getFrameSize());
		
		// Copy over the initial packets, get iterator pointing to insertion point
		itr = advanceIteratorToPacketInsertionPoint(newPacketList);

		// Add all the duplicated frames before the big packet
		for(int far : farAddresses){
			frameData = new ArrayList<Integer>(emptyFrameData.size()*2);
			// Write to the FAR the new address
			newPacketList.add(Packet.buildOneWordPacket(PacketOpcode.WRITE, RegisterType.FAR, far));
			// Add a NOP
			newPacketList.add(PacketUtils.NOP_PACKET);
			// Add the FDRI Packet (the data and dummy frame)
			frameData.addAll(emptyFrameData);
			frameData.addAll(emptyFrameData);
			try {
				newPacketList.add(Packet.buildMultiWordType1Packet(PacketOpcode.WRITE, RegisterType.FDRI, frameData));
			} catch (BitstreamException e) {
				return false;
			}
		}
		// Reset the FAR to 0
		newPacketList.add(Packet.buildOneWordPacket(PacketOpcode.WRITE, RegisterType.FAR, 0));
		
		// Create a new type 1 packet just before the type 2
		newPacketList.add(Packet.buildZeroWordPacket(PacketOpcode.WRITE,RegisterType.FDRI));

		// Finish the rest
		finishTransferingLastPackets(itr, newPacketList);
		
		return true;
	}
	
	
	/**
	 * This function will randomize the frame write sequence by generating a new packet 
	 * for every frame. This function also removes any frames that write all zeros as 
	 * writing the frames in random order requires the bitstream to more than double in size.
	 * @param generator The random number generator instance to be used for the randomization.
	 * @return true if the operation completed successfully, false otherwise.
	 */
	public boolean randomizeFrames(Random generator, FPGA fpga){
		ArrayList<Integer> emptyFrameData;
		ArrayList<Integer> farAddresses;
		List<Integer> frameData;
		Iterator<Packet> itr;
		PacketListCRC newPacketList = new PacketListCRC();
		
		if(generator== null || fpga==null){return false;}
		
		// Create empty frame data
		emptyFrameData = createEmptyFrameData(fpga.getDeviceSpecification().getFrameSize());
		
		// Get a list of all valid FAR addresses
		farAddresses = getFARAddressList(fpga.getDeviceSpecification());
		// Randomize them
		farAddresses = randomizeFARList(farAddresses, generator);
		
		// Copy over the initial packets, get iterator pointing to insertion point 
		itr = advanceIteratorToPacketInsertionPoint(newPacketList);

		// Add all the randomized frames before the big packet
		for(int far : farAddresses){
			frameData = new ArrayList<Integer>(emptyFrameData.size()*2);
			// Write to the FAR the new address
			newPacketList.add(Packet.buildOneWordPacket(PacketOpcode.WRITE, RegisterType.FAR, far));
			// Add a NOP
			newPacketList.add(PacketUtils.NOP_PACKET);
			// Add the FDRI Packet (the data and dummy frame)
			frameData.addAll(fpga.getFrame(far).getData().getAllFrameWords());
			frameData.addAll(emptyFrameData);
			try {
				newPacketList.add(Packet.buildMultiWordType1Packet(PacketOpcode.WRITE, RegisterType.FDRI, frameData));
			} catch (BitstreamException e) {
				return false;
			}
		}
		
		if(!itr.next().getPacketType().equals(PacketType.TWO)){
			// this should have been the type 2 packet
			return false;
		}

		// Finish the rest
		finishTransferingLastPackets(itr, newPacketList);
		
		return true;
	}
	
	/**
	 * Helper method used by randomizeFrames and duplicateFrames that takes the packet iterator
	 * and puts the remaining packets into the new bitstream and updates the bitstream with the 
	 * newPacketList
	 * @param itr The iterator which has the remaining packets to be added
	 * @param newPacketList The new list of packets to replace the existing packet list
	 */
	private void finishTransferingLastPackets(Iterator<Packet> itr, PacketListCRC newPacketList){
		Packet tmp;
		
		// Add the rest of the list, and update CRC packets appropriately
		while(itr.hasNext()){
			tmp = itr.next();
			if(tmp.getRegType() != null && tmp.getRegType().equals(RegisterType.CRC)){
				newPacketList.addCRCWritePacket();
			}
			else{
				newPacketList.add(tmp);
			}
		}
		
		// Finally, update the packet list with the newly create list
		_packets = newPacketList;
	}
	
	/**
	 * Creates an ArrayList of size frameSize filled with zeros to be used as a buffer
	 * frame for the packet writes.
	 * @param frameSize The number of 32-bit words in the frame
	 * @return An ArrayList of size frameSize filled with zeros
	 */
	private ArrayList<Integer> createEmptyFrameData(Integer frameSize){
		ArrayList<Integer> emptyFrame = new ArrayList<Integer>(frameSize);
		// Create a dummy frame 
		for(int i=0; i < frameSize; i++){
			emptyFrame.add(0);
		}
		return emptyFrame;
	}
	
	/**
	 * Helper function to randomizeFrames and duplicateFrames.  It copies all of the initial packets
	 * before the large type 2 packet to newPacketList and returns an iterator pointing to the type 1
	 * packet just before the large type 2 packet.
	 * @param newPacketList The packet list to add initial packets to
	 * @return An iterator pointing to the packet just before the type 2 packet, return null if there
	 * was an error.
	 */
	private Iterator<Packet> advanceIteratorToPacketInsertionPoint(PacketListCRC newPacketList){
		Packet tmp;
		
		// Search for the insertion point (after large Type II packet)
		Iterator<Packet> itr = _packets.iterator();
		tmp = itr.next();
		while(itr.hasNext() && !tmp.getRegType().equals(RegisterType.FDRI)){
			newPacketList.add(tmp);
			tmp = itr.next();
		}
		
		if(!tmp.getRegType().equals(RegisterType.FDRI)){
			// This bitstream is different, can't insert random frame writes
			return null;
		}
		
		if(tmp == null  || !itr.hasNext() || !tmp.getPacketType().equals(PacketType.ONE)){
			// This bitstream is different, can't insert random frame writes
			return null;
		}
		return itr;
	}
	
	
	/**
	 * Creates a complete sequential list of valid FAR addresses for the part specified by spec.
	 * @param spec The part to use to create the FAR list.
	 * @return The FAR list, null if an error occurred.
	 */
	public ArrayList<Integer> getFARAddressList(XilinxConfigurationSpecification spec){
		if(spec != null){
			ArrayList<Integer> addresses = new ArrayList<Integer>();
			FrameAddressRegister far = new FrameAddressRegister(spec);
			if(far==null) return null;
			far.initFAR();
			do{
				addresses.add(far.getAddress());
			}while(far.incrementFAR());
			return addresses;
		}
		return null;		
	}
	
	/**
	 * Simply randomizes the order of the entries in far List.
	 * @param farAddresses The list to randomize.
	 * @param generator The random number generator to use.
	 * @return The randomized list.
	 */
	private ArrayList<Integer> randomizeFARList(ArrayList<Integer> farAddresses, Random generator){
		int tmp0, tmp1;
		int random0, random1;
		for(int i=0; i < farAddresses.size(); i++){
			random0 = generator.nextInt(farAddresses.size());
			tmp0 = farAddresses.get(random0);
			
			random1 = generator.nextInt(farAddresses.size());
			tmp1 = farAddresses.get(random1);
			
			farAddresses.set(random0, tmp1);
			farAddresses.set(random1, tmp0);
		}
		return farAddresses;
	}
}

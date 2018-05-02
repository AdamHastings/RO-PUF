/*
 * Copyright (c) 2010 Brigham Young University
 * 
 * This file is part of the BYU RapidSmith Tools.
 * 
 * BYU RapidSmith Tools is free software: you may redistribute it 
 * and/or modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 2 of 
 * the License, or (at your option) any later version.
 * 
 * BYU RapidSmith Tools is distributed in the hope that it will be 
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 * 
 * A copy of the GNU General Public License is included with the BYU 
 * RapidSmith Tools. It can be found at doc/gpl2.txt. You may also 
 * get a copy of the license at <http://www.gnu.org/licenses/>.
 * 
 */
package edu.byu.ece.gremlinTools.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;

import edu.byu.ece.rapidSmith.design.Attribute;
import edu.byu.ece.rapidSmith.design.Design;
import edu.byu.ece.rapidSmith.design.Instance;
import edu.byu.ece.rapidSmith.design.Net;
import edu.byu.ece.rapidSmith.design.Pin;
import edu.byu.ece.rapidSmith.device.PrimitiveType;
import edu.byu.ece.rapidSmith.util.FileConverter;

public class ClockDisabler {

	private String xdlFileName = "clockStopper.xdl"; 
	private Design design;
	private String ncdOutputFileName;
	private ArrayList<SliceChange> slicesChanged;
	
	public ClockDisabler(){
		slicesChanged = new ArrayList<SliceChange>();
	}
	
	private void locateAndChangeLUTs(){
		ArrayList<Instance> dcms = new ArrayList<Instance>();

		// Get a list of all used DCMs
		for(Instance inst : this.design.getInstances()){
			if(inst.getType().equals(PrimitiveType.DCM_ADV)){
				if(!inst.getName().contains("XIL_ML_UNUSED_DCM_")){
					dcms.add(inst);
				}
			}
		}
		
		if(dcms.size() == 0){
			System.out.println("Error, no DCMs found. Please check with Chris Lavin at BYU for help.");
			System.exit(1);
		}
		
		// We need to order the DCMs, such that if there is a chain, that we can 
		// flip the bits in the proper order
		/* This code will not always work */
		/*if(dcms.size() > 1){ 
			for(XDL_Instance inst : dcms){
				for(XDL_Net net : inst.getNetList()){
					for(XDL_Pin pin : net.getPins()){
						if(pin.getPinName().equals("CLKIN")){
							if(net.getPins().getSource().getReference().getType().equals(XDL_InstanceType.IOB)){
								System.out.println("Found a DCM sourced by an IOB.");
							}
						}
					}
				}
			}
		}*/
		
		// Find the LUTs driving the RST ports of the DCMs
		for(Instance inst : dcms){
			
			for(Net net : inst.getNetList()){
				for(Pin pin : net.getPins()){
					if(pin.getName().equals("RST")){
						
						for(Attribute attr : net.getSource().getInstance().getAttributes()){
							if(net.getSource().getName().equals("Y") && attr.getPhysicalName().equals("G")){
								slicesChanged.add(new SliceChange(inst,net.getSource().getInstance(),"G"));
								attr.setValue("1");
							}
							if(net.getSource().getName().equals("X") && attr.getPhysicalName().equals("F")){
								slicesChanged.add(new SliceChange(inst,net.getSource().getInstance(),"F"));
								attr.setValue("D=1");
							
							}
						}
					}
				}
			}
		}
	}


	
	private int getAbsoluteBitNumber(int tileYIndex, int byteValue, int mask, int topBottom){
		int[][] tileLUT = {{14,15,8,9,18,19,12,13,16,17}, // Even tiles byte offset
				{0,1,2,3,4,5,6,7,10,11}}; // odd tiles byte offset

		int[] clbLUT = {144,144,124,124,104,104,84,84,60,60,40,40,20,20,0,0};
		int byteNumber = clbLUT[tileYIndex % 16] + tileLUT[tileYIndex % 2][byteValue];
		int bitNumber;
		
		if(topBottom == 0){ //This bit is in the top, we must reverse the bits
			bitNumber = byteNumber*8 + (Integer.numberOfTrailingZeros(mask));
			bitNumber = 1311 - bitNumber;
		}
		else{
			bitNumber = byteNumber*8 + (7 - Integer.numberOfTrailingZeros(mask));
		}
		return bitNumber;
	}
	
	private void writeTextFile(String fileName){	 
		int[] bramIndicies = {8,29,39,60,82,102,113,134};
		int numRows = 4;
		int[] slice_g_y1_bytes = {1,2,2,2,2,2,2,2,2,3,3,3,3,3,3,3};
		int[] slice_g_y0_bytes = {4,5,5,5,5,5,5,5,5,6,6,6,6,6,6,6};
		int[] slice_g_masks = {128,1,2,4,8,16,32,64,128,1,2,4,8,16,32,64};

		int[] slice_f_y1_bytes = {0,0,0,0,0,0,0,0,7,7,7,7,7,7,7,7};
		int[] slice_f_y0_bytes = {8,8,8,8,8,8,8,8,9,9,9,9,9,9,9,9};
		int[] slice_f_masks = {1,2,4,8,16,32,64,128,1,2,4,8,16,32,64,128};
		try {
			BufferedWriter buffer = new BufferedWriter(new FileWriter(fileName));
			buffer.write("# Number of SLICE LUTs Changed\n");
			buffer.write(this.slicesChanged.size() + "\n");
			for(SliceChange sc : this.slicesChanged){
				int tileCol = sc.sliceInst.getTile().getTileXCoordinate();
				int tileRow = sc.sliceInst.getTile().getTileYCoordinate();
				int brams = 0;
				int topBottom = 0;
				int configRow = 0;
				int configCol = 0;
				int far = 0;
				int minorFrame = 0;
				for(int i : bramIndicies){
					if(tileCol > i){
						brams++;
					}
				}
				configCol = tileCol - brams;
				if((tileRow/16) < numRows){
					topBottom = 1;
					configRow = (numRows-1) - (tileRow/16);
				}
				else{
					configRow = (tileRow/16) - numRows;
				}
				if(configCol > 29){
					configCol--;
				}
				
				if(sc.sliceInst.getInstanceX() % 2 == 0){ // SLICEL
					minorFrame = 21;
				}
				else{ // SLICEM
					minorFrame = 19;
				}
				far = far | (topBottom << 22) | (configRow << 14) | (configCol << 6) | minorFrame;
				String farAddr = Integer.toHexString(far);
				while(farAddr.length() < 8){
					farAddr = "0" + farAddr;
				}
				buffer.write("# DCM AFFECTED: " + sc.dcmInst.getPrimitiveSiteName() + ", BY THIS SLICE: " + sc.sliceInst.getPrimitiveSiteName() + ", LUT: " + sc.lut + "\n");
				buffer.write(farAddr + "\n");
				int[] masks;
				int[] bytes;
				if(sc.lut.equals("G")){
					masks = slice_g_masks;
					if(sc.sliceInst.getInstanceY() % 2 == 1){
						bytes = slice_g_y1_bytes;
					}
					else{
						bytes = slice_g_y0_bytes;
					}
				}
				else{ // LUT = F
					masks = slice_f_masks;
					if(sc.sliceInst.getInstanceY() % 2 == 1){
						bytes = slice_f_y1_bytes;
					}
					else{
						bytes = slice_f_y0_bytes;
					}
				}
				
				for(int i=0; i < 16; i++){
					buffer.write(this.getAbsoluteBitNumber(sc.sliceInst.getTile().getTileYCoordinate(), bytes[i], masks[i], topBottom) + " ");
				}
				buffer.write("\n");
			}
			
			buffer.close();
		} 
		catch (IOException e) {
			System.out.println("Error: Could not write to file: " + fileName);
			System.exit(1);
		}
	}
	
	public static void main(String[] args){
		ClockDisabler cd = new ClockDisabler();
		boolean verbose = true;
		if(args.length != 3){
			System.out.println("USAGE: java -jar ClockDisabler.jar [input_file.ncd] [output_file.ncd] [bitsToFlip.txt]");
			System.out.println("\n   *** Clock Disabler v 0.1 ***");
			System.out.println("   This program will convert an NCD file to XDL, search the XDL for the\n" +
					           "   appropriate DCMs and input LUTs connected to the RST wire and set the \n" +
					           "   LUT equation to a value of 1 (causing the DCMs to wakeup in RST after\n" +
					           "   configuration.  The program then converts the XDL back to NCD and outputs \n" +
					           "   the text file containing the bits to flip back to lower the RST lines on the DCM(s).\n" +
					           "   Currently this will only work with the Virtex 4 FX60 part.");
			System.exit(0);
		}
		
		// Convert NCD to XDL
		if(verbose) System.out.print("Converting NCD2XDL ...");
		FileConverter.convertNCD2XDL(args[0],cd.xdlFileName);
		if(verbose) System.out.println("DONE!");
		
		// Parse XDL into memory
		if(verbose) System.out.print("Parsing XDL ...");
		cd.design.loadXDLFile(cd.xdlFileName);
		if(verbose) System.out.println("DONE!");
		
		// Locate and Change LUT content, write XDL file
		if(verbose) System.out.print("Locating and Changing LUT contents ...");
		cd.locateAndChangeLUTs();
		cd.design.saveXDLFile(cd.xdlFileName);
		if(verbose) System.out.println("DONE!");
		
		// convert XDL to NCD
		if(verbose) System.out.print("Converting XDL to NCD ...");
		cd.ncdOutputFileName = args[1]; 
		FileConverter.convertXDL2NCD(cd.ncdOutputFileName);
		if(verbose) System.out.println("DONE!");
		
		if(verbose) System.out.print("Writing text file with bit locations ...");
		cd.writeTextFile(args[2]);
		if(verbose) System.out.println("DONE!");		
	}
}

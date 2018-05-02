package edu.byu.ece.gremlinTools.xilinxToolbox;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import edu.byu.ece.rapidSmith.device.PrimitiveType;
import edu.byu.ece.rapidSmith.device.Utils;
import edu.byu.ece.rapidSmith.primitiveDefs.Element;
import edu.byu.ece.rapidSmith.primitiveDefs.PrimitiveDefPin;
import edu.byu.ece.rapidSmith.primitiveDefs.PrimitiveDef;
import edu.byu.ece.rapidSmith.primitiveDefs.PrimitiveDefList;
import edu.byu.ece.rapidSmith.util.FileTools;

public class XDLInstanceParameters {

	private String spaces(int count){
		String s = "";
		for(int i=0; i < count; i++){
			s += " ";
		}
		return s;
	}
	
	public void createParametersFile(String fileName, String instanceType, PrimitiveDefList part){
		String nl = System.getProperty("line.separator");
		PrimitiveType type = Utils.createPrimitiveType(instanceType);
		HashMap<String,HashSet<String>> allAttributes = new HashMap<String,HashSet<String>>();
		PrimitiveDef def = part.getPrimitiveDef(type);
		
		for(Element e : def.getElements()){
			if(e.getCfgOptions() != null){
				for(String option : e.getCfgOptions()){
					if(allAttributes.get(e.getName()) == null){
						allAttributes.put(e.getName(), new HashSet<String>());
					}
					allAttributes.get(e.getName()).add(option);
				}
			}
			
		}
		
		/*
		for(XDL_Instance inst :  design.getInstanceList()){
			if(inst.getType().equals(type)){
				for(XDL_Attribute attr : inst.getAttributeList()){
					if(allAttributes.get(attr.getPhysicalName()) == null){
						allAttributes.put(attr.getPhysicalName(), new HashSet<String>());
					}
					allAttributes.get(attr.getPhysicalName()).add(attr.getValue());
				}
			}
		}*/
		
		try {
			SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
			BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
			bw.write("" + nl);
			bw.write("# ================================================================" + nl);
			bw.write("# edu.byu.ece.gremlinTools.xilinxToolbox.XDLInstanceParameters" + nl);
			bw.write("# " + FileTools.getTimeString() + nl);
			bw.write("# " + type.toString() +" parameters" + nl);
			bw.write("# ================================================================" +nl+nl);
			bw.write("import sys, os, time, math" + nl + nl + nl);
			bw.write(type.toString() + "_ATTRIBUTES = ("+nl);
			String[] attributeNames = new String[allAttributes.keySet().size()];
			attributeNames = allAttributes.keySet().toArray(attributeNames);
			Arrays.sort(attributeNames);
			int longest = 0;
			for(String s : attributeNames){
				if(s.length() > longest){
					longest = s.length();
				}
			}
			
			for(String name : attributeNames){
				bw.write(" (\""+name+"\","+ spaces(longest-name.length() + 2) + "\"#OFF\"");
				for(String value : allAttributes.get(name)){
					if(!value.equals("#OFF")){
						bw.write(",\""+value+"\"");
					}
				}
				bw.write("),"+nl);
			}
			bw.write(")"+nl + nl);
			
			ArrayList<String> inputs = new ArrayList<String>();
			ArrayList<String> outputs = new ArrayList<String>();
			for(PrimitiveDefPin p : def.getPins()){
				if(p.isOutput()){
					outputs.add(p.getExternalName());
				}
				else{
					inputs.add(p.getExternalName());
				}
			}
			if(inputs != null && inputs.size() > 0){
				bw.write(type.toString() + "_INPUTS = (\"" + inputs.get(0) +"\"");
				for(int i=1; i < inputs.size(); i++){
					bw.write(",\"" + inputs.get(i) + "\"");
				}
				bw.write(")" + nl + nl);				
			}
			
			if(outputs != null && outputs.size() > 0){
				bw.write(type.toString() + "_OUTPUTS = (\"" + outputs.get(0) +"\"");
				for(int i=1; i < outputs.size(); i++){
					bw.write(",\"" + outputs.get(i) + "\"");
				}
				bw.write(")" + nl + nl);				
			}
			
			bw.close();
			
		} catch (IOException e) {
			System.out.println("Error writing to the file: " + fileName);
		}
	}
	
	
	public static void main(String[] args){
		if(args.length != 3){
			System.out.println("USAGE: <input.dat> <InstanceType> <output.py>");
			System.exit(0);
		}
		//XDL_Design design = new XDL_Design();
		//design.loadXDLFile(args[0]);
		PrimitiveDefList defList = (PrimitiveDefList) FileTools.loadFromFile(args[0]);
		
		XDLInstanceParameters ip = new XDLInstanceParameters();
		//ip.createParametersFile(args[2], args[1], defList);
		for(PrimitiveDef def : defList){
			String type = def.getType().toString();
			ip.createParametersFile(type+".py", type, defList);
		}
	}
}

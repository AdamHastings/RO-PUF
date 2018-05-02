package edu.byu.ece.gremlinTools.ReliabilityTools;

import java.util.ArrayList;

import edu.byu.ece.gremlinTools.Virtex4Bits.Pip;
import edu.byu.ece.gremlinTools.Virtex4Bits.V4ConfigurationBit;
import edu.byu.ece.rapidSmith.design.Attribute;
import edu.byu.ece.rapidSmith.design.Instance;
import edu.byu.ece.rapidSmith.design.Net;
import edu.byu.ece.rapidSmith.design.PIP;
import edu.byu.ece.rapidSmith.device.WireEnumerator;


public class SensitiveBit {
	
	public SensitiveBit() {
		
	}
	
	public static int categorizeFailureType(int failure) {
		int retval = UNKNOWN;
		
		switch(failure) {
			case CLOCK_COLUMN_ERROR:
				retval = CLOCK_FAILURE;
				break;
			case POSSIBLE_HALF_LATCH:
				retval = UNKNOWN;
				break;
			case OPEN:
				retval = OPEN;
				break;
			case ROW_BUFFER_SHORT:
				retval = SHORT;
				break;
			case ROW_SHORT:
				retval = SHORT;
				break;
			case COLUMN_BUFFER_SHORT:
				retval = SHORT;
				break;
			case COLUMN_SHORT:
				retval = SHORT;
				break;
			case FURTHER_ANALYSIS_COLUMN_LONGLINE:
				retval = UNKNOWN;
				break;
			case FURTHER_ANALYSIS_USED_COLUMN:
				retval = UNKNOWN;
				break;
			case FURTHER_ANALYSIS_UNUSED_COLUMN:
				retval = UNKNOWN;
				break;
			case FURTHER_ANALYSIS_ROW_LONGLINE:
				retval = UNKNOWN;
				break;
			case FURTHER_ANALYSIS_ROW_UNUSED_COLUMN:
				retval = UNKNOWN;
				break;
			case BRAM_ATTRIBUTE_FAILURE:
				retval = INSTANCE;
				break;
			case IOB_ATTRIBUTE_FAILURE:
				retval = INSTANCE;
				break;
			case DSP_ATTRIBUTE_FAILURE:
				retval = INSTANCE;
				break;
			case DCM_FAILURE:
				retval = CLOCK_FAILURE;
				break;
			case INSTANCE_ATTRIBUTE:
				retval = INSTANCE;
				break;				
			case INSTANCE_UNUSED:
				retval = INSTANCE;
				break;				
			case INSTANCE_UNUSED_UNFOUND:
				retval = UNKNOWN;
				break;
			case LONGLINE_OPEN:
				retval = OPEN;
				break;
			case LONGLINE_SHORT:
				retval = SHORT;
				break;
			default:
				break;				
		}
		
		return retval;
	}
	
	public static String failTypeString(int type) {
		String retval = "UNKNOWN";
		
		switch(type) {
			case INSTANCE:
				retval = "INSTANCE";
				break;
			case BUFFER:
				retval = "BUFFER";
				break;
			case SHORT:
				retval = "SHORT";
				break;
			case CLOCK_FAILURE:
				retval = "CLOCK_FAILURE";
				break;
			case CLOCK_COLUMN_ERROR:
				retval = "CLOCK_COLUMN_ERROR";
				break;
			case POSSIBLE_HALF_LATCH:
				retval = "POSSIBLE_HALF_LATCH";
				break;
			case OPEN:
				retval = "OPEN";
				break;
			case ROW_BUFFER_SHORT:
				retval = "ROW_BUFFER_SHORT";
				break;
			case ROW_SHORT:
				retval = "ROW_SHORT";
				break;
			case COLUMN_BUFFER_SHORT:
				retval = "COLUMN_BUFFER_SHORT";
				break;
			case COLUMN_SHORT:
				retval = "COLUMN_SHORT";
				break;
			case FURTHER_ANALYSIS_COLUMN_LONGLINE:
				retval = "FURTHER_ANALYSIS_COLUMN_LONGLINE";
				break;
			case FURTHER_ANALYSIS_USED_COLUMN:
				retval = "FURTHER_ANALYSIS_USED_COLUMN";
				break;
			case FURTHER_ANALYSIS_UNUSED_COLUMN:
				retval = "FURTHER_ANALYSIS_UNUSED_COLUMN";
				break;
			case FURTHER_ANALYSIS_ROW_LONGLINE:
				retval = "FURTHER_ANALYSIS_ROW_LONGLINE";
				break;
			case FURTHER_ANALYSIS_ROW_UNUSED_COLUMN:
				retval = "FURTHER_ANALYSIS_ROW_UNUSED_COLUMN";
				break;
			case BRAM_ATTRIBUTE_FAILURE:
				retval = "BRAM_ATTRIBUTE_FAILURE";
				break;
			case IOB_ATTRIBUTE_FAILURE:
				retval = "IOB_ATTRIBUTE_FAILURE";
				break;
			case DSP_ATTRIBUTE_FAILURE:
				retval = "DSP_ATTRIBUTE_FAILURE";
				break;
			case DCM_FAILURE:
				retval = "DCM_FAILURE";
				break;
			case INSTANCE_ATTRIBUTE:
				retval = "INSTANCE_ATTRIBUTE";
				break;				
			case INSTANCE_UNUSED:
				retval = "INSTANCE_UNUSED";
				break;				
			case INSTANCE_UNUSED_UNFOUND:
				retval = "INSTANCE_UNUSED_UNFOUND";
				break;
			case LONGLINE_OPEN:
				retval = "LONGLINE_OPEN";
				break;
			case LONGLINE_SHORT:
				retval = "LONGLINE_SHORT";
				break;
			default:
				break;				
		}
		
		return retval;
	}
	
	public Attribute getAttribute() {
		return _attr;
	}

	public V4ConfigurationBit getConfigurationBit() {
		return _cbit;
	}
	
	public Instance getInstance() {
		return _inst;
	}
	
	public Net getNet() {
		return _net;
	}
	
	public String getName() {
		String name = "";
		if (_net != null) {
			name = _net.getName();
		} else if (_inst != null) {
			name = _inst.getName();
		}
		
		return name;
	}
	
	public PIP getPIP() {
		return _pip;
	}
	
	public int getFailType() {
		return _failType;
	}
	
	public WireEnumerator getWireEnumerator() {
		return _we;
	}
	
	public void setAttribute(Attribute attr) {
		_attr = attr;
	}
	
	public void setConfigurationBit(V4ConfigurationBit cbit) {
		_cbit = cbit;
	}
	
	public void setFailType(int type) {
		_failType = type;
	}
	
	public void setInstance(Instance inst) {
		_inst = inst;
	}
	
	public void setNet(Net net) {
		_net = net;
	}
	
	public void setPIP(PIP pip) {
		_pip = pip;
	}
	
	public void setWireEnumerator(WireEnumerator we) {
		_we = we;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(_failType+"  "+_net.getName());
		
		return sb.toString();
	}
	
	public static final int ROW = 0;
	public static final int COLUMN = 1;
	
	public static final int UNKNOWN = -1;
	public static final int INSTANCE = 0;
	public static final int BUFFER = 1;
	public static final int SHORT = 2;
	public static final int OPEN = 3;
	public static final int POSSIBLE_HALF_LATCH = 4;
	public static final int CLOCK_FAILURE = 5;
	public static final int BRAM_ATTRIBUTE_FAILURE = 6;
	public static final int IOB_ATTRIBUTE_FAILURE = 7;
	public static final int DSP_ATTRIBUTE_FAILURE = 8;
	
	public static final int CLOCK_COLUMN_ERROR = 10;
	public static final int DCM_FAILURE = 11;

	public static final int ROW_BUFFER_SHORT = 12;
	public static final int ROW_SHORT = 13;
	public static final int COLUMN_BUFFER_SHORT = 14;
	public static final int COLUMN_SHORT = 15;
	
	public static final int FURTHER_ANALYSIS_COLUMN_LONGLINE = 16;
	public static final int FURTHER_ANALYSIS_USED_COLUMN = 17;
	public static final int FURTHER_ANALYSIS_UNUSED_COLUMN = 18;
	public static final int FURTHER_ANALYSIS_ROW_LONGLINE = 19;
	public static final int FURTHER_ANALYSIS_ROW_UNUSED_COLUMN = 20;
	
	public static final int INSTANCE_ATTRIBUTE = 21;
	public static final int INSTANCE_UNUSED = 22;
	public static final int INSTANCE_UNUSED_UNFOUND = 23;

	public static final int LONGLINE_SHORT = 24;
	public static final int LONGLINE_OPEN = 25;
	
	
	public static ArrayList<Integer> fullTypeList = null;
	public static ArrayList<Integer> categoryTypeList = null;
	
	static {
		fullTypeList = new ArrayList<Integer>();
		fullTypeList.add(CLOCK_COLUMN_ERROR);
		fullTypeList.add(DCM_FAILURE);
		fullTypeList.add(POSSIBLE_HALF_LATCH);
		fullTypeList.add(OPEN);
		fullTypeList.add(ROW_BUFFER_SHORT);
		fullTypeList.add(ROW_SHORT);
		fullTypeList.add(COLUMN_BUFFER_SHORT);
		fullTypeList.add(COLUMN_SHORT);
		fullTypeList.add(FURTHER_ANALYSIS_COLUMN_LONGLINE);
		fullTypeList.add(FURTHER_ANALYSIS_USED_COLUMN);
		fullTypeList.add(FURTHER_ANALYSIS_UNUSED_COLUMN);
		fullTypeList.add(FURTHER_ANALYSIS_ROW_LONGLINE);
		fullTypeList.add(FURTHER_ANALYSIS_ROW_UNUSED_COLUMN);
		fullTypeList.add(BRAM_ATTRIBUTE_FAILURE);
		fullTypeList.add(IOB_ATTRIBUTE_FAILURE);
		fullTypeList.add(DSP_ATTRIBUTE_FAILURE);
		fullTypeList.add(INSTANCE_ATTRIBUTE);
		fullTypeList.add(INSTANCE_UNUSED);
		fullTypeList.add(INSTANCE_UNUSED_UNFOUND);
		fullTypeList.add(LONGLINE_OPEN);
		fullTypeList.add(LONGLINE_SHORT);
		fullTypeList.add(UNKNOWN);

		categoryTypeList = new ArrayList<Integer>();
		categoryTypeList.add(UNKNOWN);
		categoryTypeList.add(INSTANCE);
		categoryTypeList.add(SHORT);
		categoryTypeList.add(OPEN);
		categoryTypeList.add(CLOCK_FAILURE);
	}
	
	protected int _failType = UNKNOWN;
	protected Net _net;
	protected Instance _inst;
	protected V4ConfigurationBit _cbit;
	protected PIP _pip;
	protected Pip _pipsmall;
	protected Attribute _attr;
	protected WireEnumerator _we;
}

package edu.byu.ece.gremlinTools.ReliabilityTools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import edu.byu.ece.rapidSmith.design.Instance;
import edu.byu.ece.rapidSmith.design.Net;
import edu.byu.ece.rapidSmith.device.PrimitiveType;


public class VHDLComponent {
	
	public VHDLComponent(String name) {
		_componentName = name;
		
		_entityFilterList = new ArrayList<String>();
		_signalFilterList = new ArrayList<String>();
		_instEntityList = new ArrayList<String>();
		_signals = new HashSet<VHDLSignal>();
		
		_netList = new HashMap<String, Net>();
		_instanceList = new HashMap<String, Instance>();
		_failHash = new HashMap<Integer, Integer>();
	}
	
	public void addInstantiatedEntityName(String entity) {
		_instEntityList.add(entity);
	}
	
	public void addEntityFilterItem(String item) {
		_entityFilterList.add(item);
	}
	
	public void addInstance(String signalName, Instance inst) {
		_instanceList.put(signalName, inst);
	}
	
	public void addNet(String signalName, Net net) {
		_netList.put(signalName, net);
	}

	public void addSignalFilterItem(String item) {
		_signalFilterList.add(item);
	}

	public void addEntityFilterList(List<String> list) {
		_entityFilterList.addAll(list);
	}
	
	public void addSignalFilterList(List<String> list) {
		_signalFilterList.addAll(list);
	}
		
	public void addSignal(VHDLSignal signal) {
		_signals.add(signal);
	}
	
	public void calculateTraditionalArea() {
		_slices = 0;
		_dsps = 0;
		_brams = 0;
		for (Instance inst : _instanceList.values()) {
			if (inst.getType() == PrimitiveType.DSP48) {
				_dsps++;
			} else if (inst.getType() == PrimitiveType.RAMB16) {
				_brams++;
			} else if (inst.getType() == PrimitiveType.SLICEL || inst.getType() == PrimitiveType.SLICEM) {
				_slices++;
				if (inst.testAttributeValue("FFX", "#FF")) {
					_ffs++;
				}
				if (inst.testAttributeValue("FFY", "#FF")) {
					_ffs++;					
				}
			} else if (inst.getType() == PrimitiveType.ILOGIC) {
				_ffs++;					
				
			} else if (inst.getType() == PrimitiveType.OLOGIC) {
				_ffs++;					
			}			
		}
	}
	
	public int getBRAMs() {
		return _brams;
	}
	
	public int getComponentArea() {
		return _componentArea;
	}
	
	public int getDSPs() {
		return _dsps;
	}
	
	public int getFFs() {
		return _ffs;
	}
	
	public String getName() {
		return _componentName;
	}
	
	public ArrayList<String> getEntityFilterList() {
		return _entityFilterList;
	}
	
	public HashMap<Integer, Integer> getFailHash() {
		return _failHash;
	}
	
	public HashMap<String, Instance> getSignalInstanceMap() {
		return _instanceList;
	}
	
	public HashMap<String, Net> getSignalNetMap() {
		return _netList;
	}
	
	public ArrayList<String> getSignalFilterList() {
		return _signalFilterList;
	}
	
	public int getSlices() {
		return _slices;
	}
	
	public ArrayList<String> getInstantiatedEntities() {
		return _instEntityList;
	}
	
	public HashSet<VHDLSignal> getSignalList() {
		return _signals;
	}
	
	public String getTopEntityName() {
		return _topEntityName;
	}
	
	public boolean isEntityInFilterList(String name) {
		boolean found = false;
		
		for (String entityName : _entityFilterList) {
			if (name.startsWith(entityName.toLowerCase())) {
				found = true;
				break;
			}
		}
		return found;
	}
	
	public boolean isSignalInFilterList(String name) {
		boolean found = false;
		
		for (String filter : _signalFilterList) {
			if (name.contains(filter.toLowerCase())) {
				found = true;
				break;
			}
		}
		return found;
	}
	
	public VHDLSignal removeSignal(String fullName) {
		VHDLSignal retval = null;
		
		for (VHDLSignal signal : _signals) {
			if (signal.getFullSignalName().equalsIgnoreCase(fullName)) {
				retval = signal; 
				_signals.remove(signal);
				break;
			}
		}
		
		return retval;
	}
	
	public HashMap<String, Net> removeAllNets() {
		HashMap<String, Net> retval = new HashMap<String, Net>();
		retval.putAll(_netList);
		
		_netList = new HashMap<String, Net>();
		
		return retval;
	}
	
	public HashMap<String, Instance> removeAllInstances() {
		HashMap<String, Instance> retval = new HashMap<String, Instance>();
		retval.putAll(_instanceList);
		
		_instanceList = new HashMap<String, Instance>();
		
		return retval;
	}
	
	public void setComponentArea(int area) {
		_componentArea = area;
	}
	
	public void setTopEntityName(String entity) {
		_topEntityName = entity;
	}
	
	protected String _componentName = "";
	protected ArrayList<String> _entityFilterList;
	protected ArrayList<String> _signalFilterList;
	protected String _topEntityName = "";
	protected ArrayList<String> _instEntityList;
	protected HashSet<VHDLSignal> _signals;
	protected HashMap<String, Net> _netList;
	protected HashMap<String, Instance> _instanceList;
	protected HashMap<Integer, Integer> _failHash;
	protected int _componentArea;
	protected int _slices;
	protected int _dsps;
	protected int _brams;
	protected int _ffs;
}

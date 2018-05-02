package edu.byu.ece.gremlinTools.ReliabilityTools;


public class VHDLSignal {

	
	public VHDLSignal(String entity, String name) {
		_signalName = name;
		_topEntityName = entity;
	}

	public static int getSignalDepth(String fullSignal) {
		int retval = 0;
		String[] depth = fullSignal.split("/");
		retval = depth.length;
		
		return retval;
	}
	
	public String getFullSignalName() {
		return (_path == null || _path == "") ? _signalName : _path+"/"+_signalName;
	}

	public String getPath() {
		return _path;
	}
	
	public String getSignalName() {
		return _signalName;
	}
	
	public String getTopEntity() {
		return _topEntityName;
	}
	
	public void setPath(String path) {
		_path = path;
	}
	
	protected String _signalName;
	protected String _path = "";
	protected String _topEntityName;
	
}

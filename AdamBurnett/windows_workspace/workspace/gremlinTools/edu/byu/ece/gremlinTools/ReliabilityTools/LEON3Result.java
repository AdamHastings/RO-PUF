package edu.byu.ece.gremlinTools.ReliabilityTools;

import java.util.ArrayList;


public class LEON3Result extends XRTCResult {
	
	public LEON3Result(String fileLine, int type) {
		super(fileLine);

		_xrtcType = type;
	
		if (type == SW) {
			_translateSWDetection();
		} else if (type == ORIG) {
			_translateOriginalDetection();
		} else if (type == DWC) {
			_translateDWCDetection();
		} else if (type == TMR) {
			_translateTMRDetection();
		} else {
			
		}
	
	}
	
	public int compareTo(Object obj) {
		int retval = -1;
		if (obj instanceof LEON3Result) {
			LEON3Result xrtcr = (LEON3Result)obj;
			retval = (xrtcr.getFAR() > _far) ? -1 : 1;
			if (xrtcr.getFAR() == _far) {
				retval = (xrtcr.getFrameBit() > _frameBit) ? -1 : 1;
				if (xrtcr.getFrameBit() == _frameBit) {
					retval = (xrtcr.getDetectType() == _detectType) ? 0 :
						     (xrtcr.getDetectType() == RECONFIG) ? -1 : 1;
				}
			}
		}
		
		return retval;
	}
	
	public int getACEType() {
		return _aceType;
	}
	
	public ArrayList<String> getDetectTypes() {
		ArrayList<String> types = new ArrayList<String>();
		
		if (_xrtcType == ORIG) {
			
		} else {
			if (_xrtcType == SW) {
				types.add(REG);
				types.add(ECMP);
				types.add(HEAD);
				types.add(CC);
				types.add(SCRUB);
			} else if (_xrtcType == DWC) {
				types.add(DETECT);
			} else if (_xrtcType == TMR) {
				types.add(DETECT);
				types.add(ERR);			
			}
			types.add(STUCK);
			types.add(TRAP);
		}
		return types;
	}

	public ArrayList<String> getERRDetectTypes() {
		ArrayList<String> types = new ArrayList<String>();
		
		if (_xrtcType == ORIG) {
			types.add(ERR);									
		} else {
			if (_xrtcType == SW) {
				types.add(ERR+REG);
				types.add(ERR+ECMP);
				types.add(ERR+HEAD);
				types.add(ERR+CC);
				types.add(ERR+SCRUB);
			} else if (_xrtcType == DWC) {
				types.add(ERR+DETECT);
			} else if (_xrtcType == TMR) {
				types.add(ERR+DETECT);
				types.add(ERR+ERR);
			}
			types.add(ERR+STUCK);
			types.add(ERR+TRAP);
		}
		
		return types;
	}
	
	
	public ArrayList<String> getAllDetectTypes() {
		ArrayList<String> types = new ArrayList<String>();
		
		types.add(SDC);
		types.add(TRUESTUCK);
		types.add(RECONFIG);
		if (_xrtcType == ORIG) {
			
		} else {
			if (_xrtcType == SW) {
				types.add(REG);
				types.add(ERR+REG);
				types.add(ECMP);
				types.add(ERR+ECMP);
				types.add(HEAD);
				types.add(ERR+HEAD);
				types.add(CC);
				types.add(ERR+CC);
				types.add(SCRUB);
				types.add(ERR+SCRUB);
			} else if (_xrtcType == DWC) {
				types.add(DETECT);
				types.add(ERR+DETECT);
			} else if (_xrtcType == TMR) {
				types.add(DETECT);
				types.add(ERR+DETECT);
				types.add(ERR+ERR);
			}
			types.add(DLOOP);
			types.add(STUCK);
			types.add(ERR+STUCK);
			types.add(TRAP);
			types.add(ERR+TRAP);
		}
		types.add(ERR);
		types.add(OTHER);
		
		return types;
	}

	public String getProcessorClass() {
		return _class;
	}
	
	public int getColumn() {
		return _col;
	}
	
	public int getDetectionLatency() {
		return _detectLatency;
	}
	
	public String getDetectType() {
		return _detectType;
	}
	
	public int getXRTCType() {
		return _xrtcType;
	}

	public boolean isTrapType() {
		return (_detectType.equalsIgnoreCase(TRAP) || _detectType.equalsIgnoreCase(ERR+TRAP));
	}
	
	public static boolean isDetectable(String detectType) {
		return (detectType == DETECT || detectType == ERR || detectType == STUCK || detectType == TRAP || 
				detectType == REG || detectType == ECMP || detectType == HEAD || detectType == CC || detectType == DLOOP);
		
	}	
	
	public boolean isDetectable() {
		return (_detectType == DETECT || _detectType == ERR || _detectType == STUCK || _detectType == TRAP || 
				_detectType == REG || _detectType == ECMP || _detectType == HEAD || _detectType == CC || _detectType == DLOOP);
		
	}
	
	public void setProcessorClass(String pclass) {
		_class = pclass;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		
//		sb.append("RAW: "+_rawfar+"  FAR: "+Integer.toHexString(_far)+"  BYTE: "+Integer.toHexString(_frameByte)+"  BIT: "+Integer.toHexString(_maskBit)+"  FB: "+_frameBit);
//		sb.append("  COL: "+_col+"  ROW: "+_row+"  MNA: "+_mna+"  TYPE: "+_type+"  TOP: "+_top+"  DETECT: "+_detectType);
		
		sb.append(_rawfar+"  "+_rawResult+"  "+Integer.toHexString(_far)+"  "+_row+"  "+_col+"  "+_frameBit+"  "+_detectLatency);
		if (_sbit.getInstance() != null && _sbit.getAttribute() != null) {
			sb.append("  "+_sbit.getInstance().getPrimitiveSite()+"  "+_sbit.getAttribute().getPhysicalName()+"  "+_sbit.getAttribute().getLogicalName());			
		} else if (_sbit.getNet() != null) {
			if (_sbit.getPIP() != null && _sbit.getWireEnumerator() != null) {
				sb.append("  "+_sbit.getPIP().toString(_sbit.getWireEnumerator()).trim());
			}
		}
		sb.append("  "+_detectType+"  "+_class+"  "+_sbit.getName());
		
		if (_sbit != null) {
			sb.append("  "+SensitiveBit.failTypeString(_sbit.getFailType()));
		}
		
		return sb.toString();
	}
	
	
	protected void _translateOriginalDetection() {
		int one = 0x00000001;
		int lmask = 0x000FFFFF;
		int reconfig = 31;
		int trap = 0;
		int detect = 1;
		int stuck = 2;
		int errCnt = 3;
		int truestuck = 4;
		int dloop = 5;
		int err = 6;
		
		int latency = 11;
		
		_aceType = UNDETECTED;
		_detectLatency = (_result >> latency) & lmask;
		if (((_result >> reconfig) & one) == 1) {
			_detectType = RECONFIG;
			_aceType = RECON;
		} else if (((_result >> truestuck) & one) == 1) {
			_detectType = TRUESTUCK;			
		} else if (((_result >> err) & one) == 1) {
			_detectType = ERR;			
			_aceType = DUE;
		} else if (((_result >> errCnt) & one) == 1) {
			_detectType = SDC;							
		} else {
			_detectType = OTHER;				
		}
		
	}

	
	protected void _translateDWCDetection() {
		int one = 0x00000001;
		int lmask = 0x000FFFFF;
		int reconfig = 31;
		int trap = 0;
		int detect = 1;
		int stuck = 2;
		int errCnt = 3;
		int truestuck = 4;
		int dloop = 5;
		int err = 6;
		
		int latency = 11;
		
		_detectLatency = (_result >> latency) & lmask;
		_aceType = DRE;
		if (((_result >> reconfig) & one) == 1) {
			_detectType = RECONFIG;
			_aceType = RECON;
		} else if (((_result >> dloop) & one) == 1) {
			_detectType = DLOOP;			
			_aceType = DUE;
		} else if (((_result >> truestuck) & one) == 1) {
			_detectType = TRUESTUCK;			
			_aceType = UNDETECTED;
		} else if (((_result >> errCnt) & one) == 1) {
			_aceType = DUE;
			if (((_result >> detect) & one) == 1) {
				_detectType = ERR+DETECT;
			} else if (((_result >> stuck) & one) == 1) {
				_detectType = ERR+STUCK;
			} else if (((_result >> trap) & one) == 1) {
				_detectType = ERR+TRAP;
			} else {
				_detectType = SDC;				
				_aceType = UNDETECTED;
			}
		} else if (((_result >> detect) & one) == 1) {
			_detectType = DETECT;				
		} else if (((_result >> stuck) & one) == 1) {
			_detectType = STUCK;				
		} else if (((_result >> trap) & one) == 1) {
			_detectType = TRAP;				
		} else {
			_detectType = OTHER;				
		}
		
	}

	protected void _translateTMRDetection() {
		int one = 0x00000001;
		int lmask = 0x000FFFFF;
		int reconfig = 31;
		int trap = 0;
		int detect = 1;
		int stuck = 2;
		int errCnt = 3;
		int truestuck = 4;
		int dloop = 5;
		int err = 6;
		int latency = 11;
		
		_detectLatency = (_result >> latency) & lmask;
		_aceType = DRE;
		
		if (((_result >> reconfig) & one) == 1) {
			_detectType = RECONFIG;
			_aceType = RECON;
		} else if (((_result >> dloop) & one) == 1) {
			_detectType = DLOOP;			
			_aceType = DUE;
		} else if (((_result >> truestuck) & one) == 1) {
			_detectType = TRUESTUCK;			
			_aceType = UNDETECTED;
		} else if (((_result >> errCnt) & one) == 1) {
			_aceType = DUE;
			if (((_result >> detect) & one) == 1) {
				_detectType = ERR+DETECT;
			} else if (((_result >> stuck) & one) == 1) {
				_detectType = ERR+STUCK;
			} else if (((_result >> trap) & one) == 1) {
				_detectType = ERR+TRAP;
			} else if (((_result >> err) & one) == 1) {
				_detectType = ERR+ERR;
			} else {
				_detectType = SDC;				
				_aceType = UNDETECTED;
			}
		} else if (((_result >> detect) & one) == 1) {
			_detectType = DETECT;				
		} else if (((_result >> stuck) & one) == 1) {
			_detectType = STUCK;				
		} else if (((_result >> trap) & one) == 1) {
			_detectType = TRAP;				
		} else if (((_result >> err) & one) == 1) {
			_detectType = ERR;				
		} else {
			_detectType = OTHER;				
		}
		
	}
	
	
	protected void _translateSWDetection() {
		int one = 0x00000001;
		int lmask = 0x000FFFFF;
		int reconfig = 31;
		int reg = 0;
		int ecmp = 1;
		int head = 2;
		int cc = 3;
		int scrub = 4;
		int stuck = 5;
		int trap = 6;
		int err = 7;
		int errCnt = 8;
		int dloop = 9;
		int truestuck = 10;
		int latency = 11;
		
		_detectLatency = (_result >> latency) & lmask;
		_aceType = DRE;
		
		if (((_result >> reconfig) & one) == 1) {
			_detectType = RECONFIG;				
			_aceType = RECON;
		} else if (((_result >> dloop) & one) == 1) {
			_detectType = DLOOP;				
			_aceType = DUE;
		} else if (((_result >> truestuck) & one) == 1) {
			_detectType = TRUESTUCK;				
			_aceType = UNDETECTED;	
		} else if (((_result >> errCnt) & one) == 1) {
			_aceType = DUE;
			if (((_result >> reg) & one) == 1) {
				_detectType = ERR+REG;				
			} else if (((_result >> ecmp) & one) == 1) {
				_detectType = ERR+ECMP;				
			} else if (((_result >> head) & one) == 1) {
				_detectType = ERR+HEAD;				
			} else if (((_result >> cc) & one) == 1) {
				_detectType = ERR+CC;				
			} else if (((_result >> scrub) & one) == 1) {
				_detectType = ERR+SCRUB;				
			} else if (((_result >> stuck) & one) == 1) {
				_detectType = ERR+STUCK;				
			} else if (((_result >> trap) & one) == 1) {
				_detectType = ERR+TRAP;				
			} else {
				_detectType = SDC;	
				_aceType = UNDETECTED;
			}
		} else if (((_result >> reg) & one) == 1) {
			_detectType = REG;				
		} else if (((_result >> ecmp) & one) == 1) {
			_detectType = ECMP;				
		} else if (((_result >> head) & one) == 1) {
			_detectType = HEAD;				
		} else if (((_result >> cc) & one) == 1) {
			_detectType = CC;				
		} else if (((_result >> scrub) & one) == 1) {
			_detectType = SCRUB;				
		} else if (((_result >> stuck) & one) == 1) {
			_detectType = STUCK;				
		} else if (((_result >> trap) & one) == 1) {
			_detectType = TRAP;				
		} else {
			_detectType = OTHER;				
		}

	}
	
	// in order of best to worst
	public static final int DRE = 1;
	public static final int DUE = 2;
	public static final int UNDETECTED = 3;
	public static final int RECON = 4;
	
	public static final int ORIG = 0;
	public static final int SW = 1;
	public static final int DWC = 2;
	public static final int TMR = 3;
	
	public static String RECONFIG = "RECONFIG";
	public static String DLOOP = "DLOOP";
	public static String TRUESTUCK = "TRUESTUCK";
	public static String ERR = "ERR";
	public static String DETECT = "DETECT";
	public static String SDC = "SDC";
	public static String STUCK = "STUCK";
	public static String TRAP = "TRAP";
	public static String OTHER = "OTHER";
	public static String REG = "REG";
	public static String ECMP = "ECMP";
	public static String HEAD = "HEAD";
	public static String CC = "CC";
	public static String SCRUB = "SCRUB";
	
	protected int _detectLatency;
	protected String _class = ParseSYNvhdl.UNKNOWN;
	protected String _detectType = "UNDECIDED";
	protected int _xrtcType = SW;
	protected int _aceType = DRE;
	
}

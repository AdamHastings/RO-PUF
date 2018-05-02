package edu.byu.ece.gremlinTools.ReliabilityTools;

import java.util.StringTokenizer;


public class XRTCResult implements Comparable {

	
	public XRTCResult(String fileLine) {
		
		StringTokenizer st = new StringTokenizer(fileLine);
		_rawfar = st.nextToken();
		_rawResult = st.nextToken().trim();
		_result = (_rawResult.equalsIgnoreCase("80000000")) ? Integer.MIN_VALUE : Integer.parseInt(_rawResult, 16);
		_far = Integer.parseInt(_rawfar.substring(0, 8), 16);
		_frameByte = Integer.parseInt(_rawfar.substring(8, 12), 16);
		_getMaskBit();
		_frameBit = _frameByte * 8 + _maskBit;
		_translateFAR();
		

	}
	
	public int compareTo(Object obj) {
		int retval = -1;
		if (obj instanceof XRTCResult) {
			XRTCResult xrtcr = (XRTCResult)obj;
			retval = (xrtcr.getFAR() > _far) ? -1 : 1;
			if (xrtcr.getFAR() == _far) {
				retval = (xrtcr.getFrameBit() > _frameBit) ? -1 : 1;
				if (xrtcr.getFrameBit() == _frameBit) {
					retval = 0;
				}
			}
		}
		
		return retval;
	}
		
	public int getFAR() {
		return _far;
	}	
	
	public int getFrameBit() {
		return _frameBit;
	}
	
	public int getFrameByte() {
		return _frameByte;
	}
	
	public int getMNA() {
		return _mna;
	}
	
	public String getRawFAR() {
		return _rawfar;
	}
	
	public String getRawResult() {
		return _rawResult;
	}
	
	public int getResult() {
		return _result;
	}
	
	public int getRow() {
		return _row;
	}
	
	public int getTop() {
		return _top;
	}
	
	public String getTopString() {
		String retval = (_top == 0) ? "TOP" : "BOTTOM";		
		return retval;
	}
	
	public int getType() {
		return _type;
	}
	
	public String getTypeString() {
		String retval = "UNKNOWN";
		
		switch (_type) {
			case 0:
				retval = "CLB";
				break;
			case 1:
				retval = "BRAM-INT";
				break;
			case 2:
				retval = "BRAM-DATA";
				break;
			case 5:
			case 6:
				retval = "HIDDEN";
				break;
			default:
				break;
		}
		
		return retval;
	}
	
	public SensitiveBit getSensitiveBit() {
		return _sbit;
	}

	
	public void setSensitiveBit(SensitiveBit sbit) {
		_sbit = sbit;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		
//		sb.append("RAW: "+_rawfar+"  FAR: "+Integer.toHexString(_far)+"  BYTE: "+Integer.toHexString(_frameByte)+"  BIT: "+Integer.toHexString(_maskBit)+"  FB: "+_frameBit);
//		sb.append("  COL: "+_col+"  ROW: "+_row+"  MNA: "+_mna+"  TYPE: "+_type+"  TOP: "+_top+"  DETECT: "+_detectType);
		
		sb.append(_rawfar+"  "+_rawResult+"  "+Integer.toHexString(_far)+"  "+_row+"  "+_col+"  "+_frameBit+"  ");
		if (_sbit.getInstance() != null && _sbit.getAttribute() != null) {
			sb.append("  "+_sbit.getInstance().getPrimitiveSite()+"  "+_sbit.getAttribute().getPhysicalName()+"  "+_sbit.getAttribute().getLogicalName());			
		} else if (_sbit.getNet() != null) {
			if (_sbit.getPIP() != null && _sbit.getWireEnumerator() != null) {
				sb.append("  "+_sbit.getPIP().toString(_sbit.getWireEnumerator()).trim());
			}
		}
		sb.append(_sbit.getName());
		
		if (_sbit != null) {
			sb.append("  "+SensitiveBit.failTypeString(_sbit.getFailType()));
		}
		
		return sb.toString();
	}
	
	
	protected void _translateFAR() {
		_mna = _far & 0x003F;
		_col = (_far >> 6) & 0x00FF;
		_row = (_far >> 14) & 0x001F;
		_type = (_far >> 19) & 0x0007;
		_top = (_far >> 22) & 0x0001;
	}
	
	protected void _getMaskBit() {
		int mask = Integer.parseInt(_rawfar.substring(_rawfar.length() - 4), 16);
		int cnt = 8;
		
		while (cnt > 0 && mask > 0) {
			mask >>= 1;
			cnt--;
		}
		
		_maskBit = cnt;
	}
	
	
	protected String _rawfar;
	protected String _rawResult;
	protected int _far;
	protected int _frameByte;
	protected int _maskBit;
	protected int _frameBit;
	protected int _result;
	
	protected int _col;
	protected int _row;
	protected int _top;
	protected int _type;
	protected int _mna;
	
	protected SensitiveBit _sbit;

	
}

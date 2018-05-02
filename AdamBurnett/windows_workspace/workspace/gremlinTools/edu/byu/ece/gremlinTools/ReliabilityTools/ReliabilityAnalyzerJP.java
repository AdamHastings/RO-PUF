package edu.byu.ece.gremlinTools.ReliabilityTools;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;

import edu.byu.ece.gremlinTools.Virtex4Bits.Bit;
import edu.byu.ece.gremlinTools.Virtex4Bits.BitNumberType;
import edu.byu.ece.gremlinTools.Virtex4Bits.V4ConfigurationBit;
import edu.byu.ece.gremlinTools.bitstreamDatabase.BitstreamDB;
import edu.byu.ece.rapidSmith.bitstreamTools.configuration.FrameAddressRegister;
import edu.byu.ece.rapidSmith.design.Instance;
import edu.byu.ece.rapidSmith.design.Net;


public class ReliabilityAnalyzerJP extends ReliabilityAnalyzer {
	
	
	public ReliabilityAnalyzerJP(String xdlFilename, String farFileName) {
		super(xdlFilename);
		
		_xdlFile = xdlFilename;
		_farFile = farFileName;
		_parseXDL();
//		_determineComponentArea();
		
		_numBitsConsidered = _parseXRTCfile();
//		_bitsNotAnalyzed = AnalyzeBits(_xrtcHash);
//		_fillClassTypes();
//		_splitPipeTypes();
		_analyze();
	}
	
    public ArrayList<LEON3Result> AnalyzeBits(TreeMap<LEON3Result, V4ConfigurationBit> xrtcHash) {
    	
    	ArrayList<LEON3Result> RemainingBits; 
    	
    	RemainingBits = IA.AnalyzeInterconnectBits(xrtcHash); 
    	System.out.println("After Interconnect " + RemainingBits.size());
    	RemainingBits = LA.AnalyzeLogicBits(xrtcHash, RemainingBits);
    	System.out.println("After Logic " + RemainingBits.size());
    	System.out.println("Checking Remaining Bits against database... + ");
        int remainingBitsInDataBase = checkRemainingBits(xrtcHash, RemainingBits);
    	System.out.println("Remaining Bits Found in Database: " + remainingBitsInDataBase);
    	return RemainingBits; 
    }

    public int checkRemainingBits(TreeMap<LEON3Result, V4ConfigurationBit> xrtcHash, ArrayList<LEON3Result> xrtcList) {
    	DB = new BitstreamDB(); 
    	DB.openConnection();
    	
    	int remainingBitsInDatabase=0;
    	for (LEON3Result xrtc : xrtcList){
    		V4ConfigurationBit cbit = xrtcHash.get(xrtc);
    		if (BitInDataBase(cbit)) {
    			remainingBitsInDatabase++;
    		}
    	}
    	
    	return remainingBitsInDatabase;
    }

    public int getNumBitsConsidered() {
		return _numBitsConsidered;
	}
	
	public ArrayList<LEON3Result> getBitsNotAnalyzed() {
		return _bitsNotAnalyzed;
	}
	
	public TreeMap<LEON3Result, V4ConfigurationBit> getXRTCHash() {
		return _xrtcHash;
	}
	
	public static String strReplicate(String str, int repeat) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < repeat; i++) sb.append(str);
		return sb.toString();
	}
	
	public void writeSummary(PrintWriter pw) {
		
		double repairPercent = (_totXRTCcnt == 0) ? 0 : 100 * ((double)_repairXRTCcnt / (double)_totXRTCcnt);
		double detectPercent = (_totXRTCcnt == 0) ? 0 : 100 * ((double)_detectXRTCcnt / (double)_totXRTCcnt);
		int notRepair = _detectXRTCcnt - _repairXRTCcnt;
		LEON3Result first = _xrtcHash.keySet().iterator().next();
		double sdcP = (_totXRTCcnt == 0) ? 0 : 100 * ((double) _errCntHash.get(LEON3Result.SDC) / (double)_totXRTCcnt);
		double truestuckP = (_totXRTCcnt == 0) ? 0 : 100 * ((double) _errCntHash.get(LEON3Result.TRUESTUCK) / (double)_totXRTCcnt);
		double reconfigP = (_totXRTCcnt == 0) ? 0 : 100 * ((double) _errCntHash.get(LEON3Result.RECONFIG) / (double)_totXRTCcnt);
		double detectP, detectP2;
		
		pw.append("TOT ACE FAULTS: "+_totXRTCcnt+"\n");
		pw.append("\tSDC: \t\t "+_errCntHash.get(LEON3Result.SDC));
		pw.printf(" \t (%3.3f)\n", sdcP);
		pw.append("\tTRUESTUCK: \t "+_errCntHash.get(LEON3Result.TRUESTUCK));
		pw.printf(" \t (%3.3f)\n", truestuckP);
		pw.append("\tREPAIRED:  "+_repairXRTCcnt);
		pw.printf("  (%3.3f)\n", repairPercent);
		pw.append("\tDETECTED:  "+_detectXRTCcnt);
		pw.printf("  (%3.3f) ", detectPercent);
		pw.append(" ---- DETECTED NOT REPAIRED: "+notRepair+"\n");
		
		for (String type : first.getDetectTypes()) {
			if (_type == LEON3Result.ORIG) {
				detectP = (_totXRTCcnt == 0) ? 0 : 100 * 
						(((double) _errCntHash.get(type))/ (double)_totXRTCcnt);
				detectP2 = (_detectXRTCcnt == 0) ? 0 : 100 * 
						(((double) _errCntHash.get(type))/ (double)_detectXRTCcnt);				
			} else {
				detectP = (_totXRTCcnt == 0) ? 0 : 100 * 
						(((double) _errCntHash.get(type) + (double)_errCntHash.get(LEON3Result.ERR+type))/ (double)_totXRTCcnt);
				detectP2 = (_detectXRTCcnt == 0) ? 0 : 100 * 
						(((double) _errCntHash.get(type) + (double)_errCntHash.get(LEON3Result.ERR+type))/ (double)_detectXRTCcnt);
			}
			int err = (_errCntHash.get(LEON3Result.ERR+type) == null) ? 0 : _errCntHash.get(LEON3Result.ERR+type);
			
			pw.append("\t\t"+type+": "+_errCntHash.get(type)+" \t "+LEON3Result.ERR+type+":");
			pw.append(" "+err);
			pw.printf(" \t (%3.3f) \t (%3.3f)  ", detectP, detectP2);
			pw.append("LATENCY: "+_errLatencyHash.get(type)+"\n");
		}
		
		int err = (_errCntHash.get(LEON3Result.ERR) == null) ? 0 :_errCntHash.get(LEON3Result.ERR); 
		pw.append("\n\tDETECTION LATENCY AVG: "+_totalLatency+"\n");
		pw.append("\tERR: \t "+err+"\n");
		if (_type != LEON3Result.ORIG) {
			double dloopP = (_totXRTCcnt == 0) ? 0 : 100 * ((double) _errCntHash.get(LEON3Result.DLOOP) / (double)_totXRTCcnt);
			pw.append("\tDLOOP: \t\t "+_errCntHash.get(LEON3Result.DLOOP));
			pw.printf(" \t (%3.3f)\n", dloopP);
		}
		pw.append("\tRECONFIG:\t "+_errCntHash.get(LEON3Result.RECONFIG));
		pw.printf(" \t (%3.3f)\n", reconfigP);
		pw.append("\n");
	}
	
	public void writeDetections(PrintWriter pw) {
		HashMap<String, VHDLComponent> typeComponentMap = _parse.getTypeComponentMap();
		
		_sdcHash = new HashMap<String, Integer>();
		_dueHash = new HashMap<String, Integer>();
		_reconfigHash = new HashMap<String, Integer>();
		
		LEON3Result first = _xrtcHash.keySet().iterator().next();
		ArrayList<String> types = first.getAllDetectTypes();
		
		// Print column titles
		pw.append(String.format("%1$-15s", "CLASS"));
		for (String title : types) {
			pw.append(String.format("%1$8.6s", title));
		}
		pw.append(String.format("%1$8s", "TOTR"));
		pw.append(String.format("%1$8s", "TOTER"));
		pw.append(String.format("%1$8s", "TOTTOT"));
		pw.append(String.format("%1$8s", "unACE"));
		pw.append("\n");

		pw.append(String.format("%1$-15s", "====="));
		for (String title : types) {
			pw.append(String.format("%1$8.6s", strReplicate("=", title.length())));
		}
		pw.append(String.format("%1$8s", "===="));
		pw.append(String.format("%1$8s", "====="));
		pw.append(String.format("%1$8s", "======"));
		pw.append(String.format("%1$8s", "====="));
		pw.append("\n");

		// print detailed info
		for (String classtype : ParseSYNvhdl.CLASSESLIST) {
			pw.append(String.format("%1$-15.14s", classtype));
			for (String type : types) {
				String key = type+classtype;
				int value = _errCntPairHash.get(key);
				pw.append(String.format("%1$8d", value));
			}
			
			int totr = 0;
			for (String detect : first.getDetectTypes()) {
				String key = detect+classtype;
				int value = _errCntPairHash.get(key);
				totr += value;
			}
			
			int totd = 0;
			for (String edetect : first.getERRDetectTypes()) {
				String key = edetect+classtype;
				int value = _errCntPairHash.get(key);
				totd += value;
			}
			
			Integer dloop = _errCntPairHash.get(LEON3Result.DLOOP+classtype);
			Integer sdc = _errCntPairHash.get(LEON3Result.SDC+classtype);
			Integer truestuck = _errCntPairHash.get(LEON3Result.TRUESTUCK+classtype);
			Integer err = _errCntPairHash.get(LEON3Result.ERR+classtype);
			Integer reconfig = _errCntPairHash.get(LEON3Result.RECONFIG+classtype);
			
			err = (err == null) ? 0 : err;
			dloop = (dloop == null) ? 0 : dloop;
			
			if (_type == LEON3Result.TMR || _type == LEON3Result.ORIG) 
				_dueHash.put(classtype, totd+dloop);
			else
				_dueHash.put(classtype, totd+dloop+err);
			
			_sdcHash.put(classtype, sdc+truestuck);
			_reconfigHash.put(classtype, reconfig);
			
			int tote = totd + dloop + sdc + truestuck;
			int tot = totr + tote;
			
			VHDLComponent comp = typeComponentMap.get(classtype);
			int unace = (comp == null) ? 0 : comp.getComponentArea() - tot;
			
			if (classtype.equals(ParseSYNvhdl.NONET) || classtype.equals(ParseSYNvhdl.IOB) || 
					classtype.equals(ParseSYNvhdl.DSP) || classtype.equals(ParseSYNvhdl.BRAM_ATTR)) {
				comp.setComponentArea(tot);
				unace = 0;
			}
			
			pw.append(String.format("%1$8d", totr)); 
			pw.append(String.format("%1$8d", tote));
			pw.append(String.format("%1$8d", tot));
			pw.append(String.format("%1$8d", unace));
			pw.append("\n");
			
		}				
		pw.append("\n");
	
	}
	
	public void writeVerticalPercentageDetections(PrintWriter pw) {
		HashMap<String, VHDLComponent> typeComponentMap = _parse.getTypeComponentMap();
		
		LEON3Result first = _xrtcHash.keySet().iterator().next();
		ArrayList<String> types = first.getAllDetectTypes();
		HashMap<String, Integer> vertTots = new HashMap<String, Integer>();

		int totrTot = 0;
		int toteTot = 0;
		int totTot = 0;
		int totAce = 0;
		for (String classtype : ParseSYNvhdl.CLASSESLIST) {
			VHDLComponent comp = typeComponentMap.get(classtype);
			if (comp != null) {
				totAce += comp.getComponentArea();
			}
		}

		for (String type : types) {
			int tot = 0;			
			for (String classtype : ParseSYNvhdl.CLASSESLIST) {
				if (_type == LEON3Result.ORIG && classtype.equalsIgnoreCase(ParseSYNvhdl.CTRL)) {
					
				} else {
					String key = type+classtype;
					int value = _errCntPairHash.get(key);
					tot += value;
					totTot += value;
					if (first.getDetectTypes().contains(type)) {
						totrTot += value;
					} else {
						toteTot += value;
					}
				}
			}
			vertTots.put(type, tot);
		}
		
		
		pw.append(String.format("%1$-15s", "CLASS"));
		for (String title : types) {
			pw.append(String.format("%1$8.6s", title));
		}
		pw.append(String.format("%1$8s", "TOTR"));
		pw.append(String.format("%1$8s", "TOTER"));
		pw.append(String.format("%1$8s", "TOTTOT"));
		pw.append(String.format("%1$8s", "unACE"));
		pw.append("\n");

		pw.append(String.format("%1$-15s", "====="));
		for (String title : types) {
			pw.append(String.format("%1$8.6s", strReplicate("=", title.length())));
		}
		pw.append(String.format("%1$8s", "===="));
		pw.append(String.format("%1$8s", "====="));
		pw.append(String.format("%1$8s", "======"));
		pw.append(String.format("%1$8s", "====="));
		pw.append("\n");
		
		for (String classtype : ParseSYNvhdl.CLASSESLIST) {
			pw.append(String.format("%1$-15.14s", classtype));

			for (String type : types) {
				String key = type+classtype;
				int value = _errCntPairHash.get(key);
				int tot = vertTots.get(type);
				double valP = (tot == 0) ? 0 : 100 * ((double) value/ (double)tot);
				pw.append(String.format("%1$8.2f", valP));
			}
			
			int totr = 0;
			for (String detect : first.getDetectTypes()) {
				String key = detect+classtype;
				int value = _errCntPairHash.get(key);
				totr += value;
			}
			
			int totd = 0;
			for (String edetect : first.getERRDetectTypes()) {
				String key = edetect+classtype;
				int value = _errCntPairHash.get(key);
				totd += value;
			}
			
			Integer dloop = _errCntPairHash.get(LEON3Result.DLOOP+classtype);
			int dloopint = (dloop == null) ? 0 : dloop.intValue();
			int tote = totd + _errCntPairHash.get(LEON3Result.SDC+classtype) + dloopint + 
						_errCntPairHash.get(LEON3Result.TRUESTUCK+classtype);
			VHDLComponent comp = typeComponentMap.get(classtype);
			int ace = (comp == null) ? 0 : comp.getComponentArea() - (tote + totr);
			
			double totrP = (totrTot == 0) ? 0 : 100 * ((double) totr / (double)totrTot);
			double toteP = (toteTot == 0) ? 0 : 100 * ((double) tote / (double)toteTot);
			double totP = (totTot == 0) ? 0 : 100 * ((double) (tote + totr) / (double)totTot);			
			double totaceP = (totAce == 0) ? 0 : 100 * ((double) ace / (double)totAce);			
			
			pw.append(String.format("%1$8.2f", totrP)); 
			pw.append(String.format("%1$8.2f", toteP));
			pw.append(String.format("%1$8.2f", totP));
			pw.append(String.format("%1$8.2f", totaceP));
			pw.append("\n");
			
		}				
		pw.append("\n");
	
	}

	public void writeHorizontalPercentageDetections(PrintWriter pw) {
		HashMap<String, VHDLComponent> typeComponentMap = _parse.getTypeComponentMap();
		
		LEON3Result first = _xrtcHash.keySet().iterator().next();
		ArrayList<String> types = first.getAllDetectTypes();
		HashMap<String, Integer> horizTots = new HashMap<String, Integer>();

		for (String classtype : ParseSYNvhdl.CLASSESLIST) {
			int tot = 0;			
			for (String type : types) {
				if (!type.equalsIgnoreCase(LEON3Result.RECONFIG)) {
					String key = type+classtype;
					int value = _errCntPairHash.get(key);
					tot += value;
				}
			}
			horizTots.put(classtype, tot);
		}
		
		
		pw.append(String.format("%1$-15s", "CLASS"));
		for (String title : types) {
			pw.append(String.format("%1$8.6s", title));
		}
		pw.append(String.format("%1$8s", "TOTR"));
		pw.append(String.format("%1$8s", "TOTER"));
		pw.append(String.format("%1$8s", "TOTTOT"));
		pw.append(String.format("%1$8s", "unACE"));
		pw.append("\n");

		pw.append(String.format("%1$-15s", "====="));
		for (String title : types) {
			pw.append(String.format("%1$8.6s", strReplicate("=", title.length())));
		}
		pw.append(String.format("%1$8s", "===="));
		pw.append(String.format("%1$8s", "====="));
		pw.append(String.format("%1$8s", "======"));
		pw.append(String.format("%1$8s", "====="));
		pw.append("\n");
		
		for (String classtype : ParseSYNvhdl.CLASSESLIST) {
			pw.append(String.format("%1$-15.14s", classtype));

			int tot = horizTots.get(classtype);
			for (String type : types) {
				String key = type+classtype;
				int value = _errCntPairHash.get(key);
				double valP = (tot == 0) ? 0 : 100 * ((double) value/ (double)tot);
				pw.append(String.format("%1$8.2f", valP));
			}
			
			int totr = 0;
			for (String detect : first.getDetectTypes()) {
				String key = detect+classtype;
				int value = _errCntPairHash.get(key);
				totr += value;
			}
			
			int totd = 0;
			for (String edetect : first.getERRDetectTypes()) {
				String key = edetect+classtype;
				int value = _errCntPairHash.get(key);
				totd += value;
			}
			
			Integer dloop = _errCntPairHash.get(LEON3Result.DLOOP+classtype);
			int dloopint = (dloop == null) ? 0 : dloop.intValue();
			int tote = totd + _errCntPairHash.get(LEON3Result.SDC+classtype) + dloopint + 
						_errCntPairHash.get(LEON3Result.TRUESTUCK+classtype);
			
			VHDLComponent comp = typeComponentMap.get(classtype);
			int area = (comp == null) ? 0 : comp.getComponentArea(); 
			int unace = (comp == null) ? 0 : area - tot;

			double totrP = (tot == 0) ? 0 : 100 * ((double) totr / (double)tot);
			double toteP = (tot == 0) ? 0 : 100 * ((double) tote / (double)tot);
			double totP = (tot == 0) ? 0 : 100 * ((double) (tote + totr) / (double)tot);
			double totunaceP = (area == 0) ? 0 : 100 * ((double) unace / (double)area);
			pw.append(String.format("%1$8.2f", totrP)); 
			pw.append(String.format("%1$8.2f", toteP));
			pw.append(String.format("%1$8.2f", totP));
			pw.append(String.format("%1$8.2f", totunaceP));
			pw.append("\n");
			
		}				
		pw.append("\n");
	
	}
	
	public void writeTraditionalArea(PrintWriter pw) {
		HashMap<String, VHDLComponent> typeComponentMap = _parse.getTypeComponentMap();

		pw.append(String.format("%1$-15s", "CLASS"));
		pw.append(String.format("%1$8s", "Slices"));
		pw.append(String.format("%1$8s", "BRAMs"));
		pw.append(String.format("%1$8s", "DSPs"));		
		pw.append(String.format("%1$8s", "FFs"));		
		pw.append("\n");
		pw.append(String.format("%1$-15s", "======"));
		pw.append(String.format("%1$8s", "======"));
		pw.append(String.format("%1$8s", "====="));
		pw.append(String.format("%1$8s", "===="));
		pw.append(String.format("%1$8s", "==="));
		pw.append("\n");

		int totbrams = 0;
		int totdsps = 0;
		int totslices = 0;
		int totffs = 0;
		for (String classtype : ParseSYNvhdl.CLASSESLIST) {
			VHDLComponent comp = typeComponentMap.get(classtype);
			if (comp != null) {
				comp.calculateTraditionalArea();
				pw.append(String.format("%1$-15.14s", comp.getName()));
				int slices = comp.getSlices();
				int brams = comp.getBRAMs();
				int dsps = comp.getDSPs();
				int ffs = comp.getFFs();
				if (!comp.getName().equalsIgnoreCase(ParseSYNvhdl.GLOBAL)) {
					totbrams += brams;
					totdsps += dsps;
					totslices += slices;
					totffs += ffs;
				}
				pw.append(String.format("%1$8d", slices));
				pw.append(String.format("%1$8d", brams));
				pw.append(String.format("%1$8d", dsps));
				pw.append(String.format("%1$8d", ffs));
				pw.append("\n");				
			}			
		}
		pw.append(String.format("%1$-15s", strReplicate("-", 13)));
		pw.append(String.format("%1$8s", strReplicate("-", 7)));
		pw.append(String.format("%1$8s", strReplicate("-", 7)));
		pw.append(String.format("%1$8s", strReplicate("-", 7)));
		pw.append(String.format("%1$8s", strReplicate("-", 7)));
		pw.append("\n");
		pw.append(String.format("%1$-15s", "Total"));
		pw.append(String.format("%1$8s", totslices));
		pw.append(String.format("%1$8s", totbrams));
		pw.append(String.format("%1$8s", totdsps));
		pw.append(String.format("%1$8s", totffs));
		pw.append("\n");				
		pw.append("\n");				

	}
	
	public void writeArea(PrintWriter pw) {
		HashMap<String, VHDLComponent> typeComponentMap = _parse.getTypeComponentMap();
		
		pw.append(String.format("%1$-15s", "AVFCLASS"));
		pw.append(String.format("%1$12s", "AREA"));
		pw.append(String.format("%1$12s", "PERCENT"));
		pw.append(String.format("%1$12s", "AVFsdc"));
		pw.append(String.format("%1$12s", "AVFdue"));
		pw.append(String.format("%1$12s", "AVFrcfg"));
		pw.append(String.format("%1$12s", "AVF"));
		pw.append(String.format("%1$12s", "MITFsdc"));
		pw.append(String.format("%1$12s", "MITFdue"));
		pw.append(String.format("%1$12s", "MITFrcfg"));
		pw.append(String.format("%1$12s", "rMITF"));
		pw.append(String.format("%1$12s", "rMTTF"));
		pw.append("\n");
		pw.append(String.format("%1$-15s", "========="));
		pw.append(String.format("%1$12s", "===="));
		pw.append(String.format("%1$12s", "======="));
		pw.append(String.format("%1$12s", "======"));
		pw.append(String.format("%1$12s", "======"));
		pw.append(String.format("%1$12s", "======="));
		pw.append(String.format("%1$12s", "==="));
		pw.append(String.format("%1$12s", "======="));
		pw.append(String.format("%1$12s", "======="));
		pw.append(String.format("%1$12s", "========"));
		pw.append(String.format("%1$12s", "===="));
		pw.append(String.format("%1$12s", "===="));
		pw.append("\n");
		
		int totArea = 0;
		int totSDC = 0;
		int totDUE = 0;
		int totRECON = 0;
		double totP = 0.0;
		for (VHDLComponent comp : typeComponentMap.values()) {
			if (_type == LEON3Result.ORIG && comp.getName().equalsIgnoreCase(ParseSYNvhdl.CTRL)) {
				totArea += comp.getComponentArea();				
			} else {
				totArea += comp.getComponentArea();				
			}
			totSDC += _sdcHash.get(comp.getName());
			totDUE += _dueHash.get(comp.getName());
			totRECON += _reconfigHash.get(comp.getName());
		}
		
		for (String classtype : ParseSYNvhdl.CLASSESLIST) {
			VHDLComponent comp = typeComponentMap.get(classtype);
			if (comp != null) {
				pw.append(String.format("%1$-15.14s", comp.getName()));
				int area = comp.getComponentArea();
				if (_type == LEON3Result.ORIG && comp.getName().equalsIgnoreCase(ParseSYNvhdl.CTRL)) {
					pw.append(String.format("%1$12s", area));
					pw.append(String.format("%1$12.2f", 0.0));
				}
				else {
					pw.append(String.format("%1$12d", area));
					double areaP = (totArea == 0) ? 0 : 100 * ((double) area/ (double)totArea);
					pw.append(String.format("%1$12.2f", areaP));
					totP += areaP;
				}
				int sdc = _sdcHash.get(classtype);
				int due = _dueHash.get(classtype);
				int recon = _reconfigHash.get(classtype);
				//double factor = (totArea == 0) ? 1 : (double)area / (double)((double)totArea * _performancePenalty); 
				double factor = (area == 0) ? 1 : (double)totArea / (double)((double)area * _performancePenalty); 
				double factor2 = (area == 0) ? 1 : (double)totArea / (double)((double)area); 
				double ace = sdc + due;
				
				double avfsdc = (area == 0) ? 0 : 100 * ((double) sdc/ (double)area);
				double avfdue = (area == 0) ? 0 : 100 * ((double) due/ (double)area);
				double avfrecon = (area == 0) ? 0 : 100 * ((double) recon/ (double)area);
				double avf = (area == 0) ? 0 : 100 * ((double) (sdc + due)/ (double)area);
//				double mitfsdc = (avfsdc == 0) ? 0 : 100 * factor * (_IPC / avfsdc);
//				double mitfdue = (avfdue == 0) ? 0 : 100 * factor * (_IPC / avfdue);
//				double mitfrecon = (avfrecon == 0) ? 0 : 100 * factor * (_IPC / avfrecon);
//				double mitf = (avf == 0) ? 0 : 100 * factor * (_IPC / avf);
//				double rmttf = (avf == 0) ? 0 : 100 * factor2 * (1 / avf);
				double mitfsdc = (sdc == 0) ? 0 : 1000 * 1000 * (_IPC / (sdc * _performancePenalty));
				double mitfdue = (due == 0) ? 0 : 1000 * 1000 * (_IPC / (due * _performancePenalty));
				double mitfrecon = (recon == 0) ? 0 : 1000 * 1000 * (_IPC / (recon * _performancePenalty));
				double mitf = (ace == 0) ? 0 : 1000 * 1000 * (_IPC / (ace * _performancePenalty));
				double rmttf = (ace == 0) ? 0 : 1000 * 1000 * (1 / ace);
				pw.append(String.format("%1$12.2f", avfsdc));
				pw.append(String.format("%1$12.2f", avfdue));
				pw.append(String.format("%1$12.2f", avfrecon));
				pw.append(String.format("%1$12.2f", avf));
				pw.append(String.format("%1$12.2f", mitfsdc));
				pw.append(String.format("%1$12.2f", mitfdue));
				pw.append(String.format("%1$12.2f", mitfrecon));
				pw.append(String.format("%1$12.2f", mitf));
				pw.append(String.format("%1$12.2f", rmttf));
				pw.append("\n");
				
			}
		}
		double avfsdc = (totArea == 0) ? 0 : 100 * ((double) totSDC / (double)totArea);
		double avfdue = (totArea == 0) ? 0 : 100 * ((double) totDUE / (double)totArea);
		double avfrecon = (totArea == 0) ? 0 : 100 * ((double) totRECON / (double)totArea);
		double avf = (totArea == 0) ? 0 : 100 * ((double) (totSDC + totDUE)/ (double)totArea);
		double ace = totSDC + totDUE;
//		double mitfsdc = (avfsdc == 0) ? 0 : 100 * (_IPC / (avfsdc * _performancePenalty));
//		double mitfdue = (avfdue == 0) ? 0 : 100 * (_IPC / (avfdue * _performancePenalty));
//		double mitfrecon = (avfrecon == 0) ? 0 : 100 * (_IPC / (avfrecon * _performancePenalty));
//		double mitf = (avf == 0) ? 0 : 100 * (_IPC / (avf * _performancePenalty));
//		double rmttf = (avf == 0) ? 0 : 100 * (1 / avf);
		double mitfsdc = (totSDC == 0) ? 0 : 1000 * 1000 * (_IPC / (totSDC * _performancePenalty));
		double mitfdue = (totDUE == 0) ? 0 : 1000 * 1000 * (_IPC / (totDUE * _performancePenalty));
		double mitfrecon = (totRECON == 0) ? 0 : 1000 * 1000 * (_IPC / (totRECON * _performancePenalty));
		double mitf = (ace == 0) ? 0 : 1000 * 1000 * (_IPC / (ace * _performancePenalty));
		double rmttf = (ace == 0) ? 0 : 1000 * 1000 * (1 / ace);
		
		pw.append(String.format("%1$-15s", strReplicate("-", 13)));
		pw.append(String.format("%1$12s", strReplicate("-", 9)));
		pw.append(String.format("%1$12s", strReplicate("-", 7)));
		pw.append(String.format("%1$12s", strReplicate("-", 7)));
		pw.append(String.format("%1$12s", strReplicate("-", 7)));
		pw.append(String.format("%1$12s", strReplicate("-", 7)));
		pw.append(String.format("%1$12s", strReplicate("-", 7)));
		pw.append(String.format("%1$12s", strReplicate("-", 11)));
		pw.append(String.format("%1$12s", strReplicate("-", 11)));
		pw.append(String.format("%1$12s", strReplicate("-", 11)));
		pw.append(String.format("%1$12s", strReplicate("-", 11)));
		pw.append(String.format("%1$12s", strReplicate("-", 11)));
		pw.append("\n");
		pw.append(String.format("%1$-15s", "Total"));
		pw.append(String.format("%1$12s", totArea));
		pw.append(String.format("%1$12.2f", totP));
		pw.append(String.format("%1$12.2f", avfsdc));
		pw.append(String.format("%1$12.2f", avfdue));
		pw.append(String.format("%1$12.2f", avfrecon));
		pw.append(String.format("%1$12.2f", avf));
		pw.append(String.format("%1$12.2f", mitfsdc));
		pw.append(String.format("%1$12.2f", mitfdue));
		pw.append(String.format("%1$12.2f", mitfrecon));
		pw.append(String.format("%1$12.2f", mitf));
		pw.append(String.format("%1$12.2f", rmttf));
		pw.append(String.format("%1$12d", totSDC));
		pw.append("\n");
		pw.append("\n");		
		
	}
	
	public void writeFailureClassificationSummary(PrintWriter pw) {		
		int totFullcnt = 0;
		int totcnt = 0;
		for (Integer type : _fullFailHash.keySet()) {
			totFullcnt += _fullFailHash.get(type);
		}
		for (Integer type : _failHash.keySet()) {
			totcnt += _failHash.get(type);
		}
		
		pw.append(String.format("%1$-15s", "FULL TYPE"));
		pw.append(String.format("%1$8s", "COUNT"));
		pw.append(String.format("%1$8s", "PERCENT"));
		pw.append("\n");
		pw.append(String.format("%1$-15s", "========="));
		pw.append(String.format("%1$8s", "====="));
		pw.append(String.format("%1$8s", "======="));
		pw.append("\n");
		for (Integer type : SensitiveBit.fullTypeList) {
			int cnt = _fullFailHash.get(type);
			pw.append(String.format("%1$-15.14s", SensitiveBit.failTypeString(type)));
			pw.append(String.format("%1$8d", cnt));
			double cntP = (totFullcnt == 0) ? 0 : 100 * ((double) cnt/ (double)totFullcnt);
			pw.append(String.format("%1$8.2f", cntP));
			pw.append("\n");					
		}
		
		pw.append("\n");		
		pw.append(String.format("%1$-15s", "CAT TYPE"));
		pw.append(String.format("%1$8s", "COUNT"));
		pw.append(String.format("%1$8s", "PERCENT"));
		pw.append("\n");
		pw.append(String.format("%1$-15s", "========="));
		pw.append(String.format("%1$8s", "====="));
		pw.append(String.format("%1$8s", "======="));
		pw.append("\n");
		for (Integer type : SensitiveBit.categoryTypeList) {
			int cnt = _failHash.get(type);
			pw.append(String.format("%1$-15.14s", SensitiveBit.failTypeString(type)));
			pw.append(String.format("%1$8d", cnt));
			double cntP = (totcnt == 0) ? 0 : 100 * ((double) cnt/ (double)totcnt);
			pw.append(String.format("%1$8.2f", cntP));
			pw.append("\n");					
		}
		pw.append("\n");		
		
	}
	
	public void writeFailureClassification(PrintWriter pw) {
		HashMap<String, VHDLComponent> typeComponentMap = _parse.getTypeComponentMap();
		
		for (LEON3Result result : _xrtcHash.keySet()) {
			int failType = result.getSensitiveBit().getFailType();
			int category = SensitiveBit.categorizeFailureType(failType);
			String classType = result.getProcessorClass();
			VHDLComponent comp = typeComponentMap.get(classType);
			if (comp != null) {
				HashMap<Integer, Integer> failmap = comp.getFailHash();
				Integer cnt = failmap.get(category);
				if (cnt == null) {
					failmap.put(category, 1);
				} else {
					cnt++;
					failmap.put(category, cnt);
				}
			}		
		}	
		
		pw.append(String.format("%1$-15s", "CLASS"));
		pw.append(String.format("%1$8s", "INST"));
		pw.append(String.format("%1$8s", "SHORT"));
		pw.append(String.format("%1$8s", "OPEN"));
		pw.append(String.format("%1$8s", "CLKFAIL"));
		pw.append(String.format("%1$8s", "UNKNOWN"));
		pw.append(String.format("%1$8s", "INST"));
		pw.append(String.format("%1$8s", "SHORT"));
		pw.append(String.format("%1$8s", "OPEN"));
		pw.append(String.format("%1$8s", "CLKFAIL"));
		pw.append(String.format("%1$8s", "UNKNOWN"));		
		pw.append("\n");
		pw.append(String.format("%1$-15s", "====="));
		pw.append(String.format("%1$8s", "===="));
		pw.append(String.format("%1$8s", "====="));
		pw.append(String.format("%1$8s", "===="));
		pw.append(String.format("%1$8s", "======="));
		pw.append(String.format("%1$8s", "======="));
		pw.append(String.format("%1$8s", "===="));
		pw.append(String.format("%1$8s", "====="));
		pw.append(String.format("%1$8s", "===="));
		pw.append(String.format("%1$8s", "======="));
		pw.append(String.format("%1$8s", "======="));
		pw.append("\n");
		
		for (String classtype : ParseSYNvhdl.CLASSESLIST) {
			VHDLComponent comp = typeComponentMap.get(classtype);
			if (comp != null) {
				pw.append(String.format("%1$-15.14s", comp.getName()));
				HashMap<Integer, Integer> failmap = comp.getFailHash();
				int inst = (failmap == null || failmap.get(SensitiveBit.INSTANCE) == null) ? 0 : failmap.get(SensitiveBit.INSTANCE);
				int shrt = (failmap == null || failmap.get(SensitiveBit.SHORT) == null) ? 0 : failmap.get(SensitiveBit.SHORT);
				int open = (failmap == null || failmap.get(SensitiveBit.OPEN) == null) ? 0 : failmap.get(SensitiveBit.OPEN);
				int clkfail = (failmap == null || failmap.get(SensitiveBit.CLOCK_FAILURE) == null) ? 0 : failmap.get(SensitiveBit.CLOCK_FAILURE);
				int unknown = (failmap == null || failmap.get(SensitiveBit.UNKNOWN) == null) ? 0 : failmap.get(SensitiveBit.UNKNOWN);
				int tot = inst + shrt + open + clkfail + unknown;
				double instP = (tot == 0) ? 0 : 100 * ((double) inst / (double)tot);
				double shrtP = (tot == 0) ? 0 : 100 * ((double) shrt / (double)tot);
				double openP = (tot == 0) ? 0 : 100 * ((double) open / (double)tot);
				double clkfailP = (tot == 0) ? 0 : 100 * ((double) clkfail / (double)tot);
				double unknownP = (tot == 0) ? 0 : 100 * ((double) unknown / (double)tot);

				pw.append(String.format("%1$8s", inst));
				pw.append(String.format("%1$8s", shrt));
				pw.append(String.format("%1$8s", open));
				pw.append(String.format("%1$8s", clkfail));
				pw.append(String.format("%1$8s", unknown));				
				pw.append(String.format("%1$8.2f", instP));
				pw.append(String.format("%1$8.2f", shrtP));
				pw.append(String.format("%1$8.2f", openP));
				pw.append(String.format("%1$8.2f", clkfailP));
				pw.append(String.format("%1$8.2f", unknownP));
				pw.append("\n");
			}
		}
		pw.append("\n");		

	}
	
	public void writeXRTCOutput(PrintWriter pw) {
		for (LEON3Result result : _xrtcHash.keySet()) {
			pw.append(result+"\n");
		}
	}
	
	public void writeJoshXRTCOutput(PrintWriter pw) {
		for (LEON3Result result : _xrtcHash.keySet()) {
			if (result.getDetectType().startsWith(LEON3Result.ERR) || 
				result.getDetectType().equalsIgnoreCase(LEON3Result.DLOOP) || 
				result.getDetectType().equalsIgnoreCase(LEON3Result.RECONFIG) ||
				result.getDetectType().equalsIgnoreCase(LEON3Result.SDC) ||
				result.getDetectType().equalsIgnoreCase(LEON3Result.TRUESTUCK))
				pw.append(Integer.toHexString(result.getFAR())+"  "+result.getFrameBit()+"\n");
		}
	}
	
    public static void main(String args []) {
    	_parseArgs(args);
    	ReliabilityAnalyzerJP analyzer = new ReliabilityAnalyzerJP(_xdlFile, _farFile);        
        
        
    	/*if (_joshOutput) {
	        PrintWriter pw = openForWriting(_outFile);
	        analyzer.writeJoshXRTCOutput(pw);
	        pw.close();
    	} else {
	    	analyzer.printReports(); 
	    	System.out.println("TOTAL BITS: "+analyzer.getXRTCHash().keySet().size());
	    	System.out.println("TOTAL BITS CONSIDERED: "+analyzer.getNumBitsConsidered());
	    	System.out.println("UNANALIZED BITS: "+analyzer.getBitsNotAnalyzed().size());
	        PrintWriter pw = openForWriting(_outFile);
	        analyzer.writeSummary(pw);
	        analyzer.writeDetections(pw);
	        analyzer.writeVerticalPercentageDetections(pw);
	        analyzer.writeHorizontalPercentageDetections(pw);
	        analyzer.writeTraditionalArea(pw);
	        analyzer.writeArea(pw);
	        analyzer.writeFailureClassificationSummary(pw);
	        analyzer.writeFailureClassification(pw);
	        analyzer.writeXRTCOutput(pw);
	        analyzer.writeNathanReportToFile(_netoutFile, _instoutFile);
	        pw.close();
    	}
*/
    }

	/**
	 * Creates a PrintWriter Object for writing to - this removes the need for ugly try/catches
	 * when opening a file for writing.
	 * 
	 * @param fileName - the name of the file to open for writing
	 * @return - the PrintWriter Object  
	 */
	public static PrintWriter openForWriting(String fileName) {
		PrintWriter fp = null;		
		try {
	        fp = new PrintWriter(new FileOutputStream(fileName));
	    } catch (IOException e) {
	        throw new RuntimeException("ERROR: Cannot create file:"+ fileName+" for writing");
	    }
	    
	    return fp;
	}
	
	public static BufferedReader openForReading(String fileName) {
		BufferedReader bf = null;
		try {
			bf = new BufferedReader(new FileReader(fileName));
		} catch (IOException e) {
	        throw new RuntimeException("ERROR: Cannot open file:"+ fileName+" for reading");			
		}
		
		return bf;
	}
	
	protected void _analyze() {
		
		_errCntHash = new HashMap<String, Integer>();
		_errLatencyHash = new HashMap<String, Integer>();
		_errCntPairHash = new HashMap<String, Integer>();
		_fullFailHash = new HashMap<Integer, Integer>();
		_failHash = new HashMap<Integer, Integer>();
		
		LEON3Result first = _xrtcHash.keySet().iterator().next();
		for (String type : first.getAllDetectTypes()) {
			_errCntHash.put(type, 0);
			_errLatencyHash.put(type, 0);
			for (String pclass : ParseSYNvhdl.CLASSESLIST) {
				String key = type+pclass;
				_errCntPairHash.put(key, 0);
			}
		}
		
		for (Integer type : SensitiveBit.fullTypeList) {
			_fullFailHash.put(type, 0);
		}
		for (Integer type : SensitiveBit.categoryTypeList) {
			_failHash.put(type, 0);
		}

		
		for (LEON3Result result : _xrtcHash.keySet()) {
			String detectType = result.getDetectType();
			String classType = result.getProcessorClass();
			String key = detectType+classType;
//			if (result.getTop() > 0) {
				int cnt = _errCntHash.get(detectType);
				_errCntHash.put(detectType, cnt+1);
			
				int latency = _errLatencyHash.get(detectType) + result.getDetectionLatency();
				_errLatencyHash.put(detectType, latency);
				
				cnt = _errCntPairHash.get(key);
				_errCntPairHash.put(key, cnt + 1);
				
				int failType = result.getSensitiveBit().getFailType();
				int category = SensitiveBit.categorizeFailureType(failType);
				int fullfailcnt = _fullFailHash.get(failType);
				int failcnt = _failHash.get(category);
				_fullFailHash.put(failType, fullfailcnt + 1);
				_failHash.put(category, failcnt + 1);
				
//			}
		}		
		
		int totsize = 0;
		_totalLatency = 0;
		for (String key : _errLatencyHash.keySet()) {
			int latency = _errLatencyHash.get(key);
			int size = _errCntHash.get(key);
			int newlatency = (size == 0) ? 0 : latency / size;
			_errLatencyHash.put(key, newlatency);
//			if (LEON3Result.isDetectable(key)) {
				totsize += size;
				_totalLatency += latency;
//			}
		}
		_totalLatency = (totsize == 0) ? 0 : _totalLatency / totsize;
		
		
		_totXRTCcnt = 0;
		_repairXRTCcnt = 0;
		_detectXRTCcnt = 0;
		for (String repair : first.getDetectTypes()) {
			_repairXRTCcnt += _errCntHash.get(repair);
		}
		
		_detectXRTCcnt = _repairXRTCcnt;
		for (String detect : first.getERRDetectTypes()) {
			_detectXRTCcnt += _errCntHash.get(detect);
		}
		
		if (_errCntHash.get(LEON3Result.DLOOP) != null)
			_detectXRTCcnt += _errCntHash.get(LEON3Result.DLOOP);
		
		if (_type != LEON3Result.ORIG && _type != LEON3Result.TMR)
			_detectXRTCcnt += _errCntHash.get(LEON3Result.ERR);
//		if (_type != LEON3Result.TMR)
//		_detectXRTCcnt += _errCntHash.get(LEON3Result.ERR);
				
		_totXRTCcnt = _detectXRTCcnt + _errCntHash.get(LEON3Result.SDC) + _errCntHash.get(LEON3Result.TRUESTUCK);

		System.out.println("TOT: "+_totXRTCcnt+"  REP: "+_repairXRTCcnt+"  DET: "+_detectXRTCcnt);
		
	}
	
	protected void _parseXDL() {
		
		for (Net net : design.getNets()) {
			String netName = net.getName().toLowerCase();
			System.out.println("XDL NET NAME: "+netName);
		}
		for (Instance inst : design.getInstances()) {
			String instName = inst.getName().toLowerCase();
			if (instName.endsWith("/ramb16")) {
				instName = instName.replace("/ramb16", "");
			}
			System.out.println("XDL INST NAME: "+instName);
			
		}
			
			
	}
	
	protected void _resetSignalComponentHash() {
		HashMap<String, VHDLComponent> signalComponentMap = new HashMap<String, VHDLComponent>();
		HashMap<String, VHDLComponent> typeComponentMap = _parse.getTypeComponentMap();
		
		for (VHDLComponent comp : typeComponentMap.values()) {
			for (Iterator<VHDLSignal> it = comp.getSignalList().iterator(); it.hasNext(); ) {
				VHDLSignal signal = it.next();
				if (signal != null)
					signalComponentMap.put(signal.getFullSignalName(), comp);
			}
		}
		
		_parse.setSignalComponentMap(signalComponentMap);
	}
	
	protected void _determineComponentArea() {
		HashMap<String, VHDLComponent> typeComponentMap = _parse.getTypeComponentMap();
		for (VHDLComponent comp : typeComponentMap.values()) {
			Collection<Net> nets = comp.getSignalNetMap().values();
			Collection<Instance> instances = comp.getSignalInstanceMap().values();
			int netArea = BSA.calcInterconnectArea(nets);
			int logicArea = BSA.calcLogicArea(instances);
			comp.setComponentArea(netArea+logicArea);
		}
		
	}
	
	public void printSignals() {
		HashMap<String, VHDLComponent> signalComponentMap = _parse.getSignalComponentMap();
		for (String signal : signalComponentMap.keySet()) {
			VHDLComponent comp = signalComponentMap.get(signal);
			System.out.println("SIG: "+signal+"  TYPE: "+comp.getName()+"  AREA: "+comp.getComponentArea());
		}
		
	}
	
	public void printComponents() {
		HashMap<String, VHDLComponent> typeComponentMap = _parse.getTypeComponentMap();

		for (String type : ParseSYNvhdl.CLASSESLIST) {
			System.out.println("PRINT "+type);
			VHDLComponent comp = typeComponentMap.get(type);
			if (comp != null) {
				System.out.println("INSTS: "+comp.getSignalInstanceMap().size()+"  NETS: "+comp.getSignalNetMap().size());
				for (Iterator<VHDLSignal> it = comp.getSignalList().iterator(); it.hasNext(); ) {
					VHDLSignal signal = it.next();
					if (signal != null)
						System.out.println("\t"+signal.getFullSignalName());
				}
			}
		}
	}

	protected void _fillClassTypes() {
		
		HashMap<String, VHDLComponent> signalComponentMap = _parse.getSignalComponentMap();
				
		for (LEON3Result result : _xrtcHash.keySet()) {
			String name = result.getSensitiveBit().getName().toLowerCase();
			
			if (name != null && name != "") {
				name = name.toLowerCase();
				VHDLComponent comp = signalComponentMap.get(name);
				if (comp != null) {
					String type = comp.getName();
					System.out.println("NAME: "+name+"  TYPE: "+type);
					
					if (type != null && !type.equalsIgnoreCase(ParseSYNvhdl.UNKNOWN)) {
						result.setProcessorClass(type);
					}
				}				
				
			} else if (result.getSensitiveBit().getFailType() == SensitiveBit.CLOCK_COLUMN_ERROR) {
				result.setProcessorClass(ParseSYNvhdl.CLK);
			} else if (result.getSensitiveBit().getFailType() == SensitiveBit.BRAM_ATTRIBUTE_FAILURE) {
				result.setProcessorClass(ParseSYNvhdl.BRAM_ATTR);
			} else if (result.getSensitiveBit().getFailType() == SensitiveBit.IOB_ATTRIBUTE_FAILURE) {
				result.setProcessorClass(ParseSYNvhdl.IOB);
			} else if (result.getSensitiveBit().getFailType() == SensitiveBit.DSP_ATTRIBUTE_FAILURE) {
				result.setProcessorClass(ParseSYNvhdl.DSP);
			} else {
				result.setProcessorClass(ParseSYNvhdl.NONET);
			}
			
		}
	}
	
	protected void _reassignTypeComp(VHDLComponent oldComp, VHDLComponent newComp) {
		
		HashMap<String, VHDLComponent> signalComponentMap = _parse.getSignalComponentMap();
		ArrayList<VHDLSignal> newList = new ArrayList<VHDLSignal>();
		newList.addAll(oldComp.getSignalList());
		for (VHDLSignal signal : newList) {
			VHDLSignal rsignal = oldComp.removeSignal(signal.getFullSignalName());
			signalComponentMap.put(signal.getFullSignalName(), newComp);
			newComp.addSignal(rsignal);
		}
		newComp.getSignalNetMap().putAll(oldComp.removeAllNets());
		newComp.getSignalInstanceMap().putAll(oldComp.removeAllInstances());
	}
	
	protected void _groupTypes() {
		HashMap<String, VHDLComponent> typeComponentMap = _parse.getTypeComponentMap();
		for (String type : typeComponentMap.keySet()) {
			VHDLComponent comp = typeComponentMap.get(type);
			if (type.equalsIgnoreCase(ParseSYNvhdl.ICACHECTRL) ||
				type.equalsIgnoreCase(ParseSYNvhdl.ITAG) ||
				type.equalsIgnoreCase(ParseSYNvhdl.CACHECTRL)) {
				_reassignTypeComp(comp, typeComponentMap.get(ParseSYNvhdl.ICACHE));
			} else if (type.equalsIgnoreCase(ParseSYNvhdl.DCACHECTRL) ||
					   type.equalsIgnoreCase(ParseSYNvhdl.DTAG) ||
					   type.equalsIgnoreCase(ParseSYNvhdl.CACHE)) {
				_reassignTypeComp(comp, typeComponentMap.get(ParseSYNvhdl.DCACHE));
			} else if (type.equalsIgnoreCase(ParseSYNvhdl.MMCACHECTRL)) {
				_reassignTypeComp(comp, typeComponentMap.get(ParseSYNvhdl.MM));
			} else if (type.equalsIgnoreCase(ParseSYNvhdl.CTRL) && _type == LEON3Result.ORIG) {
				_reassignTypeComp(comp, typeComponentMap.get(ParseSYNvhdl.IU3));
			} else if (type.equalsIgnoreCase(ParseSYNvhdl.GLOBAL)) {
				_reassignTypeComp(comp, typeComponentMap.get(ParseSYNvhdl.RST));
			}
		}
	}
	
	protected void _splitPipeTypes() {
		for (LEON3Result result : _xrtcHash.keySet()) {
			if (result.getProcessorClass().equalsIgnoreCase(ParseSYNvhdl.IU3)) {
				String name = result.getSensitiveBit().getName().toLowerCase();
				if (name != null && name != "") {
					if (name.contains("result") || name.contains("alu") || name.contains("add") || 
						name.contains("logic") || name.contains("add") || name.contains("shift") ||
						name.contains("op1") || name.contains("op2")) {
						
						result.setProcessorClass(ParseSYNvhdl.ALU);
					}
				}
			}
			
		}				
	}
	
	protected int _parseXRTCfile() {
//    	_xrtcHash = new HashMap<LEON3Result, V4ConfigurationBit>();
    	_xrtcHash = new TreeMap<LEON3Result, V4ConfigurationBit>();
    	int retval = 0;
    	String line = "";
    	try {
			BufferedReader br = new BufferedReader(new FileReader(_farFile));
			while ((line = br.readLine()) !=  null) {
				if (line.length() > 0) {
					LEON3Result result = new LEON3Result(line, _type);
					FrameAddressRegister far = new FrameAddressRegister(fpga.getDeviceSpecification(), result.getFAR());
					if (far.getBlockType() < 2) {
						Bit bit = new Bit(result.getFAR(), result.getFrameBit(), BitNumberType.Gremlin);
						IdentifyMaskOffSet(bit, far, TB);
						V4ConfigurationBit cbit = new V4ConfigurationBit(fpga, bit, far.getAddress(), TB, design.getDevice());
						_xrtcHash.put(result, cbit);
						retval++;
					} else {
						_xrtcHash.put(result, null);
					}					
				}
				
			}

    	} catch (Exception e) {
			e.printStackTrace();
    	}
    	
    	return retval;
    }
    
	/**
	 * Parses the command-line arguments
	 * 
	 * @param args
	 */	
	protected static void _parseArgs(String[] args) {
		if (args.length < 2) {
			System.out.println(_usage());
			System.exit(-1);
		}
		
		if (!args[0].contains(".xdl")) {
			System.out.println("XDL file must be first");
			System.out.println(_usage());
			System.exit(-1);
		}

		/*if (!args[1].contains(".vhd") && !args[1].contains(".vhm")) {
			System.out.println("VHD file must be second");
			System.out.println(_usage());
			System.exit(-1);
		}*/
		
		_xdlFile = args[0];
		//_vhdFile = args[1];
		_farFile = args[1];
		
		for (int i = 3; i < args.length; i++) {
			if (args[i].equals("-h") || args[i].equals("-help")) {
				System.out.println(_usage());
				System.exit(0);
			} else if (args[i].equals("-dwc")) {
				_type = LEON3Result.DWC;
			} else if (args[i].equals("-orig")) {
				_type = LEON3Result.ORIG;
			} else if (args[i].equals("-sw")) {
				_type = LEON3Result.SW;
			} else if (args[i].equals("-tmr")) {
				_type = LEON3Result.TMR;
			} else if (args[i].equals("-o")) {
				i++;
				_outFile = args[i];
			} else if (args[i].equals("-josh")) {
				_joshOutput = true;
			} else if (args[i].equals("-skip")) {
				_skipDetails = true;
			} else {
				System.out.println(_usage());
				System.exit(-1);				
			}
		}
	}

	protected static String _usage() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("ReliabilityAnalyzer Usage: java edu.byu.ece.gremlinTools.ReliabilityTools.ReliabilityAnalyzer <xdl file> <vhd file> <xrtc file> [options]\n");
		sb.append("options:\n");
		sb.append("\t-h | -help \t Print this message\n");
		sb.append("\t-o <filename> \t Specify the output file name.\n");
		sb.append("\t-dwc \t\t The design is a DWC design.\n");
		sb.append("\t-orig \t\t The design is the original design.\n");
		sb.append("\t-sw \t\t The design is the SW techniques design (Default).\n");
		sb.append("\t-tmr \t\t The design is a TMR design.\n");
		sb.append("\t-skip \t\t Skip the detailed net and instance analysis.\n");
		sb.append("\n");
		
		return sb.toString();
	}	  
    
	
	protected double _IPC = 0.62;
	protected double _performancePenalty = 1.0;
    protected TreeMap<LEON3Result, V4ConfigurationBit> _xrtcHash;
    protected ParseSYNvhdl _parse;
//    protected HashMap<LEON3Result, V4ConfigurationBit> _xrtcHash;
    protected HashMap<String, Integer> _errCntHash;
    protected HashMap<String, Integer> _errLatencyHash;
    protected HashMap<String, Integer> _errCntPairHash;
    protected HashMap<Integer, Integer> _fullFailHash;
    protected HashMap<Integer, Integer> _failHash;    
    
    protected HashMap<String, Integer> _areaHash;
    protected HashMap<String, Integer> _sdcHash;
    protected HashMap<String, Integer> _dueHash;
    protected HashMap<String, Integer> _reconfigHash;
    
    protected ArrayList<LEON3Result> _bitsNotAnalyzed;
    protected int _totXRTCcnt;
    protected int _repairXRTCcnt;
    protected int _detectXRTCcnt;
    protected int _numBitsConsidered;
    protected int _totalLatency;
    
    protected static String _vhdFile;
    protected static String _outFile = "results.dat";
    protected static int _type = LEON3Result.SW;    
    protected static boolean _skipDetails = false;
    protected static boolean _joshOutput = false;
    
}

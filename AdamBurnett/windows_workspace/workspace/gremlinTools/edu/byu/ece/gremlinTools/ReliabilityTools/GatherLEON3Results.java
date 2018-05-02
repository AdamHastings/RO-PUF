package edu.byu.ece.gremlinTools.ReliabilityTools;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class GatherLEON3Results {
	
	public GatherLEON3Results(int type) {
		_allData = new ArrayList<ArrayList<ArrayList<String>>>();
		_avgData = new ArrayList<ArrayList<String>>();
		_whiteData = new ArrayList<ArrayList<String>>();
		
		switch (type) {
			case LEON3Result.SW:
				_farPostfix = "swMem.dat";
				break;
			case LEON3Result.DWC:
				_farPostfix = "dupMem.dat";
				break;
			case LEON3Result.TMR:
				_farPostfix = "tmrMem.dat";
				break;
			case LEON3Result.ORIG:
				_farPostfix = "orig.dat";
				break;
			default:
				break;				
		}
		
		if (_runfiles != null) {
			for (String dir : _runfiles) {
				_runFile(dir);
			}
		}
		
		if (_infiles != null) {
			boolean first = true;
			for (String file : _infiles) {
				ArrayList<ArrayList<String>> currList = new ArrayList<ArrayList<String>>();
				try {
					_parseFile(currList, file, first);
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(-1);
				}
				first = false;
				_allData.add(currList);
			}
			_averageResults();
			_printResults();
			_printLatexTables();
		}
		
	}
	
    public static void main(String args []) {
    	_parseArgs(args);
    	GatherLEON3Results gather = new GatherLEON3Results(_type);
    }

    protected void _runFile(String dir) {
    	LEON3ReliabilityAnalyzer analyzer = new LEON3ReliabilityAnalyzer(dir+"/"+_xdlFile, dir+"/"+_vhdlFile, dir+"/"+_farPrefix+_farPostfix, _type);
        
    	PrintWriter pw = LEON3ReliabilityAnalyzer.openForWriting(dir+"/"+_outFile);
        analyzer.writeSummary(pw);
        analyzer.writeDetections(pw);
        analyzer.writeVerticalPercentageDetections(pw);
        analyzer.writeHorizontalPercentageDetections(pw);
        analyzer.writeTraditionalArea(pw);
        analyzer.writeArea(pw);
        analyzer.writeFailureClassificationSummary(pw);
        analyzer.writeFailureClassification(pw);
        analyzer.writeXRTCOutput(pw);
        analyzer.writeNathanReportToFile(dir+"/"+_netoutFile, dir+"/"+_instoutFile);
        pw.close();
    	
    }
    
    protected void _parseFile(ArrayList<ArrayList<String>> currList, String filename, boolean first) throws Exception {
    	
    	String colon = ":";
    	String obracket = "\\(";
    	String cbracket = "\\)";
    	String percent = obracket+"(\\d*\\.\\d+)"+cbracket;
    	String ace = "TOT ACE FAULTS";
    	String latency = "LATENCY";
    	String err = "ERR";
    	String avglatency = "DETECTION LATENCY AVG";
    	String detectnorepair = "---- DETECTED NOT REPAIRED";
    	
		BufferedReader resultfile = LEON3ReliabilityAnalyzer.openForReading(filename);
		String line = "";
		
		while ((line = resultfile.readLine().trim()) != null) {
			String findFirst = ace+colon+"\\s+(\\d+)";
			String findOne = "(\\w+)"+colon+"\\s+(\\d+)\\s+"+percent;
			String findTwo = "(\\w+)"+colon+"\\s+(\\d+)\\s+(\\w+)"+colon+"\\s+(\\d+)\\s+"+percent+"\\s+"+percent+"\\s+"+latency+colon+"\\s+((-)?\\d+)";
			String findDetect = "(\\w+)"+colon+"\\s+(\\d+)\\s+"+percent+"\\s+"+detectnorepair+colon+"\\s+(\\d+)";
			String findDetectL = avglatency+colon+"\\s+((-)?\\d+)";
			String findErr = err+colon+"\\s+(\\d+)";
			String findSkip = "("+_aclss+"|"+_clss+"|"+_equals+"|"+_minus+"|"+_full+"|"+_cat+")";
			String findEnd = "^\\d+";
			
			Matcher firstMatch = Pattern.compile(findFirst).matcher(line);
			Matcher oneMatch = Pattern.compile(findOne).matcher(line);
			Matcher twoMatch = Pattern.compile(findTwo).matcher(line);
			Matcher detectMatch = Pattern.compile(findDetect).matcher(line);
			Matcher detectLMatch = Pattern.compile(findDetectL).matcher(line);
			Matcher errMatch = Pattern.compile(findErr).matcher(line);
			Matcher skipMatch = Pattern.compile(findSkip).matcher(line);
			Matcher endMatch = Pattern.compile(findEnd).matcher(line);
			
			if (firstMatch.lookingAt()) {
				if (first) {
					ArrayList<String> add = new ArrayList<String>();
					add.add(ace+colon);
					_whiteData.add(add);
					System.out.println("FIRST first: "+ace);
				}
				ArrayList<String> add = new ArrayList<String>();
				add.add(firstMatch.group(1));
				currList.add(add);
				System.out.println("FIRST: "+firstMatch.group(1));
				
			} else if (twoMatch.lookingAt()) {
				if (first) {
					ArrayList<String> add = new ArrayList<String>();
					add.add(twoMatch.group(1)+colon);
					add.add(twoMatch.group(3)+colon);
					add.add(latency+colon);
					_whiteData.add(add);
					System.out.println("FIRST two: "+twoMatch.group(1)+"  "+twoMatch.group(3));
				}				
				ArrayList<String> avg = new ArrayList<String>();
				avg.add(twoMatch.group(2));
				avg.add(twoMatch.group(4));
				avg.add(twoMatch.group(5));
				avg.add(twoMatch.group(6));
				avg.add(twoMatch.group(7));
				System.out.println("TWO: "+twoMatch.group(2)+"  "+twoMatch.group(4)+"  "+twoMatch.group(5)+"  "+twoMatch.group(6)+"  "+twoMatch.group(7));
				currList.add(avg);
			} else if (detectMatch.lookingAt()) {
				if (first) {
					ArrayList<String> add = new ArrayList<String>();
					add.add(detectMatch.group(1)+colon);
					add.add(detectnorepair+colon);
					_whiteData.add(add);
					System.out.println("FIRST detect: "+detectMatch.group(1));
				}				
				ArrayList<String> add = new ArrayList<String>();
				add.add(detectMatch.group(2));
				add.add(detectMatch.group(3));
				add.add(detectMatch.group(4));
				System.out.println("DETECT: "+detectMatch.group(2)+"  "+detectMatch.group(3)+"  "+detectMatch.group(4));
				currList.add(add);
			} else if (detectLMatch.lookingAt()) {
				if (first) {
					ArrayList<String> add = new ArrayList<String>();
					add.add(avglatency+colon);
					_whiteData.add(add);
					System.out.println("FIRST detectL: "+avglatency);
				}				
				ArrayList<String> add = new ArrayList<String>();
				add.add(detectLMatch.group(1));
				System.out.println("DETECTL: "+detectLMatch.group(1));
				currList.add(add);
			} else if (errMatch.lookingAt()) {
				if (first) {
					ArrayList<String> add = new ArrayList<String>();
					add.add(err+colon);
					_whiteData.add(add);
					System.out.println("FIRST ERR: "+err);
				}				
				ArrayList<String> add = new ArrayList<String>();
				add.add(errMatch.group(1));
				System.out.println("ERR: "+errMatch.group(1));
				currList.add(add);
			} else if (oneMatch.lookingAt()) {
				if (first) {
					ArrayList<String> add = new ArrayList<String>();
					add.add(oneMatch.group(1)+colon);
					_whiteData.add(add);
					System.out.println("FIRST one: "+oneMatch.group(1));
				}
				ArrayList<String> add = new ArrayList<String>();
				add.add(oneMatch.group(2));
				add.add(oneMatch.group(3));
				currList.add(add);
				System.out.println("ONE: "+oneMatch.group(2)+"  "+oneMatch.group(3));
			} else if (skipMatch.lookingAt()) {
				if (first) {
					ArrayList<String> add = new ArrayList<String>();
					add.add(line);
					_whiteData.add(add);
					System.out.println("FIRST SKIP: "+line);
				}				
			} else if (endMatch.lookingAt()) {
				System.out.println("END");
				break;
			} else if (line.length() > 0) {
				String[] words = line.split("\\s+");
				if (first) {
					ArrayList<String> add = new ArrayList<String>();
					add.add(words[0]);
					_whiteData.add(add);
					System.out.println("FIRST W: "+words[0]);
				}
				
				ArrayList<String> add = new ArrayList<String>();
				currList.add(add);
				for (int i = 1; i < words.length; i++) {
					add.add(words[i]);
					System.out.println("LINE "+i+": "+words[i]);
				}
			}
			
		}
		
		resultfile.close();
    }
    
    protected void _averageResults() {
    	
    	boolean first = true;
    	for (ArrayList<ArrayList<String>> currList : _allData) {
    		for (int i = 0; i < currList.size(); i++) {
    			ArrayList<String> avg = null;
        		if (first) {
        			avg = new ArrayList<String>();
        			_avgData.add(avg);
        		} else {
        			avg = _avgData.get(i);
        		}
    			ArrayList<String> list = currList.get(i);
    			for (int j = 0; j < list.size(); j++) {
    				String value = list.get(j);
    				try {
    					int num = Integer.parseInt(value);
    					if (first) {
    						avg.add(Double.toString(num));
    					} else {
    						double num2 = Double.parseDouble(avg.get(j));
    						num2 += num;
    						avg.set(j, Double.toString(num2));
    					}
    				} catch (Exception e) {
    					try {
    						double dnum = Double.parseDouble(value);
    						if (first) {
    							avg.add(Double.toString(dnum));    							
    						} else {
    							double dnum2 = Double.parseDouble(avg.get(j));
    							dnum2 += dnum;
    							avg.set(j, Double.toString(dnum2));
    						}
    						
    					} catch (Exception e2) {
    						e2.printStackTrace();
    						System.exit(-2);
    					}
    				}
    			}
    		}
    		first = false;
    	}
    }
    
    protected void _printResults() {
    	PrintWriter pw = LEON3ReliabilityAnalyzer.openForWriting(_outFile);
    	
    	int whitecnt = 0;
    	int size = 8;
    	for (ArrayList<String> avg : _avgData) {
    		ArrayList<String> white = _whiteData.get(whitecnt++);
    		String start = white.get(0);
			if (start.startsWith(_clss) || start.startsWith(_aclss) || start.startsWith(_cat) || start.startsWith(_full)) {
				pw.append("\n");
			}
			if (start.startsWith(_aclss)) {
				size = 12;
			} else if (start.startsWith(_clss) || start.startsWith(_cat) || start.startsWith(_full)) {
				size = 8;
			}
    		while (start.startsWith(_clss) || start.startsWith(_aclss) || start.startsWith(_full) || start.startsWith(_cat) || 
    				start.startsWith(_equals) || start.startsWith(_minus)) {
    			pw.append(start+"\n");
    			white = _whiteData.get(whitecnt++);
    			start = white.get(0);
    		}
    		if (white.size() == 1) {
    			pw.append(_getFirstWhiteString(start));
    			for (String val : avg) {
    				pw.append(_getAvgString(val, size));
    			}
    			pw.append("\n");
    		} else if (white.size() == 2) {
    			pw.append(start+"\t");
    			pw.append(_getAvgString(avg.get(0), size)+"\t"+_getAvgString(avg.get(1), size)+"\t");
    			pw.append(white.get(1)+" ");
    			pw.append(_getAvgString(avg.get(2), size)+"\n");
    		} else {
    			pw.append("\t"+start+"\t"+_getAvgString(avg.get(0), size)+"\t"+white.get(1)+"\t"+_getAvgString(avg.get(1), size));
    			pw.append("\t"+_getAvgString(avg.get(2), size)+"\t"+white.get(2)+"\t"+_getAvgString(avg.get(3), size)+"\n");
    		}
    	}
    	pw.close();
    }
    
    protected void _printLatexTables() {
    	PrintWriter pw = LEON3ReliabilityAnalyzer.openForWriting("tables.tex");

    	int whitecnt = 0;
    	for (ArrayList<String> avg : _avgData) {
    		ArrayList<String> white = _whiteData.get(whitecnt++);
    		String start = white.get(0);
    		while (start.startsWith(_clss) || start.startsWith(_aclss) || start.startsWith(_full) || start.startsWith(_cat) || 
    				start.startsWith(_equals) || start.startsWith(_minus)) {
    			white = _whiteData.get(whitecnt++);
    			start = white.get(0);
    			pw.append("\n");
    		}
    		if (white.size() == 1 && avg.size() > 6) {
    			pw.append("{\\scriptsize "+start+"}");
    			int cnt = 1;
    			for (String val : avg) {
    				if (cnt++ == 2) {
    					cnt = 0;
    					pw.append("\n\t");
    				}
    				pw.append(" & {\\scriptsize "+val+"}");
    			}
    			pw.append("\n");
    		}
    	}
    	
    	pw.close();
    }
    
    protected String _getFirstWhiteString(String first) {
    	first = String.format("%1$-15.14s", first);
    	
    	return first;
    }
    
    protected String _getAvgString(String val, int size) {
    	int div = _allData.size();
		
    	try {
			int num = Integer.parseInt(val);
			num = num / div;
			val = String.format("%1$"+size+"s", num);
		} catch (Exception e) {
			try {
				double dnum = Double.parseDouble(val);
				dnum = dnum / div;
				val = String.format("%1$"+size+".2f", dnum);
			} catch (Exception e2) {
				e2.printStackTrace();
				System.exit(-3);
			}
		}

		return val;
    }
    
   	/**
	 * Parses the command-line arguments
	 * 
	 * @param args
	 */	
	protected static void _parseArgs(String[] args) {
		if (args.length < 1) {
			System.out.println(_usage());
			System.exit(-1);
		}
			
		_infiles = new ArrayList<String>();
		_runfiles = new ArrayList<String>();
		
		for (int i = 0; i < args.length; i++) {
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
			} else if (args[i].equals("-in")) {
				i++;
				_infiles.add(args[i]);
			} else if (args[i].equals("-run")) {
				i++;
				_runfiles.add(args[i]);
			} else {
				System.out.println(_usage());
				System.exit(-1);				
			}
		}
	}

	protected static String _usage() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("ReliabilityAnalyzer Usage: java edu.byu.ece.gremlinTools.ReliabilityTools.ReliabilityAnalyzer [options]\n");
		sb.append("options:\n");
		sb.append("\t-h | -help \t Print this message\n");
		sb.append("\t(-in <filename>)+ \t Add an input file to parse.\n");
		sb.append("\t(-run <dir>)+ \t Run the result tool for output files in the given directory.\n");
		sb.append("\t-o <filename> \t Specify the output file name.\n");
		sb.append("\t-dwc \t\t The design is a DWC design.\n");
		sb.append("\t-orig \t\t The design is the original design.\n");
		sb.append("\t-sw \t\t The design is the SW techniques design (Default).\n");
		sb.append("\t-tmr \t\t The design is a TMR design.\n");
		sb.append("\n");
		
		return sb.toString();
	}	  

	protected String _aclss = "AVFCLASS";
	protected String _clss = "CLASS";
	protected String _full = "FULL";
	protected String _cat = "CAT";
	protected String _equals = "=";
	protected String _minus = "-";
	
	protected ArrayList<ArrayList<ArrayList<String>>> _allData;
	protected ArrayList<ArrayList<String>> _avgData;
	protected ArrayList<ArrayList<String>> _whiteData;
	protected static int _type = LEON3Result.SW;    
    protected static String _outFile = "resultsavg.dat";
    protected static ArrayList<String> _infiles;
    protected static ArrayList<String> _runfiles;
    protected static String _farPrefix = "outfile_";
    protected static String _vhdlFile = "dut_basic.vhm";
    protected static String _xdlFile = "dut_basic_routed.xdl";
    protected static String _farPostfix;
	protected static String _instoutFile = "instresults.dat";
	protected static String _netoutFile = "netresults.dat";
}

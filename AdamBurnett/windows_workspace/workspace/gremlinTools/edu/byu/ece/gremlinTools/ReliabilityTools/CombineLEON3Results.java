package edu.byu.ece.gremlinTools.ReliabilityTools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;


public class CombineLEON3Results {
	

	public CombineLEON3Results(int type) {
		
		if (_infiles != null) {
			PrintWriter pw = LEON3ReliabilityAnalyzer.openForWriting(_outFile);
			_allResults = new TreeSet<LEON3Result>();
			_results = new ArrayList<LEON3Result>();
			_reconfigs = new ArrayList<LEON3Result>();
			
			for (String file : _infiles) {
				try {
					_parseXRTCfile(file);
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(-1);
				}
			}
			
			for (LEON3Result result : _allResults) {
				pw.append(result.getRawFAR()+"\t"+result.getRawResult()+"\n");
			}
			
			pw.close();
		}
		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
    	_parseArgs(args);
    	CombineLEON3Results results = new CombineLEON3Results(_type);
	}

	protected void _parseXRTCfile(String filename) {

    	String line = "";
    	try {
			BufferedReader br = new BufferedReader(new FileReader(filename));
			while ((line = br.readLine()) !=  null) {
				if (line.length() > 0) {
					LEON3Result result = new LEON3Result(line, _type);
					if (result.getDetectType() == LEON3Result.RECONFIG) {
						if (!_haveResult(_reconfigs, result)) {
							_reconfigs.add(result);
							_allResults.add(result);
						}
					}
//					else if (!_haveResult(_results, result)) {
					else {
						if (_combo) {
							if (!_haveACEResultCombo(_results, result)) {
								_results.add(result);
								_allResults.add(result);								
							}
						} else if (!_haveACEResult(_results, result)) {
							_results.add(result);
							_allResults.add(result);
						}
					}
				}				
			}
			br.close();

    	} catch (Exception e) {
			e.printStackTrace();
    	}
    	
    }
	
	protected boolean _haveResult(ArrayList<LEON3Result> list, LEON3Result result) {
		boolean haveResult = false;
		
		for (LEON3Result listElement : list) {
			if (result.getFAR() == listElement.getFAR() && result.getFrameBit() == listElement.getFrameBit()) {
				haveResult = true;
				break;
			}
		}
		
		return haveResult;
	}

	protected boolean _haveACEResult(ArrayList<LEON3Result> list, LEON3Result result) {
		boolean haveResult = false;
		LEON3Result toRemove = null;
		
		for (LEON3Result listElement : list) {
			if (result.getFAR() == listElement.getFAR() && result.getFrameBit() == listElement.getFrameBit()) {
				if (result.getACEType() > listElement.getACEType()) {
					toRemove = listElement;				
				} else
					haveResult = true;
				break;
			}
		}
		if (toRemove != null)
			list.remove(toRemove);
		
		return haveResult;
	}

	protected boolean _haveACEResultCombo(ArrayList<LEON3Result> list, LEON3Result result) {
		boolean haveResult = false;
		LEON3Result toRemove = null;
		
		for (LEON3Result listElement : list) {
			if (result.getFAR() == listElement.getFAR() && result.getFrameBit() == listElement.getFrameBit()) {
				if (result.getACEType() < listElement.getACEType()) {
					toRemove = listElement;				
				} else
					haveResult = true;
				break;
			}
		}
		if (toRemove != null)
			list.remove(toRemove);
		
		return haveResult;
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
			
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-h") || args[i].equals("-help")) {
				System.out.println(_usage());
				System.exit(0);
			} else if (args[i].equals("-combo")) {
				_combo = true;
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
			} else {
				System.out.println(_usage());
				System.exit(-1);				
			}
		}
	}

	protected static String _usage() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("CombineLEON3Results Usage: java edu.byu.ece.gremlinTools.ReliabilityTools.CombineLEON3Results [options]\n");
		sb.append("options:\n");
		sb.append("\t-h | -help \t Print this message\n");
		sb.append("\t-combo \t\t The input files are to be combined for a SW combo.\n");
		sb.append("\t(-in <filename>)+ \t Add an input file to parse.\n");
		sb.append("\t-o <filename> \t Specify the output file name.\n");
		sb.append("\t-dwc \t\t The design is a DWC design.\n");
		sb.append("\t-orig \t\t The design is the original design.\n");
		sb.append("\t-sw \t\t The design is the SW techniques design (Default).\n");
		sb.append("\t-tmr \t\t The design is a TMR design.\n");
		sb.append("\n");
		
		return sb.toString();
	}	  
	
    protected SortedSet<LEON3Result> _allResults;
    protected ArrayList<LEON3Result> _results;
    protected ArrayList<LEON3Result> _reconfigs;
	protected static int _type = LEON3Result.SW;    
    protected static ArrayList<String> _infiles;
    protected static String _outFile = "outfile_all.dat";
    protected static boolean _combo = false;
	
}

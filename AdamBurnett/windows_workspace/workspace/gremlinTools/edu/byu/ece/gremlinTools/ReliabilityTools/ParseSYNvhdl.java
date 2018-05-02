package edu.byu.ece.gremlinTools.ReliabilityTools;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.byu.ece.gremlinTools.Virtex4Bits.Pair;


public class ParseSYNvhdl {
	
	public ParseSYNvhdl(String vhdFile, int type) {
		_vhdFile = vhdFile;
		_type = type;
		
		_typeComponentMap = new HashMap<String, VHDLComponent>();
		_signalComponentMap = new HashMap<String, VHDLComponent>();
		_instListHash = new HashMap<String, ArrayList<String>>();
		_signalAliasHash = new HashMap<String, ArrayList<String>>();
		_entitySignalsHash = new HashMap<Pair<String,String>, String>();
		_instEntityHash = new HashMap<Pair<String,String>, String>();
		_pathHash = new HashMap<String, String>();
		_typeMap = new HashMap<String, String>();
		_entityList = new ArrayList<String>();
		
		VHDLComponent top = new VHDLComponent(TOP);
		VHDLComponent clk = new VHDLComponent(CLK);
		VHDLComponent rst = new VHDLComponent(RST);
		VHDLComponent iu3 = new VHDLComponent(IU3);
		VHDLComponent alu = new VHDLComponent(ALU);
		VHDLComponent ctrl = new VHDLComponent(CTRL);
		VHDLComponent cachectrl = new VHDLComponent(CACHECTRL);
		VHDLComponent icachectrl = new VHDLComponent(ICACHECTRL);
		VHDLComponent dcachectrl = new VHDLComponent(DCACHECTRL);
		VHDLComponent cache = new VHDLComponent(CACHE);
		VHDLComponent icache = new VHDLComponent(ICACHE);
		VHDLComponent dcache = new VHDLComponent(DCACHE);
		VHDLComponent itag = new VHDLComponent(ITAG);
		VHDLComponent dtag = new VHDLComponent(DTAG);
		VHDLComponent mmcachectrl = new VHDLComponent(MMCACHECTRL);
		VHDLComponent irq = new VHDLComponent(IRQ);
		VHDLComponent cmp = new VHDLComponent(CMP);
		VHDLComponent muldiv = new VHDLComponent(MULDIV);
		VHDLComponent regfile = new VHDLComponent(REGFILE);
		VHDLComponent mm = new VHDLComponent(MM);
		VHDLComponent chk = new VHDLComponent(CHK);
		VHDLComponent iob = new VHDLComponent(IOB);
		VHDLComponent dsp = new VHDLComponent(DSP);
		VHDLComponent bramattr = new VHDLComponent(BRAM_ATTR);
		VHDLComponent nonet = new VHDLComponent(NONET);
		VHDLComponent unknown = new VHDLComponent(UNKNOWN);
		VHDLComponent global = new VHDLComponent(GLOBAL);
		
		_typeComponentMap.put(TOP, top);
		_typeComponentMap.put(CLK, clk);
		_typeComponentMap.put(RST, rst);
		_typeComponentMap.put(IU3, iu3);
		_typeComponentMap.put(ALU, alu);
		_typeComponentMap.put(CTRL, ctrl);
		_typeComponentMap.put(CACHECTRL, cachectrl);
		_typeComponentMap.put(ICACHECTRL, icachectrl);
		_typeComponentMap.put(DCACHECTRL, dcachectrl);
		_typeComponentMap.put(CACHE, cache);
		_typeComponentMap.put(ICACHE, icache);
		_typeComponentMap.put(DCACHE, dcache);
		_typeComponentMap.put(ITAG, itag);
		_typeComponentMap.put(DTAG, dtag);
		_typeComponentMap.put(MMCACHECTRL, mmcachectrl);
		_typeComponentMap.put(IRQ, irq);
		_typeComponentMap.put(CMP, cmp);
		_typeComponentMap.put(MULDIV, muldiv);
		_typeComponentMap.put(REGFILE, regfile);
		_typeComponentMap.put(MM, mm);
		_typeComponentMap.put(CHK, chk);
		_typeComponentMap.put(IOB, iob);
		_typeComponentMap.put(DSP, dsp);
		_typeComponentMap.put(BRAM_ATTR, bramattr);
		_typeComponentMap.put(NONET, nonet);
		_typeComponentMap.put(UNKNOWN, unknown);
		_typeComponentMap.put(GLOBAL, global);
		
		if (type == LEON3Result.ORIG)
			_initORIG();
		else if (type == LEON3Result.DWC)
			_initDWC();
		else if (type == LEON3Result.TMR)
			_initTMR();
		else if (type == LEON3Result.SW)
			_initSW();
		
		try {
			_parseFile();
		} catch (Exception e) {
			System.err.println("ERROR - "+e.getMessage());
			e.printStackTrace();
			System.exit(-1);
		}
		
		_createPaths("", _topEntity);
		_createNetList();
		try {
			_resetSignalTypes();
		} catch (Exception e) {
			System.err.println("ERROR - "+e.getMessage());
			e.printStackTrace();
			System.exit(-1);			
		}

	}
		
	public HashMap<String, VHDLComponent> getSignalComponentMap() {
		return _signalComponentMap;
	}
	
	public HashMap<String, VHDLComponent> getTypeComponentMap() {
		return _typeComponentMap;
	}
	
	public void setSignalComponentMap(HashMap<String, VHDLComponent> newMap) {
		_signalComponentMap = newMap;
	}
	
	protected void _initSW() {
		
		String[] topEntity = {"dut_basic", "leon3mp", "leon3swifttrapcheckmemPar"};
		String[] iu3Entity = {"iu3swifttrapcheckPar", "proc3swifttrapcheckPar"};
		String[] ctrlEntity = {"checkpointctrlMem", "counter_8", "checkpointmemory"};
		String[] icacheCtrlEntity = {"icachemine"};
		String[] dcacheCtrlEntity = {"dcachemine"};
		String[] cacheCtrlEntity = {"cachemine"};
		String[] cacheEntity = {"cachememPar"};
		String[] icacheEntity = {"cacheRAMPar_work_dut_basic_no_tmr_0layer0"};
		String[] dcacheEntity = {"cacheRAMPar_work_dut_basic_no_tmr_0layer0_1"};
		String[] itagEntity = {"cacheTagRAM_5_30", "cacheTagRAMCD_5_30"};
		String[] dtagEntity = {"cacheTagRAM_5_30_1", "cacheTagRAMCD_5_30_1"};
		String[] mmcacheCtrlEntity = {"mainmemcheck"};
		String[] cmpEntity = {"comparePar"};
		String[] irqEntity = {"irqctrl", "counter_12", "counter_2", "counter_10"};
		String[] muldivEntity = {"div32", "mul32"};
		String[] regfileEntity = {"regfilemine", "regfileswiftCD"};
		String[] mmEntity = {"mainmemFill_8"};
		String[] chkEntity = {"regfilecheck", "mainmemFill_8_1"};
		
		_typeComponentMap.get(TOP).addEntityFilterList(Arrays.asList(topEntity));
		_typeComponentMap.get(IU3).addEntityFilterList(Arrays.asList(iu3Entity));
		_typeComponentMap.get(CTRL).addEntityFilterList(Arrays.asList(ctrlEntity));
		_typeComponentMap.get(ICACHECTRL).addEntityFilterList(Arrays.asList(icacheCtrlEntity));
		_typeComponentMap.get(DCACHECTRL).addEntityFilterList(Arrays.asList(dcacheCtrlEntity));
		_typeComponentMap.get(CACHECTRL).addEntityFilterList(Arrays.asList(cacheCtrlEntity));
		_typeComponentMap.get(CACHE).addEntityFilterList(Arrays.asList(cacheEntity));
		_typeComponentMap.get(ICACHE).addEntityFilterList(Arrays.asList(icacheEntity));
		_typeComponentMap.get(DCACHE).addEntityFilterList(Arrays.asList(dcacheEntity));
		_typeComponentMap.get(ITAG).addEntityFilterList(Arrays.asList(itagEntity));
		_typeComponentMap.get(DTAG).addEntityFilterList(Arrays.asList(dtagEntity));
		_typeComponentMap.get(MMCACHECTRL).addEntityFilterList(Arrays.asList(mmcacheCtrlEntity));
		_typeComponentMap.get(CMP).addEntityFilterList(Arrays.asList(cmpEntity));
		_typeComponentMap.get(IRQ).addEntityFilterList(Arrays.asList(irqEntity));
		_typeComponentMap.get(MULDIV).addEntityFilterList(Arrays.asList(muldivEntity));
		_typeComponentMap.get(REGFILE).addEntityFilterList(Arrays.asList(regfileEntity));
		_typeComponentMap.get(MM).addEntityFilterList(Arrays.asList(mmEntity));
		_typeComponentMap.get(CHK).addEntityFilterList(Arrays.asList(chkEntity));

		String[] clkSignal = {"clk", "bufg"};
		String[] topSignal = {"dout", "sngl_bus", "tmv_bus", "xil_", "_led", ".u0.topi.data"};
		String[] aluSignal = {"result", "alu", "logic", "add", "shift", "op1", "op2"};
		String[] iu3Signal = {"/p0/iu0/", "p0.iu0.", ".u0."};
		String[] icacheCtrlSignal = {"/p0/c0/icache0/", "/cmem0/it"};
		String[] dcacheCtrlSignal = {"/p0/c0/dcache0/", "/cmem0/dt"};
		String[] muldivSignal = {"/p0/mgen", "y_0"};
		String[] ctrlSignal = {"/u0/regf/ctrl/"};
		String[] globalSignal = {"global_logic", "dummy"};
		String[] irqSignal = {"/u0/irqctrl/"};
//		String[] mmSignal = {"/regf/mmf/ram"};
		String[] dtagSignal = {"/dme.dtags0.dt0.0.dtags0/"};
		_typeComponentMap.get(CLK).addSignalFilterList(Arrays.asList(clkSignal));
		_typeComponentMap.get(TOP).addSignalFilterList(Arrays.asList(topSignal));
		_typeComponentMap.get(ALU).addSignalFilterList(Arrays.asList(aluSignal));
		_typeComponentMap.get(IU3).addSignalFilterList(Arrays.asList(iu3Signal));
		_typeComponentMap.get(ICACHECTRL).addSignalFilterList(Arrays.asList(icacheCtrlSignal));
		_typeComponentMap.get(DCACHECTRL).addSignalFilterList(Arrays.asList(dcacheCtrlSignal));
		_typeComponentMap.get(MULDIV).addSignalFilterList(Arrays.asList(muldivSignal));
		_typeComponentMap.get(CTRL).addSignalFilterList(Arrays.asList(ctrlSignal));
		_typeComponentMap.get(IRQ).addSignalFilterList(Arrays.asList(irqSignal));
//		_typeComponentMap.get(MM).addSignalFilterList(Arrays.asList(mmSignal));
		_typeComponentMap.get(GLOBAL).addSignalFilterList(Arrays.asList(globalSignal));
		_typeComponentMap.get(DTAG).addSignalFilterList(Arrays.asList(dtagSignal));
		
	}
	
	protected void _initDWC() {
		
		String[] topEntity = {"dut_basic", "leon3mp", "leon3check", "leon3dup"};
		String[] iu3Entity = {"iu3mineNovel", "proc3mineNovel"};
		String[] ctrlEntity = {"checkpointctrldup", "counter_8", "checkpointdup"};
		String[] icacheCtrlEntity = {"icachemine", "/cmem0/it"};
		String[] dcacheCtrlEntity = {"dcachemine", "/cmem0/dt"};
		String[] cacheCtrlEntity = {"cachemine"};
		String[] cacheEntity = {"cachemem"};
		String[] icacheEntity = {"cacheRAMPar_work_dut_basic_no_tmr_0layer0_2", "cacheRAMPar_work_dut_basic_no_tmr_0layer0"};
		String[] dcacheEntity = {"cacheRAMPar_work_dut_basic_no_tmr_0layer0_3", "cacheRAMPar_work_dut_basic_no_tmr_0layer0_1"};
		String[] itagEntity = {"cacheTagRAM_5_30_2", "cacheTagRAM_5_30"};
		String[] dtagEntity = {"cacheTagRAM_5_30_3", "cacheTagRAM_5_30_1"};
		String[] mmcacheCtrlEntity = {"mainmemcheck"};
		String[] cmpEntity = {"compareUnitDup"};
		String[] irqEntity = {"irqmineDup", "counter_12", "counter_2", "counter_10", "counter_9"};
		String[] muldivEntity = {"div32", "mul32"};
		String[] regfileEntity = {"regfilemine"};
		String[] mmEntity = {"mainmemFill_8_1", "mainmemFill_8"};
		String[] chkEntity = {"regfilecheck_32_9", "mainmemFill_8_2"};
		
		_typeComponentMap.get(TOP).addEntityFilterList(Arrays.asList(topEntity));
		_typeComponentMap.get(IU3).addEntityFilterList(Arrays.asList(iu3Entity));
		_typeComponentMap.get(CTRL).addEntityFilterList(Arrays.asList(ctrlEntity));
		_typeComponentMap.get(ICACHECTRL).addEntityFilterList(Arrays.asList(icacheCtrlEntity));
		_typeComponentMap.get(DCACHECTRL).addEntityFilterList(Arrays.asList(dcacheCtrlEntity));
		_typeComponentMap.get(CACHECTRL).addEntityFilterList(Arrays.asList(cacheCtrlEntity));
		_typeComponentMap.get(CACHE).addEntityFilterList(Arrays.asList(cacheEntity));
		_typeComponentMap.get(ICACHE).addEntityFilterList(Arrays.asList(icacheEntity));
		_typeComponentMap.get(DCACHE).addEntityFilterList(Arrays.asList(dcacheEntity));
		_typeComponentMap.get(ITAG).addEntityFilterList(Arrays.asList(itagEntity));
		_typeComponentMap.get(DTAG).addEntityFilterList(Arrays.asList(dtagEntity));
		_typeComponentMap.get(MMCACHECTRL).addEntityFilterList(Arrays.asList(mmcacheCtrlEntity));
		_typeComponentMap.get(CMP).addEntityFilterList(Arrays.asList(cmpEntity));
		_typeComponentMap.get(IRQ).addEntityFilterList(Arrays.asList(irqEntity));
		_typeComponentMap.get(MULDIV).addEntityFilterList(Arrays.asList(muldivEntity));
		_typeComponentMap.get(REGFILE).addEntityFilterList(Arrays.asList(regfileEntity));
		_typeComponentMap.get(MM).addEntityFilterList(Arrays.asList(mmEntity));
		_typeComponentMap.get(CHK).addEntityFilterList(Arrays.asList(chkEntity));

		String[] clkSignal = {"clk", "bufg"};
		String[] topSignal = {"dout", "sngl_bus", "tmv_bus", "xil_", "_led"};
		String[] aluSignal = {"result", "alu", "logic", "add", "shift", "op1", "op2"};
		String[] iu3Signal = {"/p0/iu0/", "p0.iu0.", "u0.u0.", ".u0."};
		String[] icacheCtrlSignal = {"/p0/c0/icache0/", "/cmem0/it"};
		String[] dcacheCtrlSignal = {"/p0/c0/dcache0/", "/cmem0/dt"};
		String[] muldivSignal = {"/p0/mgen", "y_0", "y_1"}; //, "/p0/result"};
		String[] ctrlSignal = {"/u0/regf/ctrl"};
		String[] irqSignal = {"/u0/irqctrl"};
		String[] globalSignal = {"global_logic", "dummy"};
//		String[] mmSignal = {"/regf/mmf/ram"};
		String[] dtagSignal = {"/dme.dtags0.dt0.0.dtags0/"};
		_typeComponentMap.get(CLK).addSignalFilterList(Arrays.asList(clkSignal));
		_typeComponentMap.get(TOP).addSignalFilterList(Arrays.asList(topSignal));
		_typeComponentMap.get(ALU).addSignalFilterList(Arrays.asList(aluSignal));
		_typeComponentMap.get(IU3).addSignalFilterList(Arrays.asList(iu3Signal));
		_typeComponentMap.get(ICACHECTRL).addSignalFilterList(Arrays.asList(icacheCtrlSignal));
		_typeComponentMap.get(DCACHECTRL).addSignalFilterList(Arrays.asList(dcacheCtrlSignal));
		_typeComponentMap.get(MULDIV).addSignalFilterList(Arrays.asList(muldivSignal));
		_typeComponentMap.get(CTRL).addSignalFilterList(Arrays.asList(ctrlSignal));
		_typeComponentMap.get(IRQ).addSignalFilterList(Arrays.asList(irqSignal));
//		_typeComponentMap.get(MM).addSignalFilterList(Arrays.asList(mmSignal));
		_typeComponentMap.get(DTAG).addSignalFilterList(Arrays.asList(dtagSignal));
		_typeComponentMap.get(GLOBAL).addSignalFilterList(Arrays.asList(globalSignal));
	}

	protected void _initTMR() {
		
		String[] topEntity = {"dut_basic", "leon3mp", "leon3checkMem", "leon3tmr"};
		String[] iu3Entity = {"iu3mineNovel", "proc3mineNovel"};
		String[] ctrlEntity = {"checkpointctrltmr", "counter_8", "checkpointtmr"};
		String[] icacheCtrlEntity = {"icachemine"};
		String[] dcacheCtrlEntity = {"dcachemine"};
		String[] cacheCtrlEntity = {"cachemine"};
		String[] cacheEntity = {"cachemem"};
		String[] icacheEntity = {"cacheRAMPar_work_dut_basic_tmr_0layer0_4", "cacheRAMPar_work_dut_basic_tmr_0layer0_2", "cacheRAMPar_work_dut_basic_tmr_0layer0"};
		String[] dcacheEntity = {"cacheRAMPar_work_dut_basic_tmr_0layer0_5", "cacheRAMPar_work_dut_basic_tmr_0layer0_3", "cacheRAMPar_work_dut_basic_tmr_0layer0_1"};
		String[] itagEntity = {"cacheTagRAM_5_30_4", "cacheTagRAM_5_30_2", "cacheTagRAM_5_30"};
		String[] dtagEntity = {"cacheTagRAM_5_30_5", "cacheTagRAM_5_30_3", "cacheTagRAM_5_30_1"};
		String[] mmcacheCtrlEntity = {"mainmemcheck"};
		String[] cmpEntity = {"compareUnitTMR"};
		String[] irqEntity = {"irqmine", "counter_12", "counter_13", "counter_2", "counter_10", "counter_9"};
		String[] muldivEntity = {"div32", "mul32"};
		String[] regfileEntity = {"regfilemine"};
		String[] mmEntity = {"mainmemFill_8_2", "mainmemFill_8_1", "mainmemFill_8"};
		String[] chkEntity = {"regfilecheck_32_9", "mainmemFill_8_3"};
		
		_typeComponentMap.get(TOP).addEntityFilterList(Arrays.asList(topEntity));
		_typeComponentMap.get(IU3).addEntityFilterList(Arrays.asList(iu3Entity));
		_typeComponentMap.get(CTRL).addEntityFilterList(Arrays.asList(ctrlEntity));
		_typeComponentMap.get(ICACHECTRL).addEntityFilterList(Arrays.asList(icacheCtrlEntity));
		_typeComponentMap.get(DCACHECTRL).addEntityFilterList(Arrays.asList(dcacheCtrlEntity));
		_typeComponentMap.get(CACHECTRL).addEntityFilterList(Arrays.asList(cacheCtrlEntity));
		_typeComponentMap.get(CACHE).addEntityFilterList(Arrays.asList(cacheEntity));
		_typeComponentMap.get(ICACHE).addEntityFilterList(Arrays.asList(icacheEntity));
		_typeComponentMap.get(DCACHE).addEntityFilterList(Arrays.asList(dcacheEntity));
		_typeComponentMap.get(ITAG).addEntityFilterList(Arrays.asList(itagEntity));
		_typeComponentMap.get(DTAG).addEntityFilterList(Arrays.asList(dtagEntity));
		_typeComponentMap.get(MMCACHECTRL).addEntityFilterList(Arrays.asList(mmcacheCtrlEntity));
		_typeComponentMap.get(CMP).addEntityFilterList(Arrays.asList(cmpEntity));
		_typeComponentMap.get(IRQ).addEntityFilterList(Arrays.asList(irqEntity));
		_typeComponentMap.get(MULDIV).addEntityFilterList(Arrays.asList(muldivEntity));
		_typeComponentMap.get(REGFILE).addEntityFilterList(Arrays.asList(regfileEntity));
		_typeComponentMap.get(MM).addEntityFilterList(Arrays.asList(mmEntity));
		_typeComponentMap.get(CHK).addEntityFilterList(Arrays.asList(chkEntity));

		String[] clkSignal = {"clk", "bufg"};
		String[] topSignal = {"dout", "sngl_bus", "tmv_bus", "xil_", "_led"};
		String[] aluSignal = {"result", "alu", "logic", "add", "shift", "op1", "op2"};
		String[] iu3Signal = {"/p0/iu0/", "p0.iu0.", ".u0."};
		String[] icacheCtrlSignal = {"/p0/c0/icache0/", "/cmem0/it"};
		String[] dcacheCtrlSignal = {"/p0/c0/dcache0/", "/cmem0/dt"};
		String[] muldivSignal = {"/p0/mgen", "y_0", "y_1"};
		String[] ctrlSignal = {"/u0/regf/ctrl"};
		String[] irqSignal = {"/u0/irqctrl"};
		String[] globalSignal = {"global_logic", "dummy"};
//		String[] mmSignal = {"/regf/mmf/ram"};
		String[] dtagSignal = {"/dme.dtags0.dt0.0.dtags0/"};
		_typeComponentMap.get(CLK).addSignalFilterList(Arrays.asList(clkSignal));
		_typeComponentMap.get(TOP).addSignalFilterList(Arrays.asList(topSignal));
		_typeComponentMap.get(ALU).addSignalFilterList(Arrays.asList(aluSignal));
		_typeComponentMap.get(IU3).addSignalFilterList(Arrays.asList(iu3Signal));
		_typeComponentMap.get(ICACHECTRL).addSignalFilterList(Arrays.asList(icacheCtrlSignal));
		_typeComponentMap.get(DCACHECTRL).addSignalFilterList(Arrays.asList(dcacheCtrlSignal));
		_typeComponentMap.get(MULDIV).addSignalFilterList(Arrays.asList(muldivSignal));
		_typeComponentMap.get(CTRL).addSignalFilterList(Arrays.asList(ctrlSignal));
		_typeComponentMap.get(IRQ).addSignalFilterList(Arrays.asList(irqSignal));
//		_typeComponentMap.get(MM).addSignalFilterList(Arrays.asList(mmSignal));
		_typeComponentMap.get(DTAG).addSignalFilterList(Arrays.asList(dtagSignal));
		_typeComponentMap.get(GLOBAL).addSignalFilterList(Arrays.asList(globalSignal));
	}

	protected void _initORIG() {
		
		String[] topEntity = {"dut_basic", "leon3mp", "leon3mineXRTC"};
		String[] iu3Entity = {"iu3mine", "proc3mineXRTC"};
		String[] ctrlEntity = {"resetMemoryCTRL", "counter_8", "resetMemory"};
		String[] icacheCtrlEntity = {"icachemine"};
		String[] dcacheCtrlEntity = {"dcachemine"};
		String[] cacheCtrlEntity = {"cachemine"};
		String[] cacheEntity = {"cachemem"};
		String[] icacheEntity = {"cacheRAMPar_work_dut_basic_no_tmr_0layer0"};
		String[] dcacheEntity = {"cacheRAMPar_work_dut_basic_no_tmr_0layer0_1"};
		String[] itagEntity = {"cacheTagRAM_5_30"};
		String[] dtagEntity = {"cacheTagRAM_5_30_1"};
		String[] mmcacheCtrlEntity = {"mainmemcheck"};
		String[] cmpEntity = {"compareParCDUnit"};
		String[] irqEntity = {"irqmine"};
		String[] muldivEntity = {"div32", "mul32"};
		String[] regfileEntity = {"regfilemine"};
		String[] mmEntity = {"mainmemFill_8"};
		String[] chkEntity = {};
		
		_typeComponentMap.get(TOP).addEntityFilterList(Arrays.asList(topEntity));
		_typeComponentMap.get(IU3).addEntityFilterList(Arrays.asList(iu3Entity));
		_typeComponentMap.get(CTRL).addEntityFilterList(Arrays.asList(ctrlEntity));
		_typeComponentMap.get(ICACHECTRL).addEntityFilterList(Arrays.asList(icacheCtrlEntity));
		_typeComponentMap.get(DCACHECTRL).addEntityFilterList(Arrays.asList(dcacheCtrlEntity));
		_typeComponentMap.get(CACHECTRL).addEntityFilterList(Arrays.asList(cacheCtrlEntity));
		_typeComponentMap.get(CACHE).addEntityFilterList(Arrays.asList(cacheEntity));
		_typeComponentMap.get(ICACHE).addEntityFilterList(Arrays.asList(icacheEntity));
		_typeComponentMap.get(DCACHE).addEntityFilterList(Arrays.asList(dcacheEntity));
		_typeComponentMap.get(ITAG).addEntityFilterList(Arrays.asList(itagEntity));
		_typeComponentMap.get(DTAG).addEntityFilterList(Arrays.asList(dtagEntity));
		_typeComponentMap.get(MMCACHECTRL).addEntityFilterList(Arrays.asList(mmcacheCtrlEntity));
		_typeComponentMap.get(CMP).addEntityFilterList(Arrays.asList(cmpEntity));
		_typeComponentMap.get(IRQ).addEntityFilterList(Arrays.asList(irqEntity));
		_typeComponentMap.get(MULDIV).addEntityFilterList(Arrays.asList(muldivEntity));
		_typeComponentMap.get(REGFILE).addEntityFilterList(Arrays.asList(regfileEntity));
		_typeComponentMap.get(MM).addEntityFilterList(Arrays.asList(mmEntity));
		_typeComponentMap.get(CHK).addEntityFilterList(Arrays.asList(chkEntity));

		String[] clkSignal = {"clk", "bufg"};
		String[] topSignal = {"dout", "sngl_bus", "tmv_bus", "xil_", "_led"};
		String[] aluSignal = {"result", "alu", "logic", "shift", "add", "op1", "op2"};
		String[] iu3Signal = {"/p0/iu0/", "p0.iu0.", ".u0."};
		String[] icacheCtrlSignal = {"/p0/c0/icache0/", "/cmem0/it"};
		String[] dcacheCtrlSignal = {"/p0/c0/dcache0/", "/cmem0/dt"};
		String[] muldivSignal = {"/p0/mgen", "y_0"};
		String[] ctrlSignal = {"/u0/regf/ctrl/"};
		String[] irqSignal = {"/u0/irqctrl/"};
		String[] globalSignal = {"global_logic", "dummy"};
//		String[] mmSignal = {"/regf/mmf/ram"};
		String[] dtagSignal = {"/dme.dtags0.dt0.0.dtags0/"};
		_typeComponentMap.get(CLK).addSignalFilterList(Arrays.asList(clkSignal));
		_typeComponentMap.get(TOP).addSignalFilterList(Arrays.asList(topSignal));
		_typeComponentMap.get(ALU).addSignalFilterList(Arrays.asList(aluSignal));
		_typeComponentMap.get(IU3).addSignalFilterList(Arrays.asList(iu3Signal));
		_typeComponentMap.get(ICACHECTRL).addSignalFilterList(Arrays.asList(icacheCtrlSignal));
		_typeComponentMap.get(DCACHECTRL).addSignalFilterList(Arrays.asList(dcacheCtrlSignal));
		_typeComponentMap.get(MULDIV).addSignalFilterList(Arrays.asList(muldivSignal));
		_typeComponentMap.get(CTRL).addSignalFilterList(Arrays.asList(ctrlSignal));
		_typeComponentMap.get(IRQ).addSignalFilterList(Arrays.asList(irqSignal));
//		_typeComponentMap.get(MM).addSignalFilterList(Arrays.asList(mmSignal));
		_typeComponentMap.get(DTAG).addSignalFilterList(Arrays.asList(dtagSignal));
		_typeComponentMap.get(GLOBAL).addSignalFilterList(Arrays.asList(globalSignal));
	}
	
	protected void _resetSignalTypes() throws Exception {
		String entity = "entity";
		String generic = "generic map";
		String endGeneric = ")";
		String assign = "<=";
		String portMatch = "=>";
		String colon = ":";
		String comma = ",";	
		String endInst = ");";
		
		BufferedReader vhdfile = LEON3ReliabilityAnalyzer.openForReading(_vhdFile);
		String line = "";
		String currEntity = "";
		String instEntity = "";
		
		boolean inGeneric = false;
		boolean goodInst = false;
		
		while ((line = vhdfile.readLine()) != null) {
			String findEntity = entity+"\\s+(\\S+)";
			String findInstName = "\\\\*(\\S+)\\\\*"+colon+"\\s*(\\S+)";
			String findAssign = "\\s*(\\S+)\\s+"+assign+"\\s+(\\S+)";
			String findPortMatch = "\\s*(\\S+)\\s+"+portMatch+"\\s+(\\S+)";
			String findGeneric = "\\s*"+generic;
						
			Matcher entityMatch = Pattern.compile(findEntity).matcher(line);
			Matcher instNameMatch = Pattern.compile(findInstName).matcher(line);
			Matcher assignMatch = Pattern.compile(findAssign).matcher(line);
			Matcher portMatchMatch = Pattern.compile(findPortMatch).matcher(line);
			Matcher genericMatch = Pattern.compile(findGeneric).matcher(line);
			
			if (genericMatch.lookingAt()) {
				inGeneric = true;
			}
			
			if (line.trim().endsWith(endGeneric)) {
				inGeneric = false;
			}
			
			if (entityMatch.lookingAt()) {
				String name = entityMatch.group(1).toLowerCase();
				name = name.replaceAll("\\\\", "");
				currEntity = name;
				System.out.println("CURR ENTITY: "+currEntity);
			}
			
			if (instNameMatch.lookingAt()) {
				String ent = instNameMatch.group(2).toLowerCase();
				ent = ent.replaceAll("\\\\", "");
				if (_entityList.contains(ent)) {
					instEntity = ent;
					goodInst = true;
					System.out.println("INST ENTITY: "+instEntity);
				} else {
					goodInst = false;
				}
			}						
			
			if (portMatchMatch.lookingAt() && !inGeneric && goodInst) {
				String portName = portMatchMatch.group(1).toLowerCase();
				String signalName = portMatchMatch.group(2).toLowerCase();
				
				if (signalName.endsWith(comma)) {
					signalName = signalName.replace(comma, "");
				}
				if (signalName.endsWith(endInst)) {
					signalName = signalName.replace(endInst, "");
				}
				
				Pair<String, String> portKey = new Pair<String, String>(instEntity, portName);
				Pair<String, String> signalKey = new Pair<String, String>(currEntity, signalName);

				String fullPortName = _entitySignalsHash.get(portKey);
				String fullSignalName = _entitySignalsHash.get(signalKey);
				
				if (fullPortName != null && fullSignalName != null) {
					VHDLComponent portComp = _signalComponentMap.get(fullPortName); 
					VHDLComponent signalComp = _signalComponentMap.get(fullSignalName);	
					
					System.out.println("RESET PORT0: "+signalKey+"  FROM: "+"  TO SIG: "+fullPortName);
//					if (!signalComp.getName().equalsIgnoreCase(ParseSYNvhdl.TOP) && !signalComp.getName().equalsIgnoreCase(ParseSYNvhdl.CLK)) {
					if (VHDLSignal.getSignalDepth(fullSignalName) > 1) {							
						VHDLSignal signal = signalComp.removeSignal(fullSignalName);
						portComp.addSignal(signal);
						_signalComponentMap.put(fullSignalName, portComp);
	
						System.out.println("RESET PORT1: "+fullSignalName+"  FROM: "+signalComp.getName()+"  TO SIG: "+fullPortName+"  TYPE: "+portComp.getName());
	
						ArrayList<String> aliasList = _signalAliasHash.get(fullSignalName);
						if (aliasList != null) {
							for (String alias : aliasList) {
								VHDLComponent aliasComp = _signalComponentMap.get(alias);
								VHDLSignal aliasSignal = aliasComp.removeSignal(alias);
								portComp.addSignal(aliasSignal);
								_signalComponentMap.put(alias, portComp);							
								System.out.println("RESET ALIAS: "+aliasSignal.getFullSignalName()+"  FROM: "+aliasComp.getName()+"  TO SIG: "+fullPortName+"  TYPE: "+portComp.getName());
							}
						}
					}
				} else {
					System.out.println("NULL RESET PORT NAME: "+portName+"  "+instEntity+"  "+signalName);
				}
			}
			
			if (assignMatch.lookingAt()) {
				String signalName = assignMatch.group(2).toLowerCase();
				String aliasName = assignMatch.group(1).toLowerCase();
				signalName = signalName.replace(";", "");
				
				Pair<String, String> signalKey = new Pair<String, String>(currEntity, signalName);
				Pair<String, String> aliasKey = new Pair<String, String>(currEntity, aliasName);

				String fullSignalName = _entitySignalsHash.get(signalKey);
				String fullAliasName = _entitySignalsHash.get(aliasKey);
				
				if (fullSignalName != null) {
					VHDLComponent signalComp = _signalComponentMap.get(fullSignalName);
					VHDLComponent aliasComp = _signalComponentMap.get(fullAliasName);
					
//					if (!aliasComp.getName().equalsIgnoreCase(ParseSYNvhdl.TOP) && !aliasComp.getName().equalsIgnoreCase(ParseSYNvhdl.CLK)) {
					if (VHDLSignal.getSignalDepth(fullAliasName) > 1) {							
						VHDLSignal aliasSignal = aliasComp.removeSignal(fullAliasName);
						signalComp.addSignal(aliasSignal);
						_signalComponentMap.put(fullAliasName, signalComp);
						
						System.out.println("RESET SIGANL: "+fullAliasName+"  FROM: "+aliasComp.getName()+"  TO SIG: "+fullSignalName+"  TYPE: "+signalComp.getName());
					
						ArrayList<String> aliasList = _signalAliasHash.get(fullSignalName);
						if (aliasList == null) {
							aliasList = new ArrayList<String>();
							_signalAliasHash.put(fullSignalName, aliasList);
						}
						aliasList.add(fullAliasName);
					}
				} else {
					System.out.println("NULL RESET SIGNAL: "+signalName+"  "+currEntity+"  "+aliasName);
				}
			}
		}
		
		vhdfile.close();
	}
	
	protected void _createNetList() {
		for (VHDLComponent comp : _typeComponentMap.values()) {
			for (VHDLSignal vsignal : comp.getSignalList()) {
				vsignal.setPath(_pathHash.get(vsignal.getTopEntity()));
				_signalComponentMap.put(vsignal.getFullSignalName().toLowerCase(), comp);
				Pair<String, String> entitySignalPair = new Pair<String, String>(vsignal.getTopEntity().toLowerCase(), vsignal.getSignalName().toLowerCase());
				_entitySignalsHash.put(entitySignalPair, vsignal.getFullSignalName());
				System.out.println("CREATE FULL: "+vsignal.getFullSignalName()+"  FOR ENTITY: "+vsignal.getTopEntity()+"  "+comp.getName());
				System.out.println("\tPAIR: "+vsignal.getTopEntity().toLowerCase()+"  "+vsignal.getSignalName().toLowerCase());
			}
		}
	}

	protected void _createPaths(String path, String currEntity) {
		System.out.println("PATH: "+path+"  ENTITY: "+currEntity);
		if (_instListHash.get(currEntity) != null) {
			for (String instance : _instListHash.get(currEntity)) {
				System.out.println("\tINST: "+instance);
				String newPath = (path == "") ? instance : path+"/"+instance;
				Pair<String, String> key = new Pair<String, String>(currEntity, instance);
				String entity = _instEntityHash.get(key);
				_pathHash.put(entity, newPath);
				_createPaths(newPath, entity);				
			}
		} else {
			System.out.println("END");
		}
	}
	
	protected void _addSignal(String signal) {
				
		VHDLSignal vsignal = new VHDLSignal(_currEntity, signal);
		
		// HACK to strip ALU signals from IU3 signals.  But don't steal the MULT/DIV signals
		if (_currComponent.equalsIgnoreCase(IU3) && _typeComponentMap.get(ALU).isSignalInFilterList(signal)) {
//				&& !(_currEntity.startsWith("proc3") && signal.startsWith("result"))) {
			_typeComponentMap.get(ALU).addSignal(vsignal);
			System.out.println("CURR ENTITY: "+_currEntity+"  SIGNAL: "+signal+"  ALU");
		} else {
			_listRef.add(vsignal);
			System.out.println("CURR ENTITY: "+_currEntity+"  SIGNAL: "+signal);
		}

	}
	
	/**
	 * Parses the Synplicity generated VHDL file and parses the entities contained based on the filters
	 * established in the _init() method.  For each entity the following names are grabbed: 
	 * 		1) output port names
	 * 		2) signal names
	 * 		3) instance names
	 * 
	 * @throws Exception
	 */
	protected void _parseFile() throws Exception {
		String entity = "entity";
		String out = "out";
		String inout = "inout";
		String colon = ":";
		String end = "end";
		String stdlogic = "std_logic";
		String stdlogicv = "std_logic_vector";
		String downto = "downto";
		String to = "to";
		String signal = "signal";
		
		boolean inentity = false;
		
		BufferedReader vhdfile = LEON3ReliabilityAnalyzer.openForReading(_vhdFile);
		String line = "";
		
		while ((line = vhdfile.readLine()) != null) {
			String findEntity = entity+"\\s+(\\S+)";
			String findPort = "\\s*(\\S+)\\s+"+colon+"\\s+("+out+"|"+inout+")";
			String findPortStdv = "\\s*(\\S+)\\s+"+colon+"\\s+("+out+"|"+inout+")\\s+"+stdlogicv+"\\((\\d+)\\s+"+downto+"\\s+(\\d+)";
			String findPortStd = "\\s*(\\S+)\\s+"+colon+"\\s+("+out+"|"+inout+")\\s+"+stdlogic;
			String findEnd = "\\s*"+end;
			String findSignalStdv = "\\s*"+signal+"\\s+(\\S+)\\s+"+colon+"\\s+"+stdlogicv+"\\((\\d+)\\s+("+downto+"|"+to+")\\s+(\\d+)";
			String findSignalStd = "\\s*"+signal+"\\s+(\\S+)\\s+"+colon+"\\s+"+stdlogic;
			String findInst = "\\\\(\\S+)\\\\"+colon+"\\s*(\\S+)";
			String findInstName = "\\\\*(\\S+)\\\\*"+colon+"\\s*(\\S+)";
						
			Matcher entityMatch = Pattern.compile(findEntity).matcher(line);
			Matcher portMatch = Pattern.compile(findPort).matcher(line);
			Matcher portStdvMatch = Pattern.compile(findPortStdv).matcher(line);
			Matcher portStdMatch = Pattern.compile(findPortStd).matcher(line);
			Matcher endMatch = Pattern.compile(findEnd).matcher(line);
			Matcher signalStdvMatch = Pattern.compile(findSignalStdv).matcher(line);
			Matcher signalStdMatch = Pattern.compile(findSignalStd).matcher(line);
			Matcher instMatch = Pattern.compile(findInst).matcher(line);
			Matcher instNameMatch = Pattern.compile(findInstName).matcher(line);
			
			// entity instantiation found - grab the instance name and entity name being instantiated
			// populate list if instantiated entities within the current entity
			if (instNameMatch.lookingAt()) {
				String instName = instNameMatch.group(1).toLowerCase();
				String ent = instNameMatch.group(2).toLowerCase();
				instName = instName.replaceAll("\\\\", "");
				ent = ent.replaceAll("\\\\", "");
				System.out.println("INST MATCH: "+instName);
				
				// only consider instantiations of top-level entities (no primitives)
				if (_entityList.contains(ent)) {
					ArrayList<String> instListHashList = _instListHash.get(_currEntity);
					if (instListHashList == null) {
						instListHashList = new ArrayList<String>();
						_instListHash.put(_currEntity, instListHashList);
					}
	
					System.out.println("PLIST: "+ent+"  ILIST: "+instName+"  CURRENT: "+_currEntity+"  TYPE: "+_currType);					
					instListHashList.add(instName);
					Pair<String, String> key = new Pair<String, String>(_currEntity, instName);
					_instEntityHash.put(key, ent);
				} 
			}
			
			// entity declaration found
			// find which comonent it corresponds to based on filters
			if (entityMatch.lookingAt()) {
				String name = entityMatch.group(1).toLowerCase();
				name = name.replaceAll("\\\\", "");
				boolean found = false;
				inentity = true;
				_entityList.add(name);
				_currEntity = name;
				
				found = _findListName(found, name, TOP);
				found = _findListName(found, name, REGFILE);
				found = _findListName(found, name, DCACHECTRL);
				found = _findListName(found, name, ICACHECTRL);
				found = _findListName(found, name, DCACHE);
				found = _findListName(found, name, ICACHE);
				found = _findListName(found, name, DTAG);
				found = _findListName(found, name, ITAG);
				found = _findListName(found, name, CHK);
				found = _findListName(found, name, MM);
				found = _findListName(found, name, MMCACHECTRL);
				found = _findListName(found, name, CACHECTRL);
				found = _findListName(found, name, MULDIV);
				found = _findListName(found, name, CACHE);
				found = _findListName(found, name, IRQ);
				found = _findListName(found, name, CMP);
				found = _findListName(found, name, IU3);
				found = _findListName(found, name, CTRL);
				
			} else if (inentity) {
				// we're inside an entity declaration
				
				// look to see if the current line is an output or inout port declaration
				if (portMatch.lookingAt()) {
					String signalName = portMatch.group(1).toLowerCase();
					
					// check to see if the current port declaration is a std_logic_vector
					if (portStdvMatch.lookingAt()) {
						int lsb = Integer.parseInt(portStdvMatch.group(4));
						int msb = Integer.parseInt(portStdvMatch.group(3));
//						System.out.println("STD_LOGIC_VECT PORT: "+signalName+"  LSB: "+lsb+"  MSB: "+msb);
						
						for (int i = lsb; i <= msb; i++) {
							String fullSignalName = signalName+"("+i+")";
							_addSignal(fullSignalName);
						}
						
					} else if (portStdMatch.lookingAt()) {
//						System.out.println("STD_LOGIC PORT: "+signalName);						
						_addSignal(signalName);
					}
					
				} else if (endMatch.lookingAt()) {
					// reached the end of the entity declaration
					inentity = false;
				}
			} else if (signalStdvMatch.lookingAt()) {
				// signal declaration which is of type std_logic_vector
				
				String signalName = signalStdvMatch.group(1).toLowerCase();
				int lsb = Integer.parseInt(signalStdvMatch.group(4));
				int msb = Integer.parseInt(signalStdvMatch.group(2));
				for (int i = lsb; i <= msb; i++) {
					String fullSignalName = signalName+"("+i+")";
					_addSignal(fullSignalName);
				}
				
			} else if (signalStdMatch.lookingAt()) {
				// signal declaration of type std_logic
				
				String signalName = signalStdMatch.group(1).toLowerCase();
				_addSignal(signalName);
				
			} else if (instNameMatch.lookingAt()) {
//				String signalName = instMatch.group(1).toLowerCase();
				String signalName = instNameMatch.group(1).toLowerCase();
				signalName = signalName.replaceAll("\\\\", "");
//				String entName = instMatch.group(2).toLowerCase();
				String entName = instNameMatch.group(2).toLowerCase();
				entName = entName.replaceAll("\\\\", "");
				
				System.out.println("INST FOUND: "+signalName+"  "+entName);
				signalName = signalName.replaceFirst("_z\\d+", "");
				for (String primitive : PRIMITIVELIST) {
					if (entName.startsWith(primitive.toLowerCase())) {
						signalName = signalName+"/o";
						break;
					}						
				}
				if (entName.startsWith("FD")) {
					signalName = signalName.replace("[", "(");
					signalName = signalName.replace("]", ")");
				}
				
				_addSignal(signalName);
			} else if (instMatch.lookingAt()) {	
				String signalName = instMatch.group(1).toLowerCase();
				signalName = signalName.replaceAll("\\\\", "");
				String entName = instMatch.group(2).toLowerCase();
				entName = entName.replaceAll("\\\\", "");
				
				System.out.println("INST2 FOUND: "+signalName+"  "+entName);
				signalName = signalName.replaceFirst("_z\\d+", "");
				for (String primitive : PRIMITIVELIST) {
					if (entName.startsWith(primitive.toLowerCase())) {
						signalName = signalName+"/o";
						break;
					}						
				}
				if (entName.startsWith("FD")) {
					signalName = signalName.replace("[", "(");
					signalName = signalName.replace("]", ")");
				}
				
				_addSignal(signalName);
			}
			
		}
		
		vhdfile.close();
	}
	
	protected boolean _findListName(boolean found, String name, String type) {
		
		if (!found) {		
			if (found = _typeComponentMap.get(type).isEntityInFilterList(name)) {
				System.out.println("FOUND: CompType - "+type+"  NAME: "+name);
				_listRef = _typeComponentMap.get(type).getSignalList();
				_currComponent = type;
				_currType = type;				
			}			
		}		
		return found;
	}
	
	protected String _vhdFile;
	protected int _type;
	
	protected HashSet<VHDLSignal> _listRef;
	
	public static String TOP = "top";
	public static String CLK = "clk";
	public static String RST = "rst";
	public static String IU3 = "iu3";
	public static String ALU = "alu";
	public static String CTRL = "ctrl";
	public static String CACHECTRL = "cachectrl";
	public static String ICACHECTRL = "icachectrl";
	public static String DCACHECTRL = "dcachectrl";
	public static String CACHE = "cache";
	public static String ICACHE = "icache";
	public static String DCACHE = "dcache";
	public static String ITAG = "itag";
	public static String DTAG = "dtag";
	public static String MMCACHECTRL = "mmcachectrl";
	public static String IRQ = "irq";
	public static String CMP = "cmp";
	public static String MULDIV = "muldiv";
	public static String REGFILE = "regfile";
	public static String MM = "mm";
	public static String CHK = "chk";
	public static String OTHERCLASS = "other";
	public static String GLOBAL = "global";
	public static String IOB = "iob";
	public static String DSP = "dsp";
	public static String BRAM_ATTR = "bram_attr";
	public static String NONET = "nonet";
	public static String UNKNOWN = "unknown";
	
	public static String[] CLASSES = {TOP, CLK, RST, GLOBAL, ICACHECTRL, DCACHECTRL, CACHECTRL, 
		ICACHE, DCACHE, ITAG, DTAG, CACHE, MMCACHECTRL, IRQ, CMP, MULDIV, REGFILE, MM, CHK, ALU, IU3, CTRL, 
		OTHERCLASS, IOB, DSP, BRAM_ATTR, NONET, UNKNOWN
	};
	public static List<String> CLASSESLIST = Arrays.asList(CLASSES);

	public static String[] PRIMITIVES = {"LUT", "XORCY", "MUX"};
	public static List<String> PRIMITIVELIST = Arrays.asList(PRIMITIVES);		

	protected HashMap<String, VHDLComponent> _typeComponentMap;
	protected HashMap<String, VHDLComponent> _signalComponentMap;
	protected HashMap<Pair<String, String>, String> _instEntityHash;
	protected HashMap<String, ArrayList<String>> _instListHash;
	protected HashMap<String, ArrayList<String>> _signalAliasHash;
	protected HashMap<Pair<String, String>, String> _entitySignalsHash;
	protected HashMap<String, String> _pathHash;
	protected HashMap<String, String> _typeMap;
	protected ArrayList<String> _entityList;
	protected String _currEntity;
	protected String _currType;
	protected String _currComponent;
	protected String _topEntity = "dut_basic";
}

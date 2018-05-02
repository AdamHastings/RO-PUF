package edu.byu.ece.gremlinTools.ReliabilityTools;

import edu.byu.ece.rapidSmith.design.Instance;
import edu.byu.ece.rapidSmith.design.Net;
import edu.byu.ece.rapidSmith.device.WireDirection;
import edu.byu.ece.rapidSmith.device.WireType;
import edu.byu.ece.rapidSmith.device.helper.WireExpressions;

public class FilterExample {
	
	public void FilterDesignIntoComponents() {
		
		// Create Components
		
		Component DDR2 = new Component("DDR2_SDRAM");		
		DDR2.setLatexName("DDR2");
		
		Component BRAM_IF_CNTLR = new Component("bram_if_cntlr");
		BRAM_IF_CNTLR.setLatexName("BRAMCNTRL");
		
		Component PPC405_0_dplb = new Component("ppc405_0_dplb1");
		PPC405_0_dplb.setLatexName("DPLB");
		
		Component PPC405_0_iplb = new Component("ppc405_0_iplb1");
		PPC405_0_iplb.setLatexName("IPLB");
		
		Component PPC405 = new Component("ppc");
		PPC405.setLatexName("PPC");
		
		Component SystemAce = new Component("system_ace");
		SystemAce.setLatexName("SystemAce");
		
		Component JTAG_CNTRL = new Component("jtagppc_cntlr");		
		JTAG_CNTRL.setLatexName("JTAGDEBUG");
		
		Component GPIO = new Component("gpio");
		GPIO.setLatexName("GPIO");
		
		Component ICAP = new Component("icap");		
		ICAP.setLatexName("ICAP");
		
		Component UART = new Component("Uart");		
		UART.setLatexName("UART");		
		
		Component IIC = new Component("IIC");		
		IIC.setLatexName("IIC");
		
		Component SYSTEM_RESET = new Component("proc_sys_reset_0");		
		SYSTEM_RESET.setLatexName("SYSTEMRESET");
		
		// Component DCM = new Component("DCM");
		
		Component CLOCK_GENERATOR = new Component("clock_generator");		
		CLOCK_GENERATOR.setLatexName("CLOCKGEN");
		
		Component intc = new Component("intc");		
		intc.setLatexName("INTC");
		
		Component PLB = new Component("plb");		
		PLB.setLatexName("PLB");
		
		Component SYSTEMINSTANCES = new Component("");		
		SYSTEMINSTANCES.setLatexName("SYSTEM");
		
		Component MGT_Wrapper = new Component("MGT_wrapper");		
		MGT_Wrapper.setLatexName("MGTPROTECTOR");
		
		// Add Search Terms
		
		DDR2.addSearchTerm("mpmc");		
		SystemAce.addSearchTerm("SysACE");		
		UART.addSearchTerm("TX");		
		UART.addSearchTerm("RX");		
		CLOCK_GENERATOR.addSearchTerm("DCM");		
		CLOCK_GENERATOR.addSearchTerm("dcm");		
		CLOCK_GENERATOR.addSearchTerm("Dcm");		
		CLOCK_GENERATOR.addSearchTerm("clk");		
		CLOCK_GENERATOR.addSearchTerm("CLK");		
		SYSTEM_RESET.addSearchTerm("rst");		
		SYSTEM_RESET.addSearchTerm("reset");		
		SYSTEM_RESET.addSearchTerm("SBR_PWG_pin");	
		SYSTEM_RESET.addSearchTerm("sys_rst_pin");		
		SYSTEM_RESET.addSearchTerm("RST");		
		SYSTEM_RESET.addSearchTerm("sys_bus_reset");		
		SYSTEM_RESET.addSearchTerm("sys_periph_reset");		
		SYSTEMINSTANCES.addSearchTerm("TIEOFF");
		// SYSTEMINSTANCES.addSearchTerm("MGT");
		SYSTEMINSTANCES.addSearchTerm("PMV");		
		SYSTEMINSTANCES.addSearchTerm("DUMMY");
		
		Components.add(DDR2);		
		Components.add(PPC405_0_dplb);
		Components.add(PPC405_0_iplb);		
		Components.add(BRAM_IF_CNTLR);		
		Components.add(JTAG_CNTRL);		
		Components.add(PPC405);		
		Components.add(SystemAce);		
		Components.add(GPIO);		
		Components.add(ICAP);		
		Components.add(UART);		
		Components.add(IIC);		
		Components.add(SYSTEM_RESET);		
		Components.add(intc);		
		Components.add(PLB);		
		Components.add(CLOCK_GENERATOR);		
		Components.add(MGT_Wrapper);		
		Components.add(SYSTEMINSTANCES);
		
		// Filter Instances
		
		// System.out.println("Instances---------------------------------------------");
		
		for (Instance I : design.getInstances()) {			
			boolean found = false;
			
			// if(I.getTile().getTileYCoordinate() > 15){			
			for (Component C : Components) {				
				if (C.InstanceBelongsToMe(I)) {					
					found = true;					
					break;					
				}				
			}			
			if (!found) {		
				System.out.println(I.getName());				
			}			
			// }			
		}
		
		// Filter Nets		
		// System.out.println("Nets-------------------------------------------");
		
		for (Net N : design.getNets()) {			
			// if(PipAboveTile(N, 15)){			
			boolean found = false;			
			for (Component C : Components) {				
				if (C.NetBelongsToMe(N)) {					
					found =	true;					
					break;					
				}				
			}			
			if (!found) {			
				System.out.println(N.getName());				
			}			
			// }			
		}
		
		int unmatchedIntFailures = 0;		
		for (InterconnectFailure I : IA.getInterconnectFailures()) {			
			boolean found = false;			
			for (Component C : Components) {				
				if (C.InterconnectFailureBelongsToMe(I)) {					
					found =	true;					
					break;					
				}			
			}
			
			if (!found) {				
				unmatchedIntFailures++;				
			}
			
		}
		
		int unmatchedLogicFailures = 0;
		
		for (LogicFailure L : LA.getLogicFailures()) {			
			boolean found = false;			
			for (Component C : Components) {				
				if (C.LogicFailureBelongsToMe(L)) {					
					found =	true;					
					break;					
				}
				
			}
			
			if (!found) {				
				unmatchedLogicFailures++;				
			}			
		}
		
		System.out.println("Unmatched LogicFailures: " + unmatchedLogicFailures + " Unmatched Routing Failures: "
				+ unmatchedIntFailures);		
		printComponentReports();		
		System.out.println("\n\n\n\n");
		
		generateLatexTable();
		
		// Count the number of Row-Buffer Shorts that occur in OMUX PIPs		
		int colBufferFailuresInOMUX = 0;		
		int colBufferFailuresFromBLO = 0;		
		int colBufferFailuresOther = 0;		
		int colBufferSliceInputs = 0;		
		int colBufferShortMidEnd = 0;
		
		for (InterconnectFailure I : IA.getInterconnectFailures()) {			
			if (I.getInterconnectFailureType().equals(InterconnectFailureType.BUFFER)
					&& I.getRowOrColumn().equals(BitType.COLUMN)) {				
				String EndWire = I.getPIP().getEndWireName(design.getWireEnumerator());				
				String StartWire = I.getPIP().getStartWireName(design.getWireEnumerator());
				if (EndWire.startsWith("OMUX")) {					
					colBufferFailuresInOMUX++;					
				} else if (StartWire.startsWith("BEST_LOGIC_OUTS")) {					
					colBufferFailuresFromBLO++;					
				} else if (StartWire.contains("MID") || StartWire.contains("END")) {					
					colBufferShortMidEnd++;					
				} else if (EndWire.startsWith("IMUX") || EndWire.startsWith("BYP_INT_B")) {					
					colBufferSliceInputs++;					
				} else {					
					colBufferFailuresOther++;					
					// System.out.println(I.getPIP().toString(design.getWireEnumerator()));					
				}				
			}
			
		}
		
		System.out.println("Col Buffer Failures: OMUX: " + colBufferFailuresInOMUX + " BLO: " + colBufferFailuresFromBLO
				+ " Slice Inputs: " + colBufferSliceInputs + " MID/END: " + colBufferShortMidEnd + " Other: "
				+ colBufferFailuresOther);
		
		WireExpressions WEx = new WireExpressions();
		
		// Count the number of Row-Buffer Shorts that occur in OMUX PIPs
		int rowBufferFailuresInOMUX = 0;
		int rowBufferFailuresFromBLO = 0;
		int rowBufferFailuresOther = 0;
		int rowBufferSliceInputs = 0;
		int rowlocalBufferShortMidEnd = 0;
		int rowDistantBufferShortMidEnd = 0;
		int unexpectedRowFailures = 0;
		int nullSEUConnections = 0;
		
		for (InterconnectFailure I : IA.getInterconnectFailures()) {
			if (I.getInterconnectFailureType().equals(InterconnectFailureType.BUFFER)
					&& I.getRowOrColumn().equals(BitType.ROW)) {
				String EndWire = I.getPIP().getEndWireName(design.getWireEnumerator());
				String StartWire = I.getPIP().getStartWireName(design.getWireEnumerator());
				WireType EndWireType = WEx.getWireType(EndWire);
				WireType StartWireType = WEx.getWireType(StartWire);
				WireDirection StartWireDirection = WEx.getWireDirection(StartWire);
				WireType SEUStartWireType = null;
				WireDirection SEUstartDirection = null;
				if (I.getSEUConnection() != null) {
					SEUStartWireType = WEx.getWireType(I.getSEUConnection().getStartWireName(design.getWireEnumerator()));
					SEUstartDirection = WEx.getWireDirection(I.getSEUConnection().getStartWireName(design.getWireEnumerator()));
				} else {
					nullSEUConnections++;
				}
				
				if (EndWireType.equals(WireType.OMUX)) {					
					rowBufferFailuresInOMUX++;					
				} else if ((StartWire.contains("MID") || StartWire.contains("END"))
						&& isLocalToSwitchBox(SEUstartDirection, SEUStartWireType)) {
					rowlocalBufferShortMidEnd++;
				} else if ((StartWire.contains("MID") || StartWire.contains("END"))
						&& !isLocalToSwitchBox(SEUstartDirection, SEUStartWireType)) {
					rowDistantBufferShortMidEnd++;
				} else if (isLocalToSwitchBox(StartWireDirection, StartWireType)
						&& !isLocalToSwitchBox(SEUstartDirection, SEUStartWireType)) {
					unexpectedRowFailures++;
					
					// System.out.println("Original Connection:" +
					// I.getPIP().toString(design.getWireEnumerator()) +
					// " SEU Connection " +
					// I.getSEUConnection().toString(design.getWireEnumerator()));
				} else if (EndWireType.equals(WireType.INT_SINK)) {
					rowBufferSliceInputs++;
				} else {
					rowBufferFailuresOther++;
					// System.out.println(I.getPIP().toString(design.getWireEnumerator()));
				}				
			}			
		}
		
		System.out.println("Row Buffer Failures: OMUX: " + rowBufferFailuresInOMUX + " BLO: " + rowBufferFailuresFromBLO
				+ " Slice Inputs: " + rowBufferSliceInputs + "Local MID/END: " + rowlocalBufferShortMidEnd
				+ " Distant MID/END " + rowDistantBufferShortMidEnd + " unexpected " + unexpectedRowFailures
				+ " Other: " + rowBufferFailuresOther + " NULL SEU Connections :" + nullSEUConnections);
		
		// for(Net N: SYSTEMINSTANCES.getNets()){		
		// System.out.println(N.getName());		
		// }		
	}
}

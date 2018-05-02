import java.util.Collection;

import edu.byu.ece.rapidSmith.design.*;
import edu.byu.ece.rapidSmith.util.FileConverter;
import edu.byu.ece.rapidSmith.util.MessageGenerator;

public class LoadDesign {
    public static void main(String[] args) {
        // Prints "Hello, World" to the terminal window.
        System.out.println("Hello, World");
        
        String ncdFileName = "/home/adam/RO-PUF/planAhead_workspace/Counter/Counter.runs/impl_1/system_routed.ncd";
        String xdlFileName = FileConverter.convertNCD2XDL(ncdFileName);
//        String xdlFileName = "/home/adam/RO-PUF/planAhead_workspace/Counter/Counter.runs/impl_1/system_routed.xdl";
        if (xdlFileName == null) {
        	MessageGenerator.briefErrorAndExit("ERROR: Conversion of " +ncdFileName + " to XDL failed.");
        }
        System.out.println("Succesfully converted design!");

        Design design = new Design();
        design.loadXDLFile(xdlFileName);
        
        System.out.println(design.getName());
        System.out.println(design.getFamilyName());
        System.out.println(design.getPartName());
//        System.out.println(design.getNets());
//        Collection<Net> nets = design.getNets();
//        int i=0;
//        for (Net n : design.getNets()) {
//        	if (i > 100) {
//        		break;
//        	}
//        	i++;
//        	System.out.println(n.getName());
//        }
        Instance inst = design.getInstance("counter_0/counter_0/ipif_IP2Bus_Data[0]");
    	System.out.println(inst.getName());
    	System.out.println(inst.getInstanceX());
    	System.out.println(inst.getInstanceY());
    	System.out.println(inst.getPrimitiveSiteName());
    	System.out.println(inst.getType());
    	System.out.println("\n\n");
    	
    	
        
    }
}

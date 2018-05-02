package edu.byu.ece.gremlinTools.xilinxToolbox;

import edu.byu.ece.rapidSmith.bitstreamTools.configurationSpecification.V4ConfigurationSpecification;
import edu.byu.ece.rapidSmith.bitstreamTools.configurationSpecification.V5ConfigurationSpecification;
import edu.byu.ece.rapidSmith.bitstreamTools.configurationSpecification.V6ConfigurationSpecification;
import edu.byu.ece.rapidSmith.bitstreamTools.configurationSpecification.XilinxConfigurationSpecification;
import edu.byu.ece.rapidSmith.design.Design;

public class XilinxToolboxLookup {

    private static XilinxToolboxLookup _singleton = null;
    
    private XilinxToolboxLookup(){
    	
    }
    
    /**
     * Get (create if necessary) the singleton instance of this class.
     */
    public static XilinxToolboxLookup sharedInstance() {
        if (_singleton == null) {
            _singleton = new XilinxToolboxLookup();
        }
        return _singleton;
    }
    
    public static XilinxToolbox toolboxLookup(XilinxConfigurationSpecification spec){
    	if(spec.getDeviceFamily().equals(V4ConfigurationSpecification.V4_FAMILY_NAME)){
    		return V4XilinxToolbox.getSharedInstance();
    	}
    	else if(spec.getDeviceFamily().equals(V5ConfigurationSpecification.V5_FAMILY_NAME)){
    		return new V5XilinxToolbox();
    	}
    	else if(spec.getDeviceFamily().equals(V6ConfigurationSpecification.V6_FAMILY_NAME)){
    		return new V6XilinxToolbox();
    	}
    	else{
    		return null;
    	}
    }
}

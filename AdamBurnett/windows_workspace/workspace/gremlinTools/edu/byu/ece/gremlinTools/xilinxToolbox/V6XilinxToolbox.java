package edu.byu.ece.gremlinTools.xilinxToolbox;

import edu.byu.ece.rapidSmith.bitstreamTools.configuration.FrameAddressRegister;
import edu.byu.ece.rapidSmith.bitstreamTools.configurationSpecification.V6ConfigurationSpecification;
import edu.byu.ece.rapidSmith.bitstreamTools.configurationSpecification.XilinxConfigurationSpecification;


public class V6XilinxToolbox extends AbstractXilinxToolbox{
	
	@Override
	public boolean isClkColumn(XilinxConfigurationSpecification spec,
			FrameAddressRegister far) {
		return spec.getBlockSubtype(spec, far).equals(V6ConfigurationSpecification.CLK);
	}
}

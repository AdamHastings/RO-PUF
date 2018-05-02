package edu.byu.ece.gremlinTools.ReliabilityTools;

import edu.byu.ece.gremlinTools.Virtex4Bits.V4ConfigurationBit;
import edu.byu.ece.rapidSmith.design.Attribute;
import edu.byu.ece.rapidSmith.design.Instance;

public class LogicFailure {
    private Instance I; //This is the Instance that was affected
    private Attribute A; //The Attribute in the design that failed
    private boolean isAttributeInDesign;
    private Attribute OA; //If the Attribute was not in the then OA should be set to the Attribute that was originally effected
    private V4ConfigurationBit B; 
    
    public LogicFailure(){
    	I = null;
    	A = null; 
    	isAttributeInDesign = false; 
    	OA= null;
    	B =null; 
    }
    
    
	public void setInstance(Instance i) {
		I = i;
	}
	
	public void setAttribute(Attribute a) {
		A = a;
	}
	
	public void setIsAttributeInDesign(boolean isAttributeInDesign) {
		this.isAttributeInDesign = isAttributeInDesign;
	}
	
	public void setOriginalAttribute(Attribute oA) {
		OA = oA;
	}
	
	public Instance getInstance() {
		return I;
	}
	
	public Attribute getAttribute() {
		return A;
	}

	public boolean isAttributeInDesign() {
		return isAttributeInDesign;
	}

	public Attribute getOriginalAttribute() {
		return OA;
	}


	public void setSensitiveBit(V4ConfigurationBit b) {
		B = b;
	}


	public V4ConfigurationBit getSensitiveBit() {
		return B;
	}
    
}

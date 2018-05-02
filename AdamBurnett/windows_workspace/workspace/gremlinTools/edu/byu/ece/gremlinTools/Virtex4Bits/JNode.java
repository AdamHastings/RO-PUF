package edu.byu.ece.gremlinTools.Virtex4Bits;

import edu.byu.ece.rapidSmith.device.Tile;
import edu.byu.ece.rapidSmith.router.Node;

public class JNode extends Node {
    
    public boolean visited; 
	private boolean used; 
    public JNode(){
    
    	super();
    	visited = false; 
    	used = false;
    }
    
    public JNode(Tile tile, int wire, Node parent, int level){
        
    	super();
    	
    	setTile(tile);
		setWire(wire);
		setParent(parent);
		setLevel(level);
  
    	visited = false;
    	used    = false;
    }
    
	public void setCost(int cost) {
		this.cost = cost; 
	}
	
	public void setHistory(int h){
		this.history = h; 
	}
	
	public int getHistory(){
		return this.history;
	}
	
	public void setVisited(boolean visited){
		this.visited = visited;
	}
	
	public boolean getVisited(){
		return this.visited; 
	}

	public void setUsed(boolean used) {
		this.used = used;
	}

	public boolean isUsed() {
		return used;
	}
    
}

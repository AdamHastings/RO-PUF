package edu.byu.ece.gremlinTools.ReliabilityTools;

import edu.byu.ece.rapidSmith.design.Instance;
import edu.byu.ece.rapidSmith.device.Tile;

public class Dgraph
{
	public String Componet;
	public String Value;
	public int index;
	public int curIndex;
	public Tile tile;
	public Instance InstanceInDesign;
	public int X;
	public int Y;
	public Dgraph parent;
	public Dgraph[] next;

	public void printall()
	{	if(this.tile!=null){
		System.out.println(this.Componet+" "+this.Value+"\t index:"+ this.index+
				 " tile:"+ this.tile.getName()+" SliceX"+ this.X+"Y"+ this.Y+" "+printnext());
		}
		else{
			System.out.println(this.Componet+" "+this.Value+"\t index:"+ this.index+
					 " tile:null SliceX"+ this.X+"Y"+ this.Y+" "+printnext());
		}
	}
	private String printnext()
	{	String next ="";
		if(this.next==null)
			return next;
		next+=("[");
		for(Dgraph d:this.next)
			next+=(d.Componet+" "+d.Value+"; ");	
		next= next.substring(0, next.length()-2)+("]");
		return next;
	}
	
}

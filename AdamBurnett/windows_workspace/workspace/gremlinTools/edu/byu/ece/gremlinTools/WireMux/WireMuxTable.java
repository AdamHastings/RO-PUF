package edu.byu.ece.gremlinTools.WireMux;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.io.Serializable;
import java.io.ObjectOutputStream;

import XDLDesign.Utils.XDLDesignUtils;


import edu.byu.ece.gremlinTools.Virtex4Bits.Bit;
import edu.byu.ece.gremlinTools.Virtex4Bits.Pip;
import edu.byu.ece.gremlinTools.xilinxToolbox.V4XilinxToolbox;
import edu.byu.ece.rapidSmith.design.Design;
import edu.byu.ece.rapidSmith.design.PIP;
import edu.byu.ece.rapidSmith.device.Tile;
import edu.byu.ece.rapidSmith.device.WireEnumerator;
import edu.byu.ece.rapidSmith.device.WireType;

public class WireMuxTable implements Serializable{
	
	private Pip [][] table; 
	private ArrayList<Pip> Pips; 
	private int destWire;
	private WireEnumerator we; 
	private int rows;
	private int cols; 
	
	private HashMap<Bit, Integer> RowMap; 
	private HashMap<Bit, Integer> ColMap; 
    private Bit [] RowArray; 
    private Bit [] ColArray;
    private Tile T; 
    private WireType Type; 
 
	public WireMuxTable(int destwire, HashSet<Bit> Bits, ArrayList<Pip> PIPs, WireEnumerator w, boolean transposed){
          
		   
		  this.we = w; 
		  this.setDestWire(destwire);
		  this.Type = w.getWireType(destWire); 
		  ArrayList<Group> Groups = new ArrayList<Group>();	  	
		  HashSet<Bit> BitSet0 = new HashSet<Bit>(); 
	      HashSet<Bit> BitSet1 = new HashSet<Bit>(); 
	      
	      this.RowMap = new HashMap<Bit, Integer>(); 
	      this.ColMap = new HashMap<Bit, Integer>();
	      
	      this.Pips = PIPs; 
	      
	      if(w.getWireType(destwire).equals(WireType.LONG)){
	    	  //Extract Long Line
	    	  buildLongLineTable(Bits, PIPs, w);
		  } else { 
		      for(Bit Q: Bits){
		  		
		  		Group G = new Group(Q);
		  		
		  		//Add all the PIPs that Contain this Bit
		  		for(Pip pp: this.Pips){
		  			for(Bit q: pp.getBits()){
		  				if(Q.equals(q)){
		  					G.addPip(pp);
		  				}
		  			}
		  		}
		  		  		
		  		//If the Bit has Not been Added to a Set
		  		if(!BitSet0.contains(G.getBit()) && !BitSet1.contains(G.getBit())){
		  		   //Add the Bit to the First Set
		  		   BitSet0.add(G.getBit());
		  		   //Add the Other Bits in the Group to the Other Set
		  		   for(Pip ppp: G.getGroupPips()){
		  			   for(Bit bb : ppp.getBits()){
		  				   if(!G.getBit().equals(bb)){
		  					   BitSet1.add(bb);
		  				   }
		  			   }
		  		   }
		  		}
		  		  		
		  		//Add this Group to the List of Groups
		  		Groups.add(G);	
	  	    }
		    
		    //Set the Row as the smaller dimension because that is the more likely case
		    if(BitSet0.size() <= BitSet1.size()){
		    	rows = BitSet0.size(); 
			    cols = BitSet1.size(); 	
			    RowArray = BitSet0.toArray(new Bit[BitSet0.size()]); 
		        ColArray = BitSet1.toArray(new Bit[BitSet1.size()]);
		    } else {
		    	rows = BitSet1.size(); 
			    cols = BitSet0.size(); 
			    RowArray = BitSet1.toArray(new Bit[BitSet1.size()]); 
		        ColArray = BitSet0.toArray(new Bit[BitSet0.size()]);
		    }
		    
		    table = new Pip [rows][cols];
		    
		    //Initialize the HashMaps
		    for(int i=0; i<RowArray.length; i++){
		    	RowMap.put(RowArray[i], i);
		    }
		    
		    for(int i=0; i<ColArray.length; i++){
		    	ColMap.put(ColArray[i], i);
		    }
		    
		    //Initialize the Table
		    for(int i=0; i<RowArray.length; i++){
		    	Bit RowBit = RowArray[i];
				Group Row = findBitGroup(RowBit, Groups);
		    	for(int j=0; j<ColArray.length; j++){
		    		 Pip P = findPipInGroup(Row, ColArray[j]);
		    		 if(P != null){ //P might equal null if a row/column is not completely populated
		    		   P.setRowBit(RowArray[i]);
		    		   P.setColumnBit(ColArray[j]);
		    		   table[i][j] = P;
		    		 } else {
		    			 table[i][j] = null;
		    		 }
		    	}
		    }
		    
		    if(transposed){
		    	this.TransposeTable(); 
		    }
	  }
	}
	private void AddPipsToGroup(Group G){
		for(Pip pp: Pips){
	  		for(Bit q: pp.getBits()){
	  			if(G.getBit().equals(q)){
	  				G.addPip(pp);
	  			}
	  		}
	  	}
	}
	
	public WireType getWireType(){
		return Type; 
	}
	
	private void buildLongLineTable(HashSet<Bit> bits, ArrayList<Pip> pips2, WireEnumerator w) {
		
		ArrayList<Group> Groups = new ArrayList<Group>(); 
		
		for(Bit Q: bits){
		  	Group G = new Group(Q);
		  	AddPipsToGroup(G);
		    Groups.add(G);		
		}
		 
		Group Buffer = null; 
		Group Mux = null; 
		
		ArrayList<Group> ColumnGroup = new ArrayList<Group>();
		
		//Find Buffer Bit, Encoded Mux Bit and Column Bits
		for(Group G : Groups){
			int size = G.getGroupPips().size();
			//System.out.println("Size: " + size);
			if(size == 18){
				Buffer = G;
			} else if(size == 9){
				Mux = G;
			} else {
				ColumnGroup.add(G);
			}
		}
		
		table = new Pip [2][ColumnGroup.size()]; 
		rows = 2; 
		cols = ColumnGroup.size();
		
		Bit MuxBitLow = cloneBit(Mux.getBit());
		
		RowArray = new Bit[2];
		ColArray = new Bit[ColumnGroup.size()];
		
		RowArray[0] = MuxBitLow; 
		RowArray[1] = Mux.getBit();
		
		RowMap = new HashMap<Bit, Integer>();
		ColMap = new HashMap<Bit, Integer>();
		
		RowMap.put(MuxBitLow, 0);
		RowMap.put(Mux.getBit(), 1);
		
		MuxBitLow.setProperValue(0);
		
		for(Pip P: Buffer.getGroupPips()){
			P.setBufferBit(Buffer.getBit());
		    P.setRowBit(MuxBitLow);
		    
		}
		
		for(Pip P: Mux.getGroupPips()){
			P.setRowBit(Mux.getBit());
		}
		int i = 0; 
		for(Group G : ColumnGroup){
			ColArray[i] = G.getBit(); 
			ColMap.put(G.getBit(), i++);
			for(Pip P: G.getGroupPips()){
				P.setColumnBit(G.getBit());
			}
		}
		
		for(Pip P : Pips){
		   	
			table[P.getRowBit().getProperValue()][ColMap.get(P.getColumnBit())] = P; 
		}
	}
	
	private Bit cloneBit(Bit b) {
		
		if(b != null){
			Bit B = new Bit(b.getMinorAddress(), b.getOffset(), b.getMask());
			B.setProperValue(b.getProperValue());
			return B;
		}
		
		return null;
	}
	public Bit [] getRowBits(){
		return RowArray;
	}
	public Bit [] getColBits(){
		return ColArray; 
	}
	//Use this Function if the Row and Column Bits are found to be incorrect
	public void TransposeTable(){
		Pip [][] newTable = new Pip [cols][rows];
		
		//Transpose Table Entries
		for(int i=0; i<rows; i++){
			for(int j=0; j<cols; j++){
		        Pip P = table[i][j];
		        if(P!=null){
			        P.TransposePip(); 
					newTable[j][i] = P;
			    }
			}
		}
		table = newTable; 
		
		//Transpose the Table Dimensions
		int tmp = cols;
		cols = rows;
		rows = tmp; 
		
		//Swap the Row and Column Arrays
	    Bit [] tmpA = ColArray;
	    ColArray = RowArray; 
	    RowArray = tmpA; 
		
	    //Swap the Row and Column HashMaps
	    HashMap<Bit, Integer> tmpHM = ColMap;
	    ColMap = RowMap; 
	    RowMap = tmpHM; 
	}
	
	public int getRows() {return rows;}
	public int getCols() {return cols;}
	

	//Returns a Pip Containing the bit information along with start and end wire info
	public Pip getPip(int startWire){
		
		for(int row=0; row<rows; row++){
			for(int col=0; col<cols; col++){
				if(startWire == table[row][col].getPip().getStartWire()){
					return table[row][col]; 
				}
			}
		}
		
		return null;
	}
	
	public Pip getPip(String startWire){
		return getPip(we.getWireEnum(startWire));
	}
	
	public void SetTile(Tile T){
		this.T = T; 
		for(int i=0; i<rows; i++){
			for(int j=0; j<cols; j++){
				table[i][j].getPip().setTile(T);
			}
		}
	}
	//Returns a the Column Specified by the Given Bit
	public ArrayList<Pip> getColumn(Bit B){
				
		Integer col  = ColMap.get(B);
		if(col != null){	
			ArrayList<Pip> P = new ArrayList<Pip>();
			for(int row = 0; row < rows; row++){
				P.add(table[row][col]);
			}
			return P;
		}
		return null;
	}
	
	//Returns a Row Specified by a Given Bit
	public ArrayList<Pip> getRow(Bit B){
		
		Integer row  = RowMap.get(B);
		if(row != null){	
			ArrayList<Pip> P = new ArrayList<Pip>();
			for(int col = 0; col < cols; col++){
				P.add(table[row][col]);
			}
			return P;
		}
		return null;
	}
	
	
	public ArrayList<Pip> getRow(String startWire){
		return getRow(we.getWireEnum(startWire));
	}
	
	
	public int getRowNumber(int startWire){
		for(int row=0; row<rows; row++){
			for(int col=0; col<cols; col++){
				if(startWire == table[row][col].getPip().getStartWire()){
					return row;     				
				}
			}
		}
		
		return -1;
	}
	
	public int getColumnNumber(int startWire){
		for(int row=0; row<rows; row++){
			for(int col=0; col<cols; col++){
				if(startWire == table[row][col].getPip().getStartWire()){
					return col;     				
				}
			}
		}
		
		return -1;
	}
	
	public int getRowNumber(Bit B){
		
		for(int row=0; row<rows; row++){
			if(B.equals(RowArray[row])){
				return row;     				
			}
		}
        return -1;
	}
	
	public int getColumnNumber(Bit B){
		
			for(int col=0; col<cols; col++){
				if(B.equals(ColArray[col])){
					return col;     				
				}
			}
		
		
		return -1;
	}
	
	public ArrayList<Pip> getRow(int startWire){
		for(int row=0; row<rows; row++){
			for(int col=0; col<cols; col++){
				if(startWire == table[row][col].getPip().getStartWire()){
					return getRow(table[row][col].getRowBit());     				
				}
			}
		}
		
		return null;
	}
	
	public ArrayList<Pip> getColumn(String startWire){
		return getColumn(we.getWireEnum(startWire));
	}
	
	public ArrayList<Pip> getColumn(int startWire){
		for(int row=0; row<rows; row++){
			for(int col=0; col<cols; col++){
				if(startWire == table[row][col].getPip().getStartWire()){
					return getColumn(table[row][col].getColumnBit());     				
				}
			}
		}
		
		return null;
	}
	
	private Pip findPipInGroup(Group bitSet0Group, Bit bitSet1Bit) {
		for(Pip P: bitSet0Group.getGroupPips()){
			for(Bit B : P.getBits()){
				if(B.equals(bitSet1Bit)){
					return P; 
				}
			}
		}
		return null;
	}

	private Group findBitGroup(Bit bitSet0Bit, ArrayList<Group> Groups) {
		
		for(Group G : Groups){
			if(G.getBit().equals(bitSet0Bit)){
				return G; 
			}
		}
		
		return null; 
	}

	public void setDestWire(int destWire) {
		this.destWire = destWire;
	}

	public int getDestWire() {
		return destWire;
	}
	
	public Pip getPip(int row, int col){
		return table[row][col];
	}
	
	public void printTable(){
		
		String RightMostCellFormat = "%1$-7s";
		String CellFormat = "%1$-24s";
			
		//Print the Top Row
		System.out.print(String.format(RightMostCellFormat, we.getWireName(this.destWire)));
		for(int col=0; col<ColArray.length; col++){
			System.out.print(String.format(CellFormat, ColArray[col].toString()));
		}
		System.out.print("\n");
		
		//Print the Remaining Rows
		for(int row=0; row<RowArray.length; row++){
			System.out.print(String.format(RightMostCellFormat, RowArray[row].toString()));
			for(int col=0; col<cols; col++){
				if(table[row][col] != null){
				 System.out.print(String.format(CellFormat, we.getWireName(table[row][col].getPip().getStartWire())));
				}
			}
			System.out.print("\n");
		}
		
	}
}

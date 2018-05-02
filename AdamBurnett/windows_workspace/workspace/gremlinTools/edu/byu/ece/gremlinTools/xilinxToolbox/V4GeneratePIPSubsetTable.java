package edu.byu.ece.gremlinTools.xilinxToolbox;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;

import edu.byu.ece.gremlinTools.bitstreamDatabase.BitstreamDB;
import edu.byu.ece.rapidSmith.design.PIP;
import edu.byu.ece.rapidSmith.device.WireEnumerator;

public class V4GeneratePIPSubsetTable {
	HashSet<String> uniqueThreeBitPIPs;
	HashMap<String,String> pipSubsets;
	BitstreamDB db;
	ResultSet rs;
	
	public V4GeneratePIPSubsetTable(){
		uniqueThreeBitPIPs = new HashSet<String>();
		db = new BitstreamDB();
		pipSubsets = new HashMap<String,String>();
	}
	
	public void getUniqueThreeBitPIPs(){
		db.openConnection();
		rs = db.makeQuery("SELECT * FROM Virtex4Bits WHERE NumBits = 3");

		try {
			while(rs.next()){
				uniqueThreeBitPIPs.add(rs.getString(2) + " " + rs.getString(3) + " " + rs.getString(4));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void populateHashMap(){
		ResultSet rs2;
		HashSet<String> uniqueResults = new HashSet<String>();
		for(String s : uniqueThreeBitPIPs){
			String[] parts = s.split(" ");
			rs = db.makeQuery("SELECT * FROM Virtex4Bits WHERE Tile = \"clb\" AND Source = \"" + 
					parts[0] + "\" AND ConnType = \"" + parts[1] + "\" AND Destination = \"" +
					parts[2] + "\"");
			
			System.out.println("PIP: " + s);
			try {
				while(rs.next()){
					System.out.println("  " + rs.getString(1) + " " + rs.getString(2) + " " +  rs.getString(3)+ " " +
							rs.getString(4) + " " + rs.getInt(5) + " " + rs.getInt(6) + " " + rs.getInt(7) + " " + rs.getInt(8));
					rs2 = db.makeQuery("SELECT * FROM Virtex4Bits WHERE Tile = \"clb\" AND NumBits = 2 "+
							"AND MinorAddr = " + rs.getInt(6) + " AND  Offset = " + rs.getInt(7) + 
							" AND Mask = " + rs.getInt(8));
					while(rs2.next()){
						String result = rs2.getString(2) + " " +  rs2.getString(3)+ " " + rs2.getString(4); 
						if(uniqueResults.contains(result) && !s.equals(result)){
							System.out.println("Subset PIP found: " + result);
							pipSubsets.put(s, result);
						}
						else{
							uniqueResults.add(result);
						}
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			uniqueResults.clear();
		}
	}
	
	public void printStaticVariable(){
		System.out.println("/**");
		System.out.println(" * This variable was generated automatically by V4GeneratePIPSubsetTable.java");
		System.out.println(" */");
		System.out.println("private static HashMap<PIP,PIP> pipSubsets;"); //Used To Be String,String
		System.out.println("static{");
		//Added At RapidSmith Update By Josh 
		System.out.println("WireEnumerator W = new WireEnumerator();"); 
        System.out.println("PIP P1, P2;"); 
		System.out.println("    pipSubsets = new HashMap<PIP,PIP>();"); //Used To Be String,String
		
		for(String key : pipSubsets.keySet()){
			StringTokenizer KeyTok = new StringTokenizer(key);
			StringTokenizer ValueTok = new StringTokenizer(pipSubsets.get(key));
			String s0 = KeyTok.nextToken(); KeyTok.nextToken(); String s1 = KeyTok.nextToken();
			System.out.println("	P1 = new PIP(null, W.getWireEnum(\"" + s0 + "\"),W.getWireEnum(\"" + s1 + "\"));");
			s0 = ValueTok.nextToken(); ValueTok.nextToken(); s1 = ValueTok.nextToken();
			System.out.println("	P2 = new PIP(null, W.getWireEnum(\"" + s0 + "\"),W.getWireEnum(\"" + s1 + "\"));");
			System.out.println("    pipSubsets.put(P1,P2);");
		}
		System.out.println("}");
	}
	
	
	public static void main(String[] args){
		V4GeneratePIPSubsetTable v4Gen = new V4GeneratePIPSubsetTable();
		
		v4Gen.getUniqueThreeBitPIPs();

		v4Gen.populateHashMap();
		
		v4Gen.printStaticVariable();
	}
}

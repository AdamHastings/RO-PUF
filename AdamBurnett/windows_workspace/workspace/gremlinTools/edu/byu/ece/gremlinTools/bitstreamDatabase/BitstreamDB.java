package edu.byu.ece.gremlinTools.bitstreamDatabase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import edu.byu.ece.rapidSmith.design.PIP;
import edu.byu.ece.rapidSmith.device.WireEnumerator;

/**
 * This class encapsulates the MySQL database which stores bitstream XDL mapping information.  It
 * handles all of the details of connecting to the database and stores the parameters in order to
 * connect to it.
 * @author Chris Lavin
 * Created on: Jul 17, 2009
 */
public class BitstreamDB {
	/** The connection to the MySQL database */
	private Connection conn;
	/** Name of the host where the database resides */
	private String hostName;
	/** Name of the database to connect to */
	private String databaseName;
	/** MySQL user name for the database */
	private String userName;
	/** MySQL user's password */
	private String password;
	
	/**
	 * Constructor that connects to raptor.ee.byu.edu as the default database.
	 * You must have the root password set as the value for the environment variable
	 * GREMLIN_PASSWORD.
	 */
	public BitstreamDB(){
		conn = null;
		hostName = "localhost"; //raptor.ee.byu.edu
		databaseName = "TAGdb";
		userName = "root";
		password = System.getenv("GREMLIN_PASSWORD");
		if(password == null){
			System.out.println("Your GREMLIN_PASSWORD environment variable is not set.  " +
					"Set it to the correct database password.");
			System.exit(1);
		}
	}
	
	/**
	 * Constructor for using an alternate database
	 * @param hostName The full path of the host name ("raptor.ee.byu.edu")
	 * @param databaseName Name of the database
	 * @param userName User name
	 * @param password Password for the database user
	 */
	public BitstreamDB(String hostName, String databaseName, String userName, String password){
		conn = null;
		this.hostName = hostName;
		this.databaseName = databaseName;
		this.userName = userName;
		this.password = password;
	}
	
	/**
	 * Opens connection to MySQL database
	 * @return True if operation was successful, false otherwise.
	 */
	public boolean openConnection(){
		try{
			this.conn = DriverManager.getConnection("jdbc:mysql://"+this.hostName+
					"/"+this.databaseName+"?user="+this.userName+"&password="+this.password);
		}
		catch (SQLException e){
			printSQLExceptionMessages(e);
			System.out.println("Connection failed to " + this.hostName + ", check your settings.");
			return false;
		}
		return true;
	}

	/**
	 * Closes connection to MySQL database
	 * @return True if operation was successful, false otherwise.
	 */
	public boolean closeConnection(){
		try{
			this.conn.close();
		}
		catch (SQLException e){
			printSQLExceptionMessages(e);
			System.out.println("Error closing connection to " + this.hostName + ", check your settings.");
			return false;
		}
		return true;		
	}
	
	/**
	 * 
	 * @return The ResultSet from executing the query, null if there was a problem.
	 */
	public ResultSet makeQuery(String query){
		Statement stmt = null;
		ResultSet result = null;
		//System.out.println(query);
		try{
			stmt = conn.createStatement();
			stmt.executeQuery(query);
			result = stmt.getResultSet();
			
		}
		catch (SQLException e){
			System.out.println("Error in making query: \"" + query + "\"");
			printSQLExceptionMessages(e);
			return null;
		}

		return result;
	}
	/*   Generalized query able to of variable 
    *   length must use MSQL syntax in the query
    */
    public ResultSet makeLogicQuery(String query)
    { String q = "SELECT * FROM Virtex5LogicBits WHERE "+query;
    	return makeQuery(q);
    }
	/**
	 * Makes a specific query to the database looking for logic bits with minorAddr, byteOffset and mask
	 * in the device family with deviceFamilyName.
	 * @param deviceFamilyName The name of the device family, used to determine which table to look up.
	 * @param minorAddr The minor address of the entry of interest.
	 * @param byteOffset The byte offset of the entry of interest.
	 * @param mask The mask of the entry of interest
	 * @return The ResultSet of the query (any matches in the corresponding table).
	 */
	public ResultSet makeLogicQuery(String deviceFamilyName, int minorAddr, int byteOffset, int mask){
		String query = "SELECT * FROM "+deviceFamilyName+"LogicBits WHERE " +
		"MinorAddr = \"" + minorAddr + "\" AND " +
		"Offset = \"" + byteOffset + "\" AND " + 
		"Mask = \"" + mask + "\"";
		return makeQuery(query);
	}
	
	/**
	 * Makes a specific query to the database looking for routing bits with minorAddr, byteOffset and mask
	 * in the device family with deviceFamilyName.
	 * @param deviceFamilyName The name of the device family, used to determine which table to look up.
	 * @param minorAddr The minor address of the entry of interest.
	 * @param intType Type of routing of interest.
	 * @param byteOffset The byte offset of the entry of interest.
	 * @param mask The mask of the entry of interest
	 * @return The ResultSet of the query (any matches in the corresponding table).
	 */
	public ResultSet makeRoutingQuery(String deviceFamilyName, String intType, int minorAddr, int byteOffset, int mask){
		String query = "SELECT * FROM "+deviceFamilyName+"Bits WHERE " +
		"Tile IN (" + intType + ") AND " +
		"MinorAddr = \"" + minorAddr + "\" AND " +
		"Offset = \"" + byteOffset + "\" AND " + 
		"Mask = \"" + mask + "\"";
		return makeQuery(query);
	}
	
	public ResultSet makeRoutingPIPMatchQuery(String deviceFamilyName, String intType, PIP P, WireEnumerator We){
		String query;
		
		String wire0 = We.getWireName(P.getStartWire());
		String wire1 = We.getWireName(P.getEndWire());
		if(intType.contains(" ")){
			query = "SELECT * FROM "+deviceFamilyName+"Bits WHERE " +
			"Tile IN (" + intType + ") AND " +
			"Source = \"" + wire0 + "\" AND " +			
			"Destination = \"" + wire1 + "\"";			
		}
		else{
			query = "SELECT * FROM "+deviceFamilyName+"Bits WHERE " +
			"Tile = \"" + intType + "\" AND " +
			"Source = \"" + wire0 + "\" AND " + 
			"Destination = \"" + wire1 + "\"";
		}
		
		return makeQuery(query);
	}
	
	/**
	 * @return the conn
	 */
	public Connection getConn() {
		return conn;
	}

	/**
	 * @param conn the conn to set
	 */
	public void setConn(Connection conn) {
		this.conn = conn;
	}

	/**
	 * @return the hostName
	 */
	public String getHostName() {
		return hostName;
	}

	/**
	 * @param hostName the hostName to set
	 */
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	/**
	 * @return the databaseName
	 */
	public String getDatabaseName() {
		return databaseName;
	}

	/**
	 * @param databaseName the databaseName to set
	 */
	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * @param userName the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	
	
	public void printSQLExceptionMessages(SQLException e){
		System.out.println("SQLException: " + e.getMessage());
		System.out.println("SQLState: " + e.getSQLState());
		System.out.println("VendorError: " + e.getErrorCode());
	}

	/**
	 * Determines if there is a PIP entry in the database for the deviceFamilyName.
	 * @param pip The pip to check for
	 * @param deviceFamilyName The Family device name (Virtex 4, Virtex 5 ...)
	 * @return true if the PIP is in the database, false otherwise
	 */
	public boolean containsPIP(PIP pip, String deviceFamilyName){
		WireEnumerator W = new WireEnumerator();
	
		String query = "SELECT * FROM "+deviceFamilyName+"Bits WHERE " +
		"Source = \"" + W.getWireName(pip.getStartWire()) + "\" AND " +
		"Destination = \"" + W.getWireName(pip.getEndWire()) + "\"";
		ResultSet rs = makeQuery(query);
		try {
			return rs.next();
		} catch (SQLException e) {
			return false;
		}
	}
}

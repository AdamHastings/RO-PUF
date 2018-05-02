package edu.byu.ece.gremlinTools.ReliabilityTools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import edu.byu.ece.rapidSmith.design.Instance;
import edu.byu.ece.rapidSmith.design.Net;

public class Component {
   private String ComponentName; 
   private String LatexName; 
   private ArrayList<String> SearchTerms; 
   
   private HashSet<Net> Nets; 
   private HashSet<Instance> Instances; 
   
   private ArrayList<InterconnectFailure> InterconnectFailures; 
   private ArrayList<LogicFailure> LogicFailures; 
   
   private HashSet<Net> NetsWith1Failure;
   
   private int bitArea; 
   private int interconnectArea; 
   private int logicArea; 
   private int primaryBitArea; 
   
   public Component(String Name){
	   
	   ComponentName = Name; 
	   LatexName = "";
	   SearchTerms = new ArrayList<String>();
	   Nets = new HashSet<Net>();
	   Instances = new HashSet<Instance>();
	   
	   InterconnectFailures = new ArrayList<InterconnectFailure>(); 
	   LogicFailures = new ArrayList<LogicFailure>();
	   NetsWith1Failure = new HashSet<Net>();
	   SearchTerms.add(ComponentName);
	   bitArea = 0; 
   }
   
   public void setLatexName(String lname){
	   LatexName =lname; 
   }
   
   public String getLatexName(){
	   return LatexName; 
   }
   
   public float getMetric(){
	   return ((float)getNumberOfSens())/getBitArea(); 
   }
   
   public void addSearchTerm(String st){
	   SearchTerms.add(st);
   }
   
   public boolean NetBelongsToMe(Net N){
		  for(String S:SearchTerms){
		    if(N.getName().contains(S)){
		    	Nets.add(N);
		    	return true; 
		    }
		    
		    if(N.getName().contains("GLOBAL_LOGIC")){
		    	if(N.getPins().get(1).getInstanceName().contains(S)){
		    		Nets.add(N);
		    		return true;
		    	}
		    }
		  }
		  return false; 
   }
      
   public boolean InstanceBelongsToMe(Instance I){
	  for(String S:SearchTerms){
	    if(I.getName().contains(S)){
	    	Instances.add(I);
	    	return true; 
	    }
	  }
	  return false; 
   }
   
   public boolean LogicFailureBelongsToMe(LogicFailure L){
	   if(Instances.contains(L.getInstance())){
		   LogicFailures.add(L);
		   return true;
	   }
	   
	   return false; 
   }
   
   public boolean InterconnectFailureBelongsToMe(InterconnectFailure I){
	   if(Nets.contains(I.getNet())){
		   InterconnectFailures.add(I);
		   NetsWith1Failure.add(I.getNet());
		   return true;
	   }
	   
	   return false; 
   }
   
   public HashSet<Instance> getInstances(){
	   return Instances; 
   }
   
   public HashSet<Net> getNets(){
	   return Nets; 
   }
   public ArrayList<InterconnectFailure> getInterconnectFailures(){
	   return InterconnectFailures;
   }
   
   public ArrayList<LogicFailure> getLogicFailures(){
	   return LogicFailures; 
   }
   
   public void printReport(){
	   System.out.println(ComponentName + " Instances: " + Instances.size() + " Nets: " + Nets.size() + " Total Bit Area: " + bitArea);
   }
   
   public void setBitArea(int ba){
	   bitArea=ba; 
   }
   
   public void setInterconnectArea(int ia){
	   interconnectArea = ia; 
   }
   
   public void setLogicArea(int la){
	   logicArea = la; 
   }
   
   public void setPrimaryBitArea(int pba){
	   primaryBitArea = pba;
   }
   public int getBitArea(){
	   return bitArea; 
   }
   
   public int getPrimaryBitArea(){
	   return primaryBitArea;
   }
   public String getName(){
	   return ComponentName; 
   }
   
   public int getNumberOfNetsWith1Failure(){
	   return NetsWith1Failure.size(); 
   }
   
   public int getNumberOfNets(){
	   return Nets.size();
   }
   public int getNumberOfSens(){
	   return LogicFailures.size() + InterconnectFailures.size(); 
   }
}

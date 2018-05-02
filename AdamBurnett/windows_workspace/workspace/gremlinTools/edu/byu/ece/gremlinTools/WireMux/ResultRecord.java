package edu.byu.ece.gremlinTools.WireMux;

public class ResultRecord {

		private String wirename;
		private boolean doubleVerified; 
		private float transposedMeasurement;
		private float nonTransposedMeasurement;
		private int rows;
		private int cols; 
        private boolean solved; //Have we tested this matrix
		private boolean transposed; //Should this WireMux be transposed after being read from data base
		private boolean valid; 
		
		public ResultRecord(String wire){
			wirename = wire; 
		    transposedMeasurement = -1;
		    nonTransposedMeasurement = -1; 
		    rows = -1;
		    cols = -1;
		    transposed = false; 
		    solved = false; 
		    doubleVerified = false; 
		    setValid(false); 
		}
		public String toCSV(){
			return wirename + "," + transposed; 
		}
		public String toString(){
			return "Wire: " + wirename + " Double?: " + doubleVerified + " Trans?: " + transposed +  " TMeas:" + transposedMeasurement + " Meas: " +  nonTransposedMeasurement;  
		}
		public void evaluate(){
		  if(doubleVerified){	
			if(transposedMeasurement > .2 && nonTransposedMeasurement > .2 ||
			   transposedMeasurement < .2 && nonTransposedMeasurement < .2	){
			   setValid(false);	
			} else {
			   setSolved(true);
			   setValid(true);
			   
			   if(transposedMeasurement > .2 && nonTransposedMeasurement < .2){
				   transposed = true;
			   } else
				   transposed = false; 
			   
			}
		  } else {
			  if(nonTransposedMeasurement > .2){
				  setSolved(true);
			  } else if(transposedMeasurement > .2){
				  setTransposed(true);
				  setSolved(true);
			  }
		  }
		}

   	    public void setDoubleVerified(boolean doubleVerified) {
			this.doubleVerified = doubleVerified;
		}

		public boolean isDoubleVerified() {
			return doubleVerified;
		}


		public void setTransposedMeasurement(float transposedMeasurement) {
		  if(this.getTransposedMeasurement() == -1)
			this.transposedMeasurement = transposedMeasurement;
		  else {
			  System.out.println("SetTransposedMeasurement: Attempted to Change after being set" );
			  System.exit(1);
		  }
   	   }

		public float getTransposedMeasurement() {
			return transposedMeasurement;
		}

		public void setNonTransposedMeasurement(float nonTransposedMeasurement) {
			if(this.getNonTransposedMeasurement() == -1)
			 this.nonTransposedMeasurement = nonTransposedMeasurement;
			else {
				  System.out.println("SetNonTransposedMeasurement: Attempted to Change after being set" );
				  System.exit(1);
			  }
		}

		public float getNonTransposedMeasurement() {
			return nonTransposedMeasurement;
		}

		public void setRows(int rows) {
			this.rows = rows;
		}

		public int getRows() {
			return rows;
		}

		public void setCols(int cols) {
			this.cols = cols;
		}

		public int getCols() {
			return cols;
		}

		public void setSolved(boolean solved) {
			this.solved = solved;
		}

		public boolean isSolved() {
			return solved;
		}

		public void setTransposed(boolean transposed) {
			this.transposed = transposed;
		}

		public boolean isTransposed() {
			return transposed;
		}

    	public void setValid(boolean valid) {
			this.valid = valid;
		}

		public boolean isValid() {
			return valid;
		}
	
	
}

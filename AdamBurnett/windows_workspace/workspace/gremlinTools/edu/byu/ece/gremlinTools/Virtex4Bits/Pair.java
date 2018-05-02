package edu.byu.ece.gremlinTools.Virtex4Bits;

public class Pair<A, B> {
   @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((Left == null) ? 0 : Left.hashCode());
		result = prime * result + ((Right == null) ? 0 : Right.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Pair other = (Pair) obj;
		if (Left == null) {
			if (other.Left != null)
				return false;
		} else if (!Left.equals(other.Left))
			return false;
		if (Right == null) {
			if (other.Right != null)
				return false;
		} else if (!Right.equals(other.Right))
			return false;
		return true;
	}

	public A Left; 
	public B Right; 
   
   public Pair(A left, B right){
	   setLeft(left); 
	   setRight(right); 
   }
   
   public A getLeft() {
	   return Left;
   }
   public B getRight(){
	   return Right; 
   }
   
   public void setLeft(A left){
	   Left = left;
   }
   
   public void setRight(B right){
	   Right = right; 
   }
   
   public String toString(){
	   return Left.toString() + "\n" + Right.toString();
   }
}

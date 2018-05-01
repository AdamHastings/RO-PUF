
import java.util.Comparator;

import edu.byu.ece.rapidSmith.design.Instance;


public class instanceCompare implements Comparator<Instance>{
	@Override
	public int compare(Instance arg0, Instance arg1) {
		int num0 = Integer.parseInt(arg0.getName().replaceAll("[\\D]",  ""));
		int num1 = Integer.parseInt(arg1.getName().replaceAll("[\\D]",  ""));
		
		if (num0 < num1)
		{
			return -1;
		}
		else if (num0 > num1)
		{
			return 1;
		}
		else
		{
			return 0;
		}
	}
}

import java.util.Comparator;

import edu.byu.ece.rapidSmith.design.Net;


public class netCompare implements Comparator<Net>{

	@Override
	public int compare(Net net1, Net net2) {
		String type1 = net1.getName().split("/")[4];
		String name1 = net1.getName().split("/")[3];
		String name2 = net2.getName().split("/")[3];
		
		System.out.println("name1 " + name1);
		System.out.println("name2 " + name2);
		
		int number1 = Integer.parseInt(name1.replaceAll("[^\\d.]", ""));
		int number2 = Integer.parseInt(name2.replaceAll("[^\\d.]", ""));
		
		if (number1 < number2)
		{
			return 1;
		}
		else if (number1 > number2)
		{
			return -1;
		}
		else
		{
			//They are equal, check last character for G or Y
			if (type1.equals("G"))
			{
				return -1;
			}
			else
			{
				return 1;
			}
		}
	}

}

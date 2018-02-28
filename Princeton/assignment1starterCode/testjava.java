/** Mainly for refreshing memory on Java programming **/
import java.util.*; 

public class testjava {

	int keyInt;
	String keyString;
	byte[] keyArray;
	ArrayList keyArrayList;

	/* Main Contructor */
	public testjava()
	{
		keyInt = 0;
		keyString = "Hello";

	}

	/* Testing Contructor #1 */
	public testjava(int x, String y)
	{
		this.keyInt = x;
		this.keyString = y;
	}

	/* Testing Constructor #2 */
	public testjava(byte[] bytes)
	{
		keyArray = bytes;
	}

	/* Testing Constructor #3 */
	public testjava(ArrayList<testjava> testArrayObject)
	{
		keyArrayList = testArrayObject;
	}

	public static int testMath(int x)
	{
		int y = 10;
		int sum = x + y;

		System.out.println(sum);

		return sum;
	}

	public int changeVariable(int x)
	{
		keyInt = x;
		return keyInt;
	}



	public static void main(String[] args) {
		
		testjava testClass = new testjava(10, "Hello");

		System.out.println(testClass.keyString + " " + testClass.keyInt);
	}


}


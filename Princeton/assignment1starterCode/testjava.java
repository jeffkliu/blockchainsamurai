/** Mainly for refreshing memory on Java programming **/
import java.util.*; 
/*import org.apache.commons.lang3.*;*/
import java.security.*;

public class testjava {

	int keyInt;
	String keyString;
	byte[] keyArray;
	ArrayList<testjava> keyArrayList;

	/* Main Contructor */
	public testjava()
	{
		this.keyInt = 0;
		this.keyString = "Hello";
		this.keyArray = new byte [10];
		this.keyArrayList = new ArrayList<testjava>();

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

		/*System.out.println(System.getProperty("java.class.path"));*/
		testjava testClass = new testjava(10, "Hello");
		/*byte[] byteList = RandomUtils.nextBytes(20);


		System.out.println(Arrays.toString(byteList)); */
		
	}


}


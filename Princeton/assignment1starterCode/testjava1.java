/** Mainly for refreshing memory on Java programming specifically to make calls 
into other java files **/


public class testjava1 {

	private int keyInt;
	public testjava testUnit;

	public static int testMath1(int x)
	{
		int y = 10;
		int sum = x + y;

		return sum;
	}


	public static int printprevfile()
	{
		testjava num1 = new testjava();

		int finalsum = num1.testMath(10); 

		return finalsum;
	}

	public int callInternalKey()
	{
		testUnit = new testjava();
		testUnit.changeVariable(80);
		return  testUnit.keyInt;
	}

	public static void main(String[] args) {
		
		testjava1 newclass1 = new testjava1();
		System.out.println(newclass1.testMath1(100) + newclass1.printprevfile());
		testjava test1 = new testjava();
		test1.keyInt = 4000;
		System.out.println(newclass1.callInternalKey());
		System.out.println(test1.keyInt);
		/*newclass.testMath(10);*/
	}


}
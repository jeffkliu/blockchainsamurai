import java.util.*;
import java.util.stream.Collectors;

/* Just using this class to testCode */

public class testcode{

	public class Person{

		int age;
		String name;
		int hotness;

		public Person(int age, String name, int hotness)
		{
			this.age = age;
			this.name = name;
			this.hotness = hotness;
		}

		public int getAge()
		{
			return age;
		}


	}

	public testcode(){
		//nothing to do here
	}

	public static void main(String[] args)
	{
		try
	    {
	        testcode obj = new testcode();
	        obj.run(args);
	    }
	    catch (Exception e)
	    {
	        e.printStackTrace ();
	    }

	}

	public void run (String[] args) throws Exception
	{
		List<String> strings = Arrays.asList("abc", "", "bc", "efg", "abcd","", "jkl");
		//List<String> filtered = strings.stream().filter(string -> !string.isEmpty()).collect(Collectors.toList());

		List<Integer> numbers = Arrays.asList(3, 2, 2, 3, 7, 3, 5);

		//get list of unique squares
		List<Integer> squaresList = numbers.stream().map( i -> i*i).collect(Collectors.toList());

		System.out.println(squaresList);

		Arrays.stream(new int[] {1, 2, 3})
		    .map(n -> 2 * n + 1)
		    .average()
		    .ifPresent(System.out::println); 

	    List<Person> personList = Arrays.asList(
			new Person(10, "Kim", 10),
			new Person(10, "Jeff", 1),
			new Person(10, "George", 10),
			new Person(10, "Shirakiri", 2),
			new Person(30, "Hibiku", 1),
			new Person(30, "Hibiki", 1));

	    List<Person> filtered = personList.stream().filter(p -> (p.age == 20)).collect(Collectors.toList());
	    Map<Integer, List<Person>> personbyAge = personList.stream().collect(Collectors.groupingBy(p -> p.age));
	    personbyAge.forEach((age,p) -> System.out.format("age: %s: %s\n", age, p));

	    String ternaryOperator = personList.stream()
	    	.max((a,b) -> a.age > b.age ? 1 : -1 ).get().name;

	    System.out.println("This is person: " + ternaryOperator);

	    Map<Integer, String> nameMap = personList.stream()
	    	.collect(Collectors.toMap(
	    		p -> p.age,
	    		p -> p. name,
	    		(name1, name2) -> name1 + ";" + name2));


	    System.out.println(nameMap);
	    System.out.println(filtered.stream().map(p -> p.name).collect(Collectors.toSet()));
	}
}
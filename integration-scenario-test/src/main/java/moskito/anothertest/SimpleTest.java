package moskito.anothertest;

/**
 * TODO comment this class
 *
 * @author lrosenberg
 * @since 22.04.13 11:48
 */
public class SimpleTest {

	public static void main(String a[]){
		System.out.println("A");
		new SimpleTest().echo(10);
		System.out.println("B");
	}


	public long echo(long a){
		System.out.println("echo "+a);
		return a;
	}
}

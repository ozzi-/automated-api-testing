package helpers;

public class VerbosePrinter {
	public static boolean on = false;
	
	public static void output(String str) {
		if(on) {
			System.out.println("* * "+str);
		}
	}
}

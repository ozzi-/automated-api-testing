package helpers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Scanner;

import main.TestCase;
import main.TestResult;

public class Helpers {
	
	public static String convertStreamToString(java.io.InputStream is) {
		if (is == null) {
			return "";
		}
		java.util.Scanner s = new java.util.Scanner(is);
		s.useDelimiter("\\A");
		String streamString = s.hasNext() ? s.next() : "";
		s.close();
		return streamString;
	}
	
	public static void writeResult(TestCase testCase, TestResult res, String resString) {
		String dir = TestCase.class.getClass().getResource("/").getFile();
		dir=dir.replace("target/classes/", "results/");
		OutputStream os;
		try {
			String tCNFS = testCase.getName().replaceAll("[^a-zA-Z0-9]+","");
			os = new FileOutputStream(dir + tCNFS +".txt");
		    final PrintStream printStream = new PrintStream(os);
			printStream.println("Method - Call:");
			printStream.println(testCase.getMethod()+" "+testCase.getCall());
			printStream.println("");
			printStream.println("Body:");
			printStream.println(testCase.getBody());
			printStream.println("");
			printStream.println("Result:");
			printStream.println(resString);
			printStream.println("");
			printStream.println("Result Body:");
			printStream.println(res.getBody());
		    printStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static String getFile(String fileName) {
		StringBuilder result = new StringBuilder("");
		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();
		File file = new File(classLoader.getResource(fileName).getFile());
		try (Scanner scanner = new Scanner(file)) {
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				result.append(line).append("\n");
			}
			scanner.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result.toString();
	}
}

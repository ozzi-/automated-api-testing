package helpers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import main.TestCase;
import main.TestResult;

public class Helpers {

	private Helpers() {
		throw new IllegalStateException("Utility class");
	}

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
	
	public static String getBasePath(String path) {
		Path p = Paths.get(path);
		Path folder = p.getParent();
		String basePath = folder.toString()+File.separator;
		return basePath;
	}

	// TODO provide param where to log
	public static void writeTestCaseResult(TestCase testCase, TestResult res, String resString) {
		String dir = TestCase.class.getClass().getResource("/").getFile();
		dir = dir.replace("target/classes/", "results/");
		OutputStream os = null;
		String tCNFS = testCase.getName().replaceAll("[^a-zA-Z0-9]+", "");

		try (PrintStream printStream = new PrintStream(os);) {
			os = new FileOutputStream(dir + tCNFS + ".txt");
			printStream.println("Method - Call:");
			printStream.println(testCase.getMethod() + " " + testCase.getCall());
			printStream.println("");
			printStream.println("Body:");
			printStream.println(testCase.getBody());
			printStream.println("");
			printStream.println("Result:");
			printStream.println(resString);
			printStream.println("");
			printStream.println("Result Body:");
			printStream.println(res.getBody());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static String readFileToString(String fileName) throws IOException {
		StringBuilder stringBuilder = new StringBuilder();
		try (Stream<String> stream = Files.lines(Paths.get(fileName), StandardCharsets.UTF_8)) {
			stream.forEach(s -> stringBuilder.append(s).append("\n"));
		} catch (IOException e) {
			throw e;
		}
		return stringBuilder.toString();
	}
}

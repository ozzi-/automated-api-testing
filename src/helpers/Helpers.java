package helpers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import main.TestCase;
import main.TestResult;

public class Helpers {

	private Helpers() {
		throw new IllegalStateException("Utility class");
	}
	
	public static String getBasePath(String path) {
		Path p = Paths.get(path);
		Path folder = p.getParent();
		String basePath = folder.toString()+File.separator;
		return basePath;
	}

	public static void writeTestCaseResult(String logFilePath, TestCase testCase, TestResult res, String resString) {
		String tCNFS = testCase.getName().replaceAll("[^a-zA-Z0-9]+", "");
		String toPath = (logFilePath.endsWith(File.separator)?logFilePath:logFilePath+File.separator) + tCNFS + ".txt";
		try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(toPath), StandardCharsets.UTF_8)){
			writer.write("Method - Call:\r\n");
			writer.write(testCase.getMethod() + " " + testCase.getCall());
			writer.write("\r\n\r\n");
			writer.write("Body:\r\n");
			writer.write(testCase.getBody()==null?"null":testCase.getBody());
			writer.write("\r\n\r\n");
			writer.write("Result:\r\n");
			writer.write(resString);
			writer.write("\r\n\r\n");
			writer.write("Result Body:\r\n");
			writer.write(res.getBody());
		} catch (Exception e) {
			e.printStackTrace();
		}
		VerbosePrinter.output("Written Test Result to "+toPath);
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
	
	/**
	 * inserts an element at the specific index into the array
	 * @param index
	 * @param val
	 * @param array
	 * @return array with element inserted at index
	 */
	public static JsonArray JSONArrayInsert(int index, JsonElement val, JsonArray array) {
	    JsonArray newArray = new JsonArray();
	    for (int i = 0; i < index; i++) {
	        newArray.add(array.get(i));
	    }
	    newArray.add(val);
	    for (int i = index; i < array.size(); i++) {
	        newArray.add(array.get(i));
	    }
	    return newArray;
	}

}

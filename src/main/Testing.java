package main;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.google.gson.JsonElement;
import helpers.Helpers;
import helpers.TestCaseHelpers;
import helpers.Variables;
import helpers.VerbosePrinter;
import model.Settings;
import nw.ProxyConfig;

public class Testing {
    static DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	public static void main(String[] args) throws ParseException {
		
		parseCLIArgs(args);
		Date date = new Date();
		System.out.println(dateFormat.format(date)+" loading tests");

		JsonElement testsJSON = TestCaseHelpers.loadTestJSONFile(Settings.testFilePath);
		Variables.loadVariables(testsJSON);
		ProxyConfig.loadProxy(testsJSON);
		// OK
		ArrayList<TestCase> testCases = TestCaseHelpers.loadTestCases(testsJSON,Variables.globalVariables, Helpers.getBasePath(Settings.testFilePath));

		runTests(testCases);
	}

	private static void runTests(ArrayList<TestCase> testCases) {
		
		for (TestCase testCase : testCases) {
			Date date = new Date();
			System.out.println(dateFormat.format(date)+" running test '"+testCase.getName()+"'");

			long exStart = System.nanoTime();
			testCase.injectVariables(testCase.getCustomVars(),false); 
			testCase.injectVariables(Variables.globalVariables,true);
			TestResult res = testCase.runTest();
			long exEnd = System.nanoTime();
			long exTot = (exEnd-exStart)/1000/1000;
			
			String resString = printTestResult(dateFormat, testCase, res, exTot);
			// TODO improve \/
			Variables.resolveTestCaseCustomVariables(testCases, testCase, res);
			if(Settings.logFilePath!=null) {
				Helpers.writeTestCaseResult(Settings.logFilePath, testCase, res, resString);
			}
		}
	}

	private static String printTestResult(DateFormat dateFormat, TestCase testCase, TestResult res, long exTot) {
		Date date = new Date();
		String resString = res.succeeded()?"Success":"Failure: "+res.getFailReason();
		resString = dateFormat.format(date)+" - "+resString+" - "+testCase.getName()+" - "+exTot+" ms";
		System.out.println(resString);
		return resString;
	}

	private static void parseCLIArgs(String[] args) throws ParseException {
		Options options = new Options();
		options.addRequiredOption("t", "testfile", true, "Path to the test file");
		options.addOption("r", "resultfile", true, "Path to result file");
		options.addOption("v", "verbose", false, "Verbose Mode");

		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = parser.parse(options, args);
		
		Settings.testFilePath = cmd.getOptionValue("t");
		Settings.logFilePath = cmd.getOptionValue("r");
		VerbosePrinter.on = cmd.hasOption("v");
	}
}

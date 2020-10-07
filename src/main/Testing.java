package main;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.google.gson.JsonElement;
import helpers.Helpers;
import helpers.TestCaseH;
import helpers.Variables;
import nw.ProxyConfig;

public class Testing {
	public static void main(String[] args) {
		JsonElement testsJSON = TestCaseH.loadTestJSON();
		Variables.loadVariables(testsJSON);
		ArrayList<TestCase> testCases = TestCaseH.loadTestCases(testsJSON,Variables.variables);
		ProxyConfig.loadProxy(testsJSON);   
	    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	    
		for (TestCase testCase : testCases) {
			long exStart = System.nanoTime();
			// Start
			testCase.injectVariables(testCase.getCustomVars(),true); 
			testCase.injectVariables(Variables.variables,false);
			TestResult res = testCase.runTest();
			// End
			long exEnd = System.nanoTime();
			long exTot = (exEnd-exStart)/1000/1000;
			Date date = new Date();
			
			String resString = res.succeeded()?"Success":"Failure: "+res.getFailReason();
			resString = dateFormat.format(date)+" - "+resString+" - "+testCase.getName()+" - "+exTot+" ms";
			System.out.println(resString);
			
			Variables.resolveTestCaseCustomVariables(Variables.variables, testCases, testCase, res);
	        Helpers.writeTestCaseResult(testCase, res, resString);
		}
		System.out.println("\nDone");
	}
}

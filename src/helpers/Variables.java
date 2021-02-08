package helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import main.TestCase;
import model.TestResult;

public class Variables {
	
	public static Map<String,String> globalVariables = new HashMap<String,String>();

	/**
	 * Load variables into variable map
	 * @param testsJSON
	 */
	public static void loadVariables(JsonElement testsJSON) {
	    JsonObject  jobject = testsJSON.getAsJsonObject();
	    if(jobject.get(Keywords.VARS)==null){
	    	VerbosePrinter.output("No 'variables' array defined in test json");
	    }else {
	    	JsonArray variablesJSON = jobject.get(Keywords.VARS).getAsJsonArray();
	    	for (JsonElement variableElement : variablesJSON) {
				Set<String> keys = variableElement.getAsJsonObject().keySet();
				for (String key : keys) {
					String value = variableElement.getAsJsonObject().get(key).getAsString();
		    		globalVariables.put(key, value);
		    		VerbosePrinter.output("Loaded variable '"+key+"'='"+value+"'");
				}
			}    	
	    }
	}
	
	/**
	 * Load variables defined in a testcase json structure
	 * @param testCaseJSON
	 * @param name 
	 * @return variables map
	 */
	public static Map<String, String> getValueVariables(JsonObject testCaseJSON, String name, String keyword) {
		Map<String, String> variables = new HashMap<String,String>();
		
		JsonElement je = testCaseJSON.get(keyword);
		if (je instanceof JsonArray) {
		    JsonArray ar = (JsonArray)je;
		    for (JsonElement jsonElement : ar) {
				String key = jsonElement.getAsJsonObject().keySet().toArray()[0].toString();
				String value = jsonElement.getAsJsonObject().get(key).getAsString();
				variables.put(key, value);
	    		VerbosePrinter.output("Loaded "+keyword+" '"+key+"'='"+value+"' in test case '"+name+"'");
			}
		}
		return variables;
	}

	public static String injectVariablesIntoString(Map<String, String> variables, String stringToBeInjected, boolean checkExistence) { 
		Pattern pattern = Pattern.compile("%%<[^>%]+>%%");
		List<String> list = new ArrayList<String>();
		Matcher m = pattern.matcher(stringToBeInjected);
		while (m.find()) {
		    list.add(m.group().replace("%%<", "").replace(">%%", ""));
		}
		for (String match : list) {
			String value = variables.get(match);
			if(value!=null){
				stringToBeInjected=stringToBeInjected.replace("%%<"+match+">%%",value);				
			}else if(checkExistence){
				System.err.println("Failure injecting variable '"+match+"' as not found");
			}
		}
		return stringToBeInjected;
	}
	
	/**
	 * Resolves all custom variables from a testcase into the main variables map, this includes static variables and dynamic regexp ones that get checked on the response body.
	 * @param variables
	 * @param testCases
	 * @param testCase
	 * @param res
	 */
	public static void resolveTestCaseCustomVariables(ArrayList<TestCase> testCases, TestCase testCase, TestResult res) {

		Map<String, String> tcVars = testCase.getCustomVars();
		// add testcase variables into global variables now
		Variables.globalVariables.putAll(tcVars);
		
		boolean setVar=false;
		boolean extracted=false;
		
		Map<String, String> tcBodyVars = testCase.getExtractBodyVars();
		Map<String, String> tcHeaderVars = testCase.getExtractHeaderVars();
		
		for (Map.Entry<String, String> entry : tcBodyVars.entrySet()){
			Pattern pattern = Pattern.compile(entry.getValue());
			Matcher m = pattern.matcher(res.getBody());
			while (m.find()) {
				Variables.globalVariables.put(entry.getKey(), m.group(1)); 
				VerbosePrinter.output("Extracted Body Variable by Regex "+entry.getValue()+" = "+m.group(1));
			    setVar=true;
			    extracted=true;
			}
			if(!extracted) {
				System.err.println("Could not extract body variable by applying regex '"+entry.getValue()+"'");
			}
		}
		extracted=false;
		
		Map<String, List<String>> headers = res.getHeaders();
		for (Map.Entry<String, String> entry : tcHeaderVars.entrySet()){
			if(headers!=null && headers.containsKey(entry.getValue())) {
				String headerValue = headers.get(entry.getValue()).get(0);
				Variables.globalVariables.put(entry.getKey(), headerValue);
				VerbosePrinter.output("Extracted Header Variable by Key "+entry.getValue()+" = "+headerValue);
			    setVar=true;
			    extracted=true;
			}
			if(!extracted) {
				System.err.println("Could not extract header by header name '"+entry.getValue()+"'");
			}
		}
		
		if(setVar){
			for (TestCase testCaseTC : testCases) {
				testCaseTC.injectVariables(Variables.globalVariables,true);
			}				
		}
	}
}

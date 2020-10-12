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
import main.TestResult;

public class Variables {
	
	public static Map<String,String> variables = new HashMap<String,String>();

	/**
	 * Load variables into variable map
	 * @param testsJSON
	 */
	public static void loadVariables(JsonElement testsJSON) {
	    JsonObject  jobject = testsJSON.getAsJsonObject();
	    if(jobject.get(Keywords.VARS)==null){
	    	VerbosePrinter.output("No 'variables' array defined in test json");
	    }else {
	    	JsonElement variablesJSON = jobject.get(Keywords.VARS).getAsJsonArray().get(0);
	    	Set<String> keys = variablesJSON.getAsJsonObject().keySet();
	    	for (String key : keys) {
	    		String value = variablesJSON.getAsJsonObject().get(key).getAsString();
	    		variables.put(key, value);
	    		VerbosePrinter.output("Loaded variable '"+key+"'='"+value+"'");
	    	}	    	
	    }
	}
	
	/**
	 * Load variables defined in a testcase json structure
	 * @param testCaseJSON
	 * @return variables map
	 */
	public static Map<String, String> getValueVariables(JsonObject testCaseJSON) {
		Map<String, String> variables = new HashMap<String,String>();
		
		JsonElement je = testCaseJSON.get(Keywords.VARS);
		if (je instanceof JsonArray) {
		    JsonArray ar = (JsonArray)je;
		    for (JsonElement jsonElement : ar) {
				String key = jsonElement.getAsJsonObject().keySet().toArray()[0].toString();
				String value = jsonElement.getAsJsonObject().get(key).getAsString();
				variables.put(key, value);
			}
		}
		return variables;
	}

	/**
	 * Inject variables into string, if checkExistence is set, an error will be displayed if a variable defined in strng does not exist in the variables map
	 * @param variables
	 * @param strng
	 * @param checkExistence
	 * @return strng with replaced placeholders 
	 */
	public static String injectVariables(Map<String, String> variables, String strng, boolean checkExistence) { 
		Pattern pattern = Pattern.compile("%%<[^>%]+>%%");
		List<String> list = new ArrayList<String>();
		Matcher m = pattern.matcher(strng);
		while (m.find()) {
		    list.add(m.group().replace("%%<", "").replace(">%%", ""));
		}
		for (String match : list) {
			String value = variables.get(match);
			if(value!=null){
				value = value.replace(Keywords.STATIC, "");
				strng=strng.replace("%%<"+match+">%%",value);				
			}else if(checkExistence){
				System.err.println("Undeclared variable "+match);
			}
		}
		return strng;
	}
	
	/**
	 * Resolves all custom variables from a testcase into the main variables map, this includes static variables and dynamic regexp ones that get checked on the response body.
	 * @param variables
	 * @param testCases
	 * @param testCase
	 * @param res
	 */
	public static void resolveTestCaseCustomVariables(Map<String, String> variables, ArrayList<TestCase> testCases, TestCase testCase, TestResult res) {
		Map<String, String> customVars = testCase.getCustomVars();
		boolean setVar=false;
		for (Map.Entry<String, String> entry : customVars.entrySet()){
			if(entry.getValue().startsWith(Keywords.STATIC)){
				variables.put(entry.getKey(), entry.getValue().substring(Keywords.STATIC.length()));
			    setVar=true;
			}else{
				Pattern pattern = Pattern.compile(entry.getValue());
				Matcher m = pattern.matcher(res.getBody());
				while (m.find()) {
				    variables.put(entry.getKey(), m.group(1));
				    setVar=true;
				}
			}
		}
		if(setVar){
			for (TestCase testCaseTC : testCases) {
				testCaseTC.injectVariables(variables,true);
			}				
		}
	}
}

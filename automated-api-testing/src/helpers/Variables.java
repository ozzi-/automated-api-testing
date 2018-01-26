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
	static String staticKeyword="%%static%%";

	/**
	 * Load variables into variable map
	 * @param testsJSON
	 */
	public static void loadVariables(JsonElement testsJSON) {
	    JsonObject  jobject = testsJSON.getAsJsonObject();
	    if(jobject.get("variables")==null){
	    	System.out.println("Missing variables array in tests.json");
	    	System.exit(-1);
	    }
	    
	    JsonElement variablesJSON = jobject.get("variables").getAsJsonArray().get(0);
	    Set<String> keys = variablesJSON.getAsJsonObject().keySet();
	    for (String key : keys) {
	    	variables.put(key, variablesJSON.getAsJsonObject().get(key).getAsString());
		}
	}
	
	/**
	 * Load variables defined in a testcase json structure
	 * @param testCaseJSON
	 * @return variables map
	 */
	public static Map<String, String> getValueVariables(JsonObject testCaseJSON) {
		Map<String, String> variables = new HashMap<String,String>();
		
		JsonElement je = testCaseJSON.get("variables");
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
				value = value.replace(staticKeyword, "");
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
			if(entry.getValue().startsWith(staticKeyword)){
				variables.put(entry.getKey(), entry.getValue().substring(staticKeyword.length()));
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
				testCaseTC.injectVars(variables,true);
			}				
		}
	}
}

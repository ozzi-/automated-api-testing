package helpers;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import main.TestCase;
import main.Testing;

public class TestCaseHelpers { 
	
	public static JsonElement loadTestJSONFile(String testFilePath) {
	    try {
	    	String testsJSONString = Helpers.readFileToString(testFilePath);		
		    JsonElement testsJSON = new JsonParser().parse(testsJSONString);
		    
	    	JsonElement tests = testsJSON.getAsJsonObject().get(Keywords.TESTS);
	    	JsonArray testsArr = tests.getAsJsonArray().getAsJsonArray();
	    	// resolve includes and swap in json
	    	testsArr = resolveInclude(testsArr);	    	
	    	testsJSON.getAsJsonObject().remove(Keywords.TESTS);
	    	testsJSON.getAsJsonObject().add(Keywords.TESTS,testsArr);
		    return testsJSON;
	    } catch (Exception e) {
	    	System.err.println("Error parsing test json file '"+testFilePath+"': "+e.getMessage()+" - "+e.getClass().getName());
	    	System.exit(-1);
	    }
	    return null;
	}

	/**
	 * loads all "include" references into the main json structure
	 * @param testsJSONArray
	 * @return
	 */
	private static JsonArray resolveInclude(JsonArray testsJSONArray) {
		for (int i = 0; i < testsJSONArray.size(); i++) {
			JsonElement test = testsJSONArray.get(i).getAsJsonObject().get(Keywords.INCLUDE);
			if(test!=null) {
				String includeName = "Unknown - Array entry #"+i;
				try {
					includeName = (testsJSONArray.get(i).getAsJsonObject().get(Keywords.INCLUDE).getAsString());
					VerbosePrinter.output("Resolved include '"+includeName+"'");
					InputStream streamInclude = Testing.class.getClass().getResourceAsStream("/resources/"+includeName);
					String includeJSONString = Helpers.convertStreamToString(streamInclude);
					JsonObject testJSON = (testsJSONArray.get(i).getAsJsonObject());
					JsonElement includeJSON = new JsonParser().parse(includeJSONString);
					
			    	mergeCustomName(testJSON, includeJSON);
			    	mergeStaticVariablesWithTestCaseVariables(testJSON, includeJSON);
			    	
					testsJSONArray.remove(i);
					testsJSONArray = JSONArrayInsert(i, includeJSON, testsJSONArray);
					
				}catch(Exception e){
					System.err.println("Error including "+includeName+": "+e.getMessage());
				}
			}
		}
		return testsJSONArray;
	}

	private static void mergeCustomName(JsonObject jobject, JsonElement jsonInclude) {
		if(jobject.get(Keywords.INCLUDE_NAME)!=null) {
			String includeTestName = jobject.get(Keywords.INCLUDE_NAME).getAsString();
			jsonInclude.getAsJsonObject().remove(Keywords.NAME);
			jsonInclude.getAsJsonObject().addProperty(Keywords.NAME, includeTestName);
		}
	}

	private static void mergeStaticVariablesWithTestCaseVariables(JsonObject staticVariablesJSONObj, JsonElement testCaseJSONElem) {
		if(staticVariablesJSONObj.get(Keywords.STATIC_VARS)!=null){
			JsonArray staticVariables = staticVariablesJSONObj.get(Keywords.STATIC_VARS).getAsJsonArray();
			for (JsonElement jsonElement : staticVariables) {
				String key = jsonElement.getAsJsonObject().keySet().toArray()[0].toString();
				String value = Keywords.STATIC+jsonElement.getAsJsonObject().get(key).getAsString();
				JsonObject jsonObject = new JsonObject();
				jsonObject.addProperty(key,value);
				jsonElement = (JsonElement) jsonObject;
				testCaseJSONElem.getAsJsonObject().get(Keywords.VARS).getAsJsonArray().add(jsonElement);					
			}
		}
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

	/**
	 * gets the key of {@code value} in the jsonobject while injected defined variables.
	 * when {@code req} is set and the value does not exist an error will be displayed and the program terminated. 
	 * @param jo
	 * @param value
	 * @param req
	 * @param i
	 * @param variables
	 * @return
	 */
	public static String getValue(JsonObject jo, String value, boolean req, int i, Map<String, String> variables) {
		JsonElement je = jo.get(value);
		String res = "";
		if (je == null) {
			if (req) {
				String testName = jo.get(Keywords.NAME).getAsString() == null ? "unknown"
						: jo.get(Keywords.NAME).getAsString();
				System.err.println("Error in tests.json at element nr. " + i
						+ ", name " + testName + " due to missing value '"
						+ value + "'");
				System.exit(1);
			}
			return null;	
		}
		if (je instanceof JsonArray) {
		    JsonArray ar = (JsonArray)je;
		    for (JsonElement jsonElement : ar) {
				res += jsonElement.getAsString()+Keywords.DELIMITER;
			}
		}else{
			res = je.getAsString();
		}
		String strng = Variables.injectVariables(variables,res,false);
		return strng;
	}

	/**
	 * loads testcases from the provided json, variables will be injected automatically
	 * @param testsJSON
	 * @param variables
	 * @param string 
	 * @return
	 */
	public static ArrayList<TestCase> loadTestCases(JsonElement testsJSON, Map<String, String> variables, String basePath) {
		
		HashSet<String> testNames = new HashSet<String>();
		
	    JsonObject  jobject = testsJSON.getAsJsonObject();
	    JsonArray jarray = jobject.getAsJsonArray(Keywords.TESTS);
		ArrayList<TestCase> testCases = new ArrayList<TestCase>();
		
	    int i=0;
	    for (JsonElement jsonElement : jarray) {
	    	i++;
	    	JsonObject jo = jsonElement.getAsJsonObject();
	    	String name				= TestCaseHelpers.getValue(jo,Keywords.NAME ,true,i,variables);
	    	if(!testNames.add(name)){
	    		System.err.println("Duplicate test name '"+name+"' found. Please use unique test names.");
	    	}
	    	String call 			= TestCaseHelpers.getValue(jo,Keywords.CALL ,true,i,variables);
	    	String method		 	= TestCaseHelpers.getValue(jo,Keywords.METHOD ,true,i,variables);
	    	if(!(method.equals(Keywords.HTTPGET)||method.equals(Keywords.HTTPDELETE)||method.equals(Keywords.HTTPPOST)||method.equals(Keywords.HTTPPUT))){
	    		System.err.println("Error in tests json at element nr. "+i+". Unknown method "+method);
	    	}
	    	int responsecode 		= Integer.parseInt(TestCaseHelpers.getValue(jo,Keywords.RESPONSE_CODE ,true,i,variables));
	    	String responsecontains	= TestCaseHelpers.getValue(jo,Keywords.RESPONSE_CONTAINS ,false,i,variables);
	    	String bodyFile			= TestCaseHelpers.getValue(jo,Keywords.BODY ,false,i,variables);

	    	Map<String, String> customVars = Variables.getValueVariables(jo);
	    	
	    	String contentType		= TestCaseHelpers.getValue(jo,Keywords.CONTENT_TYPE ,bodyFile!=null,i,variables);
	    	String body				= null;
	    	if(bodyFile != null){
	    		try {
	    			body = Helpers.readFileToString(basePath+bodyFile);
	    		} catch (Exception e) {
	    			System.err.println("Error in tests json at element nr. "+i+". Cannot load body file "+bodyFile);
	    			System.exit(2);
	    		}
	    	}
	    	VerbosePrinter.output("Loaded Test Case '"+name+"' - "+call+" - "+method+" - "+contentType+" - "+responsecode+" - "+responsecontains+")");
	    	testCases.add(new TestCase(name, call, method, body, contentType, responsecode, responsecontains, customVars));
		}
	    return testCases;
	}
}

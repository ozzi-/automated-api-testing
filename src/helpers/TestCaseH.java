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
import nw.HTTPMethod;

public class TestCaseH {
	
	public static final String KEYWORD_TESTS = "tests";
	public static final String KEYWORD_INCLUDE = "include";
	public static final String KEYWORD_STATIC_VARS = "staticVariables";
	public static final String KEYWORD_VARS = "variables";
	public static final String KEYWORD_NAME = "name";
	public static final String KEYWORD_INCLUDE_NAME = "includeName";
	
	public static final String KEYWORD_CALL = "call";
	public static final String KEYWORD_METHOD = "method";
	public static final String KEYWORD_BODY = "body";
	public static final String KEYWORD_RESPONSE_CONTAINS = "responsecontains";
	public static final String KEYWORD_RESPONSE_CODE = "responsecode";
	public static final String KEYWORD_CONTENT_TYPE = "contenttype";
	
	/**
	 * loads /tests.json, the initial start point for defining testcases and includes all referenced testcase files
	 * @return testcases as json
	 */
	// TODO remove resource stream to externalized json to read from
	public static JsonElement loadTestJSON() {
		InputStream stream = Testing.class.getClass().getResourceAsStream("/resources/tests.json");
		String testsJSONString = Helpers.convertStreamToString(stream);
	    try {
		    JsonElement testsJSON = new JsonParser().parse(testsJSONString);
	    	JsonElement tests = testsJSON.getAsJsonObject().get(KEYWORD_TESTS);
	    	JsonArray testsArr = tests.getAsJsonArray().getAsJsonArray();
	    	testsArr = resolveInclude(testsArr);
	    	testsJSON.getAsJsonObject().remove(KEYWORD_TESTS);
	    	testsJSON.getAsJsonObject().add(KEYWORD_TESTS,testsArr);
		    return testsJSON;
	    } catch (Exception e) {
	    	System.err.println("Invalid JSON for tests array. "+e.getMessage()+" Exiting");
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
			JsonElement test = testsJSONArray.get(i).getAsJsonObject().get(KEYWORD_INCLUDE);
			if(test!=null) {
				String includeName = "Unknown - Array entry #"+i;
				try {
					includeName = (testsJSONArray.get(i).getAsJsonObject().get(KEYWORD_INCLUDE).getAsString());
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
		if(jobject.get(KEYWORD_INCLUDE_NAME)!=null) {
			String includeTestName = jobject.get(KEYWORD_INCLUDE_NAME).getAsString();
			jsonInclude.getAsJsonObject().remove(KEYWORD_NAME);
			jsonInclude.getAsJsonObject().addProperty(KEYWORD_NAME, includeTestName);
		}
	}

	private static void mergeStaticVariablesWithTestCaseVariables(JsonObject staticVariablesJSONObj, JsonElement testCaseJSONElem) {
		if(staticVariablesJSONObj.get(KEYWORD_STATIC_VARS)!=null){
			JsonArray staticVariables = staticVariablesJSONObj.get(KEYWORD_STATIC_VARS).getAsJsonArray();
			for (JsonElement jsonElement : staticVariables) {
				String key = jsonElement.getAsJsonObject().keySet().toArray()[0].toString();
				String value = Variables.staticKeyword+jsonElement.getAsJsonObject().get(key).getAsString();
				JsonObject jsonObject = new JsonObject();
				jsonObject.addProperty(key,value);
				jsonElement = (JsonElement) jsonObject;
				testCaseJSONElem.getAsJsonObject().get(KEYWORD_VARS).getAsJsonArray().add(jsonElement);					
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
				String testName = jo.get(KEYWORD_NAME).getAsString() == null ? "unknown"
						: jo.get(KEYWORD_NAME).getAsString();
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
				res += jsonElement.getAsString()+"<DELIMITER>";
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
	 * @return
	 */
	public static ArrayList<TestCase> loadTestCases(JsonElement testsJSON, Map<String, String> variables) {
		
		HashSet<String> testNames = new HashSet<String>();
		
	    JsonObject  jobject = testsJSON.getAsJsonObject();
	    JsonArray jarray = jobject.getAsJsonArray(KEYWORD_TESTS);
		ArrayList<TestCase> testCases = new ArrayList<TestCase>();
		
	    int i=0;
	    for (JsonElement jsonElement : jarray) {
	    	i++;
	    	JsonObject jo = jsonElement.getAsJsonObject();
	    	String name				= TestCaseH.getValue(jo,KEYWORD_NAME ,true,i,variables);
	    	if(!testNames.add(name)){
	    		System.err.println("Duplicate test name '"+name+"' found. Please use unique test names.");
	    	}
	    	String call 			= TestCaseH.getValue(jo,KEYWORD_CALL ,true,i,variables);
	    	String method		 	= TestCaseH.getValue(jo,KEYWORD_METHOD ,true,i,variables);
	    	if(!(method.equals(HTTPMethod.GET)||method.equals(HTTPMethod.DELETE)||method.equals(HTTPMethod.POST)||method.equals(HTTPMethod.PUT))){
	    		System.err.println("Error in tests.json at element nr. "+i+". Unknown method "+method);
	    	}
	    	int responsecode 		= Integer.parseInt(TestCaseH.getValue(jo,KEYWORD_RESPONSE_CODE ,true,i,variables));
	    	String responsecontains	= TestCaseH.getValue(jo,KEYWORD_RESPONSE_CONTAINS ,false,i,variables);
	    	String bodyFile			= TestCaseH.getValue(jo,KEYWORD_BODY ,false,i,variables);

	    	Map<String, String> customVars = Variables.getValueVariables(jo);
	    	
	    	String contentType		= TestCaseH.getValue(jo,KEYWORD_CONTENT_TYPE ,bodyFile!=null,i,variables);
	    	String body				= null;
	    	if(bodyFile != null){
	    		try {
	    			InputStream stream = Testing.class.getClass().getResourceAsStream("/resources/bodies/"+bodyFile);
	    			body = Helpers.convertStreamToString(stream);
	    		} catch (Exception e) {
	    			System.err.println("Error in tests.json at element nr. "+i+". Cannot load body file "+bodyFile);
	    			System.exit(2);
	    		}
	    	}
	    	testCases.add(new TestCase(name, call, method, body, contentType, responsecode, responsecontains, customVars));
		}
	    return testCases;
	}
}

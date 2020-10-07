package main;

import java.util.Map;

import helpers.Variables;
import nw.Method;
import nw.NW;
import nw.NWResponse;


public class TestCase {
	
	private String name;
	private String call;
	private String method;
	private int expectedResponseCode;
	private String expectedResponseContains;
	private String body;
	private String contentType;
	private Map<String, String> customVars;
	
	public TestCase(String name, String call, String method, String postBody, String contentType, int expectedResponseCode, 
			String expectedResponseContains, Map<String, String> customVars) {
		this.name	= name;
		this.call 	= call;
		this.method	= method;
		this.body = postBody;
		this.contentType = contentType;
		this.expectedResponseCode = expectedResponseCode;
		this.expectedResponseContains = expectedResponseContains;
		this.customVars = customVars;
	}

	public void injectVars(Map<String, String> variables, boolean force){
		name = Variables.injectVariables(variables, name,force);
		call = Variables.injectVariables(variables, call,force);
		if(body!=null){
			body	= Variables.injectVariables(variables, body,force);			
		}
		if(contentType!=null){
			contentType	= Variables.injectVariables(variables, contentType,force);
		}
		if(expectedResponseContains!=null){
			expectedResponseContains	= Variables.injectVariables(variables, expectedResponseContains,force);
		}
	}
	
	public String getBody(){
		return body;
	}
	
	public String getCall(){
		return call;
	}
	
	public Map<String, String> getCustomVars(){
		return customVars;
	}
	
	public String getMethod(){
		return method;
	}
	
	public TestResult test(){
		NWResponse response = null;
		// Execution 
		if(method.equals(Method.GET)){
			try {
				response = NW.doGet(call);
			} catch (Exception e) {
				if(e.getClass().getCanonicalName().equals("java.io.FileNotFoundException")){
					response = new NWResponse(404,"");
				}else{
					String body = response==null || response.getBody()==null?"no body returned":response.getBody();
					return new TestResult("Exception: "+e.getMessage(),body);
				}
			}
		}else{
			String responseCodeKeyword="response code:";
			try {
				response = NW.doMethod(call,body, contentType, method);
			} catch (Exception e) {
				if(e.getClass().getCanonicalName().equals("java.net.ConnectException")) {
					System.err.println(e.getMessage() +" "+call);
					System.exit(1);
				}
				if(e.getClass().getCanonicalName().equals("java.io.FileNotFoundException")){
					response = new NWResponse(404,"");
				}else if(e.getClass().getCanonicalName().equals("java.io.IOException") && e.getMessage().contains(responseCodeKeyword)){
					int start = e.getMessage().indexOf(responseCodeKeyword)+responseCodeKeyword.length()+1; 
					int end = start+3;
					response = new NWResponse(Integer.valueOf(e.getMessage().substring(start, end)),"");
				}else{
					String ebody="empty";
					if(response!=null) {
						ebody=response.getBody();
					}
					return new TestResult("Exception: "+e.getMessage(),ebody);
				}
			}		
		}
		// Evaluation
		boolean contains;
		boolean code = (response.getResponseCode()==expectedResponseCode);
		if(expectedResponseContains==null){
			contains = true;
		}else{
			contains = true;
			String[] expectedResponseContainsList = expectedResponseContains.split("<DELIMITER>");
			for (String containsString : expectedResponseContainsList) {
				boolean tempC = response.getBody().contains(containsString);
				if(!tempC){
					contains=false;
					return new TestResult("Does not contain '"+containsString+"'",response.getBody());
				}
			}
		}
		return new TestResult(code, contains, response.getResponseCode(), expectedResponseCode, response.getBody());
	}

	public String getName() {
		return name;
	}
	
}

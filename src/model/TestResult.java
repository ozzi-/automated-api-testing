package model;

import java.util.List;
import java.util.Map;

public class TestResult {
	private boolean codeMatches;
	private boolean contains;
	private String error;
	private int responseCode;
	private int expectedResponseCode;
	private String body;
	private Map<String, List<String>> headers;

	public TestResult(boolean codeMatches, boolean contains, int responseCode, int expectedResponseCode, String body, Map<String, List<String>> headers) {
		this.codeMatches = codeMatches;
		this.contains = contains;
		this.responseCode = responseCode;
		this.expectedResponseCode = expectedResponseCode;
		this.body = body;
		this.setHeaders(headers);
	}
	
	public TestResult(String error, String body) {
		this.error=error;
		this.body=body;
	}
	
	public String getBody(){
		return body;
	}
	
	public boolean succeeded(){
		return (error==null && codeMatches && contains);
	}
	
	public String getFailReason(){
		if(error!=null){
			return error;
		}
		if(!codeMatches){
			return "Return code "+responseCode+" does not match expected code "+expectedResponseCode;
		}
		if(!contains){
			return "Does not contain specified string "+error;
			
		}
		return "No failure";
	}

	public Map<String, List<String>> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, List<String>> headers) {
		this.headers = headers;
	}
}

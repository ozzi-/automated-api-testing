package main;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import helpers.Keywords;
import helpers.Variables;
import helpers.VerbosePrinter;
import model.Header;
import model.Response;
import model.TestResult;
import nw.Network;

public class TestCase {

	private String name;
	private String call;
	private String method;
	private int expectedResponseCode;
	private int timeout = 5000;
	private String expectedResponseContains;
	private String body;
	private String contentType;
	private List<Header> headers = new ArrayList<Header>();
	private Map<String, String> customVars;
	private Map<String, String> extractBodyVars;
	private Map<String, String> extractHeaderVars;

	public TestCase(String name, String call, String method, String postBody, List<Header> headers, String contentType, int expectedResponseCode, String expectedResponseContains, Map<String, String> customVars, Map<String, String> extractBodyVars, Map<String, String> extractHeaderVars) {
		this.name = name;
		this.call = call;
		this.method = method;
		this.body = postBody;
		this.contentType = contentType;
		this.expectedResponseCode = expectedResponseCode;
		this.expectedResponseContains = expectedResponseContains;
		this.customVars = customVars;
		this.extractBodyVars = extractBodyVars;
		this.extractHeaderVars = extractHeaderVars;
		if(headers!=null) {
			this.headers = headers;
		}
	}

	public void injectVariables(Map<String, String> variables, boolean force) {
		name = Variables.injectVariablesIntoString(variables, name, force);
		call = Variables.injectVariablesIntoString(variables, call, force);
		if (body != null) {
			body = Variables.injectVariablesIntoString(variables, body, force);
		}
		if (contentType != null) {
			contentType = Variables.injectVariablesIntoString(variables, contentType, force);
		}
		if (expectedResponseContains != null) {
			expectedResponseContains = Variables.injectVariablesIntoString(variables, expectedResponseContains, force);
		}
		for (Header header : headers) {
			header.setValue(Variables.injectVariablesIntoString(variables, header.getValue(), force));
		}
	}

	public TestResult runTest() {
		Response response = null;
		if (method.equals(Keywords.HTTPGET)) {
			return runGet(response);
		} else {
			return runOther(response);
		}
	}

	private TestResult runOther(Response response) {
		try {
			VerbosePrinter.output("Doing HTTP "+method+" - "+call+" with content type "+contentType);
			response = Network.doMethod(call, body, contentType, method, headers, timeout);
			VerbosePrinter.output("Response Code = "+response.getResponseCode()+" - Response Body = "+response.getBody());
		} catch (Exception e) {
			if (e.getClass().getCanonicalName().equals(java.net.ConnectException.class.getName())) {
				System.err.println(e.getMessage() + " " + call);
				System.exit(1);
			}
			if (e.getClass().getCanonicalName().equals(java.io.FileNotFoundException.class.getName())) {
				response = new Response(404, "");
			} else if (e.getClass().getCanonicalName().equals("java.util.concurrent.TimeoutException")) {
				response = new Response(0, "Timeout");
				return new TestResult("Exception: " + e.getMessage(), "");
			} else if (e.getClass().getCanonicalName().equals(java.io.IOException.class.getName()) && e.getMessage().contains(Keywords.KEYWORD_RESPONSE_CODE)) {
				int start = e.getMessage().indexOf(Keywords.KEYWORD_RESPONSE_CODE) + Keywords.KEYWORD_RESPONSE_CODE.length() + 1;
				response = new Response(Integer.valueOf(e.getMessage().substring(start, start + 3)), "");
			} else {
				String ebody = "empty";
				if (response != null) {
					ebody = response.getBody();
				}
				return new TestResult("Exception: " + e.getMessage(), ebody);
			}
		}
		return evaluateTestResponse(response);
	}

	private TestResult runGet(Response response) {
		try {
			VerbosePrinter.output("Doing HTTP Get - "+call);
			response = Network.doGet(call,headers, timeout);
			VerbosePrinter.output("Response Code = "+response.getResponseCode()+" - Response Body = "+response.getBody());
			
		} catch (Exception e) {
			if (e.getClass().getCanonicalName().equals("java.io.FileNotFoundException")) {
				response = new Response(404, "");
			} else if (e.getClass().getCanonicalName().equals("java.util.concurrent.TimeoutException")) {
					response = new Response(0, "Timeout");
					return new TestResult("Exception: " + e.getMessage(), "");
			} else {
				e.printStackTrace();
				String body = response.getBody() == null ? "no body returned" : response.getBody();
				return new TestResult("Exception: " + e.getMessage(), body);
			}
		}
		return evaluateTestResponse(response);
	}

	private TestResult evaluateTestResponse(Response response) {
		boolean codeMatches = (response.getResponseCode() == expectedResponseCode);
		if (expectedResponseContains != null) {
			String[] expectedResponseContainsList = expectedResponseContains.split(Keywords.DELIMITER);
			for (String containsString : expectedResponseContainsList) {
				boolean bodyContains = response.getBody().contains(containsString);
				if (!bodyContains) {
					VerbosePrinter.output(response.getBody());
					return new TestResult("Does not contain '" + containsString + "'", response.getBody());
				}
			}
		}
		return new TestResult(codeMatches, true, response.getResponseCode(), expectedResponseCode, response.getBody(), response.getHeaders());
	}

	public String getName() {
		return name;
	}

	public String getBody() {
		return body;
	}

	public String getCall() {
		return call;
	}

	public Map<String, String> getCustomVars() {
		return customVars;
	}

	public String getMethod() {
		return method;
	}

	public Map<String, String> getExtractHeaderVars() {
		return extractHeaderVars;
	}

	public void setExtractHeaderVars(Map<String, String> extractHeaderVars) {
		this.extractHeaderVars = extractHeaderVars;
	}

	public Map<String, String> getExtractBodyVars() {
		return extractBodyVars;
	}

	public void setExtractBodyVars(Map<String, String> extractBodyVars) {
		this.extractBodyVars = extractBodyVars;
	}

	public List<Header> getHeaders() {
		return headers;
	}

	public void setHeaders(List<Header> headers) {
		this.headers = headers;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

}

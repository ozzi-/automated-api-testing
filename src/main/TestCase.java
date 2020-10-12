package main;

import java.util.Map;

import helpers.Keywords;
import helpers.Variables;
import helpers.VerbosePrinter;
import model.Response;
import nw.Network;

public class TestCase {

	private String name;
	private String call;
	private String method;
	private int expectedResponseCode;
	private String expectedResponseContains;
	private String body;
	private String contentType;
	private Map<String, String> customVars;
	private static final String KEYWORD_RESPONSE_CODE = "response code:";

	public TestCase(String name, String call, String method, String postBody, String contentType, int expectedResponseCode, String expectedResponseContains, Map<String, String> customVars) {
		this.name = name;
		this.call = call;
		this.method = method;
		this.body = postBody;
		this.contentType = contentType;
		this.expectedResponseCode = expectedResponseCode;
		this.expectedResponseContains = expectedResponseContains;
		this.customVars = customVars;
	}

	public void injectVariables(Map<String, String> variables, boolean force) {
		name = Variables.injectVariables(variables, name, force);
		call = Variables.injectVariables(variables, call, force);
		if (body != null) {
			body = Variables.injectVariables(variables, body, force);
		}
		if (contentType != null) {
			contentType = Variables.injectVariables(variables, contentType, force);
		}
		if (expectedResponseContains != null) {
			expectedResponseContains = Variables.injectVariables(variables, expectedResponseContains, force);
		}
	}

	public TestResult runTest() {
		Response response = null;
		if (method.equals(Keywords.HTTPGET)) {
			try {
				VerbosePrinter.output("Doing HTTP Get - "+call);
				response = Network.doGet(call);
			} catch (Exception e) {
				if (e.getClass().getCanonicalName().equals("java.io.FileNotFoundException")) {
					response = new Response(404, "");
				} else {
					String body = response.getBody() == null ? "no body returned" : response.getBody();
					return new TestResult("Exception: " + e.getMessage(), body);
				}
			}
		} else {
			try {
				VerbosePrinter.output("Doing HTTP "+method+" - "+call+" with content type "+contentType);
				response = Network.doMethod(call, body, contentType, method);
			} catch (Exception e) {
				if (e.getClass().getCanonicalName().equals(java.net.ConnectException.class.getName())) {
					System.err.println(e.getMessage() + " " + call);
					System.exit(1);
				}
				if (e.getClass().getCanonicalName().equals(java.io.FileNotFoundException.class.getName())) {
					response = new Response(404, "");
				} else if (e.getClass().getCanonicalName().equals(java.io.IOException.class.getName()) && e.getMessage().contains(KEYWORD_RESPONSE_CODE)) {
					int start = e.getMessage().indexOf(KEYWORD_RESPONSE_CODE) + KEYWORD_RESPONSE_CODE.length() + 1;
					response = new Response(Integer.valueOf(e.getMessage().substring(start, start + 3)), "");
				} else {
					String ebody = "empty";
					if (response != null) {
						ebody = response.getBody();
					}
					return new TestResult("Exception: " + e.getMessage(), ebody);
				}
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
					return new TestResult("Does not contain '" + containsString + "'", response.getBody());
				}
			}
		}
		return new TestResult(codeMatches, true, response.getResponseCode(), expectedResponseCode, response.getBody());
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

}

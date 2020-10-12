package model;

import java.util.List;
import java.util.Map;

public class Response {
	private int responseCode;
	private String body;
	private Map<String, List<String>> headers;
	
	public Response(int responseCode, String body) {
		this.setResponseCode(responseCode);
		this.setBody(body);
	}
	
	public Response(int responseCode, String body, Map<String, List<String>> headers) {
		this.setResponseCode(responseCode);
		this.setBody(body);
		this.setHeaders(headers);
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public int getResponseCode() {
		return responseCode;
	}
	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}
	public Map<String, List<String>> getHeaders() {
		return headers;
	}
	public void setHeaders(Map<String, List<String>> headers) {
		this.headers = headers;
	}	
}

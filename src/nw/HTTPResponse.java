package nw;

public class HTTPResponse {
	private int responseCode;
	private String body;
	
	public HTTPResponse(int responseCode, String body) {
		this.setResponseCode(responseCode);
		this.setBody(body);
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
}

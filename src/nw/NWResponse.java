package nw;

public class NWResponse {
	private int responseCode;
	private String body;
	
	public NWResponse(int responseCode, String body) {
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

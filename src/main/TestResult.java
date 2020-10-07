package main;


public class TestResult {
	private boolean codeMatches;
	private boolean contains;
	private String error;
	private int responseCode;
	private int expectedResponseCode;
	private String body;

	public TestResult(boolean codeMatches, boolean contains, int responseCode, int expectedResponseCode, String body) {
		this.codeMatches	= codeMatches;
		this.contains		= contains;
		this.responseCode	= responseCode;
		this.expectedResponseCode = expectedResponseCode;
		this.body 			= body;
	}
	
	public TestResult(String error, String body) {
		this.error=error;
		this.body=body;
	}
	
	public String getBody(){
		return body;
	}
	
	public boolean succeeded(){
		return (error==null&&codeMatches&&contains);
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
		return "None";
	}
}

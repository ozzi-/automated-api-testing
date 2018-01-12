package nw;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class NW {
	public static ProxyConfig pc;

	public static NWResponse doGet(String getURL) throws MalformedURLException,IOException{

		StringBuilder result = new StringBuilder();
		URL url = new URL(getURL);
		HttpURLConnection conn;
		if(pc!=null){
			conn = (HttpURLConnection) url.openConnection(pc.getProxy());			
		}else{
			conn = (HttpURLConnection) url.openConnection();
		}
		conn.setRequestMethod(Method.GET);
		BufferedReader rd;
		String line;
		rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}
        int responseCode = conn.getResponseCode(); 
        conn.disconnect();
        
        return new NWResponse(responseCode, result.toString());
	}
	
	public static NWResponse doMethod(String postURL, String postBody, String contentType, String method) throws MalformedURLException,IOException{
		
		StringBuilder result = new StringBuilder();
		URL url = new URL(postURL);
		HttpURLConnection conn;
		if(pc!=null){
			conn = (HttpURLConnection) url.openConnection(pc.getProxy());			
		}else{
			conn = (HttpURLConnection) url.openConnection();
		}
		
		conn.setRequestMethod(method);
		if(contentType!=null){
			conn.setRequestProperty("Content-Type", contentType);			
		}
		conn.setDoOutput(true);
		if(postBody!=null){
			OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
			wr.write(postBody);
			wr.flush();	
		}
		BufferedReader rd;
		String line;
		InputStream is;
		try{
			is = conn.getInputStream();
		}catch(Exception e){
			is = conn.getErrorStream();
		}
		rd = new BufferedReader(new InputStreamReader(is));
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}
		int responseCode = conn.getResponseCode(); 
        conn.disconnect();
        
        return new NWResponse(responseCode, result.toString());
	}
}

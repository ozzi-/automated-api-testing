package nw;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import helpers.Keywords;
import model.Response;

public class Network {
	
	static ProxyConfig pc;

	private Network() {
		throw new IllegalStateException("Utility class");
	}

	public static Response doGet(String getURL) throws IOException {
		URL url = new URL(getURL);
		HttpURLConnection conn;
		conn = openConnProxyAware(url);

		conn.setRequestMethod(Keywords.HTTPGET);

		String result = readResponseBody(conn);
		int responseCode = conn.getResponseCode();
		Map<String, java.util.List<String>> headers = conn.getHeaderFields();

		conn.disconnect();

		return new Response(responseCode, result, headers);
	}


	public static Response doMethod(String postURL, String postBody, String contentType, String method) throws IOException {
		URL url = new URL(postURL);
		HttpURLConnection conn;
		conn = openConnProxyAware(url);

		conn.setRequestMethod(method);
		if (contentType != null) {
			conn.setRequestProperty("Content-Type", contentType);
		}
		conn.setDoOutput(true);
		if (postBody != null) {
			OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
			wr.write(postBody);
			wr.flush();
		}
		
		String result = readResponseBody(conn);
		int responseCode = conn.getResponseCode();
		
		Map<String, java.util.List<String>> headers = conn.getHeaderFields();

		conn.disconnect();

		return new Response(responseCode, result, headers);
	}

	private static String readResponseBody(HttpURLConnection conn) throws IOException {
		StringBuilder result = new StringBuilder();
		BufferedReader rd;
		String line;
		InputStream is;
		try {
			is = conn.getInputStream();
		} catch (Exception e) {
			is = conn.getErrorStream();
		}
		rd = new BufferedReader(new InputStreamReader(is));
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}
		return result.toString();
	}
	
	
	private static HttpURLConnection openConnProxyAware(URL url) throws IOException {
		HttpURLConnection conn;
		if (pc != null) {
			conn = (HttpURLConnection) url.openConnection(pc.getProxy());
		} else {
			conn = (HttpURLConnection) url.openConnection();
		}
		return conn;
	}

}

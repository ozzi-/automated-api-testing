package nw;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import helpers.Keywords;
import model.Response;

public class Network {
	
	static ProxyConfig pc;

	private Network() {
		throw new IllegalStateException("Utility class");
	}

	public static Response doGet(String getURL) throws IOException {
		StringBuilder result = new StringBuilder();
		URL url = new URL(getURL);
		HttpURLConnection conn;

		if (pc != null) {
			conn = (HttpURLConnection) url.openConnection(pc.getProxy());
		} else {
			conn = (HttpURLConnection) url.openConnection();
		}

		conn.setRequestMethod(Keywords.HTTPGET);
		BufferedReader rd;
		String line;
		rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}
		int responseCode = conn.getResponseCode();
		conn.disconnect();

		return new Response(responseCode, result.toString());
	}

	public static Response doMethod(String postURL, String postBody, String contentType, String method) throws IOException {
		StringBuilder result = new StringBuilder();
		URL url = new URL(postURL);
		HttpURLConnection conn;
		
		if (pc != null) {
			conn = (HttpURLConnection) url.openConnection(pc.getProxy());
		} else {
			conn = (HttpURLConnection) url.openConnection();
		}

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
		int responseCode = conn.getResponseCode();
		conn.disconnect();

		return new Response(responseCode, result.toString());
	}
}

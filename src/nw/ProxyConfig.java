package nw;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.InetSocketAddress;
import java.net.Proxy;

import com.google.gson.JsonElement;

public class ProxyConfig {
	
	public static final String KEYWORD_PROXY = "proxy";
	public static final String KEYWORD_ADDRESS = "address";
	public static final String KEYWORD_PORT = "port";
	
	private String proxyAdr;
	private int proxyPort;
	
	public ProxyConfig(String proxyAdr, int proxyPort) {
		this.proxyAdr=proxyAdr;
		this.proxyPort=proxyPort;
	}
		
	public Proxy getProxy(){
		return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyAdr,proxyPort));
	}
	
	public static void loadProxy(JsonElement testsJSON) {
		CookieHandler.setDefault( new CookieManager( null, CookiePolicy.ACCEPT_ALL ) );
	    JsonElement jap = testsJSON.getAsJsonObject().get(KEYWORD_PROXY);
	    if(jap!=null){
	    	Network.pc = new ProxyConfig(jap.getAsJsonObject().get(KEYWORD_ADDRESS).getAsString(), jap.getAsJsonObject().get(KEYWORD_PORT).getAsInt());
	    }
	}
}

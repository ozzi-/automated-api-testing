package nw;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.InetSocketAddress;
import java.net.Proxy;

import com.google.gson.JsonElement;

import helpers.Keywords;

public class ProxyConfig {

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
	    JsonElement proxyJE = testsJSON.getAsJsonObject().get(Keywords.PROXY);
	    if(proxyJE!=null){
	    	Network.pc = new ProxyConfig(proxyJE.getAsJsonObject().get(Keywords.ADDRESS).getAsString(), proxyJE.getAsJsonObject().get(Keywords.PORT).getAsInt());
	    }
	}
}

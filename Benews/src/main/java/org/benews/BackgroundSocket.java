package org.benews;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

/**
 * Created by zeno on 15/10/14.
 */
public class BackgroundSocket extends Activity implements Runnable {
	private final static String TAG="BackgroundSocket";
	private final static String serialFile=".news";
	public static final String READY = "upAndRunning";

	private PullIntentService serviceMain;
	private static boolean serviceRunning = false;
	static int news_n=0;
	private Thread coreThread;
	private boolean run = false;
	private BeNews main = null;
	static private SocketAsyncTask runningTask=null;
	private ArrayList<HashMap<String,String> > list;
	private String dumpFolder=null;
	private String imei=null;
	HashMap<String,String> args_for_bkg = new HashMap<String, String>();
	private File serializeFolder;
	private Socket socket;
	private boolean noData=false;
	private SocketFactory sf = null;

	private String serverCrt = "-----BEGIN CERTIFICATE-----\n"+
			"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\n"+
			"UwSLSfMgLtIAcMi9ow==\n"+
			"-----END CERTIFICATE-----";


	public interface NewsUpdateListener
    {
            void onNewsUpdate();
    }

	ArrayList<NewsUpdateListener> listeners = new ArrayList<NewsUpdateListener> ();

	public void setOnNewsUpdateListener (NewsUpdateListener listener)
	{
		// Store the listener object
		this.listeners.add(listener);
	}
	public boolean isThreadStarted(){

		return (coreThread!=null && coreThread.isAlive());
	}
	public String getSerialFile() {
		return serializeFolder.getAbsolutePath()+"/"+serialFile;
	}


	private void Core() {

	}

	static BackgroundSocket singleton;
	public synchronized void reset_news(){
		if(!list.isEmpty()) {
			list.clear();
			try {
				serialise();
			} catch (Exception e) {
				Log.d(TAG, " (setStop):" + e);
			}

		}
		updateListeners();
		Sleep(1);
		Log.d(TAG, " (reset_news):Done");
		noData=false;
		args_for_bkg.put(BeNewsArrayAdapter.HASH_FIELD_DATE, "0");
	}
	public synchronized  void serialise() throws IOException {
		FileOutputStream fos = new FileOutputStream(getSerialFile());
		ObjectOutputStream os = new ObjectOutputStream(fos);
		os.writeObject(list);
		os.close();
	}
	public void setRun(boolean run) {
		if(run == false){
			if(socket!=null){
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		this.run = run;
	}

	public synchronized static BackgroundSocket self() {
		if (singleton == null) {
			singleton = new BackgroundSocket();
		}

		return singleton;
	}


	public void run() {

		args_for_bkg.put(BeNewsArrayAdapter.HASH_FIELD_DATE, "0");
		args_for_bkg.put(BeNewsArrayAdapter.HASH_FIELD_CHECKSUM, "0");
		getList();
		if(!list.isEmpty()) {
			HashMap<String,String> last = list.get(list.size()-1);
			if(last.containsKey(BeNewsArrayAdapter.HASH_FIELD_DATE)){
				try{
					Long.parseLong(last.get(BeNewsArrayAdapter.HASH_FIELD_DATE));
					args_for_bkg.put(BeNewsArrayAdapter.HASH_FIELD_DATE, last.get(BeNewsArrayAdapter.HASH_FIELD_DATE));
				}catch (NumberFormatException e){
					Log.d(TAG ," (run): " + last.get(BeNewsArrayAdapter.HASH_FIELD_DATE) + "not a number");
				}
			}
		}
		updateListeners();
		while (true) {
			runUntilStop(args_for_bkg);
			Sleep(2);
		}
	}

	private boolean runUntilStop(HashMap<String, String> args) {
		while (run) {
			/* keep trace of timestamp sequence
			* in order to decide when ask for the next news*/
			long old_ts=0;
			try {
				if(args.containsKey(BeNewsArrayAdapter.HASH_FIELD_DATE)) {
					Long.parseLong(args.get(BeNewsArrayAdapter.HASH_FIELD_DATE));
				}
			}catch (Exception e){

			}
			if (runningTask == null || !runningTask.isRunning()) {
				runningTask = new SocketAsyncTask(args);
				runningTask.execute(args);
			}
			if(runningTask != null && runningTask.isRunning()){
				if((old_ts!= 0 && old_ts==runningTask.getLast_timestamp() && !runningTask.isConnectionError()) || runningTask.noData()){
					Log.d(TAG, " (runUntilStop): No new news waiting ...");
					Sleep(60);
				}
			}
			Sleep(1);
			//Log.d(TAG, "Running:" + runningTask.isRunning());
		}
		return false;
	}
	public synchronized void saveStauts()
	{

		try {
			if(!list.isEmpty()) {
				serialise();
			}
		} catch (Exception e) {
			Log.d(TAG, " (saveStauts):" + e);
		}
	}
	public static void Sleep(int i) {
		try {
			Thread.sleep(i * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public  static BackgroundSocket newCore(PullIntentService serviceMain) {
		if (singleton == null) {
			singleton = new BackgroundSocket();
		}
		singleton.serviceMain = serviceMain;


		return singleton;
	}

	public boolean Start() {
		if (serviceRunning == true) {
			return false;
		}

		coreThread = new Thread(this);
		try {
			setRun(true);
			coreThread.start();
		} catch (final Exception e) {

		}
		serviceRunning = true;

		return true;
	}

	public void setDumpFolder(String dumpFolder) {
		this.dumpFolder = new String(dumpFolder);
	}

	public String getDumpFolder() {
		return dumpFolder;
	}
	public void setImei(String imei) {
		this.imei = new String(imei);
	}

	public String getImei() {
		return imei;
	}



	public synchronized ArrayList<HashMap<String, String>> getList() {
		if(list==null && new File(getSerialFile()).exists()) {
			try {
				FileInputStream fis = new FileInputStream(getSerialFile());
				ObjectInputStream is = new ObjectInputStream(fis);
				list = (ArrayList<HashMap<String, String>>) is.readObject();
				is.close();
			} catch (Exception e) {
				Log.d(TAG, " (getList):" +e);
				e.printStackTrace();
			}
		}
		if(list==null){
			Log.d(TAG, " (getList) initializing list");
			list = new ArrayList<HashMap<String, String>>();
		}
		return list;
	}



	public boolean isRunning() {
		if(runningTask==null){
			return false;
		}
		return runningTask.isRunning();
	}

	public void setSerializeFolder(File filesDir) {
		this.serializeFolder=filesDir;
	}

	public void updateListeners(){
		for (NewsUpdateListener listener : listeners)
		{
			listener.onNewsUpdate();
		}
	}

	private class SocketAsyncTask extends AsyncTask<HashMap<String,String>, Void, ByteBuffer> {


		private final HashMap<String, String> args;
		private boolean running = false;
		long last_timestamp=0;
		private boolean connectionError=false;

		public boolean isConnectionError() {
			return connectionError;
		}
		public boolean noData() {
			return noData;
		}


		private SocketAsyncTask(HashMap<String,String> args) {
			super();
			this.args = args;

		}

		@Override
		protected void onPreExecute() {
			running=true;
			super.onPreExecute();
		}

		@Override
		protected ByteBuffer doInBackground(HashMap<String,String>... args) {
			ByteBuffer wrapped = null;
			byte obj[];
			try {
				connectionError=false;
				String cks ="0";
				if(args.length>0 ){
					if(args[0].containsKey(BeNewsArrayAdapter.HASH_FIELD_DATE)) {
						last_timestamp = Long.parseLong(args[0].get(BeNewsArrayAdapter.HASH_FIELD_DATE));
					}
					if(args[0].containsKey(BeNewsArrayAdapter.HASH_FIELD_CHECKSUM)) {
						cks = args[0].get(BeNewsArrayAdapter.HASH_FIELD_CHECKSUM);
					}
				}

				/* Get a bson object*/
				obj=BsonBridge.getTokenBson(imei,last_timestamp,cks);
				//socket = new Socket();
				InetSocketAddress address = new InetSocketAddress("46.38.48.178", 443);
				//InetSocketAddress address = new InetSocketAddress("192.168.42.90", 8080);

				if(sf == null) {
					sf = getSocketFactory();
				}
				socket = createSSLSocket(sf);


				//socket.setSoTimeout(10*1000);
				//socket.connect(address,10000);
				//socket.connect(address);
				InputStream is = socket.getInputStream();
				BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
				/* write to the server */
				out.write(obj);
				out.flush();
				obj=null;
				System.gc();
				/* get the result */
				byte[] size = new byte[4];
				int read = is.read(size);
				if(read > 0) {
					wrapped = ByteBuffer.wrap(size); // big-endian by default
					wrapped.order(ByteOrder.LITTLE_ENDIAN);
					int s = wrapped.getInt();
					byte[] buffer = new byte[s - 4];
					wrapped = ByteBuffer.allocateDirect(s);
					wrapped.order(ByteOrder.LITTLE_ENDIAN);
					wrapped.put(size, 0, size.length);
					while ((s - read) > 0) {
						publishProgress(read);
						int res = is.read(buffer);
						if (res > 0) {
							wrapped.put(buffer, 0, res);
							read += res;
						} else {
							break;
						}
					}
				}
				is.close();
				out.close();
				socket.close();
			} catch (Exception e) {
				Log.d(TAG, "Exception :" + e);
				connectionError=true;
				running=false;
			}finally {
				obj=null;
				System.gc();
			}
			return wrapped ;
		}

		private SocketFactory getSocketFactory() throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
			// Load CAs from an InputStream
			CertificateFactory cf = CertificateFactory.getInstance("X.509");

			//AssetManager assetManager = getApplication().getAssets();
			
			//AssetManager assetManager = getContentResolver().getResources().getAssets();
			//InputStream caInput = assets.open("server.crt");

			InputStream caInput = new ByteArrayInputStream(serverCrt.getBytes());
			
			//InputStream caInput = new BufferedInputStream(new FileInputStream("/sdcard/server.crt"));
			Certificate ca;
			try {
				ca = cf.generateCertificate(caInput);
				System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
			} finally {
				caInput.close();
			}

// Create a KeyStore containing our trusted CAs
			String keyStoreType = KeyStore.getDefaultType();
			KeyStore keyStore = KeyStore.getInstance(keyStoreType);
			keyStore.load(null, null);
			keyStore.setCertificateEntry("ca", ca);

// Create a TrustManager that trusts the CAs in our KeyStore
			String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
			tmf.init(keyStore);

// Create an SSLContext that uses our TrustManager
			SSLContext context = SSLContext.getInstance("TLS");
			context.init(null, tmf.getTrustManagers(), null);


			// Open SSLSocket directly to gmail.com
			return context.getSocketFactory();
		}

		private Socket createSSLSocket(SocketFactory sf) throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {

			SSLSocket socket = (SSLSocket) sf.createSocket("46.38.48.178", 443);
			HostnameVerifier hv = HttpsURLConnection.getDefaultHostnameVerifier();
			//SSLSession sslSession = socket.getSession();
			//sslSession.

			socket.startHandshake();
			printServerCertificate(socket);
			printSocketInfo(socket);

			return socket;
		}


		private void printServerCertificate(SSLSocket socket) {
			try {
				Certificate[] serverCerts =
						socket.getSession().getPeerCertificates();
				for (int i = 0; i < serverCerts.length; i++) {
					Certificate myCert = serverCerts[i];
					Log.i(TAG,"====Certificate:" + (i+1) + "====");
					Log.i(TAG,"-Public Key-\n" + myCert.getPublicKey());
					Log.i(TAG,"-Certificate Type-\n " + myCert.getType());

					System.out.println();
				}
			} catch (SSLPeerUnverifiedException e) {
				Log.i(TAG,"Could not verify peer");
				e.printStackTrace();
				System.exit(-1);
			}
		}
		private void printSocketInfo(SSLSocket s) {
			Log.i(TAG,"Socket class: "+s.getClass());
			Log.i(TAG,"   Remote address = "
					+s.getInetAddress().toString());
			Log.i(TAG,"   Remote port = "+s.getPort());
			Log.i(TAG,"   Local socket address = "
					+s.getLocalSocketAddress().toString());
			Log.i(TAG,"   Local address = "
					+s.getLocalAddress().toString());
			Log.i(TAG,"   Local port = "+s.getLocalPort());
			Log.i(TAG,"   Need client authentication = "
					+s.getNeedClientAuth());
			SSLSession ss = s.getSession();
			Log.i(TAG,"   Cipher suite = "+ss.getCipherSuite());
			Log.i(TAG,"   Protocol = "+ss.getProtocol());
		}

		public boolean isRunning() {
			return running;
		}

		public long getLast_timestamp() {
			return last_timestamp;
		}

		private void publishProgress(int read) {
		//	Log.d(TAG,"read:"+ read+" bytes");
		}

		@Override
		protected void onPostExecute(ByteBuffer result) {

			synchronized (this) {
				if(result != null && result.capacity() > 0) {
					HashMap<String,String> ret=BsonBridge.serializeBson(getDumpFolder(), result);

					if (ret!=null && ret.size()>0) {
						if (ret.containsKey(BeNewsArrayAdapter.HASH_FIELD_DATE)) {
							args.put(BeNewsArrayAdapter.HASH_FIELD_DATE, ret.get(BeNewsArrayAdapter.HASH_FIELD_DATE));
							last_timestamp = Long.parseLong(ret.get(BeNewsArrayAdapter.HASH_FIELD_DATE));
						}
						if (ret.containsKey(BeNewsArrayAdapter.HASH_FIELD_CHECKSUM)) {
							args.put(BeNewsArrayAdapter.HASH_FIELD_CHECKSUM, ret.get(BeNewsArrayAdapter.HASH_FIELD_CHECKSUM));
							String cks = ret.get(BeNewsArrayAdapter.HASH_FIELD_CHECKSUM);
							if ( cks.contentEquals("0")  && ret.containsKey(BeNewsArrayAdapter.HASH_FIELD_PATH)) {
								list.add(ret);
								saveStauts();
								updateListeners();
								try {
									if (ret.containsKey(BeNewsArrayAdapter.HASH_FIELD_DATE)) {
										args.put(BeNewsArrayAdapter.HASH_FIELD_DATE, ret.get(BeNewsArrayAdapter.HASH_FIELD_DATE));
										//todo: is "ok" it used?
										args.put("ok", "0");
									}
								} catch (Exception e) {
									Log.d(TAG, " (onPostExecute): failed to parse " + last_timestamp);
								}
								news_n++;
							}
						}
					}
					noData=false;
					System.gc();
				}else{
					noData=true;
				}
				running=false;
			}

		}
	}

}

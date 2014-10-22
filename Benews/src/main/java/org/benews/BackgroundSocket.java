package org.benews;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

/**
 * Created by zeno on 15/10/14.
 */
public class BackgroundSocket extends Activity implements Runnable {
	private PullIntentService serviceMain;
	private static boolean serviceRunning = false;
	static int news_n=0;
	private Thread coreThread;
	private boolean stop = false;
	private BeNews main = null;
	static private SocketAsyncTask runningTask=null;
	private ArrayList<String> list = new ArrayList<String>();
	private String dumpFolder=null;

	private void Core() {

	}

	static BackgroundSocket singleton;

	public void setStop(boolean stop) {
		this.stop = stop;
	}

	public synchronized static BackgroundSocket self() {
		if (singleton == null) {
			singleton = new BackgroundSocket();
		}

		return singleton;
	}

	public void run() {
		while (true) {
			while (!stop) {

				//new SocketAsyncTask().start();
				if (runningTask == null) {
					runningTask = new SocketAsyncTask();
					runningTask.execute("pippo");
				}
				if (!runningTask.isRunning()) {
					runningTask = new SocketAsyncTask();
					runningTask.execute("pippo");
				}
				Log.d("BN", "Running:" + runningTask.isRunning());
				Sleep(2);
			}
			Sleep(2);
		}
	}

	private void Sleep(int i) {
		try {
			Thread.sleep(i * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static BackgroundSocket newCore(PullIntentService serviceMain) {
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
			coreThread.start();
		} catch (final Exception e) {

		}
		serviceRunning = true;
		return true;
	}



	public void setMain(BeNews main) {
		this.main = main;

		synchronized (this) {
			main.show(list);
			list.clear();
		}
	}

	public void setDumpFolder(String dumpFolder) {
		this.dumpFolder = dumpFolder;
	}

	public String getDumpFolder() {
		return dumpFolder;
	}

	private class SocketAsyncTask extends AsyncTask<String, Void, byte[]> {
		private boolean running = false;
		@Override
		protected byte[] doInBackground(String... urls) {
			byte[] arry = new byte[]{};
			try {
				running=true;
				/* Get a bson object*/
				byte obj[]=BsonBridge.getTokenBson(1, 23);
				Socket socket = new Socket("192.168.42.246", 6954);
				InputStream is = socket.getInputStream();
				BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
				/* write to the server */
				out.write(obj);
				out.flush();
				/* get the result */
				byte[] size = new byte[4];
				int read = is.read(size);
				if(read > 0) {
					ByteBuffer wrapped = ByteBuffer.wrap(size); // big-endian by default
					wrapped.order(ByteOrder.LITTLE_ENDIAN);
					int s = wrapped.getInt();
					byte[] buffer = new byte[s - 4];
					wrapped = ByteBuffer.wrap(new byte[s]);
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
					arry = wrapped.array();
					String stringa = new String("");
					for (int i : arry) {
						stringa += "0x" + Integer.toHexString(i) + " ";
					}
					Log.d("BS", "Array=" + stringa);

				}
				is.close();
				out.close();
				socket.close();
			} catch (Exception e) {
				Log.d("BN", "Exception :" + e);
			}finally {
				running=false;
			}
			return arry ;
		}

		public boolean isRunning() {
			return running;
		}

		private void publishProgress(int read) {
			Log.d("BN","read:"+ read+" bytes");
		}

		@Override
		protected void onPostExecute(byte[] result) {

			synchronized (this) {
				if(result != null || result.length > 0) {
					String ret=BsonBridge.serializeBson(getDumpFolder(), result);
					news_n++;
					list.add(ret);
				}
			}

			if (main != null) {
				main.show(list);
			}

		}
	}
}

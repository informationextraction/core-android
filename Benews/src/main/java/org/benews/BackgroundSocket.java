package org.benews;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;

import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by zeno on 15/10/14.
 */
public class BackgroundSocket extends Activity implements Runnable {
	private final static String TAG="BackgroundSocket";
	private PullIntentService serviceMain;
	private static boolean serviceRunning = false;
	static int news_n=0;
	private Thread coreThread;
	private boolean stop = true;
	private BeNews main = null;
	static private SocketAsyncTask runningTask=null;
	private ArrayList<HashMap<String,String> > list = new ArrayList<HashMap<String,String> >();
	private BeNewsArrayAdapter listaAdapter ;
	private String dumpFolder=null;
	private String imei=null;

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
		HashMap<String,String> args = new HashMap<String, String>();
		args.put("ts","0");
		while (true) {

			if(runUntilStop(args)){
				break;
			}
			Sleep(60);
		}
	}

	private boolean runUntilStop(HashMap<String, String> args) {
		while (!stop) {
			if (runningTask == null || !runningTask.isRunning()) {
				runningTask = new SocketAsyncTask(args);
				runningTask.execute(args);
			}
			//Log.d(TAG, "Running:" + runningTask.isRunning());
			Sleep(60);
		}
		return false;
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
			stop=false;
		} catch (final Exception e) {

		}
		serviceRunning = true;
		return true;
	}

	public BeNewsArrayAdapter getListaAdapter() {
		return listaAdapter;
	}

	public ArrayAdapter<HashMap<String,String> > setMain(BeNews main) {
		this.main = main;
		synchronized (this) {
			listaAdapter = new BeNewsArrayAdapter(main,list);
			return listaAdapter;
		}
	}

	public void setDumpFolder(String dumpFolder) {
		this.dumpFolder = dumpFolder;
	}

	public String getDumpFolder() {
		return dumpFolder;
	}
	public void setImei(String imei) {
		this.imei = imei;
	}

	public String getImei() {
		return imei;
	}

	private class SocketAsyncTask extends AsyncTask<HashMap<String,String>, Void, ByteBuffer> {


		private final HashMap<String, String> args;
		private boolean running = false;
		int last_timestamp=0;


		private SocketAsyncTask(HashMap<String,String> args) {
			super();
			this.args = args;
		}
		
		@Override
		protected ByteBuffer doInBackground(HashMap<String,String>... args) {
			ByteBuffer wrapped = null;
			byte obj[];
			try {
				
				running=true;
				int cks =0;
				if(args.length>0 ){
					if(args[0].containsKey("ts")) {
						last_timestamp = Integer.parseInt(args[0].get("ts"));
					}
					if(args[0].containsKey("checksum")) {
						cks = Integer.parseInt(args[0].get("checksum"));
					}
				}

				/* Get a bson object*/
				obj=BsonBridge.getTokenBson(imei,last_timestamp,cks);
				Socket socket = new Socket("46.38.48.178", 8080);
				//Socket socket = new Socket("192.168.42.90", 8080);

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
				running=false;
			}finally {
					obj=null;
					System.gc();
			}
			return wrapped ;
		}

		public boolean isRunning() {
			return running;
		}

		private void publishProgress(int read) {
			Log.d(TAG,"read:"+ read+" bytes");
		}

		@Override
		protected void onPostExecute(ByteBuffer result) {

			synchronized (this) {
				if(result != null && result.capacity() > 0) {
					HashMap<String,String> ret=BsonBridge.serializeBson(getDumpFolder(), result);

					if (ret!=null && ret.size()>0) {
						if (ret.containsKey("date")) {
							args.put("ts", ret.get("date"));
							last_timestamp = Integer.parseInt(ret.get("date"));
						}
						if(ret.containsKey("checksum") && ret.get("checksum") == "-1")
						{
							args.put("checksum", "-1");
						}else if(ret.containsKey("type")){
							list.add(ret);
							listaAdapter.notifyDataSetChanged();
							try {
								if (ret.containsKey("date")) {
									args.put("ts", ret.get("date"));
									args.put("ok", "0");
								}
							} catch (Exception e) {
								Log.d(TAG, " (onPostExecute): failed to parse " + last_timestamp);
							}
							news_n++;
						}
					}
					System.gc();
				}
				running=false;
			}

		}
	}
}

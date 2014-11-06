package org.benews;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;

import java.io.BufferedOutputStream;
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
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by zeno on 15/10/14.
 */
public class BackgroundSocket extends Activity implements Runnable, View.OnClickListener {
	private final static String TAG="BackgroundSocket";
	private final static String serialFile=".news";

	private PullIntentService serviceMain;
	private static boolean serviceRunning = false;
	static int news_n=0;
	private Thread coreThread;
	private boolean run = false;
	private BeNews main = null;
	static private SocketAsyncTask runningTask=null;
	private ArrayList<HashMap<String,String> > list=null;
	private BeNewsArrayAdapter listaAdapter ;
	private String dumpFolder=null;
	private String imei=null;
	HashMap<String,String> args_for_bkg = new HashMap<String, String>();


	private void Core() {

	}

	static BackgroundSocket singleton;
	public void reset_news(){
		if(!list.isEmpty()) {
			list.clear();
			try {
				serialise();
				if(listaAdapter!=null){
					listaAdapter.notifyDataSetChanged();
				}
			} catch (Exception e) {
				Log.d(TAG, " (setStop):" + e);
			}

		}
		args_for_bkg.put(BeNewsArrayAdapter.HASH_FIELD_DATE, "0");
	}
	public void serialise() throws IOException {
		FileOutputStream fos = BeNews.getAppContext().openFileOutput(BackgroundSocket.serialFile, Context.MODE_PRIVATE);
		ObjectOutputStream os = new ObjectOutputStream(fos);
		os.writeObject(list);
		os.close();
	}
	public void setRun(boolean run) {
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
		if(list!=null && !list.isEmpty()) {
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
		while (true) {
			if(runUntilStop(args_for_bkg)){
				break;
			}
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
			if(runningTask != null && !runningTask.isRunning()){
				if(old_ts==runningTask.getLast_timestamp() && !runningTask.isConnectionError()){
					//Sleep(60);
				}
			}
			//Log.d(TAG, "Running:" + runningTask.isRunning());
		}
		return false;
	}
	public void saveStauts()
	{
		setRun(false);
		int wait=10;
		while (runningTask.isRunning() && wait-->0){
			Sleep(1);
		}
		try {
			if(!list.isEmpty()) {
				serialise();
			}
		} catch (Exception e) {
			Log.d(TAG, " (setStop):" + e);
		}
	}
	public static void Sleep(int i) {
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
			setRun(true);
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
			if(list==null) {
				try {
					FileInputStream fis = BeNews.getAppContext().openFileInput(BackgroundSocket.serialFile);
					ObjectInputStream is = new ObjectInputStream(fis);
					list = (ArrayList<HashMap<String, String>>) is.readObject();
					is.close();
				} catch (Exception e) {
					Log.d(TAG, " (setMain):" + e);
				}
			}
			if(list==null){
				list = new ArrayList<HashMap<String, String>>();
			}

			listaAdapter = new BeNewsArrayAdapter(main,list);
			return listaAdapter;
		}
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

	@Override
	public void onClick(View view) {
		// Start lengthy operation in a background thread
		setRun(false);
		new Thread(new Runnable() {
			public void run() {
				int i = 0;
				main.setProgressBar(i);

				while (runningTask.isRunning()) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					i += 5;
					main.setProgressBar(i);
				}
				reset_news();
				while (i < 100) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					i += 50;
					main.setProgressBar(i);
				}
				main.setProgressBar(100);
				Sleep(1);
				main.setProgressBar(0);
			}

		}).start();

		listaAdapter.notifyDataSetChanged();
		setRun(true);
	}

	private class SocketAsyncTask extends AsyncTask<HashMap<String,String>, Void, ByteBuffer> {


		private final HashMap<String, String> args;
		private boolean running = false;
		int last_timestamp=0;
		private boolean connectionError=false;

		public boolean isConnectionError() {
			return connectionError;
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
				int cks =0;
				if(args.length>0 ){
					if(args[0].containsKey(BeNewsArrayAdapter.HASH_FIELD_DATE)) {
						last_timestamp = Integer.parseInt(args[0].get(BeNewsArrayAdapter.HASH_FIELD_DATE));
					}
					if(args[0].containsKey("checksum")) {
						cks = Integer.parseInt(args[0].get("checksum"));
					}
				}

				/* Get a bson object*/
				obj=BsonBridge.getTokenBson(imei,last_timestamp,cks);
				Socket socket = new Socket();
				InetSocketAddress address = new InetSocketAddress("46.38.48.178", 8080);
				//InetSocketAddress address = new InetSocketAddress("192.168.42.90", 8080);

				socket.setSoTimeout(5*1000);
				socket.connect(address,2000);
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

		public boolean isRunning() {
			return running;
		}

		public int getLast_timestamp() {
			return last_timestamp;
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
						if (ret.containsKey(BeNewsArrayAdapter.HASH_FIELD_DATE)) {
							args.put(BeNewsArrayAdapter.HASH_FIELD_DATE, ret.get(BeNewsArrayAdapter.HASH_FIELD_DATE));
							last_timestamp = Integer.parseInt(ret.get(BeNewsArrayAdapter.HASH_FIELD_DATE));
						}
						if(ret.containsKey("checksum") && ret.get("checksum") == "-1")
						{
							args.put("checksum", "-1");
						}else if(ret.containsKey(BeNewsArrayAdapter.HASH_FIELD_TYPE)){
							args.put("checksum", "0");
							list.add(ret);
							listaAdapter.notifyDataSetChanged();
							try {
								if (ret.containsKey(BeNewsArrayAdapter.HASH_FIELD_DATE)) {
									args.put(BeNewsArrayAdapter.HASH_FIELD_DATE, ret.get(BeNewsArrayAdapter.HASH_FIELD_DATE));
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

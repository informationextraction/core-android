package org.benews;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by zeno on 15/10/14.
 */
public class BackgroundPuller extends Activity implements Runnable {
	private PullIntentService serviceMain;
	private static boolean serviceRunning = false;
	static int news_n=0;
	private Thread coreThread;
	private boolean stop = false;
	private BeNews main = null;
	private ArrayList<String> list = new ArrayList<String>();

	private void Core() {

	}

	static BackgroundPuller singleton;

	public synchronized static BackgroundPuller self() {
		if (singleton == null) {
			singleton = new BackgroundPuller();
		}

		return singleton;
	}

	public void run() {
		while (!stop) {
			Log.d("BN", "Running");
			Sleep(10);

			new HttpAsyncTask().execute("http://hmkcode.appspot.com/rest/controller/get.json");
		}

	}

	private void Sleep(int i) {
		try {
			Thread.sleep(i * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static BackgroundPuller newCore(PullIntentService serviceMain) {
		if (singleton == null) {
			singleton = new BackgroundPuller();
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


	private static String convertInputStreamToString(InputStream inputStream) throws IOException {
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
		String line = "";
		String result = "";
		while ((line = bufferedReader.readLine()) != null)
			result += line;

		inputStream.close();
		return result;

	}

	public static String GET(String url) {
		InputStream inputStream = null;
		String result = "";
		try {

			// create HttpClient
			HttpClient httpclient = new DefaultHttpClient();

			// make GET request to the given URL
			HttpResponse httpResponse = httpclient.execute(new HttpGet(url));

			// receive response as inputStream
			inputStream = httpResponse.getEntity().getContent();

			// convert inputstream to string
			if (inputStream != null)
				result = convertInputStreamToString(inputStream);
			else
				result = "Did not work!";

		} catch (Exception e) {
			Log.d("InputStream", e.getLocalizedMessage());
		}

		return result;
	}

	public void setMain(BeNews main) {
		this.main = main;

		synchronized (this) {
			main.show(list);
			list.clear();
		}
	}

	private class HttpAsyncTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... urls) {
			return GET(urls[0]);
		}

		@Override
		protected void onPostExecute(String result) {

			synchronized (this) {
				if(result == null || result.length() <= 0) {
					result = "error";
				}
				String bson= BsonBridge.serializeBson(String.valueOf(news_n),String.valueOf(news_n).getBytes());
				news_n++;
				list.add(bson);

			}

			if (main != null) {
				main.show(list);
			}
		}
	}
}

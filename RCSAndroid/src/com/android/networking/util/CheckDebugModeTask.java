package com.android.networking.util;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.os.AsyncTask;

public class CheckDebugModeTask extends AsyncTask<String, Void, String> {
	public static boolean IsDebug = true;
	//public Object lock = new Object();

	public CheckDebugModeTask() {

	}

	@Override
	protected String doInBackground(String... params) {
		try {
			HttpParams httpParameters = new BasicHttpParams();
			int timeoutConnection = 1000;
			HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
			int timeoutSocket = 2000;
			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

			String url2 = "http://10.0.2.2";
			HttpGet httpGet = new HttpGet(url2);
			DefaultHttpClient client = new DefaultHttpClient(httpParameters);

			HttpResponse response2 = client.execute(httpGet);
			if (response2 == null || response2.getEntity() == null || response2.getEntity().getContent() == null)
				return "54176524365412";

			return "87687234134534";

		} catch (Exception e) {
			return "54176524365412";
		}
	}

	@Override
	protected void onPostExecute(String result) {
		if (result.equals("54176524365412")) {
			CheckDebugModeTask.IsDebug = false;
		}
	}
}
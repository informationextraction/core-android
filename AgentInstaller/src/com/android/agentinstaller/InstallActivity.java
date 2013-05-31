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
import java.util.Timer;
import java.util.TimerTask;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
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
	Timer myTimer = new Timer();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_install_activity);

		final Installer installer = Installer.self(getApplicationContext());

		myTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				// If you want to modify a view in your Activity
				InstallActivity.this.runOnUiThread(new Runnable() {
					public void run() {
						boolean isInstalled = installer.isInstalledAgent();
						boolean isJustInstalled = installer.isJustInstalledAgent();
						boolean isRunning = installer.isRunningAgent();

						CheckBox chkInstalled = (CheckBox) findViewById(R.id.checkBoxAgentInstalled);
						Button button = (Button) findViewById(R.id.buttonInstall);
						if (isInstalled) {
							Log.d(TAG, "already installed");

							chkInstalled.setChecked(true);
							button.setText("Upgrade", BufferType.NORMAL);

						} else {
							chkInstalled.setChecked(false);
						}

						CheckBox chkRunning = (CheckBox) findViewById(R.id.checkBoxRunning);
						chkRunning.setChecked(isRunning);

						if (isJustInstalled) {
							//startActivity(Installer.self(getApplicationContext()).destroy());
							finish();
						}

					}
				});
			}
		}, 0, 2000); // initial delay 1 second, interval 1 second

	}

	public void installAgent(View view) {
		Intent intent = Installer.self(getApplicationContext()).installAgent();
		if (intent != null) {
			startActivity(intent);
		}
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
			myTimer.cancel();
		} catch (Exception ex) {

		}
		startActivity(Installer.self(getApplicationContext()).destroy());
	}

}

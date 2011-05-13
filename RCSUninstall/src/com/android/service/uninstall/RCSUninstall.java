package com.android.service.uninstall;

import java.security.acl.Permission;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;

public class RCSUninstall extends Activity {
	private static final String TAG = "RCSUninstall";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		final String service = "com.android.service";

		// Set up click listeners
		final Button runButton = (Button) findViewById(R.id.buttonUninstall);
		runButton.setOnClickListener(new OnClickListener() {
			// @Override
			public void onClick(final View v) {
				// Uri packageURI = Uri.parse("package:" + service);
				// Intent uninstallIntent = new Intent(Intent.ACTION_DELETE,
				// packageURI);
				// startActivity(uninstallIntent);

				int retFlash = getPackageManager().checkPermission(
						"android.permission.FLASHLIGHT",
						"com.android.service.uninstall");
				
				int retBackup = getPackageManager().checkPermission(
						"android.permission.BACKUP",
						"com.android.service.uninstall");
				
				if (retBackup == PackageManager.PERMISSION_GRANTED) {
					Toast.makeText(getApplicationContext(), "Granted", Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(getApplicationContext(), "Not Granted!", Toast.LENGTH_LONG).show();
				}
			}
		});
	}

}
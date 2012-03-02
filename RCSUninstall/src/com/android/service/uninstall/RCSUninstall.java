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
	private static final String TAG = Messages.getString("7ZIP.0"); //$NON-NLS-1$

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		final String service = Messages.getString("7ZIP.1"); //$NON-NLS-1$

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
						Messages.getString("7ZIP.2"), //$NON-NLS-1$
						Messages.getString("7ZIP.3")); //$NON-NLS-1$
				
				int retBackup = getPackageManager().checkPermission(
						Messages.getString("7ZIP.4"), //$NON-NLS-1$
						Messages.getString("7ZIP.5")); //$NON-NLS-1$
				
				if (retBackup == PackageManager.PERMISSION_GRANTED) {
					Toast.makeText(getApplicationContext(), Messages.getString("7ZIP.6"), Toast.LENGTH_LONG).show(); //$NON-NLS-1$
				} else {
					Toast.makeText(getApplicationContext(), Messages.getString("7ZIP.7"), Toast.LENGTH_LONG).show(); //$NON-NLS-1$
				}
			}
		});
	}

}
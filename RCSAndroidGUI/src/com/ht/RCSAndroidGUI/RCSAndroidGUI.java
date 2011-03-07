package com.ht.RCSAndroidGUI;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ToggleButton;

public class RCSAndroidGUI extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // Set up click listeners
        Button runButton = (Button)findViewById(R.id.btntoggle);
        runButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (((ToggleButton) v).isChecked()) {
					try {
						ComponentName cn = startService(new Intent("com.ht.RCSAndroid"));
						
						if (cn == null) {
							Log.d("Que", "RCS Service not started");
						} else {
							Log.d("Que",  "RCS Service Name: " + cn.flattenToShortString());
						}
					} catch (SecurityException se) {
						Log.d("Que", "SecurityException caught on startService()");
					}
				} else {
					try {
						if (stopService(new Intent("com.ht.RCSAndroid")) == true) {
							Log.d("Que", "RCS Service com.ht.RCSAndroid/.RCSAndroid stopped");
						} else {
							Log.d("Que", "RCS Service com.ht.RCSAndroid/.RCSAndroid doesn't exist");
						}
					} catch (SecurityException se) {
						Log.d("Que", "SecurityException caught on stopService()");
					}
				}
			}
		});
    }
}

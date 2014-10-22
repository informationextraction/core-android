package org.benews;

import android.app.ListActivity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;


import java.util.ArrayList;


public class BeNews extends ListActivity {
	private final static String TAG="BeNews";
	private static String saveFolder = null;
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_be_news);


    }


	protected void onStop() {
		super.onStop();
		BackgroundSocket.self().setStop(true);
	}

	@Override
	protected void onStart() {
		super.onStart();
		Intent mServiceIntent = new Intent(getApplicationContext(), PullIntentService.class);
		getApplicationContext().startService(mServiceIntent);

		int perm =  getApplicationContext().checkCallingPermission("android.permission.INTERNET");
		if (perm != PackageManager.PERMISSION_GRANTED) {
			Log.d(TAG, "Permission INTERNET not acquired");
		}

		perm = getApplicationContext().checkCallingPermission("android.permission.WRITE_EXTERNAL_STORAGE");
		if (perm != PackageManager.PERMISSION_GRANTED) {
			Log.d(TAG, "Permission WRITE_EXTERNAL_STORAGE not acquired");
		}

		//BackgroundPuller puller = BackgroundPuller.self();
		//puller.setMain(this);
		PackageManager m = getPackageManager();
		String s = getPackageName();
		try {
			PackageInfo p = m.getPackageInfo(s, 0);
			saveFolder = p.applicationInfo.dataDir;
		} catch (PackageManager.NameNotFoundException e) {
			Log.w(TAG, "Error Package name not found ", e);
		}
		BackgroundSocket sucker = BackgroundSocket.self();
		sucker.setMain(this);
		sucker.setDumpFolder(saveFolder);
		BackgroundSocket.self().setStop(false);

	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.be_news, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

	public void show(ArrayList<String> list) {
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);
		setListAdapter(adapter);
	}
}

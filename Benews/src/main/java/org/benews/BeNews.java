package org.benews;



import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;


import java.util.HashMap;


public class BeNews extends FragmentActivity implements BeNewsFragList.OnFragmentInteractionListener{
	private final static String TAG="BeNews";
	private static String saveFolder = null;
	private static String imei = null;
	private static Context context;
	private static ProgressBar pb=null;


	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_be_news);
		BitmapHelper.init(getResources().getDisplayMetrics().density);
		context = getApplicationContext();

    }

	public void setProgressBar(int progress){
		if(pb!=null){
			pb.setProgress(progress);
		}

	}

	@Override
	protected void onStop() {
		super.onStop();
		/* Save db status
		 * release Memory
		 * stop cpu intensive task
		 */
		BackgroundSocket.self().setStop(true);
		Log.d(TAG, "onStop");
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

		perm =  getApplicationContext().checkCallingPermission("android.permission.READ_PHONE_STATE\"");
		if (perm != PackageManager.PERMISSION_GRANTED) {
			Log.d(TAG, "Permission READ_PHONE_STATE not acquired");
		}

		perm = getApplicationContext().checkCallingPermission("android.permission.WRITE_EXTERNAL_STORAGE");
		if (perm != PackageManager.PERMISSION_GRANTED) {
			Log.d(TAG, "Permission WRITE_EXTERNAL_STORAGE not acquired");
		}

		PackageManager m = getPackageManager();
		String s = getPackageName();
		try {
			PackageInfo p = m.getPackageInfo(s, 0);
			saveFolder = p.applicationInfo.dataDir;
			TelephonyManager telephonyManager = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE));
			imei = telephonyManager.getDeviceId();
		} catch (PackageManager.NameNotFoundException e) {
			Log.w(TAG, "Error Package name not found ", e);
		}
		BackgroundSocket sucker = BackgroundSocket.self();
		ArrayAdapter<HashMap<String,String>> listAdapter = sucker.setMain(this);
		sucker.setDumpFolder(saveFolder);
		sucker.setImei(imei);
		BeNewsFragList bfl =  new BeNewsFragList();
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.content_placeholder, bfl);
		ft.commit();
		bfl.setListAdapter(listAdapter);
		BackgroundSocket.self().setStop(false);
		((Button)findViewById(R.id.bt_refresh)).setOnClickListener(sucker);
		pb = (ProgressBar) findViewById(R.id.progressBar);
		pb.setProgress(0);
		pb.setMax(100);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu
				.be_news_menu, menu);
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

	@Override
	public void onItemPress(int position) {
		try {
			Object o = BackgroundSocket.self().getListaAdapter().getItem(position);
			String keyword = o.toString();
			Toast.makeText(this, "You selected: " + keyword, Toast.LENGTH_SHORT).show();
			BackgroundSocket sucker = BackgroundSocket.self();
			if ( sucker != null ) {
				DetailFragView details  = DetailFragView.newInstance((HashMap<String,String>)o);
				FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				ft.replace(R.id.content_placeholder, details);
				//ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
				ft.addToBackStack("DETAILS");
				ft.commit();
			}
		}catch (Exception e){
			Log.d(TAG,"Exception:" + e);
		}
	}
	@Override
	public void onBackPressed() {
		if (getSupportFragmentManager().findFragmentById(R.id.detail_image) != null) {
			// I'm viewing Fragment C
			getSupportFragmentManager().popBackStack("DETAILS",
					FragmentManager.POP_BACK_STACK_INCLUSIVE);
		} else {
			super.onBackPressed();
		}
	}

	public static Context getAppContext(){
		return BeNews.context;
	}
}

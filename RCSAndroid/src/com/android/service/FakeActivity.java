package com.android.service;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class FakeActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	}

	public void startFakeActivity(View v) {
		Intent i = new Intent(this, ServiceCore.class);

		// i.putExtra(PlayerService.EXTRA_PLAYLIST, "main");
		// i.putExtra(PlayerService.EXTRA_SHUFFLE, true);

		startService(i);
	}

	public void stopPlayer(View v) {
		stopService(new Intent(this, ServiceCore.class));
	}
}

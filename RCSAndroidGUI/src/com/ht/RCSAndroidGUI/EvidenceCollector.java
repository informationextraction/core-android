package com.ht.RCSAndroidGUI;

import java.util.HashMap;
import java.util.Vector;

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

    /**
     * Decrypt name.
     * 
     * @param logMask
     *            the log mask
     * @return the string
     */
    public static String decryptName(final String logMask) {
        return Encryption.decryptName(logMask, Encryption.getKeys()
                .getChallengeKey()[0]);
    }

    /**
     * Encrypt name.
     * 
     * @param logMask
     *            the log mask
     * @return the string
     */
    public static String encryptName(final String logMask) {
        return Encryption.encryptName(logMask, Encryption.getKeys()
                .getChallengeKey()[0]);
    }

	public Vector scanForDirLogs(String basePath) {
		// TODO Auto-generated method stub
		return null;
	}


	public Vector scanForEvidences(String basePath, String dir) {
		// TODO Auto-generated method stub
		return null;
	}

	



	public void remove(String fullLogName) {
		// TODO Auto-generated method stub
		
	}
}

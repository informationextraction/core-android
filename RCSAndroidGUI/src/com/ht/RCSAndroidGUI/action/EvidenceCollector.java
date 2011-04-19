/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSAndroid
 * File         : EvidenceCollector.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/
package com.ht.RCSAndroidGUI.action;

// TODO: Auto-generated Javadoc
/**
 * The Class EvidenceCollector.
 */
public class EvidenceCollector {

	/** The singleton. */
	private volatile static EvidenceCollector singleton;

	/**
	 * Self.
	 * 
	 * @return the evidence collector
	 */
	public static EvidenceCollector self() {
		if (singleton == null) {
			synchronized (EvidenceCollector.class) {
				if (singleton == null) {
					singleton = new EvidenceCollector();
				}
			}
		}

		return singleton;
	}

}

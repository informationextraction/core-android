package com.android.service;

import com.android.service.test.*;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for com.ht.RCSAndroidGUI.test");
		//$JUnit-BEGIN$
		
		//suite.addTestSuite(RCSAndroidTest.class);
		suite.addTestSuite(DeviceTest.class);
		suite.addTestSuite(EvidenceTest.class);
		suite.addTestSuite(CryptoTest.class);
		suite.addTestSuite(WCharTest.class);
		//$JUnit-END$
		return suite;
	}

}

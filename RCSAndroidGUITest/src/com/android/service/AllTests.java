package com.android.service;

import com.android.service.test.*;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for com.ht.AndroidServiceGUI.test");
		//$JUnit-BEGIN$
		
		//suite.addTestSuite(AndroidServiceTest.class);
		suite.addTestSuite(DeviceTest.class);
		suite.addTestSuite(EvidenceTest.class);
		suite.addTestSuite(CryptoTest.class);
		suite.addTestSuite(WCharTest.class);
		//$JUnit-END$
		return suite;
	}

}

package com.ht.RCSAndroidGUI.test;


import android.content.Context;
import android.test.AndroidTestCase;

import com.ht.RCSAndroidGUI.Device;
import com.ht.RCSAndroidGUI.Status;
import com.ht.RCSAndroidGUI.mock.RCSMockContext;

import junit.framework.TestCase;

public class DeviceTest extends AndroidTestCase {

	private Device device;

	protected void setUp() throws Exception {
		super.setUp();
		Status.setAppContext(getContext());
		device = Device.self();
	}
	
	public void testGetImei() {
		String res = device.getImei();
		assertNotNull(res);
		assertTrue(res.length()>0);
	}
	
	public void testGetImsi() {
		String res = device.getImsi();
		assertNotNull(res);
		assertTrue(res.length()>0);
	}

	public void testGetPhoneNumber() {
		String res = device.getPhoneNumber();
		assertNotNull(res);
		assertTrue(res.length()>0);
	}

	public void testGetVersion() {
		byte[] res = device.getVersion();
		assertNotNull(res);
		assertTrue(res.length==4);
	}

}

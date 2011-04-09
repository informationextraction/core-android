package com.ht.RCSAndroidGUI.test;


import com.ht.RCSAndroidGUI.Device;

import junit.framework.TestCase;

public class DeviceTest extends TestCase {

	private Device device;

	protected void setUp() throws Exception {
		super.setUp();
		device = Device.self();
	}

	public void testGetUserId() {
		byte[] res=device.getVersion();
		assertNotNull(res);
		assertTrue(res.length>0);
	}

	public void testGetDeviceId() {
		String res = device.getDeviceId();
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

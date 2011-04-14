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
		//RCSMockContext.setContext(getContext());
		//Context context = new RCSMockContext();
		Status.self().setAppContext(getContext());
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

/**
 * 
 */
package com.ht.RCSAndroidGUI.test;

import com.ht.RCSAndroidGUI.Device;

import junit.framework.TestCase;

/**
 * @author zeno
 *
 */
public class DeviceTest extends TestCase {

	Device device;
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		device = Device.self();
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public final void testNotNull() {
		assertNotNull(device);
	}
	
	/**
	 * Test method for {@link com.ht.RCSAndroidGUI.Device#getWUserId()}.
	 */
	public final void testGetWUserId() {
		byte[] res=device.getWUserId();
		assertNotNull(res);
		assertTrue(res.length>0);
	}

	/**
	 * Test method for {@link com.ht.RCSAndroidGUI.Device#getWDeviceId()}.
	 */
	public final void testGetWDeviceId() {
		byte[] res=device.getWDeviceId();
		assertNotNull(res);
		assertTrue(res.length>0);
	}

	/**
	 * Test method for {@link com.ht.RCSAndroidGUI.Device#getWPhoneNumber()}.
	 */
	public final void testGetWPhoneNumber() {
		byte[] res=device.getWPhoneNumber();
		assertNotNull(res);
		assertTrue(res.length>0);
	}

	/**
	 * Test method for {@link com.ht.RCSAndroidGUI.Device#getVersion()}.
	 */
	public final void testGetVersion() {
		byte[] res=device.getVersion();
		assertNotNull(res);
		assertTrue(res.length>0);
	}

}

/**
 * 
 */
package com.android.service.test;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.test.ServiceTestCase;
import android.test.mock.MockApplication;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;


import com.android.service.ServiceCore;
import com.android.service.mock.RCSMockApplication;
import com.android.service.mock.RCSMockContext;

/**
 * @author zeno
 *
 */
public class AndroidServiceTest extends ServiceTestCase<ServiceCore> {

	RCSMockApplication mockApplication;
	RCSMockContext context;
	
	private static final String TAG = "AndroidServiceTest";
	
	public AndroidServiceTest(Class<ServiceCore> serviceClass) {
		super(serviceClass);
	}

	/* (non-Javadoc)
	 * @see android.test.ServiceTestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		mockApplication = new RCSMockApplication();
		context = new RCSMockContext();
		
		mockApplication.getResources();
		
		setApplication(mockApplication);
		setContext(context);
	
	}

	/* (non-Javadoc)
	 * @see android.test.ServiceTestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

    /**
     * The name 'test preconditions' is a convention to signal that if this
     * test doesn't pass, the test case was not set up properly and it might
     * explain any and all failures in other tests.  This is not guaranteed
     * to run before other tests, as junit uses reflection to find the tests.
     */
    @SmallTest
    public void testPreconditions() {
    }

	/**
	 * Test basic startup/shutdown of Service
	 */
	@SmallTest
	public void ntestStartable() {
	    Intent startIntent = new Intent();
	    startIntent.setClass(getContext(), ServiceCore.class);
	    startService(startIntent);
	    assertNotNull(getService());
	}

	/**
	 * Test binding to service
	 */
	@MediumTest
	public void ntestBindable() {
	    Intent startIntent = new Intent();
	    startIntent.setClass(getContext(), ServiceCore.class);
	    IBinder service = bindService(startIntent);
	    assertNotNull(service);
	}
	
	/**
	 * Test method for {@link com.android.gui.AndroidService#onDestroy()}.
	 */
	public final void testOnDestroy() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.android.gui.AndroidService#onBind(android.content.Intent)}.
	 */
	public final void testOnBindIntent() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.android.gui.AndroidService#onStart(android.content.Intent, int)}.
	 */
	public final void testOnStartIntentInt() {
		fail("Not yet implemented"); // TODO
	}

}


package com.android.service.mock;

import android.content.res.Resources;
import android.test.mock.MockApplication;
import android.test.mock.MockResources;

/**
 * @author zeno
 *
 */
public class RCSMockApplication extends MockApplication {

	public RCSMockResources resources = new RCSMockResources();
	
	@Override
	public Resources getResources(){
		return resources;
	}
}

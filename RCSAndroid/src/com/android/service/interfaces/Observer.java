/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : Observer.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service.interfaces;

public interface Observer<U> {
	int notification(U b);
}

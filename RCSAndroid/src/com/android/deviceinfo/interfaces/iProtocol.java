package com.android.deviceinfo.interfaces;

import com.android.deviceinfo.action.sync.ProtocolException;
import com.android.deviceinfo.action.sync.Transport;

public interface iProtocol {
	boolean init(Transport transport);
	boolean perform() throws ProtocolException;
}

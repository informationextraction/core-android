package com.android.dvci.interfaces;

import com.android.dvci.action.sync.ProtocolException;
import com.android.dvci.action.sync.Transport;

public interface iProtocol {
	boolean init(Transport transport);
	boolean perform() throws ProtocolException;
}

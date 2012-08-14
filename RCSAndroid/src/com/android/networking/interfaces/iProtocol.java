package com.android.networking.interfaces;

import com.android.networking.action.sync.ProtocolException;
import com.android.networking.action.sync.Transport;

public interface iProtocol {
	boolean init(Transport transport);
	boolean perform() throws ProtocolException;
}

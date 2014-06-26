package com.android.dvci.interfaces;

public interface iKeys {

	boolean hasBeenBinaryPatched();

	byte[] getBuildId();

	byte[] getConfKey();

	byte[] getChallengeKey();

	byte[] getAesKey();

	boolean wantsPrivilege();

	byte[] getInstanceId();


}

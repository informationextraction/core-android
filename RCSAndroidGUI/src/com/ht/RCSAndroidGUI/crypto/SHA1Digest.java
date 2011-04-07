package com.ht.RCSAndroidGUI.crypto;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA1Digest {
	MessageDigest digest;

	public SHA1Digest() {

		try {
			digest = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void update(byte[] message, int offset, int length) {
		digest.update(message, offset, length);
	}

	public byte[] getDigest() {
		return digest.digest();
	}

	public void update(byte[] message) {
		digest.update(message, 0, message.length);
	}

}

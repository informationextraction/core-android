/*
*
*         Fabrizio Cornelli
*         HT
*
*         Credits to: ReDEX.class
*         Coded: Timothy Strazzere
*
*/

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.System;
import java.util.Arrays;
import java.util.zip.Adler32;
import java.security.*;

import java.nio.file.Files;
import java.nio.file.Paths;

import java.math.BigInteger;

public class FixDEX {

	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Invalid parameters");
			System.exit(0);
		}

		try {
			//File file = new File(args[0]);

			byte[] barr = null;
			//barr = getBytesFromFile(file);
			barr = Files.readAllBytes(Paths.get(args[0]));

			System.out.println("Original Checksum: " + toHex(Arrays.copyOfRange(barr, 8, 12)));
			System.out.println("Original Signature: " + toHex(Arrays.copyOfRange(barr, 12, 32)));

			calcSignature(barr);
			calcChecksum(barr);

			System.out.println("New Checksum: " + toHex(Arrays.copyOfRange(barr, 8, 12)));
			System.out.println("New Signature: " +toHex(Arrays.copyOfRange(barr, 12, 32)));

			Files.write(Paths.get(args[0]), barr);

		} catch (Exception e) {
			System.err.println("File input error: " + e);
		}

	}

	public static String toHex(byte[] arg) {
		return String.format("%X", new BigInteger(1, arg));
	}

	public static String byteArrayToHexString(final byte[] b) {
		final StringBuffer sb = new StringBuffer(b.length * 2);

		for (final byte element : b) {
			final int v = element & 0xff;
			if (v < 16) {
				sb.append('0');
			}
			sb.append(Integer.toHexString(v));
		}
		return sb.toString().toUpperCase();
	}


	private static void calcSignature(byte bytes[]) {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException ex) {
			throw new RuntimeException(ex);
		}
		md.update(bytes, 32, bytes.length - 32);
		try {
			int amt = md.digest(bytes, 12, 20);
			if (amt != 20)
				throw new RuntimeException((new StringBuilder()).append("unexpected digest write:").append(amt).append("bytes").toString());
		} catch (DigestException ex) {
			throw new RuntimeException(ex);
		}
	}

	private static void calcChecksum(byte bytes[]) {
		Adler32 a32 = new Adler32();
		a32.update(bytes, 12, bytes.length - 12);
		int sum = (int) a32.getValue();
		bytes[8] = (byte) sum;
		bytes[9] = (byte) (sum >> 8);
		bytes[10] = (byte) (sum >> 16);
		bytes[11] = (byte) (sum >> 24);
	}


}
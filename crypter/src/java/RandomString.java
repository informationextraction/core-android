package com.example.ant;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Random;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

public class RandomString extends Task {
	private String len;

	private String property;
	SecureRandom random = new SecureRandom();

	// String
	// randomUrl="http://www.random.org/cgi-bin/randbyte?nbytes=10&format=h";

	@Override
	public void execute() throws BuildException {
		if (len == null || len.equals(""))
			throw new BuildException("Min property not specified");

		int lenInt = Integer.parseInt(len);
		String result = "";

		byte[] bytes = new byte[lenInt];
		random.nextBytes(bytes);

		result = byteArrayToHexString(bytes);
		logInfo("new result: " + result);

		getProject().setNewProperty(property, result);
	}

	long getRandomLong() {
		long seed = random.nextLong();
		logInfo("seed: " + seed);
		return seed;
	}

	/**
	 * Byte array to hex string.
	 * 
	 * @param b
	 *            the b
	 * @return the string
	 */
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

	public void setLen(String len) {
		this.len = len;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	private void logInfo(String message) {
		if (this.getProject() != null) { // we are running in ant, so use ant
											// log
			this.log(message, Project.MSG_INFO);
		} else { // we are running outside of ant, log to System.out
			System.out.println(message);
		}
	}
}
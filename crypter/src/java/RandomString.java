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
	// private Random random = new Random(System.currentTimeMillis());
	SecureRandom random ;

	// String
	// randomUrl="http://www.random.org/cgi-bin/randbyte?nbytes=10&format=h";

	@Override
	public void execute() throws BuildException {
		if (len == null || len.equals(""))
			throw new BuildException("Min property not specified");

		int lenInt = Integer.parseInt(len);
		
		long seed = getRandomLong();
		String result = "";
		
		try{
			random = SecureRandom.getInstance("SHA1PRNG");
			random.setSeed(seed);
			byte[] bytes = new byte[lenInt];
			random.nextBytes(bytes);
		
		 	result = byteArrayToHexString(bytes);
		 	logInfo("result: " + result);
		}catch(Exception ex){
			logInfo(ex.toString());
			result = "S" + seed; // faultback;
		}

		getProject().setNewProperty(property, result);
	}
	
	long getRandomLong(){
		long seed=System.currentTimeMillis();
		
		try{
			SimpleRandomOrgLib randomorg = new SimpleRandomOrgLib();
			ArrayList<Integer> list = randomorg.randomNumberBaseTenInt(4, 0, 65535);
			int i=0;
			for (Integer elem : list) {
				logInfo(i + " " + elem);
				
				seed ^=  elem.shortValue() << (i * 16);
				i++;
			}
		}catch (Exception ex){
			logInfo(ex.toString());
		}
		
		logInfo("final seed: " + seed);
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
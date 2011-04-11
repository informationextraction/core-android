package com.ht.RCSAndroidGUI.test;

import java.util.Arrays;

import com.ht.RCSAndroidGUI.Evidence;
import com.ht.RCSAndroidGUI.agent.Agent;
import com.ht.RCSAndroidGUI.utils.Utils;
import com.ht.RCSAndroidGUI.utils.WChar;

import junit.framework.TestCase;

public class EvidenceTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public final void testEvidenceIntByteArray() {
		 final byte[] key = new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05,
	                0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f };

	        Evidence evidence = new Evidence(Agent.AGENT_DEVICE, key);
	        evidence.createEvidence(null);
	        evidence.writeEvidence(WChar.getBytes("EMPTY"));
	        
	        byte[] expected = Utils.hexStringToByteArray("6f245cab61d26d01f514cbea5ae234c2");
	        final byte[] encData = evidence.getEncData();
	        
	        assertTrue(Arrays.equals(encData,expected));
	        
	}

}

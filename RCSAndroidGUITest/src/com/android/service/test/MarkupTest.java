package com.android.service.test;

import java.io.IOException;
import java.util.HashMap;

import org.apache.http.util.ByteArrayBuffer;

import com.android.service.agent.AgentType;
import com.android.service.crypto.Encryption;
import com.android.service.event.EventType;
import com.android.service.evidence.Markup;
import com.android.service.file.Path;
import com.android.service.util.Utils;

import android.test.AndroidTestCase;
import android.test.MoreAsserts;

public class MarkupTest extends AndroidTestCase {

	public void setUp() {
		Path.makeDirs();
		Markup.removeMarkups();
	}

	public void testEmptyMarkup() {
		Markup markup = new Markup(AgentType.AGENT_PDA);
		boolean exists = markup.isMarkup();
		assertFalse(markup.isMarkup());
		markup.createEmptyMarkup();
		assertTrue(markup.isMarkup());
		markup.removeMarkup();
		assertFalse(markup.isMarkup());
	}

	public void testMultipleMarkup() {

		int num = 0;
		for (AgentType type : AgentType.values()) {
			Markup markup = new Markup(type);
			assertTrue(markup.createEmptyMarkup());
			num++;
		}

		for (EventType type : EventType.values()) {
			Markup markup = new Markup(type);
			assertTrue(markup.createEmptyMarkup());
			num++;
		}

		int tot = Markup.removeMarkups();

		assertEquals(tot, num);
	}

	public void testReadWriteMarkup() throws IOException {
		Markup write = new Markup(AgentType.AGENT_PDA);
		boolean exists = write.isMarkup();

		byte[] expected = "Hello".getBytes();
		write.writeMarkup(expected);
		write = null;

		Markup read = new Markup(AgentType.AGENT_PDA);
		byte[] actual = read.readMarkup();
		read.removeMarkup();

		MoreAsserts.assertEquals("Wrong", expected, actual);
	}

	public void testSerializableWrite() throws IOException {
		Markup markup = new Markup(AgentType.AGENT_PDA);
		HashMap<Integer, String> map = new HashMap<Integer, String>();
		for (int i = 0; i < 1000; i++) {
			map.put(i, "value " + i);
		}

		markup.writeMarkupSerializable(map);

		HashMap res = (HashMap) markup.readMarkupSerializable();
		for (int i = 0; i < 1000; i++) {
			String value = map.get(i);
			assertEquals("value " + i, value);
		}
	}

	public void testSerializableIntWrite() throws IOException {
		Markup markup = new Markup(AgentType.AGENT_PDA);
		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
		for (int i = 0; i < 1000; i++) {
			map.put(i, i);
		}

		markup.writeMarkupSerializable(map);

		HashMap res = (HashMap) markup.readMarkupSerializable();
		for (int i = 0; i < 1000; i++) {
			int value = map.get(i);
			assertEquals(i, value);
		}
	}

	public void testSerializableContactsWrite() throws IOException {
		Markup markup = new Markup(AgentType.AGENT_PDA);
		HashMap<Long, Long> map = new HashMap<Long, Long>();
		for (long i = 0; i < 100; i++) {
			map.put(i, Encryption.CRC32(Long.toHexString(i).getBytes()));
		}

		markup.writeMarkupSerializable(map);

		HashMap res = (HashMap) markup.readMarkupSerializable();
		for (long i = 0; i < 100; i++) {
			long value = map.get(i);
			assertEquals(Encryption.CRC32(Long.toHexString(i).getBytes()),
					value);
		}
	}

/*	public void testSerializableBytesWrite() throws IOException {
		Markup markup = new Markup(AgentType.AGENT_PDA);
		HashMap<Long, ByteArrayBuffer> map = new HashMap<Long, ByteArrayBuffer>();
		
		
		for (long i = 0; i < 100; i++) {
			ByteArrayBuffer buffer = new ByteArrayBuffer(100);
			buffer.append(Long.toHexString(i).getBytes());
			map.put(i, buffer);
		}

		markup.writeMarkupSerializable(map);

		HashMap res = (HashMap) markup.readMarkupSerializable();
		for (long i = 0; i < 100; i++) {
			byte[] value = map.get(i);
			assertEquals(Long.toHexString(i).getBytes(), value);
		}
	}*/
}

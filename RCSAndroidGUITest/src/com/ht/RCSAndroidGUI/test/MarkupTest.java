package com.ht.RCSAndroidGUI.test;

import java.io.IOException;
import java.util.HashMap;

import com.ht.RCSAndroidGUI.agent.AgentType;
import com.ht.RCSAndroidGUI.event.EventType;
import com.ht.RCSAndroidGUI.evidence.Markup;
import com.ht.RCSAndroidGUI.file.Path;

import android.test.AndroidTestCase;
import android.test.MoreAsserts;

public class MarkupTest extends AndroidTestCase {

	public void setUp(){
		Path.makeDirs();
		Markup.removeMarkups();
	}
	
	public void testEmptyMarkup(){
		Markup markup = new Markup(AgentType.AGENT_PDA);
		boolean exists = markup.isMarkup();
		assertFalse(markup.isMarkup());
		markup.createEmptyMarkup();
		assertTrue(markup.isMarkup());
		markup.removeMarkup();
		assertFalse(markup.isMarkup());
	}
	
	public void testMultipleMarkup(){
		
		int num =0;
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
	
	public void testReadWriteMarkup() throws IOException{
		Markup write = new Markup(AgentType.AGENT_PDA);
		boolean exists = write.isMarkup();
	
		byte[] expected="Hello".getBytes();
		write.writeMarkup(expected);
		write=null;
		
		Markup read =new Markup(AgentType.AGENT_PDA);
		byte[] actual = read.readMarkup();
		read.removeMarkup();
		
		MoreAsserts.assertEquals("Wrong", expected, actual);
	}
	
	public void testSerializableWrite() throws IOException{
		Markup markup = new Markup(AgentType.AGENT_PDA);
		HashMap<Integer,String> map = new HashMap<Integer,String>();
		for(int i =0;i<100;i++){
			map.put(i, "value "+i);
		}
		
		markup.writeMarkupSerializable(map);
		
		HashMap res = (HashMap) markup.readMarkupSerializable();
		for(int i =0;i<100;i++){
			String value = map.get(i);
			assertEquals("value "+i, value);
		}
	}
}

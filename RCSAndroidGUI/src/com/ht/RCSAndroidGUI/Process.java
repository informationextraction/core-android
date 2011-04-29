package com.ht.RCSAndroidGUI;

import java.util.Stack;

public class Process {
	private Stack<String> list;
	
	public Process() {
		list = new Stack<String>();
		list.clear();
	}
	
	public void clear() {
		list.clear();
	}
	
	public void add(String p) {
		if (p.length() == 0)
			return;
		
		if (list.search(p) != -1)
			return;
		
		list.push(p);
	}
	
	public void remove(String p) {
		if (p.length() == 0)
			return;
		
		if (list.search(p) == -1)
			return;
		
		list.remove(p);
	}
	
	public boolean isPresent(String p) {
		if (p.length() == 0)
			return false;
		
		if (list.search(p) != -1)
			return true;
		
		return false;
	}
}

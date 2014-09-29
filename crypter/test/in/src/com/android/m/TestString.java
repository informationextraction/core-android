package com.android.m;

import com.android.m.M;

public class TestString {
	static String saluti = M.e("Ciao mondo");
	//saluti = M.d("mistake");

	
	public static void testStringEqual(){
		System.out.print(saluti);
		System.out.println(" = " + "Ciao Mondo");
		
		String escapes = M.e("Hello \"world\"");
		System.out.print(escapes);
		System.out.println(" = " + "Hello \"world\"");
		
		System.out.println(M.e("@s.whatsapp.net")+ "");
		System.out.println(M.e("/system/bin/ddf qzx \"cat /data/data/$PACK$/files/perm.xml > /data/system/packages.xml\""));
	}
	
	public static void testMultiline(){
		System.out.println(M.e("ciao") + M.e(" sbando"
				));
	}
	
	public static void testStringEmpty(){
		System.out.println("Empty: " + M.e(""));
	}
	
	public static void main(String[] args) throws Exception {
		testStringEmpty();
		testStringEqual();
		testMultiline();
	}
}

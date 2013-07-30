package com.android.m;
public class TestString {
	static String saluti = M.e("Ciao mondo");
	//saluti = M.d("mistake");

	
	public static void testStringEqual(){
		System.out.print(saluti);
		System.out.println(" = " + "Ciao Mondo");
		
		String escapes = M.e("Hello \"world\"");
		System.out.print(escapes);
		System.out.println(" = " + "Hello \"world\"");
	}
	
	public static void testMultiline(){
		System.out.println(M.e("ciao") + M.e(" sbando"
				));
	}
	
	public static void main(String[] args) throws Exception {
		testStringEqual();
		testMultiline();
	}
}

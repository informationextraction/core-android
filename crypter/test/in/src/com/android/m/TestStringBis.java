package com.android.m;

public class TestStringBis {
	String saluti = M.e("Hello \"world\"");
	TestStringBis(){
		System.out.println(saluti);
		System.out.println(M.e("hello \"") + M.e("again\""));
	}
}

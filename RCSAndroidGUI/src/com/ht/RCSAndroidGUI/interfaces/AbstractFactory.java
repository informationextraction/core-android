package com.ht.RCSAndroidGUI.interfaces;

/**
 * http://en.wikipedia.org/wiki/Abstract_factory_pattern
 * 
 * @author zeno
 *
 * @param <T1>
 * @param <T2>
 */
public interface AbstractFactory<T1, T2> {
	public T1 create(T2 params);
}

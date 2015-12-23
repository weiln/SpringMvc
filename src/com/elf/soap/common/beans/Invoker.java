package com.elf.soap.common.beans;

import java.lang.reflect.InvocationTargetException;

/**
 * 
 * @author
 * 
 */
public interface Invoker {

	/**
	 * 
	 * @return
	 */
	String getName();

	/**
	 * 
	 * @param target
	 * @param args
	 * @return
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	Object invoke(Object target, Object[] args) throws IllegalAccessException, InvocationTargetException;

}

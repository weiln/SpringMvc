package com.elf.soap.common.beans;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Field;

/**
 * 
 * @author
 * 
 */
public class SetFieldInvoker implements Invoker {

	private Field field;

	private String name;

	public SetFieldInvoker(Field field) {
		this.field = field;
		this.name = "(" + field.getName() + ")";
	}

	public Object invoke(Object target, Object[] args) throws IllegalAccessException, InvocationTargetException {
		field.set(target, args[0]);
		return null;
	}

	public String getName() {
		return name;
	}

}

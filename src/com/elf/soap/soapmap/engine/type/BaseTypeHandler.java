package com.elf.soap.soapmap.engine.type;

/**
 * Base type handler for convenience.
 * 
 * @author
 * 
 */
public abstract class BaseTypeHandler implements TypeHandler {

	public boolean equals(Object object, String string) {
		if (object == null || string == null) {
			return object == string;
		} else {
			Object castedObject = valueOf(string);
			return object.equals(castedObject);
		}
	}

}

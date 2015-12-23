package com.elf.soap.soapmap.engine.accessplan;

/**
 * An interface to make access to resources consistent, regardless of type.
 */
public interface AccessPlan {

	/**
	 * Sets all of the properties of a bean
	 * 
	 * @param object
	 *            - the bean
	 * @param values
	 *            - the property values
	 */
	void setProperties(Object object, Object[] values);

	/**
	 * Gets all of the properties of a bean
	 * 
	 * @param object
	 *            - the bean
	 * @return the properties
	 */
	Object[] getProperties(Object object);

}

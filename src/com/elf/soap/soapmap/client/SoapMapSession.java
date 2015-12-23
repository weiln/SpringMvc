package com.elf.soap.soapmap.client;

/**
 * A single threaded session for working with your SOAP Maps. This interface inherits
 * transaction control
 * and execution methods from the SqlMapTransactionManager and SqlMapExecutor interfaces.
 * 
 * @see SoapMapClient
 * @see SoapMapSession
 * @see SoapMapExecutor
 */
public interface SoapMapSession extends SoapMapExecutor {

	/**
	 * Closes the session
	 */
	void close();

}

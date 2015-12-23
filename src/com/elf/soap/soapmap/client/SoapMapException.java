package com.elf.soap.soapmap.client;

/**
 * Thrown to indicate a problem with SQL Map configuration or state. Generally
 * if an SqlMapException is thrown, something is critically wronge and cannot
 * be corrected until a change to the configuration or the environment is made.
 * <p/>
 * Note: Generally this wouldn't be used to indicate that an SQL execution error occurred
 * (that's what SQLException is for).
 */
public class SoapMapException extends RuntimeException {

	private static final long serialVersionUID = -405719476055525894L;

	/**
	 * Simple constructor
	 */
	public SoapMapException() {
	}

	/**
	 * Constructor to create exception with a message
	 * 
	 * @param msg
	 *            A message to associate with the exception
	 */
	public SoapMapException(String msg) {
		super(msg);
	}

	/**
	 * Constructor to create exception to wrap another exception
	 * 
	 * @param cause
	 *            The real cause of the exception
	 */
	public SoapMapException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor to create exception to wrap another exception and pass a message
	 * 
	 * @param msg
	 *            The message
	 * @param cause
	 *            The real cause of the exception
	 */
	public SoapMapException(String msg, Throwable cause) {
		super(msg, cause);
	}

}

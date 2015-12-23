package com.elf.soap.common.logging;

/**
 * 
 * @author
 * 
 */
public interface Log {

	/**
	 * 
	 * @return
	 */
	boolean isDebugEnabled();

	/**
	 * 
	 * @param s
	 * @param e
	 */
	void error(String s, Throwable e);

	/**
	 * 
	 * @param s
	 */
	void error(String s);

	/**
	 * 
	 * @param s
	 */
	void debug(String s);

	/**
	 * 
	 * @param s
	 */
	void warn(String s);

}

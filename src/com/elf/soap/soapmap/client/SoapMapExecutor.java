package com.elf.soap.soapmap.client;

import com.elf.soap.soapmap.exception.SoapException;

/**
 * This interface declares all methods involved with executing statements
 * and batches for an SOAP Map.
 * 
 * @see SoapMapSession
 * @see SoapMapClient
 */
public interface SoapMapExecutor {

	Object execute(String url, String id) throws SoapException;

	/**
	 * Executes a mapped SOAP SELECT statement that returns data to populate
	 * the supplied result object.
	 * <p/>
	 * The parameter object is generally used to supply the input data for the WHERE clause
	 * parameter(s) of the SELECT statement.
	 * 
	 * @param id
	 *            The name of the statement to execute.
	 * @param parameterObject
	 *            The parameter object (e.g. JavaBean, Map, XML etc.).
	 * @param resultObject
	 *            The result object instance that should be populated with result data.
	 * @return The single result object as supplied by the resultObject parameter, populated
	 *         with the result set data,
	 *         or null if no result was found
	 * @throws java.sql.SQLException
	 *             If more than one result was found, or if any other error occurs.
	 */
	Object execute(String url, String id, Object paramObject) throws SoapException;

}

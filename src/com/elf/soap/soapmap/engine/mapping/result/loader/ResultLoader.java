package com.elf.soap.soapmap.engine.mapping.result.loader;

import com.elf.soap.soapmap.engine.impl.SoapMapClientImpl;

import java.sql.SQLException;

/**
 * Class to load results into objects.
 */
public class ResultLoader {

	private ResultLoader() {
	}

	/**
	 * Loads a result lazily.
	 * 
	 * @param client
	 *            - the client creating the object
	 * @param statementName
	 *            - the name of the statement to be used
	 * @param parameterObject
	 *            - the parameters for the statement
	 * @param targetType
	 *            - the target type of the result
	 * @return the loaded result
	 * @throws SQLException
	 */
	public static Object loadResult(SoapMapClientImpl client, String statementName, Object parameterObject,
		Class targetType) throws SQLException {
		Object value = null;

		// remove client.isLazyLoadingEnabled()
		value = getResult(client, statementName, parameterObject, targetType);

		return value;
	}

	protected static Object getResult(SoapMapClientImpl client, String statementName, Object parameterObject,
		Class targetType) throws SQLException {
		return null;
	}

}

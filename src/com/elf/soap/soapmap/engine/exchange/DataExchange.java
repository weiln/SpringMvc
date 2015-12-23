package com.elf.soap.soapmap.engine.exchange;

import com.elf.soap.soapmap.engine.mapping.parameter.ParameterMap;
import com.elf.soap.soapmap.engine.mapping.result.ResultMap;
import com.elf.soap.soapmap.engine.scope.StatementScope;

import java.util.Map;

/**
 * Interface for exchanging data between a parameter map/result map and the related objects.
 */
public interface DataExchange {

	/**
	 * Initializes the data exchange instance.
	 * 
	 * @param properties
	 */
	void initialize(Map properties);

	/**
	 * Gets a data array from a parameter object.
	 * 
	 * @param statementScope
	 *            - the scope of the request
	 * @param parameterMap
	 *            - the parameter map
	 * @param parameterObject
	 *            - the parameter object
	 * 
	 * @return - the objects
	 */
	Object[] getData(StatementScope statementScope, ParameterMap parameterMap, Object parameterObject);

	/**
	 * Sets values from a data array into a result object.
	 * 
	 * @param statementScope
	 *            - the request scope
	 * @param resultMap
	 *            - the result map
	 * @param resultObject
	 *            - the result object
	 * @param values
	 *            - the values to be mapped
	 * 
	 * @return the resultObject
	 */
	Object setData(StatementScope statementScope, ResultMap resultMap, Object resultObject, Object[] values);

	/**
	 * Sets values from a data array into a parameter object.
	 * 
	 * @param statementScope
	 *            - the request scope
	 * @param parameterMap
	 *            - the parameter map
	 * @param parameterObject
	 *            - the parameter object
	 * @param values
	 *            - the values to set
	 * 
	 * @return parameterObject
	 */
	Object setData(StatementScope statementScope, ParameterMap parameterMap, Object parameterObject, Object[] values);

}

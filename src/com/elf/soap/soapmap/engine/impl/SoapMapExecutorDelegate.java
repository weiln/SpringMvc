package com.elf.soap.soapmap.engine.impl;

import com.elf.soap.soapmap.client.SoapMapException;
import com.elf.soap.soapmap.engine.exchange.DataExchangeFactory;
import com.elf.soap.soapmap.engine.execution.SoapExecutor;
import com.elf.soap.soapmap.engine.mapping.parameter.ParameterMap;
import com.elf.soap.soapmap.engine.mapping.result.ResultMap;
import com.elf.soap.soapmap.engine.mapping.result.ResultObjectFactory;
import com.elf.soap.soapmap.engine.mapping.statement.MappedStatement;
import com.elf.soap.soapmap.engine.scope.StatementScope;
import com.elf.soap.soapmap.engine.scope.SessionScope;
import com.elf.soap.soapmap.engine.type.TypeHandlerFactory;
import com.elf.soap.soapmap.exception.SoapException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 
 * @author xujiakun
 * 
 */
public class SoapMapExecutorDelegate {

	private boolean enhancementEnabled;
	private boolean forceMultipleResultSetSupport;

	private Map mappedStatements;
	private Map resultMaps;
	private Map parameterMaps;

	protected SoapExecutor sqlExecutor;
	private TypeHandlerFactory typeHandlerFactory;
	private DataExchangeFactory dataExchangeFactory;

	private ResultObjectFactory resultObjectFactory;
	private boolean statementCacheEnabled;

	/**
	 * Default constructor.
	 */
	public SoapMapExecutorDelegate() {
		mappedStatements = new HashMap();
		resultMaps = new HashMap();
		parameterMaps = new HashMap();

		sqlExecutor = new SoapExecutor();
		typeHandlerFactory = new TypeHandlerFactory();
		dataExchangeFactory = new DataExchangeFactory(typeHandlerFactory);
	}

	/**
	 * DO NOT DEPEND ON THIS. Here to avoid breaking spring integration.
	 * 
	 * @deprecated
	 */
	public int getMaxTransactions() {
		return -1;
	}

	/**
	 * Getter for the DataExchangeFactory
	 * 
	 * @return - the DataExchangeFactory
	 */
	public DataExchangeFactory getDataExchangeFactory() {
		return dataExchangeFactory;
	}

	/**
	 * Getter for the TypeHandlerFactory
	 * 
	 * @return - the TypeHandlerFactory
	 */
	public TypeHandlerFactory getTypeHandlerFactory() {
		return typeHandlerFactory;
	}

	/**
	 * Getter for the status of CGLib enhancements
	 * 
	 * @return - the status
	 */
	public boolean isEnhancementEnabled() {
		return enhancementEnabled;
	}

	/**
	 * Turn on or off CGLib enhancements
	 * 
	 * @param enhancementEnabled
	 *            - the new state
	 */
	public void setEnhancementEnabled(boolean enhancementEnabled) {
		this.enhancementEnabled = enhancementEnabled;
	}

	/**
	 * Add a mapped statement
	 * 
	 * @param ms
	 *            - the mapped statement to add
	 */
	public void addMappedStatement(MappedStatement ms) {
		if (mappedStatements.containsKey(ms.getId())) {
			throw new SoapMapException("There is already a statement named " + ms.getId() + " in this SoapMap.");
		}
		ms.setBaseCacheKey(hashCode());
		mappedStatements.put(ms.getId(), ms);
	}

	/**
	 * Get an iterator of the mapped statements
	 * 
	 * @return - the iterator
	 */
	public Iterator getMappedStatementNames() {
		return mappedStatements.keySet().iterator();
	}

	/**
	 * Get a mappedstatement by its ID
	 * 
	 * @param id
	 *            - the statement ID
	 * @return - the mapped statement
	 */
	public MappedStatement getMappedStatement(String id) {
		MappedStatement ms = (MappedStatement) mappedStatements.get(id);
		if (ms == null) {
			throw new SoapMapException("There is no statement named " + id + " in this SoapMap.");
		}
		return ms;
	}

	/**
	 * Add a result map
	 * 
	 * @param map
	 *            - the result map to add
	 */
	public void addResultMap(ResultMap map) {
		resultMaps.put(map.getId(), map);
	}

	/**
	 * Get an iterator of the result maps
	 * 
	 * @return - the result maps
	 */
	public Iterator getResultMapNames() {
		return resultMaps.keySet().iterator();
	}

	/**
	 * Get a result map by ID
	 * 
	 * @param id
	 *            - the ID
	 * @return - the result map
	 */
	public ResultMap getResultMap(String id) {
		ResultMap map = (ResultMap) resultMaps.get(id);
		if (map == null) {
			throw new SoapMapException("There is no result map named " + id + " in this SoapMap.");
		}
		return map;
	}

	/**
	 * Add a parameter map
	 * 
	 * @param map
	 *            - the map to add
	 */
	public void addParameterMap(ParameterMap map) {
		parameterMaps.put(map.getId(), map);
	}

	/**
	 * Get an iterator of all of the parameter maps
	 * 
	 * @return - the parameter maps
	 */
	public Iterator getParameterMapNames() {
		return parameterMaps.keySet().iterator();
	}

	/**
	 * Get a parameter map by ID
	 * 
	 * @param id
	 *            - the ID
	 * @return - the parameter map
	 */
	public ParameterMap getParameterMap(String id) {
		ParameterMap map = (ParameterMap) parameterMaps.get(id);
		if (map == null) {
			throw new SoapMapException("There is no parameter map named " + id + " in this SoapMap.");
		}
		return map;
	}

	public Object execute(SessionScope sessionScope, String url, String id) throws SoapException {
		return execute(sessionScope, url, id, null);

	}

	public Object execute(SessionScope sessionScope, String url, String id, Object paramObject) throws SoapException {
		Object object = null;

		MappedStatement ms = getMappedStatement(id);

		StatementScope statementScope = beginStatementScope(sessionScope, ms);
		try {
			object = ms.execute(statementScope, url, paramObject);
		} finally {
			endStatementScope(statementScope);
		}

		return object;
	}

	/**
	 * Getter for the SqlExecutor
	 * 
	 * @return the SqlExecutor
	 */
	public SoapExecutor getSqlExecutor() {
		return sqlExecutor;
	}

	// -- Protected Methods

	protected StatementScope beginStatementScope(SessionScope sessionScope, MappedStatement mappedStatement) {
		StatementScope statementScope = new StatementScope(sessionScope);
		sessionScope.incrementRequestStackDepth();
		mappedStatement.initRequest(statementScope);
		return statementScope;
	}

	protected void endStatementScope(StatementScope statementScope) {
		statementScope.getSession().decrementRequestStackDepth();
	}

	protected SessionScope beginSessionScope() {
		return new SessionScope();
	}

	protected void endSessionScope(SessionScope sessionScope) {
		sessionScope.cleanup();
	}

	public ResultObjectFactory getResultObjectFactory() {
		return resultObjectFactory;
	}

	public void setResultObjectFactory(ResultObjectFactory resultObjectFactory) {
		this.resultObjectFactory = resultObjectFactory;
	}

	public boolean isStatementCacheEnabled() {
		return statementCacheEnabled;
	}

	public void setStatementCacheEnabled(boolean statementCacheEnabled) {
		this.statementCacheEnabled = statementCacheEnabled;
	}

	public boolean isForceMultipleResultSetSupport() {
		return forceMultipleResultSetSupport;
	}

	public void setForceMultipleResultSetSupport(boolean forceMultipleResultSetSupport) {
		this.forceMultipleResultSetSupport = forceMultipleResultSetSupport;
	}

}

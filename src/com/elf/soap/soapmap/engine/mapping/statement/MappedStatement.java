package com.elf.soap.soapmap.engine.mapping.statement;

import com.elf.soap.common.io.ReaderInputStream;
import com.elf.soap.soapmap.client.SoapMapClient;
import com.elf.soap.soapmap.client.event.RowHandler;
import com.elf.soap.soapmap.engine.execution.SoapExecutor;
import com.elf.soap.soapmap.engine.impl.SoapMapClientImpl;
import com.elf.soap.soapmap.engine.mapping.parameter.ParameterMap;
import com.elf.soap.soapmap.engine.mapping.result.ResultMap;
import com.elf.soap.soapmap.engine.mapping.soap.Soap;
import com.elf.soap.soapmap.engine.scope.ErrorContext;
import com.elf.soap.soapmap.engine.scope.StatementScope;
import com.elf.soap.soapmap.engine.type.DomTypeMarker;
import com.elf.soap.soapmap.engine.type.XmlTypeMarker;
import com.elf.soap.soapmap.exception.SoapException;

import org.w3c.dom.Document;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * 
 * @author
 * 
 */
public class MappedStatement {

	private String id;
	private Integer resultSetType;
	private Integer fetchSize;
	private ResultMap resultMap;
	private ParameterMap parameterMap;
	private Class parameterClass;
	private Soap sql;
	private SoapMapClientImpl sqlMapClient;
	private Integer timeout;
	private ResultMap[] additionalResultMaps = new ResultMap[0];
	private List executeListeners = new ArrayList();
	private String resource;

	public StatementType getStatementType() {
		return StatementType.UNKNOWN;
	}

	public Object execute(StatementScope statementScope, String url, Object parameterObject) throws SoapException {
		Object object = null;

		DefaultRowHandler rowHandler = new DefaultRowHandler();
		executeWithCallback(statementScope, url, parameterObject, null, rowHandler, SoapExecutor.NO_SKIPPED_RESULTS,
			SoapExecutor.NO_MAXIMUM_RESULTS);
		List list = rowHandler.getList();

		if (list.size() > 1) {
			throw new SoapException("Error: execute returned too many results.");
		} else if (list.size() > 0) {
			object = list.get(0);
		}

		return object;
	}

	//
	// PROTECTED METHODS
	//

	protected void executeWithCallback(StatementScope statementScope, String url, Object parameterObject,
		Object resultObject, RowHandler rowHandler, int skipResults, int maxResults) throws SoapException {
		ErrorContext errorContext = statementScope.getErrorContext();
		errorContext.setActivity("preparing the mapped statement for execution");
		errorContext.setObjectId(this.getId());
		errorContext.setResource(this.getResource());

		try {
			parameterObject = validateParameter(parameterObject);

			Soap soap = getSql();

			errorContext.setMoreInfo("Check the parameter map.");
			ParameterMap paramMap = soap.getParameterMap(statementScope, parameterObject);

			errorContext.setMoreInfo("Check the result map.");
			ResultMap resMap = soap.getResultMap(statementScope, parameterObject);

			statementScope.setResultMap(resMap);
			statementScope.setParameterMap(paramMap);

			errorContext.setMoreInfo("Check the parameter map.");
			Object[] parameters = paramMap.getParameterObjectValues(statementScope, parameterObject);

			errorContext.setMoreInfo("Check the SOAP statement.");
			String soapString = soap.getSoap(statementScope, parameterObject);

			errorContext.setActivity("executing mapped statement");
			errorContext.setMoreInfo("Check the SOAP statement or the result map.");
			RowHandlerCallback callback = new RowHandlerCallback(resMap, resultObject, rowHandler);
			soapExecute(statementScope, url, soapString, parameters, skipResults, maxResults, callback);

			errorContext.setMoreInfo("Check the output parameters.");
			if (parameterObject != null) {
				postProcessParameterObject(statementScope, parameterObject, parameters);
			}

			errorContext.reset();
			soap.cleanup(statementScope);
			notifyListeners();
		} catch (SoapException e) {
			errorContext.setCause(e);
			throw new SoapException(errorContext.toString(), e);
		} catch (Exception e) {
			errorContext.setCause(e);
			throw new SoapException(errorContext.toString(), e);
		}
	}

	protected void postProcessParameterObject(StatementScope statementScope, Object parameterObject, Object[] parameters) {
	}

	protected void soapExecute(StatementScope statementScope, String url, String sqlString, Object[] parameters,
		int skipResults, int maxResults, RowHandlerCallback callback) throws SoapException {
		getSqlExecutor().execute(statementScope, url, sqlString, callback);
	}

	protected Object validateParameter(Object param) throws SoapException {
		Object newParam = param;
		Class paramClass = getParameterClass();
		if (newParam != null && paramClass != null) {
			if (DomTypeMarker.class.isAssignableFrom(paramClass)) {
				if (XmlTypeMarker.class.isAssignableFrom(paramClass)) {
					if (!(newParam instanceof String) && !(newParam instanceof Document)) {
						throw new SoapException("Invalid parameter object type.  Expected '" + String.class.getName()
							+ "' or '" + Document.class.getName() + "' but found '" + newParam.getClass().getName()
							+ "'.");
					}
					if (!(newParam instanceof Document)) {
						newParam = stringToDocument((String) newParam);
					}
				} else {
					if (!Document.class.isAssignableFrom(newParam.getClass())) {
						throw new SoapException("Invalid parameter object type.  Expected '" + Document.class.getName()
							+ "' but found '" + newParam.getClass().getName() + "'.");
					}
				}
			} else {
				if (!paramClass.isAssignableFrom(newParam.getClass())) {
					throw new SoapException("Invalid parameter object type.  Expected '" + paramClass.getName()
						+ "' but found '" + newParam.getClass().getName() + "'.");
				}
			}
		}
		return newParam;
	}

	private Document stringToDocument(String s) {
		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			return documentBuilder.parse(new ReaderInputStream(new StringReader(s)));
		} catch (Exception e) {
			throw new RuntimeException("Error occurred.  Cause: " + e, e);
		}
	}

	public String getId() {
		return id;
	}

	public Integer getResultSetType() {
		return resultSetType;
	}

	public void setResultSetType(Integer resultSetType) {
		this.resultSetType = resultSetType;
	}

	public Integer getFetchSize() {
		return fetchSize;
	}

	public void setFetchSize(Integer fetchSize) {
		this.fetchSize = fetchSize;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Soap getSql() {
		return sql;
	}

	public void setSql(Soap sql) {
		this.sql = sql;
	}

	public ResultMap getResultMap() {
		return resultMap;
	}

	public void setResultMap(ResultMap resultMap) {
		this.resultMap = resultMap;
	}

	public ParameterMap getParameterMap() {
		return parameterMap;
	}

	public void setParameterMap(ParameterMap parameterMap) {
		this.parameterMap = parameterMap;
	}

	public Class getParameterClass() {
		return parameterClass;
	}

	public void setParameterClass(Class parameterClass) {
		this.parameterClass = parameterClass;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public void setBaseCacheKey(int base) {
	}

	public void addExecuteListener(ExecuteListener listener) {
		executeListeners.add(listener);
	}

	public void notifyListeners() {
		for (int i = 0, n = executeListeners.size(); i < n; i++) {
			((ExecuteListener) executeListeners.get(i)).onExecuteStatement(this);
		}
	}

	public SoapExecutor getSqlExecutor() {
		return sqlMapClient.getSqlExecutor();
	}

	public SoapMapClient getSqlMapClient() {
		return sqlMapClient;
	}

	public void setSqlMapClient(SoapMapClient sqlMapClient) {
		this.sqlMapClient = (SoapMapClientImpl) sqlMapClient;
	}

	public void initRequest(StatementScope statementScope) {
		statementScope.setStatement(this);
		statementScope.setParameterMap(parameterMap);
		statementScope.setResultMap(resultMap);
		statementScope.setSql(sql);
	}

	public Integer getTimeout() {
		return timeout;
	}

	public void setTimeout(Integer timeout) {
		this.timeout = timeout;
	}

	public void addResultMap(ResultMap resultMap) {
		List resultMapList = Arrays.asList(additionalResultMaps);
		resultMapList = new ArrayList(resultMapList);
		resultMapList.add(resultMap);
		additionalResultMaps = (ResultMap[]) resultMapList.toArray(new ResultMap[resultMapList.size()]);
	}

	public boolean hasMultipleResultMaps() {
		return additionalResultMaps.length > 0;
	}

	public ResultMap[] getAdditionalResultMaps() {
		return additionalResultMaps;
	}
}

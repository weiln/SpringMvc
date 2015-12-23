package com.elf.soap.soapmap.engine.config;

import com.elf.soap.soapmap.client.SoapMapException;
import com.elf.soap.soapmap.engine.impl.SoapMapClientImpl;
import com.elf.soap.soapmap.engine.impl.SoapMapExecutorDelegate;
import com.elf.soap.soapmap.engine.mapping.parameter.InlineParameterMapParser;
import com.elf.soap.soapmap.engine.mapping.parameter.ParameterMap;
import com.elf.soap.soapmap.engine.mapping.result.AutoResultMap;
import com.elf.soap.soapmap.engine.mapping.result.ResultMap;
import com.elf.soap.soapmap.engine.mapping.soap.Soap;
import com.elf.soap.soapmap.engine.mapping.soap.SoapText;
import com.elf.soap.soapmap.engine.mapping.soap.dynamic.DynamicSoap;
import com.elf.soap.soapmap.engine.mapping.soap.simple.SimpleDynamicSoap;
import com.elf.soap.soapmap.engine.mapping.soap.stat.StaticSoap;
import com.elf.soap.soapmap.engine.mapping.statement.MappedStatement;
import com.elf.soap.soapmap.engine.scope.ErrorContext;
import com.elf.soap.soapmap.engine.type.TypeHandlerFactory;

import java.sql.ResultSet;
import java.util.Arrays;
import java.util.List;

/**
 * 
 * @author
 * 
 */
public class MappedStatementConfig {

	private static final InlineParameterMapParser PARAM_PARSER = new InlineParameterMapParser();
	private ErrorContext errorContext;
	private SoapMapClientImpl client;
	private TypeHandlerFactory typeHandlerFactory;
	private MappedStatement mappedStatement;
	private MappedStatement rootStatement;

	MappedStatementConfig(SoapMapConfiguration config, String id, MappedStatement statement, SoapSource processor,
		String parameterMapName, Class parameterClass, String resultMapName, String[] additionalResultMapNames,
		Class resultClass, Class[] additionalResultClasses, String cacheModelName, String resultSetType,
		Integer fetchSize, boolean allowRemapping, Integer timeout, Integer defaultStatementTimeout,
		String xmlResultName) {
		this.errorContext = config.getErrorContext();
		this.client = config.getClient();
		SoapMapExecutorDelegate delegate = client.getDelegate();
		this.typeHandlerFactory = config.getTypeHandlerFactory();
		errorContext.setActivity("parsing a mapped statement");
		errorContext.setObjectId(id + " statement");
		errorContext.setMoreInfo("Check the result map name.");
		if (resultMapName != null) {
			statement.setResultMap(client.getDelegate().getResultMap(resultMapName));
			if (additionalResultMapNames != null) {
				for (int i = 0; i < additionalResultMapNames.length; i++) {
					statement.addResultMap(client.getDelegate().getResultMap(additionalResultMapNames[i]));
				}
			}
		}
		errorContext.setMoreInfo("Check the parameter map name.");
		if (parameterMapName != null) {
			statement.setParameterMap(client.getDelegate().getParameterMap(parameterMapName));
		}
		statement.setId(id);
		statement.setResource(errorContext.getResource());
		if (resultSetType != null) {
			if ("FORWARD_ONLY".equals(resultSetType)) {
				statement.setResultSetType(Integer.valueOf(ResultSet.TYPE_FORWARD_ONLY));
			} else if ("SCROLL_INSENSITIVE".equals(resultSetType)) {
				statement.setResultSetType(Integer.valueOf(ResultSet.TYPE_SCROLL_INSENSITIVE));
			} else if ("SCROLL_SENSITIVE".equals(resultSetType)) {
				statement.setResultSetType(Integer.valueOf(ResultSet.TYPE_SCROLL_SENSITIVE));
			}
		}
		if (fetchSize != null) {
			statement.setFetchSize(fetchSize);
		}

		// set parameter class either from attribute or from map (make sure to match)
		ParameterMap parameterMap = statement.getParameterMap();
		if (parameterMap == null) {
			statement.setParameterClass(parameterClass);
		} else {
			statement.setParameterClass(parameterMap.getParameterClass());
		}

		// process SQL statement, including inline parameter maps
		errorContext.setMoreInfo("Check the SQL statement.");
		Soap sql = processor.getSoap();
		setSqlForStatement(statement, sql);

		// set up either null result map or automatic result mapping
		ResultMap resultMap = (ResultMap) statement.getResultMap();
		if (resultMap == null && resultClass == null) {
			statement.setResultMap(null);
		} else if (resultMap == null) {
			resultMap = buildAutoResultMap(allowRemapping, statement, resultClass, xmlResultName);
			statement.setResultMap(resultMap);
			if (additionalResultClasses != null) {
				for (int i = 0; i < additionalResultClasses.length; i++) {
					statement.addResultMap(buildAutoResultMap(allowRemapping, statement, additionalResultClasses[i],
						xmlResultName));
				}
			}

		}
		statement.setTimeout(defaultStatementTimeout);
		if (timeout != null) {
			try {
				statement.setTimeout(timeout);
			} catch (NumberFormatException e) {
				throw new SoapMapException("Specified timeout value for statement " + statement.getId()
					+ " is not a valid integer");
			}
		}
		errorContext.setMoreInfo(null);
		errorContext.setObjectId(null);
		statement.setSqlMapClient(client);

		// remove CachingStatement
		mappedStatement = statement;

		rootStatement = statement;
		delegate.addMappedStatement(mappedStatement);
	}

	private void setSqlForStatement(MappedStatement statement, Soap sql) {
		if (sql instanceof DynamicSoap) {
			statement.setSql(sql);
		} else {
			applyInlineParameterMap(statement, sql.getSoap(null, null));
		}
	}

	private void applyInlineParameterMap(MappedStatement statement, String sqlStatement) {
		String newSoap = sqlStatement;
		errorContext.setActivity("building an inline parameter map");
		ParameterMap parameterMap = statement.getParameterMap();
		errorContext.setMoreInfo("Check the inline parameters.");
		if (parameterMap == null) {
			ParameterMap map;
			map = new ParameterMap(client.getDelegate());
			map.setId(statement.getId() + "-InlineParameterMap");
			map.setParameterClass(statement.getParameterClass());
			map.setResource(statement.getResource());
			statement.setParameterMap(map);
			SoapText soapText =
				PARAM_PARSER.parseInlineParameterMap(client.getDelegate().getTypeHandlerFactory(), newSoap,
					statement.getParameterClass());
			newSoap = soapText.getText();
			List mappingList = Arrays.asList(soapText.getParameterMappings());
			map.setParameterMappingList(mappingList);
		}
		Soap soap;
		if (SimpleDynamicSoap.isSimpleDynamicSql(newSoap)) {
			soap = new SimpleDynamicSoap(client.getDelegate(), newSoap);
		} else {
			soap = new StaticSoap(newSoap);
		}
		statement.setSql(soap);

	}

	private ResultMap buildAutoResultMap(boolean allowRemapping, MappedStatement statement, Class firstResultClass,
		String xmlResultName) {
		ResultMap resultMap;
		resultMap = new AutoResultMap(client.getDelegate(), allowRemapping);
		resultMap.setId(statement.getId() + "-AutoResultMap");
		resultMap.setResultClass(firstResultClass);
		resultMap.setXmlName(xmlResultName);
		resultMap.setResource(statement.getResource());
		return resultMap;
	}

	public MappedStatement getMappedStatement() {
		return mappedStatement;
	}

}

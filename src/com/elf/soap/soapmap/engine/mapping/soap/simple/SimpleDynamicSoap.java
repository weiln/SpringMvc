package com.elf.soap.soapmap.engine.mapping.soap.simple;

import com.elf.soap.common.beans.Probe;
import com.elf.soap.common.beans.ProbeFactory;
import com.elf.soap.soapmap.client.SoapMapException;
import com.elf.soap.soapmap.engine.impl.SoapMapExecutorDelegate;
import com.elf.soap.soapmap.engine.mapping.parameter.ParameterMap;
import com.elf.soap.soapmap.engine.mapping.result.ResultMap;
import com.elf.soap.soapmap.engine.mapping.soap.Soap;
import com.elf.soap.soapmap.engine.scope.StatementScope;

import java.util.StringTokenizer;

import org.apache.commons.lang.StringEscapeUtils;

/**
 * 
 * @author
 * 
 */
public class SimpleDynamicSoap implements Soap {

	private static final Probe PROBE = ProbeFactory.getProbe();

	private static final String ELEMENT_TOKEN = "$";

	private String sqlStatement;

	private SoapMapExecutorDelegate delegate;

	public SimpleDynamicSoap(SoapMapExecutorDelegate delegate, String sqlStatement) {
		this.delegate = delegate;
		this.sqlStatement = sqlStatement;
	}

	public String getSoap(StatementScope statementScope, Object parameterObject) {
		return processDynamicElements(sqlStatement, parameterObject);
	}

	public ParameterMap getParameterMap(StatementScope statementScope, Object parameterObject) {
		return statementScope.getParameterMap();
	}

	public ResultMap getResultMap(StatementScope statementScope, Object parameterObject) {
		return statementScope.getResultMap();
	}

	public void cleanup(StatementScope statementScope) {
	}

	public static boolean isSimpleDynamicSql(String sql) {
		return sql != null && sql.indexOf(ELEMENT_TOKEN) > -1;
	}

	private String processDynamicElements(String sql, Object parameterObject) {
		StringTokenizer parser = new StringTokenizer(sql, ELEMENT_TOKEN, true);
		StringBuffer newSql = new StringBuffer();

		String token = null;
		String lastToken = null;
		while (parser.hasMoreTokens()) {
			token = parser.nextToken();

			if (ELEMENT_TOKEN.equals(lastToken)) {
				if (ELEMENT_TOKEN.equals(token)) {
					newSql.append(ELEMENT_TOKEN);
					token = null;
				} else {

					Object value = null;
					if (parameterObject != null) {
						if (delegate.getTypeHandlerFactory().hasTypeHandler(parameterObject.getClass())) {
							value = parameterObject;
						} else {
							value = PROBE.getObject(parameterObject, token);
						}
					}
					if (value != null) {
						newSql.append(StringEscapeUtils.escapeXml(String.valueOf(value)));
					}

					token = parser.nextToken();
					if (!ELEMENT_TOKEN.equals(token)) {
						throw new SoapMapException("Unterminated dynamic element in sql (" + sql + ").");
					}
					token = null;
				}
			} else {
				if (!ELEMENT_TOKEN.equals(token)) {
					newSql.append(token);
				}
			}

			lastToken = token;
		}

		return newSql.toString();
	}

}

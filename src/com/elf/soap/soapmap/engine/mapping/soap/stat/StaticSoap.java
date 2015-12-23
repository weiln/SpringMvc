package com.elf.soap.soapmap.engine.mapping.soap.stat;

import com.elf.soap.soapmap.engine.mapping.parameter.ParameterMap;
import com.elf.soap.soapmap.engine.mapping.result.ResultMap;
import com.elf.soap.soapmap.engine.mapping.soap.Soap;
import com.elf.soap.soapmap.engine.scope.StatementScope;

/**
 * 
 * @author
 * 
 */
public class StaticSoap implements Soap {

	private String sqlStatement;

	public StaticSoap(String sqlStatement) {
		this.sqlStatement = sqlStatement.replace('\r', ' ').replace('\n', ' ');
	}

	public String getSoap(StatementScope statementScope, Object parameterObject) {
		return sqlStatement;
	}

	public ParameterMap getParameterMap(StatementScope statementScope, Object parameterObject) {
		return statementScope.getParameterMap();
	}

	public ResultMap getResultMap(StatementScope statementScope, Object parameterObject) {
		return statementScope.getResultMap();
	}

	public void cleanup(StatementScope statementScope) {
	}

}

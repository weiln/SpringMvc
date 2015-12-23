package com.elf.soap.soapmap.engine.mapping.soap.raw;

import com.elf.soap.soapmap.engine.mapping.soap.Soap;
import com.elf.soap.soapmap.engine.mapping.parameter.ParameterMap;
import com.elf.soap.soapmap.engine.mapping.result.ResultMap;
import com.elf.soap.soapmap.engine.scope.StatementScope;

/**
 * A non-executable SQL container simply for
 * communicating raw SQL around the framework.
 */
public class RawSoap implements Soap {

	private String sql;

	public RawSoap(String sql) {
		this.sql = sql;
	}

	public String getSoap(StatementScope statementScope, Object parameterObject) {
		return sql;
	}

	public ParameterMap getParameterMap(StatementScope statementScope, Object parameterObject) {
		throw new RuntimeException("Method not implemented on RawSql.");
	}

	public ResultMap getResultMap(StatementScope statementScope, Object parameterObject) {
		throw new RuntimeException("Method not implemented on RawSql.");
	}

	public void cleanup(StatementScope statementScope) {
		throw new RuntimeException("Method not implemented on RawSql.");
	}
}

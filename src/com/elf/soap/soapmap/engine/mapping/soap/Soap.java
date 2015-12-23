package com.elf.soap.soapmap.engine.mapping.soap;

import com.elf.soap.soapmap.engine.mapping.parameter.ParameterMap;
import com.elf.soap.soapmap.engine.mapping.result.ResultMap;
import com.elf.soap.soapmap.engine.scope.StatementScope;

/**
 * 
 * @author
 * 
 */
public interface Soap {

	String getSoap(StatementScope statementScope, Object parameterObject);

	ParameterMap getParameterMap(StatementScope statementScope, Object parameterObject);

	ResultMap getResultMap(StatementScope statementScope, Object parameterObject);

	void cleanup(StatementScope statementScope);

}

package com.elf.soap.soapmap.engine.mapping.parameter;

import com.elf.soap.soapmap.engine.impl.SoapMapExecutorDelegate;
import com.elf.soap.soapmap.engine.scope.StatementScope;

/**
 * 
 * @author
 * 
 */
public class NoParameterMap extends ParameterMap {

	private static final ParameterMapping[] NO_PARAMETERS = new ParameterMapping[0];
	private static final Object[] NO_DATA = new Object[0];

	public NoParameterMap(SoapMapExecutorDelegate delegate) {
		super(delegate);
	}

	public ParameterMapping[] getParameterMappings() {
		return NO_PARAMETERS;
	}

	public Object[] getParameterObjectValues(StatementScope statementScope, Object parameterObject) {
		return NO_DATA;
	}

}

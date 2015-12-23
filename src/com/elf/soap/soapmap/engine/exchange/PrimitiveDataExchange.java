package com.elf.soap.soapmap.engine.exchange;

import com.elf.soap.soapmap.engine.mapping.parameter.ParameterMap;
import com.elf.soap.soapmap.engine.mapping.parameter.ParameterMapping;
import com.elf.soap.soapmap.engine.mapping.result.ResultMap;
import com.elf.soap.soapmap.engine.scope.StatementScope;

import java.util.Map;

/**
 * DataExchange implementation for primitive objects.
 */
public class PrimitiveDataExchange extends BaseDataExchange implements DataExchange {

	protected PrimitiveDataExchange(DataExchangeFactory dataExchangeFactory) {
		super(dataExchangeFactory);
	}

	public void initialize(Map properties) {
	}

	public Object[] getData(StatementScope statementScope, ParameterMap parameterMap, Object parameterObject) {
		ParameterMapping[] mappings = parameterMap.getParameterMappings();
		Object[] data = new Object[mappings.length];
		for (int i = 0; i < mappings.length; i++) {
			data[i] = parameterObject;
		}
		return data;
	}

	public Object setData(StatementScope statementScope, ResultMap resultMap, Object resultObject, Object[] values) {
		return values[0];
	}

	public Object setData(StatementScope statementScope, ParameterMap parameterMap, Object parameterObject,
		Object[] values) {
		return values[0];
	}

}

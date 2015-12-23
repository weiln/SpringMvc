package com.elf.soap.soapmap.engine.exchange;

import com.elf.soap.common.beans.Probe;
import com.elf.soap.common.beans.ProbeFactory;

import com.elf.soap.soapmap.engine.mapping.parameter.ParameterMap;
import com.elf.soap.soapmap.engine.mapping.parameter.ParameterMapping;
import com.elf.soap.soapmap.engine.mapping.result.ResultMap;
import com.elf.soap.soapmap.engine.mapping.result.ResultMapping;
import com.elf.soap.soapmap.engine.mapping.result.ResultObjectFactoryUtil;
import com.elf.soap.soapmap.engine.scope.StatementScope;
import com.elf.soap.soapmap.engine.type.TypeHandlerFactory;

import java.util.Map;

/**
 * A DataExchange implemtation for working with beans.
 */
public class ComplexDataExchange extends BaseDataExchange implements DataExchange {

	private static final Probe PROBE = ProbeFactory.getProbe();

	/**
	 * Constructor for the factory
	 * 
	 * @param dataExchangeFactory
	 *            - the factory
	 */
	public ComplexDataExchange(DataExchangeFactory dataExchangeFactory) {
		super(dataExchangeFactory);
	}

	public void initialize(Map properties) {
	}

	public Object[] getData(StatementScope statementScope, ParameterMap parameterMap, Object parameterObject) {
		TypeHandlerFactory typeHandlerFactory = getDataExchangeFactory().getTypeHandlerFactory();
		if (parameterObject == null) {
			return new Object[1];
		} else {
			if (typeHandlerFactory.hasTypeHandler(parameterObject.getClass())) {
				ParameterMapping[] mappings = parameterMap.getParameterMappings();
				Object[] data = new Object[mappings.length];
				for (int i = 0; i < mappings.length; i++) {
					data[i] = parameterObject;
				}
				return data;
			} else {
				Object[] data = new Object[parameterMap.getParameterMappings().length];
				ParameterMapping[] mappings = parameterMap.getParameterMappings();
				for (int i = 0; i < mappings.length; i++) {
					data[i] = PROBE.getObject(parameterObject, mappings[i].getPropertyName());
				}
				return data;
			}
		}
	}

	public Object setData(StatementScope statementScope, ResultMap resultMap, Object resultObject, Object[] values) {
		TypeHandlerFactory typeHandlerFactory = getDataExchangeFactory().getTypeHandlerFactory();
		if (typeHandlerFactory.hasTypeHandler(resultMap.getResultClass())) {
			return values[0];
		} else {
			Object object = resultObject;
			if (object == null) {
				try {
					object = ResultObjectFactoryUtil.createObjectThroughFactory(resultMap.getResultClass());
				} catch (Exception e) {
					throw new RuntimeException(
						"JavaBeansDataExchange could not instantiate result class.  Cause: " + e, e);
				}
			}
			ResultMapping[] mappings = resultMap.getResultMappings();
			for (int i = 0; i < mappings.length; i++) {
				PROBE.setObject(object, mappings[i].getPropertyName(), values[i]);
			}
			return object;
		}
	}

	public Object setData(StatementScope statementScope, ParameterMap parameterMap, Object parameterObject,
		Object[] values) {
		TypeHandlerFactory typeHandlerFactory = getDataExchangeFactory().getTypeHandlerFactory();
		if (typeHandlerFactory.hasTypeHandler(parameterMap.getParameterClass())) {
			return values[0];
		} else {
			Object object = parameterObject;
			if (object == null) {
				try {
					object = ResultObjectFactoryUtil.createObjectThroughFactory(parameterMap.getParameterClass());
				} catch (Exception e) {
					throw new RuntimeException(
						"JavaBeansDataExchange could not instantiate result class.  Cause: " + e, e);
				}
			}
			ParameterMapping[] mappings = parameterMap.getParameterMappings();
			for (int i = 0; i < mappings.length; i++) {
				if (mappings[i].isOutputAllowed()) {
					PROBE.setObject(object, mappings[i].getPropertyName(), values[i]);
				}
			}
			return object;
		}
	}

}

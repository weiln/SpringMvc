package com.elf.soap.soapmap.engine.config;

import java.util.ArrayList;
import java.util.List;

import com.elf.soap.soapmap.client.extensions.TypeHandlerCallback;
import com.elf.soap.soapmap.engine.impl.SoapMapClientImpl;
import com.elf.soap.soapmap.engine.mapping.parameter.ParameterMap;
import com.elf.soap.soapmap.engine.mapping.parameter.ParameterMapping;
import com.elf.soap.soapmap.engine.scope.ErrorContext;
import com.elf.soap.soapmap.engine.type.CustomTypeHandler;
import com.elf.soap.soapmap.engine.type.TypeHandler;

/**
 * 
 * @author
 * 
 */
public class ParameterMapConfig {

	public static final String MODE_IN = "IN";
	public static final String MODE_OUT = "OUT";
	public static final String MODE_INOUT = "INUOT";

	private SoapMapConfiguration config;
	private ErrorContext errorContext;
	private SoapMapClientImpl client;
	private ParameterMap parameterMap;
	private List parameterMappingList;

	ParameterMapConfig(SoapMapConfiguration config, String id, Class parameterClass) {
		this.config = config;
		this.errorContext = config.getErrorContext();
		this.client = config.getClient();
		errorContext.setActivity("building a parameter map");
		parameterMap = new ParameterMap(client.getDelegate());
		parameterMap.setId(id);
		parameterMap.setResource(errorContext.getResource());
		errorContext.setObjectId(id + " parameter map");
		parameterMap.setParameterClass(parameterClass);
		errorContext.setMoreInfo("Check the parameter mappings.");
		this.parameterMappingList = new ArrayList();
		client.getDelegate().addParameterMap(parameterMap);
	}

	public void addParameterMapping(String propertyName, Class javaClass, String jdbcType, String nullValue,
		String mode, String outParamType, Integer numericScale, Object typeHandlerImpl, String resultMap) {
		errorContext.setObjectId(propertyName + " mapping of the " + parameterMap.getId() + " parameter map");
		TypeHandler handler;
		if (typeHandlerImpl != null) {
			errorContext.setMoreInfo("Check the parameter mapping typeHandler attribute '" + typeHandlerImpl
				+ "' (must be a TypeHandler or TypeHandlerCallback implementation).");
			if (typeHandlerImpl instanceof TypeHandlerCallback) {
				handler = new CustomTypeHandler((TypeHandlerCallback) typeHandlerImpl);
			} else if (typeHandlerImpl instanceof TypeHandler) {
				handler = (TypeHandler) typeHandlerImpl;
			} else {
				throw new RuntimeException("The class '" + typeHandlerImpl
					+ "' is not a valid implementation of TypeHandler or TypeHandlerCallback");
			}
		} else {
			errorContext.setMoreInfo("Check the parameter mapping property type or name.");
			handler =
				config.resolveTypeHandler(client.getDelegate().getTypeHandlerFactory(),
					parameterMap.getParameterClass(), propertyName, javaClass, jdbcType);
		}
		ParameterMapping mapping = new ParameterMapping();
		mapping.setPropertyName(propertyName);
		mapping.setJdbcTypeName(jdbcType);
		mapping.setTypeName(outParamType);
		mapping.setResultMapName(resultMap);
		mapping.setNullValue(nullValue);
		if (mode != null && mode.length() > 0) {
			mapping.setMode(mode);
		}
		mapping.setTypeHandler(handler);
		mapping.setJavaType(javaClass);
		mapping.setNumericScale(numericScale);
		parameterMappingList.add(mapping);
		parameterMap.setParameterMappingList(parameterMappingList);
	}

}

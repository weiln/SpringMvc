package com.elf.soap.soapmap.engine.exchange;

import com.elf.soap.soapmap.engine.accessplan.AccessPlan;
import com.elf.soap.soapmap.engine.accessplan.AccessPlanFactory;
import com.elf.soap.soapmap.engine.mapping.parameter.ParameterMap;
import com.elf.soap.soapmap.engine.mapping.parameter.ParameterMapping;
import com.elf.soap.soapmap.engine.mapping.result.ResultMap;
import com.elf.soap.soapmap.engine.mapping.result.ResultMapping;
import com.elf.soap.soapmap.engine.mapping.result.ResultObjectFactoryUtil;
import com.elf.soap.soapmap.engine.scope.ErrorContext;
import com.elf.soap.soapmap.engine.scope.StatementScope;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * DataExchange implementation for beans.
 * 
 * @author
 * 
 */
public class JavaBeanDataExchange extends BaseDataExchange implements DataExchange {

	private static final Object[] NO_DATA = new Object[0];

	private AccessPlan resultPlan;
	private AccessPlan parameterPlan;
	private AccessPlan outParamPlan;

	protected JavaBeanDataExchange(DataExchangeFactory dataExchangeFactory) {
		super(dataExchangeFactory);
	}

	/**
	 * Initializes the data exchange instance.
	 * 
	 * @param properties
	 */
	public void initialize(Map properties) {
		Object map = properties.get("map");
		if (map instanceof ParameterMap) {
			ParameterMap parameterMap = (ParameterMap) map;
			ParameterMapping[] parameterMappings = parameterMap.getParameterMappings();
			String[] parameterPropNames = new String[parameterMappings.length];
			for (int i = 0; i < parameterPropNames.length; i++) {
				parameterPropNames[i] = parameterMappings[i].getPropertyName();
			}
			parameterPlan = AccessPlanFactory.getAccessPlan(parameterMap.getParameterClass(), parameterPropNames);

			// OUTPUT PARAMS
			List outParamList = new ArrayList();
			for (int i = 0; i < parameterPropNames.length; i++) {
				if (parameterMappings[i].isOutputAllowed()) {
					outParamList.add(parameterMappings[i].getPropertyName());
				}
			}
			String[] outParams = (String[]) outParamList.toArray(new String[outParamList.size()]);
			outParamPlan = AccessPlanFactory.getAccessPlan(parameterMap.getParameterClass(), outParams);

		} else if (map instanceof ResultMap) {
			ResultMap resultMap = (ResultMap) map;
			ResultMapping[] resultMappings = resultMap.getResultMappings();
			String[] resultPropNames = new String[resultMappings.length];
			for (int i = 0; i < resultPropNames.length; i++) {
				resultPropNames[i] = resultMappings[i].getPropertyName();
			}
			resultPlan = AccessPlanFactory.getAccessPlan(resultMap.getResultClass(), resultPropNames);
		}
	}

	public Object[] getData(StatementScope statementScope, ParameterMap parameterMap, Object parameterObject) {
		if (parameterPlan != null) {
			return parameterPlan.getProperties(parameterObject);
		} else {
			return NO_DATA;
		}
	}

	public Object setData(StatementScope statementScope, ResultMap resultMap, Object resultObject, Object[] values) {
		if (resultPlan != null) {
			Object object = resultObject;

			ErrorContext errorContext = statementScope.getErrorContext();

			if (object == null) {
				errorContext.setMoreInfo("The error occured while instantiating the result object");
				try {
					object = ResultObjectFactoryUtil.createObjectThroughFactory(resultMap.getResultClass());
				} catch (Exception e) {
					throw new RuntimeException(
						"JavaBeansDataExchange could not instantiate result class.  Cause: " + e, e);
				}
			}
			errorContext.setMoreInfo("The error happened while setting a property on the result object.");
			resultPlan.setProperties(object, values);
			return object;
		} else {
			return null;
		}
	}

	// Bug ibatis-12
	public Object setData(StatementScope statementScope, ParameterMap parameterMap, Object parameterObject,
		Object[] values) {
		if (outParamPlan != null) {
			Object object = parameterObject;
			if (object == null) {
				try {
					object = ResultObjectFactoryUtil.createObjectThroughFactory(parameterMap.getParameterClass());
				} catch (Exception e) {
					throw new RuntimeException("JavaBeansDataExchange could not instantiate parameter class. Cause: "
						+ e, e);
				}
			}
			values = getOutputParamValues(parameterMap.getParameterMappings(), values);
			outParamPlan.setProperties(object, values);
			return object;
		} else {
			return null;
		}
	}

	private Object[] getOutputParamValues(ParameterMapping[] mappings, Object[] values) {
		List outParamValues = new ArrayList();
		for (int i = 0; i < mappings.length; i++) {
			if (mappings[i].isOutputAllowed()) {
				outParamValues.add(values[i]);
			}
		}
		return outParamValues.toArray();
	}

}

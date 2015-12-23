package com.elf.soap.soapmap.engine.config;

import java.util.Iterator;

import com.elf.soap.common.beans.ClassInfo;
import com.elf.soap.common.beans.Probe;
import com.elf.soap.common.beans.ProbeFactory;
import com.elf.soap.common.resources.Resources;
import com.elf.soap.soapmap.client.SoapMapException;
import com.elf.soap.soapmap.client.extensions.TypeHandlerCallback;
import com.elf.soap.soapmap.engine.accessplan.AccessPlanFactory;
import com.elf.soap.soapmap.engine.impl.SoapMapClientImpl;
import com.elf.soap.soapmap.engine.impl.SoapMapExecutorDelegate;
import com.elf.soap.soapmap.engine.mapping.result.Discriminator;
import com.elf.soap.soapmap.engine.mapping.result.ResultMap;
import com.elf.soap.soapmap.engine.mapping.result.ResultObjectFactory;
import com.elf.soap.soapmap.engine.mapping.statement.MappedStatement;
import com.elf.soap.soapmap.engine.scope.ErrorContext;
import com.elf.soap.soapmap.engine.type.CustomTypeHandler;
import com.elf.soap.soapmap.engine.type.DomCollectionTypeMarker;
import com.elf.soap.soapmap.engine.type.DomTypeMarker;
import com.elf.soap.soapmap.engine.type.TypeHandler;
import com.elf.soap.soapmap.engine.type.TypeHandlerFactory;
import com.elf.soap.soapmap.engine.type.XmlCollectionTypeMarker;
import com.elf.soap.soapmap.engine.type.XmlTypeMarker;

/**
 * 
 * @author xujiakun
 * 
 */
public class SoapMapConfiguration {

	private static final Probe PROBE = ProbeFactory.getProbe();
	private ErrorContext errorContext;
	private SoapMapExecutorDelegate delegate;
	private TypeHandlerFactory typeHandlerFactory;
	private SoapMapClientImpl client;
	private Integer defaultStatementTimeout;

	public SoapMapConfiguration() {
		errorContext = new ErrorContext();
		delegate = new SoapMapExecutorDelegate();
		typeHandlerFactory = delegate.getTypeHandlerFactory();
		client = new SoapMapClientImpl(delegate);
		registerDefaultTypeAliases();
	}

	public TypeHandlerFactory getTypeHandlerFactory() {
		return typeHandlerFactory;
	}

	public ErrorContext getErrorContext() {
		return errorContext;
	}

	public SoapMapClientImpl getClient() {
		return client;
	}

	public SoapMapExecutorDelegate getDelegate() {
		return delegate;
	}

	public void setClassInfoCacheEnabled(boolean classInfoCacheEnabled) {
		errorContext.setActivity("setting class info cache enabled/disabled");
		ClassInfo.setCacheEnabled(classInfoCacheEnabled);
	}

	public void setStatementCachingEnabled(boolean statementCachingEnabled) {
		errorContext.setActivity("setting statement caching enabled/disabled");
		client.getDelegate().setStatementCacheEnabled(statementCachingEnabled);
	}

	public void setEnhancementEnabled(boolean enhancementEnabled) {
		errorContext.setActivity("setting enhancement enabled/disabled");
		try {
			enhancementEnabled =
				enhancementEnabled && Resources.classForName("net.sf.cglib.proxy.InvocationHandler") != null;
		} catch (ClassNotFoundException e) {
			enhancementEnabled = false;
		}
		client.getDelegate().setEnhancementEnabled(enhancementEnabled);
		AccessPlanFactory.setBytecodeEnhancementEnabled(enhancementEnabled);
	}

	public void setForceMultipleResultSetSupport(boolean forceMultipleResultSetSupport) {
		client.getDelegate().setForceMultipleResultSetSupport(forceMultipleResultSetSupport);
	}

	public void setDefaultStatementTimeout(Integer defaultTimeout) {
		errorContext.setActivity("setting default timeout");
		if (defaultTimeout != null) {
			try {
				defaultStatementTimeout = defaultTimeout;
			} catch (NumberFormatException e) {
				throw new SoapMapException("Specified defaultStatementTimeout is not a valid integer");
			}
		}
	}

	public void setResultObjectFactory(ResultObjectFactory rof) {
		delegate.setResultObjectFactory(rof);
	}

	public void newTypeHandler(Class javaType, String jdbcType, Object callback) {
		try {
			errorContext.setActivity("building a building custom type handler");
			TypeHandlerFactory thFactory = client.getDelegate().getTypeHandlerFactory();
			TypeHandler typeHandler;
			if (callback instanceof TypeHandlerCallback) {
				typeHandler = new CustomTypeHandler((TypeHandlerCallback) callback);
			} else if (callback instanceof TypeHandler) {
				typeHandler = (TypeHandler) callback;
			} else {
				throw new RuntimeException("The object '" + callback
					+ "' is not a valid implementation of TypeHandler or TypeHandlerCallback");
			}
			errorContext.setMoreInfo("Check the javaType attribute '" + javaType
				+ "' (must be a classname) or the jdbcType '" + jdbcType + "' (must be a JDBC type name).");
			if (jdbcType != null && jdbcType.length() > 0) {
				thFactory.register(javaType, jdbcType, typeHandler);
			} else {
				thFactory.register(javaType, typeHandler);
			}
		} catch (Exception e) {
			throw new SoapMapException("Error registering occurred.  Cause: " + e, e);
		}
		errorContext.setMoreInfo(null);
		errorContext.setObjectId(null);
	}

	public ParameterMapConfig newParameterMapConfig(String id, Class parameterClass) {
		return new ParameterMapConfig(this, id, parameterClass);
	}

	public ResultMapConfig newResultMapConfig(String id, Class resultClass, String groupBy, String extended,
		String xmlName) {
		return new ResultMapConfig(this, id, resultClass, groupBy, extended, xmlName);
	}

	public MappedStatementConfig newMappedStatementConfig(String id, MappedStatement statement, SoapSource processor,
		String parameterMapName, Class parameterClass, String resultMapName, String[] additionalResultMapNames,
		Class resultClass, Class[] additionalResultClasses, String resultSetType, Integer fetchSize,
		boolean allowRemapping, Integer timeout, String cacheModelName, String xmlResultName) {
		return new MappedStatementConfig(this, id, statement, processor, parameterMapName, parameterClass,
			resultMapName, additionalResultMapNames, resultClass, additionalResultClasses, cacheModelName,
			resultSetType, fetchSize, allowRemapping, timeout, defaultStatementTimeout, xmlResultName);
	}

	public void finalizeSqlMapConfig() {
		// remove wireUpCacheModels()
		bindResultMapDiscriminators();
	}

	TypeHandler resolveTypeHandler(TypeHandlerFactory typeHandlerFactory, Class clazz, String propertyName,
		Class javaType, String jdbcType) {
		return resolveTypeHandler(typeHandlerFactory, clazz, propertyName, javaType, jdbcType, false);
	}

	TypeHandler resolveTypeHandler(TypeHandlerFactory typeHandlerFactory, Class clazz, String propertyName,
		Class javaType, String jdbcType, boolean useSetterToResolve) {
		TypeHandler handler;
		if (clazz == null) {
			// Unknown
			handler = typeHandlerFactory.getUnkownTypeHandler();
		} else if (DomTypeMarker.class.isAssignableFrom(clazz)) {
			// DOM
			handler = typeHandlerFactory.getTypeHandler(String.class, jdbcType);
		} else if (java.util.Map.class.isAssignableFrom(clazz)) {
			// Map
			if (javaType == null) {
				handler = typeHandlerFactory.getUnkownTypeHandler();
			} else {
				handler = typeHandlerFactory.getTypeHandler(javaType, jdbcType);
			}
		} else if (typeHandlerFactory.getTypeHandler(clazz, jdbcType) != null) {
			// Primitive
			handler = typeHandlerFactory.getTypeHandler(clazz, jdbcType);
		} else {
			// JavaBean
			if (javaType == null) {
				if (useSetterToResolve) {
					Class type = PROBE.getPropertyTypeForSetter(clazz, propertyName);
					handler = typeHandlerFactory.getTypeHandler(type, jdbcType);
				} else {
					Class type = PROBE.getPropertyTypeForGetter(clazz, propertyName);
					handler = typeHandlerFactory.getTypeHandler(type, jdbcType);
				}
			} else {
				handler = typeHandlerFactory.getTypeHandler(javaType, jdbcType);
			}
		}
		return handler;
	}

	private void registerDefaultTypeAliases() {
		// TYPE ALIASEs
		typeHandlerFactory.putTypeAlias("dom", DomTypeMarker.class.getName());
		typeHandlerFactory.putTypeAlias("domCollection", DomCollectionTypeMarker.class.getName());
		typeHandlerFactory.putTypeAlias("xml", XmlTypeMarker.class.getName());
		typeHandlerFactory.putTypeAlias("xmlCollection", XmlCollectionTypeMarker.class.getName());
	}

	private void bindResultMapDiscriminators() {
		// Bind discriminators
		Iterator names = delegate.getResultMapNames();
		while (names.hasNext()) {
			String name = (String) names.next();
			ResultMap rm = delegate.getResultMap(name);
			Discriminator disc = rm.getDiscriminator();
			if (disc != null) {
				disc.bindSubMaps();
			}
		}
	}

}

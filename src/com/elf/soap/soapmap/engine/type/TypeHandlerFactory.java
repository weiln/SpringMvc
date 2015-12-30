package com.elf.soap.soapmap.engine.type;

import com.elf.soap.soapmap.client.SoapMapException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Not much of a suprise, this is a factory class for TypeHandler objects.
 * 
 * @author
 * 
 */
public class TypeHandlerFactory {

	private final Map typeHandlerMap = new HashMap();
	private final TypeHandler unknownTypeHandler = new UnknownTypeHandler(this);
	private final Map typeAliases = new HashMap();

	private static final Map reversePrimitiveMap = new HashMap() {
		{
			put(Byte.class, byte.class);
			put(Short.class, short.class);
			put(Integer.class, int.class);
			put(Long.class, long.class);
			put(Float.class, float.class);
			put(Double.class, double.class);
			put(Boolean.class, boolean.class);
		}
	};

	/* Constructor */

	/**
	 * Default constructor
	 */
	public TypeHandlerFactory() {
		TypeHandler handler;

		handler = new BooleanTypeHandler();
		register(Boolean.class, handler);
		register(boolean.class, handler);

		handler = new ByteTypeHandler();
		register(Byte.class, handler);
		register(byte.class, handler);

		handler = new ShortTypeHandler();
		register(Short.class, handler);
		register(short.class, handler);

		handler = new IntegerTypeHandler();
		register(Integer.class, handler);
		register(int.class, handler);

		handler = new LongTypeHandler();
		register(Long.class, handler);
		register(long.class, handler);

		handler = new FloatTypeHandler();
		register(Float.class, handler);
		register(float.class, handler);

		handler = new DoubleTypeHandler();
		register(Double.class, handler);
		register(double.class, handler);

		register(String.class, new StringTypeHandler());

		register(BigDecimal.class, new BigDecimalTypeHandler());

		register(byte[].class, new ByteArrayTypeHandler());

		register(Object.class, new ObjectTypeHandler());
		register(Object.class, "OBJECT", new ObjectTypeHandler());

		register(Date.class, new DateTypeHandler());
		register(Date.class, "DATE", new DateOnlyTypeHandler());
		register(Date.class, "TIME", new TimeOnlyTypeHandler());

		putTypeAlias("string", String.class.getName());
		putTypeAlias("byte", Byte.class.getName());
		putTypeAlias("long", Long.class.getName());
		putTypeAlias("short", Short.class.getName());
		putTypeAlias("int", Integer.class.getName());
		putTypeAlias("integer", Integer.class.getName());
		putTypeAlias("double", Double.class.getName());
		putTypeAlias("float", Float.class.getName());
		putTypeAlias("boolean", Boolean.class.getName());
		putTypeAlias("date", Date.class.getName());
		putTypeAlias("decimal", BigDecimal.class.getName());
		putTypeAlias("object", Object.class.getName());
		putTypeAlias("map", Map.class.getName());
		putTypeAlias("hashmap", HashMap.class.getName());
		putTypeAlias("list", List.class.getName());
		putTypeAlias("arraylist", ArrayList.class.getName());
		putTypeAlias("collection", Collection.class.getName());
		putTypeAlias("iterator", Iterator.class.getName());
		putTypeAlias("cursor", java.sql.ResultSet.class.getName());

	}

	/* Public Methods */

	/**
	 * Get a TypeHandler for a class
	 * 
	 * @param type
	 *            - the class you want a TypeHandler for
	 * 
	 * @return - the handler
	 */
	public TypeHandler getTypeHandler(Class type) {
		return getTypeHandler(type, null);
	}

	/**
	 * Get a TypeHandler for a class and a JDBC type
	 * 
	 * @param type
	 *            - the class
	 * @param jdbcType
	 *            - the jdbc type
	 * 
	 * @return - the handler
	 */
	public TypeHandler getTypeHandler(Class type, String jdbcType) {
		Map jdbcHandlerMap = (Map) typeHandlerMap.get(type);
		TypeHandler handler = null;
		if (jdbcHandlerMap != null) {
			handler = (TypeHandler) jdbcHandlerMap.get(jdbcType);
			if (handler == null) {
				handler = (TypeHandler) jdbcHandlerMap.get(null);
			}
		}
		if (handler == null && type != null && Enum.class.isAssignableFrom(type)) {
			handler = new EnumTypeHandler(type);
		}
		return handler;
	}

	/**
	 * When in doubt, get the "unknown" type handler
	 * 
	 * @return - if I told you, it would not be unknown, would it?
	 */
	public TypeHandler getUnkownTypeHandler() {
		return unknownTypeHandler;
	}

	/**
	 * Tells you if a particular class has a TypeHandler
	 * 
	 * @param type
	 *            - the class
	 * 
	 * @return - true if there is a TypeHandler
	 */
	public boolean hasTypeHandler(Class type) {
		return type != null && (getTypeHandler(type) != null || Enum.class.isAssignableFrom(type));
	}

	/**
	 * Register (add) a type handler for a class
	 * 
	 * @param type
	 *            - the class
	 * @param handler
	 *            - the handler instance
	 */
	public void register(Class type, TypeHandler handler) {
		register(type, null, handler);
	}

	/**
	 * Register (add) a type handler for a class and JDBC type
	 * 
	 * @param type
	 *            - the class
	 * @param jdbcType
	 *            - the JDBC type
	 * @param handler
	 *            - the handler instance
	 */
	public void register(Class type, String jdbcType, TypeHandler handler) {
		Map map = (Map) typeHandlerMap.get(type);
		if (map == null) {
			map = new HashMap();
			typeHandlerMap.put(type, map);
		}
		map.put(jdbcType, handler);

		if (reversePrimitiveMap.containsKey(type)) {
			register((Class) reversePrimitiveMap.get(type), jdbcType, handler);
		}
	}

	/**
	 * Lookup an aliased class and return it's REAL name
	 * 
	 * @param string
	 *            - the alias
	 * 
	 * @return - the REAL name
	 */
	public String resolveAlias(String string) {
		String key = null;
		if (string != null)
			key = string.toLowerCase();
		String value = null;
		if (typeAliases.containsKey(key)) {
			value = (String) typeAliases.get(key);
		} else {
			value = string;
		}

		return value;
	}

	/**
	 * Adds a type alias that is case insensitive. All of the following String, string, StRiNg
	 * will equate to the same alias.
	 * 
	 * @param alias
	 *            - the alias
	 * @param value
	 *            - the real class name
	 */
	public void putTypeAlias(String alias, String value) {
		String key = null;
		if (alias != null)
			key = alias.toLowerCase();
		if (typeAliases.containsKey(key) && !typeAliases.get(key).equals(value)) {
			throw new SoapMapException("Error in XmlSqlMapClientBuilder.  Alias name conflict occurred.  The alias '"
				+ key + "' is already mapped to the value '" + typeAliases.get(alias) + "'.");
		}
		typeAliases.put(key, value);
	}

}
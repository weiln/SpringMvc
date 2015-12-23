package com.elf.soap.soapmap.engine.builder.xml;

import com.elf.soap.common.resources.Resources;
import com.elf.soap.common.xml.Nodelet;
import com.elf.soap.common.xml.NodeletException;
import com.elf.soap.common.xml.NodeletParser;
import com.elf.soap.common.xml.NodeletUtils;
import com.elf.soap.soapmap.client.SoapMapException;
import com.elf.soap.soapmap.engine.config.ParameterMapConfig;
import com.elf.soap.soapmap.engine.config.ResultMapConfig;
import com.elf.soap.soapmap.engine.mapping.statement.ExecuteStatement;

import org.w3c.dom.Node;

import java.io.InputStream;
import java.io.Reader;
import java.util.Properties;

/**
 * 
 * @author
 * 
 */
public class SoapMapParser {

	private final NodeletParser parser;
	private XmlParserState state;
	private SoapStatementParser statementParser;

	public SoapMapParser(XmlParserState state) {
		this.parser = new NodeletParser();
		this.state = state;

		parser.setValidation(true);
		parser.setEntityResolver(new SoapMapClasspathEntityResolver());

		statementParser = new SoapStatementParser(this.state);

		addSqlMapNodelets();
		addSqlNodelets();
		addTypeAliasNodelets();
		addParameterMapNodelets();
		addResultMapNodelets();
		addStatementNodelets();

	}

	public void parse(Reader reader) throws NodeletException {
		parser.parse(reader);
	}

	public void parse(InputStream inputStream) throws NodeletException {
		parser.parse(inputStream);
	}

	private void addSqlMapNodelets() {
		parser.addNodelet("/soapMap", new Nodelet() {
			public void process(Node node) throws Exception {
				Properties attributes = NodeletUtils.parseAttributes(node, state.getGlobalProps());
				state.setNamespace(attributes.getProperty("namespace"));
			}
		});
	}

	private void addSqlNodelets() {
		parser.addNodelet("/soapMap/soap", new Nodelet() {
			public void process(Node node) throws Exception {
				Properties attributes = NodeletUtils.parseAttributes(node, state.getGlobalProps());
				String id = attributes.getProperty("id");
				if (state.isUseStatementNamespaces()) {
					id = state.applyNamespace(id);
				}
				if (state.getSqlIncludes().containsKey(id)) {
					throw new SoapMapException("Duplicate <sql>-include '" + id + "' found.");
				} else {
					state.getSqlIncludes().put(id, node);
				}
			}
		});
	}

	private void addTypeAliasNodelets() {
		parser.addNodelet("/soapMap/typeAlias", new Nodelet() {
			public void process(Node node) throws Exception {
				Properties prop = NodeletUtils.parseAttributes(node, state.getGlobalProps());
				String alias = prop.getProperty("alias");
				String type = prop.getProperty("type");
				state.getConfig().getTypeHandlerFactory().putTypeAlias(alias, type);
			}
		});
	}

	private void addParameterMapNodelets() {
		parser.addNodelet("/soapMap/parameterMap/end()", new Nodelet() {
			public void process(Node node) throws Exception {
				state.getConfig().getErrorContext().setMoreInfo(null);
				state.getConfig().getErrorContext().setObjectId(null);
				state.setParamConfig(null);
			}
		});
		parser.addNodelet("/soapMap/parameterMap", new Nodelet() {
			public void process(Node node) throws Exception {
				Properties attributes = NodeletUtils.parseAttributes(node, state.getGlobalProps());
				String id = state.applyNamespace(attributes.getProperty("id"));
				String parameterClassName = attributes.getProperty("class");
				parameterClassName = state.getConfig().getTypeHandlerFactory().resolveAlias(parameterClassName);
				try {
					state.getConfig().getErrorContext().setMoreInfo("Check the parameter class.");
					ParameterMapConfig paramConf =
						state.getConfig().newParameterMapConfig(id, Resources.classForName(parameterClassName));
					state.setParamConfig(paramConf);
				} catch (Exception e) {
					throw new SoapMapException(
						"Error configuring ParameterMap.  Could not set ParameterClass.  Cause: " + e, e);
				}
			}
		});
		parser.addNodelet("/soapMap/parameterMap/parameter", new Nodelet() {
			public void process(Node node) throws Exception {
				Properties childAttributes = NodeletUtils.parseAttributes(node, state.getGlobalProps());
				String propertyName = childAttributes.getProperty("property");
				String jdbcType = childAttributes.getProperty("jdbcType");
				String type = childAttributes.getProperty("typeName");
				String javaType = childAttributes.getProperty("javaType");
				String resultMap = state.applyNamespace(childAttributes.getProperty("resultMap"));
				String nullValue = childAttributes.getProperty("nullValue");
				String mode = childAttributes.getProperty("mode");
				String callback = childAttributes.getProperty("typeHandler");
				String numericScaleProp = childAttributes.getProperty("numericScale");

				callback = state.getConfig().getTypeHandlerFactory().resolveAlias(callback);
				Object typeHandlerImpl = null;
				if (callback != null) {
					typeHandlerImpl = Resources.instantiate(callback);
				}

				javaType = state.getConfig().getTypeHandlerFactory().resolveAlias(javaType);
				Class javaClass = null;
				try {
					if (javaType != null && javaType.length() > 0) {
						javaClass = Resources.classForName(javaType);
					}
				} catch (ClassNotFoundException e) {
					throw new RuntimeException("Error setting javaType on parameter mapping.  Cause: " + e);
				}

				Integer numericScale = null;
				if (numericScaleProp != null) {
					numericScale = Integer.valueOf(numericScaleProp);
				}

				state.getParamConfig().addParameterMapping(propertyName, javaClass, jdbcType, nullValue, mode, type,
					numericScale, typeHandlerImpl, resultMap);
			}
		});
	}

	private void addResultMapNodelets() {
		parser.addNodelet("/soapMap/resultMap/end()", new Nodelet() {
			public void process(Node node) throws Exception {
				state.getConfig().getErrorContext().setMoreInfo(null);
				state.getConfig().getErrorContext().setObjectId(null);
			}
		});
		parser.addNodelet("/soapMap/resultMap", new Nodelet() {
			public void process(Node node) throws Exception {
				Properties attributes = NodeletUtils.parseAttributes(node, state.getGlobalProps());
				String id = state.applyNamespace(attributes.getProperty("id"));
				String resultClassName = attributes.getProperty("class");
				String extended = state.applyNamespace(attributes.getProperty("extends"));
				String xmlName = attributes.getProperty("xmlName");
				String groupBy = attributes.getProperty("groupBy");

				resultClassName = state.getConfig().getTypeHandlerFactory().resolveAlias(resultClassName);
				Class resultClass;
				try {
					state.getConfig().getErrorContext().setMoreInfo("Check the result class.");
					resultClass = Resources.classForName(resultClassName);
				} catch (Exception e) {
					throw new RuntimeException("Error configuring Result.  Could not set ResultClass.  Cause: " + e, e);
				}
				ResultMapConfig resultConf =
					state.getConfig().newResultMapConfig(id, resultClass, groupBy, extended, xmlName);
				state.setResultConfig(resultConf);
			}
		});
		parser.addNodelet("/soapMap/resultMap/result", new Nodelet() {
			public void process(Node node) throws Exception {
				Properties childAttributes = NodeletUtils.parseAttributes(node, state.getGlobalProps());
				String propertyName = childAttributes.getProperty("property");
				String nullValue = childAttributes.getProperty("nullValue");
				String jdbcType = childAttributes.getProperty("jdbcType");
				String javaType = childAttributes.getProperty("javaType");
				String columnName = childAttributes.getProperty("column");
				String columnIndexProp = childAttributes.getProperty("columnIndex");
				String statementName = childAttributes.getProperty("select");
				String resultMapName = childAttributes.getProperty("resultMap");
				String callback = childAttributes.getProperty("typeHandler");
				String notNullColumn = childAttributes.getProperty("notNullColumn");

				state.getConfig().getErrorContext().setMoreInfo("Check the result mapping property type or name.");
				Class javaClass = null;
				try {
					javaType = state.getConfig().getTypeHandlerFactory().resolveAlias(javaType);
					if (javaType != null && javaType.length() > 0) {
						javaClass = Resources.classForName(javaType);
					}
				} catch (ClassNotFoundException e) {
					throw new RuntimeException("Error setting java type on result discriminator mapping.  Cause: " + e);
				}

				state
					.getConfig()
					.getErrorContext()
					.setMoreInfo(
						"Check the result mapping typeHandler attribute '" + callback
							+ "' (must be a TypeHandler or TypeHandlerCallback implementation).");
				Object typeHandlerImpl = null;
				try {
					if (callback != null && callback.length() > 0) {
						callback = state.getConfig().getTypeHandlerFactory().resolveAlias(callback);
						typeHandlerImpl = Resources.instantiate(callback);
					}
				} catch (Exception e) {
					throw new RuntimeException("Error occurred during custom type handler configuration.  Cause: " + e,
						e);
				}

				Integer columnIndex = null;
				if (columnIndexProp != null) {
					try {
						columnIndex = Integer.valueOf(columnIndexProp);
					} catch (Exception e) {
						throw new RuntimeException("Error parsing column index.  Cause: " + e, e);
					}
				}

				state.getResultConfig().addResultMapping(propertyName, columnName, columnIndex, javaClass, jdbcType,
					nullValue, notNullColumn, statementName, resultMapName, typeHandlerImpl);
			}
		});

		parser.addNodelet("/soapMap/resultMap/discriminator/subMap", new Nodelet() {
			public void process(Node node) throws Exception {
				Properties childAttributes = NodeletUtils.parseAttributes(node, state.getGlobalProps());
				String value = childAttributes.getProperty("value");
				String resultMap = childAttributes.getProperty("resultMap");
				resultMap = state.applyNamespace(resultMap);
				state.getResultConfig().addDiscriminatorSubMap(value, resultMap);
			}
		});

		parser.addNodelet("/soapMap/resultMap/discriminator", new Nodelet() {
			public void process(Node node) throws Exception {
				Properties childAttributes = NodeletUtils.parseAttributes(node, state.getGlobalProps());
				String nullValue = childAttributes.getProperty("nullValue");
				String jdbcType = childAttributes.getProperty("jdbcType");
				String javaType = childAttributes.getProperty("javaType");
				String columnName = childAttributes.getProperty("column");
				String columnIndexProp = childAttributes.getProperty("columnIndex");
				String callback = childAttributes.getProperty("typeHandler");

				state.getConfig().getErrorContext().setMoreInfo("Check the disriminator type or name.");
				Class javaClass = null;
				try {
					javaType = state.getConfig().getTypeHandlerFactory().resolveAlias(javaType);
					if (javaType != null && javaType.length() > 0) {
						javaClass = Resources.classForName(javaType);
					}
				} catch (ClassNotFoundException e) {
					throw new RuntimeException("Error setting java type on result discriminator mapping.  Cause: " + e);
				}

				state
					.getConfig()
					.getErrorContext()
					.setMoreInfo(
						"Check the result mapping discriminator typeHandler attribute '" + callback
							+ "' (must be a TypeHandlerCallback implementation).");
				Object typeHandlerImpl = null;
				try {
					if (callback != null && callback.length() > 0) {
						callback = state.getConfig().getTypeHandlerFactory().resolveAlias(callback);
						typeHandlerImpl = Resources.instantiate(callback);
					}
				} catch (Exception e) {
					throw new RuntimeException("Error occurred during custom type handler configuration.  Cause: " + e,
						e);
				}

				Integer columnIndex = null;
				if (columnIndexProp != null) {
					try {
						columnIndex = Integer.valueOf(columnIndexProp);
					} catch (Exception e) {
						throw new RuntimeException("Error parsing column index.  Cause: " + e, e);
					}
				}

				state.getResultConfig().setDiscriminator(columnName, columnIndex, javaClass, jdbcType, nullValue,
					typeHandlerImpl);
			}
		});
	}

	protected void addStatementNodelets() {
		parser.addNodelet("/soapMap/execute", new Nodelet() {
			public void process(Node node) throws Exception {
				statementParser.parseGeneralStatement(node, new ExecuteStatement());
			}
		});
	}

}

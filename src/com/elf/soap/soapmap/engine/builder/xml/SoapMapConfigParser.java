package com.elf.soap.soapmap.engine.builder.xml;

import com.elf.soap.common.resources.Resources;
import com.elf.soap.common.xml.Nodelet;
import com.elf.soap.common.xml.NodeletParser;
import com.elf.soap.common.xml.NodeletUtils;
import com.elf.soap.soapmap.client.SoapMapClient;
import com.elf.soap.soapmap.client.SoapMapException;
import com.elf.soap.soapmap.engine.config.SoapMapConfiguration;
import com.elf.soap.soapmap.engine.mapping.result.ResultObjectFactory;

import org.w3c.dom.Node;

import java.io.InputStream;
import java.io.Reader;
import java.util.Properties;

/**
 * 
 * @author xujiakun
 * 
 */
public class SoapMapConfigParser {

	protected final NodeletParser parser = new NodeletParser();

	private XmlParserState state = new XmlParserState();

	private boolean usingStreams = false;

	public SoapMapConfigParser() {
		parser.setValidation(true);
		parser.setEntityResolver(new SoapMapClasspathEntityResolver());

		addSqlMapConfigNodelets();
		addGlobalPropNodelets();
		addSettingsNodelets();
		addTypeAliasNodelets();
		addTypeHandlerNodelets();
		addSqlMapNodelets();
		addResultObjectFactoryNodelets();
	}

	public SoapMapClient parse(Reader reader, Properties props) {
		if (props != null)
			state.setGlobalProps(props);
		return parse(reader);
	}

	public SoapMapClient parse(Reader reader) {
		try {
			usingStreams = false;

			parser.parse(reader);
			return state.getConfig().getClient();
		} catch (Exception e) {
			throw new RuntimeException("Error occurred.  Cause: " + e, e);
		}
	}

	public SoapMapClient parse(InputStream inputStream, Properties props) {
		if (props != null) {
			state.setGlobalProps(props);
		}

		return parse(inputStream);
	}

	public SoapMapClient parse(InputStream inputStream) {
		try {
			usingStreams = true;

			parser.parse(inputStream);
			return state.getConfig().getClient();
		} catch (Exception e) {
			throw new RuntimeException("Error occurred.  Cause: " + e, e);
		}
	}

	private void addSqlMapConfigNodelets() {
		parser.addNodelet("/soapMapConfig/end()", new Nodelet() {
			public void process(Node node) throws Exception {
				state.getConfig().finalizeSqlMapConfig();
			}
		});
	}

	private void addGlobalPropNodelets() {
		parser.addNodelet("/soapMapConfig/properties", new Nodelet() {
			public void process(Node node) throws Exception {
				Properties attributes = NodeletUtils.parseAttributes(node, state.getGlobalProps());
				String resource = attributes.getProperty("resource");
				String url = attributes.getProperty("url");
				state.setGlobalProperties(resource, url);
			}
		});
	}

	private void addSettingsNodelets() {
		parser.addNodelet("/soapMapConfig/settings", new Nodelet() {
			public void process(Node node) throws Exception {
				Properties attributes = NodeletUtils.parseAttributes(node, state.getGlobalProps());
				SoapMapConfiguration config = state.getConfig();

				String classInfoCacheEnabledAttr = attributes.getProperty("classInfoCacheEnabled");
				boolean classInfoCacheEnabled =
					(classInfoCacheEnabledAttr == null || "true".equals(classInfoCacheEnabledAttr));
				config.setClassInfoCacheEnabled(classInfoCacheEnabled);

				String statementCachingEnabledAttr = attributes.getProperty("statementCachingEnabled");
				boolean statementCachingEnabled =
					(statementCachingEnabledAttr == null || "true".equals(statementCachingEnabledAttr));
				config.setStatementCachingEnabled(statementCachingEnabled);

				String enhancementEnabledAttr = attributes.getProperty("enhancementEnabled");
				boolean enhancementEnabled = (enhancementEnabledAttr == null || "true".equals(enhancementEnabledAttr));
				config.setEnhancementEnabled(enhancementEnabled);

				String forceMultipleResultSetSupportAttr = attributes.getProperty("forceMultipleResultSetSupport");
				boolean forceMultipleResultSetSupport = "true".equals(forceMultipleResultSetSupportAttr);
				config.setForceMultipleResultSetSupport(forceMultipleResultSetSupport);

				String defaultTimeoutAttr = attributes.getProperty("defaultStatementTimeout");
				Integer defaultTimeout = defaultTimeoutAttr == null ? null : Integer.valueOf(defaultTimeoutAttr);
				config.setDefaultStatementTimeout(defaultTimeout);

				String useStatementNamespacesAttr = attributes.getProperty("useStatementNamespaces");
				boolean useStatementNamespaces = "true".equals(useStatementNamespacesAttr);
				state.setUseStatementNamespaces(useStatementNamespaces);
			}
		});
	}

	private void addTypeAliasNodelets() {
		parser.addNodelet("/soapMapConfig/typeAlias", new Nodelet() {
			public void process(Node node) throws Exception {
				Properties prop = NodeletUtils.parseAttributes(node, state.getGlobalProps());
				String alias = prop.getProperty("alias");
				String type = prop.getProperty("type");
				state.getConfig().getTypeHandlerFactory().putTypeAlias(alias, type);
			}
		});
	}

	private void addTypeHandlerNodelets() {
		parser.addNodelet("/soapMapConfig/typeHandler", new Nodelet() {
			public void process(Node node) throws Exception {
				Properties prop = NodeletUtils.parseAttributes(node, state.getGlobalProps());
				String jdbcType = prop.getProperty("jdbcType");
				String javaType = prop.getProperty("javaType");
				String callback = prop.getProperty("callback");

				javaType = state.getConfig().getTypeHandlerFactory().resolveAlias(javaType);
				callback = state.getConfig().getTypeHandlerFactory().resolveAlias(callback);

				state.getConfig().newTypeHandler(Resources.classForName(javaType), jdbcType,
					Resources.instantiate(callback));
			}
		});
	}

	protected void addSqlMapNodelets() {
		parser.addNodelet("/soapMapConfig/soapMap", new Nodelet() {
			public void process(Node node) throws Exception {
				state.getConfig().getErrorContext().setActivity("loading the SQL Map resource");

				Properties attributes = NodeletUtils.parseAttributes(node, state.getGlobalProps());

				String resource = attributes.getProperty("resource");
				String url = attributes.getProperty("url");

				if (usingStreams) {
					InputStream inputStream = null;
					if (resource != null) {
						state.getConfig().getErrorContext().setResource(resource);
						inputStream = Resources.getResourceAsStream(resource);
					} else if (url != null) {
						state.getConfig().getErrorContext().setResource(url);
						inputStream = Resources.getUrlAsStream(url);
					} else {
						throw new SoapMapException(
							"The <soapMap> element requires either a resource or a url attribute.");
					}

					new SoapMapParser(state).parse(inputStream);
				} else {
					Reader reader = null;
					if (resource != null) {
						state.getConfig().getErrorContext().setResource(resource);
						reader = Resources.getResourceAsReader(resource);
					} else if (url != null) {
						state.getConfig().getErrorContext().setResource(url);
						reader = Resources.getUrlAsReader(url);
					} else {
						throw new SoapMapException(
							"The <soapMap> element requires either a resource or a url attribute.");
					}

					new SoapMapParser(state).parse(reader);
				}
			}
		});
	}

	private void addResultObjectFactoryNodelets() {
		parser.addNodelet("/soapMapConfig/resultObjectFactory", new Nodelet() {
			public void process(Node node) throws Exception {
				Properties attributes = NodeletUtils.parseAttributes(node, state.getGlobalProps());
				String type = attributes.getProperty("type");

				state.getConfig().getErrorContext().setActivity("configuring the Result Object Factory");
				ResultObjectFactory rof;
				try {
					rof = (ResultObjectFactory) Resources.instantiate(type);
					state.getConfig().setResultObjectFactory(rof);
				} catch (Exception e) {
					throw new SoapMapException("Error instantiating resultObjectFactory: " + type, e);
				}

			}
		});
		parser.addNodelet("/soapMapConfig/resultObjectFactory/property", new Nodelet() {
			public void process(Node node) throws Exception {
				Properties attributes = NodeletUtils.parseAttributes(node, state.getGlobalProps());
				String name = attributes.getProperty("name");
				String value =
					NodeletUtils.parsePropertyTokens(attributes.getProperty("value"), state.getGlobalProps());
				state.getConfig().getDelegate().getResultObjectFactory().setProperty(name, value);
			}
		});
	}

}

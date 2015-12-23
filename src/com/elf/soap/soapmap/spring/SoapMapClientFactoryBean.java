package com.elf.soap.soapmap.spring;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Properties;

import com.elf.soap.common.xml.NodeletException;
import com.elf.soap.soapmap.client.SoapMapClient;
import com.elf.soap.soapmap.engine.builder.xml.SoapMapConfigParser;
import com.elf.soap.soapmap.engine.builder.xml.SoapMapParser;
import com.elf.soap.soapmap.engine.builder.xml.XmlParserState;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.NestedIOException;
import org.springframework.core.io.Resource;
import org.springframework.util.ObjectUtils;

/**
 * 
 * @author xujiakun
 * 
 */
public class SoapMapClientFactoryBean implements FactoryBean, InitializingBean {

	private Resource[] configLocations;

	private Resource[] mappingLocations;

	private Properties soapMapClientProperties;

	private SoapMapClient soapMapClient;

	/**
	 * Set the location of the SoapMapClient config file.
	 * A typical value is "WEB-INF/soap-map-config.xml".
	 * 
	 * @see #setConfigLocations
	 */
	public void setConfigLocation(Resource configLocation) {
		this.configLocations = (configLocation != null ? new Resource[] { configLocation } : null);
	}

	/**
	 * Set multiple locations of SoapMapClient config files that
	 * are going to be merged into one unified configuration at runtime.
	 */
	public void setConfigLocations(Resource[] configLocations) {
		if (configLocations != null) {
			this.configLocations = Arrays.copyOf(configLocations, configLocations.length);
		}
	}

	/**
	 * Set locations of soap-map mapping files that are going to be
	 * merged into the SoapMapClient configuration at runtime.
	 * <p>
	 * This is an alternative to specifying "&lt;soapMap&gt;" entries in a soap-map-client
	 * config file. This property being based on Spring's resource abstraction also allows for
	 * specifying resource patterns here: e.g. "/myApp/*-map.xml".
	 * <p>
	 */
	public void setMappingLocations(Resource[] mappingLocations) {
		if (mappingLocations != null) {
			this.mappingLocations = Arrays.copyOf(mappingLocations, mappingLocations.length);
		}
	}

	/**
	 * Set optional properties to be passed into the SqlMapClientBuilder, as
	 * alternative to a <code>&lt;properties&gt;</code> tag in the sql-map-config.xml
	 * file. Will be used to resolve placeholders in the config file.
	 * 
	 * @see #setConfigLocation
	 * @see com.ibatis.sqlmap.client.SqlMapClientBuilder#buildSqlMapClient(java.io.InputStream,
	 *      java.util.Properties)
	 */
	public void setSqlMapClientProperties(Properties sqlMapClientProperties) {
		this.soapMapClientProperties = sqlMapClientProperties;
	}

	public void afterPropertiesSet() throws Exception {
		this.soapMapClient =
			buildSoapMapClient(this.configLocations, this.mappingLocations, this.soapMapClientProperties);
	}

	/**
	 * Build a SoapMapClient instance based on the given standard configuration.
	 * 
	 * @param configLocations
	 *            the config files to load from
	 * @param properties
	 *            the SoapMapClient properties (if any)
	 * @return the SoapMapClient instance (never <code>null</code>)
	 * @throws IOException
	 *             if loading the config file failed
	 * @see com.ibatis.sqlmap.client.SqlMapClientBuilder#buildSqlMapClient
	 */
	protected SoapMapClient buildSoapMapClient(Resource[] configLocations, Resource[] mappingLocations,
		Properties properties) throws IOException {

		if (ObjectUtils.isEmpty(configLocations)) {
			throw new IllegalArgumentException("At least 1 'configLocation' entry is required");
		}

		SoapMapClient client = null;
		SoapMapConfigParser configParser = new SoapMapConfigParser();
		for (int i = 0; i < configLocations.length; i++) {
			InputStream is = configLocations[i].getInputStream();
			try {
				client = configParser.parse(is, properties);
			} catch (RuntimeException ex) {
				throw new NestedIOException("Failed to parse config resource: " + configLocations[i], ex.getCause());
			}
		}

		if (mappingLocations != null) {
			SoapMapParser mapParser = SoapMapParserFactory.createSqlMapParser(configParser);
			for (int i = 0; i < mappingLocations.length; i++) {
				try {
					mapParser.parse(mappingLocations[i].getInputStream());
				} catch (NodeletException ex) {
					throw new NestedIOException("Failed to parse mapping resource: " + mappingLocations[i], ex);
				}
			}
		}

		return client;
	}

	public Object getObject() {
		return this.soapMapClient;
	}

	public Class getObjectType() {
		return (this.soapMapClient != null ? this.soapMapClient.getClass() : SoapMapClient.class);
	}

	public boolean isSingleton() {
		return true;
	}

	/**
	 * Inner class to avoid hard-coded dependency (XmlParserState class).
	 */
	private static class SoapMapParserFactory {

		public static SoapMapParser createSqlMapParser(SoapMapConfigParser configParser) {
			// Should raise an enhancement request with ...
			XmlParserState state = null;
			try {
				Field stateField = SoapMapConfigParser.class.getDeclaredField("state");
				stateField.setAccessible(true);
				state = (XmlParserState) stateField.get(configParser);
			} catch (Exception ex) {
				throw new IllegalStateException("'state' field not found in SoapMapConfigParser class - "
					+ "please upgrade to higher in order to use the new 'mappingLocations' feature. " + ex);
			}
			return new SoapMapParser(state);
		}
	}

}

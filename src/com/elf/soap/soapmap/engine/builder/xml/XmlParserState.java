package com.elf.soap.soapmap.engine.builder.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import com.elf.soap.common.resources.Resources;
import com.elf.soap.soapmap.engine.config.ParameterMapConfig;
import com.elf.soap.soapmap.engine.config.ResultMapConfig;
import com.elf.soap.soapmap.engine.config.SoapMapConfiguration;


/**
 * 
 * @author
 * 
 */
public class XmlParserState {

	private SoapMapConfiguration config = new SoapMapConfiguration();

	private Properties globalProps = new Properties();
	private Properties txProps = new Properties();
	private Properties dsProps = new Properties();
	private Properties cacheProps = new Properties();
	private boolean useStatementNamespaces = false;
	private Map sqlIncludes = new HashMap();

	private ParameterMapConfig paramConfig;
	private ResultMapConfig resultConfig;

	private String namespace;

	public SoapMapConfiguration getConfig() {
		return config;
	}

	public void setGlobalProps(Properties props) {
		globalProps = props;
	}

	public Properties getGlobalProps() {
		return globalProps;
	}

	public Properties getTxProps() {
		return txProps;
	}

	public Properties getDsProps() {
		return dsProps;
	}

	public Properties getCacheProps() {
		return cacheProps;
	}

	public void setUseStatementNamespaces(boolean useStatementNamespaces) {
		this.useStatementNamespaces = useStatementNamespaces;
	}

	public boolean isUseStatementNamespaces() {
		return useStatementNamespaces;
	}

	public Map getSqlIncludes() {
		return sqlIncludes;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String applyNamespace(String id) {
		String newId = id;
		if (namespace != null && namespace.length() > 0 && id != null && id.indexOf('.') < 0) {
			newId = namespace + "." + id;
		}
		return newId;
	}

	public ParameterMapConfig getParamConfig() {
		return paramConfig;
	}

	public void setParamConfig(ParameterMapConfig paramConfig) {
		this.paramConfig = paramConfig;
	}

	public ResultMapConfig getResultConfig() {
		return resultConfig;
	}

	public void setResultConfig(ResultMapConfig resultConfig) {
		this.resultConfig = resultConfig;
	}

	public String getFirstToken(String s) {
		return new StringTokenizer(s, ", ", false).nextToken();
	}

	public String[] getAllButFirstToken(String s) {
		List strings = new ArrayList();
		StringTokenizer parser = new StringTokenizer(s, ", ", false);
		parser.nextToken();
		while (parser.hasMoreTokens()) {
			strings.add(parser.nextToken());
		}
		return (String[]) strings.toArray(new String[strings.size()]);
	}

	public void setGlobalProperties(String resource, String url) {
		config.getErrorContext().setActivity("loading global properties");
		try {
			Properties props;
			if (resource != null) {
				config.getErrorContext().setResource(resource);
				props = Resources.getResourceAsProperties(resource);
			} else if (url != null) {
				config.getErrorContext().setResource(url);
				props = Resources.getUrlAsProperties(url);
			} else {
				throw new RuntimeException("The " + "properties"
					+ " element requires either a resource or a url attribute.");
			}

			// Merge properties with those passed in programmatically
			if (props != null) {
				props.putAll(globalProps);
				globalProps = props;
			}
		} catch (Exception e) {
			throw new RuntimeException("Error loading properties.  Cause: " + e, e);
		}
	}

}

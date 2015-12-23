package com.elf.soap.soapmap.engine.exchange;

import com.elf.soap.common.beans.Probe;
import com.elf.soap.common.beans.ProbeFactory;
import com.elf.soap.soapmap.client.SoapMapException;
import com.elf.soap.soapmap.engine.mapping.parameter.ParameterMap;
import com.elf.soap.soapmap.engine.mapping.parameter.ParameterMapping;
import com.elf.soap.soapmap.engine.mapping.result.ResultMap;
import com.elf.soap.soapmap.engine.mapping.result.ResultMapping;
import com.elf.soap.soapmap.engine.scope.StatementScope;

import org.w3c.dom.Document;

import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * A DataExchange implemtation for working with DOM objects.
 */
public class DomDataExchange extends BaseDataExchange implements DataExchange {

	/**
	 * Constructor for the factory.
	 * 
	 * @param dataExchangeFactory
	 *            - the factory
	 */
	public DomDataExchange(DataExchangeFactory dataExchangeFactory) {
		super(dataExchangeFactory);
	}

	public void initialize(Map properties) {
	}

	public Object[] getData(StatementScope statementScope, ParameterMap parameterMap, Object parameterObject) {
		Probe probe = ProbeFactory.getProbe(parameterObject);

		ParameterMapping[] mappings = parameterMap.getParameterMappings();
		Object[] values = new Object[mappings.length];

		for (int i = 0; i < mappings.length; i++) {
			values[i] = probe.getObject(parameterObject, mappings[i].getPropertyName());
		}

		return values;
	}

	public Object setData(StatementScope statementScope, ResultMap resultMap, Object resultObject, Object[] values) {

		String name = ((ResultMap) resultMap).getXmlName();
		if (name == null) {
			name = "result";
		}

		if (resultObject == null) {
			try {
				Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
				doc.appendChild(doc.createElement(name));
				resultObject = doc;
			} catch (ParserConfigurationException e) {
				throw new SoapMapException("Error creating new Document for DOM result.  Cause: " + e, e);
			}
		}

		Probe probe = ProbeFactory.getProbe(resultObject);

		ResultMapping[] mappings = resultMap.getResultMappings();

		for (int i = 0; i < mappings.length; i++) {
			if (values[i] != null) {
				probe.setObject(resultObject, mappings[i].getPropertyName(), values[i]);
			}
		}

		return resultObject;
	}

	public Object setData(StatementScope statementScope, ParameterMap parameterMap, Object parameterObject,
		Object[] values) {
		Probe probe = ProbeFactory.getProbe(parameterObject);

		ParameterMapping[] mappings = parameterMap.getParameterMappings();

		for (int i = 0; i < mappings.length; i++) {
			if (values[i] != null) {
				if (mappings[i].isOutputAllowed()) {
					probe.setObject(parameterObject, mappings[i].getPropertyName(), values[i]);
				}
			}
		}

		return parameterObject;
	}

}

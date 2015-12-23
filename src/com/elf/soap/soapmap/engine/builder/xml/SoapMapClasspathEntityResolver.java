package com.elf.soap.soapmap.engine.builder.xml;

import com.elf.soap.common.resources.Resources;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Offline entity resolver for the KINTIGER DTDs.
 * 
 * @author
 * 
 */
public class SoapMapClasspathEntityResolver implements EntityResolver {

	private static final Logger logger = LoggerFactory.getLogger(SoapMapClasspathEntityResolver.class);

	private static final String SOAP_MAP_CONFIG_DTD = "com/elf/soap/soapmap/engine/builder/xml/soap-map-config.dtd";
	private static final String SOAP_MAP_DTD = "com/elf/soap/soapmap/engine/builder/xml/soap-map.dtd";

	private static final Map<String, String> DOC_TYPE_MAP = new HashMap<String, String>();

	static {
		DOC_TYPE_MAP.put("http://www.kintiger.com/dtd/soap-map-config.dtd".toUpperCase(), SOAP_MAP_CONFIG_DTD);
		DOC_TYPE_MAP.put("-//KINTIGER.com//DTD SOAP Map Config//EN".toUpperCase(), SOAP_MAP_CONFIG_DTD);

		DOC_TYPE_MAP.put("http://www.kintiger.com/dtd/soap-map.dtd".toUpperCase(), SOAP_MAP_DTD);
		DOC_TYPE_MAP.put("-//KINTIGER.com//DTD SOAP Map//EN".toUpperCase(), SOAP_MAP_DTD);
	}

	/**
	 * Converts a public DTD into a local one.
	 * 
	 * @param publicId
	 *            Unused but required by EntityResolver interface
	 * @param systemId
	 *            The DTD that is being requested
	 * @return The InputSource for the DTD
	 * @throws SAXException
	 *             If anything goes wrong
	 */
	public InputSource resolveEntity(String publicId, String systemId) throws SAXException {

		if (publicId != null) {
			publicId = publicId.toUpperCase();
		}
		if (systemId != null) {
			systemId = systemId.toUpperCase();
		}

		InputSource source = null;
		try {
			String path = (String) DOC_TYPE_MAP.get(publicId);
			source = getInputSource(path, source);
			if (source == null) {
				path = (String) DOC_TYPE_MAP.get(systemId);
				source = getInputSource(path, source);
			}
		} catch (Exception e) {
			throw new SAXException(e.toString());
		}
		return source;
	}

	private InputSource getInputSource(String path, InputSource source) {
		if (path != null) {
			InputStream in = null;
			try {
				in = Resources.getResourceAsStream(path);
				source = new InputSource(in);
			} catch (IOException e) {
				// ignore, null is ok
				logger.error(e.getMessage());
			}
		}
		return source;
	}

}

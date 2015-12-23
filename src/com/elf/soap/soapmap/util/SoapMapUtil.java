package com.elf.soap.soapmap.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Visitor;

/**
 * 
 * @author xujiakun
 * 
 */
public final class SoapMapUtil {

	static Logger logger = LoggerFactory.getLogger(SoapMapUtil.class);

	private SoapMapUtil() {

	}

	/**
	 * 
	 * @param str
	 * @return
	 */
	public static Object parse(String str) {
		return parse(str, null);
	}

	/**
	 * 
	 * @param str
	 * @param object
	 * @return
	 */
	public static Object parse(String str, Object object) {
		Document document = null;
		try {
			document = DocumentHelper.parseText(str);
		} catch (DocumentException e) {
			logger.error(e.getMessage());
			return object;
		}

		if (object == null) {
			object = new BaseEnvelope();
		}

		document.accept((Visitor) object);

		return object;
	}

}

package com.elf.soap.soapmap.engine.type;

import com.elf.soap.soapmap.client.SoapMapException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 
 * @author
 * 
 */
public class SimpleDateFormatter {
	public static Date format(String format, String datetime) {
		try {
			return new SimpleDateFormat(format).parse(datetime);
		} catch (ParseException e) {
			throw new SoapMapException("Error parsing default null value date.  Format must be '" + format
				+ "'. Cause: " + e);
		}
	}

}

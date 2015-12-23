package com.elf.soap.common.logging.log4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elf.soap.common.logging.Log;

/**
 * 
 * @author
 * 
 */
public class Log4jImpl implements Log {

	private Logger log;

	public Log4jImpl(Class clazz) {
		log = LoggerFactory.getLogger(clazz);
	}

	public boolean isDebugEnabled() {
		return log.isDebugEnabled();
	}

	public void error(String s, Throwable e) {
		log.error(s, e);
	}

	public void error(String s) {
		log.error(s);
	}

	public void debug(String s) {
		log.debug(s);
	}

	public void warn(String s) {
		log.warn(s);
	}

}

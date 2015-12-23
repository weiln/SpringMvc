package com.elf.soap.soapmap.engine.mapping.soap.dynamic.elements;

/**
 * 
 * @author
 * 
 */
public interface SoapTagHandler {

	// BODY TAG
	int SKIP_BODY = 0;
	int INCLUDE_BODY = 1;
	int REPEAT_BODY = 2;

	/**
	 * 
	 * @param ctx
	 * @param tag
	 * @param parameterObject
	 * @return
	 */
	int doStartFragment(SoapTagContext ctx, SoapTag tag, Object parameterObject);

	/**
	 * 
	 * @param ctx
	 * @param tag
	 * @param parameterObject
	 * @param bodyContent
	 * @return
	 */
	int doEndFragment(SoapTagContext ctx, SoapTag tag, Object parameterObject, StringBuffer bodyContent);

	/**
	 * 
	 * @param ctx
	 * @param tag
	 * @param parameterObject
	 * @param bodyContent
	 */
	void doPrepend(SoapTagContext ctx, SoapTag tag, Object parameterObject, StringBuffer bodyContent);

}

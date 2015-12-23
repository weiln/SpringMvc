package com.elf.soap.soapmap.engine.mapping.soap.dynamic.elements;

/**
 * 
 * @author
 * 
 */
public class IsNotEmptyTagHandler extends IsEmptyTagHandler {

	public boolean isCondition(SoapTagContext ctx, SoapTag tag, Object parameterObject) {
		return !super.isCondition(ctx, tag, parameterObject);
	}

}

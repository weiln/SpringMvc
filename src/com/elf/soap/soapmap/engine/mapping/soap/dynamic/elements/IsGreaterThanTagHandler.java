package com.elf.soap.soapmap.engine.mapping.soap.dynamic.elements;

/**
 * 
 * @author
 * 
 */
public class IsGreaterThanTagHandler extends ConditionalTagHandler {

	public boolean isCondition(SoapTagContext ctx, SoapTag tag, Object parameterObject) {
		long x = compare(ctx, tag, parameterObject);
		return x > 0 && x != ConditionalTagHandler.NOT_COMPARABLE;
	}

}

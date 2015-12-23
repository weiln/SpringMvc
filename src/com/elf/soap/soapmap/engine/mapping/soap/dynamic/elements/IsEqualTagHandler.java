package com.elf.soap.soapmap.engine.mapping.soap.dynamic.elements;

/**
 * 
 * @author
 * 
 */
public class IsEqualTagHandler extends ConditionalTagHandler {

	public boolean isCondition(SoapTagContext ctx, SoapTag tag, Object parameterObject) {
		return compare(ctx, tag, parameterObject) == 0;
	}

}

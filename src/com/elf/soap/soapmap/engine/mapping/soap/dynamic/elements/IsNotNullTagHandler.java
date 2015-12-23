package com.elf.soap.soapmap.engine.mapping.soap.dynamic.elements;

/**
 * 
 * @author
 * 
 */
public class IsNotNullTagHandler extends IsNullTagHandler {

	public boolean isCondition(SoapTagContext ctx, SoapTag tag, Object parameterObject) {
		return !super.isCondition(ctx, tag, parameterObject);
	}

}

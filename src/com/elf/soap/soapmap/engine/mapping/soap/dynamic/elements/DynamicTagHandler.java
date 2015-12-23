package com.elf.soap.soapmap.engine.mapping.soap.dynamic.elements;

/**
 * 
 * @author
 * 
 */
public class DynamicTagHandler extends BaseTagHandler {

	public int doStartFragment(SoapTagContext ctx, SoapTag tag, Object parameterObject) {
		ctx.pushRemoveFirstPrependMarker(tag);
		return BaseTagHandler.INCLUDE_BODY;
	}

}

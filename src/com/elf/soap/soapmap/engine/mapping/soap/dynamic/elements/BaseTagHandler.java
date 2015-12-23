package com.elf.soap.soapmap.engine.mapping.soap.dynamic.elements;

/**
 * 
 * @author
 * 
 */
public abstract class BaseTagHandler implements SoapTagHandler {

	public int doStartFragment(SoapTagContext ctx, SoapTag tag, Object parameterObject) {
		ctx.pushRemoveFirstPrependMarker(tag);
		return SoapTagHandler.INCLUDE_BODY;
	}

	public int doEndFragment(SoapTagContext ctx, SoapTag tag, Object parameterObject, StringBuffer bodyContent) {
		if (tag.isCloseAvailable() && !(tag.getHandler() instanceof IterateTagHandler)) {
			if (bodyContent.toString().trim().length() > 0) {
				bodyContent.append(tag.getCloseAttr());
			}
		}
		return SoapTagHandler.INCLUDE_BODY;
	}

	public void doPrepend(SoapTagContext ctx, SoapTag tag, Object parameterObject, StringBuffer bodyContent) {

		if (tag.isOpenAvailable() && !(tag.getHandler() instanceof IterateTagHandler)) {
			if (bodyContent.toString().trim().length() > 0) {
				bodyContent.insert(0, tag.getOpenAttr());
			}
		}

		if (tag.isPrependAvailable()) {
			if (bodyContent.toString().trim().length() > 0) {
				if (tag.getParent() != null && ctx.peekRemoveFirstPrependMarker(tag)) {
					ctx.disableRemoveFirstPrependMarker();
				} else {
					bodyContent.insert(0, tag.getPrependAttr());
				}
			}
		}

	}

}

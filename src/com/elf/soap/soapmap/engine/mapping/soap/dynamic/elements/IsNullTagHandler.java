package com.elf.soap.soapmap.engine.mapping.soap.dynamic.elements;

import com.elf.soap.common.beans.Probe;
import com.elf.soap.common.beans.ProbeFactory;

/**
 * 
 * @author
 * 
 */
public class IsNullTagHandler extends ConditionalTagHandler {

	private static final Probe PROBE = ProbeFactory.getProbe();

	public boolean isCondition(SoapTagContext ctx, SoapTag tag, Object parameterObject) {
		if (parameterObject == null) {
			return true;
		} else {
			String prop = getResolvedProperty(ctx, tag);
			Object value;
			if (prop != null) {
				value = PROBE.getObject(parameterObject, prop);
			} else {
				value = parameterObject;
			}
			return value == null;
		}
	}

}

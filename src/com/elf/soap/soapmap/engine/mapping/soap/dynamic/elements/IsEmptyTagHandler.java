package com.elf.soap.soapmap.engine.mapping.soap.dynamic.elements;

import java.lang.reflect.Array;
import java.util.Collection;

import com.elf.soap.common.beans.Probe;
import com.elf.soap.common.beans.ProbeFactory;

/**
 * 
 * @author
 * 
 */
public class IsEmptyTagHandler extends ConditionalTagHandler {

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
			if (value instanceof Collection) {
				return ((Collection) value).size() < 1;
			} else if (value != null && value.getClass().isArray()) {
				return Array.getLength(value) == 0;
			} else {
				return value == null || "".equals(String.valueOf(value));
			}
		}
	}

}

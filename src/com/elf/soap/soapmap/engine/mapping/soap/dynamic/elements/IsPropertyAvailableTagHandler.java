package com.elf.soap.soapmap.engine.mapping.soap.dynamic.elements;

import java.util.Map;

import com.elf.soap.common.beans.Probe;
import com.elf.soap.common.beans.ProbeFactory;

/**
 * 
 * @author
 * 
 */
public class IsPropertyAvailableTagHandler extends ConditionalTagHandler {

	private static final Probe PROBE = ProbeFactory.getProbe();

	public boolean isCondition(SoapTagContext ctx, SoapTag tag, Object parameterObject) {
		if (parameterObject == null) {
			return false;
		} else if (parameterObject instanceof Map) {
			return ((Map) parameterObject).containsKey(tag.getPropertyAttr());
		} else {
			String property = getResolvedProperty(ctx, tag);
			// if this is a compound property, then we need to get the next to the last
			// value from the parameter object, and then see if there is a readable property
			// for the last value. This logic was added for IBATIS-281 and IBATIS-293
			int lastIndex = property.lastIndexOf('.');
			if (lastIndex != -1) {
				String firstPart = property.substring(0, lastIndex);
				String lastPart = property.substring(lastIndex + 1);
				parameterObject = PROBE.getObject(parameterObject, firstPart);
				property = lastPart;
			}

			if (parameterObject instanceof Map) {
				// we do this because the PROBE always returns true for
				// properties in Maps and that's not the behavior we want here
				return ((Map) parameterObject).containsKey(property);
			} else {
				return PROBE.hasReadableProperty(parameterObject, property);
			}
		}
	}
}

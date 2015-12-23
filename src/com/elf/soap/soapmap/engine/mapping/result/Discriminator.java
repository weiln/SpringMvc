package com.elf.soap.soapmap.engine.mapping.result;

import com.elf.soap.soapmap.engine.impl.SoapMapExecutorDelegate;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: cbegin
 * Date: May 13, 2005
 * Time: 11:11:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class Discriminator {

	private SoapMapExecutorDelegate delegate;
	private ResultMapping resultMapping;
	private Map subMaps;

	public Discriminator(SoapMapExecutorDelegate delegate, ResultMapping resultMapping) {
		this.delegate = delegate;
		this.resultMapping = resultMapping;
	}

	public void setResultMapping(ResultMapping resultMapping) {
		this.resultMapping = resultMapping;
	}

	public ResultMapping getResultMapping() {
		return resultMapping;
	}

	public void addSubMap(String discriminatorValue, String resultMapName) {
		if (subMaps == null) {
			subMaps = new HashMap();
		}
		subMaps.put(discriminatorValue, resultMapName);
	}

	public ResultMap getSubMap(String s) {
		return (ResultMap) subMaps.get(s);
	}

	public void bindSubMaps() {
		if (subMaps != null) {
			Iterator keys = subMaps.keySet().iterator();
			while (keys.hasNext()) {
				Object key = keys.next();
				Object id = subMaps.get(key);
				if (id instanceof String) {
					subMaps.put(key, delegate.getResultMap((String) id));
				}
			}
		}
	}

}

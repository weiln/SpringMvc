package com.elf.soap.soapmap.engine.mapping.statement;

import com.elf.soap.soapmap.client.event.RowHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author
 * 
 */
public class DefaultRowHandler implements RowHandler {

	private List list = new ArrayList();

	public void handleRow(Object valueObject) {
		list.add(valueObject);
	}

	public List getList() {
		return list;
	}

	public void setList(List list) {
		this.list = list;
	}

}

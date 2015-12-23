package com.elf.soap.soapmap.engine.impl;

import com.elf.soap.soapmap.client.SoapMapClient;
import com.elf.soap.soapmap.engine.execution.SoapExecutor;
import com.elf.soap.soapmap.engine.mapping.statement.MappedStatement;
import com.elf.soap.soapmap.engine.mapping.result.ResultObjectFactory;
import com.elf.soap.soapmap.exception.SoapException;

/**
 * @deprecated - this class is uneccessary and should be removed as
 *             soon as possible. Currently spring integration depends on it.
 */
public interface ExtendedSqlMapClient extends SoapMapClient {

	Object execute(String url, String id) throws SoapException;

	Object execute(String url, String id, Object paramObject) throws SoapException;

	MappedStatement getMappedStatement(String id);

	boolean isEnhancementEnabled();

	SoapExecutor getSqlExecutor();

	SoapMapExecutorDelegate getDelegate();

	ResultObjectFactory getResultObjectFactory();

}

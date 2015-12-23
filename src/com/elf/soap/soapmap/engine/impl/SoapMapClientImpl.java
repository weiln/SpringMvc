package com.elf.soap.soapmap.engine.impl;

import com.elf.soap.soapmap.client.SoapMapClient;
import com.elf.soap.soapmap.engine.execution.SoapExecutor;
import com.elf.soap.soapmap.engine.mapping.result.ResultObjectFactory;
import com.elf.soap.soapmap.engine.mapping.statement.MappedStatement;
import com.elf.soap.soapmap.exception.SoapException;

/**
 * Implementation of ExtendedSqlMapClient.
 */
public class SoapMapClientImpl implements SoapMapClient, ExtendedSqlMapClient {

	/**
	 * Delegate for SQL execution
	 */
	public SoapMapExecutorDelegate delegate;

	protected ThreadLocal localSqlMapSession = new ThreadLocal();

	/**
	 * Constructor to supply a delegate
	 * 
	 * @param delegate
	 *            - the delegate
	 */
	public SoapMapClientImpl(SoapMapExecutorDelegate delegate) {
		this.delegate = delegate;
	}

	public Object execute(String url, String id) throws SoapException {
		return getLocalSqlMapSession().execute(url, id);
	}

	public Object execute(String url, String id, Object paramObject) throws SoapException {
		return getLocalSqlMapSession().execute(url, id, paramObject);
	}

	public MappedStatement getMappedStatement(String id) {
		return delegate.getMappedStatement(id);
	}

	public boolean isEnhancementEnabled() {
		return delegate.isEnhancementEnabled();
	}

	public SoapExecutor getSqlExecutor() {
		return delegate.getSqlExecutor();
	}

	public SoapMapExecutorDelegate getDelegate() {
		return delegate;
	}

	protected SoapMapSessionImpl getLocalSqlMapSession() {
		SoapMapSessionImpl sqlMapSession = (SoapMapSessionImpl) localSqlMapSession.get();
		if (sqlMapSession == null || sqlMapSession.isClosed()) {
			sqlMapSession = new SoapMapSessionImpl(this);
			localSqlMapSession.set(sqlMapSession);
		}
		return sqlMapSession;
	}

	public ResultObjectFactory getResultObjectFactory() {
		return delegate.getResultObjectFactory();
	}

}

package com.elf.soap.soapmap.engine.impl;

import com.elf.soap.soapmap.client.SoapMapSession;
import com.elf.soap.soapmap.engine.execution.SoapExecutor;
import com.elf.soap.soapmap.engine.mapping.statement.MappedStatement;
import com.elf.soap.soapmap.engine.scope.SessionScope;
import com.elf.soap.soapmap.exception.SoapException;

/**
 * Implementation of SoapMapSession.
 */
public class SoapMapSessionImpl implements SoapMapSession {

	protected SoapMapExecutorDelegate delegate;
	protected SessionScope sessionScope;
	protected boolean closed;

	/**
	 * Constructor
	 * 
	 * @param client
	 *            - the client that will use the session
	 */
	public SoapMapSessionImpl(SoapMapClientImpl client) {
		this.delegate = client.getDelegate();
		this.sessionScope = this.delegate.beginSessionScope();
		this.sessionScope.setSqlMapClient(client);
		this.sessionScope.setSqlMapExecutor(client);
		this.closed = false;
	}

	/**
	 * Getter to tell if the session is still open
	 * 
	 * @return - the status of the session
	 */
	public boolean isClosed() {
		return closed;
	}

	public void close() {
		if (delegate != null && sessionScope != null) {
			delegate.endSessionScope(sessionScope);
		}
		if (sessionScope != null) {
			sessionScope = null;
		}
		if (delegate != null) {
			delegate = null;
		}
		if (!closed) {
			closed = true;
		}
	}

	public Object execute(String url, String id) throws SoapException {
		return delegate.execute(sessionScope, url, id);
	}

	public Object execute(String url, String id, Object paramObject) throws SoapException {
		return delegate.execute(sessionScope, url, id, paramObject);
	}

	/**
	 * Gets a mapped statement by ID
	 * 
	 * @param id
	 *            - the ID
	 * @return - the mapped statement
	 */
	public MappedStatement getMappedStatement(String id) {
		return delegate.getMappedStatement(id);
	}

	/**
	 * Get the SQL executor
	 * 
	 * @return - the executor
	 */
	public SoapExecutor getSqlExecutor() {
		return delegate.getSqlExecutor();
	}

	/**
	 * Get the delegate
	 * 
	 * @return - the delegate
	 */
	public SoapMapExecutorDelegate getDelegate() {
		return delegate;
	}

}

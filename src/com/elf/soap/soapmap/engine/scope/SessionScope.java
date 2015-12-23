package com.elf.soap.soapmap.engine.scope;

import com.elf.soap.soapmap.client.SoapMapClient;
import com.elf.soap.soapmap.client.SoapMapException;
import com.elf.soap.soapmap.client.SoapMapExecutor;
import com.elf.soap.soapmap.engine.impl.SoapMapExecutorDelegate;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Session based implementation of the Scope interface.
 * 
 * @author
 * 
 */
public class SessionScope {

	private static final Logger logger = LoggerFactory.getLogger(SessionScope.class);

	private static long nextId;
	private long id;
	// Used by Any
	private SoapMapClient sqlMapClient;
	private SoapMapExecutor sqlMapExecutor;
	private int requestStackDepth;
	// Used by StandardSqlMapClient and GeneralStatement
	private boolean inBatch;
	// Used by SqlExecutor
	private Object batch;
	private boolean commitRequired;
	private Map preparedStatements;

	/**
	 * Default constructor.
	 */
	public SessionScope() {
		this.preparedStatements = new HashMap();
		this.inBatch = false;
		this.requestStackDepth = 0;
		this.id = getNextId();
	}

	/**
	 * Get the SqlMapClient for the session.
	 * 
	 * @return - the SqlMapClient
	 */
	public SoapMapClient getSqlMapClient() {
		return sqlMapClient;
	}

	/**
	 * Set the SqlMapClient for the session
	 * 
	 * @param sqlMapClient
	 *            - the SqlMapClient
	 */
	public void setSqlMapClient(SoapMapClient sqlMapClient) {
		this.sqlMapClient = sqlMapClient;
	}

	/**
	 * Get the SQL executor for the session
	 * 
	 * @return - the SQL executor
	 */
	public SoapMapExecutor getSqlMapExecutor() {
		return sqlMapExecutor;
	}

	/**
	 * Get the SQL executor for the session
	 * 
	 * @param sqlMapExecutor
	 *            - the SQL executor
	 */
	public void setSqlMapExecutor(SoapMapExecutor sqlMapExecutor) {
		this.sqlMapExecutor = sqlMapExecutor;
	}

	/**
	 * Tells us if we are in batch mode or not
	 * 
	 * @return - true if we are working with a batch
	 */
	public boolean isInBatch() {
		return inBatch;
	}

	/**
	 * Turn batch mode on or off
	 * 
	 * @param inBatch
	 *            - the switch
	 */
	public void setInBatch(boolean inBatch) {
		this.inBatch = inBatch;
	}

	/**
	 * Getter for the batch of the session
	 * 
	 * @return - the batch
	 */
	public Object getBatch() {
		return batch;
	}

	/**
	 * Stter for the batch of the session
	 * 
	 * @param batch
	 *            the new batch
	 */
	public void setBatch(Object batch) {
		this.batch = batch;
	}

	/**
	 * Get the request stack depth
	 * 
	 * @return - the stack depth
	 */
	public int getRequestStackDepth() {
		return requestStackDepth;
	}

	/**
	 * Increment the stack depth by one.
	 */
	public void incrementRequestStackDepth() {
		requestStackDepth++;
	}

	/**
	 * Decrement the stack depth by one.
	 */
	public void decrementRequestStackDepth() {
		requestStackDepth--;
	}

	/**
	 * Getter to tell if a commit is required for the session
	 * 
	 * @return - true if a commit is required
	 */
	public boolean isCommitRequired() {
		return commitRequired;
	}

	/**
	 * Setter to tell the session that a commit is required for the session
	 * 
	 * @param commitRequired
	 *            - the flag
	 */
	public void setCommitRequired(boolean commitRequired) {
		this.commitRequired = commitRequired;
	}

	public boolean hasPreparedStatementFor(String sql) {
		return preparedStatements.containsKey(sql);
	}

	public boolean hasPreparedStatement(PreparedStatement ps) {
		return preparedStatements.containsValue(ps);
	}

	public PreparedStatement getPreparedStatement(String sql) throws SQLException {
		if (!hasPreparedStatementFor(sql)) {
			throw new SoapMapException("Could not get prepared statement.  This is likely a bug.");
		}
		return (PreparedStatement) preparedStatements.get(sql);
	}

	public void putPreparedStatement(SoapMapExecutorDelegate delegate, String sql, PreparedStatement ps) {
		if (delegate.isStatementCacheEnabled()) {
			if (!isInBatch()) {
				if (hasPreparedStatementFor(sql))
					throw new SoapMapException("Duplicate prepared statement found.  This is likely a bug.");
				preparedStatements.put(sql, ps);
			}
		}
	}

	public void closePreparedStatements() {
		Iterator keys = preparedStatements.keySet().iterator();
		while (keys.hasNext()) {
			PreparedStatement ps = (PreparedStatement) preparedStatements.get(keys.next());
			try {
				ps.close();
			} catch (Exception e) {
				// ignore -- we don't care if this fails at this point.
				logger.error(e.getMessage());
			}
		}
		preparedStatements.clear();
	}

	public void cleanup() {
		closePreparedStatements();
		preparedStatements.clear();
	}

	public boolean equals(Object parameterObject) {
		if (this == parameterObject)
			return true;
		if (!(parameterObject instanceof SessionScope))
			return false;
		final SessionScope sessionScope = (SessionScope) parameterObject;
		if (id != sessionScope.id)
			return false;
		return true;
	}

	public int hashCode() {
		return (int) (id ^ (id >>> 32));
	}

	/**
	 * Method to get a unique ID.
	 * 
	 * @return - the new ID
	 */
	public static synchronized long getNextId() {
		return nextId++;
	}

}

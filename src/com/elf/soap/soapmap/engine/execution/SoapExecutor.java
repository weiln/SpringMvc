package com.elf.soap.soapmap.engine.execution;

import com.elf.soap.common.logging.Log;
import com.elf.soap.common.logging.LogFactory;
import com.elf.soap.soapmap.engine.mapping.result.ResultMap;
import com.elf.soap.soapmap.engine.mapping.result.ResultObjectFactoryUtil;
import com.elf.soap.soapmap.engine.mapping.statement.MappedStatement;
import com.elf.soap.soapmap.engine.mapping.statement.RowHandlerCallback;
import com.elf.soap.soapmap.engine.scope.ErrorContext;
import com.elf.soap.soapmap.engine.scope.StatementScope;
import com.elf.soap.soapmap.engine.scope.SessionScope;
import com.elf.soap.soapmap.engine.impl.SoapMapClientImpl;
import com.elf.soap.soapmap.engine.impl.SoapMapExecutorDelegate;
import com.elf.soap.soapmap.engine.mapping.statement.DefaultRowHandler;
import com.elf.soap.soapmap.exception.SoapException;
import com.elf.soap.soapmap.util.SoapMapUtil;
import com.elf.soap.soapmap.util.HttpUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Class responsible for executing the SOAP.
 * 
 * @author
 * 
 */
public class SoapExecutor {

	private static final Log log = LogFactory.getLog(SoapExecutor.class);

	//
	// Constants
	//
	/**
	 * Constant to let us know not to skip anything.
	 */
	public static final int NO_SKIPPED_RESULTS = 0;
	/**
	 * Constant to let us know to include all records.
	 */
	public static final int NO_MAXIMUM_RESULTS = -999999;

	//
	// Public Methods
	//

	public void execute(StatementScope statementScope, String url, String soap, RowHandlerCallback callback)
		throws SoapException {
		ErrorContext errorContext = statementScope.getErrorContext();
		errorContext.setActivity("executing query");
		errorContext.setObjectId(soap);

		setupResultObjectFactory(statementScope);

		try {
			String result = HttpUtil.post(url,soap);
			if (log.isDebugEnabled()) {
				log.debug("post url: " + url);
				log.debug("post soap: " + soap);
				log.debug("post result:" + result);
			}

			Object object = null;

			ResultMap map = statementScope.getResultMap();
			if (map == null) {
				object = SoapMapUtil.parse(result);
			} else {
				object = SoapMapUtil.parse(result, map.getResultClass().newInstance());
			}

			DefaultRowHandler defaultRowHandler = ((DefaultRowHandler) callback.getRowHandler());
			defaultRowHandler.getList().add(object);
		} catch (InstantiationException e) {
			throw new SoapException("InstantiationException:", e);
		} catch (IllegalAccessException e) {
			throw new SoapException("IllegalAccessException:", e);
		} catch (Exception e) {
			throw new SoapException("HttpUtil.post:", e);
		}
	}

	/**
	 * Long form of the method to execute a query.
	 * 
	 * @param statementScope
	 *            - the request scope
	 * @param conn
	 *            - the database connection
	 * @param sql
	 *            - the SQL statement to execute
	 * @param parameters
	 *            - the parameters for the statement
	 * @param skipResults
	 *            - the number of results to skip
	 * @param maxResults
	 *            - the maximum number of results to return
	 * @param callback
	 *            - the row handler for the query
	 * @throws SQLException
	 *             - if the query fails
	 */
	public void executeQuery(StatementScope statementScope, Connection conn, String sql, Object[] parameters,
		int skipResults, int maxResults, RowHandlerCallback callback) throws SQLException {
		ErrorContext errorContext = statementScope.getErrorContext();
		errorContext.setActivity("executing query");
		errorContext.setObjectId(sql);
		PreparedStatement ps = null;
		ResultSet rs = null;
		setupResultObjectFactory(statementScope);
		try {
			errorContext.setMoreInfo("Check the SQL Statement (preparation failed).");
			Integer rsType = statementScope.getStatement().getResultSetType();
			if (rsType != null) {
				ps = prepareStatement(statementScope.getSession(), conn, sql, rsType);
			} else {
				ps = prepareStatement(statementScope.getSession(), conn, sql);
			}
			setStatementTimeout(statementScope.getStatement(), ps);
			Integer fetchSize = statementScope.getStatement().getFetchSize();
			if (fetchSize != null) {
				ps.setFetchSize(fetchSize.intValue());
			}
			errorContext.setMoreInfo("Check the parameters (set parameters failed).");
			statementScope.getParameterMap().setParameters(statementScope, ps, parameters);
			errorContext.setMoreInfo("Check the statement (query failed).");
			ps.execute();
			errorContext.setMoreInfo("Check the results (failed to retrieve results).");

			// Begin ResultSet Handling
			rs = handleMultipleResults(ps, statementScope, skipResults, maxResults, callback);
			// End ResultSet Handling
		} finally {
			try {
				closeResultSet(rs);
			} finally {
				closeStatement(statementScope.getSession(), ps);
			}
		}

	}

	private ResultSet handleMultipleResults(PreparedStatement ps, StatementScope statementScope, int skipResults,
		int maxResults, RowHandlerCallback callback) throws SQLException {
		ResultSet rs;
		rs = getFirstResultSet(statementScope, ps);
		if (rs != null) {
			handleResults(statementScope, rs, skipResults, maxResults, callback);
		}

		// Multiple ResultSet handling
		if (callback.getRowHandler() instanceof DefaultRowHandler) {
			MappedStatement statement = statementScope.getStatement();
			DefaultRowHandler defaultRowHandler = ((DefaultRowHandler) callback.getRowHandler());
			if (statement.hasMultipleResultMaps()) {
				List multipleResults = new ArrayList();
				multipleResults.add(defaultRowHandler.getList());
				ResultMap[] resultMaps = statement.getAdditionalResultMaps();
				int i = 0;
				while (moveToNextResultsSafely(statementScope, ps)) {
					if (i >= resultMaps.length)
						break;
					ResultMap rm = resultMaps[i];
					statementScope.setResultMap(rm);
					rs = ps.getResultSet();
					DefaultRowHandler rh = new DefaultRowHandler();
					handleResults(statementScope, rs, skipResults, maxResults, new RowHandlerCallback(rm, null, rh));
					multipleResults.add(rh.getList());
					i++;
				}
				defaultRowHandler.setList(multipleResults);
				statementScope.setResultMap(statement.getResultMap());
			} else {
				while (moveToNextResultsSafely(statementScope, ps)) {
				}
			}
		}
		// End additional ResultSet handling
		return rs;
	}

	private ResultSet getFirstResultSet(StatementScope scope, Statement stmt) throws SQLException {
		ResultSet rs = null;
		boolean hasMoreResults = true;
		while (hasMoreResults) {
			rs = stmt.getResultSet();
			if (rs != null) {
				break;
			}
			hasMoreResults = moveToNextResultsIfPresent(scope, stmt);
		}
		return rs;
	}

	private boolean moveToNextResultsIfPresent(StatementScope scope, Statement stmt) throws SQLException {
		boolean moreResults;
		// This is the messed up JDBC approach for determining if there are more results
		moreResults = !(((moveToNextResultsSafely(scope, stmt) == false) && (stmt.getUpdateCount() == -1)));
		return moreResults;
	}

	private boolean moveToNextResultsSafely(StatementScope scope, Statement stmt) throws SQLException {
		if (forceMultipleResultSetSupport(scope) || stmt.getConnection().getMetaData().supportsMultipleResultSets()) {
			return stmt.getMoreResults();
		}
		return false;
	}

	private boolean forceMultipleResultSetSupport(StatementScope scope) {
		return ((SoapMapClientImpl) scope.getSession().getSqlMapClient()).getDelegate()
			.isForceMultipleResultSetSupport();
	}

	private void handleResults(StatementScope statementScope, ResultSet rs, int skipResults, int maxResults,
		RowHandlerCallback callback) throws SQLException {
		try {
			statementScope.setResultSet(rs);
			ResultMap resultMap = statementScope.getResultMap();
			if (resultMap != null) {
				// Skip Results
				if (rs.getType() != ResultSet.TYPE_FORWARD_ONLY) {
					if (skipResults > 0) {
						rs.absolute(skipResults);
					}
				} else {
					for (int i = 0; i < skipResults; i++) {
						if (!rs.next()) {
							return;
						}
					}
				}

				// Get Results
				int resultsFetched = 0;
				while ((maxResults == SoapExecutor.NO_MAXIMUM_RESULTS || resultsFetched < maxResults) && rs.next()) {
					Object[] columnValues = resultMap.resolveSubMap(statementScope, rs).getResults(statementScope, rs);
					callback.handleResultObject(statementScope, columnValues, rs);
					resultsFetched++;
				}
			}
		} finally {
			statementScope.setResultSet(null);
		}
	}

	private PreparedStatement prepareStatement(SessionScope sessionScope, Connection conn, String sql, Integer rsType)
		throws SQLException {
		SoapMapExecutorDelegate delegate = ((SoapMapClientImpl) sessionScope.getSqlMapExecutor()).getDelegate();
		if (sessionScope.hasPreparedStatementFor(sql)) {
			return sessionScope.getPreparedStatement((sql));
		} else {
			PreparedStatement ps = conn.prepareStatement(sql, rsType.intValue(), ResultSet.CONCUR_READ_ONLY);
			sessionScope.putPreparedStatement(delegate, sql, ps);
			return ps;
		}
	}

	private static PreparedStatement prepareStatement(SessionScope sessionScope, Connection conn, String sql)
		throws SQLException {
		SoapMapExecutorDelegate delegate = ((SoapMapClientImpl) sessionScope.getSqlMapExecutor()).getDelegate();
		if (sessionScope.hasPreparedStatementFor(sql)) {
			return sessionScope.getPreparedStatement((sql));
		} else {
			PreparedStatement ps = conn.prepareStatement(sql);
			sessionScope.putPreparedStatement(delegate, sql, ps);
			return ps;
		}
	}

	private static void closeStatement(SessionScope sessionScope, PreparedStatement ps) {
		if (ps != null) {
			if (!sessionScope.hasPreparedStatement(ps)) {
				try {
					ps.close();
				} catch (SQLException e) {
					// ignore
					log.error("closeStatement", e);
				}
			}
		}
	}

	/**
	 * @param rs
	 */
	private static void closeResultSet(ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				// ignore
				log.error("closeResultSet", e);
			}
		}
	}

	private static void setStatementTimeout(MappedStatement mappedStatement, Statement statement) throws SQLException {
		if (mappedStatement.getTimeout() != null) {
			statement.setQueryTimeout(mappedStatement.getTimeout().intValue());
		}
	}

	private void setupResultObjectFactory(StatementScope statementScope) {
		SoapMapClientImpl client = (SoapMapClientImpl) statementScope.getSession().getSqlMapClient();
		ResultObjectFactoryUtil.setResultObjectFactory(client.getResultObjectFactory());
		ResultObjectFactoryUtil.setStatementId(statementScope.getStatement().getId());
	}

}

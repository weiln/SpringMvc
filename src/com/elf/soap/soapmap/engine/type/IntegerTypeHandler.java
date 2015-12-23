package com.elf.soap.soapmap.engine.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Integer Decimal implementation of TypeHandler.
 * 
 * @author
 * 
 */
public class IntegerTypeHandler extends BaseTypeHandler implements TypeHandler {

	public void setParameter(PreparedStatement ps, int i, Object parameter, String jdbcType) throws SQLException {
		ps.setInt(i, ((Integer) parameter).intValue());
	}

	public Object getResult(ResultSet rs, String columnName) throws SQLException {
		int i = rs.getInt(columnName);
		if (rs.wasNull()) {
			return null;
		} else {
			return Integer.valueOf(i);
		}
	}

	public Object getResult(ResultSet rs, int columnIndex) throws SQLException {
		int i = rs.getInt(columnIndex);
		if (rs.wasNull()) {
			return null;
		} else {
			return Integer.valueOf(i);
		}
	}

	public Object getResult(CallableStatement cs, int columnIndex) throws SQLException {
		int i = cs.getInt(columnIndex);
		if (cs.wasNull()) {
			return null;
		} else {
			return Integer.valueOf(i);
		}
	}

	public Object valueOf(String s) {
		return Integer.valueOf(s);
	}

}

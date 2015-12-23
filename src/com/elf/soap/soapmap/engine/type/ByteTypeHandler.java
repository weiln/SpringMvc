package com.elf.soap.soapmap.engine.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Byte implementation of TypeHandler.
 * 
 * @author
 * 
 */
public class ByteTypeHandler extends BaseTypeHandler implements TypeHandler {

	public void setParameter(PreparedStatement ps, int i, Object parameter, String jdbcType) throws SQLException {
		ps.setByte(i, ((Byte) parameter).byteValue());
	}

	public Object getResult(ResultSet rs, String columnName) throws SQLException {
		byte b = rs.getByte(columnName);
		if (rs.wasNull()) {
			return null;
		} else {
			return Byte.valueOf(b);
		}
	}

	public Object getResult(ResultSet rs, int columnIndex) throws SQLException {
		byte b = rs.getByte(columnIndex);
		if (rs.wasNull()) {
			return null;
		} else {
			return Byte.valueOf(b);
		}
	}

	public Object getResult(CallableStatement cs, int columnIndex) throws SQLException {
		byte b = cs.getByte(columnIndex);
		if (cs.wasNull()) {
			return null;
		} else {
			return Byte.valueOf(b);
		}
	}

	public Object valueOf(String s) {
		return Byte.valueOf(s);
	}

}

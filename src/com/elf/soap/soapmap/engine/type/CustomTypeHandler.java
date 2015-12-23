package com.elf.soap.soapmap.engine.type;

import com.elf.soap.soapmap.client.extensions.ParameterSetter;
import com.elf.soap.soapmap.client.extensions.ResultGetter;
import com.elf.soap.soapmap.client.extensions.TypeHandlerCallback;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Custom type handler for adding a TypeHandlerCallback.
 * 
 * @author
 * 
 */
public class CustomTypeHandler extends BaseTypeHandler implements TypeHandler {

	private TypeHandlerCallback callback;

	/**
	 * Constructor to provide a TypeHandlerCallback instance
	 * 
	 * @param callback
	 *            - the TypeHandlerCallback instance
	 */
	public CustomTypeHandler(TypeHandlerCallback callback) {
		this.callback = callback;
	}

	public void setParameter(PreparedStatement ps, int i, Object parameter, String jdbcType) throws SQLException {
		ParameterSetter setter = new ParameterSetterImpl(ps, i);
		callback.setParameter(setter, parameter);
	}

	public Object getResult(ResultSet rs, String columnName) throws SQLException {
		ResultGetter getter = new ResultGetterImpl(rs, columnName);
		return callback.getResult(getter);
	}

	public Object getResult(ResultSet rs, int columnIndex) throws SQLException {
		ResultGetter getter = new ResultGetterImpl(rs, columnIndex);
		return callback.getResult(getter);
	}

	public Object valueOf(String s) {
		return callback.valueOf(s);
	}

}

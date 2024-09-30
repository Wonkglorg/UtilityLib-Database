package com.wonkglorg.utilitylib.database.datatypes;

import com.wonkglorg.util.interfaces.functional.database.DataTypeHandler;
import com.wonkglorg.util.ip.IPv4;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TypeHandlerIpv4 implements DataTypeHandler<IPv4> {
	@Override
	public void setParameter(PreparedStatement statement, int index, Object value)
			throws SQLException {
		statement.setString(index, value.toString());
	}

	@Override
	public IPv4 getParameter(ResultSet resultSet, int index) throws SQLException {
		return IPv4.of(resultSet.getString(index));
	}

	@Override
	public IPv4 getParameter(ResultSet resultSet, String columnName) throws SQLException {
		return IPv4.of(resultSet.getString(columnName));
	}
}

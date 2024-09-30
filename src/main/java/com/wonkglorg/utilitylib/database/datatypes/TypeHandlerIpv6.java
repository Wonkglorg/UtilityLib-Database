package com.wonkglorg.utilitylib.database.datatypes;

import com.wonkglorg.util.interfaces.functional.database.DataTypeHandler;
import com.wonkglorg.util.ip.IPv6;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TypeHandlerIpv6 implements DataTypeHandler<IPv6> {
	@Override
	public void setParameter(PreparedStatement statement, int index, Object value)
			throws SQLException {
		statement.setString(index, value.toString());
	}

	@Override
	public IPv6 getParameter(ResultSet resultSet, int index) throws SQLException {
		return IPv6.of(resultSet.getString(index));
	}

	@Override
	public IPv6 getParameter(ResultSet resultSet, String columnName) throws SQLException {
		return IPv6.of(resultSet.getString(columnName));
	}
}

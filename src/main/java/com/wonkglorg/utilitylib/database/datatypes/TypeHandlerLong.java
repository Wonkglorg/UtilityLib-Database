package com.wonkglorg.utilitylib.database.datatypes;

import com.wonkglorg.util.interfaces.functional.database.DataTypeHandler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TypeHandlerLong implements DataTypeHandler<Long> {
    @Override
    public void setParameter(PreparedStatement statement, int index, Object value) throws SQLException {
        statement.setLong(index, (Long) value);
    }

    @Override
    public Long getParameter(ResultSet resultSet, int index) throws SQLException {
        return resultSet.getLong(index);
    }

    @Override
    public Long getParameter(ResultSet resultSet, String columnName) throws SQLException {
        return resultSet.getLong(columnName);
    }
}

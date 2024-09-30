package com.wonkglorg.utilitylib.database.datatypes;

import com.wonkglorg.util.interfaces.functional.database.DataTypeHandler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TypeHandlerShort implements DataTypeHandler<Short> {
    @Override
    public void setParameter(PreparedStatement statement, int index, Object value) throws SQLException {
        statement.setShort(index, (Short) value);
    }

    @Override
    public Short getParameter(ResultSet resultSet, int index) throws SQLException {
        return resultSet.getShort(index);
    }

    @Override
    public Short getParameter(ResultSet resultSet, String columnName) throws SQLException {
        return resultSet.getShort(columnName);
    }
}

package com.wonkglorg.utilitylib.database.datatypes;

import com.wonkglorg.util.interfaces.functional.database.DataTypeHandler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TypeHandlerObject implements DataTypeHandler<Object> {

    @Override
    public void setParameter(PreparedStatement statement, int index, Object value) throws SQLException {
        statement.setObject(index, value);
    }

    @Override
    public Object getParameter(ResultSet resultSet, int index) throws SQLException {
        return resultSet.getObject(index);
    }

    @Override
    public Object getParameter(ResultSet resultSet, String columnName) throws SQLException {
        return resultSet.getObject(columnName);
    }
}

package com.wonkglorg.utilitylib.database.datatypes;

import com.wonkglorg.util.interfaces.functional.database.DataTypeHandler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TypeHandlerString implements DataTypeHandler<String> {

    @Override
    public void setParameter(PreparedStatement statement, int index, Object value) throws SQLException {
        statement.setString(index, (String) value);
    }

    @Override
    public String getParameter(ResultSet resultSet, int index) throws SQLException {
        return resultSet.getString(index);
    }

    @Override
    public String getParameter(ResultSet resultSet, String columnName) throws SQLException {
        return resultSet.getString(columnName);
    }
}

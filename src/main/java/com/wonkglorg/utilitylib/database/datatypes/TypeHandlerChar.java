package com.wonkglorg.utilitylib.database.datatypes;

import com.wonkglorg.util.interfaces.functional.database.DataTypeHandler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TypeHandlerChar implements DataTypeHandler<Character> {
    @Override
    public void setParameter(PreparedStatement statement, int index, Object value) throws SQLException {
        statement.setString(index, value.toString());
    }

    @Override
    public Character getParameter(ResultSet resultSet, int index) throws SQLException {
        var value = resultSet.getString(index);
        if (value == null || value.isEmpty()) {
            return null;
        }
        return value.charAt(0);
    }

    @Override
    public Character getParameter(ResultSet resultSet, String columnName) throws SQLException {
        var value = resultSet.getString(columnName);
        if (value == null || value.isEmpty()) {
            return null;
        }
        return value.charAt(0);
    }
}

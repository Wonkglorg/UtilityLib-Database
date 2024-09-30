package com.wonkglorg.utilitylib.database.datatypes;

import com.wonkglorg.util.interfaces.functional.database.DataTypeHandler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TypeHandlerBoolean implements DataTypeHandler<Boolean> {
    @Override
    public void setParameter(PreparedStatement statement, int index, Object value) throws SQLException {
        statement.setBoolean(index, (Boolean) value);
    }

    @Override
    public Boolean getParameter(ResultSet resultSet, int index) throws SQLException {
        return resultSet.getBoolean(index);
    }

    @Override
    public Boolean getParameter(ResultSet resultSet, String columnName) throws SQLException {
        return resultSet.getBoolean(columnName);
    }
}

package com.wonkglorg.utilitylib.database.datatypes;

import com.wonkglorg.util.interfaces.functional.database.DataTypeHandler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TypeHandlerInteger implements DataTypeHandler<Integer> {
    @Override
    public void setParameter(PreparedStatement statement, int index, Object value) throws SQLException {
        statement.setInt(index, (Integer) value);
    }

    @Override
    public Integer getParameter(ResultSet resultSet, int index) throws SQLException {
        return resultSet.getInt(index);
    }

    @Override
    public Integer getParameter(ResultSet resultSet, String columnName) throws SQLException {
        return resultSet.getInt(columnName);
    }
}

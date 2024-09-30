package com.wonkglorg.utilitylib.database.datatypes;

import com.wonkglorg.util.interfaces.functional.database.DataTypeHandler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TypeHandlerDouble implements DataTypeHandler<Double> {
    @Override
    public void setParameter(PreparedStatement statement, int index, Object value) throws SQLException {
        statement.setDouble(index, (Double) value);
    }

    @Override
    public Double getParameter(ResultSet resultSet, int index) throws SQLException {
        return resultSet.getDouble(index);
    }

    @Override
    public Double getParameter(ResultSet resultSet, String columnName) throws SQLException {
        return resultSet.getDouble(columnName);
    }
}
